package com.eibus.applicationconnector.sap.util;


import java.util.HashMap;
import java.util.HashSet;

/**
 * This class converts special character to HEX UTF8 codes
 * @author Vamsi Mohan Jayanti
 *
 */
public class SpecialCharactersConvertorUtil
{


	private static HashMap<String, String> charHexaMap = new HashMap<String, String>();
	private static HashMap<String, String> hexaCharMap = new HashMap<String, String>();
	private static HashSet<String> spCharSet	= new HashSet<String>();
	private static HashSet<String> hexaCharSet	= new HashSet<String>();
		
	static
	{
		charHexaMap.put("/", "_x002F_");
		charHexaMap.put("\\", "_x005C_");
		charHexaMap.put(":", "_x003A_");
		charHexaMap.put("*", "_x002A_");
		charHexaMap.put("?", "_x003F_");
		charHexaMap.put("\"", "_x0022_");		
		charHexaMap.put("<", "_x003C_");
		charHexaMap.put(">", "_x003E_");
		
		
		hexaCharMap.put("_x002F_", "/");
		hexaCharMap.put("_x005C_", "\\");
		hexaCharMap.put("_x003A_", ":");
		hexaCharMap.put("_x002A_", "*");
		hexaCharMap.put("_x003F_", "?");
		hexaCharMap.put("_x0022_", "\"");
		hexaCharMap.put("_x003C_", "<");
		hexaCharMap.put("_x003E_", ">");
		
		
//		spCharSet.add("/");
//		spCharSet.add("\\");
//		spCharSet.add(":");
//		spCharSet.add("*");
//		spCharSet.add("?");
//		spCharSet.add("\"");
//		spCharSet.add("<");
//		spCharSet.add(">");
//		
//		hexaCharSet.add("_x002F_");
//		hexaCharSet.add("_x005C_");
//		hexaCharSet.add("_x003A_");
//		hexaCharSet.add("_x002A_");
//		hexaCharSet.add("_x003F_");
//		hexaCharSet.add("_x0022_");
//		hexaCharSet.add("_x003C_");
//		hexaCharSet.add("_x003E_");
		
	}
	
		
	public static String encodeToHexa(String objectName) 
	{		
		if(objectName == null || "".equals(objectName.trim()))
		{
			return objectName;
		}
		
		
		Character[] inputCharArray	= getCharacterArray(objectName);
		
		int inputStringLength	= inputCharArray.length;
		
		StringBuffer encodedString	= new StringBuffer();
		
			
		for (int i=0; i < inputStringLength; i++)
		{
			//if(spCharSet.contains(inputCharArray[i].toString()))
			if(charHexaMap.containsKey(inputCharArray[i].toString()))
			{
				encodedString.append(charHexaMap.get(inputCharArray[i].toString()));
			}
			else
			{
				encodedString.append(inputCharArray[i].toString());
			}
		}	
		return encodedString.toString();
	}
	
	
	public static String decodeFromHexa(String objectName)
	{
		
		if(objectName == null || "".equals(objectName.trim()))
		{
			return objectName;
		}
		
		StringBuffer encodedString	= new StringBuffer();
		int length = objectName.length() - 6;
		
		if(length > 0)
		{
			for(int i=0; i < length; i++)
			{
				String spString	= getNextSPFromInput(objectName);
				
				//if(hexaCharSet.contains(spString))
				if(hexaCharMap.containsKey(spString))
				{
					encodedString.append(hexaCharMap.get(spString));
					if(objectName.length() > 7)
					{
						objectName = objectName.substring(7,objectName.length());
					}
						
					else if(objectName.length() == 7)
					{
						break;
					}
				}
				else
				{
					if(objectName.length() > 7)
					{
						objectName = objectName.substring(1,objectName.length());
						encodedString.append(spString.substring(0, 1));
					}
					else
					{
						encodedString.append(objectName);
						break;
					}
					
				}
			}
		}
			
		else
		{
			encodedString.append(objectName);
		}
	
		return encodedString.toString();
	}
	
	
	
	private static String getNextSPFromInput(String objectName)
	{
		if(objectName.length() >= 7)
		{
			if(objectName.charAt(0) == '_' && objectName.charAt(1) == 'x' && objectName.charAt(6) == '_')
			{
				return objectName.substring(0, 7);
			}
		}
		
		return objectName;
	}
	
	private static Character[] getCharacterArray(String objectName)
	{
		int length = objectName.length();
		
		Character[] inputCharArray = new Character[length];
		
		for(int i=0; i < length; i++)
		{
			inputCharArray[i] = objectName.charAt(i);
		}
		
		return inputCharArray;
	}
	
	
	public static void main(String[] args)
	{
		try
		{
			
			
			String inputString	= "abc/";
			
			System.out.println("inputString is "+ inputString);
			
			String outputString = SpecialCharactersConvertorUtil.encodeToHexa(inputString);
			
			System.out.println("outputstring is "+outputString);		
			
			String inputString1	= "1234567890";
			
			System.out.println("inputString is "+ inputString1);
			
			String outputString1 = SpecialCharactersConvertorUtil.decodeFromHexa(inputString1);
			
			System.out.println("outputstring is "+outputString1);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}

}
