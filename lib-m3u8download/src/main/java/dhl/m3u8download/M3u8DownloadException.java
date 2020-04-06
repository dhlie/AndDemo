package dhl.m3u8download;

/**
 * Author: duanhl
 * Create: 2020-03-27 11:34
 * Description:
 */
public class M3u8DownloadException extends Exception {

  public static final int ERRNO_UNKNOWN = 0;
  public static final int ERRNO_ERROR_SAVE_PATH = 1;
  public static final int ERRNO_DOWNLOAD_FILE_INVALID = 2;
  public static final int ERRNO_ERROR_URL = 3;
  public static final int ERRNO_NOT_SUPPORT_LIVE_STREAM = 4;
  public static final int ERRNO_PARSE_SUBRANGE_SEGMENT = 5;
  public static final int ERRNO_MERGE_SEGMENTS = 6;
  public static final int ERRNO_ENCRYPT_STREAM = 7;

  private int errno;

  public M3u8DownloadException(Exception e) {
    super("[" + ERRNO_UNKNOWN + "] ", e);
    errno = ERRNO_UNKNOWN;
  }

  public M3u8DownloadException(int code, String message) {
    super("[" + code + "] " + message);
    errno = code;
  }

  public M3u8DownloadException(int code, Throwable e) {
    super("[" + code + "] ", e);
    errno = code;
  }

  public int getErrorCode() {
    return errno;
  }

}
