package com.sanfai.np.objects;

import java.util.ArrayList;
import java.util.List;

public class dxfObjects
{
	public dxfHeader hdr = new dxfHeader();
	public dxfEntity ent = new dxfEntity();

	public dxfObjects()
	{
	}

	public static class Point
	{
		public float x;
		public float y;
	}

	public static class Circle
	{
		public float x;
		public float y;
		public float r;
	}

	public static class dxfHeader
	{
		public String m_AcadVer;
		public int m_nAngleDir;
		public int m_nUnit;
		public double m_dUnit;
		public Point m_Orgin = new Point();

		public dxfHeader()
		{
			Reset();
		}

		public void Reset()
		{
			m_AcadVer = "";
			m_Orgin.x = 0.0f;
			m_Orgin.y = 0.0f;
			m_nAngleDir = 0;
			m_nUnit = 4;
			m_dUnit = 1.0f;
		}

		public void SetOriginX(float x)
		{
			m_Orgin.x = x;
		};

		public void SetOriginY(float y)
		{
			m_Orgin.y = y;
		};

		public void SetOrigin(Point pt)
		{
			m_Orgin.x = pt.x;
			m_Orgin.y = pt.y;
		};

		public void SetVersion(String s)
		{
			m_AcadVer = s;
		};

		public void SetAngleDir(int d)
		{
			m_nAngleDir = d;
		}

		public void SetUnit(int u)
		{
			/*
			 * 0 = �޵�λ�� 1 = Ӣ�磻 2 = Ӣ�ߣ� 3 = Ӣ� 4 = ���ף� 5 = ���ף� 6 = �ף� 7 = ǧ�ף� 8
			 * = ΢Ӣ�磻 9 = �ܶ��� 10 = �룻 11 = ���� 12 = ���ף� 13 = ΢�ף� 14 = ���ף� 15 =
			 * ʮ�ף� 16 = ���ף� 17 = ����� 18 = ���ĵ�λ��19 = ���ꣻ 20 = ����
			 */
			// ��λ�����, �����mmΪ��λ
			double dUnit[] = { 1.0, // 0 = �޵�λ��
					25.4, // 1 = Ӣ�磻
					304.8, // 2 = Ӣ�ߣ�
					1609344.0, // 3 = Ӣ�
					10.0, // 4 = ���ף�
					10.0, // 5 = ���ף�
					1000.0, // 6 = �ף�
					1000000.0, // 7 = ǧ�ף�
					0.0000254, // 8 = ΢Ӣ�磻1Ӣ��=10��6�η�΢��
					0.0254, // 9 = �ܶ���
					914.4, // 10 = �룻
					0.0000001, // 11 = ����
					0.000001, // 12 = ���ף�
					0.001, // 13 = ΢�ף�
					100.0, // 14 = ���ף�
					10000.0, // 15 = ʮ�ף�
					100000.0, // 16 = ���ף�
					1000000000000.0, // 17 = �����
					149600000000000.0, // 18 = ���ĵ�λ��1.496��ǧ�ף�
					9.4653e15, // 19 = ����=9.4653��10^12km
					3.2616 * 9.4653e15, // 20 = ����=3.2616����
			};
			if (u < 0 || u > 20)
			{
				// ��Ч��λ
				u = 0;
			}
			m_nUnit = u;
			m_dUnit = dUnit[u];
		}

		String GetVersion()
		{
			return m_AcadVer;
		};

		double GetRation()
		{
			return m_dUnit;
		};

		int GetAngleDir()
		{
			return m_nAngleDir;
		}

		int GetUnit()
		{
			return m_nUnit;
		}

		Point GetOrigin(Point pt)
		{
			pt.x = m_Orgin.x;
			pt.y = m_Orgin.y;
			return pt;
		}
	}

	public static class dxfEntity
	{
		private List<Circle> Points = new ArrayList<Circle>();

		public void AddPoint(Circle pt)
		{
			Points.add(pt);
		}

		public List<Circle> GetPoints()
		{
			return Points;
		}
	}

}
