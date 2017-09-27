package dhl.viewbinding;

import android.view.View;

/**
 * Created by DuanHl on 2017/9/25.
 */

public interface ViewBinder<T> {

    void bind(T target, View finder);

}
