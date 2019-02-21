package com.finley.helper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class PackageHelper
{
	public static class PluginInfo
	{
		public String appName = "";
		public String appDesc = "";
		public String packageName = "";
		public String versionName = "";
		public int versionCode = 0;
		public Drawable appIcon = null;
	}

	// 获取TestB的Context
	public static Context getPackageContext(Context mContext, String pak)
	{
		Context t = null;
		try
		{
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(pak, 0);
			if (packageInfo != null)
			{
				t = mContext.getApplicationContext().createPackageContext(pak,
						Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			}
		}
		catch (Exception e)
		{
		}
		return t;
	}

	public static PackageInfo getPackageInfo(Context mContext, String pak)
	{
		try
		{
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(pak, 0);
			return packageInfo;
		}
		catch (Exception e)
		{
		}
		return null;
	}

	public static PluginInfo getPackageDetail(final Context mContext, String pak)
	{
		PackageManager pm = mContext.getPackageManager();
		PackageInfo packageInfo = null;
		try
		{
			packageInfo = mContext.getPackageManager().getPackageInfo(pak, 0);
			if (packageInfo != null)
			{
				Log.i("PakeName", pak);
				PluginInfo tmpInfo = new PluginInfo();
				tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
				tmpInfo.appDesc = packageInfo.applicationInfo.loadDescription(pm).toString();
				tmpInfo.packageName = packageInfo.packageName;
				tmpInfo.versionName = packageInfo.versionName;
				tmpInfo.versionCode = packageInfo.versionCode;
				tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);

				packageInfo = null;
				pm = null;
				return tmpInfo;
			}
		}
		catch (Exception e)
		{
		}
		pm = null;
		return null;
	}

	public static boolean isPackageInstalled(Context mContext, String pak)
	{
		boolean t = false;
		try
		{
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(pak, 0);
			if (packageInfo != null)
			{
				t = true;
			}
		}
		catch (Exception e)
		{
		}
		return t;
	}

	public static View PackageLoader(Context mContext, ViewGroup vp, String pak, String sLayout)
	{
		Context ctxTestB;

		ctxTestB = getPackageContext(mContext, pak);
		if (ctxTestB == null)
		{
			return null;
		}
		Resources res = ctxTestB.getResources();
		if (res == null)
		{
			return null;
		}

		// 获取布局文件 ctxTestB.getResources()
		int id = res.getIdentifier(sLayout, "layout", pak);
		if (id == 0)
		{
			return null;
		}
		LayoutInflater lat = (LayoutInflater) ctxTestB.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return (View) lat.inflate(id, vp);
	}

	public static void PackageLoader(Context mContext, String pack)
	{
		Context bContext;

		bContext = getPackageContext(mContext, pack);
		if (bContext == null)
		{
			return;
		}
		PackageManager pm = bContext.getPackageManager();
		Intent it = pm.getLaunchIntentForPackage(pack);
		bContext.startActivity(it);
		return;
	}

	public static void UninstalledPackageLoader(Context mContext, String apk, String apkPath, String activity)
	{
		String path = apkPath + "/";

		DexClassLoader classLoader = new DexClassLoader(apkPath + "/" + apk, apkPath + "/", null,
				mContext.getClassLoader());
		try
		{
			Class mLoadClass = classLoader.loadClass(activity);
			Constructor constructor = mLoadClass.getConstructor(new Class[] {});
			Object TestBActivity = constructor.newInstance(new Object[] {});

			Method getMoney = mLoadClass.getMethod("getMoney", null);
			getMoney.setAccessible(true);
			Object money = getMoney.invoke(TestBActivity, null);
			Toast.makeText(mContext, money.toString(), Toast.LENGTH_LONG).show();

		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 卸载APP
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void uninstallApp(Context context, String packageName)
	{
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}

	/**
	 * 安装app
	 */
	public static void openAPK(File f, Context context)
	{
		context.startActivity(getInstallApp(f, context));
	}

	public static Intent getInstallApp(File f, Context context)
	{
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 设置应用的安装来源，例如谷歌市场
		intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", context.getPackageName());
		intent.setAction(android.content.Intent.ACTION_VIEW);

		/* 设置intent的file */
		intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
		return intent;
	}

	public static List<PluginInfo> ScanApps(final Context mContext, String prefix)
	{
		PackageManager pm = mContext.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		List<PluginInfo> Plugins = new ArrayList<PluginInfo>();

		for (PackageInfo packageInfo : packages)
		{
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
			{
				if (packageInfo.packageName.startsWith(prefix))
				{
					PluginInfo tmpInfo = new PluginInfo();
					String ss = packageInfo.applicationInfo.loadLabel(pm).toString();
					String sd = packageInfo.applicationInfo.loadDescription(pm).toString();
					String[] es = ss.split("\n");
					if (es.length >= 3)
					{
						tmpInfo.appName = es[1];
						sd = es[2];
					}
					else if (es.length == 2)
					{
						tmpInfo.appName = es[1];
					}
					else
					{
						tmpInfo.appName = es[0];
					}
					if (sd == null || sd.length() == 0)
						sd = tmpInfo.appName;
					tmpInfo.appDesc = sd;
					// tmpInfo.appName =
					// packageInfo.applicationInfo.loadLabel(pm).toString();
					// tmpInfo.appDesc =
					// packageInfo.applicationInfo.loadDescription(pm).toString();
					tmpInfo.packageName = packageInfo.packageName;
					tmpInfo.versionName = packageInfo.versionName;
					tmpInfo.versionCode = packageInfo.versionCode;
					tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);
					Plugins.add(tmpInfo);// 如果非系统应用，则添加至appList
					tmpInfo = null;
				}
			}
		}
		packages = null;
		pm = null;
		return Plugins;
	}
}
