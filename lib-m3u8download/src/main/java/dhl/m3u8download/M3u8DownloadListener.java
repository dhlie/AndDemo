package dhl.m3u8download;

/**
 * Author: duanhl
 * Create: 2020-03-27 18:43
 * Description:
 */
public interface M3u8DownloadListener {
	void onStart(String id);

	void onStop(String id);

	void onProgress(String id, long length, long downloadLength);

	void onFinish(String id);

	void onError(String id, M3u8DownloadException error);
}
