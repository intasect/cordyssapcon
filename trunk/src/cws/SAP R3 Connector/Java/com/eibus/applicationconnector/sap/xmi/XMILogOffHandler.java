package com.eibus.applicationconnector.sap.xmi;

import java.io.UnsupportedEncodingException;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestSender;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;

/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XMILogOffHandler extends AbstractCORHandler4XMI {

	
	private static final String RFM_XMI_LOGOFF = "BAPI_XMI_LOGOFF";
	
	
	private static final String LOGOFF_RFC_REQUEST = 
								"<BAPI_XMI_LOGOFF xmlns=\"http://www.cordys.com/sap/xmi\">"+								
									"<INTERFACE>XBP</INTERFACE>"+								
								"</BAPI_XMI_LOGOFF>" ;
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.XMILogOffHandler.class);

	public XMILogOffHandler(XMISessionContext sessionContext) {
		super(sessionContext);
		// TODO Auto-generated constructor stub
	}
	
	

	public XMILogOffHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);
		// TODO Auto-generated constructor stub
	}



	public XMILogOffHandler() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public int processRequest(int request, int response) throws SAPConnectorException {
		Document doc = this.getNomDocument() ;
		try {
			int rfcRequest = doc.parseString(LOGOFF_RFC_REQUEST) ;			
			  return this.sendRequest(rfcRequest, RFM_XMI_LOGOFF);
//		   SAPJCoRequestSender requestSender = new SAPJCoRequestSender(this.getProcessorConfiguration()) ;
//		   int rfcResponse = requestSender.sendRFCRequest(rfcRequest, this.getSessionContext().getSessionConnection(), RFM_XMI_LOGOFF) ;
//		   if(this.getSuccessor()!= null)
//		   {
//			   return this.getSuccessor().processRequest(request, response);
//		   }
//		   return rfcResponse ;
		   		   
		} catch (XMLException ignorableException) {
			logger.log(Severity.FATAL, " XMI Logon failed - Failed while parsing Static XML",ignorableException ) ;
			
		} catch (UnsupportedEncodingException ignorableException) {
			
			logger.log(Severity.FATAL, " XMI Logon failed- Failed while parsing Static XML",ignorableException ) ;
			
		} 
		
		return 0;
	}

}
