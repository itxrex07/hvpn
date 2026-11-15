package go

import android.content.Context

object Seq {
    @Volatile
    private var appContext: Context? = null

    @JvmStatic
    fun setContext(context: Context?) {
        appContext = context?.applicationContext
    }

    @JvmStatic
    fun getContext(): Context? = appContext
}
