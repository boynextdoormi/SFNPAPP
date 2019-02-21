package com.finley.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

public class USBHelper
{
	private static final boolean D = false;
	private static final String TAG = "USBHelper";

	public static final int UART_STATE_NONE = 0;
	public static final int UART_STATE_GRANT = 1;
	public static final int UART_STATE_BIND = 2;
	public static final int UART_STATE_READY = 3;

	public static final int UART_ERR_NOMGR = -100;
	public static final int UART_ERR_NOATT = -101;
	public static final int UART_ERR_NOGRANT = -102;
	public static final int UART_ERR_NOTCONN = -103;
	public static final int UART_ERR_IOERROR = -104;

	private static final int USB_CTRL_TIMEO = 200;
	private static final int USB_READ_TIMEO = 128;

	public static final int PL2303VENDOR = 0x067b;
	public static final int PL2303PRODID = 0x2303;
	public static final String PL2303NAME = "PL2303";
	public static final int DEV_PL2303 = 1;

	public static final int FT232VENDOR = 0x0403;
	public static final int FT232PRODID = 0x6001;
	public static final String FT232NAME = "FT232";
	public static final int DEV_FT232 = 2;

	public static final int CH340VENDOR = 0x1a86;
	public static final int CH340PRODID = 0x7523;
	public static final int CH341PRODID = 0x5523;
	public static final String CH340NAME = "CH340";
	public static final String CH341NAME = "CH341";
	public static final int DEV_CH340 = 3;
	public static final int DEV_CH341 = 4;

	public static final int CP2102VENDOR = 0x10C4;
	public static final int CP2102PRODID = 0xEA60;
	public static final String CP2102NAME = "CP2102";
	public static final int DEV_CP2102 = 5;

	public static final int CC2531VENDOR = 0x0451;
	public static final int CC2531PRODID = 0x16A8;
	public static final int CC2531PRODIDe = 0x16AE;
	public static final String CC2531NAME = "CC2531";
	public static final int DEV_CC2531 = 6;

	private final Context mContext;
	private OnUSBListener fListener = null; // �ص����亯��
	private USBIOThread mThread = null;
	private int mState = UART_STATE_NONE;

	private int mBaud = 4800;
	private int mParity = UARTProp.PARITY_NONE;
	private int mDbits = UARTProp.DATABITS_8;
	private int mSbits = UARTProp.STOPBITS_1;
	private int mDTRRTS = 0;

	public static interface OnUSBListener // �ص����亯��ӿ�?
	{
		public void OnStateChanged(int state);

		public void OnDataRecved(byte[] b, int len);

		public void OnError(int err, String text);
	}

	public USBHelper(Context context)
	{
		mContext = context;
	}

	public void setOnUSBListener(OnUSBListener oc)
	{
		fListener = oc;
	}

	USBReceiver.OnUSBListener mListener = new USBReceiver.OnUSBListener()
	{
		@Override
		public void OnStateChanged(int vid, int pid, int state)
		{
			if (fListener != null)
			{
				fListener.OnStateChanged(state);
				switch (state)
				{
				case USBReceiver.UART_USB_DET:
					Close();
					break;
				case USBReceiver.UART_USB_ATT:
					Open(vid, pid, mBaud, mParity, mDbits, mSbits);
					break;
				case USBReceiver.UART_USB_GOK:
					Open(vid, pid, mBaud, mParity, mDbits, mSbits);
					break;
				}
			}
		}

		@Override
		public void OnDeviceInfo(int vid, int pid, String name, String sinfo)
		{
			String ss = "vid=" + vid + " pid=" + pid + "\n" + name + " " + sinfo;
			Toast.makeText(mContext, ss, Toast.LENGTH_SHORT).show();
		}
	};

	public static boolean isUSBFeatureSupported(final Context mContext)
	{
		boolean bool = mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host");
		return bool;
	}

	private USBReceiver mUsbReceiver = null;

	public static void installReceiver(Context mContext)
	{
		USBReceiver.installReceiver(mContext);
		USBReceiver.USBDevFilterAdd(DEV_PL2303, PL2303VENDOR, PL2303PRODID, PL2303NAME);
		USBReceiver.USBDevFilterAdd(DEV_FT232, FT232VENDOR, FT232PRODID, FT232NAME);
		USBReceiver.USBDevFilterAdd(DEV_CH340, CH340VENDOR, CH340PRODID, CH340NAME);
		USBReceiver.USBDevFilterAdd(DEV_CH341, CH340VENDOR, CH341PRODID, CH341NAME);
		USBReceiver.USBDevFilterAdd(DEV_CP2102, CP2102VENDOR, CP2102PRODID, CP2102NAME);
	}

	public void unInstallReceiver(Context mContext)
	{
		USBReceiver.unInstallReceiver(mContext);
	}

	private synchronized void setState(int b)
	{
		mState = b;
		if (fListener != null)
		{
			fListener.OnStateChanged(mState);
		}
	}

	public synchronized boolean Open(int vid, int pid, int baud, int parity, int dbits, int sbits)
	{
		mBaud = baud;
		mParity = parity;
		mDbits = dbits;
		mSbits = sbits;

		// �������ǰ�򿪵��߳�?, �ս�֮
		if (mThread != null)
		{
			mThread.Cancel();
			mThread = null;
		}
		mThread = new USBIOThread(mContext, vid, pid, baud, parity, dbits, sbits);
		mThread.start();
		return true;
	}

	public synchronized boolean Close()
	{
		if (mThread != null)
		{
			mThread.Cancel();
			mThread = null;
		}
		setState(UART_STATE_NONE);
		return true;
	}

	public synchronized int Send(byte[] dat, int len)
	{
		USBIOThread r = null;
		synchronized (this)
		{
			r = mThread;
		}
		if (r != null)
		{
			return r.Send(dat, len);
		}
		return -2;
	}

	private class USBIOThread extends Thread
	{
		private final Context mContext;
		private final int mVid, mPid;

		private USBReceiver.USBDevInfo dev;

		private UsbManager mManager;
		private UsbDeviceConnection mConn;
		private UsbInterface mIntf;
		private UsbEndpoint mEpi;
		private UsbEndpoint mEpo;
		// private UsbEndpoint mEpr;

		private int mBaud = 9600;
		private int mParity = UARTProp.PARITY_NONE;
		private int mDbits = UARTProp.DATABITS_8;
		private int mSbits = UARTProp.STOPBITS_1;
		private int maxInSize = 8;
		private int maxOutSize = 8;

		private boolean mLoop = false;
		private boolean mInThread = false;

		private byte[] recvBuf;
		private byte[] eb;

		public USBIOThread(Context context, int vid, int pid, int baud, int parity, int dbits, int sbits)
		{
			mVid = vid;
			mPid = pid;
			mContext = context;
			mBaud = baud;
			mParity = parity;
			mDbits = dbits;
			mSbits = sbits;
		}

		private void fillBauds(byte[] setting, int baud)
		{
			setting[0] = (byte) ((baud) & 0xff);
			setting[1] = (byte) ((baud >> 8) & 0xff);
			setting[2] = (byte) ((baud >> 16) & 0xff);
			setting[3] = (byte) ((baud >> 24) & 0xff);
		}

		private void fillParms(byte[] setting)
		{
			fillBauds(setting, mBaud);
			setting[4] = (byte) (mParity & 0xff);
			setting[5] = (byte) (mSbits & 0xff);
			setting[6] = (byte) (mDbits & 0xff);
		}

		private boolean PL2303Setup()
		{
			int ret = 0;
			byte[] setting = new byte[7];
			fillParms(setting);

			// Flow Control
			ret = mConn.controlTransfer(0xa1, 0x21, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (D)
				Log.i("PL FlowCtrl", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(0x21, 0x20, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (D)
				Log.i("PL Ret1", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(0x21, 0x23, 0, 0, null, 0, USB_CTRL_TIMEO);
			if (D)
				Log.i("PL Ret2", "ret=" + ret);
			if (ret < 0)
				return false;

			// Flow Control
			/*
			 * ret = mConn.controlTransfer(0x40, 1, 0, 0, null, 0, USBTIMEO); if
			 * (D) Log.i("PL Ret2", "ret="+ret); if (ret<0) return false;
			 * 
			 * ret = mConn.controlTransfer(0x40, 1, 1, 0, null, 0, USBTIMEO); if
			 * (D) Log.i("PL Ret2", "ret="+ret); if (ret<0) return false;
			 * 
			 * ret = mConn.controlTransfer(0x40, 1, 2, 0x44, null, 0, USBTIMEO);
			 * if (D) Log.i("PL Ret2", "ret="+ret); if (ret<0) return false;
			 */

			// Set DTR & RTS
			ret = mConn.controlTransfer(0x21, 0x22, mDTRRTS, 0, setting, 7, USB_CTRL_TIMEO);
			if (D)
				Log.i("PL DTR-RTS", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(0x21, 0x20, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (D)
				Log.i("PL Ret3", "ret=" + ret);
			return true;
		}

		private boolean FT232Setup()
		{
			int ret;
			ret = 0;

			// reset
			ret = mConn.controlTransfer(0x40, 0x00, 0x00, 0x00, null, 0, 0);
			if (D)
				Log.i("FT Ret1", "reset ret=" + ret);
			if (ret < 0)
				return false;

			// Clear Rx
			ret = mConn.controlTransfer(0x40, 0x00, 0x01, 0x00, null, 0, 0);
			if (D)
				Log.i("FT Ret2", "reset ret=" + ret);
			if (ret < 0)
				return false;

			// Clear Tx
			ret = mConn.controlTransfer(0x40, 0x00, 0x02, 0x00, null, 0, 0);
			if (D)
				Log.i("FT Ret3", "reset ret=" + ret);
			if (ret < 0)
				return false;

			// Set the port characteristics
			int ch = mDbits & 0xff;
			ch |= (mParity << 8);
			ch |= (mSbits << 11);
			ret = mConn.controlTransfer(0x40, 0x04, ch, 0x00, null, 0, 0);
			if (D)
				Log.i("FT Ret4", "reset ret=" + ret);
			if (ret < 0)
				return false;

			// Set baud rate
			ch = FT232.getBaudDivider(mBaud);
			ret = mConn.controlTransfer(0x40, 0x03, ch, 0x00, null, 0, 0);
			if (D)
				Log.i("FT Ret5", "reset ret=" + ret);
			if (ret < 0)
				return false;
			return true;
		}

		private boolean CH340Setup()
		{
			int ret = 0;

			long factor = 1532620800 / mBaud;
			int divisor = 3;
			while ((factor > 0xfff0) && (divisor > 0))
			{
				factor >>= 3;
				divisor--;
			}
			factor = 0x10000 - factor;
			short a = (short) ((factor & 0xff00) | divisor);
			short b = (short) (factor & 0xff);
			byte[] buffer = new byte[8];

			int reqVendorIn = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN;
			int reqVendorOut = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_OUT;

			ret = mConn.controlTransfer(reqVendorIn, 0x5f, 0, 0, buffer, 8, 1000);
			if (D)
				Log.i("CH Ret1", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0xa1, 0, 0, null, 0, 1000);
			if (D)
				Log.i("CH Ret2", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0x9a, 0x1312, a, null, 0, 1000);
			if (D)
				Log.i("CH Ret3", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0x9a, 0x0f2c, b, null, 0, 1000);
			if (D)
				Log.i("CH Ret4", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorIn, 0x95, 0x2518, 0, buffer, 8, 1000);
			if (D)
				Log.i("CH Ret5", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0x9a, 0x0518, 0x0050, null, 0, 1000);
			if (D)
				Log.i("CH Ret6", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0xa1, 0x501f, 0xd90a, null, 0, 1000);
			if (D)
				Log.i("CH Ret7", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0x9a, 0x1312, a, null, 0, 1000);
			if (D)
				Log.i("CH Ret8", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0x9a, 0x0f2c, b, null, 0, 1000);
			if (D)
				Log.i("CH Ret9", "ret=" + ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(reqVendorOut, 0xa4, 0, 0, null, 0, 1000);
			if (D)
				Log.i("CH RetA", "ret=" + ret);
			if (ret < 0)
				return false;

			return true;
		}

		private boolean CC2531Setup()
		{
			int ret = 0;

			byte[] setting = new byte[7];
			fillParms(setting);

			// SET CTRL Line ST
			ret = mConn.controlTransfer(0x21, 0x22, 0x02, 0x00, null, 0, 0);

			// Get Line Coding
			ret = mConn.controlTransfer(0xa1, 0x21, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (fListener != null)
				fListener.OnError(-1, "1 Get Line Coding : ret=" + ret);
			// if (D) Log.i("PL FlowCtrl", "ret="+ret);
			if (ret < 0)
				return false;

			// Set Line Coding
			fillParms(setting);
			ret = mConn.controlTransfer(0x21, 0x20, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (fListener != null)
				fListener.OnError(-1, "2 Set Line Coding : ret=" + ret);
			// if (D) Log.i("PL Ret1", "ret="+ret);
			if (ret < 0)
				return false;

			// Get Line Coding
			fillParms(setting);
			ret = mConn.controlTransfer(0xa1, 0x21, 0, 0, setting, 7, USB_CTRL_TIMEO);
			if (fListener != null)
				fListener.OnError(-1, "3 Get Line Coding : ret=" + ret);
			// if (D) Log.i("PL FlowCtrl", "ret="+ret);
			if (ret < 0)
				return false;

			ret = mConn.controlTransfer(0x21, 0x22, 0x02, 0x00, null, 0, USB_CTRL_TIMEO);
			if (fListener != null)
				fListener.OnError(-1, "4 Get Line Coding : ret=" + ret);
			// if (D) Log.i("PL Ret2", "ret="+ret);
			if (ret < 0)
				return false;

			return true;
		}

		private int CP210xSetConfig(int request, int value, byte[] buffer, int length)
		{
			int result = mConn.controlTransfer(CP210x.REQTYPE_HOST_TO_INTERFACE, request, value, 0x0 /* index */,
					buffer, length, USB_CTRL_TIMEO);
			if (result != length)
			{
			}
			return result;
		}

		private int CP210xSetConfigSingle(int request, int value)
		{
			return CP210xSetConfig(request, value, null, 0x0);
		}

		private void CP210xSetBaud(int baudrate)
		{
			byte[] setting = { 0, 0, 0, 0 };
			fillBauds(setting, baudrate);

			CP210xSetConfig(CP210x.SET_BAUDRATE, 0x0000, setting, setting.length);
		}

		private void CP210xSetParameters(int baudRate, int dataBits, int stopBits, int parity)
		{
			CP210xSetBaud(baudRate);

			int config = CP210x.getParmMask(dataBits, stopBits, parity);
			int result = mConn.controlTransfer(CP210x.REQTYPE_HOST_TO_INTERFACE, CP210x.SET_LINE_CTL, config,
					0 /* index */, null, 0, USB_CTRL_TIMEO);
			if (result != 0)
			{
			}
		}

		private boolean CP2102Setup()
		{
			CP210xSetConfigSingle(CP210x.IFC_ENABLE, CP210x.ENABLE);

			CP210xSetConfig(CP210x.SET_CHARS, 0x0000, CP210x.SET_CHARS_DATA, CP210x.SET_CHARS_DATA.length);
			CP210xSetConfig(CP210x.SET_FLOW, 0x0000, CP210x.SET_FLOW_DATA, CP210x.SET_FLOW_DATA.length);

			CP210xSetParameters(mBaud, mDbits, mSbits, mParity);

			CP210xSetConfigSingle(CP210x.SET_MHS, CP210x.CTRL_RTS_W); // 0x0200
			CP210xSetConfigSingle(CP210x.SET_MHS, CP210x.CTRL_DTR_W + CP210x.CTRL_DTR); // 0x0101
			CP210xSetConfigSingle(CP210x.SET_BREAK, CP210x.BREAK_OFF);

			return true;
		}

		// ==========================================
		private boolean getEndPoints()
		{
			// 查接口，找到输入输出端点
			// if (D) Log.i(TAG, "step 4.");
			mEpo = null;
			mEpi = null;
			// mEpr = null;

			int mIntfCount = dev.devUSB.getInterfaceCount();
			for (int k = 0; k < mIntfCount; k++)
			{
				mIntf = dev.devUSB.getInterface(k);
				for (int i = 0; i < mIntf.getEndpointCount(); i++)
				{
					UsbEndpoint ep = mIntf.getEndpoint(i);
					switch (ep.getType())
					{
					case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
					case UsbConstants.USB_ENDPOINT_XFER_ISOC:
						break;
					case UsbConstants.USB_ENDPOINT_XFER_BULK:
						if (ep.getDirection() == UsbConstants.USB_DIR_IN)
						{
							mEpi = ep;
						}
						else
						{
							mEpo = ep;
						}
						break;
					case UsbConstants.USB_ENDPOINT_XFER_INT:
						/*
						 * if (ep.getDirection()==UsbConstants.USB_DIR_IN) {
						 * mEpr = ep; }
						 */
						break;
					}
				}
				if (mEpo != null && mEpi != null)
				{
					return true;
				}
				mConn.releaseInterface(mIntf);
			}
			// if (D) Log.i("EndPoint", "Found !");
			return false;
		}

		private boolean prepare()
		{// 查找可用设备
			// if (D) Log.i(TAG, "step 1.");
			dev = new USBReceiver.USBDevInfo();

			dev.venID = mVid;
			dev.prodID = mPid;
			try
			{
				if (!USBReceiver.getDeviceByProdId(mContext, dev))
				{
					if (fListener != null)
					{
						fListener.OnError(UART_ERR_NOATT, "No device attached.");
					}
					return false;
				}

				mManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
				if (mManager == null)
				{
					if (fListener != null)
					{
						fListener.OnError(UART_ERR_NOMGR, "USB manager not found.");
					}
					return false;
				}

				// 看是否有权限
				if (!mManager.hasPermission(dev.devUSB))
				{
					// 获得device的临时接�? 权限
					PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0,
							new Intent(USBReceiver.ACTION_USB_DEVICE_PERMISSION), 0);
					mManager.requestPermission(dev.devUSB, mPermissionIntent);
					if (fListener != null)
					{
						fListener.OnError(UART_ERR_NOGRANT, "Not grant .");
					}
					return false;
				}

				// 打开设备
				mLoop = true;
				mConn = mManager.openDevice(dev.devUSB);
				if (mConn == null)
				{
					if (fListener != null)
					{
						fListener.OnError(UART_ERR_NOTCONN, "Failed connect.");
					}
					return false;
				}

				// SET_LINE_CODING
				if (D)
					Log.i(TAG, "Baud=" + mBaud);
				boolean bSetup;
				switch (dev.devID)
				{
				case DEV_PL2303:
					bSetup = PL2303Setup();
					break;

				case DEV_CH340:
				case DEV_CH341:
					bSetup = CH340Setup();
					break;

				case DEV_FT232:
					bSetup = FT232Setup();
					break;

				case DEV_CP2102:
					bSetup = CP2102Setup();
					break;

				case DEV_CC2531:
					bSetup = CC2531Setup();
					if (fListener != null)
					{
						fListener.OnError(-1, "Setup cc2531.");
					}
					break;

				default:
					bSetup = false;
					break;
				}

				if (!bSetup)
				{
					mConn.close();
					return false;
				}

				// 查接口，找到输入输出端点
				// if (D) Log.i(TAG, "step 4.");
				if (!getEndPoints())
				{
					mConn.releaseInterface(mIntf);
					mConn.close();
					return false;
				}

				mConn.claimInterface(mIntf, true);
				maxInSize = mEpi.getMaxPacketSize();
				maxOutSize = mEpo.getMaxPacketSize();

				recvBuf = new byte[maxInSize];

				return true;
			}
			catch (Exception e)
			{
			}
			return false;
		}

		public void run()
		{
			int ret;

			mInThread = true;
			setState(UART_STATE_NONE);

			// 进入准备
			if (!prepare())
			{
				mInThread = false;
				return;
			}

			// 进入读循环
			// if (D) Log.i(TAG, "step 6.");
			setState(UART_STATE_READY);
			mLoop = true;
			while (mLoop)
			{
				ret = -1;
				try
				{
					ret = mConn.bulkTransfer(mEpi, recvBuf, recvBuf.length, USB_READ_TIMEO);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					ret = -1;
				}
				if (ret > 0)
				{
					switch (dev.devID)
					{
					case DEV_PL2303:
						eb = new byte[ret];
						System.arraycopy(recvBuf, 0, eb, 0, ret);
						if (fListener != null)
							fListener.OnDataRecved(eb, ret);
						break;

					case DEV_FT232:
						if (ret >= 2)
						{
							ret -= 2;
							short val = (short) ((recvBuf[0] << 8) | recvBuf[1]);
							switch (val)
							{
							case 0x0160:
								eb = new byte[ret];
								System.arraycopy(recvBuf, 2, eb, 0, ret);
								if (fListener != null)
									fListener.OnDataRecved(eb, ret);
								break;
							case 0x0170:
								break;
							}
						}
						else
						{
							if (D)
								Log.i("RECV", Util.bytes2hex(recvBuf, ret));
						}
						break;
					case DEV_CH340:
					case DEV_CH341:
						if (D)
							Log.i("RECV", Util.bytes2hex(recvBuf, ret));
						eb = new byte[ret];
						System.arraycopy(recvBuf, 0, eb, 0, ret);
						if (fListener != null)
							fListener.OnDataRecved(eb, ret);
						break;

					case DEV_CP2102:
						eb = new byte[ret];
						System.arraycopy(recvBuf, 0, eb, 0, ret);
						if (fListener != null)
							fListener.OnDataRecved(eb, ret);
						break;

					case DEV_CC2531:
						eb = new byte[ret];
						System.arraycopy(recvBuf, 0, eb, 0, ret);
						if (fListener != null)
							fListener.OnDataRecved(eb, ret);
						break;

					}
				}
			}

			mConn.releaseInterface(mIntf);
			mConn.close();

			mInThread = false;
		}

		public void Cancel()
		{
			mLoop = false;
			try
			{
				while (mInThread)
				{
					Thread.sleep(50);
				}
				;
			}
			catch (Exception e)
			{
			}
		}

		public int Send(byte[] buf, int len)
		{
			int r;
			{
				int ipos = 0;
				while (ipos < len)
				{
					int qlen = maxOutSize;
					if (qlen > len - ipos)
						qlen = len - ipos;
					byte[] ob = new byte[qlen];
					System.arraycopy(buf, ipos, ob, 0, qlen);
					ipos += qlen;

					r = -1;
					try
					{
						r = mConn.bulkTransfer(mEpo, ob, qlen, qlen * 10);
					}
					catch (Exception e)
					{
					}
					ob = null;
					if (r >= 0)
					{
						if (D)
							Log.i("Send", "len=" + r);
					}
					else
					{
						return -1;
					}
				}
				if (D)
					Log.i("Send", "len=" + len + " dat=" + Util.bytes2hex(buf, len));
				return len;
			}
		}
	}

	public static class Util
	{
		public static String byte2hex(byte b)
		{
			String[] tab = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
			return tab[(b >> 4) & 0x0f] + tab[(b & 0x0f)];
		}

		public static String bytes2hex(byte[] b, int len)
		{
			String s = "";
			for (int i = 0; i < len; i++)
			{
				s += byte2hex(b[i]) + " ";
			}
			return s;
		}

		public static String bytes2hext(byte[] b, int len)
		{
			String s = "";
			for (int i = 0; i < len; i++)
			{
				s += byte2hex(b[i]);
			}
			return s;
		}

		public static String short2hex(short b)
		{
			return byte2hex((byte) (b >> 8)) + byte2hex((byte) (b));
		}
	};

	public static class UARTProp
	{
		public static final int PARITY_MASK = 0;
		public static final int PARITY_NONE = 0;
		public static final int PARITY_EVEN = 1;
		public static final int PARITY_ODD = 2;
		public static final int PARITY_MARK = 4;
		public static final int PARITY_SPACE = 8;

		public static final int DATABITS_5 = 5;
		public static final int DATABITS_6 = 6;
		public static final int DATABITS_7 = 7;
		public static final int DATABITS_8 = 8;

		public static final int STOPBITS_1 = 0;
		public static final int STOPBITS_15 = 1;
		public static final int STOPBITS_2 = 2;

		public static final int[] UART_Baud_Table = { 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600,
				115200, 230400, 460800, 921600 };

		public int Baudrate = 9600;
		public int Parity = PARITY_NONE;
		public int DataBits = DATABITS_8;
		public int StopBits = STOPBITS_1;

	};

	public static class FT232
	{

		public static final int[] ft232baudIndex = { 0x2710, 0x1388, 0x09c4, 0x04e2, 0x0271, 0x4138, 0x80d0, 0x809c,
				0xc04e, 0x0034, 0x001a, 0x000d, 0x4006, 0x8003 };

		public static int getBaudDivider(int baud)
		{
			// Set baud rate
			int ch = 0x4138; // default = 9600bps
			for (int i = 0; i < UARTProp.UART_Baud_Table.length; i++)
			{
				if (UARTProp.UART_Baud_Table[i] == baud)
				{
					ch = ft232baudIndex[i];
					break;
				}
			}
			return ch;
		}
	};

	public static class CP210x
	{
		// CP210x Config request types
		public static final int REQTYPE_HOST_TO_INTERFACE = 0x41;
		public static final int REQTYPE_INTERFACE_TO_HOST = 0xc1;
		public static final int REQTYPE_HOST_TO_DEVICE = 0x40;
		public static final int REQTYPE_DEVICE_TO_HOST = 0xc0;

		// CP210x Config request codes
		public static final int IFC_ENABLE = 0x00;
		public static final int SET_BAUDDIV = 0x01;
		public static final int GET_BAUDDIV = 0x02;
		public static final int SET_LINE_CTL = 0x03;
		public static final int GET_LINE_CTL = 0x04;
		public static final int SET_BREAK = 0x05;
		public static final int IMM_CHAR = 0x06;
		public static final int SET_MHS = 0x07;
		public static final int GET_MDMSTS = 0x08;
		public static final int SET_XON = 0x09;
		public static final int SET_XOFF = 0x0A;
		public static final int SET_EVENTMASK = 0x0B;
		public static final int GET_EVENTMASK = 0x0C;
		public static final int SET_CHAR = 0x0D;
		public static final int GET_CHARS = 0x0E;
		public static final int GET_PROPS = 0x0F;
		public static final int GET_COMM_STATUS = 0x10;
		public static final int RESET = 0x11;
		public static final int PURGE = 0x12;
		public static final int SET_FLOW = 0x13;
		public static final int GET_FLOW = 0x14;
		public static final int EMBED_EVENTS = 0x15;
		public static final int GET_EVENTSTATE = 0x16;
		public static final int SET_CHARS = 0x19;
		public static final int GET_BAUDRATE = 0x1D;
		public static final int SET_BAUDRATE = 0x1E;

		// (SET|GET)_LINE_CTL bit mask
		public static final int BITS_DATA_MASK = 0X0f00;
		public static final int BITS_DATA_5 = 0X0500;
		public static final int BITS_DATA_6 = 0X0600;
		public static final int BITS_DATA_7 = 0X0700;
		public static final int BITS_DATA_8 = 0X0800;
		public static final int BITS_DATA_9 = 0X0900;

		public static final int BITS_PARITY_MASK = 0x00f0;
		public static final int BITS_PARITY_NONE = 0x0000;
		public static final int BITS_PARITY_ODD = 0x0010;
		public static final int BITS_PARITY_EVEN = 0x0020;
		public static final int BITS_PARITY_MARK = 0x0030;
		public static final int BITS_PARITY_SPACE = 0x0040;

		public static final int BITS_STOP_MASK = 0x000f;
		public static final int BITS_STOP_1 = 0x0000;
		public static final int BITS_STOP_15 = 0x0001;
		public static final int BITS_STOP_2 = 0x0002;

		// IFC_ENABLE
		public static final int DISABLE = 0x0000;
		public static final int ENABLE = 0xffff;

		// SET_BREAK
		public static final int BREAK_ON = 0x0001;
		public static final int BREAK_OFF = 0x0000;

		// (SET_MHS|GET_MDMSTS)
		public static final int CTRL_DTR = 0x0001;
		public static final int CTRL_RTS = 0x0002;
		public static final int CTRL_CTS = 0x0010;
		public static final int CTRL_DSR = 0x0020;
		public static final int CTRL_RING = 0x0040;
		public static final int CTRL_DCD = 0x0080;
		public static final int CTRL_DTR_W = 0x0100;
		public static final int CTRL_RTS_W = 0x0200;

		// From Sniffed Data
		public static final byte[] SET_CHARS_DATA = { (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00,
				(byte) 0x00 };

		// From Sniffed Data
		public static final byte[] SET_FLOW_DATA = { (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00 };

		public static int getParmMask(int dataBits, int stopBits, int parity)
		{
			int config = 0x0000;

			switch (dataBits)
			{
			case UARTProp.DATABITS_5:
				config |= CP210x.BITS_DATA_5;
				break;
			case UARTProp.DATABITS_6:
				config |= CP210x.BITS_DATA_6;
				break;
			case UARTProp.DATABITS_7:
				config |= CP210x.BITS_DATA_7;
				break;
			case UARTProp.DATABITS_8:
				config |= CP210x.BITS_DATA_8;
				break;
			default:
				config |= CP210x.BITS_DATA_8;
				break;
			}

			switch (parity)
			{
			case UARTProp.PARITY_NONE:
				config |= CP210x.BITS_PARITY_NONE;
				break;
			case UARTProp.PARITY_ODD:
				config |= CP210x.BITS_PARITY_ODD;
				break;
			case UARTProp.PARITY_EVEN:
				config |= CP210x.BITS_PARITY_EVEN;
				break;
			case UARTProp.PARITY_MARK:
				config |= CP210x.BITS_PARITY_MARK;
				break;
			case UARTProp.PARITY_SPACE:
				config |= CP210x.BITS_PARITY_SPACE;
				break;
			default:
				config |= CP210x.BITS_PARITY_NONE;
				break;
			}

			switch (stopBits)
			{
			case UARTProp.STOPBITS_1:
				config |= CP210x.BITS_STOP_1;
				break;
			case UARTProp.STOPBITS_15:
				config |= CP210x.BITS_STOP_15;
				break;
			case UARTProp.STOPBITS_2:
				config |= CP210x.BITS_STOP_2;
				break;
			default:
				config |= CP210x.BITS_STOP_1;
				break;
			}
			return config;
		}
	};

	public static class USBReceiver extends BroadcastReceiver
	{
		private static final boolean D = false;
		private static final String TAG = "USBHelper";

		public static final String ACTION_USB_DEVICE_PERMISSION = "com.finley.coxray.USB_PERMISSION";

		public static final int UART_USB_GOK = 1001;
		public static final int UART_USB_GER = 1002;
		public static final int UART_USB_ATT = 1003;
		public static final int UART_USB_DET = 1004;

		// USB设备信息结构
		public static class USBDevInfo
		{
			public int venID;
			public int prodID;
			public int devID;
			public String ProdName;
			UsbDevice devUSB;
		}

		public USBReceiver()
		{
			super();
			fUsbListener = null;
		}

		public USBReceiver(OnUSBListener onUsbLsr)
		{
			super();
			fUsbListener = onUsbLsr;
		}

		// 设备动作监听
		public void onReceive(Context context, android.content.Intent intent)
		{
			UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (dev == null)
			{
				return;
			}
			int vid = dev.getVendorId();
			int pid = dev.getProductId();
			if (!USBDevCanAccept(vid, pid))
			{
				return;
			}

			String action = intent.getAction();

			int act = 0;
			String sac = "";

			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
			{
				act = 1;
				sac = "attached";
			}
			else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
			{
				act = 2;
				sac = "detached";
			}
			else if (ACTION_USB_DEVICE_PERMISSION.equals(action))
			{
				act = 4;
				sac = "permission ";
			}
			else
			{
				return;
			}
			int iid = getUSBDeviceID(vid, pid);

			if (fUsbListener != null)
			{
				// fUsbListener.OnDeviceInfo(vid, pid, ProdName[iid], sac);
			}

			if (iid == 0)
			{
				return;
			}

			// fListener.OnDeviceInfo(vid, pid, ProdName[iid]);

			if (act == 4)
			{
				boolean res = (boolean) intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
				sac += (res ? "true" : "false");
				if (res)
					act = 8;
			}

			// if (D)
			// Log.i(TAG, ProdName[iid] + " " + sac);

			switch (act)
			{
			case 1:
				UsbInfo(dev);
				grantAccess(context, dev);
				if (fUsbListener != null)
				{
					fUsbListener.OnStateChanged(vid, pid, UART_USB_ATT);
				}
				break;
			case 2:
				if (fUsbListener != null)
				{
					fUsbListener.OnStateChanged(vid, pid, UART_USB_DET);
				}
				break;
			case 4:
				if (fUsbListener != null)
				{
					fUsbListener.OnStateChanged(vid, pid, UART_USB_GER);
				}
				break;
			case 8:
				if (fUsbListener != null)
				{
					fUsbListener.OnStateChanged(vid, pid, UART_USB_GOK);
				}
				break;
			}
		}

		// 授权
		public static void grantAccess(final Context context, UsbDevice dev)
		{
			// 得到UsbMnanger对象
			UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
			PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0,
					new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
			manager.requestPermission(dev, mPermissionIntent);
		}

		// 所有设备列表
		public static void checkInfo(final Context mContext)
		{
			UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
			HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

			while (deviceIterator.hasNext())
			{
				UsbDevice device = deviceIterator.next();
				UsbInfo(device);
			}
		}

		// 设备信息显示
		public static void UsbInfo(final UsbDevice device)
		{
			String iss = "\n" + "DeviceID: " + device.getDeviceId() + "\n" + "DeviceName: " + device.getDeviceName()
					+ "\n" + "DeviceClass: " + device.getDeviceClass() + " - "
					+ translateDeviceClass(device.getDeviceClass()) + "\n" + "DeviceSubClass: "
					+ device.getDeviceSubclass() + "\n" + "VendorID: " + device.getVendorId() + "\n" + "ProductID: "
					+ device.getProductId() + "\n";
			if (D)
				Log.i(TAG, iss);
		}

		// 设备类型字符串
		public static String translateDeviceClass(int deviceClass)
		{
			switch (deviceClass)
			{
			case UsbConstants.USB_CLASS_APP_SPEC:
				return "Application specific USB class";
			case UsbConstants.USB_CLASS_AUDIO:
				return "USB class for audio devices";
			case UsbConstants.USB_CLASS_CDC_DATA:
				return "USB class for CDC devices (communications device class)";
			case UsbConstants.USB_CLASS_COMM:
				return "USB class for communication devices";
			case UsbConstants.USB_CLASS_CONTENT_SEC:
				return "USB class for content security devices";
			case UsbConstants.USB_CLASS_CSCID:
				return "USB class for content smart card devices";
			case UsbConstants.USB_CLASS_HID:
				return "USB class for human interface devices (for example, mice and keyboards)";
			case UsbConstants.USB_CLASS_HUB:
				return "USB class for USB hubs";
			case UsbConstants.USB_CLASS_MASS_STORAGE:
				return "USB class for mass storage devices";
			case UsbConstants.USB_CLASS_MISC:
				return "USB class for wireless miscellaneous devices";
			case UsbConstants.USB_CLASS_PER_INTERFACE:
				return "USB class indicating that the class is determined on a per-interface basis";
			case UsbConstants.USB_CLASS_PHYSICA:
				return "USB class for physical devices";
			case UsbConstants.USB_CLASS_PRINTER:
				return "USB class for printers";
			case UsbConstants.USB_CLASS_STILL_IMAGE:
				return "USB class for still image devices (digital cameras)";
			case UsbConstants.USB_CLASS_VENDOR_SPEC:
				return "Vendor specific USB class";
			case UsbConstants.USB_CLASS_VIDEO:
				return "USB class for video devices";
			case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
				return "USB class for wireless controller devices";
			default:
				return "Unknown USB class!";
			}
		}

		public static List<USBDevInfo> usbFilters = new ArrayList<USBDevInfo>();

		public static void USBDevFilterAdd(int devID, int venID, int prodID, String prodName)
		{
			USBDevInfo filter = new USBDevInfo();
			filter.devID = devID;
			filter.venID = venID;
			filter.prodID = prodID;
			filter.ProdName = prodName;
			usbFilters.add(filter);
		}

		public static void USBDevFilterAdd(List<USBDevInfo> f)
		{
			usbFilters.addAll(f);
		}

		public static boolean USBDevCanAccept(int vid, int pid)
		{
			for (USBDevInfo filter : usbFilters)
			{
				if (filter.venID == vid && filter.prodID == pid)
				{
					return true;
				}
			}
			return false;
		}

		// 根据VendorID和ProdID,确定设备型号的内部代码
		public static int getUSBDeviceID(int vid, int pid)
		{
			for (USBDevInfo filter : usbFilters)
			{
				if (filter.venID == vid && filter.prodID == pid)
				{
					return filter.devID;
				}
			}
			return 0;
		}

		// 查找已安装的设备
		public static boolean findUSBDevice(Context context, USBDevInfo dev)
		{
			UsbManager mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

			if (mManager == null)
			{
				return false;
			}

			HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

			while (deviceIterator.hasNext())
			{
				UsbDevice myDevice = deviceIterator.next();
				int vid = myDevice.getVendorId();
				int pid = myDevice.getProductId();
				int iid = getUSBDeviceID(vid, pid);
				if (iid != 0)
				{
					dev.venID = vid;
					dev.prodID = pid;
					dev.devID = iid;
					dev.devUSB = myDevice;
					return true;
				}
			}
			return false;
		}

		// 根据VendorId和ProdId找设备
		public static boolean getDeviceByProdId(Context context, USBDevInfo dev)
		{
			UsbManager mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

			if (mManager == null)
			{
				return false;
			}

			HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

			while (deviceIterator.hasNext())
			{
				UsbDevice myDevice = deviceIterator.next();
				int pid = myDevice.getProductId();
				int vid = myDevice.getVendorId();
				if (dev.prodID == pid && dev.venID == vid)
				{
					dev.devID = getUSBDeviceID(vid, pid);
					dev.devUSB = myDevice;
					return true;
				}
			}
			return false;
		}

		public static interface OnUSBListener // ??????亯?????
		{
			public void OnStateChanged(int vid, int pid, int state);

			public void OnDeviceInfo(int vid, int pid, String name, String sinfo);
		}

		private static USBReceiver mUsbReceiver = null;
		private static OnUSBListener fUsbListener = null;

		public static void SetOnUSBListener(OnUSBListener l)
		{
			fUsbListener = l;
		}

		public static void installReceiver(Context mContext)
		{
			if (mUsbReceiver == null)
			{
				if (D)
					Log.i(TAG, "installReceiver");
				mUsbReceiver = new USBReceiver();
				IntentFilter filter = new IntentFilter();
				filter.addAction(USBReceiver.ACTION_USB_DEVICE_PERMISSION);
				filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
				filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
				mContext.registerReceiver(mUsbReceiver, filter);
			}
		}

		public static void unInstallReceiver(Context mContext)
		{
			if (mUsbReceiver != null)
			{
				if (D)
					Log.i(TAG, "unInstallReceiver");
				mContext.unregisterReceiver(mUsbReceiver);
				mUsbReceiver = null;
			}
		}
	}

}
