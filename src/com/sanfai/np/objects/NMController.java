package com.sanfai.np.objects;

import java.util.ArrayList;
import java.util.List;

import com.finley.helper.ModbusRTU;
import com.sanfai.np.objects.UartUSB.UARTProp;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import weiqian.hardware.HardwareControl;

public class NMController
{

	public static final short MODEL_NEEDLE_PR = 1; // 预刺
	public static final short MODEL_NEEDLE_DN = 2; // 正刺
	public static final short MODEL_NEEDLE_DU = 3; // 异位对刺
	public static final short MODEL_NEEDLE_DX = 4; // 起绒
	public static final short MODEL_NEEDLE_UN = 0; // 未知

	public static final int MODE_UNKNOWN = 0; // 未知
	public static final int MODE_DEBUG = 1; // 手动，调试
	public static final int MODE_STANDALONE = 2; // 单机
	public static final int MODE_COMBINED = 3; // 联动

	private int MachineModel = 1;
	private boolean beSingleMainDrive = false; // 是否为单主机
	private boolean beFeedingDrive = false; // 是否带喂入

	private int RunningMode = MODE_UNKNOWN; // 运行模式
	private boolean beEmergency = false; // 是否在急停状态
	private boolean beRuuning = false; // 是否在运行状态
	private boolean beReset = false; // 是否在复位状态

	private boolean beFpwdVerified = false; // 厂商密码是否验证
	private boolean bePpwdVerified = false; // 工艺密码是否验证
	private OnStatusChangedListener callback = null;
	public OnOperatingListener onOper = null;

	public interface OnStatusChangedListener
	{
		public void OnComStatus(String port, int status);

		public void OnDevAddrChanged(byte addr);

		public void OnRegValUpdated(RequestInfo info);

		public void OnRegValModified(RequestInfo info);
	}

	public interface OnOperatingListener
	{
		public void OnModelChanged(int status);

		public void OnCtrlMode(int mode);

		public void OnEmergency(boolean b);

		public void OnWarnReset(boolean b);

		public void OnRunning(boolean b);
	}

	private final Context mContext;

	public NMController(Context context)
	{
		mContext = context;
	}

	////////////////////////////////////////////////////
	//
	// 针刺机控制板MODBUS寄存器
	//
	public static final short[] NMRegs = new short[0x800];
	//
	public static final short DEVCFG_ADDR_START = 0x0400;

	private RequestThread mRequest = null;
	public byte devAddr = 0;
	public String comPort = "";

	public void setActive(boolean bActive)
	{
		if (bActive)
		{
			if (mRequest == null)
			{
				mRequest = new RequestThread(mContext);
				mRequest.setComPort(comPort);
				mRequest.setDevAddr(devAddr);
				mRequest.setOnStatusChanged(onCommInfo);
				mRequest.start();
			}
		}
		else
		{
			if (mRequest != null)
			{
				mRequest.Cancel();
				mRequest = null;
			}
		}
	}

	public boolean isActive()
	{
		return (mRequest != null);
	}

	public void setComPort(String comport)
	{
		comPort = comport;
	}

	public void setDevAddr(byte devaddr)
	{
		devAddr = devaddr;
	}

	public void setMachineModel(int model)
	{
		switch (model)
		{
		case MODEL_NEEDLE_PR:// = 1; // 正刺
		case MODEL_NEEDLE_DN:// = 2; // 倒刺
		case MODEL_NEEDLE_DU:// = 3; // 同位对刺
			MachineModel = model;
			beSingleMainDrive = true;
			break;
		case MODEL_NEEDLE_DX:// = 4; // 异位对刺
			MachineModel = model;
			beSingleMainDrive = false;
			break;
		}
		if (onOper != null)
		{
			onOper.OnModelChanged(model);
		}
	}

	public static final short Coil_Debug = 0x03f0;
	public static final short Coil_Emerg = 0x03f1;
	public static final short Coil_Stand = 0x03f2;
	public static final short Coil_Start = 0x03f3;
	public static final short Coil_Halt = 0x03f4;
	public static final short Coil_Reset = 0x03f5;

	public static final short HREG_Output = 0x0260; // 实际输出
	public static final short HREG_Upout = 0x026f; // 上位机给出的输出
	public static final short HREG_Model = 0x04c0; // 针刺机型式

	public void setgDebugMode(boolean b)
	{
		RegCoilSet(Coil_Debug, b);
	}

	public void setCtrlMode(int mode)
	{
		switch (mode)
		{
		case NMController.MODE_STANDALONE:
			RegCoilSet(Coil_Stand, false);
			break;
		case NMController.MODE_COMBINED:
			RegCoilSet(Coil_Stand, false);
			break;
		}
	}

	public void setRunning(boolean b)
	{
		if (b)
		{
			RegCoilSet(Coil_Start, true);
		}
		else
		{
			RegCoilSet(Coil_Halt, true);
		}
	}

	public void setEmergency(boolean b)
	{
		RegCoilSet(Coil_Emerg, b);
		beEmergency = b;
		// if (onOper != null)
		// {
		// onOper.OnRunning(false);
		// onOper.OnEmergency(b);
		// }
	}

	public void setReset(boolean b)
	{
		RegCoilSet(Coil_Reset, b);
		beReset = b;
		// if (onOper != null)
		// {
		// onOper.OnWarnReset(b);
		// }
	}

	public short getMachineModel()
	{
		switch (NMRegs[HREG_Model])
		{
		case MODEL_NEEDLE_PR:
		case MODEL_NEEDLE_DN:
		case MODEL_NEEDLE_DU:
		case MODEL_NEEDLE_DX:
			return NMRegs[HREG_Model];
		}
		return MODEL_NEEDLE_UN;
	}

	public short getCtrlMode()
	{
		// 暂时只能从上位机输出得到
		if (0 == (NMRegs[HREG_Upout] & 0x04))
		{
			return NMController.MODE_STANDALONE;
		}
		return NMController.MODE_COMBINED;
	}

	public boolean isEmergency()
	{
		// 从输出---紧急停止反馈---得到,第9位
		if (0 == (NMRegs[HREG_Output] & 0x0200))
		{
			return false;
		}
		return true;
	}

	public boolean isRunning()
	{
		// 从输出---运行指示输出---得到,第14位
		if (0 == (NMRegs[HREG_Output] & 0x4000))
		{
			return false;
		}
		return true;
	}

	public boolean isReset()
	{
		// 暂时只能从上位机输出得到
		return (0 != (NMRegs[HREG_Upout] & 0x04));
	}
	//
	// public boolean isSingleMainDrive()
	// {
	// return beSingleMainDrive;
	// }
	//
	// public boolean isDoubleMainDrive()
	// {
	// return (!beSingleMainDrive);
	// }
	//
	// public boolean hasFeedingDrive()
	// {
	// return beFeedingDrive;
	// }

	public boolean verifyProcessPassword(String pwd)
	{
		return false;
	}

	public boolean verifyFactoryPassword(String pwd)
	{
		return false;
	}

	/////// 修改一个寄存器

	private void RegCoilSet(short addr, boolean OnOff)
	{
		if (mRequest != null)
		{
			short[] vals = new short[1];
			vals[0] = (short) (OnOff ? 0xff00 : 0x0000);
			RequestInfo info = new RequestInfo();
			info.ReqStyle = RequestInfo.REQ_ONESHOT;
			info.RegType = RequestInfo.REQ_WR_CREG;
			info.RegAddr = addr;
			info.RegCount = 1;
			info.RegVals = vals;
			mRequest.addRequest(info);
		}
	}

	public void RegModify(short add, short val)
	{
		if (mRequest != null)
		{
			short[] vals = new short[1];
			vals[0] = val;
			RequestInfo info = new RequestInfo();
			info.ReqStyle = RequestInfo.REQ_ONESHOT;
			info.RegType = RequestInfo.REQ_WR_HREG1;
			info.RegAddr = add;
			info.RegCount = 1;
			info.RegVals = vals;
			mRequest.addRequest(info);
		}
	}

	public void RegFetch(short add, short count)
	{
		RequestInfo info = new RequestInfo();
		info.ReqStyle = RequestInfo.REQ_ONESHOT;
		info.RegType = RequestInfo.REQ_RD_HREG;
		info.RegAddr = add;
		info.RegCount = count;
		info.RegVals = null;
		mRequest.addRequest(info);
	}

	public void RegUpdating(short add, short count)
	{
		RequestInfo info = new RequestInfo();
		info.ReqStyle = RequestInfo.REQ_PERIODIC;
		info.RegType = RequestInfo.REQ_RD_HREG;
		info.RegAddr = add;
		info.RegCount = count;
		info.RegVals = null;
		mRequest.addRequest(info);
	}

	private void RegUpdatingConfig()
	{
		RegFetch((short) 0x0400, (short) 0x0020); // 获取系统配置区块
		RegFetch((short) 0x0420, (short) 0x0020); // 获取通信配置区块
		RegFetch((short) 0x04c0, (short) 0x0020); // 获取设备配置区块
		RegFetch((short) 0x04e0, (short) 0x0020); // 获取设备配置区块
	}

	private void RegUpdatingStatus()
	{
		RegUpdating((short) 0x0000, (short) 0x0020); // 获取系统状态
		RegUpdating((short) 0x0080, (short) 0x0020); // 获取控制状态
		RegUpdating((short) 0x00a0, (short) 0x0008); // 获取开关量输入
		RegUpdating((short) 0x00b0, (short) 0x0008); // 获取模拟量输入

		RegUpdating((short) 0x0200, (short) 0x0020); // 获取控制信号
		RegUpdating((short) 0x0260, (short) 0x0008); // 获取开关量输出
		RegUpdating((short) 0x0270, (short) 0x0008); // 获取模拟量输出
	}

	public void BeginUpdate()
	{
		RegUpdatingConfig();
		RegUpdatingStatus();
	}

	public void setCallback(OnStatusChangedListener cb)
	{
		callback = cb;
	}

	/////
	private RequestThread.OnCommInfoListener onCommInfo = new RequestThread.OnCommInfoListener()
	{
		@Override
		public void OnCommStatus(String port, int status)
		{
			if (callback != null)
			{
				callback.OnComStatus(port, status);
			}
		}

		@Override
		public void OnDevAddrChanged(byte addr)
		{
			// TODO Auto-generated method stub
			if (callback != null)
			{
				callback.OnDevAddrChanged(addr);
			}
		}

		@Override
		public void OnRegValChanged(RequestInfo info)
		{
			// TODO Auto-generated method stub
			short i;
			for (i = 0; i < info.RegCount; i++)
			{
				NMRegs[info.RegAddr + i] = info.RegVals[i];
			}
			if (callback != null)
			{
				if ((info.RegType == RequestInfo.REQ_WR_HREG1) || (info.RegType == RequestInfo.REQ_WR_HREG))
				{
					callback.OnRegValModified(info);
				}
				else
				{
					//
					callback.OnRegValUpdated(info);
				}
			}
		}
	};

	//

	//
	// 串行通信-请求线程
	//
	// 通信状态 - 常量
	public final static int COM_NOINST = 0;
	public final static int COM_OPENED = 1;
	public final static int COM_CONNED = 2;

	public static class RequestInfo
	{
		public final static byte REQ_RD_HREG = 0x03;
		public final static byte REQ_RD_IREG = 0x04;

		public final static byte REQ_WR_HREG = 0x10;
		public final static byte REQ_WR_HREG1 = 0x06;
		public final static byte REQ_WR_CREG = 0x05;

		public final static byte REQ_PERIODIC = 0x01;
		public final static byte REQ_ONESHOT = 0x02;

		public byte ReqStyle;
		public byte RegType;
		public short RegAddr;
		public short RegCount;
		public short[] RegVals;
	}

	public static class RequestThread extends Thread
	{
		// 串口通信参数 - 常量
		private static final int NMBaud = 19200;
		private static final int NMData = UARTProp.DATABITS_8;
		private static final int NMStop = UARTProp.STOPBITS_1;
		private static final int NMParity = UARTProp.PARITY_NONE;
		private static final String NSParity = "N";

		private static final int STEP_WAIT = 0;
		private static final int STEP_OPEN = 1;
		private static final int STEP_CERT = 2;
		private static final int STEP_REQ = 5;

		private List<RequestInfo> ReqQueue = new ArrayList<RequestInfo>();

		private int step = 0;
		private Uart sp = null;
		private OnCommInfoListener osListener = null;
		private byte addr = 0;
		private String port = "";

		public interface OnCommInfoListener
		{
			public void OnCommStatus(String port, int status);

			public void OnDevAddrChanged(byte addr);

			public void OnRegValChanged(RequestInfo info);
		}

		private final Context mContext;

		private RequestThread(Context context)
		{
			mContext = context;
		}

		public void setOnStatusChanged(OnCommInfoListener osr)
		{
			osListener = osr;
		}

		public void setComPort(String comport)
		{
			port = comport;
		}

		public void setDevAddr(byte devaddr)
		{
			addr = devaddr;
		}

		public boolean setActive()
		{
			if (sp == null)
			{
				if (HardwareControl.moduleHasLoaded)
				{
					UartCOM uc = new UartCOM();
					sp = uc;
				}
				else
				{
					Toast.makeText(mContext, "ActiveUSB", Toast.LENGTH_SHORT).show();
					UartUSB uu = new UartUSB(mContext);
					sp = uu;
				}
			}
			if (!sp.isActive())
			{
				sp.setUartProp(port, NMBaud, NMData, NSParity, NMStop);
				sp.open();
			}
			return (sp.isActive());
		}

		public boolean setDeactive()
		{
			if (sp != null)
			{
				sp.close();
			}
			return true;
		}

		public boolean isActive()
		{
			if (sp == null)
			{
				return false;
			}
			return (sp.isActive());
		}

		public boolean isSuspended()
		{
			return bSuspend;
		}

		private boolean bSuspend = false;

		public void Suspend()
		{
			try
			{
				synchronized (this)
				{
					bSuspend = true;
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
					bSuspend = false;
					this.notify();
				}
			}
			catch (Exception e)
			{
			}
		}

		private void Sleep(int ms)
		{
			try
			{
				sleep(ms);
			}
			catch (InterruptedException e)
			{
			}
		}

		public void Cancel()
		{
			try
			{
				this.interrupt();
			}
			catch (Exception e)
			{
			}
			try
			{
				if (this.isSuspended())
				{
					this.Resume();
				}
			}
			catch (Exception e)
			{
			}
			setDeactive();
		}

		@Override
		public void run()
		{
			super.run();
			Looper.prepare();

			while (!isInterrupted())
			{
				switch (step)
				{
				case STEP_WAIT:
					Sleep(1500);
					step = STEP_OPEN;
					break;
				case STEP_OPEN:
					step1OpenComm();
					break;
				case STEP_CERT:
					step2VerifyMachine();
					break;
				case STEP_REQ:
					execRequest();
					if (ReqQueue.size() == 0)
					{
						Suspend();
					}
					break;
				}
			}
		}

		public static final int REQ_REPLYOK = 0;
		public static final int REQ_BADREQUEST = -1;
		public static final int REQ_NOREPLAY = -2;
		public static final int REQ_REPLYERR = -3;

		private byte[] req = new byte[256]; // request请求缓冲
		private byte[] rep = new byte[256]; // reply响应缓冲
		private short[] rev = new short[128]; // 读写的值

		public boolean isValidModbusReply(byte[] qbuf, int qlen, byte[] pbuf, int plen)
		{
			if (plen < 5)
			{
				return false;
			}
			short crc = (short) (((pbuf[plen - 2] << 8) & 0xff00) | (pbuf[plen - 1] & 0x00ff));
			short erc = ModbusRTU.MBCRC(pbuf, 0, plen - 2);
			if (crc != erc)
			{
				return false;
			}
			if (qbuf[1] != pbuf[1])
			{
				return false;
			}
			return true;
		}

		public int lawRequest(byte[] qbuf, int qlen, byte[] pbuf, int plen)
		{
			if (qlen != sp.write(qbuf, qlen))
			{
				return 0;
			}
			Sleep(plen + 5);
			int rlen = sp.read(pbuf, plen);
			return rlen;
		}

		public int Request(byte oper, short regaddr, short count, short[] vals)
		{
			int i;
			int reqlen = 0;
			int replen = 0;

			switch (oper)
			{
			case RequestInfo.REQ_RD_HREG:// = 0x03;
			case RequestInfo.REQ_RD_IREG:// = 0x04;
				req[0] = addr;
				req[1] = oper;
				req[2] = (byte) (regaddr >> 8);
				req[3] = (byte) (regaddr);
				req[4] = (byte) (count >> 8);
				req[5] = (byte) (count);
				ModbusRTU.MBCRCFill(req, 0, 6);
				reqlen = 8;
				replen = count * 2 + 5;
				break;

			case RequestInfo.REQ_WR_CREG:// = 0x05;
				req[0] = addr;
				req[1] = oper;
				req[2] = (byte) (regaddr >> 8);
				req[3] = (byte) (regaddr);
				req[4] = (byte) (vals[0] >> 8);
				req[5] = (byte) (vals[0]);
				ModbusRTU.MBCRCFill(req, 0, 6);
				reqlen = 8;
				replen = 8;
				break;

			case RequestInfo.REQ_WR_HREG1:// = 0x06;
				req[0] = addr;
				req[1] = oper;
				req[2] = (byte) (regaddr >> 8);
				req[3] = (byte) (regaddr);
				req[4] = (byte) (vals[0] >> 8);
				req[5] = (byte) (vals[0]);
				ModbusRTU.MBCRCFill(req, 0, 6);
				reqlen = 8;
				replen = 8;
				break;

			case RequestInfo.REQ_WR_HREG:// = 0x10;
				req[0] = addr;
				req[1] = oper;
				req[2] = (byte) (regaddr >> 8);
				req[3] = (byte) (regaddr);
				req[4] = (byte) (count >> 8);
				req[5] = (byte) (count);
				req[6] = (byte) (count * 2);
				for (i = 0; i < count; i++)
				{
					req[6 + i * 2] = (byte) (vals[i] >> 8);
					req[7 + i * 2] = (byte) (vals[i]);
				}
				reqlen = 7 + count * 2;
				ModbusRTU.MBCRCFill(req, 0, reqlen);
				reqlen += 2;
				replen = 8;
				break;

			default:
				return REQ_BADREQUEST;
			}
			if (reqlen == 0)
			{
				return REQ_BADREQUEST;
			}
			int retlen = lawRequest(req, reqlen, rep, replen);
			if (retlen == 0)
			{
				return REQ_NOREPLAY;
			}
			// 若返回期望长度，判断其是否为有效的响应
			if (!((retlen == replen) && isValidModbusReply(req, reqlen, rep, replen)))
			{

				return REQ_REPLYERR;
			}

			switch (oper)
			{
			case RequestInfo.REQ_RD_HREG:// = 0x03;
			case RequestInfo.REQ_RD_IREG:// = 0x04;
				for (i = 0; i < count; i++)
				{
					vals[i] = (short) ((rep[3 + i * 2] << 8) | rep[4 + i * 2]);
				}
				break;

			case RequestInfo.REQ_WR_HREG1:// = 0x06;
				break;

			case RequestInfo.REQ_WR_HREG: // = 0x10;
				break;

			}
			return REQ_REPLYOK;
		}

		private void step1OpenComm()
		{
			if (!isActive())
			{
				try
				{
					if (setActive())
					{
						if (osListener != null)
						{
							osListener.OnCommStatus(port, COM_OPENED);
						}
						Sleep(300);
						step = STEP_CERT;
					}
					else
					{
						step = STEP_WAIT;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					String mm = e.toString();
					Log.i("ER", mm);
				}
			}
		}

		// 与特定地址的设备通信
		private boolean VerifyMachinebyAddr(byte taddr)
		{
			byte saddr = addr;
			addr = taddr;
			if (REQ_REPLYOK == Request(RequestInfo.REQ_RD_HREG, DEVCFG_ADDR_START, (short) 0x0002, rev))
			{
				if (rep[3] == 'S' && rep[4] == 'F' && rep[5] == 'N' && rep[6] == 'M')
				{
					return true;
				}
			}
			addr = saddr;
			return false;
		}

		private void step2VerifyMachine()
		{
			int tryes = 0;
			while ((!isInterrupted()) && (tryes < 3))
			{
				if (VerifyMachinebyAddr(addr))
				{
					if (osListener != null)
					{
						osListener.OnDevAddrChanged(addr);
						osListener.OnCommStatus(port, COM_CONNED);
					}
					step = STEP_REQ;
					return;
				}
				tryes++;
			}

			addr = 1;
			while ((!isInterrupted()))
			{
				if (VerifyMachinebyAddr(addr))
				{
					if (osListener != null)
					{
						osListener.OnDevAddrChanged(addr);
						osListener.OnCommStatus(port, COM_CONNED);
					}
					step = STEP_REQ;
					return;
				}
				else
				{
					Sleep(50);
					addr++;
					addr &= 0xff;
					if (addr == 0)
						addr = 1;
				}
			}
		}

		public void addRequest(RequestInfo req)
		{
			synchronized (this)
			{
				ReqQueue.add(req);
			}
			this.Resume();
		}

		public void cllearAllRequests()
		{
			synchronized (this)
			{
				ReqQueue.clear();
			}
			this.Resume();
		}

		// 执行队列请求
		private void execRequest()
		{
			RequestInfo newreq = null;
			synchronized (this)
			{
				for (RequestInfo req : ReqQueue)
				{// 优先处理写
					if (req.RegType == RequestInfo.REQ_WR_HREG1 || req.RegType == RequestInfo.REQ_WR_HREG
							|| req.RegType == RequestInfo.REQ_WR_CREG)
					{
						ReqQueue.remove(req);
						newreq = req;
						break;
					}
				}
				if (newreq == null)
				{
					for (RequestInfo req : ReqQueue)
					{
						ReqQueue.remove(req);
						newreq = req;
						break;
					}
				}
				if (newreq != null)
				{
					if (newreq.ReqStyle == RequestInfo.REQ_PERIODIC)
					{
						ReqQueue.add(newreq);
					}
				}
			}
			if (newreq != null)
			{
				if ((newreq.RegType == RequestInfo.REQ_WR_HREG1) || (newreq.RegType == RequestInfo.REQ_WR_HREG)
						|| (newreq.RegType == RequestInfo.REQ_WR_CREG))
				{
					if (REQ_REPLYOK == Request(newreq.RegType, newreq.RegAddr, newreq.RegCount, newreq.RegVals))
					{
						if (osListener != null)
						{
							osListener.OnRegValChanged(newreq);
						}
					}
				}
				else
				{
					newreq.RegVals = rev;
					if (REQ_REPLYOK == Request(newreq.RegType, newreq.RegAddr, newreq.RegCount, newreq.RegVals))
					{
						if (osListener != null)
						{
							osListener.OnRegValChanged(newreq);
						}
					}
					newreq.RegVals = null;
				}
			}
		}

	}

	public static void InitComDevice(Context mContext)
	{
		HardwareControl.Init();
		if (HardwareControl.moduleHasLoaded)
		{
			Toast.makeText(mContext, "ActiveCOM", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(mContext, "ActiveUSB", Toast.LENGTH_SHORT).show();
			UartUSB.installReceiver(mContext);
		}
	}
}
