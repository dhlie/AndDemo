package dhl.anddemo.webview;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.LLog;

/**
 * Created by DuanHl on 2017/11/8.
 */

public class WebActivity extends BaseActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acti_webview);

        TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
            @Override
            public void onLeftClick(View v) {
                finish();
            }
        };
        TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
        titleBar.setTitleBarClickListener(titleBarClickListener);
        titleBar.setTitle(getClass().getSimpleName());

        mWebView = (WebView) findViewById(R.id.wv_web);

        initWebView();

        mWebView.loadUrl("http://www.baidu.com");
    }

    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (LLog.PRINT_LOG) LLog.i("onProgressChanged-----------newProgress:" + newProgress);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                mWebView.loadUrl(url);
                return false;
            }
        });
    }
}
