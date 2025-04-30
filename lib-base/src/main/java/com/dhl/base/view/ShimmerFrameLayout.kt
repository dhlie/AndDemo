package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout


/**
 *
 * Author: Hello
 * Create: 2024/4/10 15:51
 * Description:
 *
 */
class ShimmerFrameLayout : FrameLayout {

    private val shimmerDrawable = ShimmerDrawable()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, Paint())
        shimmerDrawable.callback = this
    }

    fun getShimmerDrawable() = shimmerDrawable

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shimmerDrawable.setBounds(0, 0, w, h)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === shimmerDrawable
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        shimmerDrawable.draw(canvas)
    }

}