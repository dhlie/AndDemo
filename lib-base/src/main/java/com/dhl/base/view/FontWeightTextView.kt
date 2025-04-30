package com.dhl.base.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.graphics.TypefaceCompat
import androidx.annotation.IntRange
import com.dhl.base.R

/**
 *
 * Author: duanhl
 * Create: 16/12/24 下午3:21
 * Description: 让textFontWeight属性支持Api29(Android9-p)以下。(https://github.com/simplepeng/FontWeightTextView)
 *
 */
class FontWeightTextView : androidx.appcompat.widget.AppCompatTextView {

    companion object {
        private const val FONT_WEIGHT_UNSPECIFIED = -1
    }

    private var mFontWeight = FONT_WEIGHT_UNSPECIFIED

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FontWeightTextView)
            try {
                if (typedArray.hasValue(R.styleable.FontWeightTextView_android_textFontWeight)) {
                    mFontWeight = typedArray.getInt(R.styleable.FontWeightTextView_android_textFontWeight, FONT_WEIGHT_UNSPECIFIED)
                } else if (typedArray.hasValue(R.styleable.FontWeightTextView_textFontWeight)) {
                    mFontWeight = typedArray.getInt(R.styleable.FontWeightTextView_textFontWeight, FONT_WEIGHT_UNSPECIFIED)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }
        innerSetFontWeight()
    }

    private fun innerSetFontWeight() {
        if (mFontWeight == FONT_WEIGHT_UNSPECIFIED) {
            return
        }

        setFontWeight(mFontWeight)
    }

    fun getFontWeight() = mFontWeight

    fun setFontWeight(@IntRange(from = 1, to = 1000) weight: Int) {
        mFontWeight = weight
        TypefaceCompat.create(context, typeface, weight, typeface.isItalic).let {
            typeface = it
        }
    }

}