package com.sanfai.np.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.finley.usercomponents.dialogOpenFile;
import com.finley.usercomponents.dialogOpenFile.dialogCallback;
import com.finley.usercomponents.ucCheckBox;
import com.sanfai.np.R;
import com.sanfai.np.objects.SFObjects.NeedlePad;
import com.sanfai.np.objects.SFObjects.PadParm;
import com.sanfai.np.objects.SFSurfaceView;
import com.sanfai.np.objects.dxfObjects;
import com.sanfai.np.objects.dxfparser;
import com.sanfai.np.objects.dxfparser.OnDxfParsed;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UI_NeedlePadSim extends RelativeLayout
{
	private final Context mContext;
	private SFSurfaceView drawView = null;

	public UI_NeedlePadSim(Context context)
	{
		super(context);
		mContext = context;
		InitLayout(context);
	}

	public UI_NeedlePadSim(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		InitLayout(context);
	}

	public UI_NeedlePadSim(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		mContext = context;
		InitLayout(context);
	}

	private void InitLayout(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.lay_ndlepad, this);
		drawView = new SFSurfaceView(this.getContext());
		setDrawerVisibility(true);

		ImageView imv;
		imv = (ImageView) findViewById(R.id.operMenu);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operOpen);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operConf);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operDisp);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operOlap);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operParm);
		imv.setOnClickListener(onClickButton);
		imv = (ImageView) findViewById(R.id.operTrns);
		imv.setOnClickListener(onClickButton);

		File sdCard = Environment.getExternalStorageDirectory();
		lastOpenPath = sdCard.getAbsolutePath();
	}

	public void Destroy()
	{
	}

	public void setDrawerVisibility(boolean b)
	{
		LinearLayout ll = (LinearLayout) findViewById(R.id.drawer);
		try
		{
			if (b)
			{
				if (ll.getChildCount() == 0)
				{
					ll.addView(drawView);
					// Log.i("AAA", "AddView");
				}
				else
				{
					// Log.i("AAA", "Not AddView");
				}
			}
			else
			{
				if (drawView != null)
				{
					drawView.CancelDraw();
				}
				// Log.i("AAA", "RemoveView");
				ll.removeAllViews();
			}
		}
		catch (Exception e)
		{
		}
	}

	private final View.OnClickListener onClickButton = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int id = v.getId();
			switch (id)
			{
			case R.id.operMenu:
				ShowOperPad();
				return;

			case R.id.operOpen:
				showConfigNeedlePadOpenDialog();
				break;

			case R.id.operConf:
				showChooseNeedlePadView(ONPADSELECTED);
				break;

			case R.id.operDisp:
				showDesign();
				break;
			case R.id.operOlap:
				showOverlap();
				break;

			case R.id.operTrns:
				ResetTransform();
				break;

			case R.id.operParm:
				showConfigNeedlePadParmView(-1, ONATTRMODIFIED);
				break;

			// case R.id.button1:
			// iLoadDxf();
			// break;
			// case R.id.button2:
			// iLoadTxt();
			// break;

			}
		}
	};

	private void ShowOperPad()
	{
		LinearLayout ll = (LinearLayout) findViewById(R.id.operPad);
		if (ll.getVisibility() == View.INVISIBLE)
		{
			ll.setVisibility(View.VISIBLE);
		}
		else
		{
			ll.setVisibility(View.INVISIBLE);
		}
	}

	private void ResetTransform()
	{
		if (drawView != null)
		{
			drawView.resetTransform();
		}
	}

	private void showDesign()
	{
		if (drawView != null)
		{
			drawView.setOverlap(false);
		}
	}

	private void showOverlap()
	{
		if (drawView != null)
		{
			drawView.setOverlap(true);
		}
	}

	private String lastOpenPath;
	private String dxfFilePath;
	private NeedlePad padDef = new NeedlePad();

	private final static int ONDXFPARSED = 199;
	private final static int ONDXFCHOOSED = 198;
	private final static int ONATTRSPECED = 197;
	private final static int ONATTRMODIFIED = 196;
	private final static int ONPADCHECKED = 195;
	private final static int ONPADSELECTED = 194;

	@SuppressLint("HandlerLeak")
	private final Handler mCallbackHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case ONDXFPARSED:
				AfterDxfParsed((msg.arg1 == 1), (dxfObjects) msg.obj);
				break;

			case ONDXFCHOOSED:
			{
				dxfFilePath = (String) msg.obj;
				File file = new File(dxfFilePath);
				lastOpenPath = file.getParentFile().getPath();
				// Log.i("LastFilePath", lastOpenPath);
				showConfigNeedlePadParmView(-1, ONATTRSPECED);
			}
				break;

			case ONPADSELECTED:
				showConfigNeedlePadParmView(msg.arg1, ONATTRMODIFIED);
				break;

			case ONATTRMODIFIED:
			{
				View v = (View) msg.obj;
				PadParm pp = new PadParm();
				fetchPadParm(v, pp);

				int sno = msg.arg1;
				if (sno > 0)
				{
					if (drawView != null)
					{
						List<NeedlePad> npl = drawView.getDataSeries();
						if (sno <= npl.size())
						{
							NeedlePad np = npl.get(sno - 1);
							np.setParm(pp);
						}
					}
				}
			}
				break;

			case ONATTRSPECED:
			{
				padDef = new NeedlePad();
				View v = (View) msg.obj;
				fetchPadParm(v, padDef.padParm);
				LoadPadDefFile(dxfFilePath);
			}
				break;

			case ONPADCHECKED:
				onPadChecked(msg.arg1, (msg.arg2 == 1));
				break;

			default:
				break;
			}
		}
	};

	private String getSuffix(String filename)
	{
		int dix = filename.lastIndexOf('.');
		if (dix < 0)
		{
			return "";
		}
		else
		{
			return filename.substring(dix + 1);
		}
	}

	private void LoadPadDefFile(String filePath)
	{
		String ext = getSuffix(filePath);
		if (0 == ext.compareToIgnoreCase("dxf"))
		{
			LoadDxfFile(filePath);
		}
		else if (0 == ext.compareToIgnoreCase("txt"))
		{
			LoadTxtFile(filePath);
		}
	}

	private void LoadDxfFile(String file)
	{
		dxfparser.Parser(file, onDxfParsed);
	}

	private void LoadTxtFile(String file)
	{
		padDef.LoadFromTxtFile(file);
		if (drawView != null)
		{
			drawView.addDataSource(padDef);
		}
	}

	// private void iLoadTxt()
	// {
	// padDef.LoadFromTxtFile(this.getContext(), "needle.txt");
	// if (drawView != null)
	// {
	// drawView.setDataSource(padDef);
	// }
	// }
	//
	// private void iLoadDxf()
	// {
	// dxfparser.Parser(this.getContext(), "needle.dxf", onDxfParsed);
	// }

	private final OnDxfParsed onDxfParsed = new OnDxfParsed()
	{
		@Override
		public void OnParsed(boolean bResult, dxfObjects dxfObj)
		{
			Message msg = mCallbackHandler.obtainMessage();
			msg.what = ONDXFPARSED;
			msg.arg1 = (bResult ? 1 : 0);
			msg.obj = dxfObj;
			mCallbackHandler.sendMessage(msg);
		}
	};

	private void AfterDxfParsed(boolean bSuc, dxfObjects obj)
	{
		if (bSuc)
		{
			if (obj != null)
			{
				padDef.LoadFromDxfObject(obj);

				if (0 == padDef.getOverlapPointList().size())
				{
					Toast.makeText(this.getContext(), "Dxf 中没有布针对象坐标!", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(this.getContext(), "Dxf 解析成功!", Toast.LENGTH_SHORT).show();
					if (drawView != null)
					{
						drawView.addDataSource(padDef);

						int sz = drawView.getDataSeriesSize();

						ucCheckBox cb = new ucCheckBox(mContext, "" + sz, oCheckPad, sz);
						cb.setChecked(true);

						LinearLayout ll = (LinearLayout) findViewById(R.id.operPad);
						ll.addView(cb);
					}
				}
			}
		}
		else
		{
			Toast.makeText(this.getContext(), "Dxf 解析失败 !", Toast.LENGTH_SHORT).show();
			// if (drawView != null)
			// {
			// drawView.setDataSource(null);
			// }
		}
	}

	private final ucCheckBox.onCheckListener oCheckPad = new ucCheckBox.onCheckListener()
	{
		@Override
		public void onClick(ucCheckBox obj, int id, boolean bChecked)
		{
			Message msg = mCallbackHandler.obtainMessage();
			msg.what = ONPADCHECKED;
			msg.arg1 = id;
			msg.arg2 = (bChecked ? 1 : 0);
			mCallbackHandler.sendMessage(msg);
		}
	};

	private void onPadChecked(int padId, boolean padSt)
	{
		if (drawView != null)
		{
			List<NeedlePad> l = drawView.getDataSeries();
			if (padId > l.size())
			{
				return;
			}
			NeedlePad p = l.get(padId - 1);
			p.setVisibility(padSt);
		}
	}

	private void showChooseNeedlePadView(final int msgId)
	{
		// final int mMsgId = msgId;
		if (drawView == null)
		{
			return;
		}
		List<NeedlePad> npl = drawView.getDataSeries();
		if (npl.size() == 0)
		{
			return;
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map;

		map = new HashMap<String, Object>();
		map.put("sno", "序号");
		map.put("width", "样宽(mm)");
		map.put("radius", "针半径(mm)");
		map.put("step", "步进量(mm)");
		list.add(map);

		for (int i = 0; i < npl.size(); i++)
		{
			NeedlePad np = npl.get(i);
			map = new HashMap<String, Object>();
			map.put("sno", "" + (i + 1));
			map.put("width", "" + np.padParm.width);
			map.put("radius", "" + np.padParm.radius);
			map.put("step", "" + np.padParm.step);
			list.add(map);
		}

		SimpleAdapter adapter;
		adapter = new SimpleAdapter(mContext, list, R.layout.lay_ndlepadlistitem,
				new String[] { "sno", "width", "radius", "step" },
				new int[] { R.id.id_sno, R.id.editPadWidth, R.id.editNdleRadius, R.id.editStepLen });

		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = mInflater.inflate(R.layout.lay_ndlepadlist, (ViewGroup) null);

		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		dialog.show();

		ListView lv = (ListView) dialogView.findViewById(R.id.listView1);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				TextView tv = (TextView) arg1.findViewById(R.id.id_sno);
				int sno = 0;
				try
				{
					sno = Integer.valueOf("0" + tv.getText().toString());
				}
				catch (Exception e)
				{

				}
				if (sno > 0)
				{
					Message msg = mCallbackHandler.obtainMessage();
					msg.what = msgId;
					msg.arg1 = sno;
					mCallbackHandler.sendMessage(msg);
					dialog.dismiss();
				}
			}
		});

		//
		// EditText et;
		// et = (EditText) dialogView.findViewById(R.id.editNdleRadius);
		// et.setText("0.15");
		// et = (EditText) dialogView.findViewById(R.id.editPadWidth);
		// et.setText("80");
		// et = (EditText) dialogView.findViewById(R.id.editStepLen);
		// et.setText("5");
		//
		// View v;
		// v = dialogView.findViewById(R.id.btn_confirm);
		// v.setOnClickListener(md);
		// v = dialogView.findViewById(R.id.btn_cancel);
		// v.setOnClickListener(md);
		//

		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.dialogbox);
		window.setContentView(dialogView);

	}

	// 点击加载配置参数后出现的Dialog界面里的显示数据
	@SuppressLint("InflateParams")
	private void showConfigNeedlePadParmView(int sno, int msgId)
	{
		Dialog dialog;
		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = mInflater.inflate(R.layout.lay_ndlepadcfg, null);

		dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		dialog.show();

		OnSetPadParmBtnClicked md = new OnSetPadParmBtnClicked(dialog, dialogView, msgId, sno);

		String ov1 = "80";
		String ov2 = "0.15";
		String ov3 = "5";

		if (sno > 0)
		{
			if (drawView != null)
			{
				List<NeedlePad> npl = drawView.getDataSeries();
				if (sno <= npl.size())
				{
					NeedlePad np = npl.get(sno - 1);
					ov1 = "" + np.padParm.width;
					ov2 = "" + np.padParm.radius;
					ov3 = "" + np.padParm.step;
				}
			}
		}

		EditText et;
		et = (EditText) dialogView.findViewById(R.id.editPadWidth);
		et.setText(ov1);
		et = (EditText) dialogView.findViewById(R.id.editNdleRadius);
		et.setText(ov2);
		et = (EditText) dialogView.findViewById(R.id.editStepLen);
		et.setText(ov3);

		View v;
		v = dialogView.findViewById(R.id.btn_confirm);
		v.setOnClickListener(md);
		v = dialogView.findViewById(R.id.btn_cancel);
		v.setOnClickListener(md);

		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.dialogbox);
		window.setContentView(dialogView);

	}

	private void fetchPadParm(View dlgView, PadParm padDef)
	{
		float fr, fw, fs;

		EditText et;

		et = (EditText) dlgView.findViewById(R.id.editNdleRadius);
		fr = Float.valueOf("0" + et.getText().toString());
		et = (EditText) dlgView.findViewById(R.id.editPadWidth);
		fw = Float.valueOf("0" + et.getText().toString());
		et = (EditText) dlgView.findViewById(R.id.editStepLen);
		fs = Float.valueOf("0" + et.getText().toString());

		padDef.radius = fr;
		padDef.width = fw;
		padDef.step = fs;
	}

	class OnSetPadParmBtnClicked implements OnClickListener
	{
		private final int Id;
		private final int msgId;
		private final View dlgView;
		private final Dialog dlg;

		public OnSetPadParmBtnClicked(Dialog d, View v, int m, int id)
		{
			Id = id;
			msgId = m;
			dlgView = v;
			dlg = d;
		}

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_confirm:
				Message msg = mCallbackHandler.obtainMessage();
				msg.what = msgId;
				msg.arg1 = Id;
				msg.obj = dlgView;
				mCallbackHandler.sendMessage(msg);
				dlg.dismiss();
				// hideDialog();
				break;
			case R.id.btn_cancel:
				dlg.dismiss();
				// hideDialog();
				break;
			}
		}
	}

	private void showConfigNeedlePadOpenDialog()
	{
		onCreateDialog(1).show();
	}

	protected Dialog onCreateDialog(int id)
	{
		Map<String, Integer> images = new HashMap<String, Integer>();

		// 下面几句设置各文件类型的图标
		images.put(dialogOpenFile.sRoot, R.drawable.filesdf); // 根目录图标
		images.put(dialogOpenFile.sParent, R.drawable.fileupd); // 返回上一层的图标
		images.put(dialogOpenFile.sFolder, R.drawable.filedir); // 文件夹图标
		images.put("dxf", R.drawable.filedxf); // wav文件图标
		images.put("txt", R.drawable.filetxt); // wav文件图标
		images.put(dialogOpenFile.sEmpty, R.drawable.fileupd);

		Dialog dialog = dialogOpenFile.createDialog(id, mContext, "打开文件", onDialogCallback, lastOpenPath, ".dxf;.txt;",
				images);
		return dialog;

	}

	private dialogCallback onDialogCallback = new dialogCallback()
	{
		@Override
		public void callback(Dialog dlg, Bundle bundle)
		{
			String filepath = bundle.getString("path");
			Message msg = mCallbackHandler.obtainMessage();
			msg.what = ONDXFCHOOSED;
			msg.obj = filepath;
			mCallbackHandler.sendMessage(msg);
		}
	};

}
