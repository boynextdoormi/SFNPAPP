package com.finley.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Base64;
import android.util.Log;

public class HttpHelper
{
	private CookieStore cookies = null;

	public static final String Agent = "AT01RequestAgent";

	public HttpHelper()
	{
		cookies = null;
	}

	public CookieStore getCookie()
	{
		return cookies;
	}

	public void setCookie(CookieStore cks)
	{
		cookies = cks;
	}

	public String httpPostCookie(String sUri, List<NameValuePair> posts)
	{
		String result = null;
		try
		{
			// z
			HttpEntity entity = new UrlEncodedFormEntity(posts, HTTP.UTF_8);

			// 闁跨喖鎽弬銈嗗HttpPost闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
			HttpPost httpPost = new HttpPost(sUri);

			// 闁跨喐鏋婚幏鐑芥晸閻偉顕滈幏鐑芥晸閺傘倖瀚圭�圭偤鏁撻弬銈嗗
			httpPost.setEntity(entity);

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
			HttpClientParams.setRedirecting(httpParams, true);
			// HttpProtocolParams.setUserAgent(httpParams, Agent);
			// 闁跨喐鏋婚幏宄板絿HttpClient闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			/*
			 * // 闁跨喐鏋婚幏宄板絿HttpClient闁跨喐鏋婚幏鐑芥晸閺傘倖瀚� HttpClient httpClient = new
			 * DefaultHttpClient(); // 闁跨喐鏋婚幏鐑芥晸閹恒儳顒查幏閿嬫
			 * httpClient.getParams().setParameter(CoreConnectionPNames
			 * .CONNECTION_TIMEOUT, 30000); // 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归敓锟�?
			 * httpClient.getParams().setParameter
			 * (CoreConnectionPNames.SO_TIMEOUT, 30000);
			 */
			// 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚� cookie
			if (cookies != null)
			{
				((AbstractHttpClient) httpClient).setCookieStore(cookies);// 閸愭獑ookie
			}

			// 闁跨喐鏋婚幏宄板絿HttpResponse鐎圭偤鏁撻弬銈嗗
			HttpResponse httpResp = httpClient.execute(httpPost);

			// 闁跨喎褰ㄧ拋瑙勫闁跨喕顫楅惂鍛婂闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归弲鎺楁晸閿燂拷?
			if (httpResp.getStatusLine().getStatusCode() == 200)
			{
				// 闁跨喐鏋婚幏宄板絿闁跨喐鏋婚幏鐑芥晸閹搭亞顣幏鐑芥晸閺傘倖瀚归柨鐕傛嫹
				result = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
				// 闁跨喐鏋婚幏绌媜okie, 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閿燂拷?
				cookies = ((AbstractHttpClient) httpClient).getCookieStore();
			}
		}
		catch (Exception e)
		{
			result = "";
		}
		return result;
	}

	/*
	 * public static InputStream OpenHttpConnection(String urlString) throws
	 * IOException { InputStream in = null; int response = -1;
	 * 
	 * URL url = new URL(urlString); URLConnection conn = url.openConnection();
	 * 
	 * if (!(conn instanceof HttpURLConnection)) { throw new IOException(
	 * "Not an HTTP connection"); }
	 * 
	 * try{ HttpURLConnection httpConn = (HttpURLConnection) conn;
	 * httpConn.setAllowUserInteraction(false);
	 * httpConn.setInstanceFollowRedirects(true);
	 * httpConn.setRequestMethod("GET"); httpConn.connect();
	 * 
	 * response = httpConn.getResponseCode(); if (response ==
	 * HttpURLConnection.HTTP_OK) { in = httpConn.getInputStream(); } } catch
	 * (Exception ex) { throw new IOException("Error connecting"); } return in;
	 * }
	 * 
	 * public static StringBuffer fetchUrl0(String surl) { Log.i(Agent, surl);
	 * StringBuffer sb = new StringBuffer();
	 * 
	 * InputStream in = null; try { in = OpenHttpConnection(surl);
	 * InputStreamReader inputReader = new InputStreamReader(in); BufferedReader
	 * reader = new BufferedReader(inputReader); String inputLine = null; while
	 * ((inputLine = reader.readLine()) != null) {
	 * sb.append(inputLine).append("\n"); } reader.close(); inputReader.close();
	 * 
	 * in.close(); } catch (IOException e1) { e1.printStackTrace();
	 * Log.d("bitMap",e1.toString()); return null; } return sb; }
	 */

	public static StringBuffer fetchUrl(String surl)
	{
		int seglen = 384;

		if (surl.length() > seglen)
		{
			Log.i(Agent, surl.substring(0, seglen));
			Log.i(Agent, surl.substring(seglen));
		}
		else
		{
			Log.i(Agent, surl);
		}
		StringBuffer sb = new StringBuffer();

		URL url = null;
		try
		{
			url = new URL(surl);
		}
		catch (Exception e)
		{
			Log.i(Agent, "Exception create url");
			e.printStackTrace();
		}
		if (url == null)
		{
			Log.i(Agent, "No url created");
			sb.append("");
			return sb;
		}
		// Log.i(Agent, "url created");

		HttpURLConnection urlConn;
		try
		{
			urlConn = (HttpURLConnection) url.openConnection();
		}
		catch (Exception e)
		{
			Log.i(Agent, "Exception openConnection");
			e.printStackTrace();
			sb.append("");
			return sb;
		}
		// Log.i(Agent, "url openConnection Ok");

		try
		{
			urlConn.setRequestMethod("GET");
			urlConn.setConnectTimeout(10000);
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
		}
		catch (Exception e)
		{
			Log.i(Agent, "Exception setConnectProperty");
			e.printStackTrace();
			sb.append("");
			return sb;
		}
		// Log.i(Agent, "setConnectProperty Ok");

		// connect url
		try
		{
			// Log.i(Agent, "before connect");
			urlConn.connect(); // 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
			// Log.i(Agent, "after connect");
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			Log.i(Agent, "Exception connect");
			sb.append("");
			return sb;
		}
		// Log.i(Agent, "connect Ok");

		// IO
		try
		{
			// Log.i(Agent, "Exception I/O 0" );
			InputStream input = urlConn.getInputStream();
			// Log.i(Agent, "Exception I/O 1" );
			InputStreamReader inputReader = new InputStreamReader(input);
			// Log.i(Agent, "Exception I/O 2");
			BufferedReader reader = new BufferedReader(inputReader);
			// Log.i(Agent, "Exception I/O 3");
			String inputLine = null;
			// Log.i(Agent, "Exception I/O 4");
			while ((inputLine = reader.readLine()) != null)
			{
				sb.append(inputLine).append("\n");
			}
			reader.close();
			inputReader.close();
			input.close();
		}
		catch (Exception e)
		{
			Log.i(Agent, "Exception I/O");
			e.printStackTrace();
			sb.append("");
		}
		// Log.i(Agent, "I/O Ok");

		try
		{
			urlConn.disconnect();
		}
		catch (Exception e)
		{
			Log.i(Agent, "Exception disconnect");
			e.printStackTrace();
		}
		Log.i(Agent, "http Req end");

		return sb;
	}

	public static StringBuffer fetchUrl(String surl, String us, String pw)
	{
		URL url = null;
		try
		{
			url = new URL(surl);
		}
		catch (MalformedURLException e)
		{
		}

		if (url == null)
		{
			return null;
		}

		String uspw = us + ":" + pw;
		String eup = Base64.encodeToString(uspw.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP);

		StringBuffer sb = new StringBuffer();
		HttpURLConnection urlConn;
		try
		{
			urlConn = (HttpURLConnection) url.openConnection();// 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔稿复閿濆繑瀚归柨鐔峰鏉堢偓瀚归崣顏堟晸鐟欐帟鎻幏鐑芥晸閺傘倖瀚归敓锟�?闁跨喐鏋婚幏宄扮杽闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹烽攱鐥呴柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻敓锟�?
		}
		catch (IOException e)
		{
			sb.append("");
			return sb;
		}

		try
		{
			urlConn.setRequestMethod("GET");
			// urlConn.setRequestProperty("Authorization", "Basic " + eup);
			urlConn.setConnectTimeout(30000);
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.connect(); // 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�

			// int res =urlConn.getResponseCode ();
			// Log.i("RES", String.valueOf(res));
			InputStream input = urlConn.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(inputReader);
			String inputLine = null;
			while ((inputLine = reader.readLine()) != null)
			{
				// if (M3App.D) Log.i("CONN", inputLine);
				sb.append(inputLine).append("\n");
			}
			reader.close();
			inputReader.close();
			input.close();
			urlConn.disconnect();
		}
		catch (IOException e)
		{
			urlConn.disconnect();
		}
		return sb;
	}
}
