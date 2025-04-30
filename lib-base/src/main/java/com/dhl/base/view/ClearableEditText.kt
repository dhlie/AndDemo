package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.dhl.base.R
import com.dhl.base.dp
import com.dhl.base.utils.log

/**
 *
 * Author: duanhaoliang
 * Create: 2022/1/20 16:47
 * Description: 带'清空'按钮的 EditText
 *
 */
class ClearableEditText : androidx.appcompat.widget.AppCompatEditText {

    companion object {
        const val HIDE_WHEN_EMPTY = 1
        const val HIDE_WHEN_LOSS_FOCUS = 2
        const val HIDE_WHEN_EMPTY_OR_LOSS_FOCUS = 3
    }

    private lateinit var clearDrawable: Drawable
    private var drawablePaddingEnd: Int = 0
    private var alwaysShowClearBtn = false
    private var hideClearBtnFlag = HIDE_WHEN_EMPTY_OR_LOSS_FOCUS

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
        val a = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText)
        clearDrawable =
            a.getDrawable(R.styleable.ClearableEditText_clearDrawable) ?: ContextCompat.getDrawable(
                context,
                R.drawable.ic_clear_input
            )!!
        drawablePaddingEnd =
            a.getDimensionPixelOffset(R.styleable.ClearableEditText_clearDrawablePaddingEnd, 0)
        alwaysShowClearBtn = a.getBoolean(R.styleable.ClearableEditText_alwaysShowClearBtn, false)
        hideClearBtnFlag =
            a.getInt(R.styleable.ClearableEditText_hideClearBtn, HIDE_WHEN_EMPTY_OR_LOSS_FOCUS)
        a.recycle()

        setPadding(
            paddingStart,
            paddingTop,
            drawablePaddingEnd + clearDrawable.intrinsicWidth + 4.dp,
            paddingBottom
        )

        val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val bounds = clearDrawable.bounds
                val y = e.y
                val dp16 = 16.dp
                if (e.x > bounds.left - dp16 && y > bounds.top - dp16 && y < bounds.bottom + dp16) {
                    setText("")
                }
                return true
            }
        })

        setOnTouchListener { _, event ->
            if (isClearBtnShown()) {
                gestureDetector.onTouchEvent(event)
            }
            false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        clearDrawable.setBounds(
            w - drawablePaddingEnd - clearDrawable.intrinsicWidth,
            (h - clearDrawable.intrinsicHeight) / 2,
            w - drawablePaddingEnd,
            (h + clearDrawable.intrinsicHeight) / 2
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isClearBtnShown()) {
            drawClearBtn(canvas)
        }
    }

    private fun isClearBtnShown(): Boolean {
        return if (alwaysShowClearBtn) {
            true
        } else {
            when (hideClearBtnFlag) {
                HIDE_WHEN_EMPTY -> text.toString().isNotEmpty()
                HIDE_WHEN_LOSS_FOCUS -> hasFocus()
                HIDE_WHEN_EMPTY_OR_LOSS_FOCUS -> hasFocus() && text.toString().isNotEmpty()
                else -> true
            }
        }
    }

    private fun drawClearBtn(canvas: Canvas) {
        log { "w-h:$width - $height  mw-mh:$measuredWidth - $measuredHeight, sx-sy:$scrollX - $scrollY" }
        canvas.save()
        canvas.translate(scrollX.toFloat(), scrollY.toFloat())
        clearDrawable.draw(canvas)
        canvas.restore()
    }

}