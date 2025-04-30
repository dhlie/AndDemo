package com.dhl.base.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.dhl.base.AppExecutors
import com.dhl.base.R
import com.dhl.base.sp

/**
 *
 * Author: duanhl
 * Create: 2023/10/21 10:06
 * Description:
 *
 */
class AZIndexView : View {

    interface LetterChangeListener {
        fun onShow(char: Char) {}
        fun onLetterChanged(char: Char)
        fun onHide() {}
    }

    companion object {

        const val VERTICAL = LinearLayout.VERTICAL
        const val HORIZONTAL = LinearLayout.HORIZONTAL

        private val letters =
            charArrayOf(
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#'
            )
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textOffsetY = 0f
    private var itemSize = 0f
    private val bgRectF = RectF()
    private var bgRadius = 0f
    private var textColor = 0
    private var textDisableColor = 0
    private var selectedColor = 0
    private var bgColor = 0
    private var orientation: Int = VERTICAL
    private var letterViewId: Int = 0
    private var enableChars: Set<Char>? = null
    private var letterView: View? = null

    private var currIndex = 0
    private var listener: LetterChangeListener = object : LetterChangeListener {
        override fun onShow(char: Char) {
            letterChangeListener?.onShow(char)
            val letterView = letterView ?: return
            AppExecutors.removeMainCallbacks("show_hide_letter_view")
            AppExecutors.postOnMain("show_hide_letter_view", 80) {
                letterView.visibility = VISIBLE
                val preAnim = letterView.tag as? ObjectAnimator
                preAnim?.cancel()
                if (letterView.alpha != 1f) {
                    val animator = ObjectAnimator.ofFloat(letterView, "alpha", letterView.alpha, 1f)
                    animator.duration = 200
                    letterView.tag = animator
                    animator.start()
                }
            }
        }

        override fun onLetterChanged(char: Char) {
            letterChangeListener?.onLetterChanged(char)
        }

        override fun onHide() {
            letterChangeListener?.onHide()
            val letterView = letterView ?: return
            AppExecutors.removeMainCallbacks("show_hide_letter_view")
            if (letterView.visibility != VISIBLE) {
                return
            }
            val preAnim = letterView.tag as? ObjectAnimator
            preAnim?.cancel()
            val animator = ObjectAnimator.ofFloat(letterView, "alpha", letterView.alpha, 0f)
            animator.duration = 200
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    letterView.visibility = GONE
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
            letterView.tag = animator
            animator.start()
        }

    }
    private var letterChangeListener: LetterChangeListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AZIndexView)
            try {
                orientation = typedArray.getInt(R.styleable.AZIndexView_letter_orientation, orientation)
                bgColor = typedArray.getColor(R.styleable.AZIndexView_bg_color, bgColor)
                textColor = typedArray.getColor(R.styleable.AZIndexView_text_color, textColor)
                selectedColor = typedArray.getColor(R.styleable.AZIndexView_select_color, selectedColor)
                letterViewId = typedArray.getResourceId(R.styleable.AZIndexView_letter_view, 0)
            } catch (e: Exception) {
            } finally {
                typedArray.recycle()
            }
        }

        paint.textSize = 10.sp
        paint.style = Paint.Style.FILL_AND_STROKE
        val fm = paint.fontMetrics
        textOffsetY = -(fm.descent + fm.ascent) / 2

        if (textColor == 0) {
            textColor = context.getColor(R.color.text_primary)
        }
        if (textDisableColor == 0) {
            textDisableColor = context.getColor(R.color.text_disable)
        }
        if (selectedColor == 0) {
            selectedColor = context.getColor(R.color.colorPrimary)
        }
    }

    fun setEnableChars(chars: Set<Char>) {
        enableChars = chars
        invalidate()
    }

    fun setSelected(char: Char) {
        if (char == letters[currIndex]) {
            return
        }
        currIndex = letters.indexOf(char)
        if (currIndex == -1) {
            currIndex = letters.size - 1
        }
        invalidate()
    }

    fun setLetterChangeListener(listener: LetterChangeListener?) {
        letterChangeListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (letterView == null) {
            letterView = (parent as? ViewGroup)?.findViewById(letterViewId)
        }
        if (orientation == HORIZONTAL) {
            itemSize = (w.toFloat() - paddingStart - paddingEnd) / letters.size

            val rectHei = h - paddingTop - paddingBottom
            bgRadius = rectHei / 2f
            val left = paddingStart + itemSize / 2f - bgRadius
            bgRectF.set(
                left,
                paddingTop.toFloat(),
                w - left,
                (h - paddingBottom).toFloat()
            )
        } else {
            itemSize = (h.toFloat() - paddingTop - paddingBottom) / letters.size
            val rectWidth = w - paddingStart - paddingEnd
            bgRadius = rectWidth / 2f
            val top = paddingTop + itemSize / 2f - bgRadius
            bgRectF.set(
                paddingStart.toFloat(),
                top,
                (w - paddingEnd).toFloat(),
                h - top
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                listener.onShow(letters[currIndex])
                touchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                touchEvent(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchEvent(event)
                listener.onHide()
            }
        }
        return true
    }

    private fun touchEvent(event: MotionEvent) {
        val indexF = if (orientation == HORIZONTAL) {
            (event.x - paddingStart) / itemSize
        } else {
            (event.y - paddingTop) / itemSize
        }
        var index = indexF.toInt()
        if (index < 0) {
            index = 0
        }
        if (index >= letters.size) {
            index = letters.size - 1
        }
        if (enableChars?.indexOf(letters[index]) == -1) {
            return
        }
        if (currIndex != index) {
            currIndex = index
            listener.onLetterChanged(letters[index])
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = bgColor
        canvas.drawRoundRect(bgRectF, bgRadius, bgRadius, paint)

        paint.color = textColor
        if (orientation == HORIZONTAL) {
            var centerX = paddingStart + itemSize / 2
            val contentCenterY = paddingTop + (height - paddingTop - paddingBottom) / 2
            val textY = contentCenterY + textOffsetY
            for (index in letters.indices) {
                if (index == currIndex) {
                    paint.color = selectedColor
                    paint.strokeWidth = 1.5f
                } else {
                    paint.color = if (enableChars?.contains(letters[index]) == false) {
                        textDisableColor
                    } else {
                        textColor
                    }
                    paint.strokeWidth = 0f
                }
                val charWidth = paint.measureText(letters, index, 1)
                canvas.drawText(letters, index, 1, centerX - charWidth / 2, textY, paint)
                centerX += itemSize
            }
        } else {
            val centerX = bgRectF.centerX()
            var centerY = paddingTop + itemSize / 2
            for (index in letters.indices) {
                if (index == currIndex) {
                    paint.color = selectedColor
                    paint.strokeWidth = 1.5f
                } else {
                    paint.color = if (enableChars?.contains(letters[index]) == false) {
                        textDisableColor
                    } else {
                        textColor
                    }
                    paint.strokeWidth = 0f
                }
                val charWidth = paint.measureText(letters, index, 1)
                canvas.drawText(letters, index, 1, centerX - charWidth / 2, centerY + textOffsetY, paint)
                centerY += itemSize
            }
        }
    }

}