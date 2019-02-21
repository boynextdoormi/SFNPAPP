package com.sanfai.np.ui;

import com.sanfai.np.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

public class UI_NMConnect extends UI_Ancestor
{
	public UI_NMConnect(Context context)
	{
		super(context);
		GetView();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.lay_connect;
	}

	public void AnimationStart()
	{
		// 设置设备连接动画
		ImageView img = (ImageView) myView.findViewById(R.id.imageView1);
		// 背景动画
		img.setBackgroundResource(R.drawable.sanfai_connect);
		AnimationDrawable ad = (AnimationDrawable) img.getBackground();
		ad.start();
	}

}
