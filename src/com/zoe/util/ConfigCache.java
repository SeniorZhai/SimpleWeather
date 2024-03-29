package com.zoe.util;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.zoe.app.Application;

public class ConfigCache {
	private static final String TAG = ConfigCache.class.getName();

	public static final int CONFIG_CACHE_MOBILE_TIMEOUT = 7200000; // 2 hour，3G网络下2小时刷新
	public static final int CONFIG_CACHE_WIFI_TIMEOUT = 1800000; // 30 minute，wifi网络下30分钟刷新

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
			// 万一系统时间不正确，时间在很久以前
			// 网络无效，只能读取缓存
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
			// 创建缓存数据到磁盘，就是创建文件
			FileUtils.writeTextFile(file, data);
		} catch (Exception e) {
			Log.d(TAG, "write " + file.getAbsolutePath() + " data failed!");
			e.printStackTrace();
		}
	}

	/**
	 * 递归的删除缓存文件
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
		// 1. 处理特殊字符
		// 2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更需要如此类似处理，否则有的手机打开图库，全是我们的缓存图片)
		if (url != null) {
			return url.replaceAll("http://(.)*?/", "")
					.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
		}
		return null;
	}
}
