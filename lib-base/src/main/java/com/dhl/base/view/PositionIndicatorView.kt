package com.dhl.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.dhl.base.R
import kotlin.math.roundToInt

/**
 *
 * Author: duanhaoliang
 * Create: 2022/1/20 14:17
 * Description:
 *
 */
class PositionIndicatorView : View {

    private var circleRadius: Float = 0f
    private var circlePadding: Int = 0
    private var selectColor: Int = 0
    private var unselectColor: Int = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var amount: Int = 0
    private var currPos = 0                 //当前位置
    private var posOffset = 0f              //位置偏移百分比

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
        val a = context.obtainStyledAttributes(attrs, R.styleable.PositionIndicatorView)
        circleRadius = a.getDimension(R.styleable.PositionIndicatorView_circleRadius, 0f)
        circlePadding =
            a.getDimensionPixelOffset(R.styleable.PositionIndicatorView_circlePadding, 0)
        selectColor = a.getColor(R.styleable.PositionIndicatorView_selectColor, 0)
        unselectColor = a.getColor(R.styleable.PositionIndicatorView_unselectColor, 0)
        a.recycle()

        paint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width =
            paddingStart + paddingEnd + circleRadius * 2 * amount + circlePadding * (amount - 1)
        val height = paddingTop + paddingBottom + circleRadius * 2
        setMeasuredDimension((width + 0.5).roundToInt(), (height + 0.5).roundToInt())
    }

    fun setAmount(amount: Int) {
        this.amount = amount
        requestLayout()
    }

    fun setIndicatorPosition(position: Int, positionOffset: Float) {
        currPos = position
        posOffset = positionOffset
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (amount <= 0) {
            return
        }

        val cy = measuredHeight / 2f
        val centerDx = circleRadius * 2 + circlePadding //两个圆心距离
        val firstCx = paddingStart + circleRadius
        //绘制未选中状态
        paint.color = unselectColor
        for (index in 0 until amount) {
            canvas.drawCircle(firstCx + centerDx * index, cy, circleRadius, paint)
        }

        //绘制选中状态
        paint.color = selectColor
        val cx = paddingStart + circleRadius + centerDx * currPos + centerDx * posOffset
        canvas.drawCircle(cx, cy, circleRadius, paint)
    }

}