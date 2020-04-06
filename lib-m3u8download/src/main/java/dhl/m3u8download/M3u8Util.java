package dhl.m3u8download;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * Author: duanhl
 * Create: 2020-03-26 19:50
 * Description:
 */
public class M3u8Util {

  public static final String MEDIA_SUFFIX = "ts";
  private static final String FILE_SUFFIX = ".ts";

  public static final int MAX_REDIRECTS = 5;

  public static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);

  private static final long CACHE_EXPIRE = TimeUnit.DAYS.toMillis(2);

  private static final int BUF_SIZE = 8 << 10;

  private static final String M3U8_DIR = "m3u8/";

  private static final boolean DEBUG = true;

  private static final String TAG = "M3u8Util";

  private static final LruCache<Integer, byte[]> ioBuffer = new LruCache<>(4);

  public static boolean isHLSFile(String path) {
    return path != null && path.endsWith(FILE_SUFFIX);
  }

  public static boolean isHLSUrl(String url) {
    if (url == null || url.length() == 0) {
      return false;
    }
    Uri uri = Uri.parse(url);
    String path = uri.getPath();
    return path != null && (path.endsWith(".m3u8") || path.endsWith(".m3u"));
  }

  public static void log(String... msg) {
    if (!DEBUG || msg == null) {
      return;
    }
    if (msg.length == 1) {
      Log.i(TAG, msg[0]);
      return;
    }
    StringBuilder sb = new StringBuilder();
    for (String s : msg) {
      sb.append(s).append(" ");
    }
    Log.i(TAG, sb.toString());
  }

  public static synchronized byte[] getBuffer() {
    byte[] buf = ioBuffer.remove(BUF_SIZE);
    if (buf == null) {
      buf = new byte[BUF_SIZE];
    }
    return buf;
  }

  public static synchronized void putBuffer(byte[] buf) {
    if (buf != null && buf.length == BUF_SIZE) {
      ioBuffer.put(buf.length, buf);
    }
  }

  public static String getTsDir(String filePath) {
    if (filePath == null || filePath.isEmpty()) {
      return filePath;
    }
    String subDir = getSaveName(filePath);
    if (subDir == null || subDir.isEmpty()) {
      return subDir;
    }
    File file = new File(filePath);
    File tsDir = new File(file.getParentFile(), M3U8_DIR + subDir);
    return tsDir.getAbsolutePath();
  }

  public static void deleteM3u8TempFile(String path) {
    if (path == null || !path.endsWith(FILE_SUFFIX)) {
      return;
    }
    deleteDir(getTsDir(path));
  }

  public static String joinPath(String basePath, String appendPath) {
    if (TextUtils.isEmpty(basePath) || TextUtils.isEmpty(appendPath)) {
      return null;
    }
    if (appendPath.startsWith(File.separator)) {
      return appendPath;
    }
    if (basePath.endsWith(File.separator)) {
      return basePath + appendPath;
    } else {
      return basePath + File.separator + appendPath;
    }
  }

  public static String getSaveName(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    String name = null;
    try {
      name = md5Digest(url);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return name;
  }

  public static String md5Digest(String input) throws NoSuchAlgorithmException {
    if (input == null) {
      return null;
    }
    MessageDigest digest = MessageDigest.getInstance("MD5");
    digest.update(input.getBytes());
    return getHexString(digest.digest());
  }

  public static String getHexString(byte[] digest) {
    BigInteger bi = new BigInteger(1, digest);
    return String.format("%032x", bi);
  }

  public static boolean isCacheValid(String path) {
    if (path == null) {
      return false;
    }
    File file = new File(path);
    return file.exists()
            && file.length() > 0
            && (file.lastModified() + CACHE_EXPIRE) > System.currentTimeMillis();
  }

  public static void deleteFile(String path) {
    if (path == null) {
      return;
    }
    File file = new File(path);
    if (file.isFile() && file.exists()) {
      if (!file.delete()) {
        File tmp = new File(file.getAbsolutePath() + System.currentTimeMillis());
        File del = file.renameTo(tmp) ? tmp : file;
        del.delete();
      }
    }
  }

  public static void deleteDir(String dirPath) {
    if (dirPath == null) {
      return;
    }
    File dir = new File(dirPath);
    if (dir.isDirectory() && dir.exists()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            deleteDir(file.getAbsolutePath());
          } else {
            file.delete();
          }
        }
      }
    }
    dir.delete();
  }

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static boolean rename(String src, String dst) {
    if (src == null || dst == null) {
      return false;
    }
    File srcFile = new File(src);
    File dstFile = new File(dst);
    if (dstFile.exists()) {
      dstFile.delete();
    }
    if (!dstFile.getParentFile().exists()) {
      dstFile.getParentFile().mkdirs();
    }
    return srcFile.renameTo(dstFile);
  }

  public static long getFileLength(String path) {
    if (path == null) {
      return -1;
    }
    File file = new File(path);
    if (file.exists() && file.isFile()) {
      return file.length();
    }
    return -1;
  }
}
