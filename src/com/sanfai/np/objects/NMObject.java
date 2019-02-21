package com.sanfai.np.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NMObject
{
	public static final String NMDefFile = "NMDefFile.xml";

	public static final int NEEDLINGTYPE_DN = 1; // ����
	public static final int NEEDLINGTYPE_UP = 2; // ����
	public static final int NEEDLINGTYPE_DU = 3; // ͬλ�Դ�
	public static final int NEEDLINGTYPE_XP = 4; // ��λ�Դ�

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

	// ��̻�
	public static class NeedleMachine
	{
		public boolean IsValid = false;

		public int NeedlingType; // �����ʽ�� ���̣����̣��Դ�
		public boolean HasFeedBelt; // �Ƿ������()
		public int NeedlingFreqency1; // ����Ƶ��
		public int NeedlingFreqency2; // ����Ƶ��
		public int NeedlingRange1; // ���̶���
		public int NeedlingRange2; // ���̶���
		public int SupporterRange; // ��������
		public int SeparatorRange; // ��������
		public int LinearSpeed; // ���ٶ�
		public int StretchRatio; // ǣ����
		public int StepAdvance; // ������
		public int NeedleDensity1; // ֲ���ܶ�
		public int NeedleDensity2; // ��λֲ���ܶ�

		public MotorDriver MainDriver; // ��������
		public MotorDriver AuxiDriver; // ��������
		public MotorDriver FeedDriver; // ����������
		public MotorDriver InputDriver; // ���������
		public MotorDriver OutputDriver; // ���������
	}

	// ������/��Ƶ����̬���ò���
	public static class MotorDriver
	{
		public int port;
		public int protocol;
		public int devaddr;

		public Inverter inverter; // ��Ƶ��

		public Map<String, String> CommProp;
		public Map<String, String> CtrlParm;
	}

	// ���ñ�Ƶ������
	public static class Inverter
	{
		public String Manufacture;
		public String Model;

		public Map<String, String> CommProp;
		public Map<String, String> CtrlParm;
	}

}
