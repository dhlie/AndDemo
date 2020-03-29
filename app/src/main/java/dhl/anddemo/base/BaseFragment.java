package dhl.anddemo.base;

import android.support.v4.app.Fragment;

import dhl.anddemo.base.util.PermissionUtil;

/**
 * Created by DuanHl on 2018/2/28.
 */

public class BaseFragment extends Fragment {

	//========================权限相关==========================
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// EasyPermissions handles the request result.
		PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}
	//========================权限相关==========================
}
