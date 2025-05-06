package dhl.anddemo.pop

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import com.dhl.base.SingleClickListener
import com.dhl.base.dp
import com.dhl.base.pop.BasePop
import dhl.anddemo.base.BuBaseActivity
import dhl.anddemo.databinding.ActiPopBinding

/**
 *
 * Author: Hello
 * Create: 2025/5/6 15:42
 * Description:
 *
 */
class PopActivity : BuBaseActivity() {

    private lateinit var binding: ActiPopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActiPopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPopWindow()
    }

    private fun initPopWindow() {
        val clickListener = SingleClickListener {
            when (it) {
                binding.btnPopTop -> {
                    val pop = getPop(BasePop.TrianglePlacement.BOTTOM)
                    pop.showAtLocationUp(
                        it,
                        -1,
                        -1,
                        -16.dp,
                        12.dp
                    )
                }

                binding.btnPopBottom -> {
                    val pop = getPop(BasePop.TrianglePlacement.TOP)
                    pop.showAtLocationDown(
                        it,
                        -1,
                        -1,
                        -16.dp,
                        16.dp
                    )
                }

                binding.btnPopLeft -> {
                    val pop = getPop(BasePop.TrianglePlacement.END)
                    pop.showAtLocationLeft(
                        it,
                        -1,
                        -1,
                        10.dp,
                        4.dp
                    )
                }

                binding.btnPopRight -> {
                    val pop = getPop(BasePop.TrianglePlacement.START)
                    pop.showAtLocationRight(
                        it,
                        -1,
                        -1,
                        10.dp,
                        8.dp
                    )
                }
            }
        }
        binding.btnPopTop.setOnClickListener(clickListener)
        binding.btnPopBottom.setOnClickListener(clickListener)
        binding.btnPopLeft.setOnClickListener(clickListener)
        binding.btnPopRight.setOnClickListener(clickListener)
    }

    private fun getPop(trianglePlacement: BasePop.TrianglePlacement): BasePop {
        val textView = TextView(this)
        textView.setPadding(16.dp, 16.dp, 16.dp, 16.dp)
        textView.layoutParams = FrameLayout.LayoutParams(70.dp, 120.dp)
        textView.text = "测试PopupWindow测试PopupWindow测试PopupWindow测试PopupWindow"
        textView.setTextColor(getColor(com.dhl.base.R.color.text_primary))

        return BasePop.Builder().setTrianglePlacement(trianglePlacement)
            .setBackgroundColor(getColor(com.dhl.base.R.color.bg_secondary))
            .setContentView(textView)
            .build(this)
    }
}