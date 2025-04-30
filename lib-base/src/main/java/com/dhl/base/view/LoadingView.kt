package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.dhl.base.R
import com.dhl.base.utils.log


/**
 *
 * Author: duanhl
 * Create: 2023/9/8 14:28
 * Description:
 *
 */
class LoadingView : View {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var degrees: Float = 0f
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var ovalWidthRatio: Float = 5.5f
    private var ovalHeightRatio: Float = 2.5f
    private var ovalWidth: Float = 0f
    private var ovalHeight: Float = 0f
    private var w: Int = 0
    private var h: Int = 0
    private val path: Path = Path()
    private var more: Boolean = false
    private var startDegree: Float = 0f
    private var duration: Int = 6666
    private var startTime: Long = 0
    private var topOval: RectF = RectF()
    private var bottomOval: RectF = RectF()
    private var leftOval: RectF = RectF()
    private var rightOval: RectF = RectF()
    private var bgColor: Int = 0
    private var colors: IntArray =
        intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt())

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
            try {
                ovalWidthRatio =
                    typedArray.getFloat(R.styleable.LoadingView_oval_width, ovalWidthRatio)
                ovalHeightRatio =
                    typedArray.getFloat(R.styleable.LoadingView_oval_height, ovalHeightRatio)
                duration = typedArray.getInt(R.styleable.LoadingView_anim_duration, duration)
                bgColor = typedArray.getColor(R.styleable.LoadingView_bg_color, bgColor)
                val colorArr = typedArray.getString(R.styleable.LoadingView_colors)?.split(",")
                if (colorArr != null && colorArr.size != 3) {
                    log(level = Log.ERROR) { "color num is wrong, must be three" }
                }
                if (colorArr?.size == 3) {
                    colors = intArrayOf(
                        Color.parseColor(colorArr[0]),
                        Color.parseColor(colorArr[1]),
                        Color.parseColor(colorArr[2])
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }

        paint.style = Paint.Style.FILL
        more = true
        startTime = SystemClock.uptimeMillis()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        val availableW = w - paddingStart - paddingEnd
        val availableH = h - paddingTop - paddingBottom
        cx = paddingStart + availableW / 2f
        cy = paddingTop + availableH / 2f
        ovalWidth = availableW / ovalWidthRatio
        ovalHeight = availableH / ovalHeightRatio

        val halfWidth = ovalWidth / 2
        topOval.set(
            cx - halfWidth,
            paddingTop.toFloat(),
            cx + halfWidth,
            paddingTop + ovalHeight
        )
        bottomOval.set(
            cx - halfWidth,
            h - paddingBottom - ovalHeight,
            cx + halfWidth,
            h.toFloat() - paddingBottom
        )
        leftOval.set(
            paddingStart.toFloat(),
            cy - halfWidth,
            paddingStart + ovalHeight,
            cy + halfWidth
        )
        rightOval.set(
            w - paddingEnd - ovalHeight,
            cy - halfWidth,
            w.toFloat() - paddingEnd,
            cy + halfWidth
        )

        path.reset()
        path.addOval(topOval, Path.Direction.CCW)
        path.addOval(bottomOval, Path.Direction.CCW)
        path.addOval(leftOval, Path.Direction.CCW)
        path.addOval(rightOval, Path.Direction.CCW)
    }

    override fun computeScroll() {
        if (more) {
            val timePass = (SystemClock.uptimeMillis() - startTime).toInt()
            degrees = startDegree + timePass * 360f / duration
            invalidate()
        }
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    fun startAnim() {
        startDegree = degrees
        startTime = SystemClock.uptimeMillis()
        more = true
        invalidate()
    }

    fun stopAnim() {
        more = false
    }

    fun getDegree() = degrees

    fun setDegree(degrees: Float) {
        this.degrees = degrees
    }

    fun addDegree(degrees: Float) {
        this.degrees += degrees
    }

    fun setSizeRatio(wRatio: Float?, hRatio: Float?) {
        ovalWidthRatio = wRatio ?: ovalWidthRatio
        ovalHeightRatio = hRatio ?: ovalHeightRatio
        onSizeChanged(width, height, 0, 0)
    }

    fun setBgColor(bgColor: Int) {
        this.bgColor = bgColor
    }

    fun setColors(colors: IntArray) {
        if (colors.size != 3) {
            throw IllegalArgumentException("colors.size must be three")
        }
        this.colors = colors
    }

    override fun onDraw(canvas: Canvas) {

        drawBackground(canvas)

        val deg = degrees % 360
        when {
            deg == 0f -> {
                drawIntersect(canvas, 0f, 0f, *colors)
            }

            deg > 0 && deg <= 30 -> {
                drawCircle(canvas, colors[0])
                canvas.save()
                canvas.rotate(deg, cx, cy)
                drawCircle(canvas, blendColor(colors[1], colors[2]))
                canvas.restore()
                drawIntersect(canvas, 0f, deg, *colors)
            }

            deg > 30 && deg <= 60 -> {
                drawCircle(canvas, colors[0])
                canvas.save()
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(deg - 30, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.restore()
                drawIntersect(canvas, 0f, 30f, colors[0], colors[1])
                drawIntersect(canvas, 30f, deg - 30, colors[1], colors[2])
                drawIntersect(canvas, deg, 90 - deg, colors[0], colors[2])

                canvas.save()
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(deg - 30, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 60 && deg <= 90 -> {
                drawCircle(canvas, colors[0])
                canvas.save()
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(deg - 30, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.restore()
                drawIntersect(canvas, 0f, 30f, colors[0], colors[1])
                drawIntersect(canvas, deg, 90 - deg, colors[0], colors[2])
                drawIntersect(canvas, 30f, deg - 30, colors[1], colors[2])

                canvas.save()
                canvas.rotate(deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(90 - deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 90 && deg <= 120 -> {
                canvas.save()
                canvas.rotate(deg - 90, cx, cy)
                drawCircle(canvas, blendColor(colors[0], colors[2]))
                canvas.rotate(120 - deg, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.restore()
                drawIntersect(canvas, deg - 90, 120 - deg, *colors)
            }

            deg > 120 && deg <= 150 -> {
                canvas.save()
                canvas.rotate(120f, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.rotate(deg - 120f, cx, cy)
                drawCircle(canvas, blendColor(colors[0], colors[1]))
                canvas.restore()
                drawIntersect(canvas, 120f, deg - 120f, *colors)
            }

            deg > 150 && deg <= 180 -> {
                canvas.save()
                canvas.rotate(120f, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[0])
                canvas.rotate(deg - 150, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.restore()
                drawIntersect(canvas, 120f, 30f, colors[0], colors[2])
                drawIntersect(canvas, 150f, deg - 150, colors[0], colors[1])
                drawIntersect(canvas, deg, 210 - deg, colors[1], colors[2])

                canvas.save()
                canvas.rotate(120f, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(deg - 150, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 180 && deg <= 210 -> {
                canvas.save()
                canvas.rotate(deg - 180, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(210 - deg, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[0])
                canvas.restore()
                drawIntersect(canvas, deg, 210 - deg, colors[1], colors[2])
                drawIntersect(canvas, 120f, 30f, colors[0], colors[2])
                drawIntersect(canvas, 150f, deg - 150, colors[0], colors[1])

                canvas.save()
                canvas.rotate(deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(210 - deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 210 && deg <= 240 -> {
                canvas.save()
                canvas.rotate(deg, cx, cy)
                drawCircle(canvas, blendColor(colors[1], colors[2]))
                canvas.rotate(240 - deg, cx, cy)
                drawCircle(canvas, colors[0])
                canvas.restore()
                drawIntersect(canvas, deg, 240 - deg, *colors)
            }

            deg > 240 && deg <= 270 -> {
                canvas.save()
                canvas.rotate(240f, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(deg - 240, cx, cy)
                drawCircle(canvas, blendColor(colors[0], colors[2]))
                canvas.restore()
                drawIntersect(canvas, 240f, deg - 240, *colors)
            }

            deg > 270 && deg <= 300 -> {
                canvas.save()
                canvas.rotate(240f, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.rotate(deg - 270, cx, cy)
                drawCircle(canvas, colors[0])
                canvas.restore()
                drawIntersect(canvas, 240f, 30f, colors[1], colors[2])
                drawIntersect(canvas, 270f, deg - 270, colors[0], colors[2])
                drawIntersect(canvas, deg, 330 - deg, colors[0], colors[1])

                canvas.save()
                canvas.rotate(240f, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(deg - 270, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 300 && deg <= 330 -> {
                canvas.save()
                canvas.rotate(deg, cx, cy)
                drawCircle(canvas, colors[0])
                canvas.rotate(330 - deg, cx, cy)
                drawCircle(canvas, colors[1])
                canvas.rotate(30f, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.restore()
                drawIntersect(canvas, deg, 330 - deg, colors[0], colors[1])
                drawIntersect(canvas, 270f, deg - 270, colors[0], colors[2])
                drawIntersect(canvas, 330f, 30f, colors[1], colors[2])

                canvas.save()
                canvas.rotate(deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(330 - deg, cx, cy)
                canvas.clipPath(path)
                canvas.rotate(30f, cx, cy)
                canvas.clipPath(path)
                canvas.drawColor(blendColor(*colors))
                canvas.restore()
            }

            deg > 330 && deg <= 360 -> {
                canvas.save()
                canvas.rotate(deg, cx, cy)
                drawCircle(canvas, blendColor(colors[0], colors[1]))
                canvas.rotate(360 - deg, cx, cy)
                drawCircle(canvas, colors[2])
                canvas.restore()
                drawIntersect(canvas, deg, 360 - deg, *colors)
            }

        }
    }

    private fun drawBackground(canvas: Canvas) {
        if (bgColor == 0) {
            return
        }
        val c = bgColor
        var rotate = 0f
        canvas.save()
        while (rotate < 360) {
            canvas.rotate(rotate, cx, cy)
            drawCircle(canvas, c)
            rotate += 30f
        }
        canvas.restore()
    }

    private fun drawCircle(canvas: Canvas, color: Int) {
        paint.color = color
        canvas.drawOval(topOval, paint)
        canvas.drawOval(bottomOval, paint)
        canvas.drawOval(leftOval, paint)
        canvas.drawOval(rightOval, paint)
    }

    /**
     * 绘制两圆相交的部分
     */
    private fun drawIntersect(canvas: Canvas, degFrom: Float, dxDeg: Float, vararg colors: Int) {
        canvas.save()
        canvas.rotate(degFrom, cx, cy)
        canvas.clipPath(path)
        canvas.rotate(dxDeg, cx, cy)
        canvas.clipPath(path)
        canvas.drawColor(blendColor(*colors))
        canvas.restore()
    }

    private fun blendColor(vararg colors: Int): Int {
        var r = 0
        for (color in colors) {
            r = r or color
        }
        return r
    }
}