package dhl.anddemo.base.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import dhl.anddemo.R;
import dhl.anddemo.base.util.PixelUtil;


public class PtrDefaultHeader extends FrameLayout implements IPtrHeader {

	private ImageView mIVAnim;

	private Animation mAnim;

	public PtrDefaultHeader(Context context) {
		this(context, null);
	}

	public PtrDefaultHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PtrDefaultHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		int pd = PixelUtil.dip2px(2);
		setPadding(0, pd, 0, pd);
		mIVAnim = new ImageView(context.getApplicationContext());
		mIVAnim.setImageResource(R.drawable.small_loop_progressbar);

		LayoutParams params = new LayoutParams(-2, -2);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		addView(mIVAnim, params);

		mAnim = new RotateAnimation(0f, 360f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnim.setDuration(1000);
		mAnim.setInterpolator(new LinearInterpolator());
		mAnim.setRepeatCount(Animation.INFINITE);
	}

	@Override
	public void onReset() {
	}

	@Override
	public void onPullToRefresh() {
	}

	@Override
	public void onReleaseToRefresh() {
	}

	@Override
	public void onRefreshing() {
		mIVAnim.startAnimation(mAnim);
	}

	@Override
	public void onRefreshComplete() {
		mIVAnim.setAnimation(null);
	}

	@Override
	public void onPositionChanged(int state, int headerHeight, int offset) {
	}

}
