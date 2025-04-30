package com.dhl.base.view

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.dhl.base.R
import com.dhl.base.dp
import com.dhl.base.utils.log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 *
 * Author: duanhl
 * Create: 2023/9/12 20:09
 * Description:
 *
 */
class SwitchView : View {

    companion object {
        private const val STATE_OFF = 0
        private const val STATE_OFF_LOADING = 1
        private const val STATE_ON = 2
        private const val STATE_ON_LOADING = 3
        private const val STATE_AUTO_SLIDING = 4
        private const val STATE_TOUCH_SLIDING = 5
    }

    interface CheckedChangeListener {
        /**
         * @return 是否显示切换 loading;
         *          true: 切换时先显示loading, 之后通过 setChecked()方法完成切换
         *          false: 不显示loading, 直接切换
         */
        fun onPreChange(switchView: SwitchView, isChecked: Boolean): Boolean = false

        fun onChange(switchView: SwitchView, isChecked: Boolean)
    }

    private var thumbColor: Int = context.getColor(R.color.thumb_color)
    private var onColor: Int = context.getColor(R.color.colorAccent)
    private var offColor: Int = context.getColor(R.color.colorControlNormal)
    private var borderColor: Int = 0
    private var borderWidth = 0

    private var thumbSize = 0
    private var thumbOffX = 0
    private var thumbOnX = 0
    private var thumbRect = RectF()

    private var isChecked = false
    private var state: Int = STATE_OFF
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val loadingDrawable: LoadingDrawable
    private var loadingSize = 0
    private var changeListener: CheckedChangeListener? = null

    //自动滚动
    private val scroller = SlideScroller()
    private var autoSlidDuration = 180
    private var loadingVisibilityAnimDuration = 180
    private val argbEvaluator = ArgbEvaluator()

    private var lastX = 0f
    private var lastY = 0f
    private var isMove = false
    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchView)
            try {
                thumbColor = typedArray.getColor(R.styleable.SwitchView_thumbColor, thumbColor)
                onColor = typedArray.getColor(R.styleable.SwitchView_onColor, onColor)
                offColor = typedArray.getColor(R.styleable.SwitchView_offColor, offColor)
                borderColor = typedArray.getColor(R.styleable.SwitchView_borderColor, borderColor)
                borderWidth = typedArray.getDimensionPixelSize(
                    R.styleable.SwitchView_borderWidth,
                    borderWidth
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }

        loadingDrawable = LoadingDrawable()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isMove = false
                if (thumbRect.contains(lastX, lastY)) {
                    if (!scroller.isThumbScrollFinished()) {
                        abortAutoSlid()
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val dx = x - lastX
                val dy = y - lastY
                if (!isMove && (abs(dx) >= touchSlop || abs(dy) >= touchSlop)) {
                    isMove = true

                    if (thumbRect.contains(lastX, lastY)) {
                        setState(STATE_TOUCH_SLIDING)
                    }

                    lastX = x
                    lastY = y
                    return true
                }
                if (state == STATE_TOUCH_SLIDING) {
                    lastX = x
                    lastY = y
                    scroller.setCurrX(
                        min(
                            thumbOnX,
                            max(thumbOffX, (thumbRect.left + dx).toInt())
                        )
                    )
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (state == STATE_TOUCH_SLIDING || state == STATE_AUTO_SLIDING) {
                    if (thumbRect.centerX() < width / 2) {
                        autoSlidOff(thumbRect.left.toInt(), thumbOffX, autoSlidDuration / 2)
                    } else {
                        autoSlidOn(thumbRect.left.toInt(), thumbOnX, autoSlidDuration / 2)
                    }
                } else if (!isMove) {
                    onClicked()
                }
            }
        }
        return true
    }

    private fun onClicked() {
        when (state) {
            STATE_OFF_LOADING, STATE_ON_LOADING -> Unit
            STATE_ON -> {
                autoSlidOff(thumbRect.left.toInt(), thumbOffX, autoSlidDuration)
            }

            STATE_OFF -> {
                autoSlidOn(thumbRect.left.toInt(), thumbOnX, autoSlidDuration)
            }
        }
    }

    private fun autoSlidOn(from: Int, to: Int, duration: Int) {
        if (changeListener == null) {
            autoSlid(from, to, duration, STATE_ON)
        } else {
            val loading = if (!isChecked) changeListener!!.onPreChange(this, true) else false
            autoSlid(
                from,
                to,
                duration,
                if (loading) STATE_ON_LOADING else STATE_ON
            )
        }
    }

    private fun autoSlidOff(from: Int, to: Int, duration: Int) {
        if (changeListener == null) {
            autoSlid(from, to, duration, STATE_OFF)
        } else {
            val loading = if (isChecked) changeListener!!.onPreChange(this, false) else false
            autoSlid(
                from,
                to,
                duration,
                if (loading) STATE_OFF_LOADING else STATE_OFF
            )
        }
    }

    private fun autoSlid(from: Int, to: Int, duration: Int, toState: Int) {
        scroller.startThumbScroll(from, to, duration, toState)
        setState(scroller.getState())

        if (toState == STATE_ON_LOADING || toState == STATE_OFF_LOADING) {
            scroller.startLoadingVisibilityAnim(0f, 1f, loadingVisibilityAnimDuration)
        }

        invalidate()
    }

    private fun abortAutoSlid() {
        scroller.abortThumbScroll()
        invalidate()
    }

    private fun setState(newState: Int) {
        if (state != newState) {
            state = newState
        }
        if (newState == STATE_ON || newState == STATE_OFF) {
            val isChecked = newState == STATE_ON
            if (this.isChecked != isChecked) {
                log { "SwitchView: notify checked change: isChecked:${isChecked}" }
                this.isChecked = isChecked
                changeListener?.onChange(this, isChecked)
            }
        }
    }

    override fun computeScroll() {
        var needInvalidate = false
        if (scroller.computeThumbScroll()) {
            setState(scroller.getState())
            needInvalidate = true
        }
        if (scroller.computeLoadingVisibilityScroll()) {
            needInvalidate = true
        }

        if (needInvalidate) {
            invalidate()
        }
    }

    fun setOnCheckedChangeListener(listener: CheckedChangeListener) {
        changeListener = listener
    }

    fun setChecked(isChecked: Boolean, smooth: Boolean = true) {
        if (state == STATE_TOUCH_SLIDING) {
            return
        }

        if (
            smooth &&
            ((scroller.isThumbScrollFinished() && (state == STATE_ON_LOADING || state == STATE_OFF_LOADING))
                    || (!scroller.isThumbScrollFinished() && (scroller.getDstState() == STATE_ON_LOADING || scroller.getDstState() == STATE_OFF_LOADING)))
        ) {
            scroller.startLoadingVisibilityAnim(1f, 0f, loadingVisibilityAnimDuration)
        }
        if (isChecked) {
            if (state == STATE_ON_LOADING) {
                setState(STATE_ON)
                invalidate()
            } else if (state != STATE_ON) {
                if (smooth) {
                    autoSlid(
                        thumbRect.left.toInt(),
                        thumbOnX,
                        computeDuration(thumbRect.left.toInt(), thumbOnX),
                        STATE_ON
                    )
                } else {
                    scroller.abortThumbScroll()
                    scroller.abortLoadingVisibilityAnim()
                    setState(STATE_ON)
                    invalidate()
                }
            }
        } else {
            if (state == STATE_OFF_LOADING) {
                setState(STATE_OFF)
                invalidate()
            } else if (state != STATE_OFF) {
                if (smooth) {
                    autoSlid(
                        thumbRect.left.toInt(),
                        thumbOffX,
                        computeDuration(thumbRect.left.toInt(), thumbOffX),
                        STATE_OFF
                    )
                } else {
                    scroller.abortThumbScroll()
                    scroller.abortLoadingVisibilityAnim()
                    setState(STATE_OFF)
                    invalidate()
                }
            }
        }
    }

    fun isChecked() = isChecked

    private fun computeDuration(from: Int, to: Int): Int {
        return max(
            (abs(from.toFloat() - to) / (thumbOnX - thumbOffX) * autoSlidDuration).toInt(),
            100
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        thumbSize = (h * 0.8f).toInt()
        val thumbPadding = (h - thumbSize) / 2
        loadingSize = (thumbSize * 0.7f).toInt()
        thumbOffX = borderWidth + thumbPadding
        thumbOnX = w - borderWidth - thumbPadding - thumbSize
    }

    private fun changePaintColor(color: Int) {
        paint.color = color
        if (!isEnabled) {
            paint.alpha = 100
        } else {
            paint.alpha = 255
        }
    }

    override fun onDraw(canvas: Canvas) {
        when (state) {
            STATE_OFF -> {
                drawOff(canvas, false)
            }

            STATE_OFF_LOADING -> {
                drawOff(canvas, true)
            }

            STATE_ON -> {
                drawOn(canvas, false)
            }

            STATE_ON_LOADING -> {
                drawOn(canvas, true)
            }

            STATE_AUTO_SLIDING, STATE_TOUCH_SLIDING -> {
                drawSliding(canvas)
            }
        }
    }

    private fun drawOff(canvas: Canvas, drawLoading: Boolean) {
        drawBackground(canvas, offColor)
        drawBorder(canvas)
        drawThumb(canvas, thumbColor, thumbOffX, drawLoading)
    }

    private fun drawOn(canvas: Canvas, drawLoading: Boolean) {
        drawBackground(canvas, onColor)
        drawBorder(canvas)
        drawThumb(canvas, thumbColor, thumbOnX, drawLoading)
    }

    private fun drawSliding(canvas: Canvas) {
        val fraction = (scroller.getCurrX().toFloat() - thumbOffX) / (thumbOnX - thumbOffX)
        val color = argbEvaluator.evaluate(fraction, offColor, onColor) as Int
        drawBackground(canvas, color)
        drawBorder(canvas)

        val drawLoading =
            scroller.getDstState() == STATE_OFF_LOADING || scroller.getDstState() == STATE_ON_LOADING
        drawThumb(canvas, thumbColor, scroller.getCurrX(), drawLoading)
    }

    private fun drawBorder(canvas: Canvas) {
        if (borderWidth == 0) {
            return
        }
        changePaintColor(borderColor)
        paint.strokeWidth = borderWidth.toFloat()
        paint.style = Paint.Style.STROKE
        val halfBorder = borderWidth / 2
        canvas.drawRoundRect(
            halfBorder.toFloat(),
            halfBorder.toFloat(),
            width.toFloat() - halfBorder,
            height.toFloat() - halfBorder,
            height / 2f,
            height / 2f,
            paint
        )
    }

    private fun drawBackground(canvas: Canvas, color: Int) {
        changePaintColor(color)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            height / 2f,
            height / 2f,
            paint
        )
    }

    private fun drawThumb(canvas: Canvas, color: Int, left: Int, drawLoading: Boolean) {
        changePaintColor(color)
        paint.style = Paint.Style.FILL
        thumbRect.set(
            left.toFloat(),
            (height - thumbSize) / 2f,
            (left + thumbSize).toFloat(),
            (height + thumbSize) / 2f
        )
        canvas.drawRoundRect(thumbRect, thumbRect.height() / 2, thumbRect.height() / 2, paint)
        if (drawLoading || !scroller.isLoadingVisibilityAnimFinished()) {
            drawLoading(canvas, (left + (thumbSize - loadingSize) / 2).toInt())
        }
    }

    private fun drawLoading(canvas: Canvas, left: Int) {
        val progress = scroller.getLoadingAnimProgress()
        loadingDrawable.alpha = (progress * 255).roundToInt()
        loadingDrawable.setBounds(
            left,
            (height - loadingSize) / 2,
            left + loadingSize,
            (height + loadingSize) / 2
        )
        loadingDrawable.draw(canvas)
        invalidate()
    }

    private class SlideScroller {

        //thumb 滑动
        private var currX = 0
        private var slidFrom = 0
        private var slidTo = 0
        private var duration = 0
        private var state = 0
        private var dstState = 0
        private var isSlidFinished = true
        private var startTime = 0L
        private val interpolator: Interpolator = LinearInterpolator()

        fun startThumbScroll(from: Int, to: Int, duration: Int, toState: Int) {
            slidFrom = from
            currX = from
            slidTo = to
            this.duration = duration
            isSlidFinished = false
            startTime = SystemClock.uptimeMillis()
            state = STATE_AUTO_SLIDING
            dstState = toState
        }

        fun abortThumbScroll() {
            isSlidFinished = true
        }

        fun computeThumbScroll(): Boolean {
            if (isSlidFinished) {
                return false
            }

            val timePassed = (SystemClock.uptimeMillis() - startTime).toInt()
            if (timePassed < duration) {
                val ratio = interpolator.getInterpolation(1f / duration * timePassed)
                currX = (slidFrom + (slidTo - slidFrom) * ratio).toInt()
            } else {
                currX = slidTo
                state = dstState
                isSlidFinished = true
            }
            return true
        }

        fun isThumbScrollFinished() = isSlidFinished

        fun getState() = state

        fun getDstState() = dstState

        fun getCurrX() = currX

        fun setCurrX(x: Int) {
            currX = x
        }


        // loading 动画
        private var loadingAnimFrom = 0f
        private var loadingAnimTo = 0f
        private var loadingAnimProgress = 0f
        private var loadingAnimDuration = 0
        private var loadingAnimStartTime = 0L
        private var loadingAnimFinished = true

        fun startLoadingVisibilityAnim(from: Float, to: Float, duration: Int) {
            loadingAnimFrom = from
            loadingAnimProgress = from
            loadingAnimTo = to
            loadingAnimDuration = duration
            loadingAnimFinished = false
            loadingAnimStartTime = SystemClock.uptimeMillis()
        }

        fun abortLoadingVisibilityAnim() {
            loadingAnimFinished = true
        }

        fun computeLoadingVisibilityScroll(): Boolean {
            if (loadingAnimFinished) {
                return false
            }

            val timePassed = (SystemClock.uptimeMillis() - loadingAnimStartTime).toInt()
            if (timePassed < loadingAnimDuration) {
                val ratio = interpolator.getInterpolation(1f / loadingAnimDuration * timePassed)
                loadingAnimProgress = loadingAnimFrom + (loadingAnimTo - loadingAnimFrom) * ratio
            } else {
                loadingAnimProgress = loadingAnimTo
                loadingAnimFinished = true
            }
            return true
        }

        fun getLoadingAnimProgress() = loadingAnimProgress

        fun isLoadingVisibilityAnimFinished() = loadingAnimFinished
    }

}