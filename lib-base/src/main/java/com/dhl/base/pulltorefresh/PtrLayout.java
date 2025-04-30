package com.dhl.base.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import java.util.Arrays;

/**
 * Created by DuanHl on 2015/11/24.
 */
public class PtrLayout extends ViewGroup {

    public static final int STATE_RESET = 0x1;
    public static final int STATE_PULL_TO_REFRESH = 0x2;
    public static final int STATE_RELEASE_TO_REFRESH = 0x3;
    public static final int STATE_REFRESHING = 0x4;
    public static final int STATE_REFRESH_COMPLETE = 0x5;

    private View mContent;
    private IPtrHeader mHeader;
    private PtrScroller mScroller;
    private int mState = STATE_RESET;
    private PtrConfig mConfig;
    private OnRefreshListener mRefreshListener;
    private boolean mUnderTouch;
    private PtrScroller mAutoRefreshScroller;
    private Runnable mAutoRefreshRunnable;
    private boolean mSendCancle;            //开始下拉刷新后要向子view发送cancel事件
    private int mTouchSlop;
    private boolean mTouchMoveEvent;
    private boolean hasRequestDisallowIntercept;

    // Last known position/pointer tracking
    private float[] mInitialMotionX;
    private float[] mInitialMotionY;
    private float[] mLastMotionX;
    private float[] mLastMotionY;
    private int mPointersDown;

    private final Runnable waitRefreshRunnable = this::autoRefresh;

    public PtrLayout(Context context) {
        this(context, null);
    }

    public PtrLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PtrLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new PtrScroller();
        mAutoRefreshScroller = new PtrScroller();
        mConfig = new PtrConfig();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalStateException("PtrLayout can only host one child");
        } else {
            mContent = getChildAt(0);
            mContent.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    public void setHeader(IPtrHeader header) {
        mHeader = header;
        if (mHeader != null) {
            View view = (View) header;
            addView(view, 0);
        }
    }

    public void setOnRefreshListener(OnRefreshListener lis) {
        mRefreshListener = lis;
    }

    public PtrConfig getConfig() {
        return mConfig;
    }

    /**
     * 刷新完成,重置ui
     */
    public void refreshComplete() {
        mState = STATE_REFRESH_COMPLETE;
        if (mHeader != null) mHeader.onRefreshComplete();
        if (!mUnderTouch) {
            final int scrollY = getScrollY();
            mScroller.startScroll(0, scrollY, 0, -scrollY, mConfig.getTimeScrollToReset());
            invalidate();
        }
    }

    /**
     * 自动滚动到刷新位置,开始加载
     */
    public void autoRefresh() {
        if (getRight() == 0) {
            postRunnable(waitRefreshRunnable);
            return;
        }

        if (mState != STATE_REFRESHING && mAutoRefreshScroller.isFinished()) {
            mScroller.forceFinished(true);

            if (mAutoRefreshRunnable == null) {
                mAutoRefreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mAutoRefreshScroller.computeScrollOffset()) {
                            scrollBy(0, mAutoRefreshScroller.getCurrY() - getScrollY());
                            postRunnable(mAutoRefreshRunnable);
                        } else {
                            if (mRefreshListener != null) mRefreshListener.onRefresh();
                        }
                    }
                };
            }
            mState = STATE_REFRESHING;
            if (mHeader != null) mHeader.onRefreshing();
            removeCallbacks(mAutoRefreshRunnable);
            mAutoRefreshScroller.startScroll(0, getScrollY(), 0, -mConfig.getHeightTriggerRefresh() - getScrollY(), mConfig.getTimeScrollToRefreshing());
            postRunnable(mAutoRefreshRunnable);
        }
    }

    private void postRunnable(Runnable runnable) {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        //    postOnAnimation(runnable);
        //} else {
        //    postDelayed(runnable, 16);
        //}
        postOnAnimation(runnable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeader != null) {
            final View header = (View) mHeader;
            measureChildWithMargins(header, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mConfig.setHeaderHeight(header.getMeasuredHeight());
        }
        measureChildWithMargins(mContent, widthMeasureSpec, 0, heightMeasureSpec, 0);
        setMeasuredDimension(resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec),
                resolveSize(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left;
        int top;
        int right;
        int bottom;

        //layout header
        if (mHeader != null) {
            final View header = (View) mHeader;
            left = getPaddingLeft();
            top = getPaddingTop() - header.getMeasuredHeight();
            right = left + getMeasuredWidth();
            bottom = getPaddingTop();
            header.layout(left, top, right, bottom);
        }

        //layout content
        left = getPaddingLeft();
        top = getPaddingTop();
        right = left + mContent.getMeasuredWidth();
        bottom = top + mContent.getMeasuredHeight();
        mContent.layout(left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final int actionIndex = ev.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            clearMotionHistory();
        }

        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mSendCancle = false;
                mTouchMoveEvent = false;
                mUnderTouch = true;
                hasRequestDisallowIntercept = false;
                mScroller.forceFinished(true);

                final float x = ev.getX();
                final float y = ev.getY();
                final int pointerId = ev.getPointerId(0);
                saveInitialMotion(x, y, pointerId);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int pointerId = ev.getPointerId(actionIndex);
                final float x = ev.getX(actionIndex);
                final float y = ev.getY(actionIndex);
                saveInitialMotion(x, y, pointerId);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int pointerId = ev.getPointerId(actionIndex);
                final float x = ev.getX(actionIndex);
                final float y = ev.getY(actionIndex);
                final int idx = (int) (x - mLastMotionX[pointerId]);
                final int idy = (int) (y - mLastMotionY[pointerId]);
                saveLastMotion(ev);

                if (!mTouchMoveEvent && (Math.abs(x - mInitialMotionX[pointerId]) >= mTouchSlop || Math.abs(y - mInitialMotionY[pointerId]) >= mTouchSlop)) {
                    mTouchMoveEvent = true;
                    if (mHeader != null) mHeader.onTouchScroll();
                }

                if (mTouchMoveEvent && Math.abs(idy) > Math.abs(idx)) {
                    handled = touchMove((int) (idy * mConfig.mFriction), ev);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerId = ev.getPointerId(actionIndex);
                clearMotionHistory(pointerId);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mUnderTouch = false;
                mTouchMoveEvent = false;

                touchEnd();
                clearMotionHistory();
                break;
            }
        }

        if (!handled) {
            handled = super.dispatchTouchEvent(ev);
        }
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;//消费掉触摸事件,保证dispatchTouchEvent()方法可以一直回调
    }

    private boolean touchMove(int offset, MotionEvent ev) {
        if (!mAutoRefreshScroller.isFinished()) {
            return false;
        }
        int scrollY = getScrollY();
        if (scrollY != 0 || (offset > 0 && !canScroll(mContent, true, offset, (int) ev.getX(), (int) ev.getY()))) {
            if (!hasRequestDisallowIntercept) {
                hasRequestDisallowIntercept = true;
                requestDisallowInterceptTouchEvent(true);
            }
            int scrollTo = scrollY - offset;
            if (scrollTo > 0) {
                offset = scrollY;
            }
            scrollBy(0, -offset);
            scrollY = getScrollY();
            if (mState != STATE_REFRESHING && mState != STATE_REFRESH_COMPLETE) {
                if (scrollY < 0 && scrollY >= -mConfig.getHeightTriggerRefresh()) {
                    if (mState != STATE_PULL_TO_REFRESH) {
                        mState = STATE_PULL_TO_REFRESH;
                        if (mHeader != null) mHeader.onPullToRefresh();
                    }
                } else if (scrollY < -mConfig.getHeightTriggerRefresh()) {
                    if (mState != STATE_RELEASE_TO_REFRESH) {
                        mState = STATE_RELEASE_TO_REFRESH;
                        if (mHeader != null) mHeader.onReleaseToRefresh();
                    }
                } else if (scrollY == 0) {
                    if (mState != STATE_RESET) {
                        mState = STATE_RESET;
                        if (mHeader != null) mHeader.onReset();
                    }
                }
            }
            if (scrollY == 0) {
                if (mState != STATE_RESET && mState != STATE_REFRESHING) {
                    mState = STATE_RESET;
                    if (mHeader != null) mHeader.onReset();
                }
            }
            if (mHeader != null)
                mHeader.onPositionChanged(mState, mConfig.mHeaderHeight, -getScrollY(), offset);

            if (scrollY < 0 && !mSendCancle) {
                //向子view发送cancel事件,用来取消子view的点击状态
                mSendCancle = true;
                MotionEvent e = MotionEvent.obtain(ev.getDownTime(),
                        ev.getEventTime() + ViewConfiguration.getLongPressTimeout(),
                        MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(),
                        ev.getMetaState());
                super.dispatchTouchEvent(e);
                e.recycle();
                return true;
            } else if (scrollY == 0 && offset < 0) {
                //向子view发送down事件,让子view开始处理滑动事件
                MotionEvent e = MotionEvent.obtain(ev.getDownTime(),
                        ev.getEventTime(), MotionEvent.ACTION_DOWN, ev.getX(),
                        ev.getY(), ev.getMetaState());
                mSendCancle = false;
                super.dispatchTouchEvent(e);
                e.recycle();
                return true;
            }
        }
        return false;
    }

    private boolean touchEnd() {
        if (!mAutoRefreshScroller.isFinished()) {
            return false;
        }
        final int scrollY = getScrollY();
        if (scrollY != 0) {
            if ((scrollY < 0 && scrollY > -mConfig.getHeightTriggerRefresh())
                    || mState == STATE_REFRESH_COMPLETE) {
                mScroller.startScroll(0, scrollY, 0, -scrollY, mConfig.getTimeScrollToReset());
                invalidate();
            } else if (scrollY <= -mConfig.getHeightTriggerRefresh()) {
                mScroller.startScroll(0, scrollY, 0, -mConfig.getHeightToKeepWhenRefreshing() - scrollY, mConfig.getTimeScrollToRefreshing());
                if (mHeader != null) mHeader.onRefreshing();
                invalidate();
            }
            return true;
        }
        return false;
    }

    private void clearMotionHistory() {
        if (mInitialMotionX == null) {
            return;
        }
        Arrays.fill(mInitialMotionX, 0);
        Arrays.fill(mInitialMotionY, 0);
        Arrays.fill(mLastMotionX, 0);
        Arrays.fill(mLastMotionY, 0);
        mPointersDown = 0;
    }

    private void clearMotionHistory(int pointerId) {
        if (mInitialMotionX == null || !isPointerDown(pointerId)) {
            return;
        }
        mInitialMotionX[pointerId] = 0;
        mInitialMotionY[pointerId] = 0;
        mLastMotionX[pointerId] = 0;
        mLastMotionY[pointerId] = 0;
        mPointersDown &= ~(1 << pointerId);
    }

    private void ensureMotionHistorySizeForId(int pointerId) {
        if (mInitialMotionX == null || mInitialMotionX.length <= pointerId) {
            float[] imx = new float[pointerId + 1];
            float[] imy = new float[pointerId + 1];
            float[] lmx = new float[pointerId + 1];
            float[] lmy = new float[pointerId + 1];

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX, 0, imx, 0, mInitialMotionX.length);
                System.arraycopy(mInitialMotionY, 0, imy, 0, mInitialMotionY.length);
                System.arraycopy(mLastMotionX, 0, lmx, 0, mLastMotionX.length);
                System.arraycopy(mLastMotionY, 0, lmy, 0, mLastMotionY.length);
            }

            mInitialMotionX = imx;
            mInitialMotionY = imy;
            mLastMotionX = lmx;
            mLastMotionY = lmy;
        }
    }

    private void saveInitialMotion(float x, float y, int pointerId) {
        ensureMotionHistorySizeForId(pointerId);
        mInitialMotionX[pointerId] = mLastMotionX[pointerId] = x;
        mInitialMotionY[pointerId] = mLastMotionY[pointerId] = y;
        mPointersDown |= 1 << pointerId;
    }

    private void saveLastMotion(MotionEvent ev) {
        final int pointerCount = ev.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            final int pointerId = ev.getPointerId(i);
            // If pointer is invalid then skip saving on ACTION_MOVE.
            if (!isValidPointerForActionMove(pointerId)) {
                continue;
            }
            final float x = ev.getX(i);
            final float y = ev.getY(i);
            mLastMotionX[pointerId] = x;
            mLastMotionY[pointerId] = y;
        }
    }

    public boolean isPointerDown(int pointerId) {
        return (mPointersDown & 1 << pointerId) != 0;
    }

    private boolean isValidPointerForActionMove(int pointerId) {
        if (!isPointerDown(pointerId)) {
            Log.e("PtrLayout", "Ignoring pointerId=" + pointerId + " because ACTION_DOWN was not received "
                    + "for this pointer before ACTION_MOVE. It likely happened because "
                    + " ViewDragHelper did not receive all the events in the event stream.");
            return false;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        int scrollY = getScrollY();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            if (mScroller.getCurrY() == -mConfig.getHeightToKeepWhenRefreshing()) {
                if (mState == STATE_RELEASE_TO_REFRESH || mState == STATE_PULL_TO_REFRESH) {
                    mState = STATE_REFRESHING;
                    if (mRefreshListener != null) mRefreshListener.onRefresh();
                }
            } else if (mScroller.getCurrY() == 0) {
                if (mState != STATE_RESET) {
                    mState = STATE_RESET;
                    if (mHeader != null) mHeader.onReset();
                }
            }
            if (mHeader != null) {
                mHeader.onPositionChanged(mState, mConfig.mHeaderHeight, -getScrollY(), -getScrollY() + scrollY);
            }
            invalidate();
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    private boolean canChildScrollDown(View view) {
        if (view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                    .getTop() < absListView.getPaddingTop());
        } else {
            return view.getScrollY() > 0;
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            // Count backwards - let topmost views consume scroll distance first.
            for (int i = count - 1; i >= 0; i--) {
                // TODO: Add versioned support here for transformed views.
                // This will nogpedit.msct work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, x + scrollX - child.getLeft(),
                                y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && v.canScrollVertically(-dx);
    }

    public class PtrConfig {
        /**
         * 滑动摩擦系数, 滑动距离 = 手指移动距离 * mFriction
         */
        private float mFriction = 0.7f;
        /**
         * 触发刷新的高度和header高度的比
         */
        private float mRatioTriggerRefresh = 1f;
        /**
         * 刷新时保持的高度和header高度的比
         */
        private float mRatioToKeepWhenRefreshing = 1f;
        /**
         * 滚动到刷新位置的时间
         */
        private int mTimeScrollToRefreshing = 250;
        /**
         * 滚动到初始位置的时间
         */
        private int mTimeScrollToReset = 250;

        private int mHeaderHeight;
        private int mHeightTriggerRefresh;
        private int mHeightToKeepWhenRefreshing;

        /**
         * 设置滑动摩擦力, 滑动距离 = 手指移动距离 * mFriction
         */
        public PtrConfig setFriction(float f) {
            mFriction = f;
            return this;
        }

        /**
         * 设置触发刷新的高度和header高度的比
         */
        public PtrConfig setRatioTriggerRefresh(float r) {
            mRatioTriggerRefresh = r;
            mHeightTriggerRefresh = (int) (mHeaderHeight * r);
            return this;
        }

        /**
         * 设置刷新时保持的高度和header高度的比
         */
        public PtrConfig setRatioToKeepWhenRefreshing(float r) {
            mRatioToKeepWhenRefreshing = r;
            mHeightToKeepWhenRefreshing = (int) (mHeaderHeight * r);
            return this;
        }

        /**
         * 设置滚动到刷新位置的时间
         */
        public PtrConfig setTimeScrollToRefreshing(int time) {
            mTimeScrollToRefreshing = time;
            return this;
        }

        /**
         * 设置滚动到初始位置的时间
         */
        public PtrConfig setTimeScrollToReset(int time) {
            mTimeScrollToReset = time;
            return this;
        }

        void setHeaderHeight(int h) {
            mHeaderHeight = h;
            mHeightTriggerRefresh = (int) (mHeaderHeight * mRatioTriggerRefresh);
            mHeightToKeepWhenRefreshing = (int) (mHeaderHeight * mRatioToKeepWhenRefreshing);
        }

        int getHeightTriggerRefresh() {
            return mHeightTriggerRefresh;
        }

        int getHeightToKeepWhenRefreshing() {
            return mHeightToKeepWhenRefreshing;
        }

        int getTimeScrollToRefreshing() {
            return mTimeScrollToRefreshing;
        }

        int getTimeScrollToReset() {
            return mTimeScrollToReset;
        }
    }

    private class PtrScroller {
        private final Interpolator mInterpolator = new ViscousFluidInterpolator();

        private int mStartX;
        private int mStartY;
        private int mFinalX;
        private int mFinalY;
        private int mCurrX;
        private int mCurrY;

        private long mStartTime;
        private int mDuration;
        private float mDurationReciprocal;
        private float mDeltaX;
        private float mDeltaY;
        private boolean mFinished = true;

        public final boolean isFinished() {
            return mFinished;
        }

        public final void forceFinished(boolean finished) {
            mFinished = finished;
        }

        public void abortAnimation() {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }

        public final int getCurrX() {
            return mCurrX;
        }

        public final int getCurrY() {
            return mCurrY;
        }

        public boolean computeScrollOffset() {
            if (mFinished) {
                return false;
            }

            int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);

            if (timePassed < mDuration) {
                final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
            } else {
                mCurrX = mFinalX;
                mCurrY = mFinalY;
                mFinished = true;
            }
            return true;
        }

        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            mFinished = false;
            mDuration = duration;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartX = startX;
            mStartY = startY;
            mFinalX = startX + dx;
            mFinalY = startY + dy;
            mDeltaX = dx;
            mDeltaY = dy;
            mDurationReciprocal = 1.0f / (float) mDuration;

            if (dx == 0 && dy == 0) {
                mDuration = 0;
            }
        }
    }

    static class ViscousFluidInterpolator implements Interpolator {
        /**
         * Controls the viscous fluid effect (how much of it).
         */
        private static final float VISCOUS_FLUID_SCALE = 2f;

        private static final float VISCOUS_FLUID_NORMALIZE;
        private static final float VISCOUS_FLUID_OFFSET;

        static {

            // must be set to 1.0 (used in viscousFluid())
            VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
            // account for very small floating-point error
            VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);
        }

        private static float viscousFluid(float x) {
            x *= VISCOUS_FLUID_SCALE;
            if (x < 1.0f) {
                x -= (1.0f - (float) Math.exp(-x));
            } else {
                float start = 0.36787944117f;   // 1/e == exp(-1)
                x = 1.0f - (float) Math.exp(1.0f - x);
                x = start + x * (1.0f - start);
            }
            return x;
        }

        @Override
        public float getInterpolation(float input) {
            final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            if (interpolated > 0) {
                return interpolated + VISCOUS_FLUID_OFFSET;
            }
            return interpolated;
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
