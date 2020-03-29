package dhl.anddemo.matrix;

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

public class CarAnimationActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

	private SeekBar mSeekbarYRotate;
	private TextView mTVYRotate;
	private CarWheelView mCarWheelView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acti_camera_matrix);

		TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
			@Override
			public void onLeftClick(View v) {
				finish();
			}
		};
		TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.setTitleBarClickListener(titleBarClickListener);
		titleBar.setTitle(getClass().getSimpleName());

		mCarWheelView = (CarWheelView) findViewById(R.id.mv1);
		mCarWheelView.startAnim();

		mSeekbarYRotate = (SeekBar) findViewById(R.id.seekbarYRotate);
		mSeekbarYRotate.setOnSeekBarChangeListener(this);
		mTVYRotate = (TextView) findViewById(R.id.txtYRotate);

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
