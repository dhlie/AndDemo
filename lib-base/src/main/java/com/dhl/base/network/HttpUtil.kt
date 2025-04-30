package com.dhl.base.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HttpUtil {
    companion object {
        private const val TAG = "HttpUtil"

        fun <T> request(call: Call<T>, callback: HttpCallback<T>) {
            enqueueCall(call, callback)
        }

        fun <T> syncRequest(call: Call<T>): T? {
            try {
                val response = call.execute()
                return response.body()
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
            return null
        }

        private fun <T> enqueueCall(call: Call<T>, callback: HttpCallback<T>) {
            callback.start(call)
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val res = response.body()
                    if (res == null) {
                        callback.failure(call.request(), RuntimeException("Response body is null"))
                    } else {
                        callback.response(call.request(), res)
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    callback.failure(call.request(), throwable)
                }
            })
        }
    }
}