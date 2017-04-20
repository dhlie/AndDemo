package dhl.anddemo.base;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.matrix.CameraMatrixActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    public void onItemClick(View view) {
        switch (view.getId()) {
            case R.id.tv_camera_matrix:
                startActivity(new Intent(getApplicationContext(), CameraMatrixActivity.class));
                break;
        }
    }

}
