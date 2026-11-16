package slop.miho.chumpy.sub

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import slop.miho.chumpy.db.ChumpyDatabase
import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType
import slop.miho.chumpy.db.SubscriptionEntity
import slop.miho.chumpy.db.balancerMemberKeys
import slop.miho.chumpy.db.computeLinkKey
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Owns subscriptions and their profiles: adding, refreshing, deleting and the manual
 * import path. A refresh replaces the subscription's profile rows wholesale, but profiles
 * whose [slop.miho.chumpy.db.ProfileEntity.linkKey] survives carry their identity — and
 * with it the selection state and the last measured ping — over to the new rows.
 *
 * Each subscription with auto-update on gets one unique periodic WorkManager job, its
 * interval taken from the panel's `profile-update-interval` header when present.
 */
class SubscriptionRepository(
    private val context: Context,
    private val db: ChumpyDatabase,
    private val tunnelUp: () -> Boolean,
    private val happSpoof: () -> HappSpoof.Fingerprint?,
) {
    private companion object {
        const val TAG = "Chumpy/SubRepo"
        const val DEFAULT_UPDATE_HOURS = 24L
        const val MIN_UPDATE_HOURS = 1L
        const val MAX_UPDATE_HOURS = 24L * 14
        const val WORK_PREFIX = "subscription-update-"
    }

    private val refreshLocks = ConcurrentHashMap<String, Mutex>()

    private val fetcher = SubscriptionFetcher(tunnelUp, happSpoof)
    private val subscriptions = db.subscriptionDao()
    private val profiles = db.profileDao()

    fun subscriptions(): Flow<List<SubscriptionEntity>> = subscriptions.all()

    /** The manual (subscription-less) profile count, as a flow — the setup gate watches it. */
    val manualProfileCount: Flow<Int> = profiles.manualCount()

    fun profilesOf(subscriptionId: String?): Flow<List<ProfileEntity>> =
        profiles.bySubscription(subscriptionId)

    suspend fun subscriptionById(id: String): SubscriptionEntity? = subscriptions.byId(id)

    suspend fun profileById(id: String): ProfileEntity? = profiles.byId(id)

    suspend fun profileCount(): Int = profiles.count()

    /** Fetches, parses and stores a new subscription; the URL is the identity. */
    suspend fun addSubscription(
        url: String,
        name: String? = null,
        filterRegex: String = "",
        customUserAgent: String = "",
        updateIntervalMinutes: Int? = null,
    ): Result<SubscriptionEntity> =
        withContext(Dispatchers.IO) {
            runCatching {
                val useMirrors = slop.miho.chumpy.data.ConnectionStore.current(context).cdnMirrorFallback
                val (body, metadata) = fetchWithNestedExpansion(
                    url = url,
                    userAgentOverride = customUserAgent.ifBlank { null },
                    useCdnMirrors = useMirrors,
                )
                val headerSources = GeodataSources.parseSafely(metadata.geodataSources)
                val allParsed = SubscriptionParser.parse(body).map {
                    it.copy(geodataSources = GeodataSources.merge(headerSources, it.geodataSources))
                }
                val parsed = applyFilterRegex(allParsed, filterRegex)
                require(parsed.isNotEmpty()) { "No server links found (or all excluded by filter)" }

                val id = UUID.randomUUID().toString()
                val entity = SubscriptionEntity(
                    id = id,
                    name = name?.takeIf { it.isNotBlank() }
                        ?: metadata.title
                        ?: fetcher.hostNameOf(url),
                    url = url.trim(),
                    lastUpdateAttempt = System.currentTimeMillis(),
                    lastUpdateSuccess = System.currentTimeMillis(),
                    updateIntervalHours = metadata.updateIntervalHours,
                    uploadBytes = metadata.uploadBytes,
                    downloadBytes = metadata.downloadBytes,
                    totalBytes = metadata.totalBytes,
                    expireEpochSeconds = metadata.expireEpochSeconds,
                    logoPath = refreshLogo(id, metadata.logoUrl, previousPath = null),
                    filterRegex = filterRegex.trim(),
                    customUserAgent = customUserAgent.trim(),
                    updateIntervalMinutes = updateIntervalMinutes,
                )
                db.withTransaction {
                    subscriptions.upsert(entity)
                    profiles.replaceProfiles(entity.id, stamped(parsed, entity.id, oldProfiles = emptyList()))
                }
                scheduleAutoUpdate(entity)
                Log.i(TAG, "Added subscription ${entity.id} with ${parsed.size} profiles")
                entity
            }.onFailure { logFailure("add", null, it) }
        }

    /** Re-fetches a subscription and swaps its profiles in place. */
    suspend fun updateSubscription(id: String): Result<SubscriptionEntity> =
        withContext(Dispatchers.IO) {
            refreshLocks.getOrPut(id) { Mutex() }.withLock {
                runCatching {
                    val entity = subscriptions.byId(id) ?: error("Subscription not found")
                    val attemptAt = System.currentTimeMillis()
                    try {
                        val useMirrors = slop.miho.chumpy.data.ConnectionStore.current(context).cdnMirrorFallback
                        val (body, metadata) = fetchWithNestedExpansion(
                            url = entity.url,
                            userAgentOverride = entity.customUserAgent.ifBlank { null },
                            useCdnMirrors = useMirrors,
                        )
                        val headerSources = GeodataSources.parseSafely(metadata.geodataSources)
                        val allParsed = SubscriptionParser.parse(body).map {
                            it.copy(geodataSources = GeodataSources.merge(headerSources, it.geodataSources))
                        }
                        val parsed = applyFilterRegex(allParsed, entity.filterRegex)
                        require(parsed.isNotEmpty()) { "No server links found (or all excluded by filter)" }

                        val logoPath = refreshLogo(id, metadata.logoUrl, previousPath = entity.logoPath)
                        val updated = db.withTransaction {
                            val current = subscriptions.byId(id) ?: error("Subscription not found")
                            val old = profiles.bySubscriptionOnce(id)
                            val updated = current.copy(
                                name = metadata.title ?: current.name,
                                lastUpdateAttempt = attemptAt,
                                lastUpdateSuccess = attemptAt,
                                updateIntervalHours = metadata.updateIntervalHours,
                                uploadBytes = metadata.uploadBytes,
                                downloadBytes = metadata.downloadBytes,
                                totalBytes = metadata.totalBytes,
                                expireEpochSeconds = metadata.expireEpochSeconds,
                                logoPath = logoPath,
                                lastError = null,
                            )
                            subscriptions.upsert(updated)
                            profiles.replaceProfiles(id, stamped(parsed, id, old))
                            updated
                        }
                        scheduleAutoUpdate(updated)
                        Log.i(TAG, "Updated subscription $id: ${parsed.size} profiles")
                        updated
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        subscriptions.setUpdateError(id, attemptAt, e.message ?: e.javaClass.simpleName)
                        throw e
                    }
                }.onFailure { logFailure("update", id, it) }
            }
        }

    suspend fun updateSubscriptionEditable(
        id: String,
        name: String,
        url: String,
        filterRegex: String,
        customUserAgent: String,
        updateIntervalMinutes: Int?,
        autoUpdate: Boolean,
    ) = withContext(Dispatchers.IO) {
        val current = subscriptions.byId(id) ?: return@withContext
        subscriptions.updateEditable(
            id = id,
            name = name.trim().ifBlank { current.name },
            url = url.trim().ifBlank { current.url },
            filterRegex = filterRegex.trim(),
            customUserAgent = customUserAgent.trim(),
            updateIntervalMinutes = updateIntervalMinutes,
            autoUpdate = autoUpdate,
        )
        val refreshed = subscriptions.byId(id) ?: return@withContext
        if (refreshed.autoUpdate) scheduleAutoUpdate(refreshed)
        else WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + id)
    }

    suspend fun prepareGeodata(profile: ProfileEntity, config: String, importedRouting: Boolean): String =
        slop.miho.chumpy.data.GeodataFiles.prepare(context, config, profile.geodataSources, importedRouting, fetcher::fetchGeodata)

    suspend fun deleteSubscription(id: String) = withContext(Dispatchers.IO) {
        subscriptions.delete(id)
        logoFile(id).delete()
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + id)
        Log.i(TAG, "Deleted subscription $id")
    }

    suspend fun setSubscriptionAutoUpdate(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        subscriptions.byId(id)?.let { sub ->
            subscriptions.setAutoUpdate(id, enabled)
            if (enabled) scheduleAutoUpdate(sub.copy(autoUpdate = true)) else {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + id)
            }
        }
    }

    /**
     * Manual import: one or more share links (one per line), a WireGuard .conf body, or
     * a whole raw Xray client config — object or array — whose proxy outbounds become
     * the group's rows. Returns the number of profiles actually stored — duplicates are
     * skipped.
     */
    suspend fun addManualProfiles(text: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val drafts = when {
                text.trim().startsWith("[Interface]", ignoreCase = true) ->
                    listOfNotNull(LinkParsers.parseWireguardConf(text))
                else -> {
                    SubscriptionParser.parse(text)
                }
            }
            require(drafts.isNotEmpty()) { "No recognizable server link found" }
            db.withTransaction {
                val old = profiles.bySubscriptionOnce(null)
                val keyed = drafts.associateBy { it.computeLinkKey() }
                val existing = old.map { it.linkKey }.toSet()
                val fresh = keyed.filterKeys { it !in existing }.values.toList()
                var changed = 0
                val updated = old.map { row ->
                    val draft = keyed[row.linkKey]
                    when {
                        draft != null && row.type == ProfileType.BALANCER -> row.copy(
                            balancerMembers = draft.balancerMembers, routingRules = draft.routingRules, geodataSources = draft.geodataSources,
                        ).also { if (it != row) changed++ }
                        draft != null && row.hidden && !draft.hidden -> row.copy(
                            hidden = false, remarks = draft.remarks, sourceLink = draft.sourceLink,
                            routingRules = draft.routingRules, geodataSources = draft.geodataSources,
                        ).also { changed++ }
                        draft != null && (row.routingRules != draft.routingRules || row.geodataSources != draft.geodataSources) ->
                            row.copy(routingRules = draft.routingRules, geodataSources = draft.geodataSources).also { changed++ }
                        else -> row
                    }
                }
                if (fresh.isNotEmpty() || changed > 0) {
                    val nextOrder = (old.maxOfOrNull { it.order } ?: -1L) + 1
                    val combined = updated + stamped(fresh, null, oldProfiles = emptyList()).mapIndexed { index, row ->
                        row.copy(order = nextOrder + index)
                    }
                    val referenced = combined.filter { it.type == ProfileType.BALANCER }.flatMap { it.balancerMemberKeys }.toSet()
                    profiles.replaceProfiles(null, combined.filter { !it.hidden || it.linkKey in referenced })
                }
                fresh.size + changed
            }
        }.onFailure { logFailure("import", null, it) }
    }

    suspend fun deleteManualProfile(id: String) = withContext(Dispatchers.IO) {
        // Manual profiles are few; replace-all is the simplest transactional delete.
        db.withTransaction {
            val old = profiles.bySubscriptionOnce(null)
            val remaining = old.filter { it.id != id }
            val referenced = remaining.filter { it.type == ProfileType.BALANCER }
                .flatMap { it.balancerMemberKeys }.toSet()
            val retained = old.filter { it.id == id && it.linkKey in referenced }
                .map { it.copy(hidden = true) }
            profiles.replaceProfiles(null, (remaining + retained).filter { !it.hidden || it.linkKey in referenced })
        }
    }

    /** Drops every standalone profile at once — the group's own delete. */
    suspend fun deleteManualProfiles() = withContext(Dispatchers.IO) {
        profiles.replaceProfiles(null, emptyList())
    }

    /** Stores a measured ping on a profile row, for the list and the home screen. */
    suspend fun setProfileDelays(results: List<Pair<String, Long?>>, testedAt: Long) =
        withContext(Dispatchers.IO) {
            db.withTransaction { results.forEach { (id, delay) -> profiles.setDelay(id, delay, testedAt) } }
        }

    suspend fun setProfileDelay(id: String, delayMillis: Long?, testedAt: Long) =
        withContext(Dispatchers.IO) { profiles.setDelay(id, delayMillis, testedAt) }

    suspend fun setProfileSpeed(id: String, speedMbps: Double?, testedAt: Long) =
        withContext(Dispatchers.IO) { profiles.setSpeed(id, speedMbps, testedAt) }

    suspend fun upsertProfile(profile: ProfileEntity): ProfileEntity = withContext(Dispatchers.IO) {
        val normalizedId = profile.id.ifBlank { UUID.randomUUID().toString() }
        val existing = profiles.byId(normalizedId)
        val order = if (existing != null) existing.order else {
            val siblings = profiles.bySubscriptionOnce(profile.subscriptionId)
            (siblings.maxOfOrNull { it.order } ?: -1L) + 1L
        }
        val toStore = profile.copy(
            id = normalizedId,
            order = order,
            linkKey = profile.computeLinkKey(),
        )
        profiles.upsert(toStore)
        toStore
    }

    suspend fun allProfilesOnce(): List<ProfileEntity> = withContext(Dispatchers.IO) {
        profiles.allOnce()
    }

    suspend fun allSubscriptionsOnce(): List<SubscriptionEntity> = withContext(Dispatchers.IO) {
        subscriptions.allOnce()
    }

    suspend fun restoreBackupData(
        restoredSubs: List<SubscriptionEntity>,
        restoredProfiles: List<ProfileEntity>,
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val validSubIds = restoredSubs.map { it.id }.toSet()
            restoredSubs.forEach { subscriptions.upsert(it) }
            restoredProfiles.forEach { profile ->
                val subId = profile.subscriptionId?.takeIf { it in validSubIds }
                profiles.upsert(
                    profile.copy(
                        subscriptionId = subId,
                        linkKey = profile.linkKey.ifBlank { profile.computeLinkKey() },
                    ),
                )
            }
        }
        restoredSubs.filter { it.autoUpdate }.forEach { scheduleAutoUpdate(it) }
    }

    /** Marks a row as just used — the pick that made it the connect's profile. */
    suspend fun setProfileLastUsed(id: String, usedAt: Long) =
        withContext(Dispatchers.IO) { profiles.setLastUsed(id, usedAt) }

    /** Pins or unpins a row — a targeted update, the row's identity untouched. */
    suspend fun setProfilePinned(id: String, pinned: Boolean) =
        withContext(Dispatchers.IO) { profiles.setPinned(id, pinned) }

    /** Hides or reveals a row from the list — a targeted update, the row's identity untouched. */
    suspend fun setProfileConcealed(id: String, concealed: Boolean) =
        withContext(Dispatchers.IO) { profiles.setConcealed(id, concealed) }

    /** Pins or unpins a subscription — a targeted update, the row's metadata untouched. */
    suspend fun setSubscriptionPinned(id: String, pinned: Boolean) =
        withContext(Dispatchers.IO) { subscriptions.setPinned(id, pinned) }

    /**
     * Stamps the drafts as rows: identity, ownership, order and — where the linkKey
     * matches an old row of the same subscription — the old id, ping and its timestamp.
     */
    private fun stamped(
        drafts: List<ProfileEntity>,
        subscriptionId: String?,
        oldProfiles: List<ProfileEntity>,
    ): List<ProfileEntity> {
        val oldByKey = oldProfiles.associateBy { it.linkKey }
        return drafts.mapIndexed { index, draft ->
            val key = draft.computeLinkKey()
            val old = oldByKey[key]
            draft.copy(
                id = old?.id ?: UUID.randomUUID().toString(),
                subscriptionId = subscriptionId,
                order = index.toLong(),
                remarks = draft.remarks.ifEmpty { old?.remarks.orEmpty() },
                linkKey = key,
                testDelayMillis = old?.testDelayMillis,
                lastTestAt = old?.lastTestAt,
                // The last use rides the identity like the ping does: the chip's
                // "last used" order keeps its memory through whatever ships next.
                lastUsedAt = old?.lastUsedAt,
                // The pin rides the identity like the ping does: a server the user
                // pinned stays pinned through whatever the panel ships next.
                pinned = old?.pinned ?: false,
                // The hide rides it too: a server the user hid stays out of sight
                // through whatever the panel ships next.
                concealed = old?.concealed ?: false,
            )
        }
    }

    /**
     * Some panels answer a subscription URL with a single URL line — a nested
     * subscription. One level of expansion, Hiddify-style, covers the panels that do it.
     * The second fetch dresses up the same way the first did — the fingerprint is global.
     */
    private suspend fun fetchWithNestedExpansion(
        url: String,
        userAgentOverride: String? = null,
        useCdnMirrors: Boolean = true,
    ): Pair<String, SubscriptionMetadata> {
        val first = fetcher.fetch(url, userAgentOverride = userAgentOverride, useCdnMirrors = useCdnMirrors)
        val nested = SubscriptionParser.isNestedSubscriptionUrl(first.body)
            ?.takeIf { it != url }
            ?: return first.body to first.metadata
        Log.d(TAG, "Subscription is nested; following one level")
        val second = fetcher.fetch(nested, userAgentOverride = userAgentOverride, useCdnMirrors = useCdnMirrors)
        // The nested response is the real one: its sources override the outer's per key.
        return second.body to second.metadata.copy(
            geodataSources = GeodataSources.merge(first.metadata.geodataSources, second.metadata.geodataSources),
        )
    }

    /**
     * Filters parsed profiles with an optional regex rule:
     * prefixing `!` excludes matching profiles; otherwise only matching profiles are kept.
     */
    private fun applyFilterRegex(items: List<ProfileEntity>, filterRegex: String): List<ProfileEntity> {
        val raw = filterRegex.trim()
        if (raw.isEmpty()) return items
        val exclude = raw.startsWith("!")
        val patternText = if (exclude) raw.removePrefix("!").trim() else raw
        if (patternText.isEmpty()) return items
        val regex = runCatching { Regex(patternText, RegexOption.IGNORE_CASE) }.getOrNull() ?: return items
        return items.filter { p ->
            val matched = regex.containsMatchIn(p.remarks) ||
                regex.containsMatchIn(p.server) ||
                regex.containsMatchIn(p.type.name)
            if (exclude) !matched else matched
        }
    }

    /** The logo's own home: files/subscription-logos/<id>.img. */
    private fun logoFile(id: String) = java.io.File(java.io.File(context.filesDir, "subscription-logos"), "$id.img")

    /**
     * Brings the card's avatar in line with the panel's `profile-logo` header, called on
     * every refresh. A missing, empty or non-URL header clears the logo — the default
     * tile — as does a body that is neither PNG nor SVG. A download that merely fails
     * (the network's fault, not the panel's) keeps the last good one.
     * Returns the path the row should carry, or null for the default tile.
     */
    private suspend fun refreshLogo(id: String, logoUrl: String?, previousPath: String?): String? {
        val parsed = logoUrl?.let { fetcher.parseLogoUrl(it) }
        if (parsed == null) {
            previousPath?.let { logoFile(id).delete() }
            return null
        }
        return try {
            val bytes = fetcher.fetchLogo(parsed)
            if (!isLogoBytes(bytes)) {
                previousPath?.let { logoFile(id).delete() }
                return null
            }
            val file = logoFile(id)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Logo download failed, keeping the previous one: ${e.message}")
            previousPath
        }
    }

    /** PNG by its signature, SVG by its angle bracket — the two formats the tile renders. */
    private fun isLogoBytes(bytes: ByteArray): Boolean {
        if (bytes.size < 8) return false
        val png = bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        if (png) return true
        val head = String(bytes, 0, minOf(256, bytes.size), Charsets.UTF_8).trimStart()
        return head.startsWith("<")
    }

    private fun scheduleAutoUpdate(subscription: SubscriptionEntity) {
        if (!subscription.autoUpdate) return
        val minutes = subscription.updateIntervalMinutes?.toLong()
            ?.coerceIn(15L, MAX_UPDATE_HOURS * 60L)
            ?: ((subscription.updateIntervalHours?.toLong() ?: DEFAULT_UPDATE_HOURS)
                .coerceIn(MIN_UPDATE_HOURS, MAX_UPDATE_HOURS) * 60L)
        val request = PeriodicWorkRequestBuilder<SubscriptionUpdateWorker>(minutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(SubscriptionUpdateWorker.KEY_SUBSCRIPTION_ID to subscription.id))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_PREFIX + subscription.id, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun logFailure(operation: String, id: String?, e: Throwable) {
        if (e is CancellationException) throw e
        Log.w(TAG, "Subscription $operation failed (id=${id ?: "new"}): ${e.message}")
    }
}
