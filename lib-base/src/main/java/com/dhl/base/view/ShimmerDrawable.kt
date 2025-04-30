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
import android.os.Looper
import android.util.Log
import androidx.annotation.FloatRange
import com.dhl.base.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.tan

/**
 *
 * Author: Hello
 * Create: 2024/4/10 15:51
 * Description:
 *
 */
class ShimmerDrawable : Drawable() {

    private val DEBUG = true

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var colors = intArrayOf(0x00FFFFFF, 0xFFFFFFFF.toInt(), 0x00FFFFFF)
    private var positions = floatArrayOf(0f, 0.5f, 1f)
    private var duration: Int = 2000
    private var lightWidth = 20.dp
    private var angle = 30f //光影和垂直方向的夹角,范围(-90, 90)
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
        @FloatRange(-90.0, 90.0, fromInclusive = false, toInclusive = false)angle: Float = this.angle,
        lightWidth: Int = this.lightWidth,
        colors: IntArray = this.colors,
        positions: FloatArray = this.positions,
        mode: PorterDuff.Mode? = this.mode,
        duration: Int = this.duration,
    ) {
        if (Looper.myLooper() !== Looper.getMainLooper()) {
            throw IllegalStateException("must be called on main thread")
        }
        this.backgroundColor = backgroundColor
        this.roundRadius = roundRadius
        this.angle = angle
        this.lightWidth = lightWidth
        this.colors = colors
        this.positions = positions
        this.mode = mode
        this.duration = duration
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

        val radians = Math.toRadians(abs(angle.toDouble()))
        if (angle >= 0) {
            val d1 = (bounds.height() * tan(radians)).toInt()
            val d2 = lightWidth / cos(radians)
            lightFromX = -(d1 + d2).toInt()
            lightToX = bounds.width()
            lightHeight = (tan(radians) * lightWidth + bounds.height() / cos(radians)).toInt()
        } else {
            val d1 = tan(radians) *  bounds.height()
            val d2 = lightWidth / cos(radians)
            lightFromX = -(d1 + d2).toInt()
            lightToX = bounds.width()
            lightHeight = (bounds.height() / cos(radians) + tan(radians) * lightWidth).toInt()
        }
        invalidateSelf()
    }

    fun startAnim(startTime: Long) {
        this.startTime = startTime
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
        if (angle >= 0) {
            canvas.translate(currX.toFloat(), bounds.height().toFloat())
            canvas.rotate(angle, 0f, 0f)
            canvas.drawRect(0f, -lightHeight.toFloat(), lightWidth.toFloat(), 0f, paint)
        } else {
            canvas.translate(currX.toFloat(), 0f)
            canvas.rotate(angle, 0f, 0f)
            canvas.drawRect(0f, 0f, lightWidth.toFloat(), lightHeight.toFloat(), paint)
        }

        canvas.restoreToCount(saveCount)

        invalidateSelf()

        if (DEBUG) {
            Log.i(
                "FlowLightDrawable",
                "@${hashCode()} percent:$percent currx:$currX fromX:$lightFromX toX:$lightToX lightHeight:$lightHeight bounds:$bounds angle:$angle"
            )
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}