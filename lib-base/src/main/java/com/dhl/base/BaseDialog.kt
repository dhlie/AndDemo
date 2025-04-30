package com.dhl.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dhl.base.utils.SystemUtil

/**
 *
 * Author: duanhaoliang
 * Create: 2021/6/25 9:35
 * Description:
 *
 */
open class BaseDialog : Dialog {

    constructor(context: Context) : this(context, R.style.Base_Dialog_Common)

    constructor(context: Context, @StyleRes themeResId: Int) : super(context, themeResId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = SystemUtil.getActivityFromContext(context) as? BaseActivity
        activity?.lifecycle?.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY && isShowing) {
                    dismiss()
                }
            }
        })
    }

    override fun onContentChanged() {
        super.onContentChanged()
        val window = window ?: return
        val attr = window.attributes
        val size = getDialogSize()
        attr.width = size.width
        attr.height = size.height

        window.attributes = attr
    }

    protected open fun getDialogSize(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.dialog_width),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun show() {
        val activity = SystemUtil.getActivityFromContext(context)
        if (!SystemUtil.isActivityValid(activity)) {
            return
        }
        super.show()
    }

}