package com.sanfai.np.objects;

import java.util.ArrayList;
import java.util.List;

import com.sanfai.np.BuildConfig;
import com.sanfai.np.objects.SFObjects.NeedlePad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

public class SFSurfaceView extends SurfaceView implements Callback
{
	// private final static String TAG = "SFView";
	private static final String TAG = BuildConfig.TAG;

	private final Context mContext;
	private final float xdpi;
	private final float ydpi;
	private boolean bSurfaceInited = false;
	// private Paint mPaint;
	private Paint mPaintB;

	private Matrix mCurveMat;
	private Region mClipRegion;

	private LoopThread mUpdateThread = null;
	private int[] colors = new int[MAX_PAD_LAYERS];

	private final static int MAX_PAD_LAYERS = 16;

	public SFSurfaceView(Context context)
	{
		super(context);
		mContext = context;
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;
		// Log.i(TAG, "create 1");
		SFSurfaceViewInit();
	}

	public SFSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;

		// Log.i(TAG, "create 2");
		SFSurfaceViewInit();
	}

	public void Destroy()
	{
		// Log.i(TAG, "Destroy");
		cancelDrawer();
	}

	private void cancelDrawer()
	{
		if (mUpdateThread != null)
		{
			synchronized (mUpdateThread)
			{
				mUpdateThread.Cancel();
				mUpdateThread = null;
			}
		}
	}

	private void SFSurfaceViewInit()
	{
		SurfaceHolder holder = getHolder();
		holder.addCallback(this); // 设置Surface生命周期回调

		// mPaint = new Paint();
		// mPaint.setColor(Color.YELLOW);
		// mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		// mPaint.setTextSize(3.0f);

		mPaintB = new Paint();
		mPaintB.setColor(Color.RED);
		mPaintB.setStyle(Paint.Style.STROKE);
		mPaintB.setTextSize(3);

		colors[0] = Color.YELLOW;
		colors[1] = Color.BLUE;
		colors[2] = Color.CYAN;
		colors[3] = Color.MAGENTA;
		colors[4] = Color.WHITE;
		colors[5] = Color.GREEN;
		colors[6] = 0x808080;
		colors[7] = 0x8080c0;
		colors[8] = 0x8080ff;
		colors[9] = 0x80c080;
		colors[10] = 0x80c0c0;
		colors[11] = 0x80c0ff;
		colors[12] = 0x80ff80;
		colors[13] = 0x80ffc0;
		colors[14] = 0xc0c0c0;

		mCurveMat = new Matrix();
		mCurveMat.reset();

		this.setClickable(true);
		this.setOnTouchListener(mOnTouch);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// Log.i(TAG, "Surface Created");
		mUpdateThread = new LoopThread(mContext, holder);
		mUpdateThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// Log.i(TAG, "Surface Changed");
		// 设置坐标变换，如果锁上holder, 则不会绘画
		synchronized (holder)
		{
			// 设置剪切区域
			mClipRegion = new Region(0, 0, width, height);
			// 重置为单位阵，即无变换
			mCurveMat.reset();
			// 后变换到以mm为单位，y轴上下颠倒
			mCurveMat.postScale(xdpi / 25.4f, -ydpi / 25.4f); //
			// 先平移到屏中心为原点
			mCurveMat.postTranslate(width / 2, height / 2); //

			bSurfaceInited = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// Log.i(TAG, "Surface Destroyed");
		cancelDrawer();
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		// Log.i(TAG, "Surface attached");
	}

	@Override
	public void onDetachedFromWindow()
	{
		cancelDrawer();
		super.onDetachedFromWindow();
		// Log.i(TAG, "Surface detached");
	}

	@Override
	public boolean performClick()
	{
		super.performClick();
		return true;
	}

	public void CancelDraw()
	{
		if (mUpdateThread != null)
		{
			synchronized (mUpdateThread)
			{
				mUpdateThread.Cancel();
			}
			mUpdateThread = null;
		}
	}

	// 绘制线程
	class LoopThread extends Thread
	{
		private final Context mContext;
		public SurfaceHolder surfaceHolder;

		public LoopThread(Context context, SurfaceHolder surfaceHolder)
		{
			this.surfaceHolder = surfaceHolder;
			this.mContext = context;
		}

		private boolean Suspended = false;

		public void Suspend()
		{
			try
			{
				if (!Suspended)
				{
					Suspended = true;
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
					if (Suspended)
					{
						this.notify();
						Suspended = false;
					}
				}
			}
			catch (Exception e)
			{
			}
		}

		private Handler handler;

		public Handler getHandler()
		{
			return handler;
		}

		private void drawInThread()
		{
			if (surfaceHolder != null)
			{
				synchronized (surfaceHolder)
				{
					Canvas c = surfaceHolder.lockCanvas(null);
					if (c != null)
					{
						try
						{
							// 裁剪，防区域外画数据
							c.clipRegion(mClipRegion);

							c.save();
							c.setMatrix(mCurveMat);

							doDrawing(c);

							c.restore();
						}
						catch (Exception e)
						{
							// Log.e(TAG, "Error in LoopThread run()");
							e.printStackTrace();
						}
					}
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}

		@Override
		public void run()
		{
			while (!interrupted())
			{
				synchronized (this)
				{
					if ((bSurfaceInited) && (surfaceHolder != null))
					{
						try
						{
							drawInThread();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				try
				{
					sleep(50);
				}
				catch (InterruptedException e)
				{
				}

			}
		}

		public void Cancel()
		{
			this.interrupt();
			this.surfaceHolder = null;
		}
	}

	private final OnTouchListener mOnTouch = new OnTouchListener()
	{
		private int ox, oy, nx, ny, ax, ay, dx, dy;
		private float otx, oty, ozm;
		private double od, dt;

		boolean bzoom = false;

		@Override
		public boolean onTouch(View v, MotionEvent e)
		{
			v.performClick();

			switch (e.getAction() & MotionEvent.ACTION_MASK)
			{
			case MotionEvent.ACTION_DOWN:
				// 单指按下
				ox = (int) e.getX(0);
				oy = (int) e.getY(0);
				otx = xTrans;
				oty = yTrans;
				ozm = mZoom;
				od = 1.0f;
				break;

			case MotionEvent.ACTION_UP:
				// 单指弹起
				bzoom = false;
				return true;

			case MotionEvent.ACTION_MOVE:
				// 滑动
				if (bzoom)
				{
					// 当双指按下滑动
					try
					{
						nx = (int) e.getX(0);
						ny = (int) e.getY(0);
						ax = (int) e.getX(1);
						ay = (int) e.getY(1);
						dx = (ax - nx);
						dy = (ay - ny);
						dt = (double) (dx * dx + dy * dy);
						double dd = Math.sqrt(dt);
						if (od > 10)
						{
							setZoom(ozm * (float) (dd / od));
						}
					}
					catch (Exception xe)
					{
						xe.printStackTrace();
					}
				}
				else
				{ // 单指在滑动
					nx = (int) e.getX(0);
					ny = (int) e.getY(0);
					ax = (ox - nx);
					ay = (oy - ny);
					float fx = ax * 25.4f / xdpi;
					float fy = ay * 25.4f / xdpi;
					setTrans(otx + fx, oty + fy);
				}
				return true;

			case MotionEvent.ACTION_POINTER_DOWN:
				// 双手指按下
				try
				{
					nx = (int) e.getX(0);
					ny = (int) e.getY(0);
					ax = (int) e.getX(1);
					ay = (int) e.getY(1);
					dx = (ax - nx);
					dy = (ay - ny);
					dt = (double) (dx * dx + dy * dy);
					od = Math.sqrt(dt);
					ozm = getZoom();

					bzoom = true;
				}
				catch (Exception xe)
				{
					xe.printStackTrace();
				}
				return true;

			case MotionEvent.ACTION_POINTER_UP:
				// 双指之一弹起
				bzoom = false;
				return true;
			}
			return true;
		}

	};

	private float mZoom = 1.0f;
	private float xTrans = 0.0f;
	private float yTrans = 0.0f;
	private boolean mOverlap = false;

	public void setZoom(float nh)
	{
		synchronized (this)
		{
			mZoom = nh;
		}
	}

	public void setZoomIn(float zi)
	{
		if (zi < 1.0f)
			return;
		synchronized (this)
		{
			mZoom *= zi;
		}
	}

	public void setZoomOut(float zo)
	{
		if (zo < 1.0f)
			return;
		synchronized (this)
		{
			mZoom /= zo;
		}
	}

	public void setTrans(float tx, float ty)
	{
		synchronized (this)
		{
			xTrans = tx;
			yTrans = ty;
		}
	}

	public void resetTransform()
	{
		synchronized (this)
		{
			mZoom = 1.0f;
			xTrans = 0.0f;
			yTrans = 0.0f;
		}
	}

	public void setOverlap(boolean b)
	{
		synchronized (this)
		{
			mOverlap = b;
		}
	}

	public float getZoom()
	{
		return mZoom;
	}

	public boolean getOverlap()
	{
		return mOverlap;
	}

	//
	//
	//
	// private NeedlePad mSrcData = new NeedlePad();

	private List<NeedlePad> mSrcDataList = new ArrayList<NeedlePad>();

	public List<NeedlePad> getDataSeries()
	{
		return mSrcDataList;
	}

	public int getDataSeriesSize()
	{
		return mSrcDataList.size();
	}

	private void getBound()
	{
		RectF border = null;

		for (NeedlePad mSrcData : mSrcDataList)
		{
			if (mSrcData.isVisible())
			{
				if (mOverlap)
				{
					border = mSrcData.overlapBorder;
				}
				else
				{
					border = mSrcData.ndlePadBorder;
				}
			}
		}

	}

	public void addDataSource(NeedlePad pad)
	{
		Paint mPaint = new Paint();
		mPaint.setColor(colors[mSrcDataList.size()]);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setTextSize(3.0f);

		NeedlePad psd = new NeedlePad();
		psd.CopyFrom(pad);
		psd.mPaint = mPaint;

		mSrcDataList.add(psd);

	}

	private void drawLabels(RectF border, Canvas c)
	{
		// 画外框
		mPaintB.setStyle(Paint.Style.STROKE); // 空心矩形
		c.drawRect(border, mPaintB);

		mPaintB.setStyle(Paint.Style.FILL_AND_STROKE); // 实心字

		// 画横标尺
		c.save();
		c.translate(border.left - 1.4f, border.bottom - 3.0f);
		c.scale(1.0f, -1.0f);
		float ow = border.width();
		int ww = 0;
		while (ww <= (ow + 0.1f))
		{
			String tw = "" + ww;
			float dx = ww;
			float dy = 0;
			c.drawText(tw, dx, dy, mPaintB);
			ww += 10;
		}
		c.restore();

		// 画纵标尺
		c.save();
		c.translate(border.left, border.bottom);
		c.rotate(90.0f);
		c.scale(1.0f, -1.0f);
		c.translate(-1.4f, -0.6f);
		float oh = Math.abs(border.height());
		int wh = 0;
		while (wh <= (oh + 0.1f))
		{
			String th = "" + wh;
			float dx = wh;
			float dy = 0;
			c.drawText(th, dx, dy, mPaintB);
			wh += 10;
		}
		c.restore();
	}

	private void doDrawing(Canvas c)
	{
		// TODO Auto-generated method stub
		// 这个很重要，清屏操作，清楚掉上次绘制的残留图像
		c.drawColor(Color.BLACK);

		// 平移和缩放
		c.translate(-xTrans, yTrans);
		c.scale(mZoom, mZoom);

		RectF boundary = new RectF();
		boundary.left = 0;
		boundary.right = 0;
		boundary.top = 0;
		boundary.bottom = 0;

		RectF border = null;

		for (NeedlePad mSrcData : mSrcDataList)
		{
			synchronized (mSrcData)
			{
				if (mSrcData.isVisible())
				{
					if (mOverlap)
					{
						border = mSrcData.overlapBorder;
					}
					else
					{
						border = mSrcData.ndlePadBorder;
					}

					if (boundary.left > border.left)
						boundary.left = border.left;
					if (boundary.right < border.right)
						boundary.right = border.right;
					if (boundary.top < border.top)
						boundary.top = border.top;
					if (boundary.bottom > border.bottom)
						boundary.bottom = border.bottom;

					// 确定要绘制的对象
					Path path;

					if (mOverlap)
					{
						border = mSrcData.overlapBorder;
						path = mSrcData.getOverlapPath();
					}
					else
					{
						border = mSrcData.ndlePadBorder;
						path = mSrcData.getOriginPath();
					}
					if (path.isEmpty())
					{
						return;
					}

					// 绘制对象
					c.save();
					c.clipRect(border);
					c.drawPath(path, mSrcData.mPaint);
					c.restore();
				}
			}
		}

		if (border != null)
		{
			// 绘制标注
			drawLabels(boundary, c);
		}
	}

}
