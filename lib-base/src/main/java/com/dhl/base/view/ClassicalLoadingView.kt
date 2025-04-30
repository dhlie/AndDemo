package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 *
 * Author: duanhl
 * Create: 2023/9/13 11:36
 * Description:
 *
 */
class ClassicalLoadingView : View {

    private val drawable = LoadingDrawable()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        drawable.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        drawable.draw(canvas)
        invalidate()
    }

}