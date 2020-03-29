package dhl.anddemo.base.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by DuanHl on 2017/11/9.
 */

public class Dirs {

	/**
	 * 应用在sdcard上的存储根目录
	 *
	 * @return
	 */
	private static String getSDCardCacheDir() {
		return Environment.getExternalStorageDirectory().getPath() + "/AndDemo";
	}

	/**
	 * 创建目录
	 *
	 * @param dir :要创建的目录路径
	 * @return :目录
	 */
	public static File createDirs(String dir) {
		if (dir == null || dir.length() == 0) return null;
		File file = new File(dir);
		if (!file.exists()) {
			return file.mkdirs() ? file : null;
		} else {
			return file;
		}
	}

	/**
	 * 应用在sdcard上的 cache 目录
	 *
	 * @return
	 */
	public static String getCacheDir() {
		return getSDCardCacheDir() + "/cache";
	}

	/**
	 * 应用在sdcard上的 tmp 目录
	 *
	 * @return
	 */
	public static String getTmpDir() {
		return getSDCardCacheDir() + "/tmp";
	}
}
