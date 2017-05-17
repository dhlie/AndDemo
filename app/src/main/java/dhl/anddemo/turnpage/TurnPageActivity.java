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
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.LLog;
import dhl.anddemo.base.util.PixelUtil;

/**
 * Created by DuanHl on 2017/5/16.
 * 参考文章:http://blog.csdn.net/hmg25/article/details/6306479
 */

public class TurnPageActivity extends BaseActivity {

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

        private static final int STATE_NONE                 = 0;
        private static final int STATE_TURNING_FROM_RT      = 1;//右上角翻页
        private static final int STATE_TURNING_FROM_RB      = 2;//右下角翻页
        private static final int STATE_TURNING_FROM_RM      = 3;//右边中间翻页
        private static final int STATE_TURNING_FROM_LT      = 4;//左上角翻页
        private static final int STATE_TURNING_FROM_LB      = 5;//左下角翻页
        private static final int STATE_TURNING_FROM_LM      = 6;//左边中间翻页

        private Bitmap mBPCurr;
        private Bitmap mBPNext;
        private Matrix mMatrix;
        private Matrix mMatrixCorner;

        private int mTouchSlop;
        private int mState = STATE_NONE;
        private float mDownX, mDownY;
        private int mWidth, mHeight;

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private Path mPath1 = new Path();
        private Path mPath2 = new Path();
        private float mAngle;//翻起的也叫水平转动的角度
        private BitmapShader mShaderCorner;
        private int mShadowWidthCurr = PixelUtil.dp2px(8);//当前页上的阴影宽度
        private int mShadowWidthNext;//下一页的阴影宽度
        private GradientDrawable mDrawableCurr;
        private GradientDrawable mDrawableNext;
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

        public PageView(Context context) {
            super(context);

            mBPCurr = BitmapFactory.decodeResource(getResources(), R.drawable.panda);
            mBPNext = BitmapFactory.decodeResource(getResources(), R.drawable.panda3);
            mMatrix = new Matrix();
            mMatrixCorner = new Matrix();
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

            mShaderCorner = new BitmapShader(mBPCurr, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
            int colorStart = Color.parseColor("#00000000");
            int colorEnd = Color.parseColor("#7F000000");
            mDrawableCurr = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorStart, colorEnd});
            mDrawableNext = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{colorEnd, colorStart});
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mWidth = w;
            mHeight = h;
            float s1 = (float) w / mBPCurr.getWidth();
            float s2 = (float) h / mBPCurr.getHeight();
            mMatrix.setScale(s1, s2);
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
            canvas.drawBitmap(mBPNext, mMatrix, null);
            canvas.restore();

            //绘制阴影
            mDrawableNext.setBounds((int)mPointH.x, (int)mPointH.y, (int)(mPointH.x+mShadowWidthNext), (int)(mPointH.y+Math.hypot(mPointB.x-mPointH.x, mPointB.y-mPointH.y)));
            float angle = (float) Math.atan((mWidth-mPointB.x)/(mHeight-mPointH.y));
            angle = (float) (180/Math.PI*angle);
            canvas.save();
            canvas.clipPath(mPath2);
            canvas.rotate(angle, mPointH.x, mPointH.y);
            mDrawableNext.draw(canvas);
            canvas.restore();
        }

        private void drawCorner(Canvas canvas) {
            //不用Shader会有两个小空白区域
            boolean shader = true;
            if (shader) {
                canvas.save();
                canvas.clipPath(mPath1);
                canvas.clipPath(mPath2, Region.Op.DIFFERENCE);
                mMatrixCorner.reset();
                mMatrixCorner.setScale((float) mWidth / mBPCurr.getWidth(), (float) mHeight / mBPCurr.getHeight());
                mMatrixCorner.postScale(-1, 1);
                mMatrixCorner.postTranslate(mWidth, 0);
                mMatrixCorner.postTranslate(mPointA.x, mPointA.y-mHeight);
                mMatrixCorner.postRotate(mAngle, mPointA.x, mPointA.y);
                mShaderCorner.setLocalMatrix(mMatrixCorner);
                mPaint.setAlpha(127);
                mPaint.setShader(mShaderCorner);
                canvas.drawPaint(mPaint);
                canvas.restore();
            } else {
                canvas.save();
                canvas.clipPath(mPath1);
                canvas.clipPath(mPath2, Region.Op.DIFFERENCE);
                mMatrixCorner.reset();
                mMatrixCorner.setScale((float) mWidth / mBPCurr.getWidth(), (float) mHeight / mBPCurr.getHeight());
                mMatrixCorner.postScale(-1, 1);
                mMatrixCorner.postTranslate(mWidth, 0);
                mMatrixCorner.postTranslate(mPointA.x, mPointA.y-mHeight);
                mMatrixCorner.postRotate(mAngle, mPointA.x, mPointA.y);
                mPaint.setAlpha(127);
                canvas.drawBitmap(mBPCurr, mMatrixCorner, mPaint);
                canvas.restore();
            }
        }

        private void drawCurrPage(Canvas canvas) {
            canvas.save();
            canvas.clipPath(mPath1, Region.Op.DIFFERENCE);
            canvas.drawBitmap(mBPCurr, mMatrix, null);
            canvas.restore();

            //绘制阴影
            //mDrawableCurr.setBounds((int)mPointH.x, (int)mPointH.y, (int)(mPointH.x+mShadowWidthNext), (int)(mPointH.y+Math.hypot(mPointB.x-mPointH.x, mPointB.y-mPointH.y)));
            //float angle = (float) Math.atan((mWidth-mPointB.x)/(mHeight-mPointH.y));
            //angle = (float) (180/Math.PI*angle);
            //canvas.save();
            //canvas.clipPath(mPath2);
            //canvas.rotate(angle, mPointH.x, mPointH.y);
            //mDrawableNext.draw(canvas);
            //canvas.restore();
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
                        if (x < getWidth()/2) {
                            if (y < getHeight()/4) {
                                mState = STATE_TURNING_FROM_LT;
                            } else if (y > getHeight() - getHeight()/4) {
                                mState = STATE_TURNING_FROM_LB;
                            } else {
                                mState = STATE_TURNING_FROM_LM;
                            }
                        } else {
                            if (y < getHeight()/4) {
                                mState = STATE_TURNING_FROM_RT;
                            } else if (y > getHeight() - getHeight()/4) {
                                mState = STATE_TURNING_FROM_RB;
                            } else {
                                mState = STATE_TURNING_FROM_RM;
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
            float maxYFromTop = getWidth()*3f/4;//从上边翻页时y的最大值
            float minYFromBottom = getHeight() - maxYFromTop;//从下边翻页时的y的最小值
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
                    //x = motionX;
                    //y = motionY < minYFromBottom ? minYFromBottom : motionY;
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
                    x = motionX;
                    y = motionY < minYFromBottom ? minYFromBottom : motionY;
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

            float ex = (mPointA.x + mWidth)/2;
            float ey = (mPointA.y + mHeight)/2;
            mPointC.x = ex - (mHeight-ey)/(mWidth-ex)*(mHeight-ey);
            mPointC.y = mHeight;
            mPointG.x = mWidth;
            mPointG.y = ey - (mWidth-ex)/(mHeight-ey)* (mWidth-ex);
            float fx = (mPointA.x + ex)/2;
            float fy = (mPointA.y + ey)/2;
            mPointB.x = fx - (mHeight-fy)/(mWidth-fx)* (mHeight-fy);
            mPointB.y = mHeight;
            mPointH.x = mWidth;
            mPointH.y = fy-(mWidth-fx)/(mHeight-fy)*(mWidth-fx);
            calculateIntersection(mPointA.x, mPointA.y, mPointC.x, mPointC.y, mPointB.x, mPointB.y, mPointH.x, mPointH.y, mPointM);
            calculateIntersection(mPointA.x, mPointA.y, mPointG.x, mPointG.y, mPointB.x, mPointB.y, mPointH.x, mPointH.y, mPointN);
            mPointO.x = ((mPointB.x+mPointM.x)/2+mPointC.x)/2;
            mPointO.y = ((mPointB.y+mPointM.y)/2+mPointC.y)/2;
            mPointP.x = ((mPointN.x+mPointH.x)/2+mPointG.x)/2;
            mPointP.y = ((mPointN.y+mPointH.y)/2+mPointG.y)/2;
            //==========计算各顶点坐标==============

            //==========计算角旋转角度==============
            //tan i = (h-ay)/(cx-ax)
            mAngle = (float) Math.atan((mHeight-mPointA.y)/(mPointC.x-mPointA.x));//弧度
            if (mAngle > 0) {
                mAngle = (float) (180/Math.PI*mAngle);//角度
            } else {
                mAngle = (float) (180/Math.PI*mAngle);//角度
                mAngle = 90 + (90+mAngle);
            }
            if (LLog.PRINT_LOG) LLog.i("mAngle  弧度:"+(float) Math.atan((mHeight-mPointA.y)/(mPointC.x-mPointA.x)) +"   角度:"+(float) (180/Math.PI*((float) Math.atan((mHeight-mPointA.y)/(mPointC.x-mPointA.x))))+ "    :"+mAngle);
            //==========计算角旋转角度==============

            //==========计算角path==============
            //make path
            mPath1.reset();
            mPath1.moveTo(mPointA.x, mPointA.y);
            mPath1.lineTo(mPointM.x, mPointM.y);
            mPath1.quadTo(mPointC.x, mPointC.y, mPointB.x, mPointB.y);
            mPath1.lineTo(mWidth, mHeight);
            mPath1.lineTo(mPointH.x, mPointH.y);
            mPath1.quadTo(mPointG.x, mPointG.y, mPointN.x, mPointN.y);
            mPath1.close();

            //make path
            mPath2.reset();
            mPath2.moveTo(mPointB.x, mPointB.y);
            mPath2.lineTo(mPointO.x, mPointO.y);
            mPath2.lineTo(mPointP.x, mPointP.y);
            mPath2.lineTo(mPointH.x, mPointH.y);
            mPath2.lineTo(mWidth, mHeight);
            mPath2.close();
            //==========计算角path==============

            //==========计算下一页阴影宽度==============
            mShadowWidthNext = (int) (Math.hypot(mWidth-mPointA.x, mHeight-mPointA.y)/4);
            //==========计算下一页阴影宽度==============
        }

        /**
         * 根据四个点求两直线的交点
         * @param x1    第一条直线上的点
         * @param y1    第一条直线上的点
         * @param x2    第一条直线上的点
         * @param y2    第一条直线上的点
         * @param x3    第二条直线上的点
         * @param y3    第二条直线上的点
         * @param x4    第二条直线上的点
         * @param y4    第二条直线上的点
         * @param result
         */
        private void calculateIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, PointF result) {
            //y = ax + b
            //y1 = a*x1 + b
            //y2 = a*x2 + b
            //y1-y2 = a(x1-x2)
            //a = (y1-y2)/(x1-x2)
            //b = y1-a*x1
            //y = (y1-y2)/(x1-x2) * x +


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

            float a = (y1-y2)/(x1-x2);
            float b = y1 - a*x1;
            float m = (y3-y4)/(x3-x4);
            float n = y3-m*x3;
            result.x = (n-b)/(a-m);
            result.y = a*result.x + b;
        }
    }
}
