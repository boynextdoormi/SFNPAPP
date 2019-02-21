//
// 针刺机操作
//
package com.sanfai.np.ui;

import com.sanfai.np.R;
import com.sanfai.np.objects.NMController;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;

public class UI_NMOperator extends UI_Ancestor
{
	public final NMController Controller;

	/*
	 * public static final int MODEL_NEEDLE_DN = 1;
	 * 
	 * public static final int MODE_UNKNOWN = 0; public static final int
	 * MODE_DEBUG = 1; public static final int MODE_STANDALONE = 2; public
	 * static final int MODE_ONLINE = 3;
	 * 
	 * private int MachineModel = 1;
	 * 
	 * private int RunningMode = MODE_UNKNOWN; private boolean beEmergency =
	 * false; private boolean beRuuning = false; private boolean beReset =
	 * false;
	 */

	public UI_NMOperator(Context context, NMController ctrler)
	{
		super(context);
		Controller = ctrler;

		GetView();
		init();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.lay_nmoperator;
	}

	private void init()
	{
		Button mi;

		mi = (Button) myView.findViewById(R.id.standaloneButton);
		mi.setOnClickListener(onClickMenu);
		mi = (Button) myView.findViewById(R.id.resetButton);
		mi.setOnClickListener(onClickMenu);
		mi = (Button) myView.findViewById(R.id.emergencyButton);
		mi.setOnClickListener(onClickMenu);
		mi = (Button) myView.findViewById(R.id.mainSwitchOn);
		mi.setOnClickListener(onClickMenu);
		mi = (Button) myView.findViewById(R.id.mainSwitchOff);
		mi.setOnClickListener(onClickMenu);

		/*
		 * mi = (Button) myView.findViewById(R.id.button1);
		 * mi.setOnClickListener(onClickMenu); mi = (Button)
		 * myView.findViewById(R.id.button2);
		 * mi.setOnClickListener(onClickMenu); mi = (Button)
		 * myView.findViewById(R.id.button3);
		 * mi.setOnClickListener(onClickMenu); mi = (Button)
		 * myView.findViewById(R.id.button4);
		 * mi.setOnClickListener(onClickMenu);
		 * 
		 */
	}

	private final View.OnClickListener onClickMenu = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int id = v.getId();
			Button b = (Button) v;
			RadioButton rb;
			CheckBox cb;

			switch (id)
			{
			case R.id.standaloneButton:
				cb = (CheckBox) v;
				if (cb.isChecked())
				{
					setCtrlMode(NMController.MODE_STANDALONE);
				}
				else
				{
					setCtrlMode(NMController.MODE_COMBINED);
				}
				break;
			case R.id.resetButton:
				cb = (CheckBox) v;
				setReset(cb.isChecked());
				break;
			case R.id.emergencyButton:
				cb = (CheckBox) v;
				setEmergency(cb.isChecked());
				break;
			case R.id.mainSwitchOff:
				rb = (RadioButton) v;
				if (rb.isChecked())
				{
					Stoping();
				}
				break;
			case R.id.mainSwitchOn:
				rb = (RadioButton) v;
				if (rb.isChecked())
				{
					Running();
				}
				break;
			}
		}
	};

	public void setModel(int m)
	{
		Controller.setMachineModel(m);
		NMUpdate();
	}

	public void setCtrlMode(int mode)
	{
		switch (mode)
		{
		case NMController.MODE_DEBUG:
		case NMController.MODE_STANDALONE:
		case NMController.MODE_COMBINED:
			Controller.setCtrlMode(mode);
			break;
		}
	}

	public void postReset(boolean st)
	{
		final CheckBox cb = (CheckBox) myView.findViewById(R.id.resetButton);

		cb.setEnabled(!st);
		cb.setChecked(st);
	}

	public void setReset(boolean b)
	{
		final CheckBox cb = (CheckBox) myView.findViewById(R.id.resetButton);

		if (b)
		{
			cb.setEnabled(false);
			new Handler().post(new Runnable()
			{
				public void run()
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					postReset(false);
				}
			});
		}
	}

	public void setEmergency(boolean b)
	{
		Controller.setEmergency(b);

		RadioButton rbof = (RadioButton) myView.findViewById(R.id.mainSwitchOff);
		rbof.setEnabled(!b);
		RadioButton rbon = (RadioButton) myView.findViewById(R.id.mainSwitchOn);
		rbon.setEnabled(!b);

		rbof.setChecked(true);
		rbon.setChecked(false);

		CheckBox cb = (CheckBox) myView.findViewById(R.id.resetButton);
		cb.setEnabled(!b);

		cb = (CheckBox) myView.findViewById(R.id.standaloneButton);
		cb.setEnabled(!b);
		/*
		 * if (b) { Stoping(); }
		 */
	}

	public void Running()
	{
		// int[] rid = { 0, R.drawable.nm_dn, R.drawable.nm_up,
		// R.drawable.nm_du, R.drawable.nm_du };
		// int model = Controller.getMachineModel();

		Controller.setRunning(true);
		/*
		 * // 设置SANFAI-Logo动画 ImageView img = (ImageView)
		 * myView.findViewById(R.id.imageView1); // 前景动画
		 * img.setImageResource(rid[model]); AnimationDrawable ad =
		 * (AnimationDrawable) img.getDrawable(); ad.start();
		 */

		CheckBox cb = (CheckBox) myView.findViewById(R.id.standaloneButton);
		cb.setEnabled(false);

		cb = (CheckBox) myView.findViewById(R.id.resetButton);
		cb.setEnabled(false);

	}

	public void Stoping()
	{
		// int[] rid = { R.drawable.nm_dn, R.drawable.nm_up, R.drawable.nm_du };
		Controller.setRunning(false);
		/*
		 * ImageView img = (ImageView) myView.findViewById(R.id.imageView1); //
		 * img.setImageResource(rid[MachineModel - 1]); try { AnimationDrawable
		 * ad = (AnimationDrawable) img.getDrawable(); if (ad != null) {
		 * ad.stop(); } } catch (Exception e) { e.printStackTrace(); }
		 */
		CheckBox cb = (CheckBox) myView.findViewById(R.id.standaloneButton);
		cb.setEnabled(true);
		cb = (CheckBox) myView.findViewById(R.id.resetButton);
		cb.setEnabled(true);
	}

	private void NMUpdate()
	{
		boolean bRun = Controller.isRunning();
		int model = Controller.getMachineModel();
		if (bRun)
		{
			Stoping();
		}
		int[] rid = { 0, R.drawable.nm_dn0, R.drawable.nm_up0, R.drawable.nm_du0, R.drawable.nm_dx0 };
		ImageView img = (ImageView) myView.findViewById(R.id.imageView1);
		img.setBackgroundResource(rid[model]);
		img.setImageResource(rid[model]);
		if (bRun)
		{
			Running();
		}
	}

}
