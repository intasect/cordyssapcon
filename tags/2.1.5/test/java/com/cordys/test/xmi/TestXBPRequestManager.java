package com.cordys.test.xmi;

import static org.junit.Assert.*;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.config.SAPConfigurationFactory;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnection;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnectionManager;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestSender;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.metadata.MetadataCacheFactory;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.xmi.XMISessionContext;
import com.eibus.applicationconnector.sap.xmi.xbp.XBPRequestManager;
import com.eibus.applicationconnector.sap.xmi.xbp.XBPSessionContext;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class TestXBPRequestManager {
	private static String COPY_RUN_JOB_REQUEST = 
						"<CopyAndRunJob xmlns=\"\">"+
							"<SourceJobDetails>"+
							"<JobName>CORDYSCUSTOMERRECV</JobName>"+
							"<JobId>13544000</JobId>"+
							"</SourceJobDetails>"+
							"<TargetJobDetails>"+
							"<JobName>CORDYSCUSTOMERRECV</JobName>"+
							"</TargetJobDetails>"+
						"</CopyAndRunJob>" ;
 static Document doc = new Document();
 private ISAPConfiguration m_config;
	@Before
	public void setUp() throws Exception 
	{
		 LoggerConfigurator.initLogger("./test/Log4jConfiguration.xml");
	        System.setProperty("java.library.path",
	                           "./docs/internal/sapdlls" + File.pathSeparator +
	                           System.getProperty("java.library.path"));
		
		
	}
	
	

	
	public void t1estProcessRequest() {
		  int node =0;
		try {
			node = doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");
		

	     
	            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
	                                                                      "o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com",
	                                                                      "cn=SAPConnector,cn=soap nodes,o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com");
	            
	            SAPJCoConnectionManager connManager = new SAPJCoConnectionManager(1) ;	            
				String user = "SESA81764";
				String password = "chateau";
				SAPJCoConnection conn = connManager.getUserConnection(m_config, user, password) ; 
				System.out.println("Got Connection"+conn);
				XBPSessionContext session = new XBPSessionContext();
				session.setExternalUserId(user);
				session.setSessionConnection(conn);
				System.out.println("Before call");
				XBPRequestManager xbpRequestManager = new XBPRequestManager() ;
				session.setNomDocument(doc);
				xbpRequestManager.setSession(session);
				int request = 0;
				int response = 0;
				int methodImplementation = 0;
				xbpRequestManager.setM_config(m_config);
				int responseNode = xbpRequestManager.processRequest(request, response, methodImplementation) ;
				System.out.println(Node.writeToString(responseNode, true));
				conn.disconnect();
	        
			} catch (XMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAPConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception SAPConnectorException) {
				// TODO Auto-generated catch block
				SAPConnectorException.printStackTrace();
			}
	        finally
	        {
	            BACUtil.deleteNode(node);
	        }
		//fail("Not yet implemented");
	}
	
	//@Test
	public void sendRFCRequestToGetAllIdocs()
	{
		
		  int node =0;
		try{
				node = doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");
			

		     
		            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
		                                                                      "o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com",
		                                                                      "cn=SAPConnector,cn=soap nodes,o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com");
		            
		            SAPJCoConnectionManager connManager = new SAPJCoConnectionManager(1) ;	            
					String user = "SESA81764";
					String password = "chateau";
					SAPJCoConnection conn = connManager.getUserConnection(m_config, user, password) ; 
					
					try{
						   SAPJCoRequestSender requestSender = new SAPJCoRequestSender(m_config) ;
						//   System.out.println(this.getProcessorConfiguration());
						   String GET_ALL_IDOCS_REQUEST = "<IDOCTYPES_FOR_MESTYPE_READ xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\">" +
                            "<P_MESTYP>*</P_MESTYP>" +
                            "</IDOCTYPES_FOR_MESTYPE_READ>";
						   int request = doc.parseString(GET_ALL_IDOCS_REQUEST) ;
						   System.out.println("Request is:" + Node.writeToString(request, true));
						   int response = 0 ; 
							 //  requestSender.sendRFCRequest(0, conn,"" ) ;
						
						   
						   int allIDOCs = requestSender.sendRFCRequestForInternalPurpose(GET_ALL_IDOCS_REQUEST,
								   conn,
                                   "IDOCTYPES_FOR_MESTYPE_READ",
                                   "rfc:IDOCTYPES_FOR_MESTYPE_READ",
                                  doc);
						   System.out.println("Response is:" + Node.writeToString(response, true));
						 //  return  response;
						} finally
						{
							
						}
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAPConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception SAPConnectorException) {
			// TODO Auto-generated catch block
			SAPConnectorException.printStackTrace();
		}
        finally
        {
            BACUtil.deleteNode(node);
        }
		
	}
	
	@Test
	public void sendRFCRequestToReadIDOCMetadata()
	{
		
		  int node =0;
		try{
				node = doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");
			

		     
		            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
		                                                                      "o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com",
		                                                                      "cn=SAPConnector,cn=soap nodes,o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com");
		            
		            SAPJCoConnectionManager connManager = new SAPJCoConnectionManager(1) ;	            
					String user = "SESA81764";
					String password = "chateau";
					SAPJCoConnection conn = connManager.getUserConnection(m_config, user, password) ; 
					
					try{
						   SAPJCoRequestSender requestSender = new SAPJCoRequestSender(m_config) ;
						//   System.out.println(this.getProcessorConfiguration());
						   String GET_IDOC_METADATA_FROM_SAP = "<IDOCTYPE_READ_COMPLETE xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\">" +
                            "<PI_IDOCTYP>DEBMAS06</PI_IDOCTYP>" +
                            "<PI_CIMTYP>YSADEBMAS06</PI_CIMTYP>" +
                            "</IDOCTYPE_READ_COMPLETE>";
						   int request = doc.parseString(GET_IDOC_METADATA_FROM_SAP) ;
						   System.out.println("Request is:" + Node.writeToString(request, true));
						   int response = 0 ; 
							 //  requestSender.sendRFCRequest(0, conn,"" ) ;
						
						   
						   int allIDOCs = requestSender.sendRFCRequestForInternalPurpose(GET_IDOC_METADATA_FROM_SAP,
								   conn,
                                   "IDOCTYPE_READ_COMPLETE",
                                   "IDOCTYPE_READ_COMPLETE",
                                  doc);
						   Node.writeToFile(allIDOCs, allIDOCs, "D:\\Projects\\Schneider\\SAPFunctions\\DEBMAS06_RFC_Metadata2.xml", 1) ;
						   //System.out.println("Response is:" + Node.writeToString(allIDOCs, true));
						 //  return  response;
						} finally
						{
							
						}
		} catch (XMLException e) {
			
			e.printStackTrace();
		} catch (SAPConfigurationException e) {
		
			e.printStackTrace();
		} catch (Exception SAPConnectorException) {
		
			SAPConnectorException.printStackTrace();
		}
        finally
        {
            BACUtil.deleteNode(node);
        }
		
	}
	
	//@Test
	public void t1estCopyAndRunProcessRequest() {
		  int node =0;
		try {
			node = doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");
		

	     
	            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
	                                                                      "o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com",
	                                                                      "cn=SAPConnector,cn=soap nodes,o=SouthwindMDMDemoModel,cn=cordys,cn=defaultInst3,o=vanenburg.com");
	            
	            SAPJCoConnectionManager connManager = new SAPJCoConnectionManager(1) ;	            
				String user = "SESA81764";
				String password = "chateau";
				SAPJCoConnection conn = connManager.getUserConnection(m_config, user, password) ; 
				System.out.println("Got Connection"+conn);
				XBPSessionContext session = new XBPSessionContext();
				session.setSessionConnection(conn);
				session.setExternalUserId(user);
				session.setExtcompany(this.m_config.getXMICompanyName()) ;
				session.setExtProduct(this.m_config.getXMIProductName());
				XBPRequestManager xbpRequestManager = new XBPRequestManager() ;
				session.setNomDocument(doc);
				xbpRequestManager.setSession(session);
				int request = doc.parseString(COPY_RUN_JOB_REQUEST);
				int response = doc.createElement("Response");
				Node.setAttribute(response, "xmlns", "http://myresponse") ;
				int methodImplementation = 0;
				xbpRequestManager.setM_config(m_config);
				int responseNode = xbpRequestManager.processRequest(request, response, methodImplementation) ;
				System.out.println(Node.writeToString(responseNode, true));
				System.out.println(Node.writeToString(response, true));
				
				conn.disconnect();
	        
			} catch (XMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAPConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception SAPConnectorException) {
				// TODO Auto-generated catch block
				SAPConnectorException.printStackTrace();
			}
	        finally
	        {
	            BACUtil.deleteNode(node);
	        }
		//fail("Not yet implemented");
	}

	
	//@Test
	public void testParseString()
	{
		try {
			int request = doc.parseString(COPY_RUN_JOB_REQUEST);
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
