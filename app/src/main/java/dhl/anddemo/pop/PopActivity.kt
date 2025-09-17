package dhl.anddemo.pop

import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.dhl.base.SingleClickListener
import com.dhl.base.dp
import com.dhl.base.pop.BasePop
import com.dhl.base.screenSize
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
        binding.titleBar.ivBack?.setOnClickListener {
            finish()
        }
        binding.vTop.updateLayoutParams {
            height = screenSize.y * 2 / 3
        }
        initPopWindow()
    }

    private fun initPopWindow() {
        val clickListener = SingleClickListener {
            when (it) {
                binding.btnPopTop -> {
                    val pop = getPop(BasePop.TrianglePlacement.BOTTOM, 70.dp, 120.dp, 0xffffffff.toInt(), 0xff464343.toInt())
                    pop.showAtLocationUp(
                        it,
                        -1,
                        -1,
                        (-16).dp,
                        12.dp
                    )
                }

                binding.btnPopBottom -> {
                    val pop = getPop(BasePop.TrianglePlacement.TOP, 320.dp, 70.dp, 0xffffffff.toInt(), 0xff464343.toInt())
                    pop.showAtLocationDown(
                        it,
                        -1,
                        -1,
                        0,
                        16.dp
                    )
                }

                binding.btnPopLeft -> {
                    val pop = getPop(BasePop.TrianglePlacement.END, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    pop.showAtLocationLeft(
                        it,
                        -1,
                        -1,
                        10.dp,
                        4.dp
                    )
                }

                binding.btnPopRight -> {
                    val pop = getPop(BasePop.TrianglePlacement.START, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
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

    private fun getPop(trianglePlacement: BasePop.TrianglePlacement, width: Int, height: Int, textColor: Int = getColor(com.dhl.base.R.color.text_primary), bgColor: Int = getColor(com.dhl.base.R.color.bg_secondary)): BasePop {
        val textView = TextView(this)
        textView.setPadding(16.dp, 16.dp, 16.dp, 16.dp)
        textView.layoutParams = FrameLayout.LayoutParams(width, height)
        textView.text = "测试PopupWindow测试PopupWindow测试PopupWindow测试PopupWindow"
        textView.setTextColor(textColor)

        return BasePop.Builder().setTrianglePlacement(trianglePlacement)
            .setBackgroundColor(bgColor)
            .setContentView(textView)
            .build(this)
    }
}