package dhl.anddemo.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

/**
 * Created by DuanHl on 2018/2/7.
 */

public class ActivityStack {

	private Stack<Activity> mActivityStack = new Stack<>();

	private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			mActivityStack.push(activity);
		}

		@Override
		public void onActivityStarted(Activity activity) {
		}

		@Override
		public void onActivityResumed(Activity activity) {
		}

		@Override
		public void onActivityPaused(Activity activity) {
		}

		@Override
		public void onActivityStopped(Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			mActivityStack.remove(activity);
		}
	};

	public void register(Application context) {
		context.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
	}

	public Activity getPreviousActivity(Activity activity) {
		if (mActivityStack.isEmpty() || activity == null) return null;
		Activity result = null;
		int index = mActivityStack.lastIndexOf(activity);
		while ((result == null || result.isFinishing()) && --index >= 0) {
			result = mActivityStack.get(index);
		}
		return result;
	}
}
