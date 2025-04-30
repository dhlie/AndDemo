package com.dhl.base.pulltorefresh.header;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.dhl.base.BaseExtKt;
import com.dhl.base.pulltorefresh.IPtrHeader;
import com.dhl.base.view.LoadingView;

public class PtrDefaultHeader extends FrameLayout implements IPtrHeader {

    private static final boolean DEBUG = false;

    private final LoadingView mViewAnim;

    public PtrDefaultHeader(Context context) {
        this(context, null);
    }

    public PtrDefaultHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PtrDefaultHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int pd = BaseExtKt.getDp(2);
        setPadding(0, pd, 0, pd);
        mViewAnim = new LoadingView(context.getApplicationContext());
        int padding = pd * 8;
        mViewAnim.setPadding(padding, padding, padding, padding);

        int size = BaseExtKt.getDp(64);
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        addView(mViewAnim, params);
    }

    @Override
    public void onReset() {
        mViewAnim.stopAnim();
        if (DEBUG) Log.i("PtrLayout", "-------onReset");
    }

    @Override
    public void onPullToRefresh() {
        if (DEBUG) Log.i("PtrLayout", "-------onPullToRefresh");
    }

    @Override
    public void onReleaseToRefresh() {
        if (DEBUG) Log.i("PtrLayout", "-------onReleaseToRefresh");
    }

    @Override
    public void onRefreshing() {
        if (DEBUG) Log.i("PtrLayout", "-------onRefreshing");
        mViewAnim.startAnim();
    }

    @Override
    public void onRefreshComplete() {
        if (DEBUG) Log.i("PtrLayout", "-------onRefreshComplete");
        //mViewAnim.stopAnim();
    }

    @Override
    public void onTouchScroll() {
        if (DEBUG) Log.i("PtrLayout", "-------onTouchScroll");
        mViewAnim.stopAnim();
    }

    @Override
    public void onPositionChanged(int state, int headerHeight, int offset, int dy) {
        //if (DEBUG) Log.i("PtrLayout", "-------onPositionChanged dy:" + dy + " offset:" + offset);
        float deg = (float) dy / headerHeight * 360;
        mViewAnim.addDegree(deg);
        mViewAnim.invalidate();
    }

}
