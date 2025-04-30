package com.dhl.base.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.viewbinding.ViewBinding
import com.dhl.base.R

/**
 *
 * Author: duanhaoliang
 * Create: 2021/7/1 13:42
 * Description:
 *
 * posInParent - 在 Parent list 中的位置
 * posInChild - 在 Child list 中的位置
 * posInParentAndChild - 在 Parent + 展开的 Child list 中的位置
 *
 * ==================================
 * ----------------------
 * |    Header           |     posInParentAndChild
 * ----------------------                                       posInParent (data中的位置)
 * ----------------------
 * |    Group            |              0        posInChild         0
 * ----------------------
 * -------------------------------
 * |        |    Child            |     1           0
 * -------------------------------
 * -------------------------------
 * |        |    Child            |     2           1
 * -------------------------------
 * ----------------------                       posInChild
 * |    Group            |              3                           1
 * ----------------------
 * -------------------------------
 * |        |    Child            |     4
 * -------------------------------
 * -------------------------------
 * |        |    Child            |     5
 * -------------------------------
 * ----------------------
 * |    Group            |              6                           2
 * ----------------------
 * ----------------------
 * |    Group            |              7                           3
 * ----------------------
 * -------------------------------                              posInParent
 * |        |    Child            |     8
 * -------------------------------      ↑
 * ----------------------               |
 * |    Footer            |             |
 * ----------------------               |
 *                                      |
 *                              posInParentAndChild
 */
abstract class ExpandableAdapter<PB : ViewBinding, CB : ViewBinding, P, C> :
    HeaderFooterAdapter<ViewBinding, P>() {

    companion object {
        private const val MASK_POSITION = 1 shl 31
    }

    private var childClickListener: ((view: View, posInAdapter: Int, data: C) -> Unit)? = null

    private val _childClickListener: View.OnClickListener by lazy {
        View.OnClickListener { target ->
            val pos = target.getTag(R.id.tag_pos) as? Int ?: return@OnClickListener
            val data = target.getTag(R.id.tag_data) as? C ?: return@OnClickListener
            childClickListener?.invoke(target, pos, data)
        }
    }

    fun setChildClickListener(listener: (view: View, posInAdapter: Int, data: C) -> Unit) {
        childClickListener = listener
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        for (posInParent in 0 until getDataSize()) {
            if (isExpanded(posInParent)) {
                count += getChildCount(posInParent)
            }
        }
        return count
    }

    /**
     * posInData 转成 posInAdapter, 二者区别参考类注释
     */
    override fun getPosInAdapter(posInData: Int): Int {
        var posInAdapter = posInData
        if (hasHeaderView()) {
            posInAdapter++
        }
        for (posInParent in 0 until posInData) {
            if (isExpanded(posInParent)) {
                posInAdapter += getChildCount(posInParent)
            }
        }
        return posInAdapter
    }

    /**
     * view 类型
     * @param posInParentAndChild 详见类注释
     */
    override fun getItemType(posInParentAndChild: Int): Int {
        return getItemTypeByPosComplex(convertPosition(posInParentAndChild))
    }

    /**
     * 坐标转换成数据集中的坐标, 通过返回值可以判断该位置的 ViewType， 也可以根据位置获取数据模型
     * 返回值 Long：
     *          低 32 位为在 data 中的位置，范围 [0, data.size - 1]
     *          最高位为 0 时是 parent 类型， 为 1 时是 child 类型, 高 32 位的值为子 item 在 parent 中的位置，范围 [0, parent.size-1]
     *
     * @param posInParentAndChild 详见类注释
     *
     * @return posInParent | (posInChild + 1) << 32
     *
     */
    private fun convertPosition(posInParentAndChild: Int): Long {
        var expandedSize = 0
        for (posInParent in 0 until getDataSize()) {
            if (posInParentAndChild == expandedSize + posInParent) {
                return posInParent.toLong()
            }

            var childCount = 0
            if (isExpanded(posInParent)) {
                childCount = getChildCount(posInParent)
            }

            if (posInParentAndChild <= expandedSize + posInParent + childCount) {
                //child 在 parent 中的 pos
                var posInChild = posInParentAndChild - expandedSize - posInParent - 1
                //最高位为 1 时该位置为 child 类型
                posInChild = posInChild or MASK_POSITION
                return posInParent.toLong() or (posInChild.toLong() shl 32)
            }

            expandedSize += childCount
        }
        return Long.MIN_VALUE
    }

    private fun getItemTypeByPosComplex(posComplex: Long): Int {
        return if (posComplex == Long.MIN_VALUE) {
            TYPE_UNKNOWN
        } else if ((posComplex ushr 32).toInt() and MASK_POSITION == MASK_POSITION) {
            TYPE_ITEM_EXPANDABLE_CHILD
        } else {
            TYPE_ITEM_EXPANDABLE_PARENT
        }
    }

    private fun getPosInParentByPosComplex(posComplex: Long): Int {
        return posComplex.toInt()
    }

    private fun getPosInChildByPosComplex(posComplex: Long): Int {
        return (posComplex ushr 32).toInt() and MASK_POSITION.inv()
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            TYPE_ITEM_EXPANDABLE_PARENT -> createParentBinding(parent, viewType)
            TYPE_ITEM_EXPANDABLE_CHILD -> createChildBinding(parent, viewType)
            else -> ViewBinding { View(parent.context) }
        }
    }

    override fun bindHolder(holder: BindingViewHolder<ViewBinding>, posInAdapter: Int) {
        val posInParentAndChild = getPosInData(posInAdapter)
        val posComplex = convertPosition(posInParentAndChild)

        when (getItemTypeByPosComplex(posComplex)) {
            TYPE_ITEM_EXPANDABLE_PARENT -> {
                val data = getDataByPos(getPosInParentByPosComplex(posComplex)) ?: return
                bindParentHolder(holder as BindingViewHolder<PB>, posInAdapter, data)
            }

            TYPE_ITEM_EXPANDABLE_CHILD -> {
                val posInChild = getPosInChildByPosComplex(posComplex)
                val data = getChild(getPosInParentByPosComplex(posComplex), posInChild) ?: return
                bindChildHolder(holder as BindingViewHolder<CB>, posInAdapter, data)
            }
        }
    }

    /**
     * 折叠
     */
    fun collapse(@IntRange(from = 0) posInAdapter: Int) {
        val posInParentAndChild = getPosInData(posInAdapter)
        val posComplex = convertPosition(posInParentAndChild)
        val posInParent = getPosInParentByPosComplex(posComplex)
        if (!isExpanded(posInParent)) {
            return
        }
        val childCount = getChildCount(posInParent)

        markPositionCollapsed(posInParent)
        if (childCount == 0) {
            notifyItemChanged(posInAdapter)
        } else {
            notifyItemRangeRemoved(posInAdapter + 1, childCount)
            val footerCount = if (hasFooterView()) 1 else 0
            val loadMoreCount = if (hasLoadMoreView()) 1 else 0
            val itemCountExceptFooter = itemCount - footerCount - loadMoreCount
            notifyItemRangeChanged(posInAdapter, itemCountExceptFooter - posInAdapter)
        }
    }

    /**
     * 展开
     */
    fun expand(@IntRange(from = 0) posInAdapter: Int) {
        val posInParentAndChild = getPosInData(posInAdapter)
        val posComplex = convertPosition(posInParentAndChild)
        val posInParent = getPosInParentByPosComplex(posComplex)
        if (isExpanded(posInParent)) {
            return
        }
        val childCount = getChildCount(posInParent)

        markPositionExpanded(posInParent)
        if (childCount == 0) {
            notifyItemChanged(posInAdapter)
        } else {
            notifyItemRangeInserted(posInAdapter + 1, childCount)
            val footerCount = if (hasFooterView()) 1 else 0
            val loadMoreCount = if (hasLoadMoreView()) 1 else 0
            val itemCountExceptFooter = itemCount - footerCount - loadMoreCount
            notifyItemRangeChanged(posInAdapter, itemCountExceptFooter - posInAdapter)
        }
    }

    /**
     * 展开并折叠其它位置
     */
    fun expandAndCollapseOther(@IntRange(from = 0) posInAdapter: Int) {
        val posInParentAndChild = getPosInData(posInAdapter)
        val posComplex = convertPosition(posInParentAndChild)
        val posInParent = getPosInParentByPosComplex(posComplex)
        expand(posInParentAndChild)

        for (pip in 0 until getDataSize()) {
            if (pip == posInParent) continue
            collapse(getPositionInParentAndChild(pip))
        }
    }

    private fun getPositionInParentAndChild(posInParent: Int): Int {
        var expandedSize = 0
        for (pip in 0 until posInParent) {
            if (isExpanded(pip)) {
                expandedSize += getChildCount(pip)
            }
        }
        return posInParent + expandedSize
    }

    protected fun bindChildClick(view: View, posInAdapter: Int, data: C) {
        view.setTag(R.id.tag_pos, posInAdapter)
        view.setTag(R.id.tag_data, data)
        view.setOnClickListener(_childClickListener)
    }

    /**
     * 标记被折叠的位置
     */
    protected abstract fun markPositionCollapsed(posInParent: Int)

    /**
     * 标记被展开的位置
     */
    protected abstract fun markPositionExpanded(posInParent: Int)

    /**
     * 该位置是否是展开的
     * @param posInParent data 中的位置 [0, data.size - 1]
     */
    protected abstract fun isExpanded(posInParent: Int): Boolean

    /**
     * 展开后子 item 个数
     * @param posInParent data 中的位置 [0, data.size - 1]
     */
    protected abstract fun getChildCount(posInParent: Int): Int

    /**
     * 获取子 item
     *
     * @param posInParent data 中的位置 [0 , data.size - 1]
     * @param posInChild 子 item 在 parent 中的位置 [0, size - 1]
     */
    protected abstract fun getChild(posInParent: Int, posInChild: Int): C?


    abstract fun createParentBinding(parent: ViewGroup, viewType: Int): PB
    abstract fun createChildBinding(parent: ViewGroup, viewType: Int): CB
    abstract fun bindParentHolder(holder: BindingViewHolder<PB>, posInAdapter: Int, data: P)
    abstract fun bindChildHolder(holder: BindingViewHolder<CB>, posInAdapter: Int, data: C)
}