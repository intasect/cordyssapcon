package com.eibus.applicationconnector.sap.xmi;



import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestSender;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

abstract public class AbstractCORHandler4XMI implements ICORHandler {
	ICORHandler successor = null ;
	XMISessionContext sessionContext ;
	CordysLogger logger = CordysLogger.getCordysLogger(com.eibus.applicationconnector.sap.xmi.AbstractCORHandler4XMI.class) ;
	Document nomDocument ;
	
	private ISAPConfiguration m_config;
	
	public static final String METHOD_NAME_RUN_JOB = "RunJob" ;
	public static final String METHOD_NAME_COPY_AND_RUN_JOB = "CopyAndRunJob" ;
	public static final String METHOD_NAME_GET_JOBSTATUS = "GetJobstatus" ;
	public static final String METHOD_NAME_GET_JOBLOGS = "GetJoblogs";
	public static final String TAG_SOURCE_JOBCOUNT = "SOURCE_JOBCOUNT" ;
	public static final String TAG_SOURCE_JOBNAME = "SOURCE_JOBNAME" ;
	public static final String TAG_STEP_NUMBER = "STEP_NUMBER" ;
	public static final String TAG_TARGET_JOBNAME = "TARGET_JOBNAME" ;
	public static final String TAG_TARGET_JOBCOUNT = "TARGET_JOBCOUNT" ;
	public static final String TAG_JOBNAME = "JOBNAME" ;
	public static final String TAG_JOBCOUNT = "JOBCOUNT" ;
	public static final String TAG_EXTERNAL_USER_NAME = "EXTERNAL_USER_NAME" ;
	public static final String TAG_EBS_SOURCE_JOB_DETAILS = "SourceJobDetails" ;
	public static final String TAG_EBS_JOB_ID = "JobId" ;
	public static final String TAG_EBS_JOB_NAME = "JobName" ;
	public static final String TAG_EBS_JOB_STATUS = "JobStatus" ;
	public static final String TAG_EBS_TARGET_JOB_DETAILS = "TargetJobDetails" ;
	public static final String TAG_EBS_JOB_DETAILS = "JobDetails" ;
	

	public AbstractCORHandler4XMI()
	{}
	public AbstractCORHandler4XMI(XMISessionContext sessionContext)
	{
		this.sessionContext = sessionContext;
	}
	
	public AbstractCORHandler4XMI(XMISessionContext sessionContext, ISAPConfiguration m_config, Document doc)
	{
		this.sessionContext = sessionContext;
		this.m_config = m_config ;
		this.nomDocument = doc ;
	}
	
	public void setSuccessor(ICORHandler successor) {
		this.successor = successor;
	}

	@Override
	public ICORHandler getSuccessor() {
		// TODO Auto-generated method stub
		return successor;
	}

	@Override
	public int processRequest(int request, int response) throws SAPConnectorException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public Document getNomDocument() {
		if (nomDocument == null)
		{
			nomDocument = this.sessionContext.getNomDocument() ;
		}
		return nomDocument;
	}
	public void setNomDocument(Document nomDocument) {
		this.nomDocument = nomDocument;
	}
	public XMISessionContext getSessionContext() {
		return sessionContext;
	}
	public void setSessionContext(XMISessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}
	public ISAPConfiguration getProcessorConfiguration() {
		return m_config;
	}
	public void setProcessorConfiguration(ISAPConfiguration m_config) {
		this.m_config = m_config;
	}
	
	/**
	 * Note : This deletes the request node in all conditions.
	 * @param request  RFC Call request Node
	 * @param rfmName RFM Name
	 * @return Response of the call
	 * @throws SAPConnectorException
	 */
	protected int sendRequest(int request, String rfmName) throws SAPConnectorException
	{
		try{
		   SAPJCoRequestSender requestSender = new SAPJCoRequestSender(this.getProcessorConfiguration()) ;
		   if(logger.isDebugEnabled())
		   {
			   logger.debug("XMI/XBP request sent to RFCRequestSender"+ Node.writeToString(request, true)) ;
		   }
		   int response = requestSender.sendRFCRequest(request, this.getSessionContext().getSessionConnection(),rfmName ) ;
		   if(logger.isDebugEnabled())
		   {
			   logger.debug("XMI/XBP response received from RFCRequestSender"+ Node.writeToString(response, true)) ;
		   }	
		   return  response;
		} finally
		{
			BACUtil.deleteNode(request);
		}
		   

	}
	
public boolean validateRequest(int request) throws SAPConnectorException {
		
		return true;
	}
	
//	public XMICallResponse parseResponse(int rfcResponse)
//	{
//		return XMICallResponse.parseResponse(rfcResponse);
//		
//	}

}
