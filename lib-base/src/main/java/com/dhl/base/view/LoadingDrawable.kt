package com.dhl.base.view

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.SystemClock
import com.dhl.base.ContextHolder
import com.dhl.base.R

/**
 *
 * Author: duanhl
 * Create: 2023/9/12 22:27
 * Description:
 *
 */
class LoadingDrawable(
    startColor: Int = ContextHolder.appContext.getColor(R.color.loading_start_color),
    endColor: Int = ContextHolder.appContext.getColor(R.color.loading_end_color),
    duration: Int = 1200,
) : Drawable() {

    private val lineCount: Int = 12
    private var colors: IntArray = IntArray(lineCount)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lineRect = RectF()
    private var roundCorner = 0f
    private val startTime: Long
    private val onStepDuration: Int
    private var alpha: Int = 255

    init {
        val argbEvaluator = ArgbEvaluator()
        for (i in 0 until lineCount) {
            val alpha: Float = i.toFloat() / (lineCount - 1)
            colors[i] = argbEvaluator.evaluate(alpha, startColor, endColor) as Int
        }
        paint.style = Paint.Style.FILL
        startTime = SystemClock.uptimeMillis()
        onStepDuration = duration / lineCount
    }

    override fun onBoundsChange(bounds: Rect) {
        val lineWidth = bounds.width() * 0.08f
        val lineHeight = bounds.height() * 0.28f
        val lineLeft = bounds.centerX() - lineWidth / 2
        lineRect.set(lineLeft, bounds.top.toFloat(), lineLeft + lineWidth, bounds.top + lineHeight)
        roundCorner = lineWidth / 2
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        val timePass = (SystemClock.uptimeMillis() - startTime).toInt()
        val stepDegree = 360 / lineCount
        val degree = timePass / onStepDuration * stepDegree
        val r = bounds.width() / 2f
        canvas.rotate(degree.toFloat(), bounds.left + r, bounds.top + r)
        for (i in 0 until lineCount) {
            paint.color = colors[i]
            paint.alpha = getAlpha()
            canvas.drawRoundRect(lineRect, roundCorner, roundCorner, paint)
            canvas.rotate(stepDegree.toFloat(), bounds.left + r, bounds.top + r)
        }
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int {
        return alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}