//
// 说明：之所以设这个类，是想让日期变化广播消息可以发送到此应用中
//     在textCalendar中需要处理日期变化的消息
//
package com.sanfai.np;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DayChangedReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
	}
}
