package com.finley.helper;

public class StringHelper
{

	/**
	 * 函数功能： byte转hex 参数： 待转byte 返回值： hex串
	 */
	public static String byte2hex(byte b)
	{
		String[] tab = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
		return tab[(b >> 4) & 0x0f] + tab[(b & 0x0f)];
	}

	/**
	 * 函数功能： byte转hex 参数： 待转byte 返回值： hex串
	 */
	public static String bytes2hex(byte[] b, int len, String sep)
	{
		String s = "";
		for (int i = 0; i < len; i++)
		{
			s += byte2hex(b[i]) + sep;
		}
		return s;
	}

	public static byte asc2byte(byte hb, byte lb)
	{
		byte h, l;
		if (hb >= '0' && hb <= '9')
			h = (byte) (hb - '0');
		else if (hb >= 'a' && hb <= 'f')
			h = (byte) (hb - 'a' + 10);
		else if (hb >= 'A' && hb <= 'F')
			h = (byte) (hb - 'A' + 10);
		else
			h = 0;
		if (lb >= '0' && lb <= '9')
			l = (byte) (hb - '0');
		else if (lb >= 'a' && lb <= 'f')
			l = (byte) (hb - 'a' + 10);
		else if (lb >= 'A' && lb <= 'F')
			l = (byte) (hb - 'A' + 10);
		else
			l = 0;
		return (byte) (((h << 4) & 0xf0) | (l & 0x0f));
	}

	public static void asc2hex(byte[] src, int st, int len, byte[] dst, int pos)
	{
		int i = 0;
		while (i < len)
		{
			dst[pos] = asc2byte(src[st + i], src[st + i + 1]);
			pos++;
			i += 2;
		}
	}

	public static void byte2asc(byte b, byte[] as, int pos)
	{
		byte[] tab = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		as[pos] = tab[(b >> 4) & 0x0f];
		as[pos + 1] = tab[(b & 0x0f)];
	}

	public static void bytes2asc(byte[] b, int st, int len, byte[] dst, int pos)
	{
		int i = 0;
		while (i < len)
		{
			byte2asc(b[st + i], dst, pos + i * 2);
			i++;
		}
	}
}
