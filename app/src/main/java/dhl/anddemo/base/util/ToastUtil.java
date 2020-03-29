package dhl.anddemo.base.util;

import android.graphics.Paint;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import dhl.anddemo.R;
import dhl.anddemo.base.App;

/**
 * Created by DuanHl on 2018/2/24.
 */

public class ToastUtil {

	private static Toast sToast;
	private static TextView sTVToast;

	static {
		sToast = new Toast(App.getInstance());
		View view = LayoutInflater.from(App.getInstance()).inflate(R.layout.toast_layout, null);
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		sTVToast = view.findViewById(R.id.tv_toast);
		sToast.setDuration(Toast.LENGTH_SHORT);
		sToast.setView(view);
		sToast.setGravity(Gravity.BOTTOM, 0, PixelUtil.dp2px(100));

		try {
			Field mTNField = Toast.class.getDeclaredField("mTN");
			mTNField.setAccessible(true);
			Object mTNObject = mTNField.get(sToast);
			Field paramsField = mTNObject.getClass().getDeclaredField("mParams");
			paramsField.setAccessible(true);
			WindowManager.LayoutParams params = (WindowManager.LayoutParams) paramsField.get(mTNObject);

			Paint.FontMetrics fm = sTVToast.getPaint().getFontMetrics();
			float lineHeight = fm.bottom - fm.top;
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.height = (int) (lineHeight * 3 + PixelUtil.dp2px(20) + 0.5f);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void show(@StringRes int resId) {
		show(App.getInstance().getString(resId));
	}

	public static void show(final String msg) {
		if (msg == null || msg.length() == 0) return;

		if (Looper.myLooper() != Looper.getMainLooper()) {
			App.postToUiThread(new Runnable() {
				@Override
				public void run() {
					doShowToast(msg);
				}
			});
		} else {
			doShowToast(msg);
		}
	}

	private static void doShowToast(String msg) {
		sTVToast.setText(msg);
		sToast.show();
	}

}
