package com.dhl.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.dhl.base.databinding.DialogBottomCommonBinding
import com.dhl.base.recyclerview.BindingViewHolder
import com.dhl.base.recyclerview.HeaderFooterAdapter
import com.dhl.base.recyclerview.ViewBindingAdapter

/**
 *
 * Author: duanhaoliang
 * Create: 2021/7/21 17:38
 * Description:
 *
 */
open class CommonBottomDialog(context: Context, val height: Int = 0) : BaseBottomSheetDialog(context) {

    protected var binding: DialogBottomCommonBinding = DialogBottomCommonBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hei = if (height == 0) (screenSize.y * 0.5f).toInt() else height
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, hei)

        setContentView(binding.root)
        setSystemBarWindowInsetsListener(binding.root) { insets ->
            binding.root.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    fun <T : ViewBinding, K> setAdapter(adapter: ViewBindingAdapter<T, K>) {
        binding.rvList.adapter = adapter
    }

    fun setCustomLayout(view: View) {
        var adapter = binding.rvList.adapter
        if (adapter == null) {
            adapter = object : HeaderFooterAdapter<ViewBinding, Any>() {
                override fun createBinding(parent: ViewGroup, viewType: Int): ViewBinding {
                    return ViewBinding { View(parent.context) }
                }

                override fun bindHolder(holder: BindingViewHolder<ViewBinding>, posInAdapter: Int) {}

            }
            setAdapter(adapter)
        }

        (adapter as HeaderFooterAdapter<*, *>).removeAllHeaderView()
        adapter.addHeaderView(view)
    }

    override fun dismiss() {
        super.dismiss()
        binding.rvList.adapter = null
    }

}