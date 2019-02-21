//
// 针刺机操作
//
package com.sanfai.np.ui;

import com.sanfai.np.R;
import com.sanfai.np.objects.NMController;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

public class UI_NMMonitor extends UI_Ancestor
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

	public UI_NMMonitor(Context context, NMController ctrler)
	{
		super(context);
		Controller = ctrler;
		// GetView();
		// init();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.lay_nmmonitor;
	}

	/*
	 * private void init() {
	 * 
	 * }
	 */
	public NMController.OnOperatingListener onOperating = new NMController.OnOperatingListener()
	{

		@Override
		public void OnModelChanged(int status)
		{
			// TODO Auto-generated method stub
			updateModel();
		}

		@Override
		public void OnCtrlMode(int mode)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void OnEmergency(boolean b)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void OnWarnReset(boolean b)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void OnRunning(boolean b)
		{
			// TODO Auto-generated method stub
			if (b)
			{
				Running();

			}
			else
			{
				Stoping();
			}
		}

	};

	public void Running()
	{
		int[] rid = { 0, R.drawable.nm_dn, R.drawable.nm_up, R.drawable.nm_du, R.drawable.nm_du };
		int model = Controller.getMachineModel();

		// 设置SANFAI-Logo动画
		ImageView img = (ImageView) myView.findViewById(R.id.imageView1);
		// 前景动画
		img.setImageResource(rid[model]);
		AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
		ad.start();

	}

	public void Stoping()
	{
		// Controller.setRunning(false);
		ImageView img = (ImageView) myView.findViewById(R.id.imageView1);
		try
		{
			AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
			if (ad != null)
			{
				ad.stop();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void updateModel()
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
