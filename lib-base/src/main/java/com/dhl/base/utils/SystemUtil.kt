package com.dhl.base.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 *
 * Author: duanhaoliang
 * Create: 2021/4/13 16:48
 * Description:
 *
 */
class SystemUtil private constructor() {
    companion object {
        fun isActivityValid(activity: Activity?): Boolean {
            return activity != null && !activity.isFinishing && !activity.isDestroyed
        }

        fun getActivityFromView(view: View?): Activity? {
            return if (view == null) {
                null
            } else {
                getActivityFromContext(view.context)
            }
        }

        fun getActivityFromContext(context: Context?): Activity? {
            var ctx = context
            while (ctx is ContextWrapper) {
                if (ctx is Activity) {
                    return ctx
                }
                ctx = ctx.baseContext
            }
            return null
        }

        fun showSoftKeyboard(editText: EditText?) {
            val context = editText?.context ?: return
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
        }

        fun hideSoftKeyboard(editText: View?) {
            if (editText == null) {
                return
            }
            editText.clearFocus()
            val imm =
                editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    ?: return
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        fun getPositionInParentView(targetView: View, parent: View, out: Rect) {
            out.set(targetView.left, targetView.top, targetView.right, targetView.bottom)
            var view = targetView.parent as? View
            while (view != null && view !== parent) {
                out.offset(view.left, view.top)
                view = view.parent as? View
            }
        }
    }
}