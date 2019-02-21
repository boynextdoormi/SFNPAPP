package com.finley.usercomponents;

import com.sanfai.np.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ucCheckBox extends RelativeLayout
{
	private int callbackId = 0;
	private onCheckListener omCallback = null;

	public interface onCheckListener
	{
		abstract void onClick(ucCheckBox obj, int id, boolean bChecked);
	}

	public ucCheckBox(Context context)
	{
		super(context);
		InitLayout(context);
	}

	public ucCheckBox(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		InitLayout(context);
	}

	public ucCheckBox(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		InitLayout(context);
	}

	public ucCheckBox(Context context, String text, onCheckListener oc, int id)
	{
		super(context);
		InitLayout(context);
		omCallback = oc;
		callbackId = id;
		setText(text);
	}

	public void setText(String s)
	{
		TextView tv = (TextView) findViewById(R.id.textView);
		tv.setText(s);
	}

	public void setChecked(boolean b)
	{
		CheckBox tv = (CheckBox) findViewById(R.id.checkBox1);
		tv.setChecked(b);
	}

	private void InitLayout(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.uc_checkbox, this);
		CheckBox tv = (CheckBox) findViewById(R.id.checkBox1);
		tv.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{
				if (omCallback != null)
				{
					CheckBox cb = (CheckBox) arg0;
					boolean b = cb.isChecked();
					omCallback.onClick(ucCheckBox.this, callbackId, b);
				}
			}
		});
		Update();
	}

	public void Destroy()
	{

	}

	public void Update()
	{

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		resizeText();
	}

	private void resizeText()
	{

	}

}
