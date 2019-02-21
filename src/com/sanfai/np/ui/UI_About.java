package com.sanfai.np.ui;

import com.sanfai.np.R;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class UI_About extends UI_Ancestor
{
	private static final String Agent = "SFAgent";

	public static final String aboutUrl = "file:///android_asset/html/aboutSanfai.html";

	public UI_About(Context context)
	{
		super(context);
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.lay_about;
	}

	public void InitWeb()
	{
		WebView wv = (WebView) myView.findViewById(R.id.webView1);
		wv.requestFocusFromTouch(); // 解决输入框无法相应问题.
		wv.getSettings().setAppCacheEnabled(false);
		wv.getSettings().setDefaultTextEncodingName("gb2312");// ("utf-8");
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setUserAgentString(Agent);
		wv.setInitialScale(100);
		wv.setBackgroundColor(0);
		wv.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}
		});
		try
		{
			wv.loadUrl(aboutUrl);
		}
		catch (Exception ex)
		{
		}
	}

}
