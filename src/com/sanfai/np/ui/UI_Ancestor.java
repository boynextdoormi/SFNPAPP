package com.sanfai.np.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class UI_Ancestor
{
	protected final Context mContext;
	protected View myView;

	protected UI_Ancestor(Context context)
	{
		mContext = context;
		myView = null;
	}

	public View GetView()
	{
		if (myView == null)
		{
			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int rid = getLayoutId();
			if (rid > 0)
			{
				myView = mInflater.inflate(rid, null);
			}
		}
		return myView;
	}

	protected int getLayoutId()
	{
		return 0;
	}

	protected void initLayout()
	{
	}

	protected void onDestroy()
	{
	}

	public void onResume()
	{
	}

	public void onPause()
	{
	}

	public static int dip2pxl(Context context, float dipValue)
	{
		float m = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	public static int pxl2dip(Context context, float pxValue)
	{
		float m = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / m + 0.5f);
	}

}
