package dhl.anddemo.matrix;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Toast;

import dhl.anddemo.R;

/**
 * Created by coolyou on 2016/3/14.
 */
public class CarView extends View {

    private final boolean   DEBUG       = true;

    private Paint           mPaint;
    private Camera          mCamera;
    private Bitmap          mCarBody;       //车身图片
    private Bitmap          mLeftLight;     //左灯
    private Bitmap          mRightLight;    //右灯
    private Bitmap          mTailLight;     //尾灯
    private Bitmap          mWheel;         //车轮
    private float           mScale;         //图片缩放比例
    private int             mCarLeft;       //车身图片左顶点x坐标
    private int             mCarTop;        //车身图片左顶点y坐标
    private Matrix          mCarMatrix;
    private float           mCarWidth;
    private float           mCarHeight;
    private int             mDegreeFrontWheel;
    private int             mDegreeBackWheel;
    private float           mDegreeTurnWheel;
    private float           mInterpolatedTime;
    private boolean         mAnimIsRunning;
    private long            mStopTime;

    public CarView(Context context) {
        this(context, null);
    }

    public CarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(21)
    public CarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint      = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mCamera     = new Camera();
        mCarBody    = BitmapFactory.decodeResource(getResources(), R.drawable.car_two);
        mLeftLight  = BitmapFactory.decodeResource(getResources(), R.drawable.car_two_left_light);
        mRightLight = BitmapFactory.decodeResource(getResources(), R.drawable.car_two_right_light);
        mTailLight  = BitmapFactory.decodeResource(getResources(), R.drawable.car_two_vent_pipe);
        mWheel      = BitmapFactory.decodeResource(getResources(), R.drawable.gift_common_wheel);
        mCarMatrix  = new Matrix();
        mDegreeFrontWheel   = 290;
        mDegreeBackWheel    = 300;
    }

    @Deprecated
    @SuppressWarnings({""})
    public void startAnim(){
        mStopTime = 0;
        RunAnimation anim = new RunAnimation(getWidth(), getHeight() * 3 / 4, (getWidth() - mCarWidth) / 2, (getHeight() - mCarHeight) / 2, 2000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RunAnimation anim = new RunAnimation((getWidth() - mCarWidth) / 2, (getHeight() - mCarHeight) / 2, -mCarWidth, getHeight() / 4 - mCarHeight, 2000);
                        anim.setInterpolator(new AccelerateInterpolator());
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                startAnim();
                                //Toast.makeText(CarView.this.getContext(), "Anim Over! restart", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        startAnimation(anim);
                    }
                }, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        startAnimation(anim);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCarWidth   = getMeasuredWidth() / 1.5f;
        mCarHeight  = mCarBody.getHeight() * mCarWidth / mCarBody.getWidth();
        mScale      = mCarWidth / mCarBody.getWidth();//图片宽度缩放为view宽度的1/3
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mAnimIsRunning) {
            mAnimIsRunning = true;
            startAnim();
        }
        drawCarBody(canvas);
        drawLeftLight(canvas);
        drawRightLight(canvas);
        drawTailLight(canvas);
        drawFrontWheel(canvas);
        drawBackWheel(canvas);
    }

    /**
     * 画车身
     * @param canvas
     */
    private void drawCarBody(Canvas canvas) {
        mCarMatrix.reset();
        mCarMatrix.postScale(mScale, mScale);
        mCarMatrix.postTranslate(mCarLeft, mCarTop);
        canvas.drawBitmap(mCarBody, mCarMatrix, null);
    }

    /**
     * 画左车灯
     * @param canvas
     */
    private void drawLeftLight(Canvas canvas) {
        if (mInterpolatedTime != 1.0f) return;
        long currTime = SystemClock.uptimeMillis();
        long timePassed = currTime - mStopTime;
        if ((timePassed >= 0 && timePassed <= 125)
                || (timePassed >= 250 && timePassed <= 375)
                || (timePassed >= 500 && timePassed <= 625)
                || (timePassed >= 750 && timePassed <= 875)) return;//每秒闪4下
        mCarMatrix.reset();
        mCarMatrix.postScale(mScale, mScale);
        mCarMatrix.postTranslate(mCarLeft + 260f / 484 * mCarWidth, mCarTop + 93f / 232 * mCarHeight);

        canvas.drawBitmap(mLeftLight, mCarMatrix, mPaint);
    }

    /**
     * 画右车灯
     * @param canvas
     */
    private void drawRightLight(Canvas canvas) {
        if (mInterpolatedTime != 1.0f) return;
        long currTime = SystemClock.uptimeMillis();
        long timePassed = currTime - mStopTime;
        if ((timePassed >= 0 && timePassed <= 125)
                || (timePassed >= 250 && timePassed <= 375)
                || (timePassed >= 500 && timePassed <= 625)
                || (timePassed >= 750 && timePassed <= 875)) return;//每秒闪4下
        mCarMatrix.reset();
        mCarMatrix.postScale(mScale, mScale);
        mCarMatrix.postTranslate(mCarLeft + 430f/484*mCarWidth, mCarTop + 50f/232*mCarHeight);
        canvas.drawBitmap(mRightLight, mCarMatrix, mPaint);
    }

    /**
     * 画尾灯
     * @param canvas
     */
    private void drawTailLight(Canvas canvas) {
        if (mInterpolatedTime == 1.0f) return;
        mCarMatrix.reset();
        mCarMatrix.postScale(mScale, mScale);
        mCarMatrix.postTranslate(mCarLeft + 360f/484*mCarWidth, mCarTop + 118f/232*mCarHeight);
        canvas.drawBitmap(mTailLight, mCarMatrix, mPaint);
    }

    /**
     * 画前轮
     * @param canvas
     */
    private void drawFrontWheel(Canvas canvas) {
        mCamera.save();
        mCamera.rotateY(mDegreeFrontWheel);
        mCamera.getMatrix(mCarMatrix);
        mCamera.restore();
        mCarMatrix.postTranslate(mCarLeft + 15f / 484 * mCarWidth, mCarTop + 47f / 232 * mCarHeight);

        final float wheelHeight = 60f/232*mCarHeight;
        float scale = wheelHeight/mWheel.getHeight();
        mCarMatrix.preRotate(mDegreeTurnWheel, wheelHeight / 2, wheelHeight / 2);
        mCarMatrix.preScale(scale, scale);

        canvas.drawBitmap(mWheel, mCarMatrix, null);
        //canvas.restore();

        /*//该段代码效果一样
        mCamera.save();
        mCamera.rotateY(mDegreeFrontWheel);
        mCamera.getMatrix(mCarMatrix);
        mCamera.restore();
        mCarMatrix.postTranslate(mCarLeft + 15f / 484 * mCarWidth, mCarTop + 47f / 232 * mCarHeight);
        canvas.save();
        canvas.concat(mCarMatrix);

        final float wheelHeight = 60f/232*mCarHeight;
        float scale = wheelHeight/mWheel.getHeight();
        mCarMatrix.setScale(scale, scale);
        mCarMatrix.postRotate(mDegreeTurnWheel, wheelHeight / 2, wheelHeight / 2);

        canvas.drawBitmap(mWheel, mCarMatrix, null);
        canvas.restore();*/
    }

    /**
     * 画后轮
     * @param canvas
     */
    private void drawBackWheel(Canvas canvas) {
        mCamera.save();
        mCamera.rotateY(mDegreeBackWheel);
        mCamera.getMatrix(mCarMatrix);
        mCamera.restore();
        mCarMatrix.postTranslate(mCarLeft + 168f / 484 * mCarWidth, mCarTop + 98f / 232 * mCarHeight);
        final float wheelHeight = 93f / 232 * mCarHeight;
        float scale = wheelHeight/mWheel.getHeight();
        mCarMatrix.preRotate(mDegreeTurnWheel, wheelHeight / 2, wheelHeight /2);
        mCarMatrix.preScale(scale, scale);

        canvas.drawBitmap(mWheel, mCarMatrix, null);

        /*//该段代码效果一样
        mCamera.save();
        mCamera.rotateY(mDegreeBackWheel);
        mCamera.getMatrix(mCarMatrix);
        mCamera.restore();
        mCarMatrix.postTranslate(mCarLeft + 168f / 484 * mCarWidth, mCarTop + 98f / 232 * mCarHeight);
        canvas.save();
        canvas.concat(mCarMatrix);

        final float wheelHeight = 93f/232*mCarHeight;
        float scale = wheelHeight/mWheel.getHeight();
        mCarMatrix.setScale(scale, scale);
        mCarMatrix.postRotate(mDegreeTurnWheel, wheelHeight/2, wheelHeight/2);

        canvas.drawBitmap(mWheel, mCarMatrix, null);
        canvas.restore();*/
    }

    private class RunAnimation extends Animation {

        private float mFromX, mFromY, mToX, mToY;
        private long mDruation, mCurrentTime;

        public RunAnimation(float fromX, float fromY, float toX, float toY, long druation) {
            mFromX  = fromX;
            mFromY  = fromY;
            mToX    = toX;
            mToY    = toY;
            mDruation = druation;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            setDuration(mDruation);
            setFillAfter(true);
        }
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mCarLeft    = (int) (mFromX + (mToX - mFromX) * interpolatedTime);
            mCarTop     = (int) (mFromY + (mToY - mFromY) * interpolatedTime);
            mInterpolatedTime   = interpolatedTime;
            mDegreeTurnWheel    = (mCurrentTime - getStartTime())%17f*21;
            if (mInterpolatedTime == 1.0f && mStopTime == 0) {
                mStopTime = SystemClock.uptimeMillis();
            }
            if (DEBUG) Log.i("CarView", "mCarLeft:"+mCarLeft+"  mCarTop:"+mCarTop+"  time pass:"+mDegreeTurnWheel+"  interpolatedTime:"+interpolatedTime);
            invalidate();
        }

        @Override
        public boolean getTransformation(long currentTime, Transformation outTransformation) {
            mCurrentTime = currentTime;
            return super.getTransformation(currentTime, outTransformation);
        }
    }
}
