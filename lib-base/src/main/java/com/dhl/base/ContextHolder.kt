package com.dhl.base

import android.app.Application

/**
 *
 * Author: duanhaoliang
 * Create: 2021/7/19 10:11
 * Description:
 *
 */
object ContextHolder {
    lateinit var appContext: Application

    fun init(application: Application) {
        appContext = application
    }
}