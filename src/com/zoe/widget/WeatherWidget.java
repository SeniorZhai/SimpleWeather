package com.zoe.widget;

import com.zoe.service.WeatherUpdateService;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherWidget extends AppWidgetProvider {
	public static final String TEXTINFO_LEFT_HOTAREA_ACTION = "TextInfoLeftHotArea";
	public static final String WEATHERICON_HOTAREA_ACTION = "WeatherIconHotArea";
	public static final String TEXTINFO_RIGHT_HOTAREA_ACTION = "TextInfoRightHotArea";
	public static final String TIME_LEFT_HOTAREA_ACTION = "TimeLeftHotArea";
	public static final String TIME_RIGHT_HOTAREA_ACTION = "TimeRightHotArea";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {// ÿ���һ��С�������һ�Σ���onDeleted��Ӧ
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i("WeatherWidget", "onUpdate");

		Intent intent = new Intent(context, WeatherUpdateService.class);
		context.startService(intent);
	}

	@Override
	public void onEnabled(Context context) {// ��һ��С������ʱ���ã���onDisabled��Ӧ
		// TODO Auto-generated method stub
		super.onEnabled(context);
		Log.i("WeatherWidget", "onEnabled");
	}

	@Override
	public void onDisabled(Context context) {// ���һ��С���ɾ��ʱ�����
		// TODO Auto-generated method stub
		super.onDisabled(context);
		Log.i("WeatherWidget", "onDisabled");
		Intent intent = new Intent(context, WeatherUpdateService.class);
		context.stopService(intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {// С���ÿɾ��һ������һ��
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		Log.i("WeatherWidget", "onDeleted");
	}

	@Override
	public void onReceive(Context context, Intent intent) {// �κ����ɾ�������������
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		String action = intent.getAction();
		Log.i("WeatherWidget", "onReceive action = " + action);
		if (action.equals("android.intent.action.USER_PRESENT")) {// �û������豸ʱ��������
			context.startService(new Intent(context, WeatherUpdateService.class));
		} else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			context.startService(new Intent(context, WeatherUpdateService.class));
		}
		// else if (action.equals(WEATHERICON_HOTAREA_ACTION)) {
		// Intent i = new Intent(context, MainActivity.class);
		// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// context.startActivity(i);
		// }
	}
}
