package dhl.viewbinding;

import android.view.View;

import dhl.annotation.viewbinding.Const;

/**
 * Created by DuanHl on 2017/9/25.
 */

public class BindUtil {

    /**
     * 绑定target中的view和点击事件
     * @param target        :目标对象
     * @param simpleName    :target混淆前的类名
     * @param finder        :findViewById调用对象
     * @param <T>
     */
    public static <T> void bindView(T target, String simpleName, View finder) {
        try {
            //className = 混淆后的包名.混淆前的类名 + 后缀
            String className = target.getClass().getPackage().getName() + "." + simpleName + Const.CLASS_SUFFIX;
            Class clazz = Class.forName(className);
            ViewBinder<T> binder = (ViewBinder<T>) clazz.newInstance();
            binder.bind(target, finder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
