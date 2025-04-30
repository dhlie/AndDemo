package com.dhl.base.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.dhl.base.R
import com.dhl.base.dp
import com.dhl.base.utils.log
import com.dhl.base.px2dp
import com.dhl.base.statusBarHeight
import com.dhl.base.utils.SystemUtil

/**
 *
 * Author: duanhaoliang
 * Create: 2022/2/10 14:13
 * Description:
 *
 */
@SuppressLint("ResourceType")
class NavigationBar : RelativeLayout {

    private var viewId = 0x7fabcd00
    private var height: Int = 0                 //导航条高度
    private var btnWidth: Int = 0
    private var btnBack: ImageView? = null      //返回按钮
    private var btnClose: ImageView? = null     //关闭按钮
    private var tvTitle: TextView? = null       //标题view
    private var endBtn: View? = null            //最右边图标按钮
    private var endSecondBtn: View? = null      //最右边图标按钮左边的按钮
    private lateinit var bottomLine: View       //底部分割线
    private var textColor = context.getColor(R.color.text_primary)

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
        var showBack = true
        var title: String? = ""
        var titleColor = 0
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationBar)
            try {
                showBack = typedArray.getBoolean(R.styleable.NavigationBar_showBack, showBack)
                title = typedArray.getString(R.styleable.NavigationBar_title)
                titleColor = typedArray.getColor(R.styleable.NavigationBar_titleColor, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }

        btnWidth = context.resources.getDimensionPixelSize(R.dimen.title_bar_height)
        height = btnWidth + statusBarHeight
        log { "navBar height:${height}px ${height.px2dp}dp; statusBarHei:${statusBarHeight.px2dp}dp" }
        setPadding(0, statusBarHeight, 0, 0)
        bottomLine = View(context)
        bottomLine.setBackgroundColor(context.getColor(R.color.line_bg))

        val params = generateDefaultLayoutParams()
        params.width = LayoutParams.MATCH_PARENT
        params.height = 0.5f.dp
        params.addRule(ALIGN_PARENT_BOTTOM)

        addView(bottomLine, params)

        if (showBack) {
            setBackBtn(R.drawable.ic_back) { SystemUtil.getActivityFromContext(context)?.onBackPressed() }
        }
        if (!title.isNullOrEmpty()) {
            val color = if (titleColor == 0) textColor else titleColor
            setTitle(title, color)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * 设置返回按钮图标和点击事件
     *
     *  @param resId            返回图标
     *  @param clickListener    返回按钮点击事件
     */
    fun setBackBtn(@DrawableRes resId: Int = 0, clickListener: OnClickListener? = null) {
        setBackBtn(if (resId == 0) null else AppCompatResources.getDrawable(context, resId), clickListener)
    }

    fun setBackBtn(drawable: Drawable?, clickListener: OnClickListener? = null) {
        if (drawable == null) {
            btnBack?.let {
                removeView(it)
                btnBack = null
            }
            return
        }

        var btn = btnBack
        if (btn == null) {
            btn = createImageBtn()
            btn.id = viewId++
            addView(btn)
            btnBack = btn
        }
        btn.setImageDrawable(drawable)
        setBackClickListener(clickListener)
        if (btn.visibility != View.VISIBLE) {
            btn.visibility = View.GONE
        }
    }

    fun setBackClickListener(listener: OnClickListener?) {
        btnBack?.setOnClickListener(listener)
    }

    fun setBackBtnVisibility(visibility: Int) {
        if (btnBack?.visibility != visibility) {
            btnBack?.visibility = visibility
        }
    }

    /**
     * 设置关闭按钮图标和点击事件(用于 WebView 页面)
     *
     *  @param resId            返回图标
     *  @param clickListener    返回按钮点击事件
     */
    fun setCloseBtn(@DrawableRes resId: Int = 0, clickListener: OnClickListener?) {
        setCloseBtn(if (resId == 0) null else AppCompatResources.getDrawable(context, resId), clickListener)
    }

    fun setCloseBtn(drawable: Drawable?, clickListener: OnClickListener?) {
        if (drawable == null) {
            btnClose?.let {
                removeView(it)
                btnClose = null
            }
            return
        }
        if (btnBack == null) {
            throw RuntimeException("返回按钮不能为空, 请先设置返回按钮")
        }
        var btn = btnClose
        if (btn == null) {
            val divider = View(context)
            divider.setBackgroundColor(context.getColor(R.color.line_bg))
            divider.id = viewId++

            val dividerParams = generateDefaultLayoutParams()
            dividerParams.width = 1.dp
            dividerParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            dividerParams.topMargin = 16.dp
            dividerParams.bottomMargin = dividerParams.topMargin
            dividerParams.addRule(END_OF, btnBack!!.id)

            addView(divider, dividerParams)

            btn = createImageBtn()
            (btn.layoutParams as LayoutParams).addRule(END_OF, divider.id)
            addView(btn)
            btnClose = btn
        }
        btn.setImageDrawable(drawable)
        btn.setOnClickListener(clickListener)
    }

    /**
     * 设置标题
     */
    fun setTitle(@StringRes resId: Int, color: Int = textColor) {
        setTitle(context.getString(resId), color)
    }

    /**
     * 设置标题
     */
    fun setTitle(title: CharSequence, color: Int = textColor) {
        var tv = tvTitle
        if (tv == null) {
            tv = BoldTextView(context)
            tv.maxLines = 1
            tv.ellipsize = TextUtils.TruncateAt.END
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tv.gravity = Gravity.CENTER

            val params = generateDefaultLayoutParams()
            params.width = LayoutParams.MATCH_PARENT
            params.height = LayoutParams.MATCH_PARENT
            params.marginStart = btnWidth * 2
            params.marginEnd = btnWidth * 2

            addView(tv, params)
            tvTitle = tv
        }
        tv.text = title
        tv.setTextColor(color)
    }

    /**
     * 设置最右侧的按钮, drawableId 不为 0 时显示图标按钮, 否则 textId 不为 0 时显示文字按钮
     * @param drawableId 图标 id
     * @param text 按钮文字
     * @param color 按钮文字颜色
     * @param textSize 文字大小,单位 sp
     * @param clickListener 点击事件
     */
    fun setEndBtn(
        @DrawableRes drawableId: Int,
        text: String = "",
        @ColorInt color: Int = textColor,
        textSize: Float = -1f,
        clickListener: OnClickListener? = null,
    ) {
        val drawable = if (drawableId == 0) null else AppCompatResources.getDrawable(context, drawableId)
        setEndBtn(drawable, text, color, textSize, clickListener)
    }

    fun setEndBtn(
        drawable: Drawable?,
        text: String = "",
        @ColorInt color: Int = textColor,
        textSize: Float = -1f,
        clickListener: OnClickListener? = null,
    ) {
        if (drawable != null) {
            var btn = endBtn as? ImageView
            if (btn == null) {
                if (endBtn is TextView) {
                    removeViewInLayout(endBtn)
                }
                btn = createImageBtn()
                btn.id = viewId++
                (btn.layoutParams as LayoutParams).apply {
                    addRule(ALIGN_PARENT_END)
                }
                addView(btn)
                endBtn = btn
            }
            btn.setImageDrawable(drawable)
            btn.setOnClickListener(clickListener)
        } else if (text.isNotEmpty()) {
            var btn = endBtn as? TextView
            if (btn == null) {
                if (endBtn is ImageView) {
                    removeViewInLayout(endBtn)
                }
                btn = createTextBtn()
                btn.id = viewId++
                (btn.layoutParams as LayoutParams).apply {
                    marginEnd = 8.dp
                    addRule(ALIGN_PARENT_END)
                }
                addView(btn)
                endBtn = btn
            }
            if (textSize > 0f) {
                btn.textSize = textSize
            }
            btn.setTextColor(color)
            btn.text = text
            btn.setOnClickListener(clickListener)
        } else {
            endBtn?.let {
                removeView(it)
                endBtn = null
            }
        }
    }

    /**
     * 设置右侧第二个按钮, drawableId 不为 0 时显示图标按钮, 否则 textId 不为 0 时显示文字按钮
     * @param drawableId 图标 id
     * @param text 按钮文字
     * @param color 按钮文字颜色
     * @param textSize 文字大小,单位 sp
     * @param clickListener 点击事件
     */
    fun setEndSecondBtn(
        @DrawableRes drawableId: Int,
        text: String = "",
        @ColorInt color: Int = textColor,
        textSize: Float = -1f,
        clickListener: OnClickListener? = null,
    ) {
        val drawable = if (drawableId == 0) null else AppCompatResources.getDrawable(context, drawableId)
        setEndSecondBtn(drawable, text, color, textSize, clickListener)
    }

    fun setEndSecondBtn(
        drawable: Drawable?,
        text: String = "",
        @ColorInt color: Int = textColor,
        textSize: Float = -1f,
        clickListener: OnClickListener? = null,
    ) {
        if (drawable != null) {
            var btn = endSecondBtn as? ImageView
            if (btn == null) {
                if (endSecondBtn is TextView) {
                    removeViewInLayout(endSecondBtn)
                }
                btn = createImageBtn()
                (btn.layoutParams as LayoutParams).apply {
                    marginEnd = (-8).dp
                    addRule(START_OF, endBtn!!.id)
                }
                addView(btn)
                endSecondBtn = btn
            }
            btn.setImageDrawable(drawable)
            btn.setOnClickListener(clickListener)
        } else if (text.isNotEmpty()) {
            var btn = endSecondBtn as? TextView
            if (btn == null) {
                if (endSecondBtn is ImageView) {
                    removeViewInLayout(endSecondBtn)
                }
                btn = createTextBtn()
                (btn.layoutParams as LayoutParams).apply {
                    addRule(START_OF, endBtn!!.id)
                }
                addView(btn)
                endSecondBtn = btn
            }
            if (textSize > 0) {
                btn.textSize = textSize
            }
            btn.setTextColor(color)
            btn.text = text
            btn.setOnClickListener(clickListener)
        } else {
            endSecondBtn?.let {
                removeView(it)
                endSecondBtn = null
            }
        }
    }

    fun setDividerVisibility(visibility: Int) {
        if (bottomLine.visibility != visibility) {
            bottomLine.visibility = visibility
        }
    }

    fun backBtn() = btnBack

    fun closeBtn() = btnClose

    fun titleTv() = tvTitle

    fun endBtn() = endBtn

    fun endSecondBtn() = endSecondBtn

    private fun createImageBtn(): ImageView {
        val btn = ImageView(context)
        btn.scaleType = ImageView.ScaleType.CENTER_INSIDE
        btn.setBackgroundResource(R.drawable.bg_selectable_action_button)
        val paddingHor = 16.dp
        btn.setPadding(paddingHor, 0, paddingHor, 0)

        val params = generateDefaultLayoutParams()
        params.width = LayoutParams.WRAP_CONTENT
        params.height = LayoutParams.MATCH_PARENT

        btn.layoutParams = params
        return btn
    }

    private fun createTextBtn(): TextView {
        val btn = TextView(context)
        btn.maxLines = 1
        btn.ellipsize = TextUtils.TruncateAt.END
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        val paddingHor = 8.dp
        btn.setPadding(paddingHor, 0, paddingHor, 0)
        btn.gravity = Gravity.CENTER
        btn.setBackgroundResource(R.drawable.bg_selectable_action_button)

        val params = generateDefaultLayoutParams()
        params.width = LayoutParams.WRAP_CONTENT
        params.height = LayoutParams.MATCH_PARENT

        btn.layoutParams = params
        return btn
    }

}