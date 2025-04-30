package com.dhl.base.utils;

import java.lang.reflect.Field;

/**
 * Author: duanhaoliang
 * Create: 2021/7/9 16:27
 * Description:
 */
public class ReflectionUtil {

    private static final String TAG = "Reflect";

    public static Object getFieldValue(Object target, String fieldName) {
        if (target == null || fieldName == null) {
            return null;
        }
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) {
            return;
        }
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
