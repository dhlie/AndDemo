package com.dhl.base

import android.os.Handler
import android.os.Looper
import androidx.core.os.HandlerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 *
 * Author: duanhaoliang
 * Create: 2021/7/6 9:27
 * Description:
 *
 */
object AppExecutors {

    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun runOnMain(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    fun postOnMain(token: Any? = null, delay: Long = 0L, runnable: Runnable) {
        HandlerCompat.postDelayed(mainHandler, runnable, token, delay)
    }

    fun removeMainCallback(runnable: Runnable) {
        mainHandler.removeCallbacks(runnable)
    }

    fun removeMainCallbacks(token: Any?) {
        mainHandler.removeCallbacksAndMessages(token)
    }

    fun runOnIO(action: suspend () -> Unit): Job {
        return ioScope.launch() {
            action.invoke()
        }
    }
}