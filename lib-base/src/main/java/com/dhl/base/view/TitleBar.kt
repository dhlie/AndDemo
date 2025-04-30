package com.dhl.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dhl.base.R
import com.dhl.base.statusBarHeight

/**
 *
 * Author: Hello
 * Create: 2024/4/10 20:46
 * Description:
 *
 */
class TitleBar : FrameLayout {

    private var statusBarHei = 0
    private var titleBarHeight: Int = 0
    private var height = 0
    private var _ivBack: ImageView? = null
    private var _tvTitle: TextView? = null

    val ivBack: ImageView?
        get() = _ivBack

    val tvTitle: TextView?
        get() = _tvTitle

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
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
            try {
                if (typedArray.hasValue(R.styleable.TitleBar_titleBarLayout)) {
                    val layoutId = typedArray.getResourceId(R.styleable.TitleBar_titleBarLayout, 0)
                    LayoutInflater.from(context).inflate(layoutId, this)
                    _ivBack = findViewById(R.id.ivTitleBarBack)
                    _tvTitle = findViewById(R.id.tvTitleBarTitle)

                    if (_ivBack != null && typedArray.hasValue(R.styleable.TitleBar_titleBarBackRes)) {
                        val backRes = typedArray.getResourceId(R.styleable.TitleBar_titleBarBackRes, 0)
                        _ivBack?.setImageResource(backRes)
                    }
                    if (_tvTitle != null && typedArray.hasValue(R.styleable.TitleBar_titleBarText)) {
                        val titleText = typedArray.getString(R.styleable.TitleBar_titleBarText)
                        _tvTitle?.text = titleText
                    }
                    if (_tvTitle != null && typedArray.hasValue(R.styleable.TitleBar_titleBarTextColor)) {
                        val titleTextColor = typedArray.getColor(R.styleable.TitleBar_titleBarTextColor, 0)
                        _tvTitle?.setTextColor(titleTextColor)
                    }
                    if (typedArray.hasValue(R.styleable.TitleBar_titleBarBackground)) {
                        val background = typedArray.getDrawable(R.styleable.TitleBar_titleBarBackground)
                        setBackground(background)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }

        statusBarHei = if (!isInEditMode) statusBarHeight else 0
        titleBarHeight = context.resources.getDimensionPixelSize(R.dimen.title_bar_height)
        height = titleBarHeight + statusBarHei
        setPadding(paddingStart, statusBarHei, paddingEnd, paddingBottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            if (statusBars.top != statusBarHei) {
                statusBarHei = statusBars.top
                height = titleBarHeight + statusBarHei
                setPadding(paddingStart, statusBarHei, paddingEnd, paddingBottom)
                requestLayout()
            }
            insets
        }
    }

    fun setTitleBarHeight(titleBarHeight: Int) {
        height = titleBarHeight + statusBarHei
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}