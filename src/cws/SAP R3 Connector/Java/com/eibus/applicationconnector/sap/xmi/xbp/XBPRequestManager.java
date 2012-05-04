package com.eibus.applicationconnector.sap.xmi.xbp;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.xmi.AbstractCORHandler4XMI;
import com.eibus.applicationconnector.sap.xmi.XMICallResponse;
import com.eibus.xml.nom.Node;

/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XBPRequestManager 
{
	XBPSessionContext session ;
	 private ISAPConfiguration m_config;
	public XBPSessionContext getSession() {
		if(session == null)
			session = new XBPSessionContext() ;
		return session;
	}
	public void setSession(XBPSessionContext session) {
		this.session = session;
	}
	
	public ISAPConfiguration getM_config() {
		return m_config;
	}
	public void setM_config(ISAPConfiguration m_config) {
		this.m_config = m_config;
	}
	
	/**
	 * This method is the routing hop for all XBP calls
	 * Does the following in the order:
	 * 1. Based on the method name it picks the correct handler.
	 * 2. Validates the request.
	 * 3. Does XMI login
	 * 4. Does request execution.
	 * 5. Does XMI Logoff
	 * @param request
	 * @param response
	 * @param methodImplementation
	 * @return
	 * @throws SAPConnectorException
	 */
	public int  processRequest(int request, int response, int methodImplementation) throws SAPConnectorException
	{
		String methodName = Node.getLocalName(request);	
		if(AbstractCORHandler4XMI.METHOD_NAME_RUN_JOB.equalsIgnoreCase(methodName))
		{
			XBPRunJobHandler jobHandler = new XBPRunJobHandler(this.getSession(), this.getM_config(),null) ;
			if(jobHandler.validateRequest(request))
			{
				if(!this.session.performLogIn(getM_config()))
				{
					return this.session.getGetESBResponseNode() ;
				}
				int runJobResponse = jobHandler.runJob(request);
				this.session.performLogOff(getM_config());				
				return runJobResponse ;
				
			}
		}else if(AbstractCORHandler4XMI.METHOD_NAME_COPY_AND_RUN_JOB.equalsIgnoreCase(methodName))
		{
			
			
			XBPCopyJobHandler copyJobHandler = new XBPCopyJobHandler(this.getSession(), this.getM_config(),null) ;
			
			if(copyJobHandler.validateRequest(request) )
			{
				if(!this.session.performLogIn(getM_config()))
				{
					return this.session.getGetESBResponseNode() ;
				}
				
				int copyJobResponse = copyJobHandler.copyJob(request);			
				if(XMICallResponse.hasException(copyJobResponse))
				{
					this.session.performLogOff(getM_config());
					return copyJobResponse ;
				}				
				String newJobId = copyJobHandler.getResponseJobId(copyJobResponse);
				BACUtil.deleteNode(copyJobResponse);
				XBPRunJobHandler runJobHandler = new XBPRunJobHandler(this.getSession(), this.getM_config(),null) ;
				
				int runJobResponse = runJobHandler.runFromCopiedJob(newJobId, request,XBPRunJobHandler.isDeferredExecutionAllowed(request)) ;
				Node.setDataElement(response,"JOBINSTANCEID", newJobId);
				
				this.session.performLogOff(getM_config());				
				return runJobResponse ;
				
			}
			
		}else if(AbstractCORHandler4XMI.METHOD_NAME_GET_JOBSTATUS.equalsIgnoreCase(methodName))
		{
			XBPJobStatusHandler jobHandler = new XBPJobStatusHandler(this.getSession(), this.getM_config(),null) ;
			if(jobHandler.validateRequest(request))
			{
				if(!this.session.performLogIn(getM_config()))
				{
					return this.session.getGetESBResponseNode() ;
				}
				int runJobResponse = jobHandler.getJobStatus(request);
				this.session.performLogOff(getM_config());				
				return runJobResponse ;
			}
			
		}else if(AbstractCORHandler4XMI.METHOD_NAME_GET_JOBLOGS.equalsIgnoreCase(methodName))
		{
			XBPJobLogHandler jobHandler = new XBPJobLogHandler(this.getSession(), this.getM_config(),null) ;
			if(jobHandler.validateRequest(request))
			{
				if(!this.session.performLogIn(getM_config()))
				{
					return this.session.getGetESBResponseNode() ;
				}
				int runJobResponse = jobHandler.getJobLogs(request);
				this.session.performLogOff(getM_config());				
				return runJobResponse ;
			}
			
		}
			
		/*Not handling this case**/
		return 0 ;
				
//		XBPSessionContext newSession = this.getSession();
//		newSession.setESBRequestNode(request) ;
//		newSession.setESBResponseNode(response);
//		// Also set the connection here
//		XMILogOnHandler logOnHandler = new XMILogOnHandler(newSession, this.getM_config(),null) ;
//		XBPRunJobHandler jobHandler = new XBPRunJobHandler(newSession, this.getM_config(),null) ;
//		XMILogOffHandler logOffHandler = new XMILogOffHandler(newSession, this.getM_config(),null) ;
//		logOnHandler.setSuccessor(jobHandler);
//		jobHandler.setSuccessor(logOffHandler);
//		return logOnHandler.processRequest(request, response);		
	
	}	

}
