package com.dhl.base.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import com.dhl.base.R

/**
 *
 * Author: duanhl
 * Create: 2023/9/7 09:05
 * Description:
 *
 */
class BoldTextView : androidx.appcompat.widget.AppCompatTextView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        var strokeWidth = 1f
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoldTextView)
            try {
                strokeWidth = typedArray.getFloat(R.styleable.BoldTextView_stroke_width, 1f)
            } catch (e: Exception) {
            } finally {
                typedArray.recycle()
            }
        }

        paint.let {
            it.strokeWidth = strokeWidth
            it.style = Paint.Style.FILL_AND_STROKE
        }
    }
}