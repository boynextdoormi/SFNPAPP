package com.sanfai.np.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sanfai.np.objects.dxfObjects.Circle;
import com.sanfai.np.objects.dxfObjects.dxfEntity;
import com.sanfai.np.objects.dxfObjects.dxfHeader;

import android.content.Context;
import android.util.Log;

public class dxfparser
{

	public static class dxfGroup
	{
		public String sGroupId;
		public String sGroupVal;
		public int iGroupId;
		// public float fGroupVal;
	}

	public static long lineNo = 0;

	public static boolean dxfGetGroup(BufferedReader reader, dxfGroup grp)
	{
		String line;
		try
		{
			lineNo++;
			line = reader.readLine();

			if (line == null)
			{
				line = "";
			}
			grp.sGroupId = line.trim();
			if (line.length() == 0)
			{
				grp.sGroupVal = "";
				return false;
			}
			lineNo++;

			line = reader.readLine();
			if (line == null)
				line = "";
			grp.sGroupVal = line.trim();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		try
		{
			grp.iGroupId = Integer.valueOf(grp.sGroupId);
		}
		catch (Exception e)
		{
			Log.i("grp.iGroupId", grp.sGroupId);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static class dxfParserBase
	{
		public final static int GROUP_UNKNOWN = -1;
		protected int m_Token;
		protected dxfGroup m_Group;

		protected int getGroupToken(String grpName)
		{
			return GROUP_UNKNOWN;
		}

		protected boolean ParseGroupToken()
		{
			return true;
		}

		public boolean SetToken(dxfGroup grp)
		{
			m_Group = grp;
			return ParseGroupToken();
		}
	}

	public static class dxfParseHeader extends dxfParserBase
	{
		private final static int HDRG_ACADVER = 1;
		private final static int HDRG_ANGDIR = 2;
		private final static int HDRG_UNIT = 3;
		private final static int HDRG_EXTMIN = 4;

		public enum HdrTokenEnum
		{
			NONE, ACADVER, ANGDIR, UNIT, EXTMIN;
		}

		private final dxfHeader hdr;

		public dxfParseHeader(dxfHeader h)
		{
			hdr = h;
		}

		@Override
		protected int getGroupToken(String grpName)
		{
			if (0 == grpName.compareToIgnoreCase("$ACADVER"))
				return HDRG_ACADVER;
			if (0 == grpName.compareToIgnoreCase("$ANGDIR"))
				return HDRG_ANGDIR;
			if (0 == grpName.compareToIgnoreCase("$INSUNITS"))
				return HDRG_UNIT;
			if (0 == grpName.compareToIgnoreCase("$EXTMIN"))
				return HDRG_EXTMIN;
			return GROUP_UNKNOWN;
		}

		@Override
		protected boolean ParseGroupToken()
		{
			if (m_Group.iGroupId == 9)
			{
				m_Token = getGroupToken(m_Group.sGroupVal);
				return true;
			}
			switch (m_Token)
			{
			case HDRG_ACADVER:
				return ParseAcadVer();
			case HDRG_ANGDIR:
				return ParseAngDir();
			case HDRG_UNIT:
				return ParseUnit();
			case HDRG_EXTMIN:
				return ParseExtMin();
			}
			return true;
		}

		private boolean ParseAcadVer()
		{
			if (m_Group.iGroupId == 1)
			{
				hdr.SetVersion(m_Group.sGroupVal);
			}
			return true;
		}

		private boolean ParseAngDir()
		{
			if (m_Group.iGroupId == 70)
			{
				hdr.SetAngleDir(Integer.valueOf(m_Group.sGroupVal));
			}
			return true;
		}

		private boolean ParseUnit()
		{
			if (m_Group.iGroupId == 70)
			{
				hdr.SetUnit(Integer.valueOf(m_Group.sGroupVal));
			}

			return true;
		}

		private boolean ParseExtMin()
		{
			switch ((m_Group.iGroupId))
			{
			case 10:
				hdr.SetOriginX(Float.valueOf(m_Group.sGroupVal));
				break;
			case 20:
				hdr.SetOriginY(Float.valueOf(m_Group.sGroupVal));
				break;
			}
			return true;
		}
	}

	public static class dxfParseEntities extends dxfParserBase
	{
		private final static int ENTG_CIRCLE = 1;

		private final dxfEntity m_ent;
		private Circle m_Point = null;

		public dxfParseEntities(dxfEntity ent)
		{
			m_ent = ent;
		}

		@Override
		protected int getGroupToken(String grpName)
		{
			if (0 == grpName.compareToIgnoreCase("ACDBCIRCLE"))
				return ENTG_CIRCLE;
			return GROUP_UNKNOWN;
		}

		@Override
		protected boolean ParseGroupToken()
		{
			if (m_Group.iGroupId == 100)
			{
				m_Token = getGroupToken(m_Group.sGroupVal);
				if (m_Token == ENTG_CIRCLE)
				{
					m_Point = new Circle();
					m_ent.AddPoint(m_Point);
				}
				else
				{
					m_Point = null;
				}
				return true;
			}

			switch (m_Token)
			{
			case ENTG_CIRCLE:
				return ParseCircle();
			}
			return true;
		}

		private boolean ParseCircle()
		{
			switch ((m_Group.iGroupId))
			{
			case 10:
				m_Point.x = Float.valueOf(m_Group.sGroupVal);
				break;
			case 20:
				m_Point.y = Float.valueOf(m_Group.sGroupVal);
				break;
			case 40:
				m_Point.r = Float.valueOf(m_Group.sGroupVal);
				break;
			// case 30://z-axis
			// case 0:
			// case 5:
			// case 330:
			}
			return true;
		}
	}

	public static class dxfTopParser extends dxfParserBase
	{
		public interface AcadSec
		{
			String NONE = "NONE";
			String HEADER = "HEADER";
			String CLASSES = "CLASSES";
			String OBJECTS = "OBJECTS";
			String TABLES = "TABLES";
			String BLOCKS = "BLOCKS";
			String ENTITIES = "ENTITIES";
		}

		public boolean m_bEndOfFile = false;
		private boolean m_bInSection = false;

		private final static int ACADSEC_NONE = 0;
		private final static int ACADSEC_HEADER = 1;
		private final static int ACADSEC_CLASSES = 2;
		private final static int ACADSEC_OBJECTS = 3;
		private final static int ACADSEC_TABLES = 4;
		private final static int ACADSEC_BLOCKS = 5;
		private final static int ACADSEC_ENTITIES = 6;
		private final static int ACADSEC_UNKNOWN = 9;

		public dxfObjects dxfObj = new dxfObjects();

		private dxfParserBase UnkParser = null;
		private dxfParseHeader HdrParser = null;
		private dxfParseEntities EntParser = null;

		public dxfTopParser()
		{
			HdrParser = new dxfParseHeader(dxfObj.hdr);
			UnkParser = new dxfParserBase();
			EntParser = new dxfParseEntities(dxfObj.ent);
		}

		@Override
		protected int getGroupToken(String grpName)
		{
			if (0 == grpName.compareToIgnoreCase(AcadSec.HEADER))
				return ACADSEC_HEADER;
			if (0 == grpName.compareToIgnoreCase(AcadSec.CLASSES))
				return ACADSEC_CLASSES;
			if (0 == grpName.compareToIgnoreCase(AcadSec.OBJECTS))
				return ACADSEC_OBJECTS;
			if (0 == grpName.compareToIgnoreCase(AcadSec.TABLES))
				return ACADSEC_TABLES;
			if (0 == grpName.compareToIgnoreCase(AcadSec.BLOCKS))
				return ACADSEC_BLOCKS;
			if (0 == grpName.compareToIgnoreCase(AcadSec.ENTITIES))
				return ACADSEC_ENTITIES;
			return ACADSEC_UNKNOWN;
		}

		@Override
		protected boolean ParseGroupToken()
		{
			// Out of Section
			if (!m_bInSection)
			{
				if (m_Group.iGroupId == 0 && 0 == m_Group.sGroupVal.compareToIgnoreCase("EOF"))
				{
					m_bEndOfFile = true;
					return true;
				}
				if (m_Group.iGroupId == 0 && 0 == m_Group.sGroupVal.compareToIgnoreCase("SECTION"))
				{
					m_Token = ACADSEC_NONE;
					m_bInSection = true;
					return true;
				}
				return true;
			}

			// now in section
			if ((m_Group.iGroupId == 0) && (0 == m_Group.sGroupVal.compareToIgnoreCase("ENDSEC")))
			{
				// 结束Section
				m_bInSection = false;
				m_Token = ACADSEC_NONE;
				return true;
			}

			if (m_Token == ACADSEC_NONE)
			{
				// 现在首先需要进入Sec
				// Section type not specified
				// Only groupID 2 is allowed
				if (!(m_Group.iGroupId == 2))
				{
					return false;
				}
				m_Token = getGroupToken(m_Group.sGroupVal);
				return true;
			}

			boolean bret = false;
			switch (m_Token)
			{
			case ACADSEC_HEADER:
				bret = HdrParser.SetToken(m_Group);
				break;

			case ACADSEC_ENTITIES:
				bret = EntParser.SetToken(m_Group);
				break;

			case ACADSEC_CLASSES:
			case ACADSEC_OBJECTS:
			case ACADSEC_TABLES:
			case ACADSEC_BLOCKS:
			case ACADSEC_UNKNOWN:
				bret = UnkParser.SetToken(m_Group);
				break;
			}
			return bret;
		}
	}

	public interface OnDxfParsed
	{
		public void OnParsed(boolean bResult, dxfObjects dxfObj);
	}

	private static class parserThread extends Thread
	{
		private final Context mContext;
		private final OnDxfParsed mOnParsed;
		private final boolean bInternal;
		private String szFile;
		int xm = 0;

		public parserThread(String file, OnDxfParsed cb)
		{
			szFile = file;
			mContext = null;
			mOnParsed = cb;
			bInternal = false;
		}

		public parserThread(Context context, String file, OnDxfParsed cb)
		{
			mContext = context;
			szFile = file;
			mOnParsed = cb;
			bInternal = true;
		}

		@Override
		public void run()
		{
			BufferedReader bufReader = null;
			try
			{
				InputStream istream = null;
				if (bInternal)
				{
					istream = mContext.getResources().getAssets().open(szFile);
				}
				else
				{
					File file = new File(szFile);
					if (file.exists())
					{
						istream = new FileInputStream(file);
					}
				}
				if (istream != null)
				{
					try
					{
						bufReader = new BufferedReader(new InputStreamReader(istream));
					}
					catch (Exception e)
					{
					}
				}
			}

			catch (Exception e)
			{
			}

			if (bufReader == null)
			{
				if (mOnParsed != null)
				{
					mOnParsed.OnParsed(false, null);
				}
				return;
			}

			boolean bSuc = true;
			dxfTopParser parser = new dxfTopParser();
			dxfGroup grp = new dxfGroup();
			lineNo = 0;

			while (!isInterrupted())
			{
				xm++;
				try
				{
					if (!dxfGetGroup(bufReader, grp))
					{
						Log.i("In", "Xp=" + xm + " lineNo=" + lineNo);
						break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				// Log.i("In", "Xp=" + xm);
				try
				{
					parser.SetToken(grp);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (mOnParsed != null)
			{
				mOnParsed.OnParsed(bSuc, parser.dxfObj);
			}
		}
	}

	public static boolean Parser(String dxfFile, OnDxfParsed cb)
	{
		parserThread ps = new parserThread(dxfFile, cb);
		ps.start();
		return true;
	}

	public static boolean Parser(Context c, String dxfFile, OnDxfParsed cb)
	{

		parserThread ps = new parserThread(c, dxfFile, cb);
		ps.start();
		return true;
	}
}
