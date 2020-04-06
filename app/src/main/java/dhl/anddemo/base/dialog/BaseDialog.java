package dhl.anddemo.base.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import dhl.anddemo.R;
import dhl.anddemo.base.util.PixelUtil;

/**
 * Created by DuanHl on 2018/2/27.
 */

public class BaseDialog extends Dialog {

	/**
	 * 对话框位置
	 */
	private int mGravity = Gravity.CENTER;

	/**
	 * 对话框宽
	 */
	private int mWidth = WindowManager.LayoutParams.MATCH_PARENT;

	/**
	 * 对话框高
	 */
	private int mHeight = WindowManager.LayoutParams.WRAP_CONTENT;

	/**
	 * 对话框水平边距
	 */
	private int mHorizontalMargin;

	/**
	 * 对话框垂直边距
	 */
	private int mVerticalMargin;

	/**
	 * 对话框进出动画
	 */
	private int mWindowAnimations;

	/**
	 * 对话框属性是否有变化
	 */
	private boolean mAttributeChanged = true;

	public BaseDialog(@NonNull Context context) {
		this(context, R.style.BaseDialog);
	}

	public BaseDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
		setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initAttribute();
	}

	private void initAttribute() {
		if (!mAttributeChanged) return;
		mAttributeChanged = false;

		Window window = getWindow();
		if (window == null) return;

		WindowManager.LayoutParams params = window.getAttributes();
		//设置默认动画
		if ((mGravity & Gravity.TOP) == Gravity.TOP) {
			params.windowAnimations = mWindowAnimations == 0 ? R.style.DialogTopAnim : mWindowAnimations;
		} else if ((mGravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
			params.windowAnimations = mWindowAnimations == 0 ? R.style.DialogBottomAnim : mWindowAnimations;
		} else {
			params.windowAnimations = mWindowAnimations == 0 ? R.style.DialogCenterAnim : mWindowAnimations;
		}

		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		if (mWidth == WindowManager.LayoutParams.MATCH_PARENT) {
			mWidth = metrics.widthPixels - (PixelUtil.dip2px(mHorizontalMargin) << 1);
		} else if (mWidth == WindowManager.LayoutParams.WRAP_CONTENT) {
			params.horizontalMargin = (float) PixelUtil.dip2px(mHorizontalMargin) / metrics.widthPixels;
		}
		if (mHeight == WindowManager.LayoutParams.MATCH_PARENT) {
			mHeight = metrics.heightPixels - (PixelUtil.dip2px(mVerticalMargin) << 1);
		} else if (mHeight == WindowManager.LayoutParams.WRAP_CONTENT) {
			params.verticalMargin = (float) PixelUtil.dip2px(mVerticalMargin) / metrics.heightPixels;
		}
		params.width = mWidth;
		params.height = mHeight;
		window.setGravity(mGravity);
	}

	/**
	 * 设置对话框位置
	 *
	 * @see #setGravity(int, int, int)
	 */
	public void setGravity(int gravity) {
		setGravity(gravity, 0, 0);
	}

	/**
	 * 设置对话框位置
	 *
	 * @param gravity          位置
	 * @param horizontalMargin 水平边距
	 * @param verticalMargin   垂直边距
	 */
	public void setGravity(int gravity, int horizontalMargin, int verticalMargin) {
		if (mGravity != gravity) mAttributeChanged = true;
		mGravity = gravity;

		if (mHorizontalMargin != horizontalMargin) mAttributeChanged = true;
		mHorizontalMargin = horizontalMargin;

		if (mVerticalMargin != verticalMargin) mAttributeChanged = true;
		mVerticalMargin = verticalMargin;
	}

	/**
	 * 设置对话框宽高
	 *
	 * @param width  宽
	 * @param height 高
	 */
	public void setLayoutParams(int width, int height) {
		if (mWidth != width || mHeight != height) mAttributeChanged = true;
		mWidth = width;
		mHeight = height;
	}

	/**
	 * 设置window动画
	 *
	 * @param resId 动画资源id
	 */
	public void setWindowAnimations(int resId) {
		if (mWindowAnimations != resId) mAttributeChanged = true;
		mWindowAnimations = resId;
	}

}
