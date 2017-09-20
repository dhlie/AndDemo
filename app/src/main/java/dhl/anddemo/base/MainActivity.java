package dhl.anddemo.base;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import dhl.anddemo.R;
import dhl.anddemo.clipregion.ClipRegionActivity;
import dhl.anddemo.matrix.CameraApiActivity;
import dhl.anddemo.test.TestAvtivity;
import dhl.anddemo.turnpage.TurnPageActivity;

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
                startActivity(new Intent(getApplicationContext(), CameraApiActivity.class));
                break;
            case R.id.tv_clipregion:
                startActivity(new Intent(getApplicationContext(), ClipRegionActivity.class));
                break;
            case R.id.tv_turnpage:
                startActivity(new Intent(getApplicationContext(), TurnPageActivity.class));
                break;
            case R.id.tv_test:
                startActivity(new Intent(getApplicationContext(), TestAvtivity.class));
                break;
        }
    }

}
