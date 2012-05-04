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
public class XBPJobLogHandler extends AbstractCORHandler4XMI
{
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.xbp.XBPJobLogHandler.class);
	
	private static final String RF_REQ_BAPI_XBP_JOB_JOBLOG_READ = 
								"<BAPI_XBP_JOB_JOBLOG_READ xmlns=\"http://www.cordys.com/sap/xmi\">"+
									"<JOBCOUNT/>"+
									"<JOBNAME/>"+
									"<EXTERNAL_USER_NAME/>"+
								"</BAPI_XBP_JOB_JOBLOG_READ>" ;
	private static final String RFM_BAPI_XBP_JOB_JOBLOG_READ = "BAPI_XBP_JOB_JOBLOG_READ";
	
	public XBPJobLogHandler(XMISessionContext sessionContext) 
	{
		super(sessionContext);
		// TODO Auto-generated constructor stub
	}

	
	public XBPJobLogHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);
		// TODO Auto-generated constructor stub
	}


	public XBPJobLogHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public int processRequest(int request, int response) throws SAPConnectorException 
	{	
		return getJobLogs(request) ;
//		if(this.getSuccessor()!= null)
//		{
//		 return	this.getSuccessor().processRequest(request, response);
//		}
//		return 0;
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
	
	
	/**
	 * Gets the logs of an executed job.
	 * @param jobName
	 * @param jobtemplateId
	 * @param externalUserName 
	 * @return
	 * @throws SAPConnectorException
	 */
	public int  getJobLogs(String jobName, String jobtemplateId, String externalUserName) throws SAPConnectorException
	{
		Document doc = this.getNomDocument() ;
		try {
			int rfcRequest = doc.parseString(RF_REQ_BAPI_XBP_JOB_JOBLOG_READ) ;
			 Node.setDataElement(rfcRequest, TAG_JOBCOUNT, jobtemplateId);
			 Node.setDataElement(rfcRequest, TAG_JOBNAME, jobName);	
			 Node.setDataElement(rfcRequest, TAG_EXTERNAL_USER_NAME, this.getSessionContext().getExternalUserId());	
			return sendRequest(rfcRequest,RFM_BAPI_XBP_JOB_JOBLOG_READ)  ;
		} catch (XMLException ignorableException) {
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} catch (UnsupportedEncodingException ignorableException) {
			
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} 
		return 0 ;
	}
	
	
	/**Gets the logs of an executed job. Request can be mapped to BUS request 
	 * @param request 
	 * @return
	 * @throws SAPConnectorException
	 */
	public int getJobLogs(int request) throws SAPConnectorException
	{
		int jobDetailsNode = Node.getElement(request, TAG_EBS_JOB_DETAILS);
		String sourceJobName = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_NAME, "") ;
		String sourceJobTemplateId = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_ID,"") ;
		return getJobLogs(sourceJobName,sourceJobTemplateId,null) ;

	}

}
