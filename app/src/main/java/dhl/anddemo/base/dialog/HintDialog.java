package dhl.anddemo.base.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dhl.anddemo.R;

/**
 * Created by DuanHl on 2018/2/27.
 */

public class HintDialog extends BaseDialog {

	private TextView mTVTitle;       //标题view
	private TextView mTVContent;    //内容view
	private TextView mTVLeft;       //左边button
	private TextView mTVRight;      //右边button

	private CharSequence mTitle;    //提示内容
	private CharSequence mText;     //提示内容
	private String mLeftBtnText;    //左边按钮文字
	private String mRightBtnText;   //右边按钮文字

	private DialogClickListener mClickListener;

	private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.tvLeft:
					if (mClickListener != null) mClickListener.onLeftBtnClick(v);
					break;
				case R.id.tvRight:
					if (mClickListener != null) mClickListener.onRightBtnClick(v);
					break;
			}
		}
	};

	public HintDialog(@NonNull Context context) {
		super(context);
		mLeftBtnText = context.getString(R.string.btn_cancel);
		mRightBtnText = context.getString(R.string.btn_ok);
		setGravity(Gravity.CENTER, 60, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialog_common_layout);

		mTVTitle = findViewById(R.id.tvTitle);
		mTVContent = findViewById(R.id.tvContent);
		mTVLeft = findViewById(R.id.tvLeft);
		mTVRight = findViewById(R.id.tvRight);

		mTVLeft.setOnClickListener(mBtnClickListener);
		mTVRight.setOnClickListener(mBtnClickListener);

		if (mTitle == null || mTitle.length() == 0) {
			mTVTitle.setVisibility(View.GONE);
		} else {
			mTVTitle.setVisibility(View.VISIBLE);
			mTVTitle.setText(mTitle);
		}
		mTVContent.setText(mText);
		mTVLeft.setText(mLeftBtnText);
		mTVRight.setText(mRightBtnText);
	}

	/**
	 * 设置提示文字
	 *
	 * @see #setText(String)
	 */
	public void setText(int resId) {
		setText(getContext().getString(resId));
	}

	/**
	 * 设置标题
	 *
	 * @param title 标题,为空时不显示标题
	 */
	public void setTitle(CharSequence title) {
		mTitle = title;
		if (mTVTitle == null) return;

		if (title == null || title.length() == 0) {
			mTVTitle.setVisibility(View.GONE);
		} else {
			mTVTitle.setVisibility(View.VISIBLE);
			mTVTitle.setText(mTitle);
		}
	}

	/**
	 * 设置提示文字
	 *
	 * @param text 提示文字
	 */
	public void setText(String text) {
		mText = text;
		if (mTVContent != null) mTVContent.setText(text);
	}

	/**
	 * 设置左边按钮文字
	 *
	 * @see #setLeftBtnText(String)
	 */
	public void setLeftBtnText(int resId) {
		setLeftBtnText(getContext().getString(resId));
	}

	/**
	 * 设置左边按钮文字
	 *
	 * @param leftText 按钮文字
	 */
	public void setLeftBtnText(String leftText) {
		mLeftBtnText = leftText;
		if (mTVLeft != null) mTVLeft.setText(leftText);
	}

	/**
	 * 设置右边按钮文字
	 *
	 * @see #setRightBtnText(String)
	 */
	public void setRightBtnText(int resId) {
		setRightBtnText(getContext().getString(resId));
	}

	/**
	 * 设置右边按钮文字
	 *
	 * @param text 按钮文字
	 */
	public void setRightBtnText(String text) {
		mRightBtnText = text;
		if (mTVRight != null) mTVRight.setText(text);
	}

	/**
	 * 设置按钮点击listener
	 *
	 * @param listener
	 */
	public void setClickListener(DialogClickListener listener) {
		mClickListener = listener;
	}

}
