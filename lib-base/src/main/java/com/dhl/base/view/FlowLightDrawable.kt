package com.dhl.base.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.IntRange
import com.dhl.base.dp
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.tan

/**
 *
 * Author: Hello
 * Create: 2024/4/10 15:51
 * Description:
 *
 */
class FlowLightDrawable : Drawable() {

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var colors = intArrayOf(0x00FFFFFF, 0x0DFFFFFF, 0x00FFFFFF)
    private var positions = floatArrayOf(0f, 0.5f, 1f)
    private var duration: Int = 2000
    private var lightWidth = 20.dp
    private var angle = 30 //光影和垂直方向的夹角
    private var roundRadius = 0f
    private var backgroundColor: Int = 0
    private var mode: PorterDuff.Mode? = null

    private var roundRectPath: Path? = null
    private var lightFromX = 0
    private var lightToX = 0
    private var lightHeight = 0

    private var isRunning = true
    private var startTime = 0L

    override fun onBoundsChange(bounds: Rect) {
        reset()
    }

    fun setup(
        backgroundColor: Int = this.backgroundColor,
        roundRadius: Float = this.roundRadius,
        @IntRange(0, 90) angle: Int = this.angle,
        lightWidth: Int = this.lightWidth,
        colors: IntArray = this.colors,
        positions: FloatArray = this.positions,
        mode: PorterDuff.Mode? = this.mode,
    ) {
        this.backgroundColor = backgroundColor
        this.roundRadius = roundRadius
        this.angle = angle
        this.lightWidth = lightWidth
        this.colors = colors
        this.positions = positions
        this.mode = mode
        reset()
    }

    private fun reset() {
        if (bounds.isEmpty) {
            return
        }
        if (roundRadius < 1f) {
            roundRectPath = null
        } else {
            if (roundRectPath == null) {
                roundRectPath = Path()
            } else {
                roundRectPath?.reset()
            }
            roundRectPath?.addRoundRect(
                0f,
                0f,
                bounds.width().toFloat(),
                bounds.height().toFloat(),
                roundRadius,
                roundRadius,
                Path.Direction.CCW
            )
        }

        lightHeight = sqrt(bounds.width() * bounds.width().toDouble() + bounds.height() * bounds.height()).toInt()
        val radians = Math.toRadians(angle.toDouble())
        val a1 = if (angle == 0) 0 else (bounds.height() / 2f * tan(radians)).toInt()
        val a2 = lightWidth / cos(radians)
        lightFromX = -(a1 + a2).toInt()
        lightToX = bounds.width() + (bounds.height() / 2f * tan(radians)).toInt()

        val linearGradient = LinearGradient(
            0f,
            0f,
            lightWidth.toFloat(),
            0f,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        if (mode == null) {
            paint.setXfermode(null)
        } else {
            paint.setXfermode(PorterDuffXfermode(mode))
        }
        paint.shader = linearGradient
    }

    fun startAnim(startTime: Long, duration: Int = this.duration) {
        this.startTime = startTime
        this.duration = duration
        isRunning = true
        invalidateSelf()
    }

    fun stopAnim() {
        isRunning = false
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (!isRunning) {
            if (backgroundColor != 0) {
                val saveCount = canvas.save()
                roundRectPath?.let { canvas.clipPath(it) }
                canvas.drawColor(backgroundColor)
                canvas.restoreToCount(saveCount)
            }
            return
        }

        val timePass = System.currentTimeMillis() - startTime
        val percent = (timePass % duration).toFloat() / duration
        val currX = (lightFromX + (lightToX - lightFromX) * percent).toInt()
        val saveCount = canvas.save()
        roundRectPath?.let { canvas.clipPath(it) }
        canvas.drawColor(backgroundColor)
        canvas.translate(currX.toFloat(), 0f)
        canvas.rotate(angle.toFloat(), 0f, bounds.height() / 2f)
        val extra = (lightHeight - bounds.height()) / 2f
        canvas.drawRect(0f, -extra, lightWidth.toFloat(), bounds.height() + extra, paint)
        canvas.restoreToCount(saveCount)

        invalidateSelf()
        Log.i(
            "FlowLightDrawable",
            "@${hashCode()} percent:$percent currx:$currX fromX:$lightFromX toX:$lightToX lightHeight:$lightHeight bounds:$bounds angle:$angle"
        )
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}