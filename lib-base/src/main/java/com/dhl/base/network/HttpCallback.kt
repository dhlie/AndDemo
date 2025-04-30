package com.dhl.base.network

import android.app.Activity
import androidx.fragment.app.Fragment
import okhttp3.Request

/**
 *
 * Author: Hello
 * Create: 2024/6/1 12:56
 * Description:
 *
 */
abstract class HttpCallback<T> : Callback<T> {

    constructor()

    constructor(host: Activity?): super(host)

    constructor(host: Fragment?): super(host)

    override fun response(request: Request, response: T) {
        super.response(request, response)
    }

}