package com.dhl.base.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * Author: duanhaoliang
 * Create: 2021/4/12 10:08
 * Description:
 *
 * @param span 列数
 * @param horPadding 左右边距
 * @param topPadding 上边距
 * @param bottomPadding 下边距
 *
 *  ________________________________________________________________________________
 * |                       |                                 |                      |
 * |                       |                                 |                      |
 * |                  topPadding                        topPadding                  |
 * |                       |                                 |                      |
 * |                 ______|_____                      ______|_____                 |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |-- horPadding --|            |-- itemHorPadding --|            |-- horPadding --|
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |____________|                    |____________|                |
 * |                       |                                 |                      |
 * |                       |                                 |                      |
 * |                itemVerPadding                     itemVerPadding               |
 * |                       |                                 |                      |
 * |                 ______|_____                      ______|_____                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |-- horPadding --|            |-- itemHorPadding --|            |-- horPadding --|
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |            |                    |            |                |
 * |                |____________|                    |____________|                |
 * |                       |                                 |                      |
 * |                       |                                 |                      |
 * |                 bottomPadding                     bottomPadding                |
 * |                       |                                 |                      |
 * |_______________________|_________________________________|______________________|
 *
 *
 */
class GridDecoration(
    recyclerView: RecyclerView,
    private val span: Int,
    horPadding: Int,
    private val topPadding: Int,
    private val bottomPadding: Int,
    private val itemHorPadding: Int,
    private val itemVerPadding: Int,
) : RecyclerView.ItemDecoration() {

    init {
        recyclerView.setPadding(horPadding, 0, horPadding, 0)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view); // 获取view 在adapter中的位置。
        val column = position % span; // view 所在的列

        outRect.left = column * itemHorPadding / span; // column * (列间距 * (1f / 列数))
        outRect.right =
            itemHorPadding - (column + 1) * itemHorPadding / span; // 列间距 - (column + 1) * (列间距 * (1f /列数))

        val row =
            if (state.itemCount % span == 0) state.itemCount / span else state.itemCount / span + 1
        when {
            position < span -> {//第一行
                outRect.top = topPadding
                outRect.bottom =
                    if (position / span == row - 1) bottomPadding else itemVerPadding / 2
            }

            position / span == row - 1 -> {//最后一行
                outRect.top = itemVerPadding / 2
                outRect.bottom = bottomPadding
            }

            else -> {
                outRect.top = itemVerPadding / 2
                outRect.bottom = itemVerPadding / 2
            }
        }
    }
}