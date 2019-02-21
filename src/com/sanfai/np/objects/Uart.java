package com.sanfai.np.objects;

import com.finley.helper.USBHelper.UARTProp;

public abstract class Uart
{
	protected String pPort = "COM1";
	protected int pBaudrate = 19200;
	protected int pDataBits = UARTProp.DATABITS_8;
	protected int pStopBits = UARTProp.STOPBITS_1;
	protected int pParity = UARTProp.PARITY_NONE;
	protected String psParity = "None";

	public Uart()
	{
	}

	public Uart(String port, int baud, int databits, String parity, int stopbits)
	{
		setUartProp(port, baud, databits, parity, stopbits);
	}

	public void setUartProp(int baud, int databits, int parity, int stopbits)
	{
		pBaudrate = baud;
		pParity = parity;
		switch (pParity)
		{
		case UARTProp.PARITY_NONE:
			psParity = "None";
			break;
		case UARTProp.PARITY_ODD:
			psParity = "Odd";
			break;
		case UARTProp.PARITY_EVEN:
			psParity = "Even";
			break;
		case UARTProp.PARITY_MARK:
			psParity = "Mark";
			break;
		case UARTProp.PARITY_SPACE:
			psParity = "Space";
			break;
		}
		pDataBits = databits;
		pStopBits = stopbits;
	}

	public void setUartProp(String path, int baud, int databits, String parity, int stopbits)
	{
		pPort = path;
		pBaudrate = baud;
		psParity = parity;
		pParity = UARTProp.PARITY_NONE;
		if (0 == parity.compareToIgnoreCase("None"))
		{
			pParity = UARTProp.PARITY_NONE;
		}
		if (0 == parity.compareToIgnoreCase("N"))
		{
			pParity = UARTProp.PARITY_NONE;
		}

		if (0 == parity.compareToIgnoreCase("Odd"))
		{
			pParity = UARTProp.PARITY_ODD;
		}
		if (0 == parity.compareToIgnoreCase("O"))
		{
			pParity = UARTProp.PARITY_ODD;
		}
		if (0 == parity.compareToIgnoreCase("Even"))
		{
			pParity = UARTProp.PARITY_EVEN;
		}
		if (0 == parity.compareToIgnoreCase("E"))
		{
			pParity = UARTProp.PARITY_EVEN;
		}
		if (0 == parity.compareToIgnoreCase("Mark"))
		{
			pParity = UARTProp.PARITY_MARK;
		}
		if (0 == parity.compareToIgnoreCase("M"))
		{
			pParity = UARTProp.PARITY_MARK;
		}
		if (0 == parity.compareToIgnoreCase("Space"))
		{
			pParity = UARTProp.PARITY_SPACE;
		}
		if (0 == parity.compareToIgnoreCase("S"))
		{
			pParity = UARTProp.PARITY_SPACE;
		}
		pDataBits = databits;
		pStopBits = stopbits;
	}

	public boolean open(String path, int baud, int databits, String parity, int stopbits)
	{
		setUartProp(path, baud, databits, parity, stopbits);
		return open();
	}

	abstract boolean isActive();

	abstract boolean open();

	abstract void close();

	abstract int read(byte[] buff, int count);

	abstract int write(byte[] buff, int count);

	abstract String read();

	abstract void write(String buff);
}
