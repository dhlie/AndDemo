package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.dhl.base.R
import com.dhl.base.dp
import com.dhl.base.utils.log
import com.dhl.base.sp
import kotlin.math.abs

/**
 *
 * Author: duanhl
 * Create: 2023/9/23 22:43
 * Description:
 *
 */
class WheelView : View {

    private val minScale = 0.05f
    private val boldStrokeWidth = 0.4f

    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var dividerHeight: Int = 0.5f.dp
    private var dividerColor: Int = context.getColor(R.color.line_bg)
    private var textColor: Int = context.getColor(R.color.text_disable)
    private var textSelectedColor: Int = context.getColor(R.color.text_primary)
    private var textSelectedBold: Boolean = false
    private var textPaddingVer = 4.dp
    private var wheelRadius = -1

    private var isCyclic = false
    private val itemHeight: Int
    private var originSelection = 0
    private var selection = 0
    private var topDividerY = 0
    private var bottomDividerY = 0
    private var transY = 0

    private val fontMetrics: FontMetrics
    private var baselineFromItemTop = 0f
    private var baselineFromItemBottom = 0f

    private var scrollFinishedWhenDown = true
    private var lastX = 0f
    private var lastY = 0f
    private var isMove = false
    private var touchSlop = 0
    private var velocityTracker: VelocityTracker? = null
    private var maxVelocity = 0f
    private var minVelocity = 0f

    private val scroller = Scroller(context)

    private var adapter: WheelAdapter? = null
    private var changeListener: OnWheelChangeListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.WheelView)
            try {
                val textSize = a.getDimension(R.styleable.WheelView_text_size, 16.sp)
                paint.textSize = textSize

                dividerHeight = a.getDimensionPixelSize(R.styleable.WheelView_divider_height, dividerHeight)
                dividerColor = a.getColor(R.styleable.WheelView_divider_color, dividerColor)
                textColor = a.getColor(R.styleable.WheelView_text_color, textColor)
                textSelectedColor = a.getColor(R.styleable.WheelView_text_selected_color, textSelectedColor)
                textSelectedBold = a.getBoolean(R.styleable.WheelView_text_selected_bold, textSelectedBold)
                isCyclic = a.getBoolean(R.styleable.WheelView_cyclic, isCyclic)

                textPaddingVer =
                    a.getDimensionPixelSize(R.styleable.WheelView_text_padding_vertical, textPaddingVer)
                if (a.hasValue(R.styleable.WheelView_wheel_radius)) {
                    wheelRadius = a.getDimensionPixelSize(R.styleable.WheelView_wheel_radius, wheelRadius)
                }
            } finally {
                a.recycle()
            }
        }

        paint.style = Paint.Style.FILL_AND_STROKE
        fontMetrics = paint.fontMetrics
        itemHeight = (fontMetrics.bottom - fontMetrics.top + textPaddingVer * 2).toInt()
        baselineFromItemTop = -fontMetrics.top + textPaddingVer
        baselineFromItemBottom = fontMetrics.bottom + textPaddingVer

        val vc = ViewConfiguration.get(context)
        touchSlop = vc.scaledTouchSlop
        maxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        minVelocity = vc.scaledMinimumFlingVelocity.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        topDividerY = (h - itemHeight) / 2
        bottomDividerY = (h + itemHeight) / 2
        if (wheelRadius == -1) {
            wheelRadius = (h * 7f / 10).toInt()
        }
    }

    fun getAdapter() = adapter

    fun setAdapter(adapter: WheelAdapter?) {
        this.adapter = adapter
        invalidate()
    }

    fun getSelection() = selection

    fun setSelection(selection: Int) {
        val old = this.selection
        this.selection = selection
        originSelection = selection
        transY = 0
        invalidate()

        if (selection != old) {
            changeListener?.onSelectionChange(this, selection)
        }
    }

    fun setOnWheelChangeListener(listener: OnWheelChangeListener?) {
        changeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (adapter == null || adapter!!.getCount() == 0) {
            return false
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isMove = false
                scrollFinishedWhenDown = scroller.isFinished
                scroller.forceFinished(true)
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val dx = x - lastX
                val dy = y - lastY
                if (!isMove && (abs(dx) >= touchSlop || abs(dy) >= touchSlop)) {
                    isMove = true
                    lastX = x
                    lastY = y
                    return true
                }
                if (isMove) {
                    scrollToY((transY + dy).toInt(), true)
                    lastX = x
                    lastY = y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isMove) {
                    if (scrollFinishedWhenDown) {
                        onClick(event.x, event.y)
                    } else {
                        adjustTransY(transY)
                    }
                } else {
                    velocityTracker!!.computeCurrentVelocity(1000, maxVelocity);
                    val yVel = clampMag(
                        velocityTracker!!.yVelocity,
                        minVelocity, maxVelocity
                    )
                    scroller.fling(0, transY, 0, yVel.toInt(), 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE)
                    invalidate()
                }

                if (velocityTracker != null) {
                    velocityTracker!!.recycle()
                    velocityTracker = null
                }
            }
        }

        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollToY(scroller.currY, false)
            if (scroller.isFinished) {
                val newSelection = getIndexByTransY(transY)
                if (selection != newSelection) {
                    selection = newSelection
                    changeListener?.onSelectionChange(this, selection)
                }
            }
        }
    }

    /**
     * Clamp the magnitude of value for absMin and absMax.
     * If the value is below the minimum, it will be clamped to zero.
     * If the value is above the maximum, it will be clamped to the maximum.
     *
     * @param value Value to clamp
     * @param absMin Absolute value of the minimum significant value to return
     * @param absMax Absolute value of the maximum value to return
     * @return The clamped value with the same sign as `value`
     */
    private fun clampMag(value: Float, absMin: Float, absMax: Float): Float {
        val absValue = abs(value)
        if (absValue < absMin) return 0f
        return if (absValue > absMax) {
            if (value > 0) absMax else -absMax
        } else {
            value
        }
    }

    private fun onClick(x: Float, y: Float) {
        if (y < topDividerY) {
            val startScaleBottom = topDividerY + fontMetrics.bottom - fontMetrics.descent + textPaddingVer
            var offset = 0
            var itemTop = topDividerY.toFloat()
            while (itemTop > y) {
                offset++
                // itemTop 是下一个 item 的 bottom
                val scale = calScale(startScaleBottom - itemTop)
                if (scale <= minScale) {
                    return
                }
                itemTop -= itemHeight * scale
            }
            val dy = offset * itemHeight
            val duration = when (offset) {
                0, 1 -> 300
                2 -> 350
                else -> 400
            }
            scroller.startScroll(0, transY, 0, dy, duration)
            invalidate()
        } else if (y > bottomDividerY) {
            val startScaleTop = bottomDividerY - (fontMetrics.ascent - fontMetrics.top) - textPaddingVer
            var offset = 0
            var itemBottom = bottomDividerY.toFloat()
            while (itemBottom < y) {
                offset++
                // itemBottom 是下一个 item 的 top
                val scale = calScale(itemBottom - startScaleTop)
                if (scale <= minScale) {
                    return
                }
                itemBottom += itemHeight * scale
            }
            val dy = offset * itemHeight
            val duration = when (offset) {
                0, 1 -> 300
                2 -> 350
                else -> 400
            }
            scroller.startScroll(0, transY, 0, -dy, duration)
            invalidate()
        }
    }

    private fun scrollToY(y: Int, isTouchScroll: Boolean) {
        var tmpTransY = y
        if (!isCyclic) {
            val offsetCount = tmpTransY / itemHeight
            val centerItemIndex = getIndex(originSelection - offsetCount)
            val centerItemOffset = tmpTransY % itemHeight

            if (centerItemIndex == adapter!!.getCount() - 1 && centerItemOffset < 0) {
                tmpTransY = offsetCount * itemHeight
                scroller.forceFinished(true)
            } else if (centerItemIndex == 0 && centerItemOffset > 0) {
                tmpTransY = offsetCount * itemHeight
                scroller.forceFinished(true)
            }
            //log { "offset:$offsetCount cindex:$centerItemIndex cOffY:$centerItemOffset" }
        }
        transY = tmpTransY
        if (!isTouchScroll && scroller.isFinished) {
            adjustTransY(tmpTransY)
        }
        invalidate()
    }

    private fun adjustTransY(currY: Int) {
        val centerItemOffsetY = currY % itemHeight
        if (centerItemOffsetY != 0) {
            val halfItemHeight = itemHeight / 2
            val dy = if (centerItemOffsetY < -halfItemHeight) {
                -(itemHeight + centerItemOffsetY)
            } else if (centerItemOffsetY < 0) {
                -centerItemOffsetY
            } else if (centerItemOffsetY < itemHeight / 2) {
                -centerItemOffsetY
            } else {
                itemHeight - centerItemOffsetY
            }
            val duration = 250 + abs(dy).toFloat() / halfItemHeight * 100

            scroller.startScroll(0, currY, 0, dy, duration.toInt())
            invalidate()
            //log { "offsetY:$centerItemOffsetY hei/2:${itemHeight / 2} dy:$dy toY:${transY + dy} mod:${(transY + dy) % itemHeight} dur:$duration" }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val adapter = adapter ?: return
        if (adapter.getCount() == 0) return

        val offsetItemCount = transY / itemHeight
        val centerItemIndex = getIndex(originSelection - offsetItemCount)
        val centerItemOffset = transY % itemHeight

        drawSelectedText(canvas, centerItemIndex, centerItemOffset)
        drawTopText(canvas, centerItemIndex, centerItemOffset)
        drawBottomText(canvas, centerItemIndex, centerItemOffset)

        drawDivider(canvas)
    }

    private fun drawSelectedText(canvas: Canvas, centerItemIndex: Int, centerItemOffset: Int) {
        canvas.save()
        canvas.clipRect(0f, topDividerY.toFloat(), width.toFloat(), bottomDividerY.toFloat())

        paint.color = textSelectedColor
        if (textSelectedBold) {
            paint.strokeWidth = boldStrokeWidth
        }
        val text = adapter!!.getText(centerItemIndex)
        val textWidth = paint.measureText(text)
        val textY = topDividerY + baselineFromItemTop + centerItemOffset
        canvas.drawText(text, (width - textWidth) / 2, textY, paint)

        if (centerItemOffset > 0) {
            val preIndex = getPreIndex(centerItemIndex)
            val preText = adapter!!.getText(preIndex)
            val tw = paint.measureText(preText)
            val ty = topDividerY + centerItemOffset - itemHeight + baselineFromItemTop
            canvas.drawText(preText, (width - tw) / 2, ty, paint)
        } else if (centerItemOffset < 0) {
            val nextIndex = getNextIndex(centerItemIndex)
            val nextText = adapter!!.getText(nextIndex)
            val tw = paint.measureText(nextText)
            val ty = topDividerY + centerItemOffset + itemHeight + baselineFromItemTop
            canvas.drawText(nextText, (width - tw) / 2, ty, paint)
        }
        paint.strokeWidth = 0f
        canvas.restore()
    }

    private fun drawTopText(canvas: Canvas, centerItemIndex: Int, centerItemOffset: Int) {
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), topDividerY.toFloat())
        paint.color = textColor

        val startScaleBottom = topDividerY + fontMetrics.bottom - fontMetrics.descent + textPaddingVer
        var index = centerItemIndex
        var itemBottom = topDividerY.toFloat() + centerItemOffset + itemHeight
        while (index >= 0 && itemBottom >= 0) {
            val text = adapter!!.getText(index)
            val textWidth = paint.measureText(text)

            val scale = calScale(startScaleBottom - itemBottom)
            if (scale <= minScale) {
                break
            }
            canvas.save()
            canvas.translate(width / 2f, itemBottom)
            canvas.scale(1f, scale)
            log { "top: $text scale:$scale itemBottom:$itemBottom topDividerY:$topDividerY itemHei:$itemHeight scaledItemHei:${itemHeight * scale}" }

            ////for debug
            //paint.style = Paint.Style.STROKE
            //paint.strokeWidth = 1f
            //val horOff = 10f
            //val verOff = 0f
            //canvas.drawRect(
            //    -width / 2f + horOff,
            //    -itemHeight + verOff,
            //    width / 2f - horOff,
            //    -verOff,
            //    paint
            //)
            //paint.style = Paint.Style.FILL

            canvas.drawText(text, -textWidth / 2f, -baselineFromItemBottom, paint)
            canvas.restore()

            itemBottom -= itemHeight * scale
            index = getPreIndex(index)
        }

        canvas.restore()
    }

    private fun drawBottomText(canvas: Canvas, centerItemIndex: Int, centerItemOffsetY: Int) {
        canvas.save()
        canvas.clipRect(0f, bottomDividerY.toFloat(), width.toFloat(), height.toFloat())
        paint.color = textColor

        val startScaleTop = bottomDividerY - (fontMetrics.ascent - fontMetrics.top) - textPaddingVer
        var index = centerItemIndex
        var itemTop = topDividerY + centerItemOffsetY.toFloat()
        while (index >= 0 && itemTop <= height) {
            val text = adapter!!.getText(index)
            val textWidth = paint.measureText(text)

            val scale = calScale(itemTop - startScaleTop)
            if (scale <= minScale) {
                break
            }
            canvas.save()
            canvas.translate(width / 2f, itemTop)
            canvas.scale(1f, scale)
            log { "bottom: $text scale:$scale itemTop:$itemTop bottomDividerY:$bottomDividerY hei:$height itemHei:$itemHeight scaledItemHei:${itemHeight * scale}" }

            ////for debug
            //paint.style = Paint.Style.STROKE
            //paint.strokeWidth = 1f
            //val horOff = 10f
            //val verOff = 0f
            //canvas.drawRect(
            //    -width / 2f + horOff,
            //    verOff,
            //    width / 2f - horOff,
            //    itemHeight - verOff,
            //    paint
            //)
            //paint.style = Paint.Style.FILL

            canvas.drawText(text, -textWidth / 2f, baselineFromItemTop, paint)
            canvas.restore()

            itemTop += itemHeight * scale
            index = getNextIndex(index)
        }

        canvas.restore()
    }

    private fun drawDivider(canvas: Canvas) {
        paint.color = dividerColor
        val topDividerTop = topDividerY - dividerHeight / 2f
        canvas.drawRect(0f, topDividerTop, width.toFloat(), topDividerTop + dividerHeight, paint)

        val bottomDividerTop = bottomDividerY - dividerHeight / 2f
        canvas.drawRect(0f, bottomDividerTop, width.toFloat(), bottomDividerTop + dividerHeight, paint)
    }

    private fun getIndexByTransY(transY: Int): Int {
        val offsetItemCount = transY / itemHeight
        return getIndex(originSelection - offsetItemCount)
    }

    private fun getIndex(unsafeIndex: Int): Int {
        val itemCount = adapter!!.getCount()
        var index = 0
        if (isCyclic) {
            index = if (unsafeIndex > 0) {
                unsafeIndex % itemCount
            } else {
                val mod = unsafeIndex % itemCount
                if (mod == 0) {
                    0
                } else {
                    itemCount + mod
                }
            }
        } else {
            val lastIndex = itemCount - 1
            index = if (unsafeIndex > lastIndex) {
                lastIndex
            } else if (unsafeIndex < 0) {
                0
            } else {
                unsafeIndex
            }
        }
        return index
    }

    private fun getPreIndex(index: Int): Int {
        return if (index == 0) {
            if (isCyclic) {
                adapter!!.getCount() - 1
            } else {
                -1
            }
        } else {
            index - 1
        }
    }

    private fun getNextIndex(index: Int): Int {
        return if (index == adapter!!.getCount() - 1) {
            if (isCyclic) {
                0
            } else {
                -1
            }
        } else {
            index + 1
        }
    }

    private fun calScale(dyToStartScaleY: Float): Float {
        if (wheelRadius == 0 || dyToStartScaleY <= 0) {
            return 1f
        }
        //缩放计算公式
        // y = (-1f / wheelRadius) * x + 1
        var scale = (-1f / wheelRadius) * dyToStartScaleY + 1
        if (scale < 0f) {
            scale = 0f
        }
        return scale
    }

    abstract class WheelAdapter {

        /**
         * item 个数
         */
        abstract fun getCount(): Int

        /**
         * 获取 position 未知对应的data
         * @param position
         */
        abstract fun getItem(position: Int): Any?

        /**
         * 获取 position 未知对应的文字
         * @param position
         */
        abstract fun getText(position: Int): String

    }

    fun interface OnWheelChangeListener {
        fun onSelectionChange(wheelView: WheelView, selection: Int)
    }
}