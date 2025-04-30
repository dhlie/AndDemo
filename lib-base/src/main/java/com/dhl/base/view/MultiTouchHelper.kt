package com.dhl.base.view

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.atan

/**
 *
 * Author: duanhl
 * Create: 2024/6/8 09:00
 * Description:
 *
 */
class MultiTouchHelper(context: Context) {

    companion object {
        private const val DEBUG = false
        private fun log(msg: String) {
            if (DEBUG) {
                Log.i("MultiTouchHelper", "$msg @${hashCode()}")
            }
        }
    }

    abstract class TouchCallback {
        open fun onActionDown(event: MotionEvent) {}
        open fun onPreMove(event: MotionEvent, x: Float, y: Float, dx: Float, dy: Float) {}
        open fun onMove(event: MotionEvent) {}
        open fun onActionUp(event: MotionEvent, isSingleClick: Boolean, isDoubleClick: Boolean) {}
        open fun onActionPointerDown(event: MotionEvent) {}
        open fun onActionPointerUp(event: MotionEvent) {}
        open fun onActionCancel(event: MotionEvent) {}
    }

    private var mIsDragging = false

    private var mInitialMotionX: FloatArray? = null
    private var mInitialMotionY: FloatArray? = null
    private var mLastMotionX: FloatArray? = null
    private var mLastMotionY: FloatArray? = null
    private var mPointersDown = 0

    private var mIsTrackVelocity = false
    private var mVelocityTracker: VelocityTracker? = null
    private var mMaxVelocity = 0f
    private var mMinVelocity = 0f
    private var mTouchSlop = 0

    //双击手势判定
    private var mDoubleTapSlop = 0
    private var mDoubleTapTimeout = 300
    private var mLastDownTime = 0L
    private var mLastDownX = 0
    private var mLastDownY = 0
    private var mCurrDownTime = 0L
    private var mCurrDownX = 0
    private var mCurrDownY = 0

    private var mTouchCallback: TouchCallback? = null

    init {
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
        mDoubleTapSlop = vc.scaledDoubleTapSlop
        mMaxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        mMinVelocity = vc.scaledMinimumFlingVelocity.toFloat()
    }

    fun setTouchCallback(callback: TouchCallback) {
        mTouchCallback = callback
    }

    fun setTrackVelocity(track: Boolean) {
        mIsTrackVelocity = track
    }

    fun onTouchEvent(event: MotionEvent) {
        val action: Int = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mIsTrackVelocity) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain()
            }
            mVelocityTracker!!.addMovement(event)
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val pointerId: Int = event.getPointerId(0)
                val x = event.x
                val y = event.y
                saveInitialMotion(x, y, pointerId)

                mCurrDownTime = SystemClock.uptimeMillis()
                mCurrDownX = x.toInt()
                mCurrDownY = y.toInt()

                mIsDragging = false
                mTouchCallback?.onActionDown(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val actionIndex: Int = event.actionIndex
                val pointerId: Int = event.getPointerId(actionIndex)
                val x = event.getX(actionIndex)
                val y = event.getY(actionIndex)
                saveInitialMotion(x, y, pointerId)

                mIsDragging = true
                mTouchCallback?.onActionPointerDown(event)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val actionIndex: Int = event.actionIndex
                val pointerId: Int = event.getPointerId(actionIndex)
                clearMotionHistory(pointerId)

                mTouchCallback?.onActionPointerUp(event)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mIsDragging) {
                    val pointerId: Int = event.getPointerId(0)
                    if (!isValidPointerForActionMove(pointerId)) return

                    val x = event.getX(0)
                    val y = event.getY(0)
                    val dx = x - getInitMotionX(pointerId)
                    val dy = y - getInitMotionY(pointerId)

                    if (checkSlop(dx, dy, mTouchSlop)) {
                        mTouchCallback?.onPreMove(event, x, y, dx, dy)
                        mIsDragging = true
                    }
                } else {
                    mTouchCallback?.onMove(event)
                }
                saveLastMotion(event)
            }

            MotionEvent.ACTION_UP -> {
                var isSingleClick = false
                var isDoubleClick = false
                if (!mIsDragging) {
                    if (SystemClock.uptimeMillis() - mLastDownTime <= mDoubleTapTimeout) {
                        if (!checkSlop(event.x - mLastDownX, event.y - mLastDownY, mDoubleTapSlop)) {
                            isDoubleClick = true
                            mLastDownTime = 0
                            mLastDownX = 0
                            mLastDownY = 0
                        } else {
                            mLastDownTime = mCurrDownTime
                            mLastDownX = mCurrDownX
                            mLastDownY = mCurrDownY
                        }
                    } else {
                        mLastDownTime = mCurrDownTime
                        mLastDownX = mCurrDownX
                        mLastDownY = mCurrDownY
                    }
                    if (!isDoubleClick) {
                        isSingleClick = true
                    }
                } else {
                    mLastDownTime = 0
                    mLastDownX = 0
                    mLastDownY = 0
                }
                mTouchCallback?.onActionUp(event, isSingleClick, isDoubleClick)
                cancel()
                mIsDragging = false
            }

            MotionEvent.ACTION_CANCEL -> {
                mTouchCallback?.onActionCancel(event)
                mLastDownTime = 0
                mLastDownX = 0
                mLastDownY = 0
                cancel()
                mIsDragging = false
            }
        }
    }

    /**
     * 调用之前要先用 [MultiTouchHelper.isValidPointerForActionMove] 检查 pointerId 是否有效
     *
     * val actionIndex: Int = event.actionIndex
     * val pointerId: Int = event.getPointerId(actionIndex)
     * if (isValidPointerForActionMove(pointerId)) {
     *     val x = getInitMotionX(pointerId)
     *     val y = getInitMotionY(pointerId)
     * }
     *
     */
    fun getInitMotionX(pointerId: Int) = mInitialMotionX!![pointerId]

    /**
     * @see getInitMotionX
     */
    fun getInitMotionY(pointerId: Int) = mInitialMotionY!![pointerId]

    /**
     * @see getInitMotionX
     */
    fun getLastMotionX(pointerId: Int) = mLastMotionX!![pointerId]

    /**
     * @see getInitMotionX
     */
    fun getLastMotionY(pointerId: Int) = mLastMotionY!![pointerId]

    fun isValidPointerForActionMove(pointerId: Int): Boolean {
        return isPointerDown(pointerId)
    }

    fun computeCurrentVelocity(): Pair<Float, Float> {
        if (mVelocityTracker == null) {
            return Pair(0f, 0f)
        }
        mVelocityTracker!!.computeCurrentVelocity(1000, mMaxVelocity)
        val xVel: Float = clampMag(
            mVelocityTracker!!.xVelocity,
            mMinVelocity, mMaxVelocity
        )
        val yVel: Float = clampMag(
            mVelocityTracker!!.yVelocity,
            mMinVelocity, mMaxVelocity
        )
        return Pair(xVel, yVel)
    }

    private fun clearMotionHistory(pointerId: Int) {
        if (mInitialMotionX == null || !isPointerDown(pointerId)) {
            return
        }
        mInitialMotionX!![pointerId] = 0f
        mInitialMotionY!![pointerId] = 0f
        mLastMotionX!![pointerId] = 0f
        mLastMotionY!![pointerId] = 0f
        mPointersDown = mPointersDown and (1 shl pointerId).inv()
    }

    private fun saveInitialMotion(x: Float, y: Float, pointerId: Int) {
        ensureMotionHistorySizeForId(pointerId)
        mLastMotionX!![pointerId] = x
        mInitialMotionX!![pointerId] = mLastMotionX!![pointerId]
        mLastMotionY!![pointerId] = y
        mInitialMotionY!![pointerId] = mLastMotionY!![pointerId]
        mPointersDown = mPointersDown or (1 shl pointerId)
    }

    private fun saveLastMotion(ev: MotionEvent) {
        val pointerCount = ev.pointerCount
        for (i in 0 until pointerCount) {
            val pointerId = ev.getPointerId(i)
            // If pointer is invalid then skip saving on ACTION_MOVE.
            if (!isValidPointerForActionMove(pointerId)) {
                continue
            }
            val x = ev.getX(i)
            val y = ev.getY(i)
            mLastMotionX!![pointerId] = x
            mLastMotionY!![pointerId] = y
        }
    }

    private fun ensureMotionHistorySizeForId(pointerId: Int) {
        if (mInitialMotionX == null || mInitialMotionX!!.size <= pointerId) {
            val imx = FloatArray(pointerId + 1)
            val imy = FloatArray(pointerId + 1)
            val lmx = FloatArray(pointerId + 1)
            val lmy = FloatArray(pointerId + 1)

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX!!, 0, imx, 0, mInitialMotionX!!.size)
                System.arraycopy(mInitialMotionY!!, 0, imy, 0, mInitialMotionY!!.size)
                System.arraycopy(mLastMotionX!!, 0, lmx, 0, mLastMotionX!!.size)
                System.arraycopy(mLastMotionY!!, 0, lmy, 0, mLastMotionY!!.size)
            }

            mInitialMotionX = imx
            mInitialMotionY = imy
            mLastMotionX = lmx
            mLastMotionY = lmy
        }
    }

    /**
     * Check if the given pointer ID represents a pointer that is currently down (to the best
     * of the ViewDragHelper's knowledge).
     *
     *
     * The state used to report this information is populated by the methods
     * [.shouldInterceptTouchEvent] or
     * [.processTouchEvent]. If one of these methods has not
     * been called for all relevant MotionEvents to track, the information reported
     * by this method may be stale or incorrect.
     *
     * @param pointerId pointer ID to check; corresponds to IDs provided by MotionEvent
     * @return true if the pointer with the given ID is still down
     */
    private fun isPointerDown(pointerId: Int): Boolean {
        return (mPointersDown and (1 shl pointerId)) != 0
    }

    private fun checkSlop(dx: Float, dy: Float, slop: Int): Boolean {
        return dx * dx + dy * dy > slop * slop
    }

    /**
     * The result of a call to this method is equivalent to
     * [.processTouchEvent] receiving an ACTION_CANCEL event.
     */
    private fun cancel() {
        clearMotionHistory()

        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun clearMotionHistory() {
        if (mInitialMotionX == null) {
            return
        }
        Arrays.fill(mInitialMotionX!!, 0f)
        Arrays.fill(mInitialMotionY!!, 0f)
        Arrays.fill(mLastMotionX!!, 0f)
        Arrays.fill(mLastMotionY!!, 0f)
        mPointersDown = 0
    }

    private fun clampMag(value: Float, absMin: Float, absMax: Float): Float {
        val absValue = abs(value)
        if (absValue < absMin) return 0f
        if (absValue > absMax) return if (value > 0) absMax else -absMax
        return value
    }

    fun calculateAngle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int, x4: Int, y4: Int): Double {
        try {
            //a: 直线斜率 y = ax + b
            val slope1 = (y2 - y1).toDouble() / (x2 - x1).toDouble()
            val slope2 = (y4 - y3).toDouble() / (x4 - x3).toDouble()
            if (slope1.isInfinite() || slope1.isNaN() || slope2.isInfinite() || slope2.isNaN()) {
                return 0.0
            }

            val angle = atan(abs((slope2 - slope1) / (1 + slope1 * slope2)))
            // 将弧度转换为角度
            val degree = Math.toDegrees(angle)
            //if (DEBUG) log("($x1 ,$y1)($x2 ,$y2)($x3 ,$y3)($x4 ,$y4) y2 - y1:${y2 - y1} x2 - x1:${x2 - x1} y4 - y3:${y4 - y3} x4 - x3:${x4 - x3}")
            //if (DEBUG) log("degree:$degree angle:$angle slope1:$slope1 slope2:$slope2 分子:${slope2 - slope1} ad:${slope1 * slope2} 分母:${1 + slope1 * slope2}")
            if (degree.isInfinite() || degree.isNaN()) {
                return 0.0
            }
            //斜率变大是逆时针旋转, 变小是顺时针旋转
            return if (slope2 > slope1) {
                degree
            } else {
                -degree
            }
        } catch (e: Exception) {
            return 0.0
        }
    }

    class ScaleScroller(interpolator: Interpolator? = null) {

        private val mInterpolator: Interpolator = interpolator ?: DecelerateInterpolator()

        var mStartScale = 0f
        var mFinalScale = 0f
        var mCurrScale = 0f
        private var mDeltaScale = 0f

        var mStartX = 0
        var mFinalX = 0
        var mCurrX = 0
        private var mDeltaX = 0

        var mStartY = 0
        var mFinalY = 0
        var mCurrY = 0
        private var mDeltaY = 0

        private var mStartTime = 0L
        private var mDuration = 0
        private var mDurationReciprocal = 0f
        private var mFinished = true

        fun isFinished(): Boolean {
            return mFinished
        }

        fun forceFinished(finished: Boolean) {
            mFinished = finished
        }

        fun startScale(startScale: Float, dstScale: Float, startX: Int, dstX: Int, startY: Int, dstY: Int, duration: Int) {
            mFinished = false
            mStartScale = startScale
            mCurrScale = startScale
            mFinalScale = dstScale
            mDeltaScale = dstScale - startScale

            mStartX = startX
            mCurrX = startX
            mFinalX = dstX
            mDeltaX = dstX - startX

            mStartY = startY
            mCurrY = startY
            mFinalY = dstY
            mDeltaY = dstY - startY

            mDuration = duration
            mStartTime = AnimationUtils.currentAnimationTimeMillis()
            mDurationReciprocal = 1.0f / mDuration
        }

        fun computeScale(): Boolean {
            if (mFinished) {
                return false
            }

            val timePassed = (AnimationUtils.currentAnimationTimeMillis() - mStartTime).toInt()

            if (timePassed < mDuration) {
                val x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal)
                mCurrScale = mStartScale + x * mDeltaScale
                mCurrX = mStartX + (x * mDeltaX).toInt()
                mCurrY = mStartY + (x * mDeltaY).toInt()
            } else {
                mCurrScale = mFinalScale
                mCurrX = mFinalX
                mCurrY = mFinalY
                mFinished = true
            }
            return true
        }

    }

}