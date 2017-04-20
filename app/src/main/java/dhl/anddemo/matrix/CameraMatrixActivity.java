package dhl.anddemo.matrix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;

/**
 * Created by DuanHl on 2017/4/19.
 */

public class CameraMatrixActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekbarYRotate;
    private TextView mTVYRotate;
    private CarWheelView mCarWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_camera_matrix);

        TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
        titleBar.setLeftBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCarWheelView = (CarWheelView) findViewById(R.id.mv1);
        mCarWheelView.startAnim();

        mSeekbarYRotate = (SeekBar) findViewById(R.id.seekbarYRotate);
        mSeekbarYRotate.setOnSeekBarChangeListener(this);
        mTVYRotate = (TextView) findViewById(R.id.txtYRotate);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CameraApiActivity.class));
            }
        });
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MatrixEditActivity.class));
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mSeekbarYRotate) {
            mTVYRotate.setText(progress + "゜");
            mCarWheelView.setDegree(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
