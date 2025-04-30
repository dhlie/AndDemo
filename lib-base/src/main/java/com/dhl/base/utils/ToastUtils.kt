package com.dhl.base.utils

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.dhl.base.AppExecutors
import com.dhl.base.R

/**
 *
 * Author: duanhaoliang
 * Create: 2021/12/1 14:06
 * Description:
 *
 */
class ToastUtils private constructor() {
    companion object {
        fun showToast(
            context: Context,
            @StringRes resId: Int,
            duration: Int = Toast.LENGTH_SHORT,
        ) {
            showToast(context, context.resources.getText(resId), duration)
        }

        fun showToast(
            context: Context,
            message: CharSequence,
            duration: Int = Toast.LENGTH_SHORT,
        ) {
            if (message.isBlank()) return
            AppExecutors.runOnMain {
                doShowToast(context, message, duration)
            }
        }

        private fun doShowToast(
            context: Context,
            message: CharSequence,
            duration: Int = Toast.LENGTH_SHORT,
        ) {
            val textView = TextView(context).apply {
                text = message
                setPadding(36, 36, 36, 36)
                textSize = 14f
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.bg_toast)
            }

            Toast(context).apply {
                view = textView
                setGravity(Gravity.CENTER, 0, 0)
                this.duration = duration
                show()
            }
        }
    }
}