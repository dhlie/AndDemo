package com.dhl.base.recyclerview

import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dhl.base.R
import com.dhl.base.utils.log
import kotlin.math.max

/**
 *
 * Author: duanhaoliang
 * Create: 2021/6/3 10:28
 * Description:RecyclerView 通用 Adapter
 *
 * [posInData] - 数据集 data 中的位置
 * [posInAdapter] - adapter 中的位置, 可能包含 Header Footer LoadMoreView
 * 没有 Header Footer, LoadMoreView 时两个值一样
 *
 * @Author lab
 * =========================      posInAdapter
 * ----------------------
 * |    Header           |
 * |                     |
 * |    View1            |
 * |    View2            |             0
 * |    View3            |
 * |     ...             |
 * ----------------------                       posInData
 * ----------------------
 * |    Item            |              1            0
 * ----------------------
 * ----------------------
 * |    Item            |              2            1
 * ----------------------
 * ----------------------
 * |    Footer           |                     posInData
 * |                     |
 * |    View1            |
 * |    View2            |             3
 * |    View3            |
 * |     ...             |
 * |                     |
 * ----------------------
 * ----------------------
 * | LoadMoreView         |            4
 * ----------------------
 * =========================       posInAdapter
 *
 */
abstract class ViewBindingAdapter<T : ViewBinding, K> :
    RecyclerView.Adapter<BindingViewHolder<ViewBinding>>() {

    /**
     * 对 data 修改不能直接转成 MutableList, 因为 java 中的不可变 List 子类也能转换成 MutableList
     * 请使用:
     * @see setData(K, Int)
     * @see addData(K, Int)
     * @see addData(List<K>, Int)
     * @see remove(K)
     * @see removeDataByAdapterPos(Int)
     * @see removeDataByPos(Int)
     */
    var data: List<K>? = null
    private var clickListener: ((view: View, posInAdapter: Int, data: K) -> Unit)? = null
    private var longClickListener: ((view: View, posInAdapter: Int, data: K) -> Unit)? = null

    private val _clickListener: View.OnClickListener by lazy {
        object : View.OnClickListener {
            override fun onClick(view: View) {
                clickListener?.let {
                    val pos = view.getTag(R.id.tag_pos) as? Int ?: return
                    val data = view.getTag(R.id.tag_data) as? K ?: return
                    it.invoke(view, pos, data)
                }
            }
        }
    }

    private val _longClickListener: OnLongClickListener by lazy {
        object : OnLongClickListener {
            override fun onLongClick(view: View): Boolean {
                longClickListener?.let {
                    val pos = view.getTag(R.id.tag_pos) as? Int ?: return false
                    val data = view.getTag(R.id.tag_data) as? K ?: return false
                    it.invoke(view, pos, data)
                    return true
                }
                return false
            }
        }
    }

    override fun getItemCount(): Int {
        var count = getDataSize()
        if (hasHeaderView()) {
            count++
        }
        if (hasFooterView()) {
            count++
        }
        if (hasLoadMoreView()) {
            count++
        }
        return count
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BindingViewHolder<ViewBinding> {
        return BindingViewHolder(createBinding(parent, viewType))
    }

    override fun onBindViewHolder(holder: BindingViewHolder<ViewBinding>, posInAdapter: Int) {
        bindHolder(holder as BindingViewHolder<T>, posInAdapter)
    }

    /**
     * 创建 ViewBinding
     */
    abstract fun createBinding(parent: ViewGroup, viewType: Int): T

    /**
     * 更新 ViewHolder
     * @param holder
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     */
    abstract fun bindHolder(holder: BindingViewHolder<T>, posInAdapter: Int)

    /**
     * 设置点击事件
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     */
    protected fun bindClick(view: View, posInAdapter: Int, data: K) {
        view.setTag(R.id.tag_pos, posInAdapter)
        view.setTag(R.id.tag_data, data)
        view.setOnClickListener(_clickListener)
    }

    /**
     * 清除点击事件
     */
    protected fun unBindClick(view: View) {
        view.setOnClickListener(null)
    }

    /**
     * 设置长按事件
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     */
    protected fun bindLongClick(view: View, posInAdapter: Int, data: K) {
        view.setTag(R.id.tag_pos, posInAdapter)
        view.setTag(R.id.tag_data, data)
        view.setOnLongClickListener(_longClickListener)
    }

    /**
     * 清除长按事件
     */
    protected fun unBindLongClick(view: View) {
        view.setOnLongClickListener(null)
    }

    /**
     * 设置点击事件 listener
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     * @param data item 对应的数据
     */
    fun setClickListener(listener: ((view: View, posInAdapter: Int, data: K) -> Unit)?) {
        clickListener = listener
    }

    /**
     * 设置长按事件 listener
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     * @param data item 对应的数据
     */
    fun setLongClickListener(listener: ((view: View, posInAdapter: Int, data: K) -> Unit)?) {
        longClickListener = listener
    }

    /**
     * 是否有 Header
     */
    open fun hasHeaderView(): Boolean = false

    /**
     * 是否有 Footer
     */
    open fun hasFooterView(): Boolean = false

    /**
     * 是否有 LoadMoreView
     */
    open fun hasLoadMoreView(): Boolean = false

    fun changeData(data: List<K>?) {
        this.data = data
        notifyDataSetChanged()
    }

    fun getDataSize(): Int {
        return data?.size ?: 0
    }

    /**
     * @param posInData [data]中的位置, 范围: [0, getDataSize()-1]. 详见类注释
     */
    fun getDataByPos(posInData: Int): K? {
        return data?.getOrNull(posInData)
    }

    /**
     * @param posInAdapter adapter 中的位置, 不是 [data]中的位置, 范围: [0, getItemCount()-1]. 详见类注释
     */
    fun getDataByAdapterPos(posInAdapter: Int): K? {
        return data?.getOrNull(getPosInData(posInAdapter))
    }

    /**
     * posInAdapter 转成 posInData, 二者区别参考类注释
     */
    fun getPosInData(posInAdapter: Int): Int {
        var posInData = posInAdapter
        if (hasHeaderView()) {
            posInData--
        }
        return posInData
    }

    /**
     * posInData 转成 posInAdapter, 二者区别参考类注释
     */
    open fun getPosInAdapter(posInData: Int): Int {
        var posInAdapter = posInData
        if (hasHeaderView()) {
            posInAdapter++
        }
        return posInAdapter
    }

    /**
     * 修改 item
     * @param posInData [data]中的位置, 详见类注释
     */
    open fun setData(item: K, posInData: Int = getDataSize()) {
        if (posInData < 0 || posInData >= getDataSize()) {
            log { "ViewBindingAdapter.setData(item: K, posInData: Int = getDataSize()) param posInData is invalid, range:[0, ${getDataSize()}]" }
            return
        }

        var mutableList = data as? MutableList ?: data!!.toMutableList().apply { data = this }
        var changedPosInAdapter = posInData
        if (hasHeaderView()) {
            changedPosInAdapter++
        }

        try {
            mutableList[posInData] = item
        } catch (e: UnsupportedOperationException) {
            mutableList = mutableList.toMutableList().apply { data = this }
            mutableList[posInData] = item
        }
        notifyItemChanged(changedPosInAdapter)
    }

    /**
     * 添加 item
     *
     * @param posInData [data]中的位置, 详见类注释
     *
     */
    open fun addData(item: K, posInData: Int = getDataSize()) {
        addData(listOf(item), posInData)
    }

    /**
     * 添加 list
     *
     * @param posInData [data]中的位置, 详见类注释
     */
    open fun addData(list: List<K>?, posInData: Int = getDataSize()) {
        if (list.isNullOrEmpty()) {
            return
        }

        if (posInData < 0 || posInData > getDataSize()) {
            log(level = Log.ERROR) { "ViewBindingAdapter.addData(list: List<K>?, posInData: Int) param posInData is invalid, range:[0, ${getDataSize()}]" }
            return
        }

        if (data == null) {
            data = mutableListOf()
        }
        var mutableList = data as? MutableList ?: data!!.toMutableList().apply { data = this }
        var startInsertPosInAdapter = posInData
        if (hasHeaderView()) {
            startInsertPosInAdapter++
        }

        try {
            mutableList.addAll(posInData, list)
        } catch (e: UnsupportedOperationException) {
            mutableList = mutableList.toMutableList().apply { data = this }
            mutableList.addAll(posInData, list)
        }
        //先更新上一个 item
        // 为什么更新上一个: 插入后上一个的 ItemDecoration 影响可能会发生变化
        // 例如添加到最后时, 添加前最后一个的 divider 可能会有变化
        notifyItemChanged(max(startInsertPosInAdapter - 1, 0))
        notifyItemRangeInserted(startInsertPosInAdapter, list.size)

        val changeStartPos = startInsertPosInAdapter + list.size
        val changeCount = mutableList.size - (posInData + list.size)
        if (changeCount > 0) {
            notifyItemRangeChanged(changeStartPos, changeCount)
        }
    }

    /**
     * 移除 item
     */
    fun remove(item: K) {
        val index = data?.indexOf(item) ?: -1
        if (index == -1) {
            return
        }
        removeDataByPos(index)
    }

    /**
     * 移除指定位置的 item
     *
     * @param posInAdapter adapter 中的位置, 详见类注释
     */
    fun removeDataByAdapterPos(posInAdapter: Int) {
        removeDataByPos(getPosInData(posInAdapter))
    }

    /**
     * 移除指定位置的 item
     *
     * @param posInData [data]中的位置, 详见类注释
     */
    fun removeDataByPos(posInData: Int) {
        if (posInData < 0 || posInData >= getDataSize()) {
            log(level = Log.ERROR) { "ViewBindingAdapter.removeDataByPos(posInData: Int) param posInData is invalid, range:[0, ${getDataSize()}]" }
            return
        }

        var mutableList = data as? MutableList ?: data!!.toMutableList().apply { data = this }
        var startRemovePosInAdapter = posInData
        if (hasHeaderView()) {
            startRemovePosInAdapter++
        }

        try {
            mutableList.removeAt(posInData)
        } catch (e: UnsupportedOperationException) {
            mutableList = mutableList.toMutableList().apply { data = this }
            mutableList.removeAt(posInData)
        }
        notifyItemRemoved(startRemovePosInAdapter)

        // 更新范围: 被移除 item 的上一个 到 data中的最后一个(header, footer, loadmore 不用更新)
        // 为什么从上一个开始更新: 因为移除后上一个的 ItemDecoration 影响可能会发生变化
        // 例如移除后上一个变成了最后一个 item, 它的 divider 可能会有变化
        val changeStartPos = max(startRemovePosInAdapter - 1, 0)
        val changeCount = mutableList.size - posInData
        if (changeCount > 0) {
            notifyItemRangeChanged(changeStartPos, changeCount)
        }
    }

}