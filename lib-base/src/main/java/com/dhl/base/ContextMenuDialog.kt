package com.dhl.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhl.base.databinding.DialogContextMenuBinding
import com.dhl.base.databinding.ItemDialogContextMenuBinding
import com.dhl.base.recyclerview.BindingViewHolder
import com.dhl.base.recyclerview.DividerDecoration
import com.dhl.base.recyclerview.ViewBindingAdapter

/**
 *
 * Author: duanhaoliang
 * Create: 2021/10/26 14:43
 * Description:
 *
 */
class MenuItem<T>(
    val text: String = "",
    val iconResId: Int = 0,
    val enable: Boolean = true,
    val data: T,
    val selectedTextColor: Int = R.color.colorPrimary,//选中item文字颜色
    val unSelectedTextColor: Int = R.color.text_primary,//未选中item文字颜色
    val isRadioMode: Boolean = false,//是否是单选模式: 只有选中item显示图标
    val isSelected: Boolean = false,
    val changeIconColor: Boolean = false,//是否修改图标颜色
    val selectedIconColor: Int = R.color.colorPrimary,//选中item图标颜色
    val unSelectedIconColor: Int = R.color.text_primary,//未选中item图标颜色
)

class ContextMenuDialog(context: Context) : BaseBottomSheetDialog(context) {

    private var binding: DialogContextMenuBinding = DialogContextMenuBinding.inflate(layoutInflater)

    private val adapter: MenuAdapter by lazy {
        MenuAdapter().apply {
            setClickListener { view, _, data ->
                menuClickAction?.invoke(this@ContextMenuDialog, view, data)
            }
        }
    }
    private var menuClickAction: ((ContextMenuDialog, View, MenuItem<*>?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSystemBarWindowInsetsListener(binding.root) { insets ->
            binding.root.updatePadding(bottom = insets.bottom + 8.dp)
            WindowInsetsCompat.CONSUMED
        }

        val layoutManager = LinearLayoutManager(context)
        binding.rvList.addItemDecoration(DividerDecoration(layoutManager.orientation, 0))
        binding.rvList.layoutManager = layoutManager
        binding.rvList.adapter = adapter
        binding.tvCancel.setOnClickListener {
            menuClickAction?.invoke(this@ContextMenuDialog, it, null)
        }
        binding.rvList.roundCorner(8.dp)
        binding.tvCancel.roundCorner(8.dp)
    }

    fun <T> setMenuItems(
        cancelText: String? = null,
        menus: List<MenuItem<T>>,
        menuClickAction: (ContextMenuDialog, View, MenuItem<T>?) -> Unit,
    ) {
        if (cancelText.isNullOrEmpty()) {
            binding.tvCancel.visibility = View.GONE
        } else {
            binding.tvCancel.visibility = View.VISIBLE
            binding.tvCancel.text = cancelText
        }
        adapter.changeData(menus)
        this.menuClickAction = menuClickAction as (ContextMenuDialog, View, MenuItem<*>?) -> Unit
    }

    class MenuAdapter : ViewBindingAdapter<ItemDialogContextMenuBinding, MenuItem<*>>() {

        override fun createBinding(parent: ViewGroup, viewType: Int): ItemDialogContextMenuBinding {
            return ItemDialogContextMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }

        override fun bindHolder(holder: BindingViewHolder<ItemDialogContextMenuBinding>, posInAdapter: Int, ) {
            val item = getDataByAdapterPos(posInAdapter) ?: return
            val binding = holder.binding

            binding.tvMenu.text = item.text
            if (item.isSelected) {
                binding.tvMenu.setTextColor(ContextCompat.getColor(binding.root.context, item.selectedTextColor))
                if (item.iconResId == 0) {
                    binding.ivIcon.visibility = View.GONE
                } else {
                    binding.ivIcon.visibility = View.VISIBLE
                    AppCompatResources.getDrawable(binding.root.context, item.iconResId)?.let { drawable ->
                        if (item.changeIconColor) {
                            drawable.setTint(ContextCompat.getColor(binding.root.context, item.selectedIconColor))
                        }
                        binding.ivIcon.setImageDrawable(drawable)
                    }
                }
            } else {
                binding.tvMenu.setTextColor(ContextCompat.getColor(binding.root.context, item.unSelectedTextColor))
                if (item.iconResId == 0 || item.isRadioMode) {
                    binding.ivIcon.visibility = View.GONE
                } else {
                    binding.ivIcon.visibility = View.VISIBLE
                    AppCompatResources.getDrawable(binding.root.context, item.iconResId)?.let { drawable ->
                        if (item.changeIconColor) {
                            drawable.setTint(ContextCompat.getColor(binding.root.context, item.unSelectedIconColor))
                        }
                        binding.ivIcon.setImageDrawable(drawable)
                    }
                }
            }
            //if (item.enable) {
            //    binding.tvMenu.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_primary))
            //    if (item.iconResId > 0) {
            //        binding.ivIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.text_primary))
            //    }
            //} else {
            //    binding.tvMenu.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_disable))
            //    if (item.iconResId > 0) {
            //        binding.ivIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.text_disable))
            //    }
            //}
            binding.root.isEnabled = item.enable
            bindClick(binding.root, posInAdapter, item)
        }

    }

}