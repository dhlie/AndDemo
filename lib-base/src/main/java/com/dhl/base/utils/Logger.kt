package com.dhl.base.utils

import android.util.Log
import com.dhl.base.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 *
 * Author: duanhl
 * Create: 2023/9/1 17:22
 * Description:
 *
 */
fun log(level: Int? = Log.INFO, block: (() -> String?)?) {
    if (Logger.LOGGABLE) {
        when (level) {
            Log.VERBOSE -> Logger.v(null, block?.invoke() ?: "")
            Log.DEBUG -> Logger.d(null, block?.invoke() ?: "")
            Log.WARN -> Logger.w(null, block?.invoke() ?: "")
            Log.ERROR -> Logger.e(null, block?.invoke() ?: "")
            else -> {
                Logger.i(null, block?.invoke() ?: "")
            }
        }
    }
}

fun logJson(tag: String = "", block: () -> String?) {
    if (Logger.LOGGABLE) {
        Logger.json(tag, block.invoke().orEmpty())
    }
}

class Logger {

    companion object {

        const val LOGGABLE = BuildConfig.BUILD_TYPE == "debug"

        private fun getTag(tag: String?, default: String): String {
            return if (tag.isNullOrEmpty()) default else tag
        }

        private fun getMsg(msg: String, lineNum: String?): String {
            return "${lineNum ?: ""}:$msg"
        }

        fun v(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                Log.v(getTag(tag, info[0]), getMsg(msg, info[1]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun d(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                Log.d(getTag(tag, info[0]), getMsg(msg, info[1]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun i(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                Log.i(getTag(tag, info[0]), getMsg(msg, info[1]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun w(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                Log.w(getTag(tag, info[0]), getMsg(msg, info[1]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun e(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                Log.e(getTag(tag, info[0]), getMsg(msg, info[1]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun json(tag: String?, msg: String) {
            try {
                val info: Array<String> = getStackInfo()
                val maxLength = 3000
                var prettyMsg = msg
                if (msg.startsWith("{")) {
                    prettyMsg = JSONObject(msg).toString(2)
                } else if (msg.startsWith("[")) {
                    prettyMsg = JSONArray(msg).toString(2)
                }
                if (prettyMsg.length <= maxLength) {
                    Log.i(getTag(tag, info[0]), getMsg(prettyMsg, info[1]))
                    return
                }
                Log.d(getTag(tag, info[0]), "============== long json begin ==============")
                val clsTag = "\n"
                var jsonStr: String? = prettyMsg
                while (!jsonStr.isNullOrEmpty()) {
                    jsonStr = if (jsonStr.length <= maxLength) {
                        Log.i(getTag(tag, info[0]), getMsg(clsTag + jsonStr, info[1]))
                        null
                    } else {
                        val index = jsonStr.lastIndexOf(',', maxLength) + 2
                        Log.i(
                            getTag(tag, info[0]),
                            getMsg(clsTag + jsonStr.substring(0, index), info[1])
                        )

                        jsonStr.substring(index)
                    }
                }
                Log.d(getTag(tag, info[0]), "============== long json end ==============")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getStackInfo(): Array<String> {
            val stackTrace = Throwable().stackTrace
            var element: StackTraceElement? = null
            for (e in stackTrace) {
                if (e.fileName != "Logger.kt") {
                    element = e
                    break
                }
            }
            val className = element?.fileName ?: "noClsName"
            //val methodName = stackTrace[index].methodName;
            val lineNumber = element?.lineNumber ?: "noLineNum"
            val stringBuilder = StringBuilder()
            stringBuilder.append("(")
                .append(className)
                .append(":")
                .append(lineNumber)
                .append(")")
            return arrayOf("Logger", stringBuilder.toString())
        }
    }
}