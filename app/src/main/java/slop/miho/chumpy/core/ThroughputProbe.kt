package slop.miho.chumpy.core

import android.os.SystemClock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Single-stream download throughput probe through a temporary SOCKS5 loopback proxy.
 * Uses chunked download stream (capped at 6s) and returns Mbps, or -1.0 on failure.
 */
object ThroughputProbe {
    const val DEFAULT_TEST_URL = "https://speed.cloudflare.com/__down?bytes=25000000"
    private const val MAX_SAMPLE_MILLIS = 6_000L

    fun measureMbps(proxy: Proxy, testUrl: String = DEFAULT_TEST_URL): Double {
        val client = OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
        val request = Request.Builder()
            .url(testUrl)
            .header("User-Agent", "HollowVPN/1.0")
            .get()
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return -1.0
                val body = response.body ?: return -1.0
                val buffer = ByteArray(32 * 1024)
                val startMs = SystemClock.elapsedRealtime()
                var totalBytes = 0L
                body.byteStream().use { stream ->
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        totalBytes += read
                        if (SystemClock.elapsedRealtime() - startMs >= MAX_SAMPLE_MILLIS) break
                    }
                }
                val elapsedMs = (SystemClock.elapsedRealtime() - startMs).coerceAtLeast(1L)
                if (totalBytes < 1024L) return -1.0
                val bits = totalBytes * 8.0
                val seconds = elapsedMs / 1000.0
                bits / seconds / 1_000_000.0
            }
        } catch (_: Exception) {
            -1.0
        }
    }
}
