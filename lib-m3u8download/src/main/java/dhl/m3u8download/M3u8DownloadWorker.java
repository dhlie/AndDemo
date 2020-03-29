package dhl.m3u8download;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import dhl.m3u8download.model.MasterPlaylist;
import dhl.m3u8download.model.MediaPlaylist;
import dhl.m3u8download.model.MediaSegment;
import dhl.m3u8download.model.Playlist;
import dhl.m3u8download.model.VariantStream;

/**
 * Author: duanhl
 * Create: 2020-03-25 16:28
 * Description:
 */
public class M3u8DownloadWorker implements Callable, M3u8DownloadTask.DownloadListener {
	private static final int DOWNLOAD_THREAD_COUNT = 4;
	private static final int MAX_RETRY_TIMES = 2;
	private static final int PROGRESS_NOTIFY_INTERVAL = 200;

	private ExecutorService executorService;
	private final String urlString;
	private final String filePath;
	private List<M3u8DownloadTask> allTasks = new ArrayList<>();
	private List<M3u8DownloadTask> runningTasks = new ArrayList<>();
	private M3u8DownloadListener listener;
	private Future future;
	private boolean stop;
	private long totalLength;
	private volatile long downloadLength;
	private MediaPlaylist playlist;
	private Handler handler = new Handler(Looper.getMainLooper());
	private int retryTimes;
	private volatile long lastNotifyTime;

	public M3u8DownloadWorker(String url, String filePath) {
		this.urlString = url;
		this.filePath = filePath;
	}

	public void setExecutorService(ExecutorService service) {
		executorService = service;
	}

	public void setListener(M3u8DownloadListener listener) {
		this.listener = listener;
	}

	private boolean canRetry() {
		synchronized (this) {
			retryTimes++;
			return !stop && retryTimes <= MAX_RETRY_TIMES;
		}
	}

	public void start() {
		synchronized (this) {
			if (future == null) {
				future = executorService.submit(this);
			}
		}
	}

	public void stop() {
		synchronized (this) {
			for (M3u8DownloadTask task : runningTasks) {
				task.stop();
			}
			stop = true;
		}
		notifyStop();
	}

	@Override
	public Object call() throws Exception {
		try {
			synchronized (this) {
				if (stop) {
					return "";
				}
			}
			notifyStart();

			downloadPlaylist();
			downloadMediaSegments(playlist);
		} catch (M3u8DownloadException e) {
			notifyError(e);
		} catch (Exception e) {
			notifyError(new M3u8DownloadException(e));
		}
		return "";
	}

	private void downloadPlaylist() throws Exception {
		try {
			String savePath = download(urlString);
			parsePlaylist(urlString, savePath);
		} catch (Exception e) {
			if (canRetry()) {
				downloadPlaylist();
			} else {
				throw e;
			}
		}
	}

	private String download(String url) throws M3u8DownloadException {
		try {
			if (url == null || url.isEmpty()) {
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_URL, "url is empty");
			}

			String saveName = M3u8Util.getSaveName(url);
			if (saveName == null) {
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getSaveName error, url:" + url);
			}

			String tsDir = M3u8Util.getTsDir(filePath);
			String savePath = M3u8Util.joinPath(tsDir, saveName);
			if (M3u8Util.isCacheValid(savePath)) {
				return savePath;
			}

			M3u8Util.log("download:", url);
			HttpDownloader.download(url, savePath);
			if (M3u8Util.getFileLength(savePath) > 0) {
				return savePath;
			} else {
				M3u8Util.deleteFile(savePath);
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "file length invalid");
			}
		} catch (Exception e) {
			if (e instanceof M3u8DownloadException) {
				throw (M3u8DownloadException) e;
			} else {
				throw new M3u8DownloadException(e);
			}
		}
	}

	private void parsePlaylist(String url, String path) throws Exception {
		Playlist playlist = null;
		try {
			synchronized (this) {
				if (stop) {
					return;
				}
			}
			playlist = PlaylistParser.parsePlaylist(path);
		} catch (Exception e) {
			M3u8Util.deleteFile(path);
			throw e;
		}
		if (playlist instanceof MasterPlaylist) {
			MasterPlaylist masterPlaylist = (MasterPlaylist) playlist;
			if (masterPlaylist.getVariantStreams() == null || masterPlaylist.getVariantStreams().isEmpty()) {
				M3u8Util.deleteFile(path);
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "Master Playlist file invalid");
			}
			masterPlaylist.setUrl(url);
			List<VariantStream> variantStreams = masterPlaylist.getVariantStreams();
			Collections.sort(variantStreams, new Comparator<VariantStream>() {
				@Override
				public int compare(VariantStream o1, VariantStream o2) {
					return o1.getBandwidth() - o2.getBandwidth();
				}
			});
			VariantStream stream = variantStreams.get(variantStreams.size() / 2);

			String playlistUri = masterPlaylist.getPlaylistUrl(stream);
			synchronized (this) {
				if (stop) {
					return;
				}
			}
			String savePath = download(playlistUri);
			parsePlaylist(playlistUri, savePath);
		} else {
			MediaPlaylist mediaPlaylist = (MediaPlaylist) playlist;
			if (mediaPlaylist == null || mediaPlaylist.getMediaSegments() == null || mediaPlaylist.getMediaSegments().isEmpty()) {
				M3u8Util.deleteFile(path);
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "Media Playlist fail invalid");
			}
			mediaPlaylist.setUrl(url);
			this.playlist = mediaPlaylist;
		}
	}

	private void downloadMediaSegments(MediaPlaylist playlist) throws M3u8DownloadException {
		if (!playlist.isVod()) {
			throw new M3u8DownloadException(M3u8DownloadException.ERRNO_NOT_SUPPORT_LIVE_STREAM, "Not support m3u8 live stream");
		}

		String tsDir = M3u8Util.getTsDir(filePath);
		if (tsDir == null || tsDir.isEmpty()) {
			throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getTsDir fail:" + filePath);
		}

		this.playlist = playlist;
		totalLength = M3u8DownloadTask.calculateLength(playlist.getMediaSegments().size() + 1/**merge step*/);
		List<MediaSegment> mediaSegments = playlist.getMediaSegments();
		int size = mediaSegments.size();
		int perTaskCount = size / DOWNLOAD_THREAD_COUNT;
		perTaskCount = perTaskCount == 0 ? 1 : perTaskCount;
		List<String> flingUris = new ArrayList<>();
		for (int i = 0; i < DOWNLOAD_THREAD_COUNT; i++) {
			int startIndex = i * perTaskCount;
			if (startIndex >= size) {
				break;
			}
			int endIndex = i == (DOWNLOAD_THREAD_COUNT - 1) ? size - 1 : startIndex + perTaskCount - 1;
			endIndex = Math.min(size - 1, endIndex);

			synchronized (this) {
				if (stop) {
					break;
				}

				M3u8DownloadTask task = new M3u8DownloadTask(i, tsDir, startIndex, endIndex, playlist);
				allTasks.add(task);
				runningTasks.add(task);
				task.setExecutorService(executorService);
				task.setFlingUris(flingUris);
				task.setDownloadListener(this);
				task.start();
			}
		}
	}

	@Override
	public void onProgress(M3u8DownloadTask worker, long start, long length, long downLength) {
		//M3u8Util.log("seq", String.valueOf(worker.getSeq()), "start", String.valueOf(start), "length", String.valueOf(length), "down", String.valueOf(downLength));

		long time = System.currentTimeMillis();
		if (time - lastNotifyTime >= PROGRESS_NOTIFY_INTERVAL) {
			synchronized (this) {
				lastNotifyTime = time;
			}
			notifyProgress(0);
		} else {
			if (time < lastNotifyTime) {
				return;
			}
			int dt = (int) (time - lastNotifyTime);
			notifyProgress(PROGRESS_NOTIFY_INTERVAL - dt);
			synchronized (this) {
				lastNotifyTime = lastNotifyTime + PROGRESS_NOTIFY_INTERVAL;
			}
		}
	}

	@Override
	public void onFinished(M3u8DownloadTask worker) {
		boolean allFinish;
		synchronized (this) {
			runningTasks.remove(worker);
			allFinish = runningTasks.isEmpty();
		}

		if (allFinish) {
			try {
				mergeMediaSegments();
				synchronized (this) {
					downloadLength = totalLength;
				}
				notifyProgress(0);
				notifyFinish();
			} catch (M3u8DownloadException e) {
				onError(worker, e);
			}
		}
	}

	@Override
	public void onError(M3u8DownloadTask worker, final M3u8DownloadException error) {
		synchronized (this) {
			if (stop) {
				error.printStackTrace();
				M3u8Util.log("Task cancel");
				return;
			}

			for (M3u8DownloadTask task : runningTasks) {
				task.stop();
			}
			stop = true;
		}
		notifyError(error);
	}

	private void mergeMediaSegments() throws M3u8DownloadException {
		if (playlist == null || playlist.getMediaSegments() == null || playlist.getMediaSegments().isEmpty()) {
			throw new M3u8DownloadException(M3u8DownloadException.ERRNO_UNKNOWN, "Playlist invalid");
		}

		String tmpFile = filePath + ".tmp";
		BufferedOutputStream out = null;
		byte[] buf = null;
		try {
			String tsDir = M3u8Util.getTsDir(filePath);
			out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			buf = M3u8Util.getBuffer();
			int len = -1;
			for (MediaSegment segment : playlist.getMediaSegments()) {
				FileInputStream fis = null;
				try {
					String uri = playlist.getMediaSegmentUrl(segment);
					String name = M3u8Util.getSaveName(uri);
					String tsPath = M3u8Util.joinPath(tsDir, name);
					File tsFile = new File(tsPath);
					if (!tsFile.exists() || !tsFile.isFile()) {
						throw new M3u8DownloadException(M3u8DownloadException.ERRNO_MERGE_SEGMENTS, "merge fail:ts file invalid");
					}

					fis = new FileInputStream(tsFile);
					if (segment.getRangeStart() > 0) {
						fis.skip(segment.getRangeStart());
					}
					int rangeLength = segment.getRangeLength();
					int readLen = 0;
					while ((len = fis.read(buf)) != -1) {
						if (rangeLength > 0 && readLen + len > rangeLength) {
							out.write(buf, 0, rangeLength - readLen);
							break;
						}
						out.write(buf, 0, len);
						readLen += len;
					}
				} finally {
					M3u8Util.close(fis);
				}
			}
			M3u8Util.rename(tmpFile, filePath);
			if (M3u8Util.getFileLength(filePath) <= 0) {
				M3u8Util.deleteFile(tmpFile);
				M3u8Util.deleteFile(filePath);
				throw new M3u8DownloadException(M3u8DownloadException.ERRNO_MERGE_SEGMENTS, "rename fail");
			}
			M3u8Util.deleteDir(tsDir);
		} catch (M3u8DownloadException e) {
			throw e;
		} catch (Exception e) {
			throw new M3u8DownloadException(e);
		} finally {
			M3u8Util.close(out);
			M3u8Util.putBuffer(buf);
		}
	}

	private void notifyStart() {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onStart(urlString);
				}
			});
		}
	}

	private void notifyStop() {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onStop(urlString);
				}
			});
		}
	}

	private void notifyProgress(int delay) {
		if (listener != null) {
			handler.postDelayed(progressRunnable, delay);
		}
	}

	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			if (downloadLength < totalLength) {
				long totalDownLength = 0;
				for (M3u8DownloadTask task : allTasks) {
					totalDownLength += task.getDownloadLength();
				}
				synchronized (this) {
					downloadLength = totalDownLength;
				}
			}
			listener.onProgress(urlString, totalLength, downloadLength);
		}
	};

	private void notifyFinish() {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					handler.removeCallbacks(progressRunnable);
					listener.onFinish(urlString);
				}
			});
		}
	}

	private void notifyError(final M3u8DownloadException error) {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onError(urlString, error);
				}
			});
		}
	}

}
