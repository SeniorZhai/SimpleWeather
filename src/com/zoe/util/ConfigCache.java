package com.zoe.util;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.zoe.app.Application;

public class ConfigCache {
	private static final String TAG = ConfigCache.class.getName();

	public static final int CONFIG_CACHE_MOBILE_TIMEOUT = 7200000; // 2 hour��3G������2Сʱˢ��
	public static final int CONFIG_CACHE_WIFI_TIMEOUT = 1800000; // 30 minute��wifi������30����ˢ��

	public static String getUrlCache(String url) {
		if (url == null) {
			return null;
		}

		String result = null;
		File file = new File(Application.getInstance().getCacheDir()
				+ File.separator + replaceUrlWithPlus(url));
		if (file.exists() && file.isFile()) {
			long expiredTime = System.currentTimeMillis() - file.lastModified();
			Log.d(TAG, file.getAbsolutePath() + " expiredTime:" + expiredTime
					/ 60000 + "min");
			// ��һϵͳʱ�䲻��ȷ��ʱ���ںܾ���ǰ
			// ������Ч��ֻ�ܶ�ȡ����
			if (Application.mNetWorkState != NetUtil.NETWORN_NONE
					&& expiredTime < 0) {
				return null;
			}
			if (Application.mNetWorkState == NetUtil.NETWORN_WIFI
					&& expiredTime > CONFIG_CACHE_WIFI_TIMEOUT) {
				return null;
			} else if (Application.mNetWorkState == NetUtil.NETWORN_MOBILE
					&& expiredTime > CONFIG_CACHE_MOBILE_TIMEOUT) {
				return null;
			}
			try {
				result = FileUtils.readTextFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static void setUrlCache(String data, String url) {
		if (Application.getInstance().getCacheDir() == null) {
			return;
		}
		File file = new File(Application.getInstance().getCacheDir()
				+ File.separator + replaceUrlWithPlus(url));
		try {
			// �����������ݵ����̣����Ǵ����ļ�
			FileUtils.writeTextFile(file, data);
		} catch (Exception e) {
			Log.d(TAG, "write " + file.getAbsolutePath() + " data failed!");
			e.printStackTrace();
		}
	}

	/**
	 * �ݹ��ɾ�������ļ�
	 * @param cacheFile
	 */
	public static void clearCache(File cacheFile) {
		if (cacheFile == null) {
			try {
				File cacheDir = Application.getInstance().getCacheDir();
				if (cacheDir.exists()) {
					clearCache(cacheDir);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (cacheFile.isFile()) {
			cacheFile.delete();
		} else if (cacheFile.isDirectory()) {
			File[] childFiles = cacheFile.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				clearCache(childFiles[i]);
			}
		}
	}

	public static String replaceUrlWithPlus(String url) {
		// 1. ���������ַ�
		// 2. ȥ����׺���������ļ����������ͼ����(�ر���ͼƬ����Ҫ������ƴ��������е��ֻ���ͼ�⣬ȫ�����ǵĻ���ͼƬ)
		if (url != null) {
			return url.replaceAll("http://(.)*?/", "")
					.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
		}
		return null;
	}
}
