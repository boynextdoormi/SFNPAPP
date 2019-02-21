package com.finley.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class SysStorage
{

	/**
	 * 
	 * vold_fstab ��Ч��ʽ
	 * 
	 * dev_mount <label> <mount_point> <part> <sysfs_path>
	 * 
	 */
	public final String HEAD = "dev_mount";
	public final String LABEL = "<label>";
	public final String MOUNT_POINT = "<mount_point>";
	public final String PARTITION = "<part>";
	public final String SYSFS_PATH = "<sysfs_path1...>";

	// private final int NHEAD = 0; // header
	private final int NLABEL = 1; // Label for the volume
	private final int NMOUNT_POINT = 2; // mount_point
	private final int NPARTITION = 3;
	private final int NSYSFS_PATH = 4;

	private final int DEV_INTERNAL = 0;
	private final int DEV_EXTERNAL = 1;
	private ArrayList<String> cache = new ArrayList<String>();
	private static SysStorage dev;
	private DevInfo info;
	private final File VOLD_FSTAB = new File(
			Environment.getRootDirectory().getAbsoluteFile() + File.separator + "etc" + File.separator + "vold.fstab");

	public static SysStorage getInstance()
	{
		if (null == dev)
			dev = new SysStorage();
		return dev;
	}

	private DevInfo getInfo(final int device)
	{
		// for(String str:cache)
		// System.out.println(str);
		if (null == info)
			info = new DevInfo();
		try
		{
			initVoldFstabToCache();
		}
		catch (IOException e)
		{
			Log.e("System.err", "Cannot access system file.");
			// e.printStackTrace();
		}
		if (device >= cache.size())
			return null;
		String[] sinfo = cache.get(device).split(" ");
		info.setLabel(sinfo[NLABEL]);
		info.setMountPoint(sinfo[NMOUNT_POINT]);
		info.setPartition(sinfo[NPARTITION]);
		info.setSysFsPath(sinfo[NSYSFS_PATH]);
		return info;
	}

	/**
	 * init the words into the cache array
	 * 
	 * @throws IOException
	 */
	private void initVoldFstabToCache() throws IOException
	{
		cache.clear();
		BufferedReader br = new BufferedReader(new FileReader(VOLD_FSTAB));
		String tmp = null;
		while ((tmp = br.readLine()) != null)
		{
			// the words startsWith "dev_mount" are the SD info
			if (tmp.startsWith(HEAD))
			{
				cache.add(tmp);
			}
		}
		br.close();
		cache.trimToSize();
	}

	public static class DevInfo
	{
		private String label, mount_point, part, sysfs_path;

		/**
		 * return the label name of the SD card
		 * 
		 * @return
		 */
		public String getLabel()
		{
			return label;
		}

		private void setLabel(String label)
		{
			this.label = label;
		}

		/**
		 * the mount point of the SD card
		 * 
		 * @return
		 */
		public String getMountPoint()
		{
			return mount_point;
		}

		private void setMountPoint(String mount_point)
		{
			this.mount_point = mount_point;
		}

		/**
		 * SD mount path
		 * 
		 * @return
		 */
		public String getPartition()
		{
			return part;
		}

		private void setPartition(String p)
		{
			this.part = p;
		}

		/**
		 * "unknow"
		 * 
		 * @return
		 */
		public String getSysFsPath()
		{
			return sysfs_path;
		}

		private void setSysFsPath(String dev)
		{
			this.sysfs_path = dev;
		}
	}

	public DevInfo getInternalInfo()
	{
		return getInfo(DEV_INTERNAL);
	}

	public DevInfo getExternalInfo()
	{
		return getInfo(DEV_EXTERNAL);
	}

	public static final String SDPATH = Environment.getExternalStorageDirectory().toString();

	/*
	 * public static boolean getMountedDirectory(Context mContext, List<String>
	 * mntdir) { mntdir.clear(); Log.i("www1", "1");
	 * 
	 * SysStorage dev = SysStorage.getInstance(); SysStorage.DevInfo info =
	 * null;
	 * 
	 * Log.i("www2", "1"); try { info = dev.getExternalInfo();// External SD
	 * Card Informations } catch (Exception e) { e.printStackTrace();
	 * Log.i("www3", "1"); } Log.i("www4", "1"); if (info != null) { String sf =
	 * info.getMountPoint(); if (sf.equals(SDPATH)) { if
	 * (Environment.MEDIA_MOUNTED
	 * .equals(Environment.getExternalStorageState())) {
	 * mntdir.add(info.getMountPoint()); } } else {
	 * mntdir.add(info.getMountPoint()); } } Log.i("wwe1", "1");
	 * 
	 * 
	 * info = dev.getInternalInfo();// Internal SD Card Informations
	 * Log.i("wwe2", "1"); if (info != null) { String sf = info.getMountPoint();
	 * if (sf.equals(SDPATH)) { if (Environment.MEDIA_MOUNTED
	 * .equals(Environment.getExternalStorageState())) {
	 * mntdir.add(info.getMountPoint()); } } else {
	 * mntdir.add(info.getMountPoint()); } Log.i("iPoint",
	 * info.getMountPoint());//SD �����ص� } if (mntdir.size() > 0) { return
	 * true; } return false; }
	 */
	public static String getDefaultDirectory(final Context mContext, String root)
	{
		List<String> ldf = new ArrayList<String>();
		/*
		 * try { SysStorage.getMountedDirectory(mContext, ldf); for (String ss :
		 * ldf) { String sfn = ss + root; Log.i("qqq0", sfn); File ffn = new
		 * File(sfn); if (ffn.exists()) { return ffn.getPath(); } } } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
		ldf.add(Environment.getExternalStorageDirectory().getPath());
		// Log.i("qqq1", Environment.getExternalStorageDirectory().getPath());
		for (String ss : ldf)
		{
			File pfn = new File(ss);
			if (pfn.canWrite())
			{
				String sfn = ss + root;
				// Log.i("qqq2", sfn);
				File ffn = new File(sfn);
				if (ffn.exists() && ffn.canWrite())
				{
					return ffn.getPath();
				}
				if (ffn.mkdirs())
				{
					return ffn.getPath();
				}
			}
		}
		return root;
	}
}
