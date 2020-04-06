package dhl.m3u8download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dhl.m3u8download.model.Key;
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
public class M3u8Downloader implements Callable, SegmentDownloader.SegmentDownloadListener {
  private static final int DOWNLOAD_THREAD_COUNT = 4;
  private static final int MAX_RETRY_TIMES = 2;
  private static final int PROGRESS_NOTIFY_INTERVAL = 200;

  private ExecutorService executorService;
  private final String urlString;
  private final String filePath;
  private List<SegmentDownloader> allTasks = new ArrayList<>();
  private List<SegmentDownloader> runningTasks = new ArrayList<>();
  private M3u8DownloadListener listener;
  private Future future;
  private volatile boolean stop;
  private long totalLength;
  private volatile long downloadLength;
  private MediaPlaylist playlist;
  private int retryTimes;
  private volatile long lastNotifyTime;
  private int downloadThreadCount = DOWNLOAD_THREAD_COUNT;
  private volatile int delay = 0;

  public M3u8Downloader(String url, String filePath) {
    this.urlString = url;
    this.filePath = filePath;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void setListener(M3u8DownloadListener listener) {
    this.listener = listener;
  }

  public void setDownloadThreadCount(int threadCount) {
    downloadThreadCount = threadCount;
  }

  public String getTsDir() {
    if (filePath != null && filePath.endsWith(".download")) {
      return M3u8Util.getTsDir(filePath.substring(0, filePath.lastIndexOf('.')));
    }
    return M3u8Util.getTsDir(filePath);
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
      if (future != null) {
        future.cancel(true);
      }
      for (SegmentDownloader task : runningTasks) {
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

      String tsDir = getTsDir();
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

      String playlistUri = masterPlaylist.getResUrl(stream.getUri());
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

    String tsDir = getTsDir();
    if (tsDir == null || tsDir.isEmpty()) {
      throw new M3u8DownloadException(M3u8DownloadException.ERRNO_ERROR_SAVE_PATH, "getTsDir fail:" + filePath);
    }

    this.playlist = playlist;
    totalLength = SegmentDownloader.calculateLength(playlist.getMediaSegments().size() + 1/**merge step*/);
    notifyTotalSize();
    List<MediaSegment> mediaSegments = playlist.getMediaSegments();
    int size = mediaSegments.size();
    int perTaskCount = size / downloadThreadCount;
    perTaskCount = perTaskCount == 0 ? 1 : perTaskCount;
    FlingRequest flingRequest = new FlingRequest();
    for (int i = 0; i < downloadThreadCount; i++) {
      int startIndex = i * perTaskCount;
      if (startIndex >= size) {
        break;
      }
      int endIndex = i == (downloadThreadCount - 1) ? size - 1 : startIndex + perTaskCount - 1;
      endIndex = Math.min(size - 1, endIndex);

      synchronized (this) {
        if (stop) {
          break;
        }

        SegmentDownloader task = new SegmentDownloader(i, tsDir, startIndex, endIndex, playlist);
        allTasks.add(task);
        runningTasks.add(task);
        task.setExecutorService(executorService);
        task.setFlingRequest(flingRequest);
        task.setDownloadListener(this);
        task.start();
      }
    }
  }

  private void mergeMediaSegments() throws M3u8DownloadException {
    if (playlist == null || playlist.getMediaSegments() == null || playlist.getMediaSegments().isEmpty()) {
      throw new M3u8DownloadException(M3u8DownloadException.ERRNO_UNKNOWN, "Playlist invalid");
    }

    String tmpFile = filePath + ".tmp";
    OutputStream out = null;
    try {
      out = new FileOutputStream(tmpFile);
      for (MediaSegment segment : playlist.getMediaSegments()) {

				Key key = segment.getKey();
				if (key != null && !SegmentDownloader.ENCRYPT_METHOD_NONE.equals(key.getMethod())) {
					decryptMediaSegment(segment, out);
					continue;
				}

				readeSegmentToStream(segment, out);
      }
      M3u8Util.rename(tmpFile, filePath);
      if (M3u8Util.getFileLength(filePath) <= 0) {
        M3u8Util.deleteFile(tmpFile);
        M3u8Util.deleteFile(filePath);
        throw new M3u8DownloadException(M3u8DownloadException.ERRNO_MERGE_SEGMENTS, "rename fail");
      }
      M3u8Util.deleteDir(getTsDir());
    } catch (M3u8DownloadException e) {
      throw e;
    } catch (Exception e) {
      throw new M3u8DownloadException(e);
    } finally {
      M3u8Util.close(out);
    }
  }

  private void readeSegmentToStream(MediaSegment segment, OutputStream out) throws Exception {
		String tsDir = getTsDir();
		String uri = playlist.getResUrl(segment.getUri());
		String name = M3u8Util.getSaveName(uri);
		String tsPath = M3u8Util.joinPath(tsDir, name);
		File tsFile = new File(tsPath);
		if (!tsFile.exists() || !tsFile.isFile()) {
			throw new M3u8DownloadException(M3u8DownloadException.ERRNO_MERGE_SEGMENTS, "merge fail:ts file invalid");
		}

		FileInputStream fis = null;
		byte[] buf = null;
		try {
			fis = new FileInputStream(tsFile);
			buf = M3u8Util.getBuffer();
			if (segment.getRangeStart() > 0) {
				fis.skip(segment.getRangeStart());
			}
			int readLen = 0;
			int len = -1;
			int rangeLength = segment.getRangeLength();
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
			M3u8Util.putBuffer(buf);
		}
	}

  private void decryptMediaSegment(MediaSegment segment, OutputStream out) throws Exception{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		readeSegmentToStream(segment, bos);
		byte[] contentByte = bos.toByteArray();

		Key key = segment.getKey();
		String keyName = M3u8Util.getSaveName(playlist.getResUrl(key.getUri()));
		String keyPath = M3u8Util.joinPath(getTsDir(), keyName);
		String keyStr = M3u8Util.readFile(keyPath);
		if (keyStr == null || keyStr.length() == 0) {
			M3u8Util.deleteFile(keyPath);
			throw new M3u8DownloadException(M3u8DownloadException.ERRNO_DOWNLOAD_FILE_INVALID, "key file is invalid");
		}
		String ivStr = key.getIv();
		if (ivStr == null || ivStr.length() == 0) {
			ivStr = makeEncryptIV(playlist.getMediaSegmentSequence(segment));
		}

		byte[] decryptBytes = decrypt(contentByte, keyStr.getBytes("utf-8"), ivStr.getBytes("utf-8"));
		out.write(decryptBytes);
	}

	private byte[] decrypt(byte[] content, byte[] key, byte[] iv) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(key ,"AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		return cipher.doFinal(content);
	}

	private String makeEncryptIV(int seq) {
		int ivLength = 16;
		String str = Integer.toBinaryString(seq);
		StringBuilder sb = new StringBuilder();
		for (int i = 0, j = ivLength - str.length(); i < j; i++) {
			sb.append('0');
		}
		sb.append(str);
		return sb.toString();
	}

  @Override
  public void onProgress(SegmentDownloader worker, int start, int length, int downLength) {
    //M3u8Util.log("task:", String.valueOf(worker.getSeq()), "start", String.valueOf(start), "length", String.valueOf(length), "down", String.valueOf(downLength));
    long time = System.currentTimeMillis();
    if (time - lastNotifyTime >= PROGRESS_NOTIFY_INTERVAL) {
      synchronized (this) {
        lastNotifyTime = time;
      }

      notifyProgress();
    } else {
      if (time <= lastNotifyTime) {
        return;
      }
      int dt = (int) (time - lastNotifyTime);
      synchronized (this) {
        delay = PROGRESS_NOTIFY_INTERVAL - dt;
        executorService.submit(progressRunnable);
        lastNotifyTime = lastNotifyTime + PROGRESS_NOTIFY_INTERVAL;
      }
    }
  }

  @Override
  public void onFinished(SegmentDownloader worker) {
    M3u8Util.log("task:", worker.getSeq() + "", "finished");
    boolean allFinish;
    synchronized (this) {
      runningTasks.remove(worker);
      allFinish = runningTasks.isEmpty();
    }

    if (allFinish) {
      try {
        M3u8Util.log("all finished, merge segments");
        mergeMediaSegments();
        synchronized (this) {
          downloadLength = totalLength;
        }

        M3u8Util.log("task success");
        notifyProgress();
        notifyFinish();
      } catch (M3u8DownloadException e) {
        onError(worker, e);
      }
    }
  }

  @Override
  public void onError(SegmentDownloader worker, final M3u8DownloadException error) {
    synchronized (this) {
    	if (stop) {
    		return;
			}
      for (SegmentDownloader task : runningTasks) {
        task.stop();
      }
			notifyError(error);
      stop = true;
		}
  }

  private void notifyStart() {
    if (listener != null) {
      listener.onStart(urlString);
    }
  }

  private void notifyTotalSize() {
    if (listener != null) {
      listener.onTotalSizeConfirmed(urlString, totalLength);
    }
  }

  private void notifyStop() {
    if (listener != null) {
      listener.onStop(urlString);
    }
  }

  private void notifyProgress() {
    M3u8Util.log("notifyProgress");
    synchronized (this) {
      if (downloadLength < totalLength) {
        long totalDownLength = 0;
        for (SegmentDownloader task : allTasks) {
          totalDownLength += task.getDownloadLength();
        }
        downloadLength = totalDownLength;
				listener.onProgress(urlString, totalLength, downloadLength);
			}
    }
  }

  private Runnable progressRunnable = new Runnable() {
    @Override
    public void run() {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      notifyProgress();
    }
  };

  private void notifyFinish() {
    if (listener != null) {
      listener.onFinish(urlString);
    }
  }

  private void notifyError(final M3u8DownloadException error) {
    if (stop) {
      M3u8Util.log("Task stoped");
      return;
    }

		error.printStackTrace();
    if (listener != null) {
      listener.onError(urlString, error);
    }
  }

}
