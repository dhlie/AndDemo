package dhl.anddemo.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import dhl.anddemo.R;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.anddemo.base.util.Dirs;
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
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//bug:webview有内容但是不显示,滑动一下才显示
        initWebView();

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                loadUrl();
                return false;
            }
        });
    }

    private void loadUrl() {
        //方式1:加载网络html
        //mWebView.loadUrl("http://www.zhangyue.com/products/iReader");
        //mWebView.loadUrl("https://www.taobao.com/");
        mWebView.loadUrl("http://www.runoob.com/try/demo_source/tryhtml5_html_manifest.htm");
        //方式2:加载本地html
        //mWebView.loadUrl("file:///android_asset/c51.html");
        //方式3：加载手机本地的html页面(不能关闭文件获取功能,否则无法加载 setAllowFileAccess(true))
        //mWebView.loadUrl("file:///mnt/sdcard/c51.html");
        //方式3:加载 HTML 页面的一小段内容
        //mWebView.loadData(html, "text/html; charset=UTF-8", null);
    }

    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        settings.setDatabaseEnabled(true);   //开启 database storage API 功能
        settings.setAppCacheEnabled(true);//开启 Application Caches 功能
        settings.setAppCachePath(Dirs.createDirs(Dirs.getCacheDir()).getPath());
        settings.setAppCacheMaxSize(50*1024*1024);
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
        settings.setSupportZoom(true);

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (LLog.PRINT_LOG) LLog.i("onDownloadStart-----------url:" + url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //页面加载进度,任何加载方式都会有进度
                //if (LLog.PRINT_LOG) LLog.i("onProgressChanged-----------newProgress:" + newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //<title>测试html</title>
                if (LLog.PRINT_LOG) LLog.i("onReceivedTitle-----------title:" + title);
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (LLog.PRINT_LOG) LLog.i("shouldOverrideUrlLoading-----------url:" + url);
                //mWebView.loadUrl(url);
                //return true;
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (LLog.PRINT_LOG) LLog.i("onPageStarted-----------url:" + url);
                //页面加载过程中,禁止加载网络图片,提高加载速度
                mWebView.getSettings().setBlockNetworkImage(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (LLog.PRINT_LOG) LLog.i("onPageFinished-----------url:" + url);
                mWebView.getSettings().setBlockNetworkImage(false);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                //if (LLog.PRINT_LOG) LLog.i("onLoadResource-----------url:" + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (LLog.PRINT_LOG) LLog.i("onReceivedError-----------failingUrl:" + failingUrl);
                //mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);//清除页面内容
                mWebView.loadUrl("file:///android_asset/error.html");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (LLog.PRINT_LOG) LLog.i("onReceivedSslError-----------");
                //webView默认是不处理https请求的，页面显示空白，需要进行如下设置：
                handler.proceed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //避免webview内存泄漏: 1清空页面内容 2从view数中移除 3调用destroy()方法
        mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);//清除页面内容
        if (mWebView.getParent() != null) {
            ((ViewGroup)mWebView.getParent()).removeViewInLayout(mWebView);
        }
        mWebView.destroy();//doc: this method should be called after this WebView has been removed from the view system.
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    String html = "<!DOCTYPE html>\n" +
            "<!-- saved from url=(0061)http://pics.showself.com/upload_pics/operation/shall/c51.html -->\n" +
            "<html>\n" +
            "<head lang=\"en\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title></title>\n" +
            "</head>\n" +
            "\n" +
            "<body style=\"-webkit-user-select: none; background-color: rgb(254, 249, 243);\">\n" +
            "<div id=\"panel\">\n" +
            "    <div class=\"wrap\"> <img height=\"13\" width=\"13\" src=\"./c51_files/xiaolaba.png\"><font color=\"blue\">认准官方帐号特征<img height=\"14\" width=\"14\" src=\"./c51_files/shall_role_xs.png\"> <img height=\"14\" width=\"14\" src=\"./c51_files/shall_identity_xg.png\"> <img height=\"14\" width=\"14\" src=\"./c51_files/shall_identity_gf.png\">，谨防您的权益受损！</font>  </div>\n" +
            "\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
}
