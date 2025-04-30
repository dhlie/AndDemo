package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.dhl.base.dp
import com.dhl.base.utils.log
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 * Author: duanhl
 * Create: 2023/9/23 16:44
 * Description:
 *
 */
class CheckView : View {
    private var mColorSelect = 0
    private var mColorUnSelect = 0
    private var mColorBackground = 0
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTransTime = 300 //ms
    private var tick1StartX = 0f
    private var tick1StartY = 0f
    private var tick1EndX = 0f
    private var tick1EndY = 0f
    private var tick2EndX = 0f
    private var tick2EndY = 0f

    private var mChecked = false
    private var mScroller: Scroller
    private var mPercent = 0
    private val mStrokeWidth: Int = 1.2f.dp
    private var mRectF: RectF
    private var mLength = 0f //线条总长度

    private var mLengthCircle = 0f //圆周长
    private var mLengthTickLeft = 0f //对号左边长度
    private var mLengthTickRight = 0f //对号右边长度

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mColorSelect = context.getColor(com.dhl.base.R.color.colorControlActivated)
        mColorUnSelect = context.getColor(com.dhl.base.R.color.colorControlNormal)
        mColorBackground = mColorSelect
        mScroller = Scroller(context, LinearInterpolator())
        mRectF = RectF()
        mPaint.strokeCap = Paint.Cap.ROUND
    }

    fun setChecked(checked: Boolean, anim: Boolean) {
        if (mChecked == checked) return
        mChecked = checked
        mScroller.forceFinished(true)
        if (anim) {
            if (checked) {
                mScroller.startScroll(mPercent, 0, 100 - mPercent, 0, (mTransTime * 0.01 * (100 - mPercent)).toInt())
            } else {
                mScroller.startScroll(mPercent, 0, 0 - mPercent, 0, (mTransTime * 0.01 * mPercent).toInt())
            }
        } else {
            mPercent = if (checked) {
                100
            } else {
                0
            }
        }
        invalidate()
    }

    fun isChecked(): Boolean {
        return mChecked
    }

    override fun setSelected(selected: Boolean) {
        setChecked(selected, true)
    }

    fun setSelected(selected: Boolean, anim: Boolean) {
        setChecked(selected, anim)
    }

    override fun isSelected(): Boolean {
        return isChecked()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val d = (h - paddingTop - paddingBottom).toFloat() //圆的直径
        val left = paddingStart.toFloat()
        val top = paddingTop.toFloat()
        mRectF.set(left, top, left + d, top + d)
        mLengthCircle = 0f//(Math.PI * d).toFloat()

        val tick1StartXPer = 0.25f
        val tick1StartYPer = 0.45789f
        val tick1EndXPer = 0.42126f
        val tick1EndYPer = 0.6505f
        val tick2EndXPer = 1 - tick1StartXPer//0.77368f
        val tick2EndYPer = 1 - tick1EndYPer

        tick1StartX = paddingStart + d * tick1StartXPer
        tick1StartY = paddingTop + d * tick1StartYPer
        tick1EndX = paddingStart + d * tick1EndXPer
        tick1EndY = paddingTop + d * tick1EndYPer
        tick2EndX = paddingStart + d * tick2EndXPer
        tick2EndY = paddingTop + d * tick2EndYPer

        mLengthTickLeft =
            sqrt(abs(tick1EndX - tick1StartX).toDouble().pow(2.0) + abs(tick1EndY - tick1StartY).toDouble().pow(2.0))
                .toFloat()
        mLengthTickRight =
            sqrt(abs(tick2EndX - tick1EndX).toDouble().pow(2.0) + abs(tick2EndY - tick1EndY).toDouble().pow(2.0))
                .toFloat()

        mLength = mLengthCircle + mLengthTickLeft + mLengthTickRight
        log { "mLengthCircle:$mLengthCircle   mLength$mLength   mRectF:$mRectF" }
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mPercent = mScroller.currX
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val d = mRectF.height()
        val per = mPercent * 0.01f


        //画圆
        mPaint.color = mColorUnSelect
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mStrokeWidth.toFloat()
        canvas.drawCircle(centerX, centerY, d / 2, mPaint)

        //画√
        canvas.drawLine(
            tick1StartX,
            tick1StartY,
            tick1EndX,
            tick1EndY,
            mPaint
        )
        canvas.drawLine(
            tick1EndX,
            tick1EndY,
            tick2EndX,
            tick2EndY,
            mPaint
        )

        //画背景
        mPaint.color = mColorBackground
        mPaint.alpha = (255 * per).toInt()
        mPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(centerX, centerY, d / 2, mPaint)

        mPaint.style = Paint.Style.STROKE
        mPaint.color = mColorSelect
        val curLen = per * mLength

        //画选中圆圈
        //var swapAngle = if (curLen > mLengthCircle) 360 else (curLen / mLengthCircle * 360).toInt()
        //swapAngle = if (swapAngle > 360) 360 else swapAngle
        //canvas.drawArc(mRectF, (270 - swapAngle).toFloat(), swapAngle.toFloat(), false, mPaint)

        val lineLen = curLen - mLengthCircle
        if (lineLen <= 0) return

        //画选中√
        mPaint.color = Color.WHITE
        if (lineLen > mLengthTickLeft) {
            canvas.drawLine(
                tick1StartX,
                tick1StartY,
                tick1EndX,
                tick1EndY,
                mPaint
            )
            val per = (lineLen - mLengthTickLeft) / mLengthTickRight
            canvas.drawLine(
                tick1EndX,
                tick1EndY,
                tick1EndX + (tick2EndX - tick1EndX) * per,
                tick1EndY + (tick2EndY - tick1EndY) * per,
                mPaint
            )
        } else {
            val per = lineLen / mLengthTickLeft
            canvas.drawLine(
                tick1StartX,
                tick1StartY,
                tick1StartX + (tick1EndX - tick1StartX) * per,
                tick1StartY + (tick1EndY - tick1StartY) * per,
                mPaint
            )
        }
    }
}