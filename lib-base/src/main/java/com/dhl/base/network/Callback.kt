package com.dhl.base.network

import android.app.Activity
import androidx.fragment.app.Fragment
import com.dhl.base.AppExecutors
import com.dhl.base.utils.SystemUtil
import okhttp3.Request
import retrofit2.Call
import java.lang.ref.WeakReference

/**
 *
 * Author: Hello
 * Create: 2024/6/1 12:56
 * Description:
 *
 */
abstract class Callback<Res> {

    private var call: Call<*>? = null
    private var hostRef: WeakReference<Any>? = null

    @Volatile
    private var isCanceled = false

    constructor()

    constructor(host: Activity?) {
        if (host != null) {
            hostRef = WeakReference(host)
        }
    }

    constructor(host: Fragment?) {
        if (host != null) {
            hostRef = WeakReference(host)
        }
    }

    fun cancel() {
        AppExecutors.runOnMain {
            if (!isCanceled) {
                call?.cancel()
                isCanceled = true
            }
        }
    }

    fun start(call: Call<*>) {
        AppExecutors.runOnMain {
            this.call = call
            if (isValid()) {
                onStart(call.request())
            }
        }
    }

    open fun response(request: Request, response: Res) {
        AppExecutors.runOnMain {
            if (isValid()) {
                onResponse(request, response)
            }
        }
    }

    fun failure(request: Request, throwable: Throwable) {
        AppExecutors.runOnMain {
            if (isValid()) {
                onFailure(request, throwable)
            }
        }
    }

    private fun isValid(): Boolean {
        if (isCanceled) return false
        val ref = hostRef ?: return true
        return when (val host = ref.get()) {
            is Activity -> return SystemUtil.isActivityValid(host)
            is Fragment -> return SystemUtil.isActivityValid(host.activity) && !host.isDetached
            else -> false
        }
    }

    open fun onStart(request: Request) {}

    abstract fun onResponse(request: Request, response: Res)
    abstract fun onFailure(request: Request, throwable: Throwable)
}