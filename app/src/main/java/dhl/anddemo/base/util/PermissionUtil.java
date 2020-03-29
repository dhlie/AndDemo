package dhl.anddemo.base.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dhl.anddemo.R;
import dhl.anddemo.base.dialog.BaseDialog;
import dhl.anddemo.base.dialog.BaseDialogClickListener;
import dhl.anddemo.base.dialog.HintDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by duanhl on 2018/3/7.
 */

public class PermissionUtil {

	private static Map<String, WeakReference<BasePermissionCallback>> sHosts = new HashMap<>();
	public static String[] sRequiredPers = {
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.INTERNET
	};

	public static String permissionsToName(List<String> perms) {
		StringBuilder sb = new StringBuilder();
		if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE) || perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			sb.append(ResUtil.getString(R.string.permission_storage));
		}
		if (perms.contains(Manifest.permission.INTERNET)) {
			sb.append(" ").append(ResUtil.getString(R.string.permission_internet));
		}
		return sb.toString();
	}

	public static boolean hasPermission(Context context, String... permission) {
		return EasyPermissions.hasPermissions(context, permission);
	}

	/**
	 * 请求权限
	 *
	 * @param actiOrFragment ：Activity或Fragment
	 * @param requestCode
	 * @param callback
	 * @param permisions
	 */
	public static void requestPermission(Object actiOrFragment, int requestCode, BasePermissionCallback callback, String... permisions) {
		if (actiOrFragment == null || callback == null || permisions == null || permisions.length == 0)
			return;

		if (!(actiOrFragment instanceof Activity) && !(actiOrFragment instanceof Fragment)) {
			throw new IllegalArgumentException("param actiOrFragment must be instance of Activity/Fragment");
		}

		String key = actiOrFragment.getClass().getName();
		sHosts.put(key, new WeakReference<>(callback));
		String rationale = String.format(ResUtil.getString(R.string.permission_request), permissionsToName(Arrays.asList(permisions)));
		if (actiOrFragment instanceof Activity) {
			EasyPermissions.requestPermissions((Activity) actiOrFragment, rationale, requestCode, permisions);
		} else {
			EasyPermissions.requestPermissions((Fragment) actiOrFragment, rationale, requestCode, permisions);
		}
	}

	public static void onRequestPermissionsResult(Object actiOrFragment, int requestCode, String[] permissions, int[] grantResults) {
		if (!(actiOrFragment instanceof Activity) && !(actiOrFragment instanceof Fragment)) {
			throw new IllegalArgumentException("param actiOrFragment must be instance of Activity/Fragment");
		}

		String key = actiOrFragment.getClass().getName();
		WeakReference<BasePermissionCallback> weakReference = sHosts.remove(key);
		EasyPermissions.PermissionCallbacks callback = weakReference == null ? null : weakReference.get();
		if (callback != null) {
			EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, callback);
		}
	}

	private static void showSettingDialog(final Activity activity, @NonNull List<String> perms) {
		if (activity == null || activity.isFinishing()) return;
		HintDialog dialog = new HintDialog(activity);
		String permNames = permissionsToName(perms);
		dialog.setText(String.format(ResUtil.getString(R.string.permission_prompt), permNames));
		dialog.setRightBtnText(R.string.setting);
		dialog.setCancelable(false);
		dialog.setClickListener(new BaseDialogClickListener() {
			@Override
			public void onRightBtnClick(View view) {
				activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", activity.getPackageName(), null)));
			}
		});
		dialog.show();
	}

	public static BaseDialog showExitDialog(final Activity activity, @NonNull List<String> perms) {
		if (activity == null || activity.isFinishing()) return null;
		final HintDialog dialog = new HintDialog(activity);
		String permNames = permissionsToName(perms);
		dialog.setText(String.format(ResUtil.getString(R.string.permission_exit_prompt), permNames));
		dialog.setLeftBtnText(R.string.btn_ok);
		dialog.setRightBtnText(R.string.setting);
		dialog.setCancelable(false);
		dialog.setClickListener(new BaseDialogClickListener() {
			@Override
			public void onRightBtnClick(View view) {
				activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", activity.getPackageName(), null)));
				System.exit(0);
			}

			@Override
			public void onLeftBtnClick(View view) {
				System.exit(0);
			}
		});
		dialog.show();
		return dialog;
	}

	public abstract static class BasePermissionCallback implements EasyPermissions.PermissionCallbacks {

		private WeakReference<Activity> mHost;

		public BasePermissionCallback(Activity activity) {
			mHost = new WeakReference<Activity>(activity);
		}

		@Override
		public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
			// (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
			// This will display a dialog directing them to enable the permission in app settings.
			Activity activity = mHost.get();
			if (activity != null) {
				if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
					showSettingDialog(activity, perms);
				}
			}
		}

		@Override
		public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		}
	}
}
