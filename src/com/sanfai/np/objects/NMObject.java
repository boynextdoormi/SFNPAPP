package com.sanfai.np.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NMObject
{
	public static final String NMDefFile = "NMDefFile.xml";

	public static final int NEEDLINGTYPE_DN = 1; // 正刺
	public static final int NEEDLINGTYPE_UP = 2; // 倒刺
	public static final int NEEDLINGTYPE_DU = 3; // 同位对刺
	public static final int NEEDLINGTYPE_XP = 4; // 异位对刺

	public static final int PROTO_MODBUS_RTU = 1;
	public static final int PROTO_MODBUS_TCP = 2;

	public static final int PORT_UART1 = 1;
	public static final int PORT_UART2 = 2;
	public static final int PORT_UART3 = 3;
	public static final int PORT_UART4 = 4;
	public static final int PORT_UART5 = 5;
	public static final int PORT_CAN1 = 8;
	public static final int PORT_CAN2 = 9;
	public static final int PORT_TCP = 10;
	public static final int PORT_UDP = 11;

	public static List<Inverter> Inverters = new ArrayList<Inverter>();
	public static NeedleMachine Machine = new NeedleMachine();

	// 针刺机
	public static class NeedleMachine
	{
		public boolean IsValid = false;

		public int NeedlingType; // 针刺型式， 正刺，倒刺，对刺
		public boolean HasFeedBelt; // 是否带进给()
		public int NeedlingFreqency1; // 主刺频率
		public int NeedlingFreqency2; // 副刺频率
		public int NeedlingRange1; // 正刺动程
		public int NeedlingRange2; // 倒刺动程
		public int SupporterRange; // 托网动程
		public int SeparatorRange; // 剥网动程
		public int LinearSpeed; // 线速度
		public int StretchRatio; // 牵伸率
		public int StepAdvance; // 步进量
		public int NeedleDensity1; // 植针密度
		public int NeedleDensity2; // 异位植针密度

		public MotorDriver MainDriver; // 主轴驱动
		public MotorDriver AuxiDriver; // 辅轴驱动
		public MotorDriver FeedDriver; // 过渡帘驱动
		public MotorDriver InputDriver; // 输入辊驱动
		public MotorDriver OutputDriver; // 输出辊驱动
	}

	// 驱动器/变频器动态配置参数
	public static class MotorDriver
	{
		public int port;
		public int protocol;
		public int devaddr;

		public Inverter inverter; // 变频器

		public Map<String, String> CommProp;
		public Map<String, String> CtrlParm;
	}

	// 内置变频器参数
	public static class Inverter
	{
		public String Manufacture;
		public String Model;

		public Map<String, String> CommProp;
		public Map<String, String> CtrlParm;
	}

}
