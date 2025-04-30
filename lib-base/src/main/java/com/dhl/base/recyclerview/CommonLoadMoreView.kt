package com.dhl.base.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.dhl.base.R

/**
 *
 * Author: duanhl
 * Create: 6/3/21 11:24 PM
 * Description:
 *
 */
class CommonLoadMoreView : BaseLoadMoreView {

    private lateinit var pbLoading: View
    private lateinit var tvText: TextView

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.load_more_layout, this)
        pbLoading = findViewById(R.id.pbLoading)
        tvText = findViewById(R.id.tvText)

        pbLoading.visibility = View.INVISIBLE
        tvText.visibility = View.INVISIBLE
        tvText.text = context.getString(R.string.no_more)
    }

    override fun setLoadEnable(enable: Boolean) {
        super.setLoadEnable(enable)

        if (enable) {
            pbLoading.visibility = View.VISIBLE
            tvText.visibility = View.INVISIBLE
        } else {
            pbLoading.visibility = View.INVISIBLE
            tvText.visibility = if (showWhenDisable) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onLoadingStart() {
        pbLoading.visibility = View.VISIBLE
        tvText.visibility = View.INVISIBLE
    }

    override fun onLoadingFinish() {
        //pbLoading.visibility = View.INVISIBLE
        //tvText.visibility = View.INVISIBLE
    }

}