package dhl.anddemo.clipregion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;

/**
 * Created by DuanHl on 2017/5/16.
 */

public class ClipRegionActivity extends BaseActivity {

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
		String text = "演示clipPath(Path path, Region.Op op)第二个参数的含义\nOP.UNION";
		mTVOP.setText(text);
		FrameLayout fl = (FrameLayout) findViewById(R.id.view_clip);
		mClipView = new ClipRegionView(this);
		mClipView.setOP(Region.Op.UNION);
		fl.addView(mClipView);
	}

	public void onClick(View view) {
		Button btn = (Button) view;
		String text = btn.getText().toString();
		text = "演示clipPath(Path path, Region.Op op)第二个参数的含义\n" + text;
		mTVOP.setText(text);
		Region.Op op = null;
		if (text.contains(".DIFFERENCE")) {
			op = Region.Op.DIFFERENCE;
		} else if (text.contains("REVERSE_DIFFERENCE")) {
			op = Region.Op.REVERSE_DIFFERENCE;
		} else if (text.contains("REPLACE")) {
			op = Region.Op.REPLACE;
		} else if (text.contains("INTERSECT")) {
			op = Region.Op.INTERSECT;
		} else if (text.contains("UNION")) {
			op = Region.Op.UNION;
		} else if (text.contains("XOR")) {
			op = Region.Op.XOR;
		}
		mClipView.setOP(op);
		mClipView.invalidate();
	}

	private static class ClipRegionView extends View {

		private Paint mPaint;
		private Rect mRectOne = new Rect();
		private int mRadius;
		private int mCenterX, mCenterY;
		private Region.Op mOP;
		private Path mPath;

		public ClipRegionView(Context context) {
			super(context);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStrokeWidth(6);
			mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.titlebar_title_textsize));
			mPath = new Path();
		}

		public void setOP(Region.Op op) {
			mOP = op;
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int rectWidt = w / 4;
			mRectOne.set(rectWidt, h / 2 - rectWidt, w - rectWidt, h / 2 + rectWidt);
			mRadius = w / 4;
			mCenterX = w - mRadius;
			mCenterY = h - mRadius;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.save();
			canvas.clipRect(mRectOne);
			mPath.reset();
			mPath.addCircle(mCenterX, mCenterY, mRadius, Path.Direction.CCW);
			canvas.clipPath(mPath, mOP);
			drawRect(canvas);
			drawCircle(canvas);
			canvas.restore();
		}

		private void drawRect(Canvas canvas) {
			mPaint.setColor(Color.RED);
			canvas.drawRect(mRectOne, mPaint);
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
