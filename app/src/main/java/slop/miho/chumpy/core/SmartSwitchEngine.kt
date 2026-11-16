package slop.miho.chumpy.core

import slop.miho.chumpy.db.ProfileEntity
import slop.miho.chumpy.db.ProfileType

/**
 * Dead-tunnel detection & auto-failover engine (ported from v2rayV SmartSwitch):
 * Detects sustained outbound upload stalls where tx > 2 KB/s while rx < 1 KB/s for
 * [stallThreshold] consecutive 1-second samples, guarded by cooldown and hourly rate caps.
 */
class SmartSwitchEngine(
    private val stallThreshold: Int = 8,
    private val cooldownMillis: Long = 45_000L,
    private val maxSwitchesPerHour: Int = 4,
) {
    private var consecutiveStalls = 0
    private var lastSwitchAt = 0L
    private val recentSwitches = ArrayDeque<Long>()

    fun reset() {
        consecutiveStalls = 0
    }

    /**
     * Feeds a 1-second bandwidth delta sample (in bytes/sec).
     * Returns true when failover should trigger immediately.
     */
    fun recordSample(rxBytesPerSec: Long, txBytesPerSec: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (rxBytesPerSec >= 1_024L) {
            consecutiveStalls = 0
            return false
        }
        if (txBytesPerSec > 2_048L && rxBytesPerSec < 1_024L) {
            consecutiveStalls++
        } else {
            consecutiveStalls = 0
        }
        if (consecutiveStalls < stallThreshold) return false

        // Respect cooldown & hourly cap
        if (nowMillis - lastSwitchAt < cooldownMillis) {
            consecutiveStalls = 0
            return false
        }
        while (recentSwitches.isNotEmpty() && nowMillis - recentSwitches.first() > 3_600_000L) {
            recentSwitches.removeFirst()
        }
        if (recentSwitches.size >= maxSwitchesPerHour) return false

        consecutiveStalls = 0
        lastSwitchAt = nowMillis
        recentSwitches.addLast(nowMillis)
        return true
    }

    companion object {
        fun pickNextCandidate(
            candidates: List<ProfileEntity>,
            currentId: String,
        ): ProfileEntity? {
            val usable = candidates.filter {
                it.id != currentId &&
                    it.type != ProfileType.BALANCER &&
                    !it.hidden &&
                    !it.concealed
            }
            if (usable.isEmpty()) return null
            // Prefer working measured nodes (positive delay), sorted by lowest latency, then highest speed.
            val testedWorking = usable.filter { (it.testDelayMillis ?: -1L) > 0L }
                .sortedWith(
                    compareBy<ProfileEntity> { it.testDelayMillis ?: Long.MAX_VALUE }
                        .thenByDescending { it.speedMbps ?: 0.0 },
                )
            if (testedWorking.isNotEmpty()) return testedWorking.first()

            // Otherwise pick untested nodes (delay == null) before known-dead (-1) nodes.
            val untested = usable.filter { it.testDelayMillis == null }
            return untested.firstOrNull() ?: usable.firstOrNull()
        }
    }
}
