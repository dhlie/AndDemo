package com.dhl.base.pop

import android.content.Context
import android.graphics.Paint
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.dhl.base.R
import com.dhl.base.appWindowSize
import com.dhl.base.dp
import com.dhl.base.statusBarHeight
import com.dhl.base.utils.log
import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapePath
import kotlin.math.tan

/**
 *
 * Author: duanhl
 * Create: 2023/10/6 09:29
 * Description:
 *
 */

class PopMenuItem<T>(
    val text: String = "",
    val iconResId: Int = 0,
    val enable: Boolean = true,
    val data: T,
)

fun interface MenuClickListener<T> {
    fun onMenuClick(popWindow: PopupWindow, item: PopMenuItem<T>)
}

open class BasePop(private val context: Context, private val builder: Builder) : PopupWindow() {

    enum class TrianglePlacement {
        NONE, TOP, BOTTOM, START, END
    }

    private val container: SizeObserverFrameLayout

    private val triangleEdgeTreatment = object : EdgeTreatment() {
        override fun getEdgePath(length: Float, center: Float, interpolation: Float, shapePath: ShapePath) {

            /**使用贝塞尔曲线阴影有问题
             * [ShapePath.lineTo] shapePath.addArc 方法里有对阴影的处理
             * [ShapePath.quadToPoint] 方法里没有对阴影的处理
             */
            //val triangleSize = triangleSize * interpolation
            //val quadLength = 0//triangleSize * 10 / 32f //二次贝塞尔取消控制点到两个顶点的距离
            //shapePath.lineTo(center - triangleSize - quadLength, 0f)
            //val quadDx = (quadLength / 1.41421).toFloat()
            //shapePath.quadToPoint(
            //    center - triangleSize,
            //    0f,
            //    center - triangleSize + quadDx,
            //    0 - quadDx
            //)
            //shapePath.lineTo(center - quadDx, 0 - triangleSize + quadDx)
            //shapePath.quadToPoint(center, 0 - triangleSize, center + quadDx, 0 - triangleSize + quadDx)
            //shapePath.lineTo(center + triangleSize - quadDx, 0 - quadDx)
            //shapePath.quadToPoint(center + triangleSize, 0f, center + triangleSize + quadLength, 0f)
            //shapePath.lineTo(length, 0f)

            val triangleSize = builder.getTriangleSize() * interpolation
            val tangentLength = triangleSize * 10 / 32f //三角形左右角到曲线开始结束点的距离
            val arcRadius = (tan(Math.toRadians(67.5)) * tangentLength).toFloat()
            val tangentLengthTop = triangleSize * 8.5f / 32f //三角形顶点到曲线开始结束点的距离
            val arcRadiusTop = tangentLengthTop

            shapePath.lineTo(center - triangleSize - tangentLength, 0f)
            shapePath.addArc(
                center - triangleSize - tangentLength - arcRadius,
                -2 * arcRadius,
                center - triangleSize + (arcRadius - tangentLength),
                0f,
                90f,
                -45f
            )
            var dx = tangentLengthTop / 1.41421f
            shapePath.lineTo(center - dx, 0 - triangleSize + dx)
            shapePath.addArc(
                center - arcRadiusTop,
                0 - triangleSize + (tangentLengthTop * 1.41421f - tangentLengthTop),
                center + arcRadiusTop,
                0 - triangleSize + tangentLengthTop * 2 + (tangentLengthTop * 1.41421f - tangentLengthTop),
                225f,
                90f
            )

            dx = tangentLength / 1.41421f
            shapePath.lineTo(center + triangleSize - dx, 0 - dx)
            shapePath.addArc(
                center + triangleSize - (arcRadius - tangentLength),
                -2 * arcRadius,
                center + triangleSize + tangentLength + arcRadius,
                0f,
                135f,
                -45f
            )
            shapePath.lineTo(length, 0f)
        }
    }

    init {
        width = builder.width
        height = builder.height
        isFocusable = true
        animationStyle = builder.animationStyle
        container = SizeObserverFrameLayout(context)
        container.clipChildren = false
        container.clipToPadding = false
        //container.setBackgroundColor(0x10000000)

        contentView = builder.contentView
    }

    override fun setContentView(contentView: View?) {
        if (contentView == null) {
            super.setContentView(null)
        } else {
            container.removeAllViewsInLayout()
            container.addView(contentView)
            super.setContentView(container)
        }
    }

    /**
     * 更新 [builder.contentView] 的margin, 四周留出距离显示箭头和阴影
     */
    private fun updateContentMargin() {
        val contentView = builder.contentView ?: return
        val params = if (contentView.layoutParams == null) {
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                contentView.layoutParams = this
            }
        } else {
            if (contentView.layoutParams !is FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams(contentView.layoutParams)
            } else {
                contentView.layoutParams as FrameLayout.LayoutParams
            }
        }
        val margin = builder.contentMargin

        when (builder.trianglePlacement) {
            TrianglePlacement.NONE -> {
                params.topMargin = margin
                params.bottomMargin = margin
                params.marginStart = margin
                params.marginEnd = margin
            }

            TrianglePlacement.TOP -> {
                params.topMargin = margin + builder.getTriangleSize()
                params.bottomMargin = margin
                params.marginStart = margin
                params.marginEnd = margin
            }

            TrianglePlacement.BOTTOM -> {
                params.bottomMargin = margin + builder.getTriangleSize()
                params.topMargin = margin
                params.marginStart = margin
                params.marginEnd = margin
            }

            TrianglePlacement.START -> {
                params.marginStart = margin + builder.getTriangleSize()
                params.topMargin = margin
                params.bottomMargin = margin
                params.marginEnd = margin
            }

            TrianglePlacement.END -> {
                params.marginEnd = margin + builder.getTriangleSize()
                params.topMargin = margin
                params.bottomMargin = margin
                params.marginStart = margin
            }
        }
    }

    /**
     * 更新 [builder.contentView] 的背景和箭头位置
     */
    private fun updateContentBackground() {
        val contentView = builder.contentView ?: return
        val shapeModelBuilder = ShapeAppearanceModel.builder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(builder.cornerRadius.toFloat())
        val offsetEdgeTreatment = OffsetEdgeTreatment(triangleEdgeTreatment, builder.triangleOffset.toFloat())
        when (builder.trianglePlacement) {
            TrianglePlacement.NONE -> Unit

            TrianglePlacement.TOP -> {
                shapeModelBuilder.setTopEdge(offsetEdgeTreatment)

            }

            TrianglePlacement.BOTTOM -> {
                shapeModelBuilder.setBottomEdge(offsetEdgeTreatment)

            }

            TrianglePlacement.START -> {
                shapeModelBuilder.setLeftEdge(offsetEdgeTreatment)

            }

            TrianglePlacement.END -> {
                shapeModelBuilder.setRightEdge(offsetEdgeTreatment)

            }
        }

        val background = contentView.background as? MaterialShapeDrawable
        if (background == null) {
            val backgroundDrawable = MaterialShapeDrawable(shapeModelBuilder.build()).apply {
                setTint(builder.backgroundColor)
                paintStyle = Paint.Style.FILL

                //绘制阴影
                shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
                initializeElevationOverlay(context)
                setShadowColor(builder.shadowColor)
                elevation = builder.elevation.toFloat()
            }
            contentView.background = backgroundDrawable
        } else {
            background.shapeAppearanceModel = shapeModelBuilder.build()
        }
    }

    /**
     * 显示在坐标点的下边, 空间不足时显示在上边
     * 坐标([arrowX], [arrowY]) 为箭头顶点坐标
     * 箭头默认显示在边缘的中心点, 不显示时可视为大小为 0 的箭头
     *
     * @param arrowX 箭头顶点的 x 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的中点 x 坐标
     * @param arrowY 箭头顶点的 y 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的 bottom
     * @param offsetX 内容(不包含箭头)水平方向上的偏移
     * @param offsetY 箭头垂直方向上的偏移
     */
    fun showAtLocationDown(anchorView: View, arrowX: Int, arrowY: Int, offsetX: Int, offsetY: Int) {
        val displayView = builder.contentView ?: return

        val anchorPosition = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(anchorPosition)
        var arrX = arrowX
        var arrY = arrowY
        if (arrX == -1) {
            arrX = anchorPosition[0] + anchorView.measuredWidth / 2
        }
        if (arrY == -1) {
            arrY = anchorPosition[1] + anchorView.measuredHeight
        }

        //一定要使用 container 测量, 用 [displayView] 测量, 结果不会是 layoutParams 设置的宽高
        container.measure(0, 0)
        val contentWidth = displayView.measuredWidth
        val contentHeight = displayView.measuredHeight
        val contentMargin = builder.contentMargin
        val triangleSize = builder.getTriangleSize()

        if (builder.trianglePlacement != TrianglePlacement.NONE) {
            container.setOnSizeChangeListener { w, h, oldw, oldh ->
                if (w > 0 && h > 0) {
                    val popPosition = intArrayOf(0, 0)
                    container.getLocationOnScreen(popPosition)

                    val popCenterX = popPosition[0] + w / 2
                    val dx = arrX - popCenterX
                    builder.setTriangleOffset(if (builder.trianglePlacement == TrianglePlacement.TOP) -dx else dx)
                    log { "setOnSizeChangeListener:w:$w h:$h popcx:$popCenterX cx:$arrX offset:${builder.triangleOffset}" }
                    updateContentBackground()
                }
            }
        }

        if (arrY + contentMargin + contentHeight + triangleSize + offsetY >= appWindowSize.y) {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            } else if (builder.trianglePlacement == TrianglePlacement.TOP) {
                builder.setTrianglePlacement(TrianglePlacement.BOTTOM)
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin - contentWidth / 2 + offsetX,
                anchorPosition[1] - contentMargin - contentHeight - triangleSize - offsetY
            )
        } else {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin - contentWidth / 2 + offsetX,
                arrY - contentMargin + offsetY
            )
        }
    }

    /**
     * 显示在坐标点的上边, 空间不足时显示在下边
     * 坐标([arrowX], [arrowY]) 为箭头顶点坐标
     * 箭头默认显示在边缘的中心点, 不显示时可视为大小为 0 的箭头
     *
     * @param arrowX 箭头顶点的 x 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的中点 x 坐标
     * @param arrowY 箭头顶点的 y 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的 bottom
     * @param offsetX 内容(不包含箭头)水平方向上的偏移
     * @param offsetY 箭头垂直方向上的偏移
     */
    fun showAtLocationUp(anchorView: View, arrowX: Int, arrowY: Int, offsetX: Int, offsetY: Int) {
        val displayView = builder.contentView ?: return

        val anchorPosition = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(anchorPosition)
        var arrX = arrowX
        var arrY = arrowY
        if (arrX == -1) {
            arrX = anchorPosition[0] + anchorView.measuredWidth / 2
        }
        if (arrY == -1) {
            arrY = anchorPosition[1]
        }

        //一定要使用 container 测量, 用 [displayView] 测量, 结果不会是 layoutParams 设置的宽高
        container.measure(0, 0)
        val contentWidth = displayView.measuredWidth
        val contentHeight = displayView.measuredHeight
        val contentMargin = builder.contentMargin
        val triangleSize = builder.getTriangleSize()

        if (builder.trianglePlacement != TrianglePlacement.NONE) {
            container.setOnSizeChangeListener { w, h, oldw, oldh ->
                if (w > 0 && h > 0) {
                    val popPosition = intArrayOf(0, 0)
                    container.getLocationOnScreen(popPosition)

                    val popCenterX = popPosition[0] + w / 2
                    val dx = arrX - popCenterX
                    builder.setTriangleOffset(if (builder.trianglePlacement == TrianglePlacement.TOP) -dx else dx)
                    log { "setOnSizeChangeListener:w:$w h:$h popcx:$popCenterX cx:$arrX offset:${builder.triangleOffset}" }
                    updateContentBackground()
                }
            }
        }

        if (arrY - contentMargin - contentHeight - triangleSize - offsetY <= statusBarHeight) {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            } else if (builder.trianglePlacement == TrianglePlacement.BOTTOM) {
                builder.setTrianglePlacement(TrianglePlacement.TOP)
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin - contentWidth / 2 + offsetX,
                anchorPosition[1] + anchorView.measuredHeight - contentMargin + offsetY
            )
        } else {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin - contentWidth / 2 + offsetX,
                arrY - contentMargin - contentHeight - triangleSize - offsetY
            )
        }
    }

    /**
     * 显示在坐标点的左边, 空间不足时显示在右边
     * 坐标([arrowX], [arrowY]) 为箭头顶点坐标
     * 箭头默认显示在边缘的中心点, 不显示时可视为大小为 0 的箭头
     *
     * @param arrowX 箭头顶点的 x 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的 left
     * @param arrowY 箭头顶点的 y 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的的中点 y 坐标
     * @param offsetX 箭头水平方向上的偏移
     * @param offsetY 内容(不包含箭头)垂直方向上的偏移
     */
    fun showAtLocationLeft(anchorView: View, arrowX: Int, arrowY: Int, offsetX: Int, offsetY: Int) {
        val displayView = builder.contentView ?: return

        val anchorPosition = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(anchorPosition)
        var arrX = arrowX
        var arrY = arrowY
        if (arrX == -1) {
            arrX = anchorPosition[0]
        }
        if (arrY == -1) {
            arrY = anchorPosition[1] + anchorView.measuredHeight / 2
        }

        //一定要使用 container 测量, 用 [displayView] 测量, 结果不会是 layoutParams 设置的宽高
        container.measure(0, 0)
        val contentWidth = displayView.measuredWidth
        val contentHeight = displayView.measuredHeight
        val contentMargin = builder.contentMargin
        val triangleSize = builder.getTriangleSize()

        if (builder.trianglePlacement != TrianglePlacement.NONE) {
            container.setOnSizeChangeListener { w, h, oldw, oldh ->
                if (w > 0 && h > 0) {
                    val popPosition = intArrayOf(0, 0)
                    container.getLocationOnScreen(popPosition)

                    val popCenterY = popPosition[1] + h / 2
                    val dy = arrY - popCenterY
                    builder.setTriangleOffset(if (builder.trianglePlacement == TrianglePlacement.END) -dy else dy)
                    log { "setOnSizeChangeListener:w:$w h:$h popcx:$popCenterY cx:$arrX offset:${builder.triangleOffset}" }
                    updateContentBackground()
                }
            }
        }

        if (arrX - contentMargin - contentWidth - triangleSize - offsetX <= 0) {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            } else if (builder.trianglePlacement == TrianglePlacement.END) {
                builder.setTrianglePlacement(TrianglePlacement.START)
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX + anchorView.measuredWidth - contentMargin + offsetX,
                arrY - contentMargin - contentHeight / 2 + offsetY
            )
        } else {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin - contentWidth - triangleSize - offsetX,
                arrY - contentMargin - contentHeight / 2 + offsetY
            )
        }
    }


    /**
     * 显示在坐标点的右边, 空间不足时显示在左边
     * 坐标([arrowX], [arrowY]) 为箭头顶点坐标
     * 箭头默认显示在边缘的中心点, 不显示时可视为大小为 0 的箭头
     *
     * @param arrowX 箭头顶点的 x 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的 right
     * @param arrowY 箭头顶点的 y 坐标, use view.getLocationOnScreen() 获取;
     *               -1 时默认为 [anchorView] 的的中点 y 坐标
     * @param offsetX 箭头水平方向上的偏移
     * @param offsetY 内容(不包含箭头)垂直方向上的偏移
     */
    fun showAtLocationRight(anchorView: View, arrowX: Int, arrowY: Int, offsetX: Int, offsetY: Int) {
        val displayView = builder.contentView ?: return

        val anchorPosition = intArrayOf(0, 0)
        anchorView.getLocationOnScreen(anchorPosition)
        var arrX = arrowX
        var arrY = arrowY
        if (arrX == -1) {
            arrX = anchorPosition[0] + anchorView.measuredWidth
        }
        if (arrY == -1) {
            arrY = anchorPosition[1] + anchorView.measuredHeight / 2
        }

        //一定要使用 container 测量, 用 [displayView] 测量, 结果不会是 layoutParams 设置的宽高
        container.measure(0, 0)
        val contentWidth = displayView.measuredWidth
        val contentHeight = displayView.measuredHeight
        val contentMargin = builder.contentMargin
        val triangleSize = builder.getTriangleSize()

        if (builder.trianglePlacement != TrianglePlacement.NONE) {
            container.setOnSizeChangeListener { w, h, oldw, oldh ->
                if (w > 0 && h > 0) {
                    val popPosition = intArrayOf(0, 0)
                    container.getLocationOnScreen(popPosition)

                    val popCenterY = popPosition[1] + h / 2
                    val dy = arrY - popCenterY
                    builder.setTriangleOffset(if (builder.trianglePlacement == TrianglePlacement.END) -dy else dy)
                    log { "setOnSizeChangeListener:w:$w h:$h popcx:$popCenterY cx:$arrX offset:${builder.triangleOffset}" }
                    updateContentBackground()
                }
            }
        }

        if (arrX + contentMargin + contentWidth + triangleSize + offsetX >= appWindowSize.x) {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            } else if (builder.trianglePlacement == TrianglePlacement.START) {
                builder.setTrianglePlacement(TrianglePlacement.END)
            }
            updateContentMargin()
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - anchorView.measuredWidth - contentMargin - contentWidth - triangleSize - offsetX,
                arrY - contentMargin - contentHeight / 2 + offsetY
            )
        } else {
            if (builder.trianglePlacement == TrianglePlacement.NONE) {
                updateContentBackground()
            }
            updateContentMargin()
            showAtLocation(
                anchorView,
                Gravity.START or Gravity.TOP,
                arrX - contentMargin + offsetX,
                arrY - contentMargin - contentHeight / 2 + offsetY
            )
        }
    }

    open class Builder {

        companion object {
            internal const val INVALID_VALUE = -1
        }

        internal var width = FrameLayout.LayoutParams.WRAP_CONTENT
        internal var height = FrameLayout.LayoutParams.WRAP_CONTENT
        internal var cornerRadius = INVALID_VALUE
        private var triangleSize = INVALID_VALUE
        internal var trianglePlacement = TrianglePlacement.NONE
        internal var triangleOffset: Int = 0
        internal var backgroundColor: Int = 0
        internal var shadowColor: Int = INVALID_VALUE
        internal var elevation: Int = INVALID_VALUE
        internal var contentMargin: Int = INVALID_VALUE
        internal var animationStyle: Int = R.style.CenterDialogAnimation

        internal var contentView: View? = null

        fun getTriangleSize() = if (trianglePlacement == TrianglePlacement.NONE) 0 else triangleSize

        fun setWidth(w: Int): Builder {
            width = w
            return this
        }

        fun setHeight(h: Int): Builder {
            height = h
            return this
        }

        fun setCornerRadius(radius: Int): Builder {
            cornerRadius = radius
            return this
        }

        fun setTriangleSize(size: Int): Builder {
            triangleSize = size
            return this
        }

        fun setTrianglePlacement(placement: TrianglePlacement): Builder {
            trianglePlacement = placement
            return this
        }

        fun setTriangleOffset(offset: Int): Builder {
            triangleOffset = offset
            return this
        }

        open fun setContentView(content: View?): Builder {
            contentView = content
            return this
        }

        fun setBackgroundColor(color: Int): Builder {
            backgroundColor = color
            return this
        }

        fun setShadowColor(color: Int): Builder {
            shadowColor = color
            return this
        }

        fun setElevation(elevation: Int): Builder {
            this.elevation = elevation
            return this
        }

        fun setAnimationStyle(style: Int): Builder {
            animationStyle = style
            return this
        }

        fun setContentMargin(margin: Int): Builder {
            contentMargin = margin
            return this
        }

        open fun preBuild(context: Context) {
            val dp8 = 8.dp
            if (cornerRadius == INVALID_VALUE) {
                cornerRadius = dp8
            }
            if (triangleSize == INVALID_VALUE && trianglePlacement != TrianglePlacement.NONE) {
                triangleSize = dp8
            }
            if (elevation == INVALID_VALUE) {
                elevation = dp8// / 2
            }
            if (shadowColor == INVALID_VALUE) {
                shadowColor = context.getColor(R.color.shadow_color)
            }
            if (contentMargin == INVALID_VALUE) {
                contentMargin = elevation
            }
        }

        open fun build(context: Context): BasePop {
            preBuild(context)
            return BasePop(context, this)
        }
    }

}