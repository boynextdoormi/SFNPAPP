package weiqian.hardware;

public class SerialPort
{

	private int mFd;

	public SerialPort()
	{
		mFd = -1;
	}

	public boolean isActive()
	{
		return (mFd > 0);
	}

	public boolean open(String path, int baud, int databits, String parity, int stopbits)
	{

		if (!HardwareControl.moduleHasLoaded)
		{
			try
			{
				HardwareControl.Init();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		if (!HardwareControl.moduleHasLoaded)
		{
			return false;
		}

		try
		{
			mFd = HardwareControl.OpenSerialPort(path, baud, databits, parity, stopbits);
			return (mFd > 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void close()
	{
		HardwareControl.CloseSerialPort(mFd);
	}

	public int read(byte[] buff, int count)
	{
		return HardwareControl.ReadSerialPort(mFd, buff, count);
	}

	public int write(byte[] buff, int count)
	{
		return HardwareControl.WriteSerialPort(mFd, buff, count);
	}

	public String read()
	{
		byte[] buff = new byte[64];
		read(buff, 64);
		return buff.toString();
	}

	public void write(String buff)
	{
		write(buff.getBytes(), buff.length());
	}
}
