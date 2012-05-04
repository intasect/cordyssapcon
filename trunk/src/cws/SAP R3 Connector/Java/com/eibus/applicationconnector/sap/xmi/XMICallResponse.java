package com.eibus.applicationconnector.sap.xmi;

import com.eibus.xml.nom.Node;

/** This is a utility class to parse XMICall response
 * @author Vamsi Mohan Jayanti
 *
 */
public class XMICallResponse 
{
	/*
	 * <RETURN>
		<TYPE/>
		<ID/>
		<NUMBER>000</NUMBER>
		<MESSAGE/>
		<LOG_NO/>
		<LOG_MSG_NO>000000</LOG_MSG_NO>
		<MESSAGE_V1/>
		<MESSAGE_V2/>
		<MESSAGE_V3/>
		<MESSAGE_V4/>
		<PARAMETER/>
		<ROW>0</ROW>
		<FIELD/>
		<SYSTEM/>
	</RETURN>
	 */
	
	String type ;
	String id ;
	String number ;
	String message ;
	String log_on ;
	String log_msg_on;
	String message_v1 ;
	String message_v2 ;
	String message_v3 ;
	String message_v4 ;
	String parameter ;
	String row ;
	String field ;
	String system ;
	
	public static String TAG_TYPE = "TYPE";
	public static String TAG_ID ="ID";
	public static String TAG_NUMBER ="NUMBER";
	public static String TAG_MESSAGE ="MESSAGE";
	public static String TAG_LOG_ON ="LOG_ON";
	public static String TAG_LOG_MSG_NO ="LOG_MSG_NO";
	public static String TAG_MESSAGE_V1 = "MESSAGE_V1";
	public static String TAG_MESSAGE_V2 = "MESSAGE_V2";
	public static String TAG_MESSAGE_V3 = "MESSAGE_V3";
	public static String TAG_MESSAGE_V4 = "MESSAGE_V4";
	public static String TAG_PARAMETER= "PARAMETER" ;
	public static String TAG_ROW = "ROW";
	public static String TAG_FIELD = "FIELD";
	public static String TAG_SYSTEM = "SYSTEM";
	
//	public static XMICallResponse parseResponse(int rfcCallResponseNode)
//	{
//		return new XMICallResponse(rfcCallResponseNode) ;
//	
//	}
//	
//	public boolean hasApplicationError()
//	{
//		return false;
//	}	
	
//	private XMICallResponse(int rfcCallResponse)
//	{
//		int returnNode = Node.getElement(rfcCallResponse, "RETURN");
//		type = Node.getDataElement(returnNode, TAG_TYPE, "") ;
//		id = Node.getDataElement(returnNode, TAG_ID, "") ;
//		number = Node.getDataElement(returnNode, TAG_NUMBER, "") ;
//		message = Node.getDataElement(returnNode, TAG_MESSAGE, "") ;
//		log_on = Node.getDataElement(returnNode, TAG_LOG_ON, "") ;
//		log_msg_on = Node.getDataElement(returnNode, TAG_LOG_MSG_NO, "") ;
//		message_v1 = Node.getDataElement(returnNode, TAG_MESSAGE_V1, "") ;
//		message_v2 = Node.getDataElement(returnNode, TAG_MESSAGE_V2, "") ;
//		message_v2 = Node.getDataElement(returnNode, TAG_MESSAGE_V3, "") ;
//		type = Node.getDataElement(returnNode, TAG_MESSAGE_V4, "") ;
//		type = Node.getDataElement(returnNode, TAG_PARAMETER, "") ;
//		type = Node.getDataElement(returnNode, TAG_ROW, "") ;
//		type = Node.getDataElement(returnNode, TAG_FIELD, "") ;
//		type = Node.getDataElement(returnNode, TAG_SYSTEM, "") ;
//		
//		
//	}
	
	public static boolean hasException(int rfcCallResponse)
	{
		int returnNode = Node.getElement(rfcCallResponse, "RETURN");
		String type = Node.getDataElement(returnNode, TAG_TYPE, "") ;
		if("E".equalsIgnoreCase(type))
		{
			return true;
		}
		return false ;
	}

}
