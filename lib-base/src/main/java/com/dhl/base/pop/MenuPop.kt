package com.dhl.base.pop

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dhl.base.R
import com.dhl.base.databinding.PopMenuHorItemBinding
import com.dhl.base.databinding.PopMenuLayoutBinding
import com.dhl.base.databinding.PopMenuVerItemBinding
import com.dhl.base.dp
import com.dhl.base.recyclerview.BindingViewHolder
import com.dhl.base.recyclerview.ViewBindingAdapter

/**
 *
 * Author: duanhl
 * Create: 2023/10/6 09:23
 * Description:
 *
 */
class MenuPop(context: Context, builder: Builder) : BasePop(context, builder) {

    class MenuAdapter(private val builder: Builder) : ViewBindingAdapter<ViewBinding, PopMenuItem<*>>() {

        override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
            return if (builder.orientation == RecyclerView.VERTICAL) {
                val itemBinding =
                    PopMenuVerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                itemBinding.root.setPadding(builder.itemStartPadding, 0, builder.itemEndPadding, 0)
                itemBinding.tvMenu.setPadding(0, builder.itemTopPadding, 0, builder.itemBottomPadding)
                itemBinding
            } else {
                val itemBinding =
                    PopMenuHorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                itemBinding.root.setPadding(builder.itemStartPadding, 0, builder.itemEndPadding, 0)
                itemBinding.tvMenu.setPadding(0, builder.itemTopPadding, 0, builder.itemBottomPadding)
                itemBinding
            }
        }

        override fun bindHolder(holder: BindingViewHolder<ViewBinding>, posInAdapter: Int) {
            when (val binding = holder.binding) {
                is PopMenuVerItemBinding -> bindVerHolder(binding, posInAdapter)
                is PopMenuHorItemBinding -> bindHorHolder(binding, posInAdapter)
            }
        }

        private fun bindVerHolder(itemBinding: PopMenuVerItemBinding, posInAdapter: Int) {
            val item = getDataByAdapterPos(posInAdapter) ?: return

            if (posInAdapter > 0 && posInAdapter < itemCount - 1) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item)
            } else if (posInAdapter == 0) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item_top_corner8)
            } else if (posInAdapter == itemCount - 1) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item_bottom_corner8)
            }
            (itemBinding.root.background as? RippleDrawable)?.setColor(
                ColorStateList.valueOf(itemBinding.root.context.getColor(R.color.nightColorControlHighlight))
            )

            if (item.iconResId == 0) {
                itemBinding.ivIcon.visibility = View.GONE
            } else {
                itemBinding.ivIcon.visibility = View.VISIBLE
                itemBinding.ivIcon.setImageResource(item.iconResId)
            }
            itemBinding.tvMenu.text = item.text
            if (item.enable) {
                itemBinding.tvMenu.setTextColor(builder.textColor)
                if (item.iconResId > 0) {
                    itemBinding.ivIcon.drawable?.setTint(builder.textColor)
                }
            } else {
                itemBinding.tvMenu.setTextColor(itemBinding.root.context.getColor(R.color.text_disable))
                if (item.iconResId > 0) {
                    itemBinding.ivIcon.drawable?.setTint(itemBinding.root.context.getColor(R.color.text_disable))
                }
            }
            itemBinding.root.isEnabled = item.enable
            bindClick(itemBinding.root, posInAdapter, item)
        }

        private fun bindHorHolder(itemBinding: PopMenuHorItemBinding, posInAdapter: Int) {
            val item = getDataByAdapterPos(posInAdapter) ?: return

            if (posInAdapter > 0 && posInAdapter < itemCount - 1) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item)
            } else if (posInAdapter == 0) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item_top_corner8)
            } else if (posInAdapter == itemCount - 1) {
                itemBinding.root.setBackgroundResource(R.drawable.bg_selectable_item_bottom_corner8)
            }
            (itemBinding.root.background as? RippleDrawable)?.setColor(
                ColorStateList.valueOf(itemBinding.root.context.getColor(R.color.nightColorControlHighlight))
            )

            if (item.iconResId == 0) {
                itemBinding.ivIcon.visibility = View.GONE
            } else {
                itemBinding.ivIcon.visibility = View.VISIBLE
                itemBinding.ivIcon.setImageResource(item.iconResId)
            }

            itemBinding.tvMenu.text = item.text
            if (item.enable) {
                itemBinding.tvMenu.setTextColor(builder.textColor)
                if (item.iconResId > 0) {
                    itemBinding.ivIcon.drawable?.setTint(builder.textColor)
                }
            } else {
                itemBinding.tvMenu.setTextColor(itemBinding.root.context.getColor(R.color.text_disable))
                if (item.iconResId > 0) {
                    itemBinding.ivIcon.drawable?.setTint(itemBinding.root.context.getColor(R.color.text_disable))
                }
            }
            itemBinding.root.isEnabled = item.enable
            bindClick(itemBinding.root, posInAdapter, item)
        }
    }

    class Builder : BasePop.Builder() {

        internal var orientation = RecyclerView.VERTICAL
        internal var itemDecoration: RecyclerView.ItemDecoration? = null
        internal var itemStartPadding: Int = INVALID_VALUE
        internal var itemEndPadding: Int = INVALID_VALUE
        internal var itemTopPadding: Int = INVALID_VALUE
        internal var itemBottomPadding: Int = INVALID_VALUE
        internal var textColor: Int = INVALID_VALUE
        internal var menus: List<PopMenuItem<*>>? = null
        internal var clickListener: ((view: View, posInAdapter: Int, data: PopMenuItem<*>) -> Unit)? = null

        override fun setContentView(content: View?): BasePop.Builder {
            TODO("not support")
        }

        fun setOrientation(ori: Int): Builder {
            orientation = ori
            return this
        }

        fun setItemDecoration(decoration: RecyclerView.ItemDecoration): Builder {
            this.itemDecoration = decoration
            return this
        }

        fun setItemPadding(horPadding: Int, verPadding: Int): Builder {
            itemStartPadding = horPadding
            itemEndPadding = horPadding
            itemTopPadding = verPadding
            itemBottomPadding = verPadding
            return this
        }

        fun setItemPadding(
            start: Int = INVALID_VALUE,
            top: Int = INVALID_VALUE,
            end: Int = INVALID_VALUE,
            bottom: Int = INVALID_VALUE,
        ): Builder {
            itemStartPadding = start
            itemEndPadding = end
            itemTopPadding = top
            itemBottomPadding = bottom
            return this
        }

        fun setMenus(menus: List<PopMenuItem<*>>): Builder {
            this.menus = menus
            return this
        }

        fun <K> setItemClickListener(clickAction: ((view: View, posInAdapter: Int, data: PopMenuItem<K>) -> Unit)?): Builder {
            clickListener = clickAction as ((View, Int, PopMenuItem<*>) -> Unit)?
            return this
        }

        override fun preBuild(context: Context) {
            super.preBuild(context)

            val dp16 = 16.dp
            if (itemStartPadding == INVALID_VALUE) {
                itemStartPadding = dp16
            }
            if (itemEndPadding == INVALID_VALUE) {
                itemEndPadding = dp16
            }
            if (itemTopPadding == INVALID_VALUE) {
                itemTopPadding = dp16
            }
            if (itemBottomPadding == INVALID_VALUE) {
                itemBottomPadding = dp16
            }
            if (textColor == INVALID_VALUE) {
                textColor = ContextCompat.getColor(context, R.color.white)
            }

            val binding = PopMenuLayoutBinding.inflate(LayoutInflater.from(context))
            val layoutManager = LinearLayoutManager(context, orientation, false)
            binding.rvList.layoutManager = layoutManager
            if (itemDecoration != null) {
                binding.rvList.addItemDecoration(itemDecoration!!)
            }
            binding.rvList.adapter = MenuAdapter(this).apply {
                setClickListener(clickListener)
                changeData(menus)
            }

            super.setContentView(binding.root)
        }

        override fun build(context: Context): BasePop {
            preBuild(context)
            return MenuPop(context, this)
        }
    }
}