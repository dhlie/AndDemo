package dhl.anddemo.matrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import dhl.anddemo.R;

/**
 * Created by DuanHl on 2016/3/14.
 */
public class CarWheelView extends View {

	private Matrix mCanvasMatrix;
	private Bitmap mBitmap;
	private Camera mCamera;
	private float mInterpolatedTime = 1.0f;
	private WheelAnimation mCoverAnimation;
	private Matrix mMatrix;
	private int mDegree;

	public CarWheelView(Context context) {
		this(context, null);
	}

	public CarWheelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CarWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gift_common_wheel);
		mCamera = new Camera();
		mCanvasMatrix = new Matrix();
		mMatrix = new Matrix();
		mCoverAnimation = new WheelAnimation();
	}

	public void setDegree(int degree) {
		mDegree = degree;
	}

	public void startAnim() {
		startAnimation(mCoverAnimation);
		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		final float width = getWidth();
		final float height = getHeight();
		mCamera.save();
		mCamera.rotateY(mDegree);
		mCamera.getMatrix(mCanvasMatrix);
		mCamera.restore();
		//以View的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心
		mCanvasMatrix.preTranslate(-width / 2, -height / 2);
		mCanvasMatrix.postTranslate(width / 2, height / 2);

		mMatrix.setScale(width / mBitmap.getWidth(), height / mBitmap.getHeight());
		mMatrix.postRotate(360f * mInterpolatedTime, width / 2, height / 2);

		canvas.save();
		canvas.concat(mCanvasMatrix);
		canvas.drawBitmap(mBitmap, mMatrix, null);
		canvas.restore();
	}

	private class WheelAnimation extends Animation {
		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			setDuration(800);
			setInterpolator(new LinearInterpolator());
			setRepeatCount(Integer.MAX_VALUE);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			mInterpolatedTime = interpolatedTime;
			invalidate();
		}
	}

}
