package dhl.m3u8download;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import dhl.m3u8download.model.MediaPlaylist;
import dhl.m3u8download.model.MediaSegment;

/**
 * Author: duanhl
 * Create: 2020-03-27 10:20
 * Description:
 */
public class M3u8DownloadTask implements Callable {

  interface DownloadListener {
    void onProgress(M3u8DownloadTask worker, long start, long length, long downloadLength);
    void onFinished(M3u8DownloadTask worker);
    void onError(M3u8DownloadTask worker, M3u8DownloadException e);
  }

  private static final long SEGMENT_LENGTH = 1000;
  private static final int MAX_RETRY_TIMES = 2;

  private ExecutorService executorService;
  private MediaPlaylist playlist;
  private int startIndex;//下载范围 [startIndex, endIndex]
  private int endIndex;
  private String tsDir;
  private Future future = null;
  private volatile boolean stop;
  private long start;
  private long length;
  private long downloadLength;
  private DownloadListener listener;
  private List<String> flingUris;
  private int seq;
  private int retryTimes;

  public M3u8DownloadTask(int seq, String saveDir, int startIndex, int endIndex, MediaPlaylist playlist) {
    this.seq = seq;
    this.playlist = playlist;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    start = M3u8DownloadTask.calculateStart(startIndex);
    length = M3u8DownloadTask.calculateLength(endIndex - startIndex + 1);
    tsDir = saveDir;
  }

  public void setExecutorService(ExecutorService service) {
    executorService = service;
  }

  public void setFlingUris(List<String> flingUris) {
    this.flingUris = flingUris;
  }

  private MediaSegment getMediaSegment(int index) {
    return playlist.getMediaSegments().get(index);
  }

  public void setDownloadListener(DownloadListener listener) {
    this.listener = listener;
  }

  public int getSeq() {
    return seq;
  }

  public static long calculateStart(int startIndex) {
    return startIndex * SEGMENT_LENGTH;
  }

  public static long calculateLength(int count) {
    return count * SEGMENT_LENGTH;
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
        String uri = playlist.getMediaSegmentUrl(mediaSegment);
        String name = M3u8Util.getSaveName(uri);
        if (name == null || name.isEmpty()) {
          throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getSaveName error, uri:" + uri);
        }
        String tsPath = M3u8Util.joinPath(tsDir, name);

        synchronized (M3u8DownloadTask.class) {
          if (flingUris.contains(uri) || M3u8Util.isCacheValid(tsPath)) {
            long length = calculateLength(i - startIndex + 1);
            notifyProgress(length);
            continue;
          }
          flingUris.add(uri);
        }

        try {
          M3u8Util.log(String.valueOf(seq), "download", uri);
          HttpDownloader.download(uri, tsPath);
        } finally {
          synchronized (M3u8DownloadTask.class) {
            flingUris.remove(uri);
          }
        }
        if (M3u8Util.getFileLength(tsPath) > 0) {
          long length = calculateLength(i - startIndex + 1);
          notifyProgress(length);
        } else {
          M3u8Util.deleteFile(tsPath);
          throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "");
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

  private void notifyProgress(long downLength) {
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
