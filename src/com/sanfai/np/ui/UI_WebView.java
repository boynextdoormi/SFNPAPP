package com.sanfai.np.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UI_WebView extends WebView
{
	private boolean is_gone = false;

	private static final String Agent = "SFAgent";

	public static final String aboutUrl = "file:///android_asset/html/aboutSanfai.html";

	private final String mURL;
	// private final Context mContext;

	public UI_WebView(Context context)
	{
		super(context);
		// mContext = context;
		mURL = aboutUrl;
		// initWeb();
	}

	public UI_WebView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// mContext = context;
		mURL = aboutUrl;
		// initWeb();
	}

	public UI_WebView(Context context, String url)
	{
		super(context);
		// mContext = context;
		mURL = url;
		// initWeb();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		super.onWindowVisibilityChanged(visibility);
		if (visibility == View.GONE)
		{
			try
			{// stop flash
				WebView.class.getMethod("onPause").invoke(this);
			}
			catch (Exception e)
			{
			}
			this.pauseTimers();
			this.is_gone = true;
		}
		else if (visibility == View.VISIBLE)
		{
			try
			{// resume flash
				WebView.class.getMethod("onResume").invoke(this);
			}
			catch (Exception e)
			{
			}
			this.resumeTimers();
			this.is_gone = false;
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void initWeb()
	{
		this.requestFocusFromTouch(); // 解决输入框无法相应问题.
		this.getSettings().setAppCacheEnabled(false);
		this.getSettings().setDefaultTextEncodingName("gb2312");// ("utf-8");
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setUserAgentString(Agent);
		this.setInitialScale(100);
		this.setBackgroundColor(0);
		this.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}
		});
		try
		{
			this.loadUrl(mURL);
		}
		catch (Exception ex)
		{
		}
	}

	public void Refresh()
	{
		try
		{
			this.loadUrl(mURL);
		}
		catch (Exception ex)
		{
		}
		this.invalidate();
	}

}
