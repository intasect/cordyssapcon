package com.eibus.applicationconnector.sap.xmi.xbp;

import java.io.UnsupportedEncodingException;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.Util;
import com.eibus.applicationconnector.sap.xmi.AbstractCORHandler4XMI;
import com.eibus.applicationconnector.sap.xmi.XMISessionContext;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;

/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XBPCopyJobHandler extends AbstractCORHandler4XMI 
{
	private static final String RFM_BAPI_XBP_JOB_COPY = "BAPI_XBP_JOB_COPY";
	private static final String RF_REQ_BAPI_XBP_JOB_COPY = 
		"<BAPI_XBP_JOB_COPY xmlns=\"http://www.cordys.com/sap/xmi\">"+
			"<SOURCE_JOBCOUNT/>"+
			"<SOURCE_JOBNAME/>"+
			"<STEP_NUMBER>"+"0"+"</STEP_NUMBER>"+
			"<TARGET_JOBNAME/>"+
			"<EXTERNAL_USER_NAME/>"+
		"</BAPI_XBP_JOB_COPY>" ;
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.xbp.XBPCopyJobHandler.class);
	
	
	public XBPCopyJobHandler(XMISessionContext sessionContext,
			ISAPConfiguration m_config, Document doc) {
		super(sessionContext, m_config, doc);
		// TODO Auto-generated constructor stub
	}
	@Override
	public	boolean validateRequest(int request) throws SAPConnectorException
	{
		/* Validate request here
		 *  throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_VALUE);
		*/
		int sourceJobDetailsNode = Node.getElement(request, TAG_EBS_SOURCE_JOB_DETAILS);
		if(sourceJobDetailsNode < 0)
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_SOURCE_JOB_DETAILS);
		}
		String sourceJobName = Node.getDataElement(sourceJobDetailsNode, TAG_EBS_JOB_NAME, "") ;
		if("".equalsIgnoreCase(sourceJobName))
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_JOB_NAME);
		}
		String sourceJobTemplateId = Node.getDataElement(sourceJobDetailsNode, TAG_EBS_JOB_ID, "") ;
		
		if("".equalsIgnoreCase(sourceJobTemplateId))
		{
			throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_EBS_SOURCE_JOB_DETAILS+"-"+TAG_EBS_JOB_ID);
		}
		return true ;
	}
	
//	boolean hasApplicationErrors(int rfcResponse)
//	{
//		return false;
//	}
	
	/**
	 * Copies and creates the job definition from an existing job template 
	 * @param sourceJobName
	 * @param sourceJobTemplateId
	 * @param targetJobName - All my testing I found the target job name has to be same as source job name. So I doubt the relevance of this.
	 * @param externalUserName - Incase the job has to be scheduled to run in a specific user context
	 * @return
	 * @throws SAPConnectorException
	 */
	public int  copyJob(String sourceJobName, String sourceJobTemplateId,String targetJobName, String externalUserName) throws SAPConnectorException
	{
		Document doc = this.getNomDocument() ;
		int rfcRequest = 0 ;
		int rfcResponse = 0 ;
		
		try{
			rfcRequest = doc.parseString(RF_REQ_BAPI_XBP_JOB_COPY) ;
			 Node.setDataElement(rfcRequest, TAG_SOURCE_JOBCOUNT, sourceJobTemplateId);
			 Node.setDataElement(rfcRequest, TAG_SOURCE_JOBNAME, sourceJobName);
			 if(Util.isSet(targetJobName))
			 {
				 Node.setDataElement(rfcRequest, TAG_TARGET_JOBNAME, sourceJobName);
			 }
			 if(Util.isSet(externalUserName))
			 {
				 Node.setDataElement(rfcRequest, TAG_EXTERNAL_USER_NAME, externalUserName);
			 }
			 else
			 {
				 Node.setDataElement(rfcRequest, TAG_EXTERNAL_USER_NAME, this.getSessionContext().getExternalUserId());
			 }
			 rfcResponse = sendRequest(rfcRequest,RFM_BAPI_XBP_JOB_COPY) ;
			return  rfcResponse;
		} catch (XMLException weirdException) {
			logger.log(Severity.FATAL, "Failed while parsing Static XML",weirdException ) ;
			
		} catch (UnsupportedEncodingException weirdException) {
			
			logger.log(Severity.FATAL, "Failed while parsing Static XML",weirdException ) ;
			
		}
		return 0 ;
	}

	public String getResponseJobId (int responseNode)
	{
		return Node.getDataElement(responseNode, TAG_TARGET_JOBCOUNT, "") ;
	}
	public int copyJob(int request) throws SAPConnectorException
	{		
		int sourceJobDetailsNode = Node.getElement(request, TAG_EBS_SOURCE_JOB_DETAILS);		
			String sourceJobName = Node.getDataElement(sourceJobDetailsNode, TAG_EBS_JOB_NAME, "") ;	
			String sourceJobTemplateId = Node.getDataElement(sourceJobDetailsNode, TAG_EBS_JOB_ID, "") ;
		int targetJobDetailsNode = Node.getElement(request, TAG_EBS_TARGET_JOB_DETAILS);
			String targetJobName = Node.getDataElement(targetJobDetailsNode, TAG_EBS_JOB_NAME, "") ;		
		return this.copyJob(sourceJobName, sourceJobTemplateId, targetJobName, null);

	}

}
