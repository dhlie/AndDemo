package dhl.anddemo.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.view.BadgeTextView;
import dhl.annotation.viewbinding.BindClick;
import dhl.annotation.viewbinding.BindView;
import dhl.viewbinding.BindUtil;

/**
 * Created by DuanHl on 2017/9/17.
 */

public class TestActivity extends BaseActivity {

  @BindView(R.id.textView)
	public BadgeTextView mBadgeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acti_test);
		BindUtil.bindView(this, "TestAvtivity", getWindow().getDecorView());
	}

	@BindClick({R.id.textView})
	public void clickEvent(View view) {
		switch (view.getId()) {
			case R.id.textView:
				Toast.makeText(getApplicationContext(), "button", Toast.LENGTH_SHORT).show();
				break;
		}
	}
}
