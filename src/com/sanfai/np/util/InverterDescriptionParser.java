//
// Inverter±äÆµÆ÷½âÎöÆ÷
//
package com.sanfai.np.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sanfai.np.objects.NMObject.Inverter;

import android.content.Context;

public class InverterDescriptionParser
{
	public static final String TAG_TEXT_ = "#text";
	public static final String TAG_CMNT_ = "#comment";

	public static final String TAG_INVERSTERDESC = "INVERTERDESCPTION";
	public static final String TAG_INVERSTER = "INVERTER";

	public static final String ATT_MANUF = "MANUFACTURE";
	public static final String ATT_MODEL = "MODEL";

	private static String GetAttrVal(NamedNodeMap atts, String att)
	{
		for (int k = 0; k < atts.getLength(); k++)
		{
			Node q = atts.item(k);
			String nn = q.getNodeName();
			String nv = q.getNodeValue();
			if (nn.equalsIgnoreCase(att))
			{
				return nv;
			}
		}
		return "";
	}

	private static String GetNodeText(Node pnode)
	{
		String sv = "";
		NodeList nodes = pnode.getChildNodes();//
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node inode = nodes.item(i);
			String iNodeName = inode.getNodeName();
			if (iNodeName.equalsIgnoreCase(TAG_TEXT_))
			{
				sv += inode.getNodeValue();
			}
		}
		return sv;
	}

	private static Map<String, String> GetNodeAttrs(Node pnode)
	{
		NamedNodeMap atts = pnode.getAttributes();
		Map<String, String> map = new HashMap<String, String>();
		for (int k = 0; k < atts.getLength(); k++)
		{
			Node q = atts.item(k);
			String nn = q.getNodeName();
			String nv = q.getNodeValue();
			map.put(nn.toUpperCase(), nv);
		}
		return map;
	}

	public static String ParseStream(InputStream inputStream, List<Inverter> inverters)
	{
		String sqlStatement = "";
		NodeList nodes;

		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		factory = DocumentBuilderFactory.newInstance();
		try
		{
			builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);

			// Element
			Element root = document.getDocumentElement();

			NamedNodeMap pp = root.getAttributes();
			String rnname = root.getNodeName();
			if (rnname.equalsIgnoreCase(TAG_INVERSTERDESC))
			{
				nodes = root.getChildNodes();//
				for (int i = 0; i < nodes.getLength(); i++)
				{
					Node inode = nodes.item(i);
					String iNodeName = inode.getNodeName();

					if (iNodeName.equalsIgnoreCase(TAG_TEXT_))
					{
					}
					else if (iNodeName.equalsIgnoreCase(TAG_CMNT_))
					{
					}
					else if (iNodeName.equalsIgnoreCase(TAG_INVERSTER))
					{ // è§£æžåˆ›å»ºæ•°æ®åº“çš„è„šæœ¬
						Inverter inv = new Inverter();
						inverters.add(inv);

						inv.CommProp = GetNodeAttrs(inode);

						inv.Manufacture = inv.CommProp.get(ATT_MANUF);
						inv.Model = inv.CommProp.get(ATT_MODEL);

						// NamedNodeMap atts = inode.getAttributes();
						/*
						 * String ver = GetAttrVal(atts, ATT_ver); int iver =
						 * Integer.valueOf(ver); if (iver == vernew) { String sv
						 * = GetNodeText(inode);// .getNodeValue(); sqlStatement
						 * = sv; break; }
						 */
					}
				}
			}

			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			nodes = null;
			root = null;
			document = null;
			builder = null;
			factory = null;
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return sqlStatement;
	}

	public static String ParseExternalFile(Context mContext, String scriptFile, List<Inverter> inverters)
	{
		File sf = null;
		sf = new File(scriptFile);
		if (!sf.exists())
		{
			return "";
		}

		InputStream inputStream = null;

		try
		{
			inputStream = new FileInputStream(sf);

			ParseStream(inputStream, inverters);

			inputStream.close();

			return "";
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		return "";
	}

	public static String ParseAssetFile(Context mContext, String scriptFile, List<Inverter> inverters)
	{
		InputStream inputStream = null;
		try
		{
			inputStream = mContext.getResources().getAssets().open(scriptFile);
			return ParseStream(inputStream, inverters);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		return "";
	}

}
