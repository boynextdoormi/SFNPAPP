//
// 涓夎緣鏃犵汉鏈烘鍘傛湁闄愬叕鍙稿簲鐢ㄤ富鍏ュ彛
//
// 
// 
// June 2017, by Finley Zhu @ HUST
//
package com.sanfai.np;

import com.finley.helper.CrashHandler;
import com.finley.usercomponents.KeyboardBuilder;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class AppEntry extends Application
{
	public static final boolean D = true;
	public static final String TAG = BuildConfig.TAG;

	private static final String S_PKG = "packageName";

	public final AppConfig mAppConfig = new AppConfig();

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (D)
			Log.d(TAG, "Application Start up.");
		if (!BuildConfig.DEBUG)
		{// crash Handler
			CrashHandler crashHandler = CrashHandler.getInstance();
			crashHandler.init(getApplicationContext());
		}
		// mAppConfig = new AppConfig();
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
	}

	/**
	 * 
	 * InitApplication
	 */
	public void AppInit()
	{
		SharedPreferences sp = getSharedPreferences(TAG, 2);
		SharedPreferences.Editor ed = sp.edit();
		ed.putString(S_PKG, getPackageName());
		ed.commit();

		mAppConfig.Load();
		return;
	}

	/**
	 * 
	 * InitApplication
	 */
	public void AppCleanup()
	{
		mAppConfig.Save();
	}

	public static final String TAG_COMPORT = "COMPORT";
	public static final String TAG_DEVADDR = "DEVADDR";

	public class AppConfig
	{
		public String ComPort;
		public int DevAddr;

		public void Save()
		{
			SharedPreferences sp = getSharedPreferences(TAG, 2);
			SharedPreferences.Editor ed = sp.edit();

			ed.putString(TAG_COMPORT, ComPort);
			ed.putInt("DEVADDR", DevAddr);

			ed.commit();
		}

		public void Load()
		{
			SharedPreferences sp = getSharedPreferences(TAG, 1);
			ComPort = sp.getString(TAG_COMPORT, "COM1");
			DevAddr = sp.getInt(TAG_DEVADDR, -1);
		}

		public void SetDevAddr(int addr)
		{
			DevAddr = addr & 0x00ff;
			Save();
		}

		public void SetComPort(String port)
		{
			ComPort = port;
			Save();
		}
	}

	public KeyboardBuilder kbBuilder = null;
}
