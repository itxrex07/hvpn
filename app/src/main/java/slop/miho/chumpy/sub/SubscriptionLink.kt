package slop.miho.chumpy.sub

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal object SubscriptionLink {
    fun parse(link: String): String? {
        val prefix = "chumpy://add/"
        if (!link.startsWith(prefix, ignoreCase = true) || link.length > 16_384) return null
        val url = link.substring(prefix.length).toHttpUrlOrNull() ?: return null
        if (url.username.isNotEmpty() || url.password.isNotEmpty()) return null
        return url.toString()
    }
}
