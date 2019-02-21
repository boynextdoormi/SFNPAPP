package com.sanfai.np.objects;

import weiqian.hardware.HardwareControl;

public class UartCOM extends Uart
{
	private int mFd;

	public UartCOM()
	{
		mFd = -1;
	}

	@Override
	boolean isActive()
	{
		return (mFd > 0);
	}

	@Override
	boolean open()
	{
		if (!HardwareControl.moduleHasLoaded)
		{
			try
			{
				HardwareControl.Init();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		if (!HardwareControl.moduleHasLoaded)
		{
			return false;
		}

		try
		{
			mFd = HardwareControl.OpenSerialPort(pPort, pBaudrate, pDataBits, psParity, pStopBits);
			return (mFd > 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	void close()
	{
		HardwareControl.CloseSerialPort(mFd);
		mFd = -1;
	}

	private byte[] rdbuf = new byte[32];
	private static final long COMUART_RTMO = 50;

	@Override
	int read(byte[] buff, int count)
	{
		int rd = 0;
		int rc = 0;
		long ms = System.currentTimeMillis() + COMUART_RTMO;
		while (rc < count && ms > System.currentTimeMillis())
		{
			int lf = count - rc;
			if (lf > rdbuf.length)
				lf = rdbuf.length;
			rd = HardwareControl.ReadSerialPort(mFd, rdbuf, lf);
			if (rd > 0)
			{
				System.arraycopy(rdbuf, 0, buff, rc, rd);
				rc += rd;
			}
		}
		return rc;
		// return HardwareControl.ReadSerialPort(mFd, buff, count);
	}

	@Override
	int write(byte[] buff, int count)
	{
		return HardwareControl.WriteSerialPort(mFd, buff, count);
	}

	@Override
	String read()
	{
		byte[] buff = new byte[64];
		read(buff, 64);
		return buff.toString();
	}

	@Override
	void write(String buff)
	{
		write(buff.getBytes(), buff.length());
	}
}
