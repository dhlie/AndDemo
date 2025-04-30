package com.dhl.base

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.dhl.base.databinding.DialogCommonBinding

/**
 *
 * Author: duanhaoliang
 * Create: 2021/7/21 17:38
 * Description:
 *
 */
open class CommonDialog(context: Context) : BaseDialog(context, R.style.Base_Dialog_Common) {

    protected var binding: DialogCommonBinding = DialogCommonBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.run {
            setBackgroundResource(R.drawable.shape_bg_color_corner_12)
            elevation = 12.dp.toFloat()
        }
        setContentView(binding.root)
    }

    fun setCustomLayout(layoutId: Int) {
        val view = layoutInflater.inflate(layoutId, binding.flContent, false)
        setCustomView(view)
    }

    fun setCustomView(view: View) {
        binding.flContent.removeAllViews()
        binding.flContent.addView(view)
    }

    override fun setTitle(@StringRes titleId: Int) {
        super.setTitle(titleId)
        setTitle(context.getString(titleId))
    }

    override fun setTitle(text: CharSequence?) {
        setTitleText(text)
    }

    /**
     * 设置标题
     * @param text 标题，为空时不显示标题
     */
    fun setTitleText(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            binding.tvTitle.visibility = View.GONE
            binding.tvContent.setPadding(
                16.dp,
                46.dp,
                16.dp,
                46.dp
            )
        } else {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvContent.setPadding(
                16.dp,
                4.dp,
                16.dp,
                24.dp
            )
        }
        binding.tvTitle.text = text
    }

    /**
     * 设置显示内容
     */
    fun setContentText(text: CharSequence) {
        if (text is SpannableString) {
            binding.tvContent.highlightColor = Color.TRANSPARENT
            binding.tvContent.movementMethod = LinkMovementMethod.getInstance()
        }
        binding.tvContent.text = text
        binding.tvContent.measure(0, 0)
        if (binding.tvContent.lineCount == 1) {
            (binding.tvContent.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.CENTER_HORIZONTAL
        } else {
            (binding.tvContent.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
        }
    }

    /**
     * 设置取消按钮
     * @param text 按钮文字，为空时不显示取消按钮
     * @param autoDismiss 点击后是否关闭对话框
     */
    fun setNegativeButton(
        text: CharSequence?,
        autoDismiss: Boolean = true,
        listener: View.OnClickListener? = null,
    ) {
        if (text.isNullOrEmpty()) {
            binding.groupNegBtn.visibility = View.GONE
            return
        }
        binding.groupNegBtn.visibility = View.VISIBLE
        binding.btnNegative.text = text
        if (autoDismiss) {
            binding.btnNegative.setOnClickListener {
                dismiss()
                listener?.onClick(it)
            }
        } else {
            binding.btnNegative.setOnClickListener(listener)
        }
    }

    /**
     * 设置确定按钮
     * @param text 按钮文字，为空时不显示确定按钮
     * @param autoDismiss 点击后是否关闭对话框
     */
    fun setPositiveButton(
        text: CharSequence?,
        autoDismiss: Boolean = true,
        listener: View.OnClickListener? = null,
    ) {
        if (text.isNullOrEmpty()) {
            binding.btnPositive.visibility = View.GONE
            binding.dividerVer.visibility = View.GONE
            return
        }
        binding.btnPositive.visibility = View.VISIBLE
        binding.btnPositive.text = text
        if (autoDismiss) {
            binding.btnPositive.setOnClickListener {
                dismiss()
                listener?.onClick(it)
            }
        } else {
            binding.btnPositive.setOnClickListener(listener)
        }
    }

    private fun setBtnClickEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (binding.groupNegBtn.visibility != View.VISIBLE && binding.btnPositive.visibility == View.VISIBLE) {
                binding.btnPositive.foreground = AppCompatResources.getDrawable(
                    context,
                    R.drawable.bg_selectable_item_bottom_corner12
                )

            } else if (binding.groupNegBtn.visibility == View.VISIBLE && binding.btnPositive.visibility != View.VISIBLE) {
                binding.btnNegative.foreground = AppCompatResources.getDrawable(
                    context,
                    R.drawable.bg_selectable_item_bottom_corner12
                )
            }
        }
    }

    override fun show() {
        setBtnClickEffect()
        super.show()
    }

    companion object {

        /**
         * 显示通用对话框
         *
         * @param content
         * @param canceledOnTouchOutside 点击弹窗外部是否消失
         * @param title 弹窗标题, 为空时不显示标题
         * @param content 弹窗内容
         * @param negativeText 左边按钮文字, 为空时不显示
         * @param negativeAutoDismiss 左边按钮点击后是否取消弹窗
         * @param negativeClickListener 左边按钮点击 listener
         * @param positiveText 右边按钮文字, 为空时不显示
         * @param positiveAutoDismiss 右边按钮点击后是否取消弹窗
         * @param positiveClickListener 右边按钮点击 listener
         */
        fun showTipsDialog(
            context: Context,
            canceledOnTouchOutside: Boolean = false,
            title: CharSequence? = context.getString(R.string.dialog_hint),
            content: CharSequence,
            negativeText: CharSequence? = context.getString(R.string.cancel),
            negativeAutoDismiss: Boolean = true,
            negativeClickListener: View.OnClickListener? = null,
            positiveText: CharSequence? = context.getString(R.string.confirm),
            positiveAutoDismiss: Boolean = true,
            positiveClickListener: View.OnClickListener? = null,
        ): CommonDialog {
            return CommonDialog(context).apply {
                setCanceledOnTouchOutside(canceledOnTouchOutside)
                setTitleText(title)
                setContentText(content)
                setNegativeButton(negativeText, negativeAutoDismiss, negativeClickListener)
                setPositiveButton(positiveText, positiveAutoDismiss, positiveClickListener)
                show()
            }
        }

    }

}