package com.zoe.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.zoe.app.Application;
import com.zoe.util.L;
import com.zoe.util.Lunar;
import com.zoe.util.SharePreferenceUtil;
import com.zoe.util.TimeUtil;
import com.zoe.weather.MainActivity;
import com.zoe.weather.R;
import com.zoe.widget.WeatherWidget;

public class WeatherUpdateService extends Service {
	// ʱ���ʽ
	private static final int TIME_FORMAT_24 = 0;
	private static final int TIME_FORMAT_AM = 1;
	private static final int TIME_FORMAT_PM = 2;
	private RemoteViews remoteViews;
	// ����ͼ��
	private int[] timesImg = { R.drawable.nw0, R.drawable.nw1, R.drawable.nw2,
			R.drawable.nw3, R.drawable.nw4, R.drawable.nw5, R.drawable.nw6,
			R.drawable.nw7, R.drawable.nw8, R.drawable.nw9, };
	private int[] dateViews = { R.id.h_left, R.id.h_right, R.id.m_left,
			R.id.m_right };
	private SharePreferenceUtil mSpUtil;

	@Override
	public IBinder onBind(Intent intent) {
		// ʲôҲ����
		return null;
	}

	// �㲥������ȥ����ϵͳÿ���ӵ���ʾ�㲥��������ʱ��
	private BroadcastReceiver mTimePickerBroadcast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					MainActivity.UPDATE_WIDGET_WEATHER_ACTION)) {
				L.i("updateWeather.........");
				updateWeather();
			} else {
				updateTime();
			}
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mSpUtil = Application.getInstance().getSharePreferenceUtil();
		remoteViews = new RemoteViews(getApplication().getPackageName(),
				R.layout.widget_4x2);// ʵ����RemoteViews
		PendingIntent WeatherIconHotAreaPI = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
//		Intent active = new Intent(getApplicationContext(),WeatherWidget.class);
//		active.setAction(WeatherWidget.WEATHERICON_HOTAREA_ACTION);
//		PendingIntent WeatherIconHotAreaPI = PendingIntent.getBroadcast(this, 0,
//				active, 0);
		remoteViews.setOnClickPendingIntent(R.id.WeatherIconHotArea,
				WeatherIconHotAreaPI);
	}

	private void updateTime() {
		int timeFormat = getTimeFormat();
		// ����SimpleDateFormat����
		SimpleDateFormat df = new SimpleDateFormat("hhmm");
		if (timeFormat == TIME_FORMAT_24) {
			df = new SimpleDateFormat("HHmm");
		}
		// ����ǰʱ���ʽ����HHmm����ʽ
		String timeStr = df.format(new Date(System.currentTimeMillis()));
		for (int i = 0; i < timeStr.length(); i++) {
			// ����i�������ַ�ת��Ϊ��Ӧ������
			int num2 = Integer.parseInt(timeStr.substring(i, i + 1));
			// ����i��ͼƬ����Ϊ��Ӧ������ͼƬ
			// Log.i("WeatherWidget", "ʱ�䣺" + num2);
			remoteViews.setImageViewResource(dateViews[i], timesImg[num2]);
		}
		if (timeFormat == 1) {
			remoteViews.setImageViewResource(R.id.am_pm, R.drawable.w_amw);
		} else if (timeFormat == 2) {
			remoteViews.setImageViewResource(R.id.am_pm, R.drawable.w_pmw);
		} else {
			remoteViews.setImageViewResource(R.id.am_pm,
					R.drawable.switch_camera_hide);
		}
		// remoteViews.setTextViewText(R.id.weather_icon_left, "����" + "\r\n"
		// + "���� 30");
		// remoteViews.setTextViewText(R.id.weather_icon_right, "07/02 �ܶ�"
		// + "\r\n" + "���¶���");
		remoteViews.setTextViewText(R.id.weather_icon_right,
				TimeUtil.getZhouWeek() + "\r\n" + Lunar.getDay());
		ComponentName componentName = new ComponentName(getApplication(),
				WeatherWidget.class);
		AppWidgetManager.getInstance(getApplication()).updateAppWidget(
				componentName, remoteViews);
	}

	private void updateWeather() {
		String city = mSpUtil.getCity();
		String climate = parseWeather(mSpUtil.getSimpleClimate());
		String temp = mSpUtil.getSimpleTemp() + "��";
		L.i(city + " " + climate + " " + temp);
		remoteViews.setTextViewText(R.id.weather_icon_left, city + "\r\n"
				+ climate + " " + temp);
		remoteViews
				.setImageViewResource(
						R.id.weather_icon,
						Application.getInstance().getWidgetWeatherIcon(
								mSpUtil.getSimpleClimate()));
		ComponentName componentName = new ComponentName(getApplication(),
				WeatherWidget.class);
		AppWidgetManager.getInstance(getApplication()).updateAppWidget(
				componentName, remoteViews);
	}

	private String parseWeather(String climate) {
		String[] strs;
		if (climate.contains("ת")) {// ������ת�֣�ȡǰ���ǲ���
			strs = climate.split("ת");
			climate = strs[0];
		}
		return climate;
	}

	/**
	 * ��ȡʱ����24Сʱ�ƻ���12Сʱ��
	 */
	private int getTimeFormat() {
		ContentResolver cv = this.getContentResolver();
		String strTimeFormat = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.TIME_12_24);
		if (strTimeFormat != null) {
			if (strTimeFormat.equals("24")) {
				L.i("24Сʱ��");
				return TIME_FORMAT_24;
			} else {
				// String amPmValues;
				Calendar c = Calendar.getInstance();
				if (c.get(Calendar.AM_PM) == 0) {
					// amPmValues = "AM";
					return TIME_FORMAT_AM;
				} else {
					// amPmValues = "PM";
					return TIME_FORMAT_PM;
				}
			}
		}
		return TIME_FORMAT_24;
	}

	// private Bitmap getBitmap(int imgRes, boolean top) {
	// Bitmap bm = BitmapFactory.decodeResource(getResources(), imgRes);
	// if (top)
	// return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
	// bm.getHeight() / 2);
	// else
	// return Bitmap.createBitmap(bm, 0, bm.getHeight() / 2,
	// bm.getWidth(), bm.getHeight() / 2);
	// }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerReceiver();// ע��㲥
		updateTime();
		updateWeather();
		return super.onStartCommand(intent, flags, startId);
	}

	private void registerReceiver() {
		IntentFilter updateIntent = new IntentFilter();
		updateIntent.addAction(MainActivity.UPDATE_WIDGET_WEATHER_ACTION);
		updateIntent.addAction("android.intent.action.TIME_TICK");
		updateIntent.addAction("android.intent.action.TIME_SET");
		updateIntent.addAction("android.intent.action.DATE_CHANGED");
		updateIntent.addAction("android.intent.action.TIMEZONE_CHANGED");
		registerReceiver(mTimePickerBroadcast, updateIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// ע��������
		if (mTimePickerBroadcast != null)
			unregisterReceiver(mTimePickerBroadcast);
	}

}
