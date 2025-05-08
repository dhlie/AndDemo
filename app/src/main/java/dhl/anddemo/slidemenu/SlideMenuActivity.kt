package dhl.anddemo.slidemenu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhl.base.dp
import com.dhl.base.recyclerview.BindingViewHolder
import com.dhl.base.recyclerview.DividerDecoration
import com.dhl.base.recyclerview.ViewBindingAdapter
import com.dhl.base.roundCorner
import com.dhl.base.utils.ToastUtils
import com.dhl.base.view.SlideMenuLayout
import dhl.anddemo.R
import dhl.anddemo.base.BuBaseActivity
import dhl.anddemo.databinding.ActiSlideMenuBinding
import dhl.anddemo.databinding.AdapterSlideMenuBinding
import dhl.anddemo.databinding.SlideMenuRightBinding

/**
 *
 * Author: Hello
 * Create: 2025/5/6 16:10
 * Description:
 *
 */
class SlideMenuActivity : BuBaseActivity() {

    private lateinit var binding: ActiSlideMenuBinding
    private var adapter: MenuListAdapter? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActiSlideMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleBar.ivBack?.setOnClickListener {
            finish()
        }
        binding.clMenu1.roundCorner(8.dp)

        binding.rvList.layoutManager = LinearLayoutManager(this)
        adapter = MenuListAdapter()
        adapter?.setClickListener { view, posInAdapter, data ->
            val pos = adapter?.getPosInData(posInAdapter) ?: return@setClickListener
            when (view.id) {
                R.id.tvMenuEdit -> {
                    ToastUtils.showToast(this, "pos:$pos - Edit")
                }
                R.id.ivMenuWeChat -> {
                    ToastUtils.showToast(this, "pos:$pos - WeChat")
                }
                R.id.llMenuShare -> {
                    ToastUtils.showToast(this, "pos:$pos - Share")
                }
                else -> {
                    ToastUtils.showToast(this, "pos:$pos")
                }
            }
        }
        binding.rvList.adapter = adapter
        val data = mutableListOf<Int>()
        repeat(100) {
            data.add(it)
        }
        adapter?.changeData(data)
        binding.rvList.addItemDecoration(
            DividerDecoration(
                LinearLayoutManager.VERTICAL,
                dividerHeight = 12.dp,
                color = 0,
                showFirstItemDivider = true,
                showLastItemDivider = true,
            )
        )
    }

    private class MenuListAdapter : ViewBindingAdapter<AdapterSlideMenuBinding, Int>() {

        private val slideListener = object : SlideMenuLayout.OnSlideListener {
            private var lastMenu: SlideMenuLayout? = null
            override fun onPreDrag(slidMenuLayout: SlideMenuLayout) {
                if (lastMenu != null && lastMenu != slidMenuLayout) {
                    lastMenu?.closeMenu(true)
                }
                lastMenu = slidMenuLayout
            }
        }

        override fun createBinding(parent: ViewGroup, viewType: Int): AdapterSlideMenuBinding {
            return AdapterSlideMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                root.roundCorner(8.dp)
                root.setSlideListener(slideListener)
                root.getContentLayout()?.roundCorner(8.dp)
                root.getRightMenuLayout()?.let {
                    it.roundCorner(8.dp)
                    root.tag = SlideMenuRightBinding.bind(it)
                }
            }
        }

        override fun bindHolder(holder: BindingViewHolder<AdapterSlideMenuBinding>, posInAdapter: Int) {
            holder.binding.tvText.text = "item $posInAdapter"
            holder.binding.root.getContentLayout()?.let {
                bindClick(it, posInAdapter, posInAdapter)
            }
            val menuBinding = holder.binding.root.tag as? SlideMenuRightBinding
            menuBinding?.let {
                bindClick(it.tvMenuEdit, posInAdapter, posInAdapter)
                bindClick(it.ivMenuWeChat, posInAdapter, posInAdapter)
                bindClick(it.llMenuShare, posInAdapter, posInAdapter)
            }
            holder.binding.root.closeMenu(false)
        }

    }
}