package dhl.anddemo.shimmer

import android.graphics.PorterDuff
import android.os.Bundle
import com.dhl.base.dp
import dhl.anddemo.base.BuBaseActivity
import dhl.anddemo.databinding.ActiShimmerBinding

/**
 *
 * Author: Hello
 * Create: 2025/4/30 16:16
 * Description:
 *
 */
class ShimmerActivity : BuBaseActivity() {

    private lateinit var binding: ActiShimmerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActiShimmerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.ivBack?.setOnClickListener {
            finish()
        }
        val colors = intArrayOf(0x00ffffff.toInt(), 0xffffffff.toInt(), 0x00ffffff.toInt())
        val pos = floatArrayOf(0f, 0.5f, 1f)
        val angel = 30f
        val duration = 2000
        binding.fl1.getShimmerDrawable().setup(
            colors=colors,
            positions = pos,
            lightWidth = 100.dp,
            mode = PorterDuff.Mode.SRC_ATOP,
            angle = angel,
            duration = duration
        )

        val colors2 = intArrayOf(0x00ffffff.toInt(), 0x7fff0000.toInt(), 0x00ffffff.toInt())
        val pos2 = floatArrayOf(0f, 0.5f, 1f)
        val angel2 = -30f
        val duration2 = 2000
        binding.fl2.getShimmerDrawable().setup(
            backgroundColor = 0x33ff0000,
            roundRadius = 12.dp.toFloat(),
            colors = colors2,
            positions = pos2,
            lightWidth = 100.dp,
            angle = angel2,
            duration = duration2
        )
    }
}