package com.finley.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.format.Time;
import android.view.Gravity;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler
{

	/** Debug Log tag */
	public static final String TAG = "CrashHandler";
	/**
	 * 闁哄嫷鍨伴幆浣割嚕閿熶粙宕ラ娑欙級闊洦顨夌欢顓㈠礄閿燂拷?,闁革讣鎷�?閿熺禍bug闁绘鍩栭敓鎴掓缁楀懎顕ｉ敓浠嬪触閿燂拷?,
	 * 闁革腹澧絜lease闁绘鍩栭敓鎴掓缁楀懘宕楅幎鑺ワ紨濞寸姰鍎茶ぐ浣虹矆閾忓厜鏌ら幖鏉戠箲閿熺獤鍡楀幋
	 */
	public static final boolean DEBUG = false;
	/** 缂侇垵宕电划鐑橆渶濡鍚囬柣銊ュ瀶ncaughtException濠㈣泛瀚幃濠勭尵閿燂拷? */
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	/** CrashHandler閻庡湱鍋橀敓锟�? */
	private static CrashHandler INSTANCE;
	/** 缂佸顑呯花顓㈡儍閸戞笝ntext閻庣數顢婇敓锟�? */
	private Context mContext;
	/**
	 * 濞达綀娉曢弫顥秗operties闁哄鍎扮换姘炬嫹?閿熻姤顭堥鏇熷緞閸モ晜鐣卞ǎ鍥ｅ墲娴煎懘宕畝鍕櫓閻犲浂鍨伴悥銏ゅ冀閸粈绻嗛柟顓ㄦ嫹
	 */
	private Properties mDeviceCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";
	/** 闂佹寧鐟ㄩ銈夊箮閵夈儲鍟為柡鍌氭矗濞嗐垽鎯冮崟顒�鈷栭悘鐐存礀閿燂拷? */
	private static final String CRASH_REPORTER_EXTENSION = ".cr";

	/** 濞ｅ洦绻嗛惁澶愬矗椤忓懏绠掑☉鎿勬嫹濞戞挾瀚漴ashHandler閻庡湱鍋橀敓锟�? */
	private CrashHandler()
	{
	}

	/** 闁兼儳鍢茶ぐ鍢媟ashHandler閻庡湱鍋橀敓锟�? ,闁告娲戠欢銉ノ熼垾宕囩 */
	public static CrashHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	/**
	 * 闁告帗绻傞～鎰板礌閿燂拷?,婵炲鍔岄崬绱簅ntext閻庣數顢婇敓锟�?,
	 * 闁兼儳鍢茶ぐ鍥╁寲閼姐倗鍩犲娑欘焾椤撳鎯冮崚鍒礳aughtException濠㈣泛瀚幃濠囧闯閿燂拷?,
	 * 閻犱礁澧介悿鍡欐嫚椤у『ashHandler濞戞捁娅ｉ埢鍏兼償韫囨洘鐣卞娑欘焾椤撶粯寰勯崟顓熷�為柛锝忔嫹
	 * 
	 * @param ctx
	 */
	public void init(Context ctx)
	{
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 鐟滅増褰歯caughtException闁告瑦鍨归弫鎾诲籍閺堢數绐楅弶鐑嗗墮閸欏棛鎷犻妷銉ユ瘣闁轰胶澧楀鍨緞閸曨厽鍊�
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{
		if (!handleException(ex) && mDefaultHandler != null)
		{
			// 濠碘�冲�归悘澶愭偨閵婏箑鐓曟繛灞稿墲濠�浣瑰緞閸曨厽鍊為柛鎺撶懆閿燂拷?缂侇垵宕电划鐑橆渶濡鍚囬柣銊ュ缁辨挾鏁粙娆炬П闁荤偛妫楀▍鎺楀级閵夈儺妲遍柣鐑囨嫹
			mDefaultHandler.uncaughtException(thread, ex);
		}
		else
		{
			// Sleep濞戞搫鎷峰ù鍏艰壘閹绱掗幘瀛樺皢缂佸顑呴敓锟�?
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				// Log.e(TAG, "Error : ", e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	/**
	 * 闁煎浜滈悾鐐▕婢舵劖鏅╅悹鍥跺灠椤︹晠鎮堕敓锟�?,閿燂拷?閸洘鑲犻梺鎸庣懆椤曘倖绌遍埄鍐х礀
	 * 闁告瑦鍨块敓鎴掔窔閺佸﹦鎷犻娑樞撻柛娑橈功閻℃垿骞欏鍕▕闁秆冩搐濠�顏勵潰閵堝懐鏆氶柟杈炬嫹.
	 * 鐎殿噯鎷烽柛娆愬灱閿熻棄鎳庤ぐ鍙夌閵夛妇澹岄柟璇″枦閸ゆ粌顔忔潏鈺傜暠闁诡垰鎳庨崰宀勫级閵夈劌娈伴悗瑙勭煯缁犵喎顕ｉ崒姘卞煑濠㈣泛瀚
	 * 幃濠囨焻閺勫繒甯�
	 * 
	 * @param ex
	 * @return true:濠碘�冲�归悘澶嬪緞閸曨厽鍊炲ù婊冩椤曟艾顕ｉ崒姘卞煑濞ｅ洠鍓濋敓锟�?;闁告熬绠戦崹顖涙交閺傛寧绀�false
	 */
	private boolean handleException(Throwable ex)
	{
		if (ex == null)
		{
			// Log.w(TAG, "handleException --- ex==null");
			return true;
		}
		final String msg = ex.getLocalizedMessage();
		if (msg == null)
		{
			return false;
		}
		// 濞达綀娉曢弫顥簅ast闁哄鍎插Ο澶岀矆閸濆嫮纾介悽顖炴櫜娣囧﹪骞侀敓锟�?
		new Thread()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast toast = Toast.makeText(mContext, "缂佸顑呯花顓㈠礄濞差亝鏅╅柨娑樿嫰瀹撳棛浜搁崱娑虫嫹閿熶粙宕欓敓锟�?:\r\n" + msg,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				// MsgPrompt.showMsg(mContext, "缂佸顑呯花顓㈠礄濞差亝鏅╅柛鐕傛嫹",
				// msg+"\n闁绘劕婀遍垾妯兼媼閵堝鎷烽敓浠嬪礄閿燂拷?");
				Looper.loop();
			}
		}.start();
		// 閿燂拷?閸洘鑲犻悹浣瑰劤椤︻剚绌遍埄鍐х礀
		collectCrashDeviceInfo(mContext);
		// 濞ｅ洦绻傞悺銊╂煥濞嗘帩鍤栭柟韬插劚閹诧繝寮崶锔筋偨
		saveCrashInfoToFile(ex);
		// 闁告瑦鍨块敓鎴掔窔閺佸﹦鎷犻娑樞撻柛娑橈工閸╁矂寮靛鍛潳闁革綇鎷�
		// sendCrashReportsToServer(mContext);
		return true;
	}

	/**
	 * 闁革负鍔庨埢鍏兼償韫囨挻鍎欓柛鏂诲妽濡炲倿宕愰敓锟�?,
	 * 闁告瑯鍨禍鎺旀嫬閸愵亝鏆忛悹鍥ュ劚閸ら亶寮悧鍫熼檷闁告瑦鍨块敓鎴掓娴滄帡宕滃鍡欐⒕闁哄牆顦ぐ鍌炴焻娴ｇ儤鐣遍柟韬插劚閿燂拷?
	 */
	public void sendPreviousReportsToServer()
	{
		sendCrashReportsToServer(mContext);
	}

	/**
	 * 闁硅泛锕弫濠勬嫚椤栨稑袚闁告稑锕よぐ鍌炴焻娴ｈ櫣鑸堕柡鍫濈Т婵喖宕抽敓锟�?,闁告牕鎳庨幆鍫ュ棘妫颁線鐛撻柣銏㈠枔濞堟垿宕
	 * 仦闂寸鞍闁告挸绉甸惀鍛村矗閹达讣鎷锋担鐑樼暠.
	 * 
	 * @param ctx
	 */
	private void sendCrashReportsToServer(Context ctx)
	{
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0)
		{
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));
			for (String fileName : sortedFiles)
			{
				File cr = new File(ctx.getFilesDir(), fileName);
				postReport(cr);
				cr.delete();// 闁告帞濞�濞呭骸顔忛幓鎺戠岛闂侇偂鑳跺▓鎴﹀箮閵夈儲鍟�
			}
		}
	}

	private void postReport(File file)
	{
		// TODO 闁告瑦鍨块敓鎴掔窔閺佸﹦鎷犻娑樞撻柛娑橈工閸╁矂寮靛鍛潳闁革綇鎷�
	}

	/**
	 * 闁兼儳鍢茶ぐ鍥煥濞嗘帩鍤栭柟韬插劚閹诧繝寮崶锔筋偨闁告熬鎷�
	 * 
	 * @param ctx
	 * @return
	 */
	private String[] getCrashReportFiles(Context ctx)
	{
		File filesDir = ctx.getFilesDir();
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

	/**
	 * 濞ｅ洦绻傞悺銊╂煥濞嗘帩鍤栧ǎ鍥ｅ墲娴煎懘宕氶悧鍫熺�ù鐘虫构閿燂拷?
	 * 
	 * @param ex
	 * @return
	 */
	private String saveCrashInfoToFile(Throwable ex)
	{
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null)
		{
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put("EXEPTION", ex.getLocalizedMessage());
		mDeviceCrashInfo.put(STACK_TRACE, result);
		try
		{
			// long timestamp = System.currentTimeMillis();
			Time t = new Time("GMT+8");
			t.setToNow(); // 闁告瑦鐗曠欢杈╁寲閼姐倗鍩犻柡鍐ㄧ埣閿燂拷?
			int date = t.year * 10000 + t.month * 100 + t.monthDay;
			int time = t.hour * 10000 + t.minute * 100 + t.second;
			String fileName = "crash-" + date + "-" + time + CRASH_REPORTER_EXTENSION;
			FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			mDeviceCrashInfo.store(trace, "");
			trace.flush();
			trace.close();
			return fileName;
		}
		catch (Exception e)
		{
			// Log.e(TAG, "an error occured while writing report file...", e);
		}
		return null;
	}

	/**
	 * 閿燂拷?閸洘鑲犵紒瀣儏缁厼鐣烽埡鍌滅毦闁汇劌瀚鏇熷緞閸ワ缚绻嗛柟顓ㄦ嫹
	 * 
	 * @param ctx
	 */
	public void collectCrashDeviceInfo(Context ctx)
	{
		try
		{
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null)
			{
				mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
			}
		}
		catch (NameNotFoundException e)
		{
			// Log.e(TAG, "Error while collect package info", e);
		}
		// 濞达綀娉曢弫銈夊矗瀹ュ懐娈搁柡澶堝劜閺佸綊姊块崱姘煎晭濠㈣泛娲ｆ穱濠囧箒閿燂拷?.闁肝绘獪ild缂侇偉顔婇懙鎴﹀礌閸涱厽鍎撻柛姘椤帞鎷嬮幆褜妲靛ǎ鍥ｅ墲閿燂拷?,
		// 濞撴艾顑呴敓锟�?: 缂侇垵宕电划娲偋閸喐鎷遍柛娆欐嫹,閻犱焦鍎抽ˇ顒勬偨閻斿爼鐛撻柛鐕傛嫹
		// 缂佹稑顦惔婊堝礉閳哄嫮娈堕悹鍥ㄦ礈閳诲吋鎯旇箛鏇熺暠闁哄牆顦遍弫銈嗙┍閳╁啩绱�
		// 闁稿繗娓圭紞瀣┍閳╁啩绱栭悹鍥у槻瀵剟鎳撻崘銊﹀�甸梻鍫涘灮濞堟垿骞嬮鍕
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
				// if (DEBUG) {
				// Log.d(TAG, field.getName() + " : " + field.get(null));
				// }
			}
			catch (Exception e)
			{
				// Log.e(TAG, "Error while collect crash info", e);
			}
		}
	}

}
