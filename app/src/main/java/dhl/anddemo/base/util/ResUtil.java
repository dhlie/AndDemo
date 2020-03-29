package dhl.anddemo.base.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import dhl.anddemo.base.App;

/**
 * Created by DuanHl on 2017/3/14.
 */

public class ResUtil {

	/**
	 * 获取StateListDrawable,只有按压和默认状态
	 *
	 * @param defaultId 默认状态下资源id
	 * @param pressedId 按压状态下资源id
	 * @return
	 */
	public static Drawable getStateListDrawable(int defaultId, int pressedId) {
		return getStateListDrawable(App.getInstance().getResources().getDrawable(defaultId),
						new int[]{android.R.attr.state_pressed}, App.getInstance().getResources().getDrawable(pressedId));
	}

	/**
	 * 获取StateListDrawable,只有按压和默认状态
	 *
	 * @param defDrawable     默认状态下drawable
	 * @param pressedDrawable 按压状态下drawable
	 * @return
	 */
	public static Drawable getStateListDrawable(Drawable defDrawable, Drawable pressedDrawable) {
		return getStateListDrawable(defDrawable, new int[]{android.R.attr.state_pressed}, pressedDrawable);
	}

	/**
	 * 获取StateListDrawable
	 *
	 * @param defDrawable    默认状态下drawable
	 * @param states         自定义状态
	 * @param statesDrawable 自定义状态下drawable
	 * @return
	 */
	public static Drawable getStateListDrawable(Drawable defDrawable, int[] states, Drawable statesDrawable) {
		StateListDrawable drawable = new StateListDrawable();
		if (states != null) drawable.addState(states, statesDrawable);
		drawable.addState(new int[0], defDrawable);
		return drawable;
	}

	public static String getString(int resId) {
		return App.getInstance().getString(resId);
	}
}
