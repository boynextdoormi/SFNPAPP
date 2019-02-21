package com.finley.usercomponents;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardBuilder
{
	// private Activity mActivity;
	private KeyboardView mKeyboardView;
	private Activity EditorActivity;
	private Context mContext;

	public class Constant
	{
		public static final int CodeDelete = 60001;
		public static final int CodeCancel = 60002;
	}

	public KeyboardBuilder(Context context, KeyboardView keyboardView, int keyBoardXmlResId)
	{
		mContext = context;

		mKeyboardView = keyboardView;
		Keyboard mKeyboard = new Keyboard(mContext, keyBoardXmlResId);
		// Attach the keyboard to the view
		mKeyboardView.setKeyboard(mKeyboard);
		// Do not show the preview balloons
		mKeyboardView.setPreviewEnabled(false);

		KeyboardView.OnKeyboardActionListener keyboardListener = new KeyboardView.OnKeyboardActionListener()
		{
			@Override
			public void onKey(int primaryCode, int[] keyCodes)
			{
				// Get the EditText and its Editable
				if (EditorActivity == null)
				{
					return;
				}

				View focusCurrent = ((Activity) EditorActivity).getCurrentFocus();

				if (focusCurrent == null || !(focusCurrent instanceof EditText))
				{
					return;
				}

				EditText edittext = (EditText) focusCurrent;
				Editable editable = edittext.getText();
				int start = edittext.getSelectionStart();
				// Handle key
				if (primaryCode == Constant.CodeCancel)
				{
					hideCustomKeyboard();
				}
				else if (primaryCode == Constant.CodeDelete)
				{
					if (editable != null && start > 0)
					{
						editable.delete(start - 1, start);
					}
				}
				else
				{
					// Insert character
					editable.insert(start, Character.toString((char) primaryCode));
				}
			}

			@Override
			public void onPress(int arg0)
			{
			}

			@Override
			public void onRelease(int primaryCode)
			{
			}

			@Override
			public void onText(CharSequence text)
			{
			}

			@Override
			public void swipeDown()
			{
			}

			@Override
			public void swipeLeft()
			{
			}

			@Override
			public void swipeRight()
			{
			}

			@Override
			public void swipeUp()
			{
			}
		};
		mKeyboardView.setOnKeyboardActionListener(keyboardListener);
	}

	// °ó¶¨Ò»¸öEditText
	public void registerEditText(Activity edact, EditText editText)
	{
		EditorActivity = edact;

		// Make the custom keyboard appear
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					showCustomKeyboard(v);
				}
				else
				{
					hideCustomKeyboard();
				}
			}
		});
		editText.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showCustomKeyboard(v);
			}
		});
		editText.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				EditText edittext = (EditText) v;
				int inType = edittext.getInputType(); // Backup the input type
				edittext.setInputType(InputType.TYPE_NULL); // Disable standard
															// keyboard
				edittext.onTouchEvent(event); // Call native handler
				edittext.setInputType(inType); // Restore input type
				edittext.setSelection(edittext.getText().length());
				return true;
			}
		});
	}

	public void hideCustomKeyboard()
	{
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}

	public void showCustomKeyboard(View v)
	{
		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);
		if (v != null)
		{
			((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	public boolean isCustomKeyboardVisible()
	{
		return mKeyboardView.getVisibility() == View.VISIBLE;
	}
}
