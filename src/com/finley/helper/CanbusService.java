package com.finley.helper;

import weiqian.hardware.CanFrame;
import weiqian.hardware.HardwareControl;

public class CanbusService extends Thread
{
	protected OnRecvListener mOnRecv;
	protected int mBaudrate;
	protected int mFd = -1;

	public interface OnRecvListener
	{
		void onRecv(CanFrame vf);

		void onStatusChanged(int devid, boolean b);
	}

	public CanbusService()
	{
	}

	public CanbusService(OnRecvListener listener)
	{
		mOnRecv = listener;
	}

	public CanbusService(String portno, int baudrate, OnRecvListener listener)
	{
		mOnRecv = listener;
		mBaudrate = baudrate;
	}

	public void setOnRecvListener(OnRecvListener listener)
	{
		mOnRecv = listener;
	}

	@Override
	public void destroy()
	{
		if (mFd != -1)
		{
			HardwareControl.CloseSerialPort(mFd);
			mFd = -1;
		}
	}

	public boolean Open(int baud)
	{
		mBaudrate = baud;
		return Open();
	}

	public boolean Open()
	{
		if (mFd != -1)
		{
			HardwareControl.CloseCan();
			mFd = -1;
		}
		HardwareControl.InitCan(mBaudrate);
		mFd = HardwareControl.OpenCan();
		return (mFd != -1);
	}

	public boolean isOpened()
	{
		return (mFd != -1);
	}

	public void EnableReadThread()
	{
		this.start();
	}

	public void Suspend()
	{
		try
		{
			synchronized (this)
			{
				this.wait();
			}
		}
		catch (Exception e)
		{
		}
	}

	public void Resume()
	{
		try
		{
			synchronized (this)
			{
				this.notify();
			}
		}
		catch (Exception e)
		{
		}
	}

	protected void OnRecv(CanFrame fr)
	{
		synchronized (this)
		{
			HardwareControl.CanRead(fr, 100);
		}
	}

	protected final CanFrame cfRecv = new CanFrame();
	protected final CanFrame cfSend = new CanFrame();

	@Override
	public void run()
	{
		super.run();
		while (!isInterrupted())
		{
			CanFrame rt = HardwareControl.CanRead(cfRecv, 1);
			if (rt != null)
			{
				OnRecv(cfRecv);
			}
		}
	}

	public int write(int id, byte[] buf)
	{
		if (mFd == -1)
		{
			return -1;
		}
		synchronized (this)
		{
			return HardwareControl.CanWrite(id, buf);
		}
	}

	public int write(CanFrame fr)
	{
		if (mFd == -1)
		{
			return -1;
		}
		synchronized (this)
		{
			return HardwareControl.CanWrite(fr.can_id, fr.data);
		}
	}

	public synchronized void cancel()
	{
		try
		{
			this.interrupt();
			// 因为读超时为500ms
			sleep(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (mFd != -1)
		{
			HardwareControl.CloseSerialPort(mFd);
			mFd = -1;
		}
	}
}
