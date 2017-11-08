package dhl.anddemo.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dhl.anddemo.R;
import dhl.anddemo.base.util.PixelUtil;
import dhl.anddemo.clipregion.ClipRegionActivity;
import dhl.anddemo.matrix.CameraApiActivity;
import dhl.anddemo.test.TestAvtivity;
import dhl.anddemo.turnpage.TurnPageActivity;
import dhl.anddemo.webview.WebActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initItemView();
    }

    private void initItemView() {
        List<Class> items = initItems();
        LinearLayout viewGroup = (LinearLayout) findViewById(R.id.ll_container);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(48));
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View.OnClickListener lis = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class clazz = (Class) view.getTag();
                startActivity(new Intent(getApplicationContext(), clazz));
            }
        };
        for (Class clazz : items) {
            TextView tv = new TextView(getApplicationContext());
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setText(clazz.getSimpleName());
            tv.setTextColor(Color.BLACK);
            tv.setPadding(PixelUtil.dp2px(16), 0, PixelUtil.dp2px(16), 0);
            tv.setBackgroundResource(R.drawable.button_pressed_selector);
            tv.setOnClickListener(lis);
            tv.setTag(clazz);

            View divider = new View(getApplicationContext());
            divider.setBackgroundColor(getResources().getColor(R.color.divider_line));

            viewGroup.addView(tv, params1);
            viewGroup.addView(divider, params2);
        }
    }

    private List<Class> initItems() {
        List<Class> items = new ArrayList<>();
        items.add(CameraApiActivity.class);
        items.add(ClipRegionActivity.class);
        items.add(TurnPageActivity.class);
        items.add(WebActivity.class);
        items.add(TestAvtivity.class);
        return items;
    }

}
