package weiqian.hardware;

import android.os.Build;

public class HardwareControl
{
	public static boolean moduleHasLoaded = false;

	public native static void InitCan(int baudrate);

	public native static int OpenCan();

	public native static int CanWrite(int canId, byte[] data);

	public native static CanFrame CanRead(CanFrame mcanFrame, int time);

	public native static void CloseCan();

	public native static void Beep();

	public native static void SetBackLight(boolean flag);

	public native static void StarWatchDog();

	public native static void StopWatchDog();

	public native static void SetWatchDog(int timeout);

	public native static int GetWatchDog();

	public native static void FeedWatchDog();

	public native static void WatchDogEnable(boolean flag);

	public native static int SetMacAddress(String mac);

	public native static int OpenSerialPort(String path, int baud, int databits, String parity, int stopbits);

	public native static void CloseSerialPort(int fd);

	public native static int ReadSerialPort(int fd, byte[] buff, int count);

	public native static int WriteSerialPort(int fd, byte[] buff, int count);

	public static void Init()
	{
		try
		{
			Build bld = new Build();
			String hw = bld.HARDWARE;
			if (0 == hw.compareToIgnoreCase("weiqian"))
			{
				System.loadLibrary("WeiqianHardwareJni");
				moduleHasLoaded = true;
			}
			else
			{
				moduleHasLoaded = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
