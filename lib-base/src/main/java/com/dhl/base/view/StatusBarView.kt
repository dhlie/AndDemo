package com.dhl.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.dhl.base.statusBarHeight

/**
 *
 * Author: Hello
 * Create: 2024/4/10 20:46
 * Description:
 *
 */
class StatusBarView : View {

    private var statusBarHei = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        statusBarHei = statusBarHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = MeasureSpec.makeMeasureSpec(statusBarHei, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}