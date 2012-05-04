package com.eibus.applicationconnector.sap.xmi;

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

/**
 * COR - Chain of responsibility 
 * This represents a link in the chain of responsibilities
 * @author Vamsi Mohan Jayanti
 *
 */
public interface ICORHandler 
{
	/**To get the successor for responsibility
	 * @return
	 */
	ICORHandler getSuccessor();	
	/**
	 * @param request -can be mapped to bus request
	 * @param response - can be mapped to bus response
	 * @return
	 * @throws SAPConnectorException
	 */
	int processRequest(int request, int response) throws SAPConnectorException;
	/**
	 * Implement this method to validate the request parameters. Places where XMLLogin if needed , this should be called first
	 * @param request -- can be mapped to bus request
	 * @return
	 * @throws SAPConnectorException
	 */
	boolean validateRequest(int request) throws SAPConnectorException;		

}
