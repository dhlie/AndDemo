package dhl.m3u8download;

import dhl.m3u8download.model.Key;
import dhl.m3u8download.model.MediaPlaylist;
import dhl.m3u8download.model.MediaSegment;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * Author: duanhl
 * Create: 2020-03-27 10:20
 * Description:
 */
public class SegmentDownloader implements Callable {

  interface SegmentDownloadListener {
    void onProgress(SegmentDownloader worker, int start, int length, int downloadLength);

    void onFinished(SegmentDownloader worker);

    void onError(SegmentDownloader worker, M3u8DownloadException e);
  }

  private static final int MAX_RETRY_TIMES = 2;

	public static final String ENCRYPT_METHOD_NONE = "NONE";

	public static final String ENCRYPT_METHOD_AES = "AES-128";

	public static final String ENCRYPT_METHOD_SAMPLE_AES = "SAMPLE-AES";

  private ExecutorService executorService;
  private MediaPlaylist playlist;
  private int startIndex;//下载范围 [startIndex, endIndex]
  private int endIndex;
  private String tsDir;
  private Future future = null;
  private volatile boolean stop;
  private int start;
  private int length;
  private int downloadLength;
  private SegmentDownloadListener listener;
  private FlingRequest flingRequest;
  private int seq;
  private int retryTimes;

  public SegmentDownloader(int seq, String saveDir, int startIndex, int endIndex, MediaPlaylist playlist) {
    this.seq = seq;
    this.playlist = playlist;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    start = calculateStart(startIndex);
    length = calculateLength(endIndex - startIndex + 1);
    tsDir = saveDir;
  }

  public void setExecutorService(ExecutorService service) {
    executorService = service;
  }

  public void setFlingRequest(FlingRequest flingRequest) {
    this.flingRequest = flingRequest;
  }

  private MediaSegment getMediaSegment(int index) {
    return playlist.getMediaSegments().get(index);
  }

  public void setDownloadListener(SegmentDownloadListener listener) {
    this.listener = listener;
  }

  public int getSeq() {
    return seq;
  }

  public static int calculateStart(int startIndex) {
    return startIndex;
  }

  public static int calculateLength(int count) {
    return count;
  }

  public boolean isStart() {
    synchronized (this) {
      return future != null;
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
      stop = true;
      if (future != null) {
        future.cancel(true);
      }
    }
  }

  @Override
  public Object call() throws Exception {
    synchronized (this) {
      if (stop) {
        return "";
      }
    }

    try {
      for (int i = startIndex; i <= endIndex; i++) {
        synchronized (this) {
          if (stop) {
            M3u8Util.log("Task stop.", startIndex + "", endIndex + "", i + "");
            return "";
          }
        }

        MediaSegment mediaSegment = getMediaSegment(i);

				//download key
				Key key = mediaSegment.getKey();
				if (key != null && !ENCRYPT_METHOD_NONE.equals(key.getMethod())) {
					String keyUri = playlist.getResUrl(key.getUri());
					String keyName = M3u8Util.getSaveName(keyUri);
					if (keyName == null || keyName.isEmpty()) {
						throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getSaveName error, uri:" + keyUri);
					}
					String keyPath = M3u8Util.joinPath(tsDir, keyName);

					if (!flingRequest.isFlingAndMakeInFling(keyUri)) {
						try {
							if (!M3u8Util.isCacheValid(keyPath)) {
								M3u8Util.log(String.valueOf(seq), "download key", keyUri);
								HttpDownloader.download(keyUri, keyPath);
								if (M3u8Util.getFileLength(keyPath) <= 0L) {
									M3u8Util.deleteFile(keyPath);
									throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "download key failed");
								}
							}
						} finally {
							flingRequest.removeRequest(keyUri);
						}
					}
				}

				//download ts
				String uri = playlist.getResUrl(mediaSegment.getUri());
				String name = M3u8Util.getSaveName(uri);
				if (name == null || name.isEmpty()) {
					throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getSaveName error, uri:" + uri);
				}
				String tsPath = M3u8Util.joinPath(tsDir, name);

        if (flingRequest.isFlingAndMakeInFling(uri)) {
          int length = calculateLength(i - startIndex + 1);
          notifyProgress(length);
          continue;
        }

        try {
          if (!M3u8Util.isCacheValid(tsPath)) {
						M3u8Util.log(String.valueOf(seq), "download ts", uri);
						HttpDownloader.download(uri, tsPath);
					}
        } finally {
          flingRequest.removeRequest(uri);
        }
        long tsLen = (int) M3u8Util.getFileLength(tsPath);
        if (tsLen > 0) {
          int downloadedLength = calculateLength(i - startIndex + 1);
          notifyProgress(downloadedLength);
        } else {
          M3u8Util.deleteFile(tsPath);
          throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "download Media Segment failed");
        }
      }
      notifyFinished();
    } catch (M3u8DownloadException e) {
      notifyError(e);
    } catch (Exception e) {
      notifyError(new M3u8DownloadException(e));
    }
    return "";
  }

  private void notifyProgress(int downLength) {
    if (downLength > downloadLength) {
      downloadLength = downLength;
      if (listener != null) {
        listener.onProgress(this, start, length, downloadLength);
      }
    }
  }

  private void notifyFinished() {
    if (listener != null) {
      listener.onFinished(this);
    }
  }

  private void notifyError(M3u8DownloadException e) {
    synchronized (this) {
      if (canRetry()) {
        e.printStackTrace();
        M3u8Util.log(String.valueOf(seq), "retry", String.valueOf(retryTimes));

        future = null;
        start();
        return;
      }
    }

    if (listener != null) {
      listener.onError(this, e);
    }
  }

  private boolean canRetry() {
    synchronized (this) {
      retryTimes++;
      return !stop && retryTimes <= MAX_RETRY_TIMES;
    }
  }

  public long getStart() {
    return start;
  }

  public long getLength() {
    return length;
  }

  public long getDownloadLength() {
    return downloadLength;
  }
}
