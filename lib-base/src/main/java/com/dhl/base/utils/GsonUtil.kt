package com.dhl.base.utils

import com.google.gson.Gson
import java.lang.reflect.Type


/**
 *
 * Author: Hello
 * Create: 2024/4/16 19:57
 * Description:
 *
 */
class GsonUtil private constructor() {
    companion object {

        fun <T> fromJson(json: String?, classOfT: Class<T>?): T? {
            try {
                return Gson().fromJson(json, classOfT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun <T> fromJson(json: String?, type: Type?): T? {
            try {
                return Gson().fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun toJson(src: Any?): String {
            try {
                return Gson().toJson(src) ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}