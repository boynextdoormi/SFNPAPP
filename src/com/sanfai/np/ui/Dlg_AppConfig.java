package com.sanfai.np.ui;

import java.util.ArrayList;
import java.util.List;

import com.sanfai.np.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

public class Dlg_AppConfig
{

	public interface OnDlgClosedListener
	{
		public boolean OnClose(View view, int buttonid, String port, int addr);
	}

	public static void Dialog_ConfigComport(final Context mContext, final OnDlgClosedListener osl)
	{
		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogView = mInflater.inflate(R.layout.lay_comportconfig, (ViewGroup) null);
		final Dialog dialog = ShowDialog(mContext, dialogView);

		View v;
		v = dialogView.findViewById(R.id.btn_confirm);
		v.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (osl != null)
				{
					String[] mPortList = mContext.getResources().getStringArray(R.array.port_available);
					Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnerPortList);
					int portid = spinner.getSelectedItemPosition();
					if (portid > mPortList.length)
					{
						return;
					}
					String port = mPortList[portid];
					spinner = (Spinner) dialogView.findViewById(R.id.editDevAddr);
					int iaddr = 1 + spinner.getSelectedItemPosition();
					if (osl.OnClose(dialogView, R.id.btn_confirm, port, iaddr))
					{
						dialog.dismiss();
					}
				}
			}
		});

		v = dialogView.findViewById(R.id.btn_cancel);
		v.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (osl != null)
				{
					if (osl.OnClose(dialogView, R.id.btn_confirm, null, 0))
					{
						dialog.dismiss();
					}
				}
			}
		});

		int i;
		final String[] addrList = new String[255];
		for (i = 1; i <= 255; i++)
		{
			addrList[i - 1] = Integer.toString(i);
		}
		// ����һ��SimpleAdapter���������������Ӧ��ϵ
		ArrayAdapter<String> aadapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
				addrList);
		Spinner aspinner = (Spinner) dialogView.findViewById(R.id.editDevAddr);
		// ��Adapter��Spinner��
		aspinner.setAdapter(aadapter);
		// Spinner��ѡ���¼��󶨡�
		aspinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});

		// װ�����飬��ΪSpinner����Դ
		final String[] mPortList = mContext.getResources().getStringArray(R.array.port_available);
		// ����һ��SimpleAdapter���������������Ӧ��ϵ
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
				mPortList);
		Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnerPortList);
		// ��Adapter��Spinner��
		spinner.setAdapter(adapter);
		// Spinner��ѡ���¼��󶨡�
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});
	}

	public static Dialog ShowDialog(Context mContext, View dialogView)
	{
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر���
		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.dialogbox);
		window.setContentView(dialogView);
		dialog.show();
		return dialog;
	}

	public static class OnDialogBtnClicked implements OnClickListener
	{
		private final int Id;
		private final int msgId;
		private final Handler mCallbackHandler;
		private final View dlgView;
		private final Dialog dlg;

		public OnDialogBtnClicked(Dialog d, View v, Handler h, int m, int id)
		{
			dlg = d;
			dlgView = v;
			mCallbackHandler = h;
			msgId = m;
			Id = id;
		}

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_confirm:
			{
				Message msg = mCallbackHandler.obtainMessage();
				msg.what = msgId;
				msg.arg1 = Id;
				msg.obj = dlgView;
				mCallbackHandler.sendMessage(msg);
			}
				dlg.dismiss();
				break;
			case R.id.btn_cancel:
				dlg.dismiss();
				break;
			}
		}
	}

	//
	// �����б��ؼ�
	//
	public static class DropDownListView extends LinearLayout
	{
		private final Context mContext;
		private TextView editText;
		private ImageView imageView;
		private PopupWindow popupWindow = null;
		private List<String> dataList = new ArrayList<String>();
		private View mView;

		public DropDownListView(Context context)
		{
			super(context);
			mContext = context;
		}

		public DropDownListView(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			mContext = context;
			initView();
		}

		public DropDownListView(Context context, AttributeSet attrs, int defStyle)
		{
			super(context, attrs, defStyle);
			mContext = context;
			initView();
		}

		public void initView()
		{
			String infServie = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater layoutInflater;
			layoutInflater = (LayoutInflater) mContext.getSystemService(infServie);
			View view = layoutInflater.inflate(R.layout.lays_dropdownlistview, this, true);
			editText = (TextView) findViewById(R.id.textEditor);
			imageView = (ImageView) findViewById(R.id.btn_dropdown);
			this.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					if (popupWindow == null)
					{
						showPopWindow();
					}
					else
					{
						closePopWindow();
					}
				}
			});
		}

		// �������б���
		private void showPopWindow()
		{
			// ����popupWindow�Ĳ����ļ�
			String infServie = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater layoutInflater;
			layoutInflater = (LayoutInflater) mContext.getSystemService(infServie);
			View contentView = layoutInflater.inflate(R.layout.lays_dropdownlistwindow, (ViewGroup) null, false);
			ListView listView = (ListView) contentView.findViewById(R.id.listView);

			listView.setAdapter(new DropDownListAdapter(mContext, dataList));
			popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
			popupWindow.setOutsideTouchable(true);
			popupWindow.showAsDropDown(this);
		}

		// �ر������б���
		private void closePopWindow()
		{
			popupWindow.dismiss();
			popupWindow = null;
		}

		// ��������
		public void setItemsData(List<String> list)
		{
			dataList = list;
			editText.setText(list.get(0).toString());
		}

		// ����������
		class DropDownListAdapter extends BaseAdapter
		{

			Context mContext;
			List<String> mData;
			LayoutInflater inflater;

			public DropDownListAdapter(Context ctx, List<String> data)
			{
				mContext = ctx;
				mData = data;
				inflater = LayoutInflater.from(mContext);
			}

			@Override
			public int getCount()
			{
				// TODO Auto-generated method stub
				return mData.size();
			}

			@Override
			public Object getItem(int position)
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int position)
			{
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				// TODO Auto-generated method stub
				// �Զ�����ͼ
				ListItemView listItemView = null;
				if (convertView == null)
				{
					// ��ȡlist_item�����ļ�����ͼ
					convertView = inflater.inflate(R.layout.lays_dropdownlistitem, (ViewGroup) null);

					listItemView = new ListItemView();
					// ��ȡ�ؼ�����
					listItemView.tv = (TextView) convertView.findViewById(R.id.itemtext);
					listItemView.layout = (LinearLayout) convertView.findViewById(R.id.layout_container);
					// ���ÿؼ�����convertView
					convertView.setTag(listItemView);
				}
				else
				{
					listItemView = (ListItemView) convertView.getTag();
				}

				// ��������
				listItemView.tv.setText(mData.get(position).toString());
				final String text = mData.get(position).toString();
				listItemView.layout.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						// TODO Auto-generated method stub
						editText.setText(text);
						closePopWindow();
					}
				});
				return convertView;
			}
		}
	}

	private static class ListItemView
	{
		TextView tv;
		LinearLayout layout;
	}
}
