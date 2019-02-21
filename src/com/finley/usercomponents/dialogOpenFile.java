package com.finley.usercomponents;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sanfai.np.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class dialogOpenFile
{
	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/";
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "No rights to access!";

	// 简单的Bundle参数回调接口
	public interface dialogCallback
	{
		abstract void callback(Dialog obj, Bundle bundle);
	}

	// 参数说明
	// context:上下文
	// dialogid:对话框ID
	// title:对话框标题
	// callback:一个传递Bundle参数的回调接口
	// suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
	// images:用来根据后缀显示的图标资源ID。
	// 根目录图标的索引为sRoot;
	// 父目录的索引为sParent;
	// 文件夹的索引为sFolder;
	// 默认图标的索引为sEmpty;
	// 其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
	public static Dialog createDialog(int id, Context context, String title, dialogCallback callback, String stdir,
			String suffix, Map<String, Integer> images)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		FileSelectView fsv = new FileSelectView(context, id, null, callback, stdir, suffix, images);
		builder.setView(fsv);
		Dialog dialog = builder.create();
		fsv.setDialog(dialog);
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);
		return dialog;
	}

	static class FileSelectView extends ListView implements OnItemClickListener
	{
		private dialogCallback callback = null;
		private int dialogid;
		private Dialog mdialog;

		private String path = sRoot;
		private List<Map<String, Object>> list = null;
		private String topDir = sRoot;

		private String suffix = null;
		List<String> suffixList = new ArrayList<String>();

		private Map<String, Integer> imagemap = null;

		public FileSelectView(Context context, int dlgId, Dialog dialog, dialogCallback callback, String stdir,
				String psuffix, Map<String, Integer> images)
		{
			super(context);
			this.path = stdir;
			this.topDir = stdir;

			this.imagemap = images;
			this.suffix = (psuffix == null ? "" : psuffix.toLowerCase());

			this.dialogid = dlgId;
			this.mdialog = dialog;
			this.callback = callback;

			this.setOnItemClickListener(this);

			this.setMinimumHeight(600);

			refreshFileList();
		}

		public void setDialog(Dialog dialog)
		{
			this.mdialog = dialog;
		}

		private String getSuffix(String filename)
		{
			int dix = filename.lastIndexOf('.');
			if (dix < 0)
			{
				return "";
			}
			else
			{
				return filename.substring(dix + 1);
			}
		}

		private int getImageId(String s)
		{
			if (imagemap == null)
			{
				return 0;
			}
			else if (imagemap.containsKey(s))
			{
				return imagemap.get(s);
			}
			else if (imagemap.containsKey(sEmpty))
			{
				return imagemap.get(sEmpty);
			}
			else
			{
				return 0;
			}
		}

		private int refreshFileList()
		{
			// 刷新文件列表
			File[] files = null;
			try
			{
				files = new File(path).listFiles();

				// Collections.sort();

			}
			catch (Exception e)
			{
				files = null;
			}
			if (files == null)
			{
				// 访问出错
				Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT).show();
				return -1;
			}
			try
			{
				Arrays.sort(files, 0, files.length, new Comparator()
				{
					@Override
					public int compare(Object lhs, Object rhs)
					{
						File a = (File) lhs;
						File b = (File) rhs;
						return a.getName().compareToIgnoreCase(b.getName());
					}

				});
			}
			catch (Exception e)
			{
			}

			if (list != null)
			{
				list.clear();
			}
			else
			{
				list = new ArrayList<Map<String, Object>>(files.length);
			}

			// 用来先保存文件夹和文件夹的两个列表
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();

			if (!this.path.equals(sRoot))
			{
				// 添加根目录 和 上一层目录
				Map<String, Object> map;

				// map = new HashMap<String, Object>();
				// map.put("name", sRoot);
				// map.put("path", sRoot);
				// map.put("img", getImageId(sRoot));
				// list.add(map);

				// if (0 != topDir.compareToIgnoreCase(path))
				{
					map = new HashMap<String, Object>();
					map.put("name", sParent);
					map.put("path", path);
					map.put("img", getImageId(sParent));
					list.add(map);
				}
			}

			for (File file : files)
			{
				if (!file.getName().startsWith("."))
				{
					if (file.isDirectory() && file.listFiles() != null)
					{
						// 添加文件夹
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("img", getImageId(sFolder));
						lfolders.add(map);
					}
					else if (file.isFile())
					{
						// 添加文件
						String sf = getSuffix(file.getName()).toLowerCase();

						if (suffix == null || suffix.length() == 0
								|| (sf.length() > 0 && suffix.indexOf("." + sf + ";") >= 0))
						{
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("name", file.getName());
							map.put("path", file.getPath());
							map.put("img", getImageId(sf));
							lfiles.add(map);
						}
					}
				}
			}

			list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
			list.addAll(lfiles); // 再添加文件

			SimpleAdapter adapter;

			adapter = new SimpleAdapter(getContext(), list, R.layout.uc_dialogopenfileitem,
					new String[] { "img", "name", "path" },
					new int[] { R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path });
			this.setAdapter(adapter);
			return files.length;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id)
		{
			// 条目选择
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			if (fn.equals(sRoot) || fn.equals(sParent))
			{
				// 如果是更目录或者上一层
				File fl = new File(pt);
				String ppt = fl.getParent();
				if (ppt != null)
				{
					// 返回上一层
					path = ppt;
				}
				else
				{
					// 返回更目录
					path = sRoot;
				}
			}
			else
			{
				File fl = new File(pt);
				if (fl.isFile())
				{
					// 如果是文件
					// 设置回调的返回值
					Bundle bundle = new Bundle();
					bundle.putString("path", pt);
					bundle.putString("name", fn);
					// 调用事先设置的回调函数
					this.callback.callback(this.mdialog, bundle);
					// 让文件夹对话框消失
					if (null != this.mdialog)
					{
						this.mdialog.dismiss();
					}
					return;
				}
				else if (fl.isDirectory())
				{
					// 如果是文件夹
					// 那么进入选中的文件夹
					path = pt;
				}
			}
			this.refreshFileList();
		}
	}
}