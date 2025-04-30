package dhl.anddemo.base.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import dhl.anddemo.R;

/**
 * Created by DuanHl on 2018/2/27.
 */

public class ProgressDialog extends BaseDialog {

	private Object mTag;

	private OnProgressCancelListener mListener;

	private DialogInterface.OnCancelListener mCancelListener = new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			if (mListener != null) mListener.onCancel(dialog, mTag);
		}
	};

	public ProgressDialog(@NonNull Context context) {
		super(context, R.style.ProgressDialogStyle);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_progress_layout);
		setGravity(Gravity.CENTER);
		setOnCancelListener(mCancelListener);
		setLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
		setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mTag = null;
				mListener = null;
			}
		});
	}

	public void setCancelListener(Object tag, OnProgressCancelListener listener) {
		mTag = tag;
		mListener = listener;
	}
}
