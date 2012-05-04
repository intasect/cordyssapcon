package com.eibus.applicationconnector.sap.xmi;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnection;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.xml.nom.Document;
import com.sap.mw.jco.IRepository;


/**
 * @author Vamsi Mohan Jayanti
 *
 */
public class XMISessionContext implements ICORContext 
{

	int esbRequestNode = 0;
	Document nomDocument ;
	int logInErrorResponse = 0 ;
	int logOffErrorResponse = 0 ;
	int xmlSessionId ;
	boolean hasLoggedIn = false;
	String externalUserId ;
	String extcompany;
	String extProduct;
	SAPJCoConnection stateFullConnection;
	public int getGetESBRequestNode() {
		return esbRequestNode;
	}
	public void setESBRequestNode(int getESBRequestNode) {
		this.esbRequestNode = getESBRequestNode;
	}
	public int getGetESBResponseNode() {
		return esbResponseNode;
	}
	public void setESBResponseNode(int getESBResponseNode) {
		this.esbResponseNode = getESBResponseNode;
	}
	int esbResponseNode = 0 ;
	
	public SAPJCoConnection getSessionConnection() {
		return stateFullConnection;
	}
	public void setSessionConnection(SAPJCoConnection stateFullConnection) {
		this.stateFullConnection = stateFullConnection;
	}
	public Document getNomDocument() {
		return nomDocument;
	}
	public void setNomDocument(Document nomDocument) {
		this.nomDocument = nomDocument;
	}
	
	
	public String getExternalUserId() {
		return externalUserId;
	}
	
	public String getExtcompany() {
		return extcompany;
	}
	public void setExtcompany(String extcompany) {
		this.extcompany = extcompany;
	}
	public String getExtProduct() {
		return extProduct;
	}
	public void setExtProduct(String extProduct) {
		this.extProduct = extProduct;
	}
	public void setExternalUserId(String externalUserId) {
		this.externalUserId = externalUserId;
	}
	public int getLogInErrorResponse() {
		return logInErrorResponse;
	}
	
	
	public int getLogOffErrorResponse() {
		return logOffErrorResponse;
	}
	
	/**
	 * Fire the XML Login request
	 * @param m_config
	 * @return
	 * @throws SAPConnectorException
	 */
	public boolean performLogIn(ISAPConfiguration m_config) throws SAPConnectorException
	{
		XMILogOnHandler logOnHandler = new XMILogOnHandler(this, m_config,null) ;
		int response = logOnHandler.processRequest(0, 0) ;	
		if(XMICallResponse.hasException(response))
		{
			logInErrorResponse = response ;
			return false;
		}
		else
		{
			BACUtil.deleteNode(response);
			this.hasLoggedIn = true ;
		}
	
		return this.hasLoggedIn;
	}
	
	/**Fire the XML logoff request
	 * @param m_config
	 * @return
	 * @throws SAPConnectorException
	 */
	public boolean performLogOff(ISAPConfiguration m_config) throws SAPConnectorException
	{
		XMILogOffHandler logOffHandler = new XMILogOffHandler(this, m_config,null) ;
		int response = logOffHandler.processRequest(0, 0) ;		
		if(XMICallResponse.hasException(response))
		{
			logInErrorResponse = response ;
			this.hasLoggedIn = false ;
			return false;
		}
		else
		{
			BACUtil.deleteNode(response);
			this.hasLoggedIn = false ;
		}	
		return true;
	}
	
	
	
	
	
}
