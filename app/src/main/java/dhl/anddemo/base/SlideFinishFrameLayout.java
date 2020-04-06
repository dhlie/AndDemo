package dhl.anddemo.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import dhl.anddemo.base.util.LLog;
import dhl.anddemo.base.util.PixelUtil;

public class SlideFinishFrameLayout extends FrameLayout {

	public static final int TYPE_TRANSLATE = 1;//上一个界面左右滑动效果
	public static final int TYPE_SCALE = 2;//上一个界面缩放效果
	public static final int TYPE = TYPE_SCALE;

	private Drawable mShadowDrawable;
	private int mShadowWidth = PixelUtil.dip2px(16);
	private Activity mActivity;
	private Activity mPreviousActivity;
	private SlideFinishDragHelper mDragger;
	private float mScrollPercent;
	private final float mSlidePercent = 0.2f;//前一个activity滚动的最大百分比 android:toXDelta="-20%p"
	private int mScrimeMaxDistance;
	private boolean mEnable = true;
	private final float mScale = 0.97f;//跟scale_in.xml/scale_out.xml中缩放值保持一致
	private Paint mPaint = new Paint();


	public SlideFinishFrameLayout(Context context) {
		this(context, null);
	}

	public SlideFinishFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideFinishFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//init
		int colorStart = Color.parseColor("#00000000");
		int colorEnd = Color.parseColor("#7F000000");
		mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStart, colorEnd});
		mPaint.setColor(0xff000000);
		mPaint.setStyle(Paint.Style.FILL);
	}

	public void attachToActivity(Activity act) {
		mActivity = act;
		ViewGroup decorView = (ViewGroup) act.getWindow().getDecorView();
		if (decorView.getChildCount() == 0) return;
		View contentView = decorView.getChildAt(0);
		decorView.removeView(contentView);
		addView(contentView);
		decorView.addView(this, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mDragger = SlideFinishDragHelper.create(decorView, new SlideFinishDragHelper.Callback() {

			@Override
			public boolean tryCaptureView(View child, int pointerId) {
				if (getWidth() > getHeight()) return false;
				return child == SlideFinishFrameLayout.this;
			}

			@Override
			public void onViewCaptured(View capturedChild, int activePointerId) {
				if (capturedChild == SlideFinishFrameLayout.this) {
					mPreviousActivity = App.getInstance().getPreviousActivity(mActivity);
					if (mPreviousActivity != null && mPreviousActivity.isFinishing()) {
						mPreviousActivity = null;
					}
				}
			}

			@Override
			public int getViewHorizontalDragRange(View child) {
				return 1;
			}

			@Override
			public int getViewVerticalDragRange(View child) {
				return 0;
			}

			@Override
			public int clampViewPositionHorizontal(View child, int left, int dx) {
				if (LLog.PRINT_LOG) LLog.i("clampViewPositionHorizontal   left:" + left + "   dx:" + dx);
				if (mEnable) {
					return Math.min(Math.max(0, left), getWidth());
				} else {
					return 0;
				}
			}

			@Override
			public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
				if (LLog.PRINT_LOG) LLog.i("-----onViewPositionChanged   left:" + left + "   dx:" + dx);
				if (mPreviousActivity == null) return;
				scrollTo(-left, 0);
				final int width = getWidth();
				mScrollPercent = (float) left / width;
				if (left >= width) {
					mActivity.finish();
					mActivity.overridePendingTransition(0, 0);
				}
			}

			@Override
			public void onViewReleased(View releasedChild, float xvel, float yvel) {
				if (mPreviousActivity == null) return;
				int finalX = -getScrollX() + mDragger.getFlingDistanceX(xvel, yvel);
				if (LLog.PRINT_LOG)
					LLog.i("-----onViewReleased   xvel:" + xvel + "  yvel:" + yvel + "finalX:" + finalX);

				if (finalX > getWidth() * 0.8f || (-getScrollX() > getWidth() / 2 && xvel > -200)) {
					finalX = getWidth();
				} else {
					finalX = 0;
				}

				//if (xvel > 4000 || (-getScrollX() > getWidth()/2 && xvel > -100)) {
				//    finalX = getWidth();
				//} else {
				//    finalX = 0;
				//
				//}
				if (mDragger.settleCapturedViewAt(finalX, 0)) {
					ViewCompat.postInvalidateOnAnimation(SlideFinishFrameLayout.this);
				}
			}
		});
	}

	public void enable() {
		mEnable = true;
	}

	public void disable() {
		mEnable = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mShadowDrawable.setBounds(0, 0, mShadowWidth, h);
		mScrimeMaxDistance = (int) (w * mSlidePercent);
	}

	@Override
	public void computeScroll() {
		if (mDragger == null) return;
		if (LLog.PRINT_LOG) LLog.i("computeScroll");
		if (mDragger.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean intercept;
		if (mEnable && mDragger != null) {
			intercept = mDragger.shouldInterceptTouchEvent(ev);
		} else {
			intercept = super.onInterceptTouchEvent(ev);
		}
		return intercept;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mEnable && mDragger != null) {
			mDragger.processTouchEvent(event);
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		//long time = System.nanoTime();
		super.dispatchDraw(canvas);
		drawPrePage(canvas);
		drawEdgeShadow(canvas);
		//if (LLog.PRINT_LOG) LLog.i("liftcircly----dispatchDraw:"+(System.nanoTime()-time)/1000);
	}

	private void drawPrePage(Canvas canvas) {
		if (TYPE == TYPE_TRANSLATE) {
			drawPrePageTranslate(canvas);
		} else if (TYPE == TYPE_SCALE) {
			drawPrePageScale(canvas);
		}
	}

	/**
	 * 微信效果
	 *
	 * @param canvas
	 */
	private void drawPrePageTranslate(Canvas canvas) {
		final int scrollX = getScrollX();
		final int height = getChildAt(0).getHeight();
		if (scrollX != 0 && mPreviousActivity != null) {
			canvas.save();
			canvas.clipRect(scrollX, 0, 0, height);//优化过度绘制,只画可见部分
			canvas.translate(scrollX - mScrimeMaxDistance * (1 - mScrollPercent), 0);
			mPreviousActivity.getWindow().getDecorView().draw(canvas);
			canvas.restore();
		}
	}

	/**
	 * 今日头条效果
	 *
	 * @param canvas
	 */
	private void drawPrePageScale(Canvas canvas) {
		final int scrollX = getScrollX();
		final int width = getWidth();
		final int height = getChildAt(0).getHeight();
		if (scrollX != 0 && mPreviousActivity != null) {
			final float scale = mScale + (1 - mScale) * mScrollPercent;

			final float clipLeft = (width - width * scale) / 2 + scrollX;
			final float clipTop = (height - height * scale) / 2;
			final float clipRight = 0;
			final float clipBottom = height - clipTop;

			if (clipLeft < 0) {//绘制上一个activity
				canvas.save();
				canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
				canvas.translate(clipLeft, clipTop);
				canvas.scale(scale, scale);
				mPreviousActivity.getWindow().getDecorView().draw(canvas);
				canvas.restore();
			}

			canvas.save();
			canvas.drawRect(scrollX, 0, clipLeft > 0 ? 0 : clipLeft, height, mPaint);
			if (clipLeft < 0) {
				canvas.drawRect(clipLeft, 0, 0, clipTop, mPaint);
				canvas.drawRect(clipLeft, clipBottom, 0, height, mPaint);
			}
			canvas.restore();
		}
	}

	private void drawEdgeShadow(Canvas canvas) {
		if (getScrollX() != 0) {
			canvas.save();
			canvas.translate(-mShadowWidth, 0);
			mShadowDrawable.setAlpha((int) (255 * (1 - mScrollPercent)));
			mShadowDrawable.draw(canvas);
			canvas.restore();
		}
	}

}