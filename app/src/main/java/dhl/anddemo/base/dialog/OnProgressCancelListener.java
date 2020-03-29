package dhl.anddemo.base.dialog;

import android.content.DialogInterface;

/**
 * Created by DuanHl on 2018/2/28.
 * <p>
 * 进度对话框取消时的回掉接口
 */

public interface OnProgressCancelListener {
	void onCancel(DialogInterface dialog, Object tag);
}
