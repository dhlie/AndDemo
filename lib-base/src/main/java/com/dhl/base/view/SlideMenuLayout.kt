package com.dhl.base.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Scroller
import com.dhl.base.R
import kotlin.math.abs

/**
 *
 * Author: duanhl
 * Create: 13/02/25 下午5:30
 * Description:
 *
 */
class SlideMenuLayout : FrameLayout {

    interface OnSlideListener {
        fun onTouchDown(slidMenuLayout: SlideMenuLayout) {}
        fun onPreDrag(slidMenuLayout: SlideMenuLayout) {}
        fun onDrag(slidMenuLayout: SlideMenuLayout) {}
        fun onTouchUp(slidMenuLayout: SlideMenuLayout) {}
        fun onTouchCancel(slidMenuLayout: SlideMenuLayout) {}

        fun onRightMenuOpened(slidMenuLayout: SlideMenuLayout) {}
        fun onMenuClosed(slidMenuLayout: SlideMenuLayout) {}
    }

    companion object {
        private const val DEBUG = false

        private const val STATE_IDLE = 0
        private const val STATE_TOUCH_DOWN = 1
        private const val STATE_DRAGGING = 2
        private const val STATE_SETTLING = 3

        private const val MENU_STATE_CLOSED = 0
        private const val MENU_STATE_RIGHT_OPENED = 1

        private const val DURATION = 300
    }

    private var contentView: View? = null
    private var rightMenuLayout: View? = null
    private var rightMenuWidthWithMargin = 0 //右边menu展开最大宽度
    private var meanVelocity = 0f //滑动平均速度 rightMenuWidthWithMargin / DURATION
    private var slideListener: OnSlideListener? = null
    private var mScroller = Scroller(context, DecelerateInterpolator())
    private var mState = STATE_IDLE
    private var mMenuState = MENU_STATE_CLOSED

    private var downX = 0
    private var downY = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var mMaxVelocity = 0f
    private var mMinVelocity = 0f
    private var mTouchSlop = 0
    private var mDownTransX = 0f

    init {
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
        mMaxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        mMinVelocity = vc.scaledMinimumFlingVelocity.toFloat()
    }

    fun isMenuOpened(): Boolean {
        val contentView = contentView ?: return false
        return contentView.translationX.toInt() != 0
    }

    fun closeMenu(withAnim: Boolean) {
        val contentView = contentView ?: return
        val transX = contentView.translationX.toInt()
        if (transX == 0) {
            return
        }

        if (!withAnim) {
            translateTo(0)
            mMenuState = MENU_STATE_CLOSED
            slideListener?.onMenuClosed(this)
        } else {
            val fromX = transX
            val dx = -transX
            val duration = calcDuration(abs(transX))
            mScroller.startScroll(fromX, 0, dx, 0, duration)
            setState(STATE_SETTLING)
        }
        invalidate()
    }

    fun openRightMenu(withAnim: Boolean) {
        val contentView = contentView ?: return
        val transX = contentView.translationX.toInt()
        if (transX == -rightMenuWidthWithMargin) {
            return
        }

        if (!withAnim) {
            translateTo(-rightMenuWidthWithMargin)
            mMenuState = MENU_STATE_RIGHT_OPENED
            slideListener?.onRightMenuOpened(this)
        } else {
            val fromX = transX
            val dx = -rightMenuWidthWithMargin - transX
            val duration = calcDuration(abs(dx))
            mScroller.startScroll(fromX, 0, dx, 0, duration)
            setState(STATE_SETTLING)
        }
        invalidate()
    }

    fun setSlideListener(slideMenuListener: OnSlideListener) {
        this.slideListener = slideMenuListener
    }

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
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideMenuLayout)
            try {
                val rightMenuLayoutId = typedArray.getResourceId(R.styleable.SlideMenuLayout_rightMenuLayout, 0)
                if (rightMenuLayoutId != 0) {
                    rightMenuLayout = LayoutInflater.from(context).inflate(rightMenuLayoutId, this, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                typedArray.recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val count = childCount
        if (count != 1) {
            throw IllegalStateException("SlideMenuLayout must has only one child")
        }

        contentView = getChildAt(0)
        if (rightMenuLayout != null) {
            addView(rightMenuLayout)
        }
    }

    fun getContentLayout() = contentView

    fun getRightMenuLayout() = rightMenuLayout

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val contentView = contentView
        val rightMenuLayout = rightMenuLayout
        if (contentView == null || rightMenuLayout == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        var maxHeight = 0
        var maxWidth = 0

        measureChildWithMargins(contentView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        val lp = contentView.layoutParams as MarginLayoutParams
        maxWidth = Math.max(maxWidth, contentView.measuredWidth + lp.leftMargin + lp.rightMargin)
        maxHeight = Math.max(maxHeight, contentView.measuredHeight + lp.topMargin + lp.bottomMargin)

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)

        // Check against our foreground's minimum height and width
        val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground
        } else {
            null
        }
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.minimumHeight)
            maxWidth = Math.max(maxWidth, drawable.minimumWidth)
        }

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec))

        measureChildWithMargins(rightMenuLayout, MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY), 0, MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY), 0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val contentView = contentView
        val rightMenuLayout = rightMenuLayout
        if (contentView == null || rightMenuLayout == null) {
            super.onLayout(changed, l, t, r, b)
            return
        }
        val layoutParams = contentView.layoutParams as MarginLayoutParams
        val contentLeft = paddingLeft + layoutParams.leftMargin
        val contentRight = contentLeft + contentView.measuredWidth
        val contentTop = paddingTop + layoutParams.topMargin
        val contentBottom =  contentTop + contentView.measuredHeight
        contentView.layout(contentLeft, contentTop, contentRight, contentBottom)

        val menuParams = rightMenuLayout.layoutParams as MarginLayoutParams
        val menuLeft = contentRight + menuParams.leftMargin
        val menuRight = menuLeft + rightMenuLayout.measuredWidth
        val menuTop = (measuredHeight - rightMenuLayout.measuredHeight) / 2
        val menuBottom = menuTop + rightMenuLayout.measuredHeight
        rightMenuLayout.layout(menuLeft, menuTop, menuRight, menuBottom)

        rightMenuWidthWithMargin = menuParams.leftMargin + rightMenuLayout.measuredWidth + menuParams.rightMargin
        meanVelocity = rightMenuWidthWithMargin.toFloat() / DURATION
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            translateTo(mScroller.currX)
            invalidate()
            if (mState == STATE_SETTLING && mScroller.isFinished) {
                setState(STATE_IDLE)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val contentView = contentView
        val rightMenuLayout = rightMenuLayout
        if (contentView == null || rightMenuLayout == null) {
            return super.onInterceptTouchEvent(event)
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        var intercept = false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onTouchDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x.toInt()
                val y = event.y.toInt()
                val dx = x - downX
                val dy = y - downY
                if (checkSlop(dx, dy, mTouchSlop) && abs(dx) > abs(dy)) {
                    if (canScrollHorizontally(dx)) {
                        intercept = true
                        downX = x
                        downY = y
                        requestDisallowInterceptTouchEvent(true)
                        setState(STATE_DRAGGING)
                        slideListener?.onPreDrag(this)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchEnd(event)
            }
        }
        if (DEBUG) log("onInterceptTouchEvent:action:${getTouchActionDesc(event.actionMasked)} intercept=$intercept")
        return intercept
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (DEBUG) log("onTouchEvent action:${getTouchActionDesc(event.actionMasked)}")
        val contentView = contentView
        val rightMenuLayout = rightMenuLayout
        if (contentView == null || rightMenuLayout == null) {
            return super.onTouchEvent(event)
        }

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mState == STATE_TOUCH_DOWN) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    val dx = x - downX
                    val dy = y - downY
                    if (checkSlop(dx, dy, mTouchSlop) && abs(dx) > abs(dy)) {
                        if (canScrollHorizontally(dx)) {
                            downX = x
                            downY = y
                            requestDisallowInterceptTouchEvent(true)
                            setState(STATE_DRAGGING)
                            slideListener?.onPreDrag(this)
                        }
                    }
                } else if (mState == STATE_DRAGGING) {
                    touchMove(event)
                    slideListener?.onDrag(this)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchEnd(event)
            }
        }
        return true
    }

    private fun onTouchDown(event: MotionEvent) {
        val x = event.x.toInt()
        val y = event.y.toInt()
        downX = x
        downY = y
        mDownTransX = contentView!!.translationX
        setState(STATE_TOUCH_DOWN)
        slideListener?.onTouchDown(this)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        val contentView = contentView
        val rightMenuLayout = rightMenuLayout
        if (contentView == null || rightMenuLayout == null) {
            return canScrollHorizontally(direction)
        }
        if (direction < 0) {
            return contentView.translationX > -rightMenuWidthWithMargin
        } else {
            return contentView.translationX < 0
        }
    }

    private fun touchMove(event: MotionEvent) {
        val x = event.x.toInt()
        val dx = x - downX
        var transX = (mDownTransX + dx).toInt()
        transX = Math.min(0, Math.max(transX, -rightMenuWidthWithMargin))
        translateTo(transX)
    }

    private fun translateTo(transX: Int) {
        if (DEBUG) log("iptvtest translateTo:$transX")
        contentView?.translationX = transX.toFloat()
        rightMenuLayout?.translationX = transX.toFloat()
    }

    private fun touchEnd(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            slideListener?.onTouchUp(this)
        } else {
            slideListener?.onTouchCancel(this)
        }

        mVelocityTracker?.computeCurrentVelocity(1000, mMaxVelocity)
        val xVel: Float = clampMag(
            mVelocityTracker?.xVelocity ?: 0f,
            mMinVelocity, mMaxVelocity
        )
        val transX = contentView!!.translationX.toInt()
        mScroller.fling(transX, 0, xVel.toInt(), 0, -rightMenuWidthWithMargin, 0, 0, 0)
        val finalX = mScroller.finalX
        mScroller.forceFinished(true)

        val fromX = transX
        var dx = 0
        var duration = 0
        if (abs(finalX) > rightMenuWidthWithMargin / 2) {
            dx = -rightMenuWidthWithMargin - transX
            duration = calcDuration(abs(dx))
        } else {
            dx = -transX
            duration = calcDuration(abs(dx))
        }

        if (fromX == fromX + dx) {
            if (DEBUG) log("touchEnd ignore")
            setState(STATE_IDLE)
            return
        }

        mScroller.startScroll(fromX, 0, dx, 0, duration)

        if (DEBUG) {
            val action = if (event.actionMasked == MotionEvent.ACTION_UP) {
                "ACTION_UP"
            } else if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
                "ACTION_CANCEL"
            } else {
                "UNKNOWN"
            }
            log("touchEnd xVel:$xVel action:$action finalX:$finalX rightMenuWidthWithMargin:$rightMenuWidthWithMargin duration:$duration")
        }

        invalidate()
        cancel()
        setState(STATE_SETTLING)
    }

    private fun calcDuration(distance: Int): Int {
        return Math.round(distance / meanVelocity)
    }

    private fun clampMag(value: Float, absMin: Float, absMax: Float): Float {
        val absValue = abs(value)
        if (absValue < absMin) return 0f
        if (absValue > absMax) return if (value > 0) absMax else -absMax
        return value
    }

    private fun checkSlop(dx: Int, dy: Int, slop: Int): Boolean {
        return dx * dx + dy * dy > slop * slop
    }

    private fun cancel() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun setState(state: Int) {
        if (DEBUG) log("state change:old:${getStateDesc(mState)} new:${getStateDesc(state)}")
        mState = state
        if (mState == STATE_IDLE) {
            invokeMenuStateChanged()
        }
    }

    private fun invokeMenuStateChanged() {
        val transX = contentView?.translationX?.toInt() ?: return
        if (transX == 0) {
            if (mMenuState != MENU_STATE_CLOSED) {
                mMenuState = MENU_STATE_CLOSED
                slideListener?.onMenuClosed(this)
            }
        } else if (transX == -rightMenuWidthWithMargin) {
            if (mMenuState != MENU_STATE_RIGHT_OPENED) {
                mMenuState = MENU_STATE_RIGHT_OPENED
                slideListener?.onRightMenuOpened(this)
            }
        }
    }

    private fun getStateDesc(state: Int): String {
        return when (state) {
            STATE_IDLE -> "STATE_IDLE"
            STATE_TOUCH_DOWN -> "STATE_TOUCH_DOWN"
            STATE_DRAGGING -> "STATE_DRAGGING"
            STATE_SETTLING -> "STATE_SETTLING"
            else -> "Unknown"
        }
    }

    private fun getTouchActionDesc(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
            MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
            MotionEvent.ACTION_UP -> "ACTION_UP"
            MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
            else -> "Unknown"
        }
    }

    private fun log(msg: String) {
        Log.i("SlideMenuLayout", "$msg @${hashCode()}")
    }
}