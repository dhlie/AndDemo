package dhl.anddemo.m3u8;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dhl.anddemo.R;
import dhl.anddemo.base.App;
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.annotation.viewbinding.BindClick;
import dhl.annotation.viewbinding.BindView;
import dhl.m3u8download.M3u8DownloadException;
import dhl.m3u8download.M3u8DownloadListener;
import dhl.m3u8download.M3u8Downloader;
import dhl.m3u8download.M3u8Util;
import dhl.viewbinding.BindUtil;

/**
 * Created by DuanHl on 2017/11/8.
 */

public class M3u8DownloadActivity extends BaseActivity {

  @BindView(R.id.et_url)
  public EditText mETInput;
  @BindView(R.id.btn_clear)
  public Button mBtnClear;
  @BindView(R.id.btn_start)
  public Button mBtnStart;
  @BindView(R.id.btn_stop)
  public Button mBtnStop;
  @BindView(R.id.btn_play)
  public Button mBtnPlay;
  @BindView(R.id.btn_del)
  public Button mBtnDel;
  @BindView(R.id.tv_status)
  public TextView mTVStatus;

  //single .ts file, with byte-ranges in the playlists
  //private String mDefaultUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/gear2/prog_index.m3u8";
  //encrypted
  //private String mDefaultUrl = "https://d2y4aoza0fc2pu.cloudfront.net/20200304/1pondo-020117_475/1000kb/hls/index.m3u8";
  private String mDefaultUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear3/prog_index.m3u8";

  ExecutorService mThreadPoolExecutor = new ThreadPoolExecutor(
          10,
          20,
          5,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>());
  String mSaveDir = "/sdcard/hls";
  int mDownloadThreadCount = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.acti_m3u8_download);
    BindUtil.bindView(this, "M3u8DownloadActivity", getWindow().getDecorView());

    TitleBar.SimpleTitleBarClickListener titleBarClickListener = new TitleBar.SimpleTitleBarClickListener() {
      @Override
      public void onLeftClick(View v) {
        finish();
      }
    };
    TitleBar titleBar = (TitleBar) findViewById(R.id.titlebar);
    titleBar.setTitleBarClickListener(titleBarClickListener);
    titleBar.setTitle(getClass().getSimpleName());

    mETInput.setText(mDefaultUrl);
  }

  @BindClick({R.id.btn_clear, R.id.btn_start, R.id.btn_stop, R.id.btn_play
          , R.id.btn_del})
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_clear:
        mETInput.setText("");
        break;
      case R.id.btn_start:
        download();
        break;
      case R.id.btn_stop:
        if (mWorker != null) mWorker.stop();
        break;
      case R.id.btn_play:
        startPlayer();
        break;
      case R.id.btn_del:
        delFile();
        break;
    }
  }

  private void startPlayer() {
    if (M3u8Util.getFileLength(mPath) <= 0) {
      mTVStatus.setText("file invalid");
      return;
    }
    File file = new File(mPath);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri data;
    // 判断版本大于等于7.0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      // "dhl.anddemo.fileprovider" 即是在清单文件中配置的 authorities
      data = FileProvider.getUriForFile(getApplicationContext(), "dhl.anddemo.fileprovider", file);
      // 给目标应用一个临时授权
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    } else {
      data = Uri.fromFile(file);
    }
    intent.setDataAndType(data, "video/*");
    startActivity(intent);
  }

  private void delFile() {
    String url = mETInput.getText().toString().trim();
    if (url.isEmpty()) {
      url = mDefaultUrl;
    }
    String path = M3u8Util.joinPath(mSaveDir, M3u8Util.getSaveName(url)) + ".ts";

    M3u8Util.deleteFile(path);
    String tsDir = M3u8Util.getTsDir(path);
    M3u8Util.deleteDir(tsDir);
  }

  String mPath = null;
  M3u8Downloader mWorker;

  private void download() {

    String url = mETInput.getText().toString().trim();
    if (url.isEmpty()) {
      url = mDefaultUrl;
    }
    mPath = M3u8Util.joinPath(mSaveDir, M3u8Util.getSaveName(url)) + ".ts";
    if (M3u8Util.getFileLength(mPath) > 0) {
      mTVStatus.setText("already download");
      return;
    }
    mWorker = new M3u8Downloader(url, mPath);
    mWorker.setExecutorService(mThreadPoolExecutor);
    mWorker.setDownloadThreadCount(mDownloadThreadCount);
    mWorker.setListener(new M3u8DownloadListener() {
      @Override
      public void onStart(String id) {
        App.postToUiThread(new Runnable() {
          @Override
          public void run() {
            mTVStatus.setText("Download start");
          }
        });
      }

      @Override
      public void onTotalSizeConfirmed(String id, long length) {

      }

      @Override
      public void onStop(String id) {
        App.postToUiThread(new Runnable() {
          @Override
          public void run() {
            mTVStatus.setText("Download stop");
          }
        });
      }

      @Override
      public void onProgress(String id, final long length, final long downloadLength) {
        M3u8Util.log("progress:", downloadLength + "", "/", length + "");
        App.postToUiThread(new Runnable() {
          @Override
          public void run() {
            mTVStatus.setText(String.format("Download %.2f%%\n%d/%d", (double) downloadLength / length * 100, downloadLength, length));
          }
        });
      }

      @Override
      public void onFinish(String id) {
        App.postToUiThread(new Runnable() {
          @Override
          public void run() {
            mTVStatus.setText("Download finish");
          }
        });
      }

      @Override
      public void onError(String id, M3u8DownloadException error) {
        App.postToUiThread(new Runnable() {
          @Override
          public void run() {
            mTVStatus.setText("Download error");
          }
        });
      }
    });
    mWorker.start();
  }

}
