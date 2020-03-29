package dhl.anddemo.base.util;

import android.util.TypedValue;

import dhl.anddemo.base.App;

/**
 * Created by coolyou on 2017/3/14.
 */

public class PixelUtil {

	public static int dp2px(int dp) {
		final float f = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, App.getInstance().getResources().getDisplayMetrics());
		final int res = (int) (f + 0.5f);
		if (res != 0) return res;
		if (dp == 0) return 0;
		if (dp > 0) return 1;
		return -1;
	}

	public static int sp2px(int sp) {
		final float f = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, App.getInstance().getResources().getDisplayMetrics());
		final int res = (int) (f + 0.5f);
		if (res != 0) return res;
		if (sp == 0) return 0;
		if (sp > 0) return 1;
		return -1;
	}
}
