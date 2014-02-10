package com.zoe.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.zoe.bean.City;
import com.zoe.bean.Pm2d5;
import com.zoe.bean.SimpleWeatherinfo;
import com.zoe.bean.Weatherinfo;
import com.zoe.db.CityDB;
import com.zoe.util.NetUtil;
import com.zoe.util.SharePreferenceUtil;
import com.zoe.util.T;
import com.zoe.weather.R;

public class Application extends android.app.Application {
	public static ArrayList<EventHandler> mListeners = new ArrayList<EventHandler>();
	private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private static final int CITY_LIST_SCUESS = 0;
	private static final String FORMAT = "^[a-z,A-Z].*$";
	private static Application mApplication;
	private CityDB mCityDB;
	private Map<String, Integer> mWeatherIcon;// ����ͼ��
	private Map<String, Integer> mWidgetWeatherIcon;// �������ͼ��
	private List<City> mCityList;
	// ����ĸ��
	private List<String> mSections;
	// ��������ĸ�������
	private Map<String, List<City>> mMap;
	// ����ĸλ�ü�
	private List<Integer> mPositions;
	// ����ĸ��Ӧ��λ��
	private Map<String, Integer> mIndexer;
	private boolean isCityListComplite;

	private LocationClient mLocationClient = null;
	private SharePreferenceUtil mSpUtil;
	private Weatherinfo mCurWeatherinfo;
	private SimpleWeatherinfo mCurSimpleWeatherinfo;
	private Pm2d5 mCurPm2d5;
	public static int mNetWorkState;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CITY_LIST_SCUESS:
				isCityListComplite = true;
				if (mListeners.size() > 0)// ֪ͨ�ӿ���ɼ���
					for (EventHandler handler : mListeners) {
						handler.onCityComplite();
					}
				break;
			default:
				break;
			}
		}
	};

	public static synchronized Application getInstance() {
		return mApplication;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initData();
	}

	private LocationClientOption getLocationClientOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");
		option.setServiceName(this.getPackageName());
		option.setScanSpan(0);
		option.disableCache(true);
		return option;
	}

	private void initData() {
		mApplication = this;
		mNetWorkState = NetUtil.getNetworkState(this);
		initCityList();
		mLocationClient = new LocationClient(this, getLocationClientOption());
		initWeatherIconMap();
		initWidgetWeather();
		mSpUtil = new SharePreferenceUtil(this,
				SharePreferenceUtil.CITY_SHAREPRE_FILE);
		IntentFilter filter = new IntentFilter(NET_CHANGE_ACTION);
		registerReceiver(netChangeReceiver, filter);
	}

	public synchronized CityDB getCityDB() {
		if (mCityDB == null)
			mCityDB = openCityDB();
		return mCityDB;
	}

	public synchronized SharePreferenceUtil getSharePreferenceUtil() {
		if (mSpUtil == null)
			mSpUtil = new SharePreferenceUtil(this,
					SharePreferenceUtil.CITY_SHAREPRE_FILE);
		return mSpUtil;
	}

	public synchronized LocationClient getLocationClient() {
		if (mLocationClient == null)
			mLocationClient = new LocationClient(this,
					getLocationClientOption());
		return mLocationClient;
	}

	private CityDB openCityDB() {
		String path = "/data"
				+ Environment.getDataDirectory().getAbsolutePath()
				+ File.separator + "com.zoe.weather" + File.separator
				+ CityDB.CITY_DB_NAME;
		File db = new File(path);
		if (!db.exists()) {
			// L.i("db is not exists");
			try {
				InputStream is = getAssets().open("city.db");
				FileOutputStream fos = new FileOutputStream(db);
				int len = -1;
				byte[] buffer = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
					fos.flush();
				}
				fos.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				T.showLong(mApplication, e.getMessage());
				System.exit(0);
			}
		}
		return new CityDB(this, path);
	}

	public List<City> getCityList() {
		return mCityList;
	}

	public List<String> getSections() {
		return mSections;
	}

	public Map<String, List<City>> getMap() {
		return mMap;
	}

	public List<Integer> getPositions() {
		return mPositions;
	}

	public Map<String, Integer> getIndexer() {
		return mIndexer;
	}

	public boolean isCityListComplite() {
		return isCityListComplite;
	}

	public Map<String, Integer> getWeatherIconMap() {
		return mWeatherIcon;
	}

	public int getWeatherIcon(String climate) {
		int weatherRes = R.drawable.biz_plugin_weather_qing;
		if (TextUtils.isEmpty(climate))
			return weatherRes;
		String[] strs = { "��", "��" };
		if (climate.contains("ת")) {// ������ת�֣�ȡǰ���ǲ���
			strs = climate.split("ת");
			climate = strs[0];
			if (climate.contains("��")) {// ���ת��ǰ���ǲ��ִ����֣���ȡ���ĺ󲿷�
				strs = climate.split("��");
				climate = strs[1];
			}
		}
		if (mWeatherIcon.containsKey(climate)) {
			weatherRes = mWeatherIcon.get(climate);
		}
		return weatherRes;
	}

	public int getWidgetWeatherIcon(String climate) {
		int weatherRes = R.drawable.na;
		if (TextUtils.isEmpty(climate))
			return weatherRes;
		String[] strs = { "��", "��" };
		if (climate.contains("ת")) {// ������ת�֣�ȡǰ���ǲ���
			strs = climate.split("ת");
			climate = strs[0];
			if (climate.contains("��")) {// ���ת��ǰ���ǲ��ִ����֣���ȡ���ĺ󲿷�
				strs = climate.split("��");
				climate = strs[1];
			}
		}
		if (mWidgetWeatherIcon.containsKey(climate)) {
			weatherRes = mWidgetWeatherIcon.get(climate);
		}
		return weatherRes;
	}

	public Weatherinfo getmCurWeatherinfo() {
		return mCurWeatherinfo;
	}

	public SimpleWeatherinfo getCurSimpleWeatherinfo() {
		return mCurSimpleWeatherinfo;
	}

	public void setCurSimpleWeatherinfo(SimpleWeatherinfo simpleWeatherinfo) {
		this.mCurSimpleWeatherinfo = simpleWeatherinfo;
	}

	public void setmCurWeatherinfo(Weatherinfo mCurWeatherinfo) {
		this.mCurWeatherinfo = mCurWeatherinfo;
	}

	public Pm2d5 getmCurPm2d5() {
		return mCurPm2d5;
	}

	public void setmCurPm2d5(Pm2d5 mCurPm2d5) {
		this.mCurPm2d5 = mCurPm2d5;
	}

	private void initWeatherIconMap() {
		mWeatherIcon = new HashMap<String, Integer>();
		mWeatherIcon.put("��ѩ", R.drawable.biz_plugin_weather_baoxue);
		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_baoyu);
		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_dabaoyu);
		mWeatherIcon.put("��ѩ", R.drawable.biz_plugin_weather_daxue);
		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_dayu);

		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_duoyun);
		mWeatherIcon.put("������", R.drawable.biz_plugin_weather_leizhenyu);
		mWeatherIcon.put("���������",
				R.drawable.biz_plugin_weather_leizhenyubingbao);
		mWeatherIcon.put("��", R.drawable.biz_plugin_weather_qing);
		mWeatherIcon.put("ɳ����", R.drawable.biz_plugin_weather_shachenbao);

		mWeatherIcon.put("�ش���", R.drawable.biz_plugin_weather_tedabaoyu);
		mWeatherIcon.put("��", R.drawable.biz_plugin_weather_wu);
		mWeatherIcon.put("Сѩ", R.drawable.biz_plugin_weather_xiaoxue);
		mWeatherIcon.put("С��", R.drawable.biz_plugin_weather_xiaoyu);
		mWeatherIcon.put("��", R.drawable.biz_plugin_weather_yin);

		mWeatherIcon.put("���ѩ", R.drawable.biz_plugin_weather_yujiaxue);
		mWeatherIcon.put("��ѩ", R.drawable.biz_plugin_weather_zhenxue);
		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_zhenyu);
		mWeatherIcon.put("��ѩ", R.drawable.biz_plugin_weather_zhongxue);
		mWeatherIcon.put("����", R.drawable.biz_plugin_weather_zhongyu);
	}

	private void initWidgetWeather() {
		mWidgetWeatherIcon = new HashMap<String, Integer>();
		mWidgetWeatherIcon.put("��ѩ", R.drawable.w17);
		mWidgetWeatherIcon.put("����", R.drawable.w10);
		mWidgetWeatherIcon.put("����", R.drawable.w10);
		mWidgetWeatherIcon.put("��ѩ", R.drawable.w16);
		mWidgetWeatherIcon.put("����", R.drawable.w9);

		mWidgetWeatherIcon.put("����", R.drawable.w1);
		mWidgetWeatherIcon.put("������", R.drawable.w4);
		mWidgetWeatherIcon.put("���������", R.drawable.w19);
		mWidgetWeatherIcon.put("��", R.drawable.w0);
		mWidgetWeatherIcon.put("ɳ����", R.drawable.w20);

		mWidgetWeatherIcon.put("�ش���", R.drawable.w10);
		mWidgetWeatherIcon.put("��", R.drawable.w18);
		mWidgetWeatherIcon.put("Сѩ", R.drawable.w14);
		mWidgetWeatherIcon.put("С��", R.drawable.w7);
		mWidgetWeatherIcon.put("��", R.drawable.w2);

		mWidgetWeatherIcon.put("���ѩ", R.drawable.w6);
		mWidgetWeatherIcon.put("��ѩ", R.drawable.w13);
		mWidgetWeatherIcon.put("����", R.drawable.w3);
		mWidgetWeatherIcon.put("��ѩ", R.drawable.w15);
		mWidgetWeatherIcon.put("����", R.drawable.w8);
	}

	private void initCityList() {
		mCityList = new ArrayList<City>();
		mSections = new ArrayList<String>();
		mMap = new HashMap<String, List<City>>();
		mPositions = new ArrayList<Integer>();
		mIndexer = new HashMap<String, Integer>();
		mCityDB = openCityDB();// ����������ȸ�����,�����ҷ��ڵ��߳��д���
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isCityListComplite = false;
				prepareCityList();
				mHandler.sendEmptyMessage(CITY_LIST_SCUESS);
			}
		}).start();
	}

	private boolean prepareCityList() {
		mCityList = mCityDB.getAllCity();// ��ȡ���ݿ������г���
		for (City city : mCityList) {
			String firstName = city.getFirstPY();// ��һ����ƴ���ĵ�һ����ĸ
			if (firstName.matches(FORMAT)) {
				if (mSections.contains(firstName)) {
					mMap.get(firstName).add(city);
				} else {
					mSections.add(firstName);
					List<City> list = new ArrayList<City>();
					list.add(city);
					mMap.put(firstName, list);
				}
			} else {
				if (mSections.contains("#")) {
					mMap.get("#").add(city);
				} else {
					mSections.add("#");
					List<City> list = new ArrayList<City>();
					list.add(city);
					mMap.put("#", list);
				}
			}
		}
		Collections.sort(mSections);// ������ĸ��������
		int position = 0;
		for (int i = 0; i < mSections.size(); i++) {
			mIndexer.put(mSections.get(i), position);// ����map�У�keyΪ����ĸ�ַ�����valueΪ����ĸ��listview��λ��
			mPositions.add(position);// ����ĸ��listview��λ�ã�����list��
			position += mMap.get(mSections.get(i)).size();// ������һ������ĸ��listview��λ��
		}
		return true;
	}

	BroadcastReceiver netChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(NET_CHANGE_ACTION)) {
				if (mListeners.size() > 0)// ֪ͨ�ӿ���ɼ���
					for (EventHandler handler : mListeners) {
						handler.onNetChange();
					}
			}
			mNetWorkState = NetUtil.getNetworkState(mApplication);
		}

	};

	public static abstract interface EventHandler {
		public abstract void onCityComplite();

		public abstract void onNetChange();
	}
}
