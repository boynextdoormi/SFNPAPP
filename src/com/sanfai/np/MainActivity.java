//
// 三辉针刺机主控应用主入口
//
// 
// 
// June 2018, by Finley Zhu @ HUST
//
package com.sanfai.np;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.finley.helper.SysStorage;
import com.finley.helper.exPagerAdapter;
import com.finley.usercomponents.KeyboardBuilder;
import com.finley.usercomponents.ucTextCalendar;
import com.finley.usercomponents.ucTextCalendar.OnDayChanged;
import com.finley.usercomponents.xViewPager;
import com.sanfai.np.objects.NMController;
import com.sanfai.np.objects.NMController.RequestInfo;
import com.sanfai.np.ui.Dlg_AppConfig;
import com.sanfai.np.ui.UI_About;
import com.sanfai.np.ui.UI_NMAdjust;
import com.sanfai.np.ui.UI_NMConfig;
import com.sanfai.np.ui.UI_NMConnect;
import com.sanfai.np.ui.UI_NMMonitor;
import com.sanfai.np.ui.UI_NMOperator;
import com.sanfai.np.ui.UI_NeedlePadSim;
import com.sanfai.np.ui.UI_PProcess;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private static final boolean NOTUSED = false;
	private static final boolean D = BuildConfig.DEBUG;
	private static final String TAG = BuildConfig.TAG;
	// private GLSurfaceView mGLView;
	// private static final int backOffCount = 90;
	private int backLightCount;
	private Timer backLightTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (D)
			Log.d(TAG, "++ ON CREATE ++");

		super.onCreate(savedInstanceState);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		if (dm.widthPixels == 1280 && dm.heightPixels == 764)
		{ // 说明：微嵌10雨屏的dpi值不正确，这里给予修正
			dm.xdpi = 151.2186f;
			dm.ydpi = 150.5185f;
		}

		Locale.setDefault(Locale.CHINA);

		// 防止休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		hideVritualKey();

		// 设置起始界面
		setContentView(R.layout.sf_main);

		setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);

		//
		layoutComponents();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (D)
			Log.d(TAG, "++ ON START ++");
	}

	private boolean bFirstStart = true;

	@Override
	public synchronized void onResume()
	{
		super.onResume();
		if (D)
			Log.d(TAG, "++ ON RESUME ++");
		hideVritualKey();

		// 延时启动
		if (bFirstStart)
		{
			bFirstStart = false;
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					startup();
				}
			}, 500);
		}
	}

	@Override
	public synchronized void onPause()
	{
		super.onPause();
		if (D)
			Log.d(TAG, "++ ON PAUSE ++");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (D)
			Log.d(TAG, "++ ON STOP ++");
	}

	@Override
	protected void onDestroy()
	{
		if (D)
			Log.d(TAG, "++ ON DESTROY ++");
		cleanup();
		super.onDestroy();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent arg1)
	{
		super.dispatchTouchEvent(arg1);
		backLightCount = 0;
		return false;
	}

	private long longTouchTime = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			// longTouchTime = System.currentTimeMillis();
			break;

		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			/*
			 * if (longTouchTime>0) { long dif = System.currentTimeMillis() -
			 * longTouchTime; if (dif>(long)500) { ShowMore(true); longTouchTime
			 * = -1; } }
			 */
			break;
		}
		return true;
	}

	private void hideVritualKey()
	{
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19)
		{ // lower api
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		}
		else if (Build.VERSION.SDK_INT >= 19)
		{
			// for new api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	private void layoutComponents()
	{ // 根据屏幕大小，修正主要控件大小及位置
		// 说明：修正屏幕dpi值

		DisplayMetrics dm = getResources().getDisplayMetrics();
		// int wp = dm.widthPixels;
		ViewGroup tly = (ViewGroup) findViewById(R.id.topLayerView);

		int cal = 1280; // tly.getWidth();
		int wp = tly.getWidth();
		wp = dm.widthPixels;
		wp = 1024;
		if (dm.widthPixels == 1280)
		{ // 说明：微嵌10雨屏的dpi值不正确，这里给予修正
			wp = 1280;
		}
		if (dm.widthPixels == 1024 && dm.heightPixels == 564)
		{
			wp = 1024;
			cal = 1024;

		}
		if (dm.widthPixels == 1184 && dm.heightPixels == 720)
		{
			wp = 1024;
		}

		try
		{// 以下参数以1280*800为设计蓝图的参数
			ViewGroup ly;
			ViewGroup.LayoutParams params;

			// 下部状态栏
			int sth = wp * 64 / cal;
			ly = (ViewGroup) findViewById(R.id.statusPanelLayout);
			params = (ViewGroup.LayoutParams) ly.getLayoutParams();
			params.height = sth;
			ly.setLayoutParams(params);

			// 标题栏,
			int fh = wp * 76 / cal;

			// 顶部标题栏
			ly = (ViewGroup) findViewById(R.id.headerLayout);
			params = (ViewGroup.LayoutParams) ly.getLayoutParams();
			params.height = fh;
			ly.setLayoutParams(params);

			// 标题栏内的时钟
			AnalogClock ac = (AnalogClock) findViewById(R.id.analogClock1);
			params = ac.getLayoutParams();
			params.height = fh;
			params.width = fh;
			ac.setLayoutParams(params);

			// 标题栏内的日历
			ucTextCalendar tc = (ucTextCalendar) findViewById(R.id.calendarView);
			params = tc.getLayoutParams();
			params.height = fh;
			params.width = fh;
			tc.setLayoutParams(params);
			tc.invalidate();

			// 工作区
			RelativeLayout lay = (RelativeLayout) findViewById(R.id.workareaLayout);
			RelativeLayout.LayoutParams rparams = (RelativeLayout.LayoutParams) lay.getLayoutParams();
			rparams.topMargin = fh;
			rparams.bottomMargin = sth;
			lay.setLayoutParams(rparams);
			lay.invalidate();

			// 设置SANFAI-Logo动画
			ImageView img = (ImageView) findViewById(R.id.logoView);
			// 前景动画
			img.setImageResource(R.drawable.sanfai_logo);
			// 若设置为背景动画
			// img.setBackgroundResource(R.drawable.sanfai_logo);

			AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
			ad.start();

			// TODO
			ShowComStatus(NMController.COM_NOINST);

			View vs = findViewById(R.id.comStartView);
			vs.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					onConfigComport();
				}
			});

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void enableBacklightControl()
	{
		backLightTimer = new Timer();
		backLightTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				backLightCount++;

			}
		}, 1000, 1000);
	}

	public void OffBackLight()
	{
		// if (canBackLightOff)
		{

		}
		backLightCount = 0;
	}

	private void startup()
	{
		try
		{
			AppEntry ap = (AppEntry) getApplication();

			ap.AppInit();
			// ap.mAppConfig.DevAddr = -1;

			if (ap.kbBuilder == null)
			{
				KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboardview);
				ap.kbBuilder = new KeyboardBuilder(this, keyboardView, R.xml.lay_akeypad);
			}

			initData();

			NMController.InitComDevice(this);

			Message message = mHandler.obtainMessage();
			message.what = COMM_ACTION;
			mHandler.sendMessage(message);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		enableBacklightControl();
	}

	private void cleanup()
	{
		try
		{
			ucTextCalendar t = (ucTextCalendar) findViewById(R.id.calendarView);
			t.UnRegReceiver();
		}
		catch (Exception e)
		{
		}
		try
		{
			LinearLayout b = (LinearLayout) findViewById(R.id.drawer);
			b.removeAllViews();
		}
		catch (Exception e)
		{
		}

		try
		{
			backLightTimer.cancel();
		}
		catch (Exception e)
		{
		}
		AppEntry ap = (AppEntry) getApplication();
		ap.AppCleanup();
	}

	private static final String SFROOT = "/sanfai";
	private String sfRoot;

	private void makeDir(String p)
	{
		String spath = sfRoot + p;
		File sf = new File(spath);
		if (!sf.exists())
		{
			sf.mkdirs();
		}
	}

	private void initData()
	{
		sfRoot = SysStorage.getDefaultDirectory(this, SFROOT);
		makeDir("/parms");
		String fi = sfRoot + "/parms/config.xml";

		// ParseConfig.ParseExternalParm(this, fi);

		System.gc();
	}

	public void getScreenSizeOfDevice()
	{
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int dens = dm.densityDpi;

		double xl = 25.4 * width / (double) (dm.xdpi);
		double yl = 25.4 * height / (double) (dm.ydpi);
		Log.d(TAG, "Width=" + (int) (xl + 0.5) + "mm");
		Log.d(TAG, "Height=" + (int) (yl + 0.5) + "mm");
	}

	//
	// 日历消息
	public static class onDayChangedReceiver extends BroadcastReceiver
	{
		private final OnDayChanged mOnDayChanged;

		public onDayChangedReceiver(OnDayChanged odc)
		{
			mOnDayChanged = odc;
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (mOnDayChanged != null)
			{
				mOnDayChanged.onChanged();
			}
		}
	}

	////////////////////////////////////////////////////////////////
	// 主界面控制
	/////////////////
	private void startUI()
	{
		prepareUIView();
	}

	private boolean UI_Inited = false;
	private xViewPager mPager = null;
	private UI_NMOperator mUI_NMOperator = null;
	private UI_NMMonitor mUI_NMMonitor = null;
	private UI_NeedlePadSim mUI_NeedlePadSim = null;
	private UI_PProcess mUI_PProcess = null;
	private UI_NMAdjust mUI_NMAdjust = null;
	private UI_NMConfig mUI_NMConfig = null;
	private UI_About mUI_About = null;

	private String[] pg_Title;

	//
	// 初始化UI
	//
	private void prepareUIView()
	{
		if (UI_Inited)
		{
			return;
		}
		UI_Inited = true;

		pg_Title = getResources().getStringArray(R.array.page_header);

		List<View> views = new ArrayList<View>();

		if (mUI_NMOperator == null)
			mUI_NMOperator = new UI_NMOperator(this, Controller);

		if (mUI_NMMonitor == null)
			mUI_NMMonitor = new UI_NMMonitor(this, Controller);
		if (mUI_NeedlePadSim == null)
			mUI_NeedlePadSim = new UI_NeedlePadSim(this);
		if (mUI_PProcess == null)
			mUI_PProcess = new UI_PProcess(this);
		if (mUI_NMAdjust == null)
			mUI_NMAdjust = new UI_NMAdjust(this);
		if (mUI_NMConfig == null)
			mUI_NMConfig = new UI_NMConfig(this);
		if (mUI_About == null)
			mUI_About = new UI_About(this);
		// new UI_WebView(this);

		LinearLayout loper = (LinearLayout) findViewById(R.id.operPanelLayout);
		loper.addView(mUI_NMOperator.GetView());

		views.add(mUI_NMMonitor.GetView());
		views.add(mUI_PProcess.GetView());
		views.add(mUI_NeedlePadSim);
		views.add(mUI_NMAdjust.GetView());
		views.add(mUI_NMConfig.GetView());
		views.add(mUI_About.GetView());
		// views.add(mUI_About);

		mUI_About.InitWeb();

		mPager = (xViewPager) findViewById(R.id.vPager);
		exPagerAdapter adap = new exPagerAdapter(views);
		adap.setOnViewChanged(mOnViewChanged);

		mPager.setAdapter(adap);
		mPager.setOnPageChangeListener(mOnPageChanged);
		mPager.postInvalidate();
		mPager.setCurrentItem(exPagerAdapter.STARTVIEW);
		mPager.setVisibility(View.VISIBLE);
		mPager.setFocusable(true);

	}

	//
	// 当切换显示页面时
	//
	private void onPageChanged(int pg)
	{// 设置每页的标题
		TextView tv = (TextView) findViewById(R.id.stepView);
		tv.setText(pg_Title[pg]);

		switch (pg)
		{
		case 5:
			Log.i(TAG, "onPageChanged mUI_About ");
			// mUI_About.Refresh();
			break;
		case 2:
			Log.i(TAG, "onPageChanged mUI_NeedlePadSim ");
			break;
		}

		// 当在针迹仿真时，隐藏主操作面板
		LinearLayout lpanel = (LinearLayout) findViewById(R.id.operPanelLayout);
		if (pg == 2)
		{
			lpanel.setVisibility(View.GONE);
			mUI_NeedlePadSim.setDrawerVisibility(true);
		}
		else
		{
			lpanel.setVisibility(View.VISIBLE);
			mUI_NeedlePadSim.setDrawerVisibility(false);
		}
	}

	//
	// ViewPager适配器Adapter的回调
	//
	// TODO Auto-generated method stub
	private final exPagerAdapter.OnViewChangedListener mOnViewChanged = new exPagerAdapter.OnViewChangedListener()
	{
		@Override
		public void OnRefresh(View view, int pageno)
		{// 特殊页面需要处理
			// if (view == mUI_NeedlePadSim)
			// {
			// mUI_NeedlePadSim.setDrawerVisibility(true);
			// }
		}

		@Override
		public void OnDestoryView(View view, int pageno)
		{// 特殊页面需要处理
			/*
			 * if (view == mUI_NMOperator.GetView()) { Log.e(TAG,
			 * "OnDestoryView mUI_NMOperator "); } if (view ==
			 * mUI_PProcess.GetView()) { Log.e(TAG,
			 * "OnDestoryView mUI_PProcess "); } if (view ==
			 * mUI_PMachine.GetView()) { Log.e(TAG,
			 * "OnDestoryView mUI_PMachine "); } if (view ==
			 * mUI_PFConfig.GetView()) { Log.e(TAG,
			 * "OnDestoryView mUI_PFConfig "); } // if (view ==
			 * mUI_About.GetView()) if (view == mUI_About) { Log.e(TAG,
			 * "OnDestoryView mUI_About "); }
			 */
			if (view == mUI_NeedlePadSim)
			{
				// Log.e(TAG, "OnDestoryView mUI_NeedlePadSim ");
				// mUI_NeedlePadSim.setDrawerVisibility(false);
			}
		}
	};

	//
	// ViewPager的回调
	//
	private final OnPageChangeListener mOnPageChanged = new OnPageChangeListener()
	{
		// 滑动状态变化
		// SCROLL_STATE_DRAGGING(1) ->
		// SCROLL_STATE_SETTLING(2) ->
		// SCROLL_STATE_IDLE(0)
		public void onPageScrollStateChanged(int arg0)
		{
			// exPagerAdapter adap = (exPagerAdapter) mPager.getAdapter();
			switch (arg0)
			{
			case ViewPager.SCROLL_STATE_DRAGGING:
				// 点击、滑屏
				// Log.i(TAG, "onPageScrollStateChanged - hold");
				break;
			case ViewPager.SCROLL_STATE_SETTLING:
				// 释放
				// Log.i(TAG, "onPageScrollStateChanged - release");
				break;
			case ViewPager.SCROLL_STATE_IDLE:
				// 无动作、初始状态
				// Log.i(TAG, "onPageScrollStateChanged - done");
				break;
			}
			// Log.i(TAG, "onPageScrollStateChanged -" + arg0);
		}

		// 当从左向右滑动时
		// arg0 - 为左页的页码
		// arg1 - 从1.0 -> 0.0 逐渐减小-- 当arg1<0.5后松手指， 则翻到左页
		// arg2 - 为对应滑动的pixel数
		// 当从右向左滑动时 arg0 - 为本页的页码
		// arg1 - 从0.0 -> 1.0逐渐增大 -- 当arg1>0.5后松手指， 则翻到右页
		// arg2 - 为对应滑动的pixel数
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
			// exPagerAdapter adap = (exPagerAdapter) mPager.getAdapter();
			// int mCurPage = adap.getRealPage(arg0);
			// Log.i(TAG, "onPageScrolled" + arg0 + "--" + arg1 + "--" + arg2);
		}

		public void onPageSelected(int arg0)
		{
			// Log.i(TAG, "onPageSelected - " + arg0);
			exPagerAdapter adap = (exPagerAdapter) mPager.getAdapter();
			int mCurPage = adap.getRealPage(arg0);
			onPageChanged(mCurPage);
		}
	};

	//////////////////////////////////////////////////////////////
	// 通信管理
	//
	private static final int COMM_ACTION = 1;
	private static final int COMM_ACTION_ACTIVATE = 1;
	private static final int COMM_ACTION_STARTING = 2;
	private static final int COMM_STATUS = 2;
	private static final int COMM_DEV_ADDR = 4;

	private static final int UI_START = 3;
	private static final int REG_UPDD = 10;
	private static final int REG_MODD = 11;

	private NMController Controller = new NMController(this);

	private UI_NMConnect mUI_NMConnect = null;

	// 显示通信连接过程
	private void ShowConnectingProgress(boolean bShow)
	{
		if (bShow)
		{
			if (mUI_NMConnect == null)
			{
				mUI_NMConnect = new UI_NMConnect(this);
				RelativeLayout lay = (RelativeLayout) findViewById(R.id.workareaLayout);
				lay.addView(mUI_NMConnect.GetView());
				mUI_NMConnect.AnimationStart();
			}
		}
		else
		{
			if (mUI_NMConnect != null)
			{
				RelativeLayout lay = (RelativeLayout) findViewById(R.id.workareaLayout);
				lay.removeView(mUI_NMConnect.GetView());
				mUI_NMConnect = null;
			}
		}
	}

	// 通信端口配置回调结果
	private Dlg_AppConfig.OnDlgClosedListener onAppDlgClosed = new Dlg_AppConfig.OnDlgClosedListener()
	{
		@Override
		public boolean OnClose(View view, int buttonid, String port, int addr)
		{
			switch (buttonid)
			{
			case R.id.btn_confirm:
			{
				// stop controller communication
				if (Controller.isActive())
				{
					Controller.setActive(false);
				}
				// save config
				AppEntry ap = (AppEntry) getApplication();
				ap.mAppConfig.ComPort = port;
				ap.mAppConfig.DevAddr = addr;
				ap.mAppConfig.Save();
				// notify to restart
				Message msg = mHandler.obtainMessage();
				msg.what = COMM_ACTION;
				mHandler.sendMessage(msg);
				return true;
			}

			case R.id.btn_cancel:
				return true;

			}
			return false;
		}
	};

	// 通信端口配置
	private void onConfigComport()
	{
		Dlg_AppConfig.Dialog_ConfigComport(this, onAppDlgClosed);
	}

	// 启动通信端口
	private void startCOM()
	{
		AppEntry ap = (AppEntry) getApplication();
		if (ap.mAppConfig.DevAddr == -1)
		{
			onConfigComport();
			return;
		}
		if (!Controller.isActive())
		{
			ShowConnectingProgress(true);

			Controller.setCallback(onCommStatus);
			Controller.setComPort(ap.mAppConfig.ComPort);
			Controller.setDevAddr((byte) ap.mAppConfig.DevAddr);
			Controller.setActive(true);
		}
	}

	// 显示通信端口状态
	private void ShowComStatus(int st)
	{
		LinearLayout ll = (LinearLayout) findViewById(R.id.comStartView);

		int[] id = { R.drawable.com_db9a, R.drawable.com_db9b, R.drawable.com_db9c };

		switch (st)
		{
		case NMController.COM_NOINST:
		case NMController.COM_OPENED:
		case NMController.COM_CONNED:
			ll.setBackgroundResource(id[st]);
			break;
		}
	}

	// 处理通信消息
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case COMM_ACTION:
				ShowComStatus(NMController.COM_NOINST);
				startCOM();
				break;
			case COMM_STATUS:
				ShowComStatus(msg.arg1);
				switch (msg.arg1)
				{
				case NMController.COM_NOINST:
					break;
				case NMController.COM_OPENED:
					break;
				case NMController.COM_CONNED:
				{
					Message message = mHandler.obtainMessage();
					message.what = UI_START;
					mHandler.sendMessage(message);
				}
					break;
				}
				break;
			case COMM_DEV_ADDR:
			{
				// 设备地址改变
				AppEntry ap = (AppEntry) getApplication();
				ap.mAppConfig.SetDevAddr(msg.arg1);
			}
				break;

			case UI_START:
				ShowConnectingProgress(false);
				startUI();
				Controller.onOper = mUI_NMMonitor.onOperating;
				Controller.BeginUpdate();
				break;

			case REG_UPDD:
			case REG_MODD:
				break;
			}
		}
	};

	// 通信状态回调
	NMController.OnStatusChangedListener onCommStatus = new NMController.OnStatusChangedListener()
	{
		@Override
		public void OnComStatus(String port, int status)
		{
			// TODO Auto-generated method stub
			Message message = mHandler.obtainMessage();
			message.what = COMM_STATUS;
			message.arg1 = status;
			mHandler.sendMessage(message);
		}

		@Override
		public void OnDevAddrChanged(byte addr)
		{
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			Message message = mHandler.obtainMessage();
			message.what = COMM_DEV_ADDR;
			message.arg1 = addr;
			mHandler.sendMessage(message);
		}

		@Override
		public void OnRegValUpdated(RequestInfo info)
		{
			// TODO Auto-generated method stub
			Message message = mHandler.obtainMessage();
			message.what = REG_UPDD;
			message.arg1 = info.RegAddr;
			message.arg2 = info.RegCount;
			mHandler.sendMessage(message);
		}

		@Override
		public void OnRegValModified(RequestInfo info)
		{
			// TODO Auto-generated method stub
			Message message = mHandler.obtainMessage();
			message.what = REG_MODD;
			message.arg1 = info.RegAddr;
			message.arg2 = info.RegCount;
			mHandler.sendMessage(message);
		}
	};
}
