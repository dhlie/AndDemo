package dhl.anddemo.base;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

/**
 * Created by DuanHl on 2017/3/14.
 */

public class App extends Application {

    private static App sInstance;
    private static Handler sHandler = new Handler();
    private ActivityStack mActivityStack;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        mActivityStack = new ActivityStack();
        mActivityStack.register(this);
    }

    public Activity getPreviousActivity(Activity activity) {
        return mActivityStack.getPreviousActivity(activity);
    }

    public static void postToUiThread(Runnable action) {
        postToUiThread(action, 0);
    }

    public static void postToUiThread(Runnable action, int delay) {
        sHandler.postDelayed(action, delay);
    }

}
