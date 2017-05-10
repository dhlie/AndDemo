package dhl.anddemo.matrix;

import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;

/**
 * Camera与Matrix的比较：<br/>
 * Camera的rotate()相关方法是指定某一维度上旋转指定的角度。<br/>
 * Matrix的rotate()相关方法实现的效果是顺时针旋转指定的角度；与Camera指定Z轴旋转效果相同，但方向相反。<br/>
 *
 * Camera的translate()方法根据某一维度上视点的位移实现图像的缩放，与Matrix的scale()相关方法作用效果相似，
 * 只是Matrix的scale()相关方法是直接指定缩放比例。<br/>
 *
 * Camera不支持倾斜操作，Matrix可以直接实现倾斜操作。<br/>
 *
 * <a href="http://my.oschina.net/arthor" class="referer" target="_blank">@author</a> Sodino E-mail:sodinoopen@hotmail.com
 * @version Time：2011-9-26 下午04:17:49
 */
public class CameraApiActivity extends BaseActivity implements OnSeekBarChangeListener {

    // views
    private SeekBar mSeekbarXRotate;
    private SeekBar mSeekbarYRotate;
    private SeekBar mSeekbarZRotate;
    private TextView mTVXRotate;
    private TextView mTVYRotate;
    private TextView mTVZRotate;

    private SeekBar mSeekbarXSkew;
    private SeekBar mSeekbarYSkew;
    private TextView mTVXSkew;
    private TextView mTVYSkew;

    private SeekBar mSeekbarXTranslate;
    private SeekBar mSeekbarYTranslate;
    private SeekBar mSeekbarZTranslate;
    private TextView mTVXTranslate;
    private TextView mTVYTranslate;
    private TextView mTVZTranslate;

    private ImageView mIVResult;

    private Camera mCamera;
    private int mRotateX, mRotateY, mRotateZ;
    private int mTranslateX, mTranslateY, mTranslateZ;
    private float mSkewX, mSkewY;

    //解决seekbar在scrollable容器(isInScrollingContainer())时滑动事件冲突
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (v.getParent() != null) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_camera_api);

        TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
            @Override
            public void onLeftClick(View v) {
                finish();
            }

            @Override
            public void onRightFirstClick(View v) {
                reset();
            }
        };
        TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
        titleBar.setTitleBarClickListener(titleBarClickListener);
        titleBar.setTitle(getClass().getSimpleName());

        mCamera = new Camera();
        // initViews
        //rotate
        mSeekbarXRotate = (SeekBar) findViewById(R.id.seekbarXRotate);
        mSeekbarXRotate.setOnSeekBarChangeListener(this);
        mSeekbarYRotate = (SeekBar) findViewById(R.id.seekbarYRotate);
        mSeekbarYRotate.setOnSeekBarChangeListener(this);
        mSeekbarZRotate = (SeekBar) findViewById(R.id.seekbarZRotate);
        mSeekbarZRotate.setOnSeekBarChangeListener(this);
        mTVXRotate = (TextView) findViewById(R.id.txtXRotate);
        mTVYRotate = (TextView) findViewById(R.id.txtYRotate);
        mTVZRotate = (TextView) findViewById(R.id.txtZRotate);
        // translate
        mSeekbarXTranslate = (SeekBar) findViewById(R.id.seekbarXTranslate);
        mSeekbarXTranslate.setOnSeekBarChangeListener(this);
        mSeekbarYTranslate = (SeekBar) findViewById(R.id.seekbarYTranslate);
        mSeekbarYTranslate.setOnSeekBarChangeListener(this);
        mSeekbarZTranslate = (SeekBar) findViewById(R.id.seekbarZTranslate);
        mSeekbarZTranslate.setOnSeekBarChangeListener(this);
        mTVXTranslate = (TextView) findViewById(R.id.txtXTranslate);
        mTVYTranslate = (TextView) findViewById(R.id.txtYTranslate);
        mTVZTranslate = (TextView) findViewById(R.id.txtZTranslate);
        //skew
        mSeekbarXSkew = (SeekBar) findViewById(R.id.seekbarXSkew);
        mSeekbarXSkew.setOnSeekBarChangeListener(this);
        mSeekbarYSkew = (SeekBar) findViewById(R.id.seekbarYSkew);
        mSeekbarYSkew.setOnSeekBarChangeListener(this);
        mTVXSkew = (TextView) findViewById(R.id.txtXSkew);
        mTVYSkew = (TextView) findViewById(R.id.txtYSkew);

        mIVResult = (ImageView) findViewById(R.id.iv_image);

        mSeekbarXRotate.setOnTouchListener(mOnTouchListener);
        mSeekbarYRotate.setOnTouchListener(mOnTouchListener);
        mSeekbarZRotate.setOnTouchListener(mOnTouchListener);
        mSeekbarXTranslate.setOnTouchListener(mOnTouchListener);
        mSeekbarYTranslate.setOnTouchListener(mOnTouchListener);
        mSeekbarZTranslate.setOnTouchListener(mOnTouchListener);
        mSeekbarXSkew.setOnTouchListener(mOnTouchListener);
        mSeekbarYSkew.setOnTouchListener(mOnTouchListener);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MatrixEditActivity.class));
            }
        });
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CarAnimationActivity.class));
            }
        });

        // refresh
        refreshImage();
    }

    private void reset() {
        mRotateX = mRotateY = mRotateZ = 0;
        mTranslateX = mTranslateY = mTranslateZ;
        mSkewX = mSkewY = 0f;

        mSeekbarXRotate.setProgress(0);
        mSeekbarYRotate.setProgress(0);
        mSeekbarZRotate.setProgress(0);
        mTVXRotate.setText("0゜");
        mTVYRotate.setText("0゜");
        mTVZRotate.setText("0゜");

        mSeekbarXTranslate.setProgress(100);
        mSeekbarYTranslate.setProgress(100);
        mSeekbarZTranslate.setProgress(100);
        mTVXTranslate.setText("0");
        mTVYTranslate.setText("0");
        mTVZTranslate.setText("0");

        mSeekbarXSkew.setProgress(100);
        mSeekbarYSkew.setProgress(100);
        mTVXSkew.setText("0.0");
        mTVYSkew.setText("0.0");
        refreshImage();
    }

    private void refreshImage() {
        mCamera.save();
        Matrix matrix = new Matrix();
        // rotate
        mCamera.rotateX(mRotateX);
        mCamera.rotateY(mRotateY);
        mCamera.rotateZ(mRotateZ);
//         translate
        mCamera.translate(mTranslateX, mTranslateY, mTranslateZ);
        mCamera.getMatrix(matrix);
        // 恢复到之前的初始状态。
        mCamera.restore();
        // 设置图像处理的中心点
        matrix.preTranslate(-mIVResult.getWidth()/2, -mIVResult.getHeight()/2);
        matrix.postTranslate(mIVResult.getWidth()/2, mIVResult.getHeight()/2);

        matrix.preTranslate(mIVResult.getWidth()/2, mIVResult.getHeight()/2);
        matrix.preSkew(mSkewX, mSkewY);
        matrix.preTranslate(-mIVResult.getWidth()/2, -mIVResult.getHeight()/2);
        // 直接setSkew()，则前面处理的rotate()、translate()等等都将无效。
        mIVResult.setImageMatrix(matrix);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mSeekbarXRotate) {
            mTVXRotate.setText(progress + "゜");
            mRotateX = progress;
        } else if (seekBar == mSeekbarYRotate) {
            mTVYRotate.setText(progress + "゜");
            mRotateY = progress;
        } else if (seekBar == mSeekbarZRotate) {
            mTVZRotate.setText(progress + "゜");
            mRotateZ = progress;
        } else if (seekBar == mSeekbarXTranslate) {
            mTranslateX = progress - 100;
            mTVXTranslate.setText(String.valueOf(mTranslateX));
        } else if (seekBar == mSeekbarYTranslate) {
            mTranslateY = progress - 100;
            mTVYTranslate.setText(String.valueOf(mTranslateY));
        } else if (seekBar == mSeekbarZTranslate) {
            mTranslateZ = progress - 100;
            mTVZTranslate.setText(String.valueOf(mTranslateZ));
        } else if (seekBar == mSeekbarXSkew) {
            mSkewX = (progress - 100) * 1.0f / 100;
            mTVXSkew.setText(String.valueOf(mSkewX));
        } else if (seekBar == mSeekbarYSkew) {
            mSkewY = (progress - 100) * 1.0f / 100;
            mTVYSkew.setText(String.valueOf(mSkewY));
        }
        refreshImage();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}