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
			 * 0 = 无单位； 1 = 英寸； 2 = 英尺； 3 = 英里； 4 = 毫米； 5 = 厘米； 6 = 米； 7 = 千米； 8
			 * = 微英寸； 9 = 密耳； 10 = 码； 11 = 埃； 12 = 纳米； 13 = 微米； 14 = 分米； 15 =
			 * 十米； 16 = 百米； 17 = 百万公里； 18 = 天文单位；19 = 光年； 20 = 秒差距
			 */
			// 单位换算表, 换算成mm为单位
			double dUnit[] = { 1.0, // 0 = 无单位；
					25.4, // 1 = 英寸；
					304.8, // 2 = 英尺；
					1609344.0, // 3 = 英里；
					10.0, // 4 = 毫米；
					10.0, // 5 = 厘米；
					1000.0, // 6 = 米；
					1000000.0, // 7 = 千米；
					0.0000254, // 8 = 微英寸；1英寸=10的6次方微寸
					0.0254, // 9 = 密耳；
					914.4, // 10 = 码；
					0.0000001, // 11 = 埃；
					0.000001, // 12 = 纳米；
					0.001, // 13 = 微米；
					100.0, // 14 = 分米；
					10000.0, // 15 = 十米；
					100000.0, // 16 = 百米；
					1000000000000.0, // 17 = 百万公里；
					149600000000000.0, // 18 = 天文单位≈1.496亿千米；
					9.4653e15, // 19 = 光年=9.4653×10^12km
					3.2616 * 9.4653e15, // 20 = 秒差距=3.2616光年
			};
			if (u < 0 || u > 20)
			{
				// 无效单位
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
