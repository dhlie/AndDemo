package dhl.anddemo.m3u8;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import dhl.anddemo.base.BaseActivity;
import dhl.anddemo.base.TitleBar;
import dhl.annotation.viewbinding.BindClick;
import dhl.annotation.viewbinding.BindView;
import dhl.m3u8download.M3u8DownloadException;
import dhl.m3u8download.M3u8DownloadListener;
import dhl.m3u8download.M3u8DownloadWorker;
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

	//master:https://www.dailymotion.com/cdn/manifest/video/x7mwvsh.m3u8?sec=D5vghDNHRkqU5NAhhFipC4VPvgenRRQ5MkKvuzCLriw8okmCDqHvJiP4fF_9755e1tZPkzqkRxnAYArwuoFx5g
	//media:https://proxy-34.sg1.dailymotion.com/sec(_MR2-nuL8DGH1FsPGkJ4SSRJHvdlcNIw7vCcbfTERTfWeSldR6YUCfSG7pHXPgc-zUL0O82qifJ7DyLevTWqCv8hMeN2ZPympnosDRqwL-E)/video/779/847/461748977_mp4_h264_aac_l2.m3u8
	private String mDefaultUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/gear0/prog_index.m3u8";

	ExecutorService mThreadPoolExecutor = new ThreadPoolExecutor(
					10,
					20,
					5,
					TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
	String mSaveDir = "/sdcard/hls";

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
		Intent intent = new Intent(Intent.ACTION_VIEW);
		File file = new File(mPath);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "video/*");
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
	M3u8DownloadWorker mWorker;

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
		mWorker = new M3u8DownloadWorker(url, mPath);
		mWorker.setExecutorService(mThreadPoolExecutor);
		mWorker.setListener(new M3u8DownloadListener() {
			@Override
			public void onStart(String id) {
				mTVStatus.setText("Download start");
			}

			@Override
			public void onStop(String id) {
				mTVStatus.setText("Download stop");
			}

			@Override
			public void onProgress(String id, long length, long downloadLength) {
				M3u8Util.log("progress", length + "", downloadLength + "");
				mTVStatus.setText(String.format("Download %.2f%%\n%d/%d", (double) downloadLength / length * 100, downloadLength, length));
			}

			@Override
			public void onFinish(String id) {
				mTVStatus.setText("Download finish");
			}

			@Override
			public void onError(String id, M3u8DownloadException error) {
				mTVStatus.setText("Download error");
			}
		});
		mWorker.start();
	}

}
