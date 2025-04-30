//package com.dhl.base.recyclerview
//
//import android.graphics.Canvas
//import android.graphics.Paint
//import android.graphics.Rect
//import android.view.View
//import androidx.recyclerview.widget.RecyclerView
//import com.dhl.base.ContextHolder
//import com.dhl.base.R
//import com.dhl.base.dp
//
///**
// *
// * Author: duanhaoliang
// * Create: 2021/4/12 10:08
// * Description:
// *
// */
//class RecyclerViewDividerDecoration(
//    private val leftPadding: Int = 16.dp,
//    private val rightPadding: Int = 16.dp,
//    private val height: Int = 0.5f.dp,
//    color: Int = ContextHolder.appContext.getColor(R.color.line_bg),
//    private val showLastItemDivider: Boolean = false,
//) : RecyclerView.ItemDecoration() {
//
//    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
//    private val mBounds = Rect()
//
//    init {
//        paint.color = color
//        paint.style = Paint.Style.FILL
//    }
//
//    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
//        val layoutManager = parent.layoutManager ?: return
//        val left = leftPadding
//        val right = parent.width - rightPadding
//        val childCount = parent.childCount
//
//        val adapter = parent.adapter
//        if (adapter is HeaderFooterAdapter<*, *>) {
//            for (i in 0 until childCount) {
//                val child = parent.getChildAt(i)
//                val viewHolder = parent.getChildViewHolder(child)
//                val isLoadMore = viewHolder.itemViewType == HeaderFooterAdapter.TYPE_LOAD_MORE
//                if (isLoadMore) {
//                    break
//                }
//                if (showLastItemDivider) {
//                    val bottom: Int = layoutManager.getDecoratedBottom(child)
//                    val top: Int = bottom - height
//                    c.drawRect(
//                        left.toFloat(),
//                        top.toFloat(),
//                        right.toFloat(),
//                        bottom.toFloat(),
//                        paint
//                    )
//                } else {
//                    val adapterPos = parent.getChildAdapterPosition(child)
//                    if (adapterPos == state.itemCount - 1) {
//                        break //最后一个
//                    }
//                    if (adapter.getItemViewType(adapterPos + 1) == HeaderFooterAdapter.TYPE_LOAD_MORE) {
//                        break //下一个是 LoadMoreView
//                    }
//                    val bottom: Int = layoutManager.getDecoratedBottom(child)
//                    val top: Int = bottom - height
//                    c.drawRect(
//                        left.toFloat(),
//                        top.toFloat(),
//                        right.toFloat(),
//                        bottom.toFloat(),
//                        paint
//                    )
//                }
//            }
//        } else {
//            for (i in 0 until childCount) {
//                val child = parent.getChildAt(i)
//                val adapterPos = parent.getChildAdapterPosition(child)
//                if (!showLastItemDivider && adapterPos == state.itemCount - 1) {
//                    break //最后一个
//                }
//                val top: Int
//                val bottom: Int
//                if (showLastItemDivider) {
//                    bottom = layoutManager.getDecoratedBottom(child)
//                    top = bottom - height
//                } else {
//                    top = (layoutManager.getDecoratedBottom(child) - height / 2f).toInt()
//                    bottom = top + height
//                }
//                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
//            }
//        }
//
//    }
//
//    override fun getItemOffsets(
//        outRect: Rect,
//        view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State,
//    ) {
//        outRect.set(0, 0, 0, height)
//    }
//}

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dhl.base.recyclerview

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.dhl.base.ContextHolder
import com.dhl.base.R
import com.dhl.base.dp
import kotlin.math.roundToInt

/**
 * DividerItemDecoration is a [RecyclerView.ItemDecoration] that can be used as a divider
 * between items of a [LinearLayoutManager]. It supports both [.HORIZONTAL] and
 * [.VERTICAL] orientations.
 *
 * <pre>
 * mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
 * mLayoutManager.getOrientation());
 * recyclerView.addItemDecoration(mDividerItemDecoration);
</pre> *
 */
class DividerDecoration : RecyclerView.ItemDecoration {

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL
    }

    private var mOrientation: Int = LinearLayout.VERTICAL
    private var mMarginStart: Int = 0
    private var mMarginEnd: Int = 0
    private var mDividerHeight: Int = 0
    private var isShowFirstItemDivider = false
    private var isShowLastItemDivider = false
    private val mBounds = Rect()

    /**
     * @return the [Drawable] for this divider.
     */
    var drawable: Drawable

    /**
     * Creates a divider [RecyclerView.ItemDecoration] that can be used with a
     * [LinearLayoutManager].
     *
     * 只控制非 header, footer, loadmore item 的 divider, 这三种 item 应该通过自身布局设置间距
     *
     * @param orientation 使用 LinearLayoutManager.orientation.
     * @param marginStart 距离 RecyclerView 的左右(orientation =VERTICAL)/上下(orientation = HORIZONTAL)边距
     * @param marginEnd 距离 RecyclerView 的左右(orientation =VERTICAL)/上下(orientation = HORIZONTAL)边距
     * @param dividerHeight divider 高度
     * @param color divider 颜色
     * @param showFirstItemDivider 第一个非 header,footer,loadmore item 的上方是否显示 divider
     * @param showLastItemDivider 最后个非 header,footer,loadmore item 的下方是否显示 divider
     */
    constructor(
        orientation: Int,
        marginStart: Int = 0,
        marginEnd: Int = 0,
        dividerHeight: Int = 0.5f.dp,
        color: Int = ContextHolder.appContext.getColor(R.color.line_bg),
        showFirstItemDivider: Boolean = false,
        showLastItemDivider: Boolean = false,
    ) {
        setOrientation(orientation)
        mMarginStart = marginStart
        mMarginEnd = marginEnd
        mDividerHeight = dividerHeight
        isShowFirstItemDivider = showFirstItemDivider
        isShowLastItemDivider = showLastItemDivider
        drawable = ColorDrawable(color)
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL && orientation != VERTICAL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
        mOrientation = orientation
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        if (mOrientation == VERTICAL) {
            drawOrientationVertical(c, parent, state)
        } else {
            drawOrientationHorizontal(c, parent, state)
        }
    }

    private fun drawOrientationVertical(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft + mMarginStart
            right = parent.width - parent.paddingRight - mMarginEnd
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = mMarginStart
            right = parent.width - mMarginEnd
        }

        val adapter = parent.adapter as ViewBindingAdapter<*, *>
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val adapterPos = parent.getChildAdapterPosition(child)
            val itemType = adapter.getItemViewType(adapterPos)
            if (
                itemType == HeaderFooterAdapter.TYPE_HEADER
                || itemType == HeaderFooterAdapter.TYPE_FOOTER
                || itemType == HeaderFooterAdapter.TYPE_LOAD_MORE
            ) {
                continue
            }

            val posInData = if (adapter is HeaderFooterAdapter<*, *>) {
                adapter.getPosInData(adapterPos)
            } else {
                adapterPos
            }

            parent.getDecoratedBoundsWithMargins(child, mBounds)
            if (posInData == 0 && isShowFirstItemDivider) {
                val top = mBounds.top + child.translationY.roundToInt()
                val bottom = top + mDividerHeight
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
            if (posInData != adapter.getDataSize() - 1 || isShowLastItemDivider) {
                val bottom = mBounds.bottom + child.translationY.roundToInt()
                val top = bottom - mDividerHeight
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }
        canvas.restore()
    }

    private fun drawOrientationHorizontal(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop + mMarginStart
            bottom = parent.height - parent.paddingBottom - mMarginEnd
            canvas.clipRect(
                parent.paddingLeft, top,
                parent.width - parent.paddingRight, bottom
            )
        } else {
            top = mMarginStart
            bottom = parent.height - mMarginEnd
        }

        val adapter = parent.adapter as ViewBindingAdapter<*, *>
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val adapterPos = parent.getChildAdapterPosition(child)
            val itemType = adapter.getItemViewType(adapterPos)
            if (
                itemType == HeaderFooterAdapter.TYPE_HEADER
                || itemType == HeaderFooterAdapter.TYPE_FOOTER
                || itemType == HeaderFooterAdapter.TYPE_LOAD_MORE
            ) {
                continue
            }

            val posInData = if (adapter is HeaderFooterAdapter<*, *>) {
                adapter.getPosInData(adapterPos)
            } else {
                adapterPos
            }

            parent.getDecoratedBoundsWithMargins(child, mBounds)
            if (posInData == 0 && isShowFirstItemDivider) {
                val left = mBounds.left + child.translationX.roundToInt()
                val right = left + mDividerHeight
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
            if (posInData != adapter.getDataSize() - 1 || isShowLastItemDivider) {
                val right = mBounds.right + child.translationX.roundToInt()
                val left = right - mDividerHeight
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val adapter = parent.adapter as ViewBindingAdapter<*, *>
        val adapterPos = parent.getChildAdapterPosition(view)
        val itemType = adapter.getItemViewType(adapterPos)
        if (
            itemType == HeaderFooterAdapter.TYPE_HEADER
            || itemType == HeaderFooterAdapter.TYPE_FOOTER
            || itemType == HeaderFooterAdapter.TYPE_LOAD_MORE
        ) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val posInData = if (adapter is HeaderFooterAdapter<*, *>) {
            adapter.getPosInData(adapterPos)
        } else {
            adapterPos
        }

        var top = 0
        var bottom = mDividerHeight
        if (posInData == 0 && isShowFirstItemDivider) {
            top = mDividerHeight
        }
        if (posInData == adapter.getDataSize() - 1 && !isShowLastItemDivider) {
            bottom = 0
        }

        if (mOrientation == VERTICAL) {
            outRect.set(0, top, 0, bottom)
        } else {
            outRect.set(top, 0, bottom, 0)
        }
    }

}