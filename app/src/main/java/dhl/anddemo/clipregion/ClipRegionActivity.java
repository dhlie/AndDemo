package dhl.anddemo.clipregion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.BuBaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.PixelUtil;

/**
 * Created by DuanHl on 2017/5/16.
 */

public class ClipRegionActivity extends BuBaseActivity {

	private TextView mTVOP;
	private ClipRegionView mClipView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clipregion_activity);

		TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
			@Override
			public void onLeftClick(View v) {
				finish();
			}
		};
		TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.setTitleBarClickListener(titleBarClickListener);
		titleBar.setTitle(getClass().getSimpleName());

		mTVOP = (TextView) findViewById(R.id.tv_op);
		PorterDuff.Mode mode = PorterDuff.Mode.SRC_OVER;
		String text = mode.toString();
		mTVOP.setText(text);
		FrameLayout fl = (FrameLayout) findViewById(R.id.view_clip);
		mClipView = new ClipRegionView(this);
		mClipView.setMode(mode);
		fl.addView(mClipView);
	}

	public void onClick(View view) {
		Button btn = (Button) view;
		String text = btn.getText().toString();
		PorterDuff.Mode mode = PorterDuff.Mode.valueOf(text);
		mClipView.setMode(mode);
		mClipView.invalidate();
		mTVOP.setText(text);
	}

	private static class ClipRegionView extends View {

		private Paint mPaint;
		private Rect mRectOne = new Rect();
		private int mRadius;
		private int mCenterX, mCenterY;
		private PorterDuff.Mode mMode;
		private PorterDuffXfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

		public ClipRegionView(Context context) {
			super(context);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStrokeWidth(6);
			mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.titlebar_title_textsize));
		}

		public void setMode(PorterDuff.Mode mode) {
			mMode = mode;
			mXfermode = new PorterDuffXfermode(mMode);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int rectWidt = w / 4;
			mRectOne.set(rectWidt, h / 2 - rectWidt, w - rectWidt, h / 2 + rectWidt);
			mRadius = w / 6;
			mCenterX = w - mRadius;
			mCenterY = h - mRadius;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//border
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.GRAY);
			mPaint.setStrokeWidth(20);
			canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);

			//border
			mPaint.setStrokeWidth(6);
			canvas.drawRect(mRectOne, mPaint);
			canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);

			/**
			 * 使用 PorterDuff.Mode 注意事项:https://www.jianshu.com/p/2a967143f894
			 * 1. 图层混合模式仅作用于src源图像
			 * 2. 禁用硬件加速
			 * 	  原因: 在Android api 14之后,图层混合的有些api是不支持硬件加速的,系统的硬件加速是默认开启的,所以在使用图层混合模式之前,禁用掉硬件加速
			 * 	  <code>
			 * 	      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			 * 	  </code>
			 * 3. 离屏绘制
			 * 	  原因: 在不采用离屏绘制的情况下,控件的背景会影响图层混合模式的计算结果,导致得到期望之外的效果
			 * 	  通过使用离屏绘制(离屏缓冲),把要绘制的内容单独绘制在缓冲层,保证Xfermode的使用不会出现错误的结果
			 * 	  离屏绘制有两种使用方式,一般使用第一种方式就足够了:
			 * 	  1. canvas.saveLayer(RectF, Paint)
			 * 	  	<code>
			 * 	  	   int saveId= canvas.saveLayer(0, 0, width, height, Canvas.ALL_SAVE_FLAG);
			 * 	  	   canvas.translate(x, y);
			 *         canvas.drawBitmap(mDstB, 0, 0, paint);//绘制操作
			 *         paint.setXfermode(xfermode);//设置xfermode
			 *         canvas.drawBitmap(mSrcB, 0, 0, paint);//绘制操作
			 *         paint.setXfermode(null); //用完清除
			 *         canvas.restoreToCount(saveId);//图层恢复
			 * 	  	</code>
			 * 	  2. View.setLayerType() 直接把整个View都绘制在离屏缓冲中
			 * 	  	<code>
			 * 	  	   setLayerType(LAYER_TYPE_HARDWARE,paint);//使用GPU缓冲
			 * 		   setLayerType(LAYER_TYPE_SOFTWARE,paint);//使用一个Bitmap缓冲
			 * 	  	</code>
			 *
			 */
			mPaint.setStyle(Paint.Style.FILL);
            int saveId = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                saveId = canvas.saveLayer(0, 0, getWidth(), getHeight(), mPaint);
            }
			//draw dst
			drawRect(canvas);
			drawText(canvas);
			mPaint.setXfermode(mXfermode);
			//draw src
			drawCircle(canvas);
			mPaint.setXfermode(null);
			canvas.restoreToCount(saveId);//图层恢复
		}

		private void drawRect(Canvas canvas) {
			mPaint.setColor(Color.RED);
			canvas.drawRect(mRectOne, mPaint);
		}

		private void drawText(Canvas canvas) {
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(PixelUtil.sp2px(40));
			String text = "hello world test";
			float textWidth = mPaint.measureText(text);
			canvas.drawText(text, (getWidth() - textWidth) / 2, mRectOne.bottom + 150, mPaint);
		}

		private void drawCircle(Canvas canvas) {
			mPaint.setColor(Color.BLUE);
			canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					getParent().requestDisallowInterceptTouchEvent(true);
					mCenterX = (int) event.getX();
					mCenterY = (int) event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					mCenterX = (int) event.getX();
					mCenterY = (int) event.getY();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mCenterX = (int) event.getX();
					mCenterY = (int) event.getY();
					break;
			}
			invalidate();
			return true;
		}
	}
}
