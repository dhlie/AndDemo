package com.dhl.base.network

import androidx.annotation.Keep

@Keep
class EmptyBody

@Keep
data class BaseResponse<T>(
    val code: Int = -1,
    val msg: String = "",
    val data: T? = null,
) {
    fun isSuccess(): Boolean {
        return code == 200 || code == 0
    }
}