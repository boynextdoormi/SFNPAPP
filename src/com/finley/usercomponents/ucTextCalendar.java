package com.finley.usercomponents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.sanfai.np.R;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ucTextCalendar extends RelativeLayout
{
	private static SimpleDateFormat sdf_yearmonth = new SimpleDateFormat("yyyy年MM月");
	private static SimpleDateFormat sdf_monthday = new SimpleDateFormat("dd");

	private static String[] weekDays = { "", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
	private IntentFilter filter = new IntentFilter();
	private onDayChangedReceiver receiver;
	private boolean isRegistered = false;

	// private float pxy = 0;
	// private float pxm = 0;
	// private float pxd = 0;
	// private int pxWidth = 0;
	// private int pxHeight = 0;

	public ucTextCalendar(Context context)
	{
		super(context);
		InitLayout(context);
	}

	public ucTextCalendar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		InitLayout(context);
	}

	public ucTextCalendar(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		InitLayout(context);
	}

	private void InitLayout(Context context)
	{
		RegReceiver();
		// TextView yearMonthView = new TextView(context);
		// yearMonthView.setId
		LayoutInflater.from(context).inflate(R.layout.uc_calendar, this);
		// TextView yearMonthView = (TextView) findViewById(R.id.yearMonthView);
		// TextView monthDayView = (TextView) findViewById(R.id.monthDayView);
		// TextView weekDayView = (TextView) findViewById(R.id.weekDayView);
		// pxy = yearMonthView.getTextSize();
		// pxm = monthDayView.getTextSize();
		// pxd = weekDayView.getTextSize();

		Update();
	}

	public void Destroy()
	{
		UnRegReceiver(); // 注册广播接收器
	}

	public void RegReceiver()
	{
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		if (receiver == null)
		{
			receiver = new onDayChangedReceiver(myOnDayChanged);
		}
		if (!isRegistered && receiver != null)
		{
			isRegistered = true;
			this.getContext().registerReceiver(receiver, filter);
		}
	}

	public void UnRegReceiver()
	{
		// Log.i("DATE CHANGED", "Date unregisterReceiver");
		if (receiver != null)
		{
			this.getContext().unregisterReceiver(receiver);
			receiver = null;
		}
	}

	public void Update()
	{
		TextView yearMonthView = (TextView) findViewById(R.id.yearMonthView);
		TextView monthDayView = (TextView) findViewById(R.id.monthDayView);
		TextView weekDayView = (TextView) findViewById(R.id.weekDayView);

		// Log.e("tcal", "width=" + this.getWidth());
		// Log.e("tcal", "height=" + this.getHeight());

		int m = yearMonthView.getWidth();
		// Log.i("Width", "Width=" + m);

		Date date = new Date(System.currentTimeMillis());
		yearMonthView.setText(sdf_yearmonth.format(date));
		monthDayView.setText(sdf_monthday.format(date));
		Calendar now = Calendar.getInstance();
		int wd = now.get(Calendar.DAY_OF_WEEK); // 获得本周的第几天
		int cl = 0xffffffff; // white
		switch (wd)
		{
		case Calendar.SATURDAY:
			cl = 0xff80ff80; // green
			break;

		case Calendar.SUNDAY:
			cl = 0xffff2020; // red
			break;

		default:
			cl = 0xffffffff;
		}
		// Log.i("WD", "" + wd);
		yearMonthView.setTextColor(cl);
		monthDayView.setTextColor(cl);
		weekDayView.setTextColor(cl);
		weekDayView.setText(weekDays[wd]);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// pxWidth = getDefaultSize(getSuggestedMinimumWidth(),
		// widthMeasureSpec);
		// pxHeight = getDefaultSize(getSuggestedMinimumHeight(),
		// heightMeasureSpec);
		resizeText();
	}

	private void resizeText()
	{
		ViewGroup.LayoutParams params = this.getLayoutParams();
		int ch = (params.height > params.width ? params.height : params.width);
		float sy = ch / 7;
		float sm = ch * 2 / 5;
		float sd = ch / 6;

		TextView yearMonthView = (TextView) findViewById(R.id.yearMonthView);
		TextView monthDayView = (TextView) findViewById(R.id.monthDayView);
		TextView weekDayView = (TextView) findViewById(R.id.weekDayView);

		yearMonthView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sy);
		monthDayView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sm);
		weekDayView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sd);
	}

	private final OnDayChanged myOnDayChanged = new OnDayChanged()
	{
		@Override
		public void onChanged()
		{
			ucTextCalendar.this.invalidate();
			ucTextCalendar.this.Update();
			// Log.i("DATE CHANGED", "Date changed");
		}
	};

	public interface OnDayChanged
	{
		void onChanged();
	}

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

}
