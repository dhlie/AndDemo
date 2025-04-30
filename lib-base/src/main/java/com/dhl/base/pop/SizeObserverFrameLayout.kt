package com.dhl.base.pop

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 *
 * Author: duanhl
 * Create: 2023/10/7 16:38
 * Description:
 *
 */
class SizeObserverFrameLayout : FrameLayout {

    fun interface OnSizeChangeListener {
        fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    }

    private var sizeChangeListener: OnSizeChangeListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        sizeChangeListener?.onSizeChanged(w, h, oldw, oldh)
    }

    fun setOnSizeChangeListener(listener: OnSizeChangeListener?) {
        sizeChangeListener = listener
    }

}