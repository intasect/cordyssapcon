package com.cordys.test.idoc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.soap.encoding.soapenc.Base64;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.config.SAPConfigurationFactory;
import com.eibus.applicationconnector.sap.connection.jco.JCoMethodGenerator;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestSender;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.metadata.MethodGenerator;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;

public class StartIDOCServer 
{
	public  static Document doc = new Document() ;
	public static  ISAPConfiguration m_config;

	public static void main(String args[]) throws XMLException, SAPConfigurationException, SAPConnectorException, IOException
	{
		if(true)
		{
			String fName = "D:/Projects/Schneider/cache/f3.txt" ;
			readObjectContent(fName);
			return ;
		}
		LoggerConfigurator.initLogger("./test/Log4jConfiguration.xml");
//		  System.setProperty("java.library.path",
//                  "D:/Program Files/Cordys3/defaultInst3/lib" + File.pathSeparator +
//                  System.getProperty("java.library.path"));
        System.setProperty("java.library.path",
                           "./docs/internal/sapdlls" + File.pathSeparator +
                           System.getProperty("java.library.path"));
		 
		
		  int node = doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");
		  m_config = SAPConfigurationFactory.createSAPConfiguration(node,
                  "o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com",
                  "cn=SAPConnector,cn=soap nodes,o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com");
		  if(true)
			 {
			  serializeIDOCMetadataObject() ;
				// createMethod() ;
			  //getIDOCStatus() ;
			  System.out.println("-------------------------------------------DONE");
				 return ;
			 }
		  System.out.println("config created");
		 // m_config.startIDOCListeners(1, "10.222.138.242", "sapgw01", "CORDYSIDOCLIST");
	}
	
	public static void  createMethod() throws SAPConnectorException, XMLException
	{
	
		int response = 0;
		int request = doc.load(".\\test\\java\\com\\cordys\\test\\idoc\\GenerateMethodsRequest.xml") ;
		response = doc.createElementNS("GenerateMethodsResponse", null, null, "http://schemas.cordys.com/2.0/SAPSchema", 0);
		System.out.println(Node.writeToString(response, true));
		System.out.println(Node.writeToString(request, true));
		
		if(false)
			return ;
		  MethodGenerator mg = new JCoMethodGenerator(request, response,
				  m_config.getRepository(),
				  m_config.getIDOCRepository());
		  mg.execute();
		  System.out.println("After execution");
		 // System.out.println(Node.writeToString(response, true));
		  Node.writeToFile(response, response, "D:\\Projects\\Schneider\\SAPFunctions\\wsdl1.xml", 1);
		
	}
	
	public static void getIDOCStatus() throws SAPConnectorException
	{
		SAPJCoRequestSender requestSender = new SAPJCoRequestSender(m_config) ;
		//requestSender.synchronizeIDOCStatus("", m_config.getJCoConnectionManager().getUserConnection(m_config, m_config.getUserID(),m_config.getPassword()), doc);
		int statusNode = requestSender.getIDOCStatusFromSAP("0000000000510809", m_config.getJCoConnectionManager().getUserConnection(m_config, m_config.getUserID(),m_config.getPassword()), doc);
		System.out.println(Node.writeToString(statusNode, true));
		
	}
	
	public static void serializeIDOCMetadataObject()
	{
		IDoc.Document idoc = JCoIDoc.createDocument( m_config.getIDOCRepository(), "DEBMAS06", "YSADEBMAS06");
        IDoc.Segment rootSegment = idoc.getRootSegment();
        
        String fileName = "D:/Projects/Schneider/cache/f3.txt" ;
        try {
			FileOutputStream fos = new FileOutputStream(fileName) ;
			ObjectOutputStream oBjos = new ObjectOutputStream(fos) ;
			oBjos.writeObject(rootSegment.getSegmentMetaData()) ;
			oBjos.close();
			fos.close();
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
		} catch (IOException e) {
		
			e.printStackTrace();
		}       
        
		File fileObj = new File(fileName);
		try {
			FileInputStream fReadStream = new FileInputStream(fileObj) ;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void readObjectContent(String fileName) throws IOException
	{
		File file = new File(fileName);
		byte[] byteArray = new byte[202400] ;
		FileInputStream fis = new FileInputStream(file);
		StringBuffer sBuffer = new StringBuffer() ;
		int bytesRead = 0 ;
		while((bytesRead = fis.read(byteArray))!=-1)
		{
			byte[] actualBytesRead = new byte[bytesRead];
			String encodedString = Base64.encode(actualBytesRead) ;
			sBuffer.append(encodedString);
			System.arraycopy(byteArray, 0, actualBytesRead, 0, bytesRead);
			System.out.println(actualBytesRead.length);
		}
		System.out.println(sBuffer.length());
		fis.close();
		
	}
	}
