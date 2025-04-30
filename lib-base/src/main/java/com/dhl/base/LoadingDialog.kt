package com.dhl.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.dhl.base.databinding.DialogLoadingBinding

/**
 *
 * Author: duanhaoliang
 * Create: 2021/10/2 17:35
 * Description: 通用 loading 对话框
 *
 */
class LoadingDialog(context: Context) : BaseDialog(context, R.style.Base_Dialog_Common) {

    private var binding: DialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
    private var cancelAction: (() -> Unit)? = null
    private val delayShowRunnable = Runnable { show() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
    }

    override fun getDialogSize(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(104.dp, 104.dp)
    }

    fun setText(text: String?) {
        binding.tvLoadingHint.text = text
    }

    override fun cancel() {
        super.cancel()
        cancelAction?.invoke()
    }

    fun showLoading(text: String? = null, delay: Long = 0, cancelAction: (() -> Unit)? = null) {
        if (text.isNullOrEmpty()) {
            binding.tvLoadingHint.visibility = View.GONE
        } else {
            binding.tvLoadingHint.visibility = View.VISIBLE
            binding.tvLoadingHint.text = text
        }
        this.cancelAction = cancelAction
        if (delay <= 0) {
            show()
        } else {
            AppExecutors.postOnMain(delay, runnable = delayShowRunnable)
        }
    }

    fun dismissLoading() {
        AppExecutors.removeMainCallback(delayShowRunnable)
        dismiss()
    }

}