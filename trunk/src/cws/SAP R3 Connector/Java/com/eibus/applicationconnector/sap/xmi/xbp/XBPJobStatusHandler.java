package com.eibus.applicationconnector.sap.xmi.xbp;

import java.io.UnsupportedEncodingException;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.xmi.AbstractCORHandler4XMI;
import com.eibus.applicationconnector.sap.xmi.XMISessionContext;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XBPJobStatusHandler extends AbstractCORHandler4XMI 
{
	private static final String RF_REQ_BAPI_XBP_JOB_STATUS_GET = 
		"<BAPI_XBP_JOB_STATUS_GET xmlns=\"http://www.cordys.com/sap/xmi\">"+
			"<JOBCOUNT/>"+
			"<JOBNAME/>"+
			"<EXTERNAL_USER_NAME/>"+
		"</BAPI_XBP_JOB_STATUS_GET>" ;
	private static final String RFM_BAPI_XBP_JOB_STATUS_GET = "BAPI_XBP_JOB_STATUS_GET";
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.xbp.XBPJobStatusHandler.class);
	
	
	public XBPJobStatusHandler(XMISessionContext sessionContext) 
	{
		super(sessionContext);
		// TODO Auto-generated constructor stub
	}

	
	public XBPJobStatusHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);
		// TODO Auto-generated constructor stub
	}


	public XBPJobStatusHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int processRequest(int request, int response) throws SAPConnectorException 
	{	
		//runJob(request) ;
		if(this.getSuccessor()!= null)
		{
		 return	this.getSuccessor().processRequest(request, response);
		}
		return 0;
	}
	
	public boolean validateRequest(int request) throws SAPConnectorException
	{
		/* Validate request here
		 *  throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_VALUE);
		*/
		int jobDetailsNode = Node.getElement(request, TAG_EBS_JOB_DETAILS);
		if(jobDetailsNode < 0)
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_SOURCE_JOB_DETAILS);
		}
		String sourceJobName = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_NAME, "") ;
		if("".equalsIgnoreCase(sourceJobName))
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_JOB_NAME);
		}
		String sourceJobTemplateId = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_ID, "") ;
		
		if("".equalsIgnoreCase(sourceJobTemplateId))
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_SOURCE_JOB_DETAILS+"-"+TAG_EBS_JOB_ID);
		}

		return true ;
	}
	
	public int  getJobStatus(String jobName, String jobtemplateId, String externalUserName) throws SAPConnectorException
	{
		Document doc = this.getNomDocument() ;
		try {
			int rfcRequest = doc.parseString(RF_REQ_BAPI_XBP_JOB_STATUS_GET) ;
			 Node.setDataElement(rfcRequest, TAG_JOBCOUNT, jobtemplateId);
			 Node.setDataElement(rfcRequest, TAG_JOBNAME, jobName);	
			 Node.setDataElement(rfcRequest, TAG_EXTERNAL_USER_NAME, this.getSessionContext().getExternalUserId());	
			return sendRequest(rfcRequest,RFM_BAPI_XBP_JOB_STATUS_GET)  ;
		} catch (XMLException ignorableException) {
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} catch (UnsupportedEncodingException ignorableException) {
			
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} 
		return 0 ;
	}
	
	public int getJobStatus(int request) throws SAPConnectorException
	{
		int jobDetailsNode = Node.getElement(request, TAG_EBS_JOB_DETAILS);
		String sourceJobName = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_NAME, "") ;
		String sourceJobTemplateId = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_ID,"") ;
		return getJobStatus(sourceJobName,sourceJobTemplateId,null) ;

	}

}
