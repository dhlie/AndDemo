package dhl.anddemo.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.dhl.aidl.ITestAidlInterface;
import cn.dhl.ipcserver.City;
import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.LLog;
import dhl.annotation.viewbinding.BindClick;
import dhl.annotation.viewbinding.BindView;
import dhl.viewbinding.BindUtil;

/**
 * Created by DuanHl on 2017/9/17.
 */

public class AidlActivity extends BaseActivity {

	@BindView(R.id.titlebar)
	public TitleBar mTitleBar;
	@BindView(R.id.bindservice)
	public Button mBtnBind;
	@BindView(R.id.unbindservice)
	public Button mBtnUnbind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acti_aidl);
		BindUtil.bindView(this, "AidlActivity", getWindow().getDecorView());

		TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
			@Override
			public void onLeftClick(View v) {
				finish();
			}

			@Override
			public void onRightFirstClick(View v) {
			}
		};
		mTitleBar.setTitleBarClickListener(titleBarClickListener);
		mTitleBar.setTitle(getClass().getSimpleName());
	}

	@BindClick({R.id.bindservice, R.id.unbindservice})
	public void clickEvent(View view) {
		switch (view.getId()) {
			case R.id.bindservice:
				startRemoteService();
				break;
			case R.id.unbindservice:
				Toast.makeText(getApplicationContext(), "button2", Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void startRemoteService() {
		LLog.i("startRemoteService");
		Intent intent = new Intent();
		ComponentName cm = new ComponentName("cn.dhl.ipcserver", "cn.dhl.ipcserver.AidlDemoService");
		intent.setComponent(cm);
		intent.setPackage(getPackageName());

		bindService(intent, new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				LLog.i("onServiceConnected");
				ITestAidlInterface inter = ITestAidlInterface.Stub.asInterface(service);
				try {
					String result = inter.basicTypes(0, 0L, true, 0f, 0d, "string from client");
					Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

					City city = new City();
					city.name = "shenzhen";
					City ci = inter.changeCityName(city);
					LLog.i("city name:" + ci.name);

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				LLog.i("onServiceDisconnected");
			}
		}, Context.BIND_AUTO_CREATE);
	}
}
