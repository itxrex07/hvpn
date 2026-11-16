package slop.miho.chumpy.sub

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Staggered mirror racing (ported from v2rayV RouteRace): launches candidate URLs
 * staggered by [staggerMs] so fast primary routes win without flooding mirrors, and
 * falls back automatically if the primary stalls or is blocked.
 */
object RouteRace {
    private val RAW_GITHUB_REGEX = Regex(
        """^https?://raw\.githubusercontent\.com/([^/]+)/([^/]+)/([^/]+)/(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val GITHUB_RELEASE_REGEX = Regex(
        """^https?://github\.com/([^/]+/[^/]+/releases/(?:download|latest/download)/.+)$""",
        RegexOption.IGNORE_CASE,
    )

    fun mirrorUrlsFor(url: String): List<String> {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return emptyList()
        val rawMatch = RAW_GITHUB_REGEX.matchEntire(trimmed)
        if (rawMatch != null) {
            val (user, repo, branch, path) = rawMatch.destructured
            return listOf(
                trimmed,
                "https://fastly.jsdelivr.net/gh/$user/$repo@$branch/$path",
                "https://cdn.jsdelivr.net/gh/$user/$repo@$branch/$path",
                "https://raw.githack.com/$user/$repo/$branch/$path",
            ).distinct()
        }
        val relMatch = GITHUB_RELEASE_REGEX.matchEntire(trimmed)
        if (relMatch != null) {
            return listOf(
                trimmed,
                "https://gh-proxy.com/$trimmed",
                "https://mirror.ghproxy.com/$trimmed",
            ).distinct()
        }
        return listOf(trimmed)
    }

    suspend fun <T> first(
        urls: List<String>,
        staggerMs: Long = 250L,
        attempt: suspend (String) -> T?,
    ): T? {
        if (urls.isEmpty()) return null
        if (urls.size == 1) return attempt(urls.first())
        return coroutineScope {
            val channel = Channel<T?>(urls.size)
            val jobs = urls.mapIndexed { index, candidate ->
                launch {
                    if (index > 0) delay(staggerMs * index)
                    val value = try {
                        attempt(candidate)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                    channel.trySend(value)
                }
            }
            var remaining = urls.size
            var winner: T? = null
            try {
                while (remaining > 0) {
                    val result = channel.receive()
                    remaining--
                    if (result != null) {
                        winner = result
                        break
                    }
                }
            } finally {
                jobs.forEach { it.cancel() }
                channel.close()
            }
            winner
        }
    }
}
