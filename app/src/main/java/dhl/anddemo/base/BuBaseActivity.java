package dhl.anddemo.base;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.dhl.base.BaseActivity;

import dhl.anddemo.R;

/**
 * Created by DuanHl on 2017/3/13.
 */

public class BuBaseActivity extends BaseActivity {

	private boolean mSlideToFinishEnable = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!(this instanceof MainActivity)) {
			if (SlideFinishFrameLayout.TYPE == SlideFinishFrameLayout.TYPE_SCALE) {
				overridePendingTransition(R.anim.right_in, R.anim.scale_out);
			} else if (SlideFinishFrameLayout.TYPE == SlideFinishFrameLayout.TYPE_TRANSLATE) {
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		}
		super.onCreate(savedInstanceState);
		prepareSlideToFinish();
	}

	@Override
	public void finish() {
		super.finish();
		if (!(this instanceof MainActivity)) {
			if (SlideFinishFrameLayout.TYPE == SlideFinishFrameLayout.TYPE_SCALE) {
				overridePendingTransition(R.anim.scale_in, R.anim.right_out);
			} else if (SlideFinishFrameLayout.TYPE == SlideFinishFrameLayout.TYPE_TRANSLATE) {
				overridePendingTransition(R.anim.scale_in, R.anim.right_out);
			}
		}
	}


	/**
	 * 设置透明状态栏
	 */
	protected void setupTransparencyStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
							| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
											| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(R.color.transparent));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	/**
	 * 开启右滑关闭功能
	 */
	public void enableSlideToFinish() {
		mSlideToFinishEnable = true;
		ViewGroup deco = (ViewGroup) getWindow().getDecorView();
		for (int i = 0; i < deco.getChildCount(); i++) {
			View child = deco.getChildAt(i);
			if (child instanceof SlideFinishFrameLayout) {
				((SlideFinishFrameLayout) child).enable();
				break;
			}
		}
	}

	/**
	 * 关闭右滑关闭功能
	 */
	public void disableSlideToFinish() {
		mSlideToFinishEnable = false;
		ViewGroup deco = (ViewGroup) getWindow().getDecorView();
		for (int i = 0; i < deco.getChildCount(); i++) {
			View child = deco.getChildAt(i);
			if (child instanceof SlideFinishFrameLayout) {
				((SlideFinishFrameLayout) child).disable();
				break;
			}
		}
	}

	private void prepareSlideToFinish() {
		if (mSlideToFinishEnable) {
			new SlideFinishFrameLayout(getApplicationContext()).attachToActivity(this);
		}
	}

}
