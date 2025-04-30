package com.dhl.base.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * Author: duanhaoliang
 * Create: 2021/6/3 10:28
 * Description:
 *
 */

private class HeaderFooterViewHolder(view: View) :
    BindingViewHolder<ViewBinding>(ViewBinding { view })

abstract class BaseLoadMoreView : FrameLayout {

    companion object {
        const val STATE_IDLE = 0
        const val STATE_LOADING = 1
    }

    /**是否能加载更多*/
    private var loadEnable: Boolean = true

    /**不能加载更多时是否显示*/
    var showWhenDisable = true
    private var state: Int = STATE_IDLE
    private var loadMoreCallback: (() -> Unit)? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    protected abstract fun onLoadingStart()
    protected abstract fun onLoadingFinish()

    internal fun isLoadEnable(): Boolean {
        return loadEnable
    }

    internal open fun setLoadEnable(enable: Boolean) {
        loadEnable = enable
    }

    fun setLoadMoreCallback(callback: (() -> Unit)?) {
        loadMoreCallback = callback
    }

    internal fun startLoading() {
        if (!loadEnable) {
            return
        }
        if (state == STATE_LOADING) {
            return
        }
        state = STATE_LOADING
        onLoadingStart()
        loadMoreCallback?.invoke()
    }

    internal fun finishLoading() {
        if (state != STATE_IDLE) {
            state = STATE_IDLE
            onLoadingFinish()
        }
    }
}

abstract class HeaderFooterAdapter<T : ViewBinding, K>(private val orientation: Int = LinearLayout.VERTICAL) :
    ViewBindingAdapter<T, K>() {

    companion object {
        const val TYPE_UNKNOWN = 10000
        const val TYPE_HEADER = 10001
        const val TYPE_ITEM = 10002
        const val TYPE_FOOTER = 10003
        const val TYPE_LOAD_MORE = 10004

        const val TYPE_ITEM_EXPANDABLE_PARENT = 10005      //可展开 item 的父节点
        const val TYPE_ITEM_EXPANDABLE_CHILD = 10006       //可展开 item 的子节点
    }

    private var headerLayout: LinearLayout? = null
    private var footerLayout: LinearLayout? = null
    var loadMoreView: BaseLoadMoreView? = null
        set(value) {
            if (loadMoreView == value) return
            field = value?.apply {
                layoutParams = if (orientation == LinearLayout.VERTICAL) {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
            notifyItemChanged(getFooterViewPosition())
        }


    @Volatile
    var loadingId: String? = null

    final override fun getItemViewType(posInAdapter: Int): Int {
        val hasHeader = hasHeaderView()
        val hasFooter = hasFooterView()
        val hasLoadMore = hasLoadMoreView()

        val itemCount = itemCount
        if (!hasLoadMore) {
            return if (!hasHeader && !hasFooter) {
                return getItemType(posInAdapter)
            } else if (hasHeader && !hasFooter) {
                if (posInAdapter == 0) TYPE_HEADER else getItemType(posInAdapter - 1)
            } else if (!hasHeader && hasFooter) {
                if (posInAdapter == itemCount - 1) TYPE_FOOTER else getItemType(posInAdapter)
            } else {
                when (posInAdapter) {
                    0 -> TYPE_HEADER
                    itemCount - 1 -> TYPE_FOOTER
                    else -> getItemType(posInAdapter - 1)
                }
            }
        } else {
            return if (!hasHeader && !hasFooter) {
                if (posInAdapter == itemCount - 1) TYPE_LOAD_MORE else getItemType(posInAdapter)
            } else if (hasHeader && !hasFooter) {
                when (posInAdapter) {
                    0 -> {
                        TYPE_HEADER
                    }

                    itemCount - 1 -> {
                        TYPE_LOAD_MORE
                    }

                    else -> {
                        getItemType(posInAdapter - 1)
                    }
                }
            } else if (!hasHeader && hasFooter) {
                when (posInAdapter) {
                    itemCount - 1 -> TYPE_LOAD_MORE
                    itemCount - 2 -> TYPE_FOOTER
                    else -> getItemType(posInAdapter)
                }
            } else {
                when (posInAdapter) {
                    0 -> TYPE_HEADER
                    itemCount - 1 -> TYPE_LOAD_MORE
                    itemCount - 2 -> TYPE_FOOTER
                    else -> getItemType(posInAdapter - 1)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BindingViewHolder<ViewBinding> {
        return when (viewType) {
            TYPE_HEADER -> HeaderFooterViewHolder(headerLayout!!).apply {
                (headerLayout?.parent as? ViewGroup)?.run { removeView(headerLayout) }
            }

            TYPE_FOOTER -> HeaderFooterViewHolder(footerLayout!!).apply {
                (footerLayout?.parent as? ViewGroup)?.run { removeView(footerLayout) }
            }

            TYPE_LOAD_MORE -> HeaderFooterViewHolder(loadMoreView!!).apply {
                (loadMoreView?.parent as? ViewGroup)?.run { removeView(loadMoreView) }
            }

            else -> BindingViewHolder(createBinding(parent, viewType))
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder<ViewBinding>, posInAdapter: Int) {
        if (holder is HeaderFooterViewHolder) {
            return
        }
        bindHolder(holder as BindingViewHolder<T>, posInAdapter)
    }

    override fun onViewAttachedToWindow(holder: BindingViewHolder<ViewBinding>) {
        super.onViewAttachedToWindow(holder)
        if (holder is HeaderFooterViewHolder && holder.binding.root == loadMoreView) {
            loadMoreView?.startLoading()
        }
    }

    /**
     * @param posInData [data]中的位置, 详见[ViewBindingAdapter]类注释
     */
    protected open fun getItemType(posInData: Int) = TYPE_ITEM

    override fun hasHeaderView(): Boolean {
        val count = headerLayout?.childCount ?: 0
        return count > 0
    }

    override fun hasFooterView(): Boolean {
        val count = footerLayout?.childCount ?: 0
        return count > 0
    }

    override fun hasLoadMoreView(): Boolean {
        val loadMoreView = loadMoreView ?: return false
        return (loadMoreView.isLoadEnable() || loadMoreView.showWhenDisable) && !data.isNullOrEmpty()
    }

    fun setLoadMoreEnable(enable: Boolean) {
        val hasLoadMoreView = hasLoadMoreView()
        loadMoreView?.setLoadEnable(enable)
        if (hasLoadMoreView != hasLoadMoreView()) {
            notifyItemChanged(getFooterViewPosition())
        }
    }

    fun finishLoadingMore() {
        loadMoreView?.finishLoading()
    }

    fun resetLoadMoreStatus() {
        setLoadMoreEnable(false)
        loadMoreView?.finishLoading()
        loadingId = null
    }

    fun addHeaderView(view: View, index: Int = -1) {
        if (headerLayout == null) {
            headerLayout = LinearLayout(view.context).apply {
                orientation = this@HeaderFooterAdapter.orientation
                layoutParams = if (orientation == LinearLayout.VERTICAL) {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        headerLayout?.run {
            if (index > childCount) {
                addView(view, childCount)
            } else {
                addView(view, index)
            }
            if (childCount == 1) {
                notifyItemInserted(0)
                val count = getDataSize()
                if (count > 0) {
                    notifyItemRangeChanged(0, count)
                }
            }
        }
    }

    fun removeHeaderView(header: View) {
        headerLayout?.run {
            if (childCount == 0) {
                return@run
            }
            removeView(header)
            if (childCount == 0) {
                notifyItemRemoved(0)
                val count = getDataSize()
                if (count > 0) {
                    notifyItemRangeChanged(0, count)
                }
            }
        }
    }

    fun removeAllHeaderView() {
        headerLayout?.run {
            if (childCount == 0) {
                return@run
            }
            removeAllViews()
            notifyItemRemoved(0)
            val count = getDataSize()
            if (count > 0) {
                notifyItemRangeChanged(0, count)
            }
        }
    }

    fun addFooterView(view: View, index: Int = -1) {
        if (footerLayout == null) {
            footerLayout = LinearLayout(view.context).apply {
                orientation = this@HeaderFooterAdapter.orientation
                layoutParams = if (orientation == LinearLayout.VERTICAL) {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        footerLayout?.run {
            if (index > childCount) {
                addView(view, childCount)
            } else {
                addView(view, index)
            }
            if (childCount == 1) {
                notifyItemInserted(getFooterViewPosition())
            }
        }
    }

    fun removeFooterView(footer: View) {
        footerLayout?.run {
            if (childCount == 0) {
                return@run
            }
            removeView(footer)
            if (childCount == 0) {
                notifyItemRemoved(getFooterViewPosition())
            }
        }
    }

    fun removeAllFooterView() {
        footerLayout?.run {
            if (childCount == 0) {
                return@run
            }
            removeAllViews()
            notifyItemRemoved(getFooterViewPosition())
        }
    }

    private fun getFooterViewPosition(): Int {
        var dataSize = data?.size ?: 0
        if (hasHeaderView()) {
            dataSize++
        }
        return dataSize
    }
}