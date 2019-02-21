package com.finley.usercomponents;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class xViewPager extends ViewPager
{
	// 是否可以进行滑动
	private boolean canSlide = true;

	public xViewPager(Context context)
	{
		super(context);
	}

	public xViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if (canSlide)
		{
			return super.onInterceptTouchEvent(ev);
		}
		else
		{
			return canSlide;
		}
	}

	public void setSlide(boolean slide)
	{
		canSlide = slide;
	}

	public void EnableSlide()
	{
		canSlide = true;
	}

	public void DisableSlide()
	{
		canSlide = false;
	}
}