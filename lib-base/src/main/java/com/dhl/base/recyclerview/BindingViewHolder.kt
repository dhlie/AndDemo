package com.dhl.base.recyclerview

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * Author: duanhaoliang
 * Create: 2021/6/3 10:28
 * Description:RecyclerView 通用 ViewHolder
 *
 */
open class BindingViewHolder<out T : ViewBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root)