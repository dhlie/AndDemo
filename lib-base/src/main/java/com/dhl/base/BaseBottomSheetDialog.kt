package com.dhl.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dhl.base.utils.SystemUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.EdgeToEdgeUtils

/**
 *
 * Author: duanhaoliang
 * Create: 2023/9/3 9:35
 * Description:
 *
 */
open class BaseBottomSheetDialog : BottomSheetDialog {

    constructor(context: Context) : this(context, R.style.Base_Dialog_Common_BottomSheet)

    constructor(context: Context, @StyleRes themeResId: Int) : super(context, themeResId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window?.let { EdgeToEdgeUtils.applyEdgeToEdge(it,true) }
        }
    }

    protected fun setSystemBarWindowInsetsListener(view: View, callback: (Insets) -> WindowInsetsCompat) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            callback.invoke(systemBars)
        }
    }

    override fun onContentChanged() {
        super.onContentChanged()

        val window = window ?: return
        val attr = window.attributes
        attr.width = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = attr
    }

    override fun show() {
        val activity = SystemUtil.getActivityFromContext(context)
        if (!SystemUtil.isActivityValid(activity)) {
            return
        }
        super.show()
    }

    override fun dismiss() {
        val activity = SystemUtil.getActivityFromContext(context)
        if (!SystemUtil.isActivityValid(activity)) {
            return
        }
        super.dismiss()
    }

}