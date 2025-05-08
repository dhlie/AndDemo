package dhl.anddemo.wheelview

import android.os.Bundle
import com.dhl.base.view.WheelView
import dhl.anddemo.base.BuBaseActivity
import dhl.anddemo.databinding.ActiWheelviewBinding

/**
 *
 * Author: Hello
 * Create: 2025/5/6 15:23
 * Description:
 *
 */
class WheelViewActivity : BuBaseActivity() {

    private lateinit var binding: ActiWheelviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActiWheelviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleBar.ivBack?.setOnClickListener {
            finish()
        }
        initWheelView()
    }

    private fun initWheelView() {
        val listener = WheelView.OnWheelChangeListener { wheelView, selection ->
            val text = wheelView.getAdapter()?.getText(selection) ?: ""
            if (wheelView === binding.wheelView) {
                binding.tvWVLabel.text = text
            } else if (wheelView === binding.wheelView2) {
                binding.tvWVLabel2.text = text
            } else if (wheelView === binding.wheelView3) {
                binding.tvWVLabel3.text = text
            }
        }

        binding.wheelView.setOnWheelChangeListener(listener)
        binding.wheelView.setAdapter(object : WheelView.WheelAdapter() {
            override fun getCount(): Int {
                return 19999
            }

            override fun getItem(position: Int): Any? {
                return null
            }

            override fun getText(position: Int): String {
                //    0 : -9999
                // 19998: 9999
                return "${position - 9999}年"
            }
        })
        binding.wheelView.setSelection(12022)

        binding.wheelView2.setOnWheelChangeListener(listener)
        binding.wheelView2.setAdapter(object : WheelView.WheelAdapter() {

            private val list = listOf(
                Pair("包含", 0),
                Pair("等于", 1),
                Pair("开始于", 2),
                Pair("结束于", 3),
            )

            override fun getCount(): Int {
                return list.size
            }

            override fun getItem(position: Int): Int {
                return list[position].second
            }

            override fun getText(position: Int): String {
                return list[position].first
            }
        })
        binding.wheelView2.setSelection(2)

        binding.wheelView3.setOnWheelChangeListener(listener)
        binding.wheelView3.setAdapter(object : WheelView.WheelAdapter() {

            private val list = listOf(
                Pair("包含", 0),
                Pair("等于", 1),
                Pair("开始于", 2),
                Pair("结束于", 3),
            )

            override fun getCount(): Int {
                return list.size
            }

            override fun getItem(position: Int): Int {
                return list[position].second
            }

            override fun getText(position: Int): String {
                return list[position].first
            }
        })
        binding.wheelView3.setSelection(2)
    }
}