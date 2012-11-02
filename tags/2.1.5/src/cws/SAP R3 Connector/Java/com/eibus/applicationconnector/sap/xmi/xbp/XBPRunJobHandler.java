package com.eibus.applicationconnector.sap.xmi.xbp;

import java.io.UnsupportedEncodingException;



import com.eibus.applicationconnector.sap.config.ISAPConfiguration;

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.Util;
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
public class XBPRunJobHandler extends AbstractCORHandler4XMI{
	

	
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.xbp.XBPRunJobHandler.class);
	
	
	private static final String RFM_BAPI_XBP_JOB_START_IMMEDIATELY = "BAPI_XBP_JOB_START_IMMEDIATELY";
	


	
	private static final String RF_REQ_BAPI_XBP_JOB_START_IMMEDIATELY = 
								"<BAPI_XBP_JOB_START_IMMEDIATELY xmlns=\"http://www.cordys.com/sap/xmi\">"+
									"<JOBCOUNT/>"+
									"<JOBNAME/>"+
									"<EXTERNAL_USER_NAME/>"+
								"</BAPI_XBP_JOB_START_IMMEDIATELY>" ;
	
private static final String RFM_BAPI_XBP_JOB_START_ASAP = "BAPI_XBP_JOB_START_ASAP";
	


	
	private static final String RF_REQ_BAPI_XBP_JOB_START_ASAP = 
								"<BAPI_XBP_JOB_START_ASAP xmlns=\"http://www.cordys.com/sap/xmi\">"+
									"<JOBCOUNT/>"+
									"<JOBNAME/>"+
									"<EXTERNAL_USER_NAME/>"+
								"</BAPI_XBP_JOB_START_ASAP>" ;
	
	private static final String REQUEST_TAG_ALLOW_DEFERRED_EXECUTION = "AllowDeferredExecution" ;
	



	public XBPRunJobHandler(XMISessionContext sessionContext) 
	{
		super(sessionContext);		
	}

	
	public XBPRunJobHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);
	
	}


	public XBPRunJobHandler() {
		
	}

	@Override
	public int processRequest(int request, int response) throws SAPConnectorException 
	{	
		runJob(request) ;
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
	
	/**
	 * This method is called when the CopyAndRunJob is invoked. To be able to handle the BUS request
	 * @param newJobId
	 * @param request
	 * @param allowDefferedExecution
	 * @return
	 * @throws SAPConnectorException
	 */
	public int runFromCopiedJob(String newJobId, int request, boolean allowDefferedExecution) throws SAPConnectorException
	{
		int targetJobDetailsNode = Node.getElement(request, TAG_EBS_TARGET_JOB_DETAILS);
		String targetJobName = Node.getDataElement(targetJobDetailsNode, TAG_EBS_JOB_NAME,"");
		String jobName = "" ;
		if(Util.isSet(targetJobName))
		{
			jobName = targetJobName ;
		}
		else
		{
			int sourceJobDetailsNode = Node.getElement(request, TAG_EBS_SOURCE_JOB_DETAILS);
			String sourceJobName = Node.getDataElement(sourceJobDetailsNode, TAG_EBS_JOB_NAME,"");
			if(Util.isSet(targetJobName))
			{
				jobName = sourceJobName ;
			}			
		}
		
		return this.runJob(jobName, newJobId, null,allowDefferedExecution);
	}
	
	public int  runJob(String jobName, String jobtemplateId, String externalUserName, boolean allowDefferedExecution) throws SAPConnectorException
	{
		Document doc = this.getNomDocument() ;
		try {
			int rfcRequest = 0 ;
			if(allowDefferedExecution)
				{
					rfcRequest = doc.parseString(RF_REQ_BAPI_XBP_JOB_START_ASAP) ; 
				}
			else			
				{
					rfcRequest =	doc.parseString(RF_REQ_BAPI_XBP_JOB_START_IMMEDIATELY) ;
				}
			 Node.setDataElement(rfcRequest, TAG_JOBCOUNT, jobtemplateId);
			 Node.setDataElement(rfcRequest, TAG_JOBNAME, jobName);	
			 Node.setDataElement(rfcRequest, TAG_EXTERNAL_USER_NAME, this.getSessionContext().getExternalUserId());	
			 if(allowDefferedExecution)
				{
				 	return sendRequest(rfcRequest,RFM_BAPI_XBP_JOB_START_ASAP)  ;
				}
			 else
				{
				 	return sendRequest(rfcRequest,RFM_BAPI_XBP_JOB_START_IMMEDIATELY)  ;
				}
		} catch (XMLException ignorableException) {
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} catch (UnsupportedEncodingException ignorableException) {
			
			logger.log(Severity.FATAL, "Failed while parsing Static XML",ignorableException ) ;
			ignorableException.printStackTrace();
		} 
		return 0 ;
	}
	
	
	
	public int runJob(int request) throws SAPConnectorException
	{
		int jobDetailsNode = Node.getElement(request, TAG_EBS_JOB_DETAILS);
		String sourceJobName = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_NAME, "") ;
		String sourceJobTemplateId = Node.getDataElement(jobDetailsNode, TAG_EBS_JOB_ID,"") ;
		return runJob(sourceJobName,sourceJobTemplateId,null, XBPRunJobHandler.isDeferredExecutionAllowed(request)) ;

	}
	
	/** If differed execution is allowed the job gets queued for execution using BAPI_XBP_JOB_START_ASAP api
	 * @param request
	 * @return
	 */
	public static boolean isDeferredExecutionAllowed (int request)
	{
		String deferredExecutionInsValue = Node.getDataElement(request, REQUEST_TAG_ALLOW_DEFERRED_EXECUTION, "false") ;
		if("true".equalsIgnoreCase(deferredExecutionInsValue))
		{
			return true;
		}
		return false ;
	}

}
