package dhl.anddemo.turnpage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import dhl.anddemo.R;
import dhl.anddemo.base.BuBaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.LLog;
import dhl.anddemo.base.util.PixelUtil;

/**
 * Created by DuanHl on 2017/5/16.
 * 参考文章:http://blog.csdn.net/hmg25/article/details/6306479
 */

public class TurnPageActivity extends BuBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.turnpage_activity);

		TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
			@Override
			public void onLeftClick(View v) {
				finish();
			}
		};
		TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.setTitleBarClickListener(titleBarClickListener);
		titleBar.setTitle(getClass().getSimpleName());

		FrameLayout fl = (FrameLayout) findViewById(R.id.fl_content);
		fl.addView(new PageView(this));
	}

	private static class PageView extends View {

		private static final int STATE_NONE = 0;
		private static final int STATE_TURNING_FROM_RT = 1;//右上角翻页
		private static final int STATE_TURNING_FROM_RB = 2;//右下角翻页
		private static final int STATE_TURNING_FROM_RM = 3;//右边中间翻页
		private static final int STATE_TURNING_FROM_LT = 4;//左上角翻页
		private static final int STATE_TURNING_FROM_LB = 5;//左下角翻页
		private static final int STATE_TURNING_FROM_LM = 6;//左边中间翻页

		private Bitmap mBPCurr;
		private Bitmap mBPNext;
		private Matrix mMatrixCurr;
		private Matrix mMatrixNext;
		private Matrix mMatrixCorner;

		private int mTouchSlop;
		private int mState = STATE_NONE;
		private float mDownX, mDownY;
		private int mWidth, mHeight;
		private int mCornerX, mCornerY;//翻页角的原始坐标(左上右下四个坐标之一)

		private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		private Path mPath1 = new Path();//翻起的书角加上下一页可见部分
		private Path mPath2 = new Path();//下一页可见部分
		private float mAngle;//翻起的书角水平转动的角度
		private BitmapShader mShaderCorner;
		private int mShadowWidthCurr = PixelUtil.dip2px(10);//当前页上的阴影宽度
		private int mShadowWidthNext;//下一页的阴影宽度
		private GradientDrawable mShadowDrawableH;//水平边的阴影
		private GradientDrawable mShadowDrawableV;//垂直边的阴影
		private GradientDrawable mShadowDrawableC;//角旁边正方形的阴影
		private GradientDrawable mShadowDrawableNext;//下一页上的阴影
		private float mAngleShadowNext;//下一页阴影绘制时的旋转角度
		private float mAngleShadowH;//水平边阴影绘制时的旋转角度
		private float mAngleShadowV;//垂直边阴影绘制时的旋转角度
		private float mAngleShadowC;//角旁边正方形的阴影绘制时的旋转角度
		//绘制过程需要的顶点坐标
		private PointF mPointA = new PointF();
		private PointF mPointB = new PointF();
		private PointF mPointC = new PointF();
		private PointF mPointM = new PointF();
		private PointF mPointO = new PointF();
		private PointF mPointN = new PointF();
		private PointF mPointH = new PointF();
		private PointF mPointG = new PointF();
		private PointF mPointP = new PointF();

		private PointF mPointQ = new PointF();//角阴影顶点

		public PageView(Context context) {
			super(context);

			mBPCurr = BitmapFactory.decodeResource(getResources(), R.drawable.page_right_down);
			mBPNext = BitmapFactory.decodeResource(getResources(), R.drawable.page_left_down);
			mMatrixCurr = new Matrix();
			mMatrixNext = new Matrix();
			mMatrixCorner = new Matrix();
			mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

			mShaderCorner = new BitmapShader(mBPCurr, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
			int colorStart = Color.parseColor("#00000000");
			int colorEnd = Color.parseColor("#7F000000");
			int colorEnd2 = Color.parseColor("#1e000000");
			int colorEnd3 = Color.parseColor("#10000000");
			mShadowDrawableH = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStart, colorEnd2});
			mShadowDrawableV = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{colorStart, colorEnd2});
			mShadowDrawableC = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{colorStart, colorEnd3});
			mShadowDrawableNext = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStart, colorEnd});
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mWidth = w;
			mHeight = h;
			mMatrixCurr.setScale((float) w / mBPCurr.getWidth(), (float) h / mBPCurr.getHeight());
			mMatrixNext.setScale((float) w / mBPNext.getWidth(), (float) h / mBPNext.getHeight());
		}

		@Override
		protected void onDraw(Canvas canvas) {
			//绘制下一页显示区域
			drawNextPage(canvas);
			//绘制翻起的页脚区域
			drawCorner(canvas);
			//绘制当前页显示区域
			drawCurrPage(canvas);
		}

		private void drawNextPage(Canvas canvas) {
			canvas.save();
			canvas.clipPath(mPath2);
			canvas.drawBitmap(mBPNext, mMatrixNext, null);
			canvas.restore();

			//绘制阴影
			canvas.save();
			canvas.clipPath(mPath2);
			canvas.rotate(mAngleShadowNext, mPointH.x, mPointH.y);
			mShadowDrawableNext.draw(canvas);
			canvas.restore();
		}

		private void drawCorner(Canvas canvas) {
			//不用Shader会有两个小空白区域
			boolean shader = true;
			if (shader) {
				canvas.save();
				canvas.clipPath(mPath1);
				canvas.clipPath(mPath2, Region.Op.DIFFERENCE);
				mShaderCorner.setLocalMatrix(mMatrixCorner);
				mPaint.setAlpha(127);
				mPaint.setShader(mShaderCorner);
				canvas.drawPaint(mPaint);
				mPaint.setAlpha(255);
				mPaint.setShader(null);
				canvas.restore();
			} else {
				canvas.save();
				canvas.clipPath(mPath1);
				canvas.clipPath(mPath2, Region.Op.DIFFERENCE);
				mPaint.setAlpha(127);
				canvas.drawBitmap(mBPCurr, mMatrixCorner, mPaint);
				mPaint.setAlpha(255);
				canvas.restore();
			}
		}

		private void drawCurrPage(Canvas canvas) {
			canvas.save();
			canvas.clipPath(mPath1, Region.Op.DIFFERENCE);
			canvas.drawBitmap(mBPCurr, mMatrixCurr, null);

			//绘制阴影
			canvas.save();
			canvas.rotate(mAngleShadowH, mPointQ.x, mPointQ.y);
			mShadowDrawableH.draw(canvas);
			canvas.restore();

			canvas.save();
			canvas.rotate(mAngleShadowV, mPointQ.x, mPointQ.y);
			mShadowDrawableV.draw(canvas);
			canvas.restore();

			canvas.save();
			canvas.rotate(mAngleShadowC, mPointQ.x, mPointQ.y);
			mShadowDrawableC.draw(canvas);
			canvas.restore();

			canvas.restore();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mState = STATE_NONE;
					mDownX = event.getX();
					mDownY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					float x = event.getX();
					float y = event.getY();
					if (mState != STATE_NONE) {
						setCornerPoint(x, y);
						break;
					}

					float dx = x - mDownX;
					float dy = y - mDownY;
					if (Math.abs(dx) >= mTouchSlop || Math.abs(dy) >= mTouchSlop) {
						getParent().requestDisallowInterceptTouchEvent(true);
						if (x < getWidth() / 2) {
							if (y < getHeight() / 4) {
								mState = STATE_TURNING_FROM_LT;
								mCornerX = 0;
								mCornerY = 0;
							} else if (y > getHeight() - getHeight() / 4) {
								mState = STATE_TURNING_FROM_LB;
								mCornerX = 0;
								mCornerY = mHeight;
							} else {
								mState = STATE_TURNING_FROM_LM;
								mCornerX = 0;
								mCornerY = mHeight / 2;
							}
						} else {
							if (y < getHeight() / 4) {
								mState = STATE_TURNING_FROM_RT;
								mCornerX = mWidth;
								mCornerY = 0;
							} else if (y > getHeight() - getHeight() / 4) {
								mState = STATE_TURNING_FROM_RB;
								mCornerX = mWidth;
								mCornerY = mHeight;
							} else {
								mState = STATE_TURNING_FROM_RM;
								mCornerX = mWidth;
								mCornerY = mHeight / 2;
							}
						}
						setCornerPoint(x, y);
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mState = STATE_NONE;
					break;
			}
			return true;
		}

		private void setCornerPoint(float motionX, float motionY) {
			float maxYFromTop = mWidth * 3f / 5;//从上边翻页时y的最大值
			float minYFromBottom = mHeight - maxYFromTop;//从下边翻页时的y的最小值
			float x = -1, y = -1;
			switch (mState) {
				case STATE_TURNING_FROM_LT:
					//x = motionX;
					//y = motionY > maxYFromTop ? maxYFromTop : motionY;
					break;
				case STATE_TURNING_FROM_LM:
					//x = motionX;
					//y = motionY;
					break;
				case STATE_TURNING_FROM_LB:
					//计算方法参考page_right_down.png
					x = Math.min(mWidth - 1, Math.max(1, motionX));
					y = Math.min(mHeight - 1, Math.max(minYFromBottom, motionY));
					if (LLog.PRINT_LOG) LLog.i("corner Point:" + x + " - " + y);
					mPointA.set(x, y);
					calculateVertexes();
					invalidate();
					break;
				case STATE_TURNING_FROM_RT:
					//x = motionX;
					//y = motionY > maxYFromTop ? maxYFromTop : motionY;
					break;
				case STATE_TURNING_FROM_RM:
					//x = motionX;
					//y = motionY;
					break;
				case STATE_TURNING_FROM_RB:
					//计算方法参考page_right_down.png
					x = Math.min(mWidth - 1, Math.max(1, motionX));
					y = Math.min(mHeight - 1, Math.max(minYFromBottom, motionY));
					if (LLog.PRINT_LOG) LLog.i("corner Point:" + x + " - " + y);
					mPointA.set(x, y);
					calculateVertexes();
					invalidate();
					break;
			}
		}

		private void calculateVertexes() {
			//==========计算各顶点坐标==============
			//ex = (ax + dx)/2 ey = (ay + dy)/2
			//ix = ex iy = dy
			//cx = ex - (dy - ey)/(dx-ex) * (dy-ey)    ei/id = ci/ei ci = ei/id * ei = (dy - ey)/(dx-ex) * (dy-ey) = (ex - cx)
			//gy = ey - (jx-ex)/(dy-ey) * (jx-ex)      gj/ej = ej/dj  (ey-gy)/(jx-ex) = (jx-ex)/(dy-ey) (ey-gy) = (jx-ex)/(dy-ey) * (jx-ex)
			//fx = (ax + ex)/2 fy = (ay + ey)/2 // TODO: 2017/5/17 可定制
			//bx = fx - (dy-fy)/(dx-fx) * (dy-fy)  (fx-bx)/(dy-fy) = (dy-fy)/(dx-fx) (fx-bx) = (dy-fy)/(dx-fx) * (dy-fy)
			//hy = fy - (dx-fx)/(dy-fy) * (dx-fx) (fy-hy)/(dx-fx) = (dx-fx)/(dy-fy) (fy-hy) = (dx-fx)/(dy-fy) * (dx-fx)
			//mx, my 通过直线交点获取
			//ox = ((bx+mx)/2 + cx)/2 oy = ((by + my)/2 + cy)/2
			//px = ((nx + hx)/2 + gx)/2 py = ((ny+hy)/2 + gy)/2

			float ex = (mPointA.x + mCornerX) / 2;
			float ey = (mPointA.y + mCornerY) / 2;
			mPointC.x = ex - (mCornerY - ey) / (mCornerX - ex) * (mCornerY - ey);
			mPointC.y = mCornerY;
			mPointG.x = mCornerX;
			mPointG.y = ey - (mCornerX - ex) / (mCornerY - ey) * (mCornerX - ex);
			float fx = (mPointA.x + ex) / 2;
			float fy = (mPointA.y + ey) / 2;
			mPointB.x = fx - (mCornerY - fy) / (mCornerX - fx) * (mCornerY - fy);
			mPointB.y = mCornerY;
			mPointH.x = mCornerX;
			mPointH.y = fy - (mCornerX - fx) / (mCornerY - fy) * (mCornerX - fx);
			calculateIntersection(mPointA.x, mPointA.y, mPointC.x, mPointC.y, mPointB.x, mPointB.y, mPointH.x, mPointH.y, mPointM);
			calculateIntersection(mPointA.x, mPointA.y, mPointG.x, mPointG.y, mPointB.x, mPointB.y, mPointH.x, mPointH.y, mPointN);
			mPointO.x = ((mPointB.x + mPointM.x) / 2 + mPointC.x) / 2;
			mPointO.y = ((mPointB.y + mPointM.y) / 2 + mPointC.y) / 2;
			mPointP.x = ((mPointN.x + mPointH.x) / 2 + mPointG.x) / 2;
			mPointP.y = ((mPointN.y + mPointH.y) / 2 + mPointG.y) / 2;
			//==========计算各顶点坐标==============

			//==========计算角旋转角度==============
			//tan i = (h-ay)/(cx-ax)
			mAngle = (float) Math.atan((mHeight - mPointA.y) / (mPointC.x - mPointA.x));//弧度
			if (mAngle > 0) {
				mAngle = (float) (180 / Math.PI * mAngle);//角度
			} else {
				mAngle = (float) (180 / Math.PI * mAngle);//角度
				mAngle = 90 + (90 + mAngle);
			}
			//if (LLog.PRINT_LOG) LLog.i("mAngle  弧度:"+(float) Math.atan((mHeight-mPointA.y)/(mPointC.x-mPointA.x)) +"   角度:"+(float) (180/Math.PI*((float) Math.atan((mHeight-mPointA.y)/(mPointC.x-mPointA.x))))+ "    :"+mAngle);
			//==========计算角旋转角度==============

			//==========计算角path==============
			//make path
			mPath1.reset();
			mPath1.moveTo(mPointA.x, mPointA.y);
			mPath1.lineTo(mPointM.x, mPointM.y);
			mPath1.quadTo(mPointC.x, mPointC.y, mPointB.x, mPointB.y);
			mPath1.lineTo(mCornerX, mCornerY);
			mPath1.lineTo(mPointH.x, mPointH.y);
			mPath1.quadTo(mPointG.x, mPointG.y, mPointN.x, mPointN.y);
			mPath1.close();

			//make path
			mPath2.reset();
			mPath2.moveTo(mPointB.x, mPointB.y);
			mPath2.lineTo(mPointO.x, mPointO.y);
			mPath2.lineTo(mPointP.x, mPointP.y);
			mPath2.lineTo(mPointH.x, mPointH.y);
			mPath2.lineTo(mCornerX, mCornerY);
			mPath2.close();
			//==========计算角path==============

			if (mState == STATE_TURNING_FROM_RB) {
				//==========计算下一页阴影宽度==============
				mShadowWidthNext = (int) (Math.hypot(mCornerX - mPointA.x, mCornerY - mPointA.y) / 4);
				mAngleShadowNext = (float) Math.atan((mCornerX - mPointB.x) / (mCornerY - mPointH.y));
				mAngleShadowNext = (float) (180 / Math.PI * mAngleShadowNext);
				mShadowDrawableNext.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
				mShadowDrawableNext.setBounds((int) mPointH.x, (int) mPointH.y, (int) (mPointH.x + mShadowWidthNext), (int) (mPointH.y + Math.hypot(mPointB.x - mPointH.x, mPointB.y - mPointH.y)));
				//==========计算下一页阴影宽度==============


				//==========计算当前页阴影==============
				//直线ac:y = ax + b  与ac平行的阴影边:y = ax + c
				//直线ag:y = mx + n  与ag平行的阴影边:y = mx + p
				float a = (mPointA.y - mPointC.y) / (mPointA.x - mPointC.x);
				float b = mPointA.y - a * mPointA.x;
				float m = (mPointA.y - mPointG.y) / (mPointA.x - mPointG.x);
				float n = mPointA.y - m * mPointA.x;

				//y = ax + b  ax - y + b = 0
				//y = ax + c  ax - y + c = 0
				//mShadowWidthCurr = Math.abs(b-c)/Math.sqrt(a*a + 1*1);
				//Math.abs(b-c) = mShadowWidthCurr*Math.sqrt(a*a + 1*1)
				float c = 0f;
				float p = 0f;
				if (a > 0) {
					c = (float) (b + mShadowWidthCurr * Math.sqrt(a * a + 1));
				} else {
					c = (float) (b - mShadowWidthCurr * Math.sqrt(a * a + 1));
				}
				p = (float) (n - mShadowWidthCurr * Math.sqrt(m * m + 1));

				//ax + c = mx + p
				mPointQ.x = (p - c) / (a - m);
				mPointQ.y = a * mPointQ.x + c;

				mShadowDrawableH.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
				mShadowDrawableH.setBounds((int) mPointQ.x, (int) mPointQ.y + mShadowWidthCurr, (int) (mPointQ.x + mShadowWidthCurr), (int) mPointQ.y + mWidth + mShadowWidthCurr);
				mShadowDrawableV.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
				mShadowDrawableV.setBounds((int) (mPointQ.x - mShadowWidthCurr), (int) mPointQ.y + mShadowWidthCurr, (int) (mPointQ.x), (int) mPointQ.y + mHeight + mShadowWidthCurr);
				mShadowDrawableC.setBounds((int) mPointQ.x, (int) mPointQ.y, (int) (mPointQ.x + mShadowWidthCurr), (int) mPointQ.y + mShadowWidthCurr);
				mAngleShadowH = mAngle - 90;
				mAngleShadowV = mAngle + 180;
				mAngleShadowC = mAngle - 90;
				//if (LLog.PRINT_LOG) LLog.i("y = ax + b   a-b "+a+" - "+b);
				//if (LLog.PRINT_LOG) LLog.i("y = mx + n   m-n "+m+" - "+n);

				//辅助线
				//if (canvas != null) {
				//    mPaint.setStrokeWidth(4);
				//    mPaint.setColor(Color.RED);
				//    canvas.drawLine(0, p, -p/m, 0, mPaint);
				//    canvas.drawLine(0, p, (mHeight-p)/m, mHeight, mPaint);
				//    mPaint.setColor(Color.BLUE);
				//    canvas.drawLine(0, c, -c/a, 0, mPaint);
				//    canvas.drawLine(0, c, (mHeight-c)/a, mHeight, mPaint);
				//}
				//==========计算当前页阴影==============

				mMatrixCorner.reset();
				mMatrixCorner.setScale((float) mWidth / mBPCurr.getWidth(), (float) mHeight / mBPCurr.getHeight());
				mMatrixCorner.postScale(-1, 1);
				mMatrixCorner.postTranslate(mWidth, 0);
				mMatrixCorner.postTranslate(mPointA.x, mPointA.y - mHeight);
				mMatrixCorner.postRotate(mAngle, mPointA.x, mPointA.y);
			} else if (mState == STATE_TURNING_FROM_LB) {
				//==========计算下一页阴影宽度==============
				mShadowWidthNext = (int) (Math.hypot(mCornerX - mPointA.x, mCornerY - mPointA.y) / 4);
				mAngleShadowNext = (float) Math.atan(mPointB.x / (mCornerY - mPointH.y));
				mAngleShadowNext = (float) -(180 / Math.PI * mAngleShadowNext);
				mShadowDrawableNext.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
				mShadowDrawableNext.setBounds((int) (mPointH.x - mShadowWidthNext), (int) mPointH.y, (int) mPointH.x, (int) (mPointH.y + Math.hypot(mPointB.x - mPointH.x, mPointB.y - mPointH.y)));
				//==========计算下一页阴影宽度==============


				//==========计算当前页阴影==============
				//直线ac:y = ax + b  与ac平行的阴影边:y = ax + c
				//直线ag:y = mx + n  与ag平行的阴影边:y = mx + p
				float a = (mPointA.y - mPointC.y) / (mPointA.x - mPointC.x);
				float b = mPointA.y - a * mPointA.x;
				float m = (mPointA.y - mPointG.y) / (mPointA.x - mPointG.x);
				float n = mPointA.y - m * mPointA.x;

				//y = ax + b  ax - y + b = 0
				//y = ax + c  ax - y + c = 0
				//mShadowWidthCurr = Math.abs(b-c)/Math.sqrt(a*a + 1*1);
				//Math.abs(b-c) = mShadowWidthCurr*Math.sqrt(a*a + 1*1)
				float c = 0f;
				float p = 0f;
				if (a < 0) {
					c = (float) (b + mShadowWidthCurr * Math.sqrt(a * a + 1));
				} else {
					c = (float) (b - mShadowWidthCurr * Math.sqrt(a * a + 1));
				}
				p = (float) (n - mShadowWidthCurr * Math.sqrt(m * m + 1));

				//ax + c = mx + p
				mPointQ.x = (p - c) / (a - m);
				mPointQ.y = a * mPointQ.x + c;

				mShadowDrawableH.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
				mShadowDrawableH.setBounds((int) mPointQ.x - mShadowWidthCurr, (int) mPointQ.y + mShadowWidthCurr, (int) mPointQ.x, (int) mPointQ.y + mWidth + mShadowWidthCurr);
				mShadowDrawableV.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
				mShadowDrawableV.setBounds((int) mPointQ.x, (int) mPointQ.y + mShadowWidthCurr, (int) (mPointQ.x + mShadowWidthCurr), (int) mPointQ.y + mHeight + mShadowWidthCurr);
				mShadowDrawableC.setBounds((int) mPointQ.x, (int) mPointQ.y, (int) (mPointQ.x + mShadowWidthCurr), (int) mPointQ.y + mShadowWidthCurr);
				mAngleShadowH = mAngle + 270;
				mAngleShadowV = mAngle;
				mAngleShadowC = mAngle;
				//if (LLog.PRINT_LOG) LLog.i("y = ax + b   a-b "+a+" - "+b);
				//if (LLog.PRINT_LOG) LLog.i("y = mx + n   m-n "+m+" - "+n);

				//辅助线
				//if (canvas != null) {
				//    mPaint.setStrokeWidth(4);
				//    mPaint.setColor(Color.RED);
				//    canvas.drawLine(0, p, -p/m, 0, mPaint);
				//    canvas.drawLine(0, p, (mHeight-p)/m, mHeight, mPaint);
				//    mPaint.setColor(Color.BLUE);
				//    canvas.drawLine(0, c, -c/a, 0, mPaint);
				//    canvas.drawLine(0, c, (mHeight-c)/a, mHeight, mPaint);
				//}
				//==========计算当前页阴影==============

				mMatrixCorner.reset();
				mMatrixCorner.setScale((float) mWidth / mBPCurr.getWidth(), (float) mHeight / mBPCurr.getHeight());
				mMatrixCorner.postScale(-1, 1);
				mMatrixCorner.postTranslate(mPointA.x, mPointA.y - mHeight);
				mMatrixCorner.postRotate(mAngle + 180, mPointA.x, mPointA.y);
			}
		}

		/**
		 * 根据四个点求两直线的交点
		 *
		 * @param x1     第一条直线上的点
		 * @param y1     第一条直线上的点
		 * @param x2     第一条直线上的点
		 * @param y2     第一条直线上的点
		 * @param x3     第二条直线上的点
		 * @param y3     第二条直线上的点
		 * @param x4     第二条直线上的点
		 * @param y4     第二条直线上的点
		 * @param result 存放交点坐标
		 */
		private void calculateIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, PointF result) {
			//y = ax + b
			//y1 = a*x1 + b
			//y2 = a*x2 + b
			//y1-y2 = a(x1-x2)
			//a = (y1-y2)/(x1-x2)
			//b = y1-a*x1

			//y = mx + n
			//y3 = m*x3 + n
			//y4 = m*x4 + n
			//y3-y4 = m(x3-x4)
			//m = (y3-y4)/(x3-x4)
			//n = y3-m*x3

			//ax + b = mx+n
			//(a-m)x = n-b
			//x = (n-b)/(a-m)
			//y = ax + b

			float a = (y1 - y2) / (x1 - x2);
			float b = y1 - a * x1;
			float m = (y3 - y4) / (x3 - x4);
			float n = y3 - m * x3;
			result.x = (n - b) / (a - m);
			result.y = a * result.x + b;
		}

	}
}
