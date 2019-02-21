package com.sanfai.np.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.sanfai.np.objects.dxfObjects.Circle;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

public class SFObjects
{
	public static class Point
	{
		public float x;
		public float y;

		public Point()
		{
			x = 0.0f;
			y = 0.0f;
		}

		public Point(float dx, float dy)
		{
			x = dx;
			y = dy;
		}
	}

	public static class PadParm
	{
		public float height = 300.0f; //
		public float width = 80.0f; // 取样宽
		public float radius = 0.115f; // 针粗（孔径）
		public float step = 10.0f; // 步长
	}

	//
	// 针板
	//
	public static class NeedlePad
	{
		public Color color;
		public Paint mPaint;
		public PadParm padParm = new PadParm();

		public RectF ndlePadBorder = new RectF();
		public RectF overlapBorder = new RectF();
		private boolean bVisible = true;

		private float maxx, maxy, minx, miny;
		private float paxx, pinx;

		private List<Point> points = new ArrayList<Point>();
		private List<Point> Overlap = new ArrayList<Point>();

		private Path orgPath = new Path();
		private Path ovrPath = new Path();

		public List<Point> getPointList()
		{
			return points;
		}

		public List<Point> getOverlapPointList()
		{
			return Overlap;
		}

		public Path getOriginPath()
		{
			return orgPath;
		}

		public Path getOverlapPath()
		{
			return ovrPath;
		}

		public void setVisibility(boolean b)
		{
			bVisible = b;
		}

		public boolean isVisible()
		{
			return bVisible;
		}

		public void setParm(PadParm p)
		{
			padParm.height = p.height;
			padParm.width = p.width;
			padParm.radius = p.radius;
			padParm.step = p.step;

			ReFormPad();
		}

		public void ReFormPad()
		{
			synchronized (this)
			{
				Overlap.clear();
				orgPath.reset();
				ovrPath.reset();

				if (points.size() == 0)
				{
					return;
				}
				transformPad();
				genOverlapData();
				genDrawPath();
			}
		}

		public void CopyFrom(NeedlePad obj)
		{
			this.padParm.width = obj.padParm.width;
			this.padParm.height = obj.padParm.height;
			this.padParm.radius = obj.padParm.radius;
			this.padParm.step = obj.padParm.step;

			points.clear();
			points.addAll(obj.getPointList());
			ReFormPad();
		}

		private void transformPad()
		{
			synchronized (points)
			{
				// 计算针板里最大最小坐标
				float nx = points.get(0).x;
				float ny = points.get(0).y;
				float ax = nx;
				float ay = ny;

				for (Point pt : points)
				{
					if (nx > pt.x)
						nx = pt.x;
					if (ny > pt.y)
						ny = pt.y;
					if (ax < pt.x)
						ax = pt.x;
					if (ay < pt.y)
						ay = pt.y;
				}

				// 把针坐标平移到中心
				float mx = (ax + nx) / 2.0f; // x方向离中心点偏差
				float my = (ay + ny) / 2.0f; // y方向离中心点偏差
				for (Point pt : points)
				{
					pt.x -= mx;
					pt.y -= my;
				}

				maxx = ax - mx;
				maxy = ay - my;
				minx = nx - mx;
				miny = ny - my;

				ndlePadBorder.left = minx;
				ndlePadBorder.right = maxx;
				ndlePadBorder.bottom = miny;
				ndlePadBorder.top = maxy;
			}
		}

		private void genOverlapData()
		{
			synchronized (Overlap)
			{
				Overlap.clear();
				if (points.size() == 0)
				{
					return;
				}
				int steps = (int) ((maxx - minx) / padParm.step + 0.5f);
				float gxx = minx + padParm.width; // 只需给定的宽度

				for (Point pt : points)
				{
					if (pt.x > minx && pt.x < gxx)
					{
						Overlap.add(new Point(pt.x, pt.y));
					}
				}

				for (int i = 1; i <= steps; i++)
				{
					for (Point pt : points)
					{
						float newxl = pt.x + padParm.step * ((float) i);
						float newxr = pt.x - padParm.step * ((float) i);
						if (newxl > minx && newxl < gxx)
						{
							Overlap.add(new Point(newxl, pt.y));
						}
						if (newxr > minx && newxr < gxx)
						{
							Overlap.add(new Point(newxr, pt.y));
						}
					}
				}

				// 计算针板里最大最小坐标
				float nx = minx;
				float ny = miny;
				float ax = minx;
				float ay = miny;

				for (Point pt : Overlap)
				{
					if (nx > pt.x)
						nx = pt.x;
					if (ny > pt.y)
						ny = pt.y;
					if (ax < pt.x)
						ax = pt.x;
					if (ay < pt.y)
						ay = pt.y;
				}

				// 把针坐标平移到中心
				float mx = (ax + nx) / 2.0f; // x方向离中心点偏差
				float my = (ay + ny) / 2.0f; // y方向离中心点偏差
				for (Point pt : Overlap)
				{
					pt.x -= mx;
					pt.y -= my;
				}
				//
				pinx = minx - mx;
				paxx = gxx - mx;

				overlapBorder.left = pinx;
				overlapBorder.right = paxx;
				overlapBorder.bottom = miny;
				overlapBorder.top = maxy;
			}
		}

		private void genDrawPath()
		{
			List<Point> pts;

			synchronized (this)
			{
				orgPath.reset();
				ovrPath.reset();

				pts = getPointList();

				for (Point pt : pts)
				{
					orgPath.addCircle(pt.x, pt.y, padParm.radius, Direction.CW);
				}

				pts = getOverlapPointList();

				for (Point pt : pts)
				{
					ovrPath.addCircle(pt.x, pt.y, padParm.radius, Direction.CW);
				}
			}
		}

		public void LoadFromDxfObject(dxfObjects obj)
		{
			if (obj.ent == null)
			{
				return;
			}
			List<Circle> cp = obj.ent.GetPoints();

			if (cp.size() <= 0)
			{
				return;
			}
			points.clear();

			for (Circle cr : cp)
			{
				points.add(new Point(cr.x, cr.y));
			}
			ReFormPad();
		}

		private void LoadFromTxtFile(InputStream ist)
		{
			if (ist == null)
			{
				return;
			}
			BufferedReader bufReader = null;
			try
			{
				bufReader = new BufferedReader(new InputStreamReader(ist));
			}
			catch (Exception e)
			{
			}
			if (bufReader == null)
			{
				return;
			}

			points.clear();
			boolean bSuc = true;
			while (bSuc)
			{
				String line = null;
				try
				{
					line = bufReader.readLine();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					line = null;
				}
				if (line == null)
				{
					break;
				}
				String[] ms = line.split(" |\t");
				// Log.i("ms spli", "MS=" + ms.length);
				if (ms.length >= 2)
				{
					Point pt = new Point();
					try
					{
						pt.x = Float.valueOf(ms[0]);
						pt.y = Float.valueOf(ms[1]);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					points.add(pt);
				}
			}
			ReFormPad();
		}

		public void LoadFromTxtFile(String szFile)
		{
			InputStream istream = null;
			try
			{
				File file = new File(szFile);
				if (file.exists())
				{
					istream = new FileInputStream(file);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (istream != null)
			{
				LoadFromTxtFile(istream);
			}
		}

		public void LoadFromTxtFile(Context context, String szFile)
		{
			InputStream istream = null;
			try
			{
				istream = context.getResources().getAssets().open(szFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (istream != null)
			{
				LoadFromTxtFile(istream);
			}
		}

	}

}
