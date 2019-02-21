package com.finley.helper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class exPagerAdapter extends PagerAdapter
{
	public final static int STARTVIEW = 10000;
	private final int mCount = 20000;
	private final int mInPos;
	private int mPages;

	private final List<View> mListViews;
	private final List<View> mTempViews;

	public exPagerAdapter(List<View> listViews)
	{
		mPages = 0;
		mInPos = 0;
		this.mTempViews = null;
		this.mListViews = listViews;
	}

	public exPagerAdapter(List<View> listViews, List<View> tempViews, int insertpos, int morepage)
	{
		this.mTempViews = tempViews;
		this.mListViews = listViews;
		mInPos = insertpos;
		if (morepage < 3)
			morepage = 3;
		mPages = morepage;
	}

	public exPagerAdapter(Activity context, List<View> listViews, int layoutid, int insertpos, int morepage)
	{
		this.mListViews = listViews;
		this.mTempViews = new ArrayList<View>();
		mInPos = insertpos;

		for (int k = 0; k < 4; k++)
		{
			View lay1 = context.getLayoutInflater().inflate(layoutid, null);
			mListViews.add(lay1);
			lay1 = null;
		}
	}

	public synchronized void setAdditionPages(int morepage)
	{
		if (morepage < 3)
			morepage = 3;
		mPages = morepage;
	}

	public interface OnViewChangedListener
	{
		public void OnRefresh(View view, int pageno);

		public void OnDestoryView(View view, int pageno);
	}

	private OnViewChangedListener fOnViewChanged = null;

	public void setOnViewChanged(OnViewChangedListener rf)
	{
		fOnViewChanged = rf;
	}

	public int getRealPage(int pos)
	{
		int totalPages = mListViews.size() + mPages;
		while (pos < STARTVIEW)
			pos += totalPages;
		pos -= STARTVIEW;
		while (pos < 0)
			pos += totalPages;
		pos %= totalPages;
		return pos;
	}

	@Override
	public int getCount()
	{
		return (mCount);
	}

	@Override
	public void destroyItem(View collection, int pos, Object arg2)
	{

	}

	@Override
	public void finishUpdate(View arg0)
	{
	}

	@Override
	public Object instantiateItem(View collection, int pos)
	{
		int realPages = mListViews.size() + mPages;

		pos -= STARTVIEW;
		while (pos < 0)
			pos += realPages;
		pos %= realPages;

		if ((pos > mInPos) && (pos <= mInPos + mPages))
		{
			pos -= (mInPos + 1);
			int rpos = (pos % 3);
			if (pos == 0)
				rpos = 3;

			View t = mTempViews.get(rpos);
			try
			{
				ViewPager pp = (ViewPager) collection;
				// Log.i("CHILD", String.valueOf(pp.getChildCount()));
				int xt = pp.indexOfChild(t);
				if (xt >= 0)
				{
					pp.removeViewAt(xt);
				}
				else
				{
				}
				if (fOnViewChanged != null)
					this.fOnViewChanged.OnRefresh(mTempViews.get(rpos), pos);
				((ViewPager) collection).addView(mTempViews.get(rpos), 0);
			}
			catch (Exception e)
			{
				// e.printStackTrace();
			}
			return mTempViews.get(rpos);
		}
		else
		{
			if (pos > mInPos + mPages)
			{
				pos -= (mPages);
			}
			View t = mListViews.get(pos);
			try
			{
				ViewPager pp = (ViewPager) collection;
				int xt = pp.indexOfChild(t);
				if (xt >= 0)
				{
					View v = pp.getChildAt(xt);
					// Log.e("GGGGG", "pos=" + pos + " obj=" +
					// v.getClass().toString());
					if (fOnViewChanged != null)
					{
						fOnViewChanged.OnDestoryView(v, pos);
					}
					pp.removeViewAt(xt);
				}
				// Log.e("AAAAAAA", "pos=" + pos + " obj=" +
				// mListViews.get(pos).getClass().toString());
				((ViewPager) collection).addView(mListViews.get(pos), 0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return mListViews.get(pos);
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == (object);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1)
	{
	}

	@Override
	public Parcelable saveState()
	{
		return null;
	}

	@Override
	public void startUpdate(View arg0)
	{
	}

	public View getViewAt(int pos)
	{
		int realPages = mListViews.size() + mPages;

		pos -= STARTVIEW;
		while (pos < 0)
			pos += realPages;
		pos %= realPages;

		if ((pos > mInPos) && (pos <= mInPos + mPages))
		{
			pos -= (mInPos + 1);
			int rpos = (pos % 3);
			if (pos == 0)
				rpos = 3;
			return mTempViews.get(rpos);
		}
		else
		{
			if (pos > mInPos + mPages)
			{
				pos -= (mPages);
			}
			return mListViews.get(pos);
		}
	}
}
