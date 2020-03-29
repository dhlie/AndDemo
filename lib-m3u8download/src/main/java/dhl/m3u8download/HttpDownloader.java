package dhl.m3u8download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Author: duanhl
 * Create: 2020-03-26 20:15
 * Description:
 */
public class HttpDownloader {

	private static final int HTTP_TEMP_REDIRECT = 307;

	public static void download(String reqUrl, String savePath) throws IOException {
		URL url = new URL(reqUrl);
		int redirectionCount = 0;
		while (redirectionCount++ < M3u8Util.MAX_REDIRECTS) {
			HttpURLConnection conn = null;
			File tmpFile = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setConnectTimeout(M3u8Util.DEFAULT_TIMEOUT);
				conn.setReadTimeout(M3u8Util.DEFAULT_TIMEOUT);
				int responseCode = conn.getResponseCode();
				switch (responseCode) {
					case HTTP_OK:
						InputStream in = null;
						FileOutputStream fos = null;
						tmpFile = new File(savePath + ".tmp");
						try {
							File parentFile = tmpFile.getParentFile();
							if (!parentFile.exists()) {
								parentFile.mkdirs();
							}

							in = conn.getInputStream();
							fos = new FileOutputStream(tmpFile);
							final byte[] buf = M3u8Util.getBuffer();
							int len = -1;
							while ((len = in.read(buf)) != -1) {
								fos.write(buf, 0, len);
							}
							M3u8Util.putBuffer(buf);
							boolean rename = M3u8Util.rename(tmpFile.getAbsolutePath(), savePath);
							if (!rename) {
								throw new IOException("rename fail, src:" + tmpFile.getAbsolutePath() + " dst:" + savePath);
							}
						} finally {
							M3u8Util.close(in);
							M3u8Util.close(fos);
						}
						return;
					case HTTP_MOVED_PERM:
					case HTTP_MOVED_TEMP:
					case HTTP_SEE_OTHER:
					case HTTP_TEMP_REDIRECT:
						final String location = conn.getHeaderField("Location");
						url = new URL(url, location);
						continue;
					default:
						throw new IOException("get Playlist error, status code " + responseCode);
				}
			} catch (IOException e) {
				if (tmpFile != null) {
					tmpFile.delete();
				}
				throw e;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
		throw new IOException("Too many redirects");
	}

}
