package dhl.anddemo.base.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by DuanHl on 2016/1/7.
 */
public class DeviceInfo {

    private static int              mStatusBarHeight = -1;          //状态栏高度
    private static Boolean          mHasNavigationBar = null;       //是否有虚拟按键
    private static int              mNavigationBarHeight = -1;      //虚拟按键的高度

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        if (mStatusBarHeight == -1) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            } else {
                mStatusBarHeight = PixelUtil.dp2px(25);
            }
        }
        return mStatusBarHeight;
    }

    /**
     * 获取虚拟按键的高度
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        if (mNavigationBarHeight == -1) {
            int navigationBarHeight = 0;
            if (hasNavigationBar(context)) {
                Resources rs = context.getResources();
                int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
                if (id > 0) {
                    navigationBarHeight = rs.getDimensionPixelSize(id);
                }
            }
            mNavigationBarHeight = navigationBarHeight;
        }
        return mNavigationBarHeight;
    }

    /**
     * 是否有虚拟按键
     * @param context
     * @return
     */
    public static boolean hasNavigationBar(Context context) {
        if (mHasNavigationBar == null) {
            boolean hasNavigationBar = false;
            if (Build.VERSION.SDK_INT >= 14) {
                Resources rs = context.getResources();
                int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
                if (id > 0) {
                    hasNavigationBar = rs.getBoolean(id);
                }
                try {
                    Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                    Method m = systemPropertiesClass.getMethod("get", String.class);
                    String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
                    if ("1".equals(navBarOverride)) {
                        hasNavigationBar = false;
                    } else if ("0".equals(navBarOverride)) {
                        hasNavigationBar = true;
                    }
                } catch (Exception e) {
                    LLog.e(e.toString());
                }
            }
            mHasNavigationBar = hasNavigationBar;
        }
        return mHasNavigationBar;
    }
}
