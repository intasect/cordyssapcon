package com.eibus.applicationconnector.sap.xmi;


import java.io.UnsupportedEncodingException;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestSender;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.JCO;

/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XMILogOnHandler extends AbstractCORHandler4XMI 
{
	private static final String RFM_XMI_LOGIN = "BAPI_XMI_LOGON";
	private static final String TAG_EXTCOMPANY = "EXTCOMPANY" ;
	private static final String TAG_EXTPRODUCT = "EXTPRODUCT" ;
	private static final String TAG_INTERFACE = "INTERFACE" ;
	private static final String TAG_VERSION = "VERSION" ;
	
	private static final String LOGON_RFC_REQUEST = 
								"<BAPI_XMI_LOGON xmlns=\"http://www.cordys.com/sap/xmi\">"+
									"<EXTCOMPANY>CORDYS</EXTCOMPANY>"+
									"<EXTPRODUCT>CORDYS-SCH</EXTPRODUCT>"+
									"<INTERFACE>XBP</INTERFACE>"+
									"<VERSION>2.0</VERSION>"+
								"</BAPI_XMI_LOGON>" ;
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.XMILogOnHandler.class);

	public XMILogOnHandler(XMISessionContext sessionContext) {
		super(sessionContext);
	
	}
	

	public XMILogOnHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);		
	}


	public XMILogOnHandler() {
		
	}

	@Override
	public int processRequest(int request, int response) throws SAPConnectorException 
	{
		Document doc = this.getNomDocument() ;
		
		try {
			int rfcRequest = doc.parseString(LOGON_RFC_REQUEST) ;
			
		   Node.setDataElement(rfcRequest, TAG_EXTCOMPANY, this.getSessionContext().getExtcompany());
		   Node.setDataElement(rfcRequest, TAG_EXTPRODUCT, this.getSessionContext().getExtProduct());	
		   return this.sendRequest(rfcRequest, RFM_XMI_LOGIN);
		 
		   		   
		} catch (XMLException ignorableException) {
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} catch (UnsupportedEncodingException ignorableException) {
			
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		}
		
		return 0;
	}
	
	public boolean isLogOnSuccessful(int response)
	{
		return true ;
	}	

}
