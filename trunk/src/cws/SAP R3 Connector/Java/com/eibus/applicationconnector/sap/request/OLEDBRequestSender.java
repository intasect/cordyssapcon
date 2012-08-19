/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.eibus.applicationconnector.sap.request;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cordys.coe.util.xml.NamespaceDefinitions;
import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.LDAPInterface;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;
import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;

/**
 * This class generates SOAP requests to the OLEDB processor to store and retrieve IDOCs from the
 * database.
 *
 * @author  ygopal
 */
public class OLEDBRequestSender
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(OLEDBRequestSender.class);
    /**
     * Holds the name of the Update method.
     */
    private static final String METHOD_UPDATE = "Update";
    /**
     * Holds the name of the GetTID method.
     */
    private static final String METHOD_GET_TID = "GetTid";
    /**
     * To format the date and time values from SAP.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * To format the time from SAP.
     */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    /**
     * Holds the LDAP interface to use..
     */
    private LDAPInterface m_ldapInterface;
    /**
     * The namespace mappings.
     */
    private XPathMetaInfo m_xmi;
    /**
     * The name of the table.
     */
    private String tableName;
    
    
    public static final String IDOCTABLE_DIRECTION_TAG = "DIRECTION";
    public static final String IDOCTABLE_TID_TAG = "TID";
    public static final String IDOCTABLE_IDOCNUM_TAG = "IDOCNUM";
    public static final String IDOCTABLE_CREATIONDATE_TAG = "CREATIONDATE";
    public static final String IDOCTABLE_MESTYPE_TAG = "MESTYPE";
    public static final String IDOCTABLE_IDOCTYPE_TAG = "IDOCTYPE";
    public static final String IDOCTABLE_CIMTYPE_TAG = "CIMTYPE";
    public static final String IDOCTABLE_SENDERLS_TAG = "SENDERLS";
    public static final String IDOCTABLE_RECEIVERLS_TAG = "RECEIVERLS";
    public static final String IDOCTABLE_LOCALSTATUS_TAG = "LOCALSTATUS";
    public static final String IDOCTABLE_ERRORTEXT_TAG = "ERRORTEXT";
    public static final String IDOCTABLE_TARGETSYSTEM_TAG = "TARGETSYSTEM";
    public static final String IDOCTABLE_DESTINATIONSTATUS_TAG = "DESTINATIONSTATUS";
    public static final String IDOCTABLE_SOAPNODEDN_TAG = "SOAPNODEDN";
    public static final String IDOCTABLE_CONTROLRECORD_TAG = "CONTROLRECORD";
    public static final String IDOCTABLE_DATARECORD_TAG = "DATARECORD";
    

    /**
     * Constructor.
     *
     * @param   tableName  Name of the database table in which IDOCs are stored.
     * @param   orgDN      DN of the organization where the SAP processor is running.
     *
     * @throws  SAPConnectorException  DOCUMENTME
     */
    public OLEDBRequestSender(String tableName, String orgDN)
                       throws SAPConnectorException
    {
        this.tableName = tableName;
        m_ldapInterface = new LDAPInterface(orgDN);

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("SOAP", NamespaceDefinitions.XMLNS_SOAP_1_1);
        m_xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_IDOC_DB);
    }

    /**
     * This method frmaes a tuple for the idoc to be inserted in the database.
     *
     * @param   idoc          DOCUMENTME
     * @param   idocXMLNode   DOCUMENTME
     * @param   tid           DOCUMENTME
     * @param   localStatus   DOCUMENTME
     * @param   errorText     DOCUMENTME
     * @param   targetSystem  DOCUMENTME
     * @param   soapNodeDN    DOCUMENTME
     * @param   doc           DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int frameIDOCInsertTuple(IDoc.Document idoc, int idocXMLNode, String tid,
                                    String localStatus, String errorText, String targetSystem,
                                    String soapNodeDN, Document doc)
                             throws SAPConnectorException
    {
        if (idocXMLNode == 0)
        {
            String idocXMLString = idoc.toXML();

            try
            {
                idocXMLNode = doc.parseString(idocXMLString);
            }
            catch (Exception xe)
            {
            	LOG.error("Parsing the IDOC Xml failed. Insert tuple formation failed") ;
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_PARSING_IDOC,
                                                idocXMLString);
            }
        }

        int duplicateIDOCXMLNode = Node.duplicate(idocXMLNode);
        String[] params = new String[16];
        String direction = idoc.getDirection();

        if (direction.equals("1"))
        {
            params[0] = "O";
        }
        else
        {
            params[0] = "I";
        }
        params[1] = tid;

        String idocNumber = idoc.getIDocNumber();

        // If IDOCNUM is not there, tid is made idoc number temporarily.
        if ((idocNumber == null) || idocNumber.equals(""))
        {
            idocNumber = tid;
        }
        params[2] = getSixteenCharIDOCNum(idocNumber);

        Date createdDate = idoc.getCreationDate();
        Date createdTime = idoc.getCreationTime();
        // To make it compatible with OLEDB connector.
        String creationDateTime = DATE_FORMAT.format(createdDate) + "T" +
                                  TIME_FORMAT.format(createdTime);
        params[3] = creationDateTime;
        params[4] = idoc.getMessageType(); // Message type
        params[5] = idoc.getIDocType(); // IDOC type
        params[6] = idoc.getIDocTypeExtension(); // Extension
        params[7] = idoc.getSenderPartnerNumber(); // sender LS
        params[8] = idoc.getRecipientPartnerNumber(); // receiver LS
        params[9] = localStatus;
        params[10] = errorText;
        params[11] = targetSystem;

        // It will be updated later.
        String destinationStatus = idoc.getStatus();
        params[12] = destinationStatus;
        params[13] = soapNodeDN;

        // This is either EDI_DC or EDI_DC40
        String tableStructureName = idoc.getTableStructureName();
        // System.out.println(tableStructureName + ":"+ params[5]);
        // System.out.println(tableStructureName);
        int controlRecordNode = Find.firstMatch(duplicateIDOCXMLNode,
                                                "<" + params[5] + "><IDOC><" + tableStructureName +
                                                ">");

        if (controlRecordNode == 0)
        {
        	LOG.error("Control record not available in IDOC, updation to local database is aborted") ;
            throw new SAPConnectorException(SAPConnectorExceptionMessages.CONTROL_RECORD_NOT_FOUND_FOR_IDOC,
                                            params[2]);
        }
        params[14] = Node.writeToString(controlRecordNode, false);
        Node.delete(controlRecordNode);
        // Only data record is left in the idocXMLNode.
        params[15] = Node.writeToString(duplicateIDOCXMLNode, false);
        Node.delete(duplicateIDOCXMLNode);
        return frameInsertXMLRequest(params, doc);
    }

    /**
     * This method checks if a record with the given tid is already in the database or not. It sends
     * a request to the OLEDB processor to get the tid and returns the response.
     *
     * @param   tid  DOCUMENTME
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int getTid(String tid, Document doc)
               throws SAPConnectorException
    {
        int[] params = new int[1];
        params[0] = doc.createTextElement("Tid", tid);

        int responseNode = m_ldapInterface.executeMethod(METHOD_GET_TID,
                                                         SAPConnectorConstants.NS_SAP_IDOC_DB,
                                                         params, doc, m_xmi);
        BACUtil.deleteNode(params[0]);
        return responseNode;
    }
    
    /** Returns the control structure node. The same should be available with idoc.getTableStructure, but it is getting overwritten after we do fill from xml
     * @param idocNode Idoc Node
     * @return
     */
    private int getControlStructureNode(int idocNode)
    {
    	int controlStructureNode = Node.getElement(idocNode, "EDI_DC40") ;
    	if(controlStructureNode <= 0)
    	{
    		controlStructureNode = Node.getElement(idocNode, "EDI_DC") ;
    	}    	
    	if(controlStructureNode <= 0)
    	{
    		controlStructureNode = Node.getElement(idocNode, "EDI_DC40_U") ;
    	}    	
    	return controlStructureNode ;    		
    }

    /**
     * This method saves the IDOC in the database table.
     *
     * @param   idoc          DOCUMENTME
     * @param   idocXMLNode   DOCUMENTME
     * @param   tid           DOCUMENTME
     * @param   localStatus   DOCUMENTME
     * @param   errorText     DOCUMENTME
     * @param   targetSystem  DOCUMENTME
     * @param   soapNodeDN    DOCUMENTME
     * @param   doc           DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void saveIDOCInDataBase(JCoIDoc.JCoDocument idoc, int idocXMLNode, String tid,
                                   String localStatus, String errorText, String targetSystem,
                                   String soapNodeDN, Document doc)
                            throws SAPConnectorException
    {
        if (doc == null)
        {
            doc = new Document();
        }  
        if (idocXMLNode == 0)
        {
            String idocXMLString = idoc.toXML();

            try
            {
                idocXMLNode = doc.parseString(idocXMLString);
            }
            catch (Exception xe)
            {
            	LOG.error("Parsing the IDOC XML fails") ;
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_PARSING_IDOC,
                                                idocXMLString);
            }
        }

        int duplicateIDOCXMLNode = Node.duplicate(idocXMLNode);
        String[] params = new String[16];
        String direction = idoc.getDirection();

        if (direction.equals("1"))
        {
            params[0] = "O";
        }
        else
        {
            params[0] = "I";
        }
        params[1] = tid;

        String idocNumber = idoc.getIDocNumber();

        // If IDOCNUM is not there, tid is made idoc number temporarily.
        if ((idocNumber == null) || idocNumber.equals(""))
        {
            idocNumber = tid;
        }
        params[2] = getSixteenCharIDOCNum(idocNumber);

        Date createdDate = idoc.getCreationDate();
        Date createdTime = idoc.getCreationTime();
        // To make it compatible with OLEDB connector.
        String creationDateTime ="";
        try
        {
        creationDateTime = DATE_FORMAT.format(createdDate) + "T" +
                                  TIME_FORMAT.format(createdTime);
        params[3] = creationDateTime; // This line throws exception
        }
      catch (Exception notHandlerYet) 
      {
    	  LOG.log(Severity.ERROR, "Exception while setting the date and time format. Can not update the creation time " +creationDateTime , notHandlerYet);
      }
      catch (Throwable notHandlerYet) 
      {
    	  LOG.log(Severity.ERROR, "Exception while setting the date and time format. Can not update the creation time " +creationDateTime , notHandlerYet);
      }
        params[4] = idoc.getMessageType(); // Message type
        params[5] = idoc.getIDocType(); // IDOC type
        params[6] = idoc.getIDocTypeExtension(); // Extension
        params[7] = idoc.getSenderPartnerNumber(); // sender LS
        params[8] = idoc.getRecipientPartnerNumber(); // receiver LS
        params[9] = localStatus;
        params[10] = errorText;
        params[11] = targetSystem;
        LOG.error("idoctype ="+params[5]+" cimtype"+params[6]); // delete this
        // It will be updated later.
        String destinationStatus = idoc.getStatus();
        params[12] = destinationStatus;
        params[13] = soapNodeDN;

        // This is either EDI_DC or EDI_DC40
        String tableStructureName = idoc.getTableStructureName();   
        int idocNode = Node.getElement(duplicateIDOCXMLNode, "IDOC") ;
        int controlRecordNode = Node.getElement(idocNode, tableStructureName) ;
        if(controlRecordNode <= 0 )
        {
        	controlRecordNode = getControlStructureNode(idocNode) ;
        }
        // If still!! control structure is not available , throw an exception.
        if (controlRecordNode == 0)
        {
        	LOG.error("Searching = "+tableStructureName+" Incomming IDOC Node failed" + Node.writeToString(duplicateIDOCXMLNode, true));
            throw new SAPConnectorException(SAPConnectorExceptionMessages.CONTROL_RECORD_NOT_FOUND_FOR_IDOC,
                                            params[2]);
        }
        params[14] = Node.writeToString(controlRecordNode, false);
        Node.delete(controlRecordNode);
        // Only data record is left in the idocXMLNode.
        params[15] = Node.writeToString(duplicateIDOCXMLNode, false);
        Node.delete(duplicateIDOCXMLNode);

        int[] parametersForUpdateMethod = new int[1];
        parametersForUpdateMethod[0] = frameInsertXMLRequest(params, doc);

        int responseNode = m_ldapInterface.executeMethod(METHOD_UPDATE,
                                                         SAPConnectorConstants.NS_SAP_IDOC_DB,
                                                         parametersForUpdateMethod, doc, m_xmi);
        BACUtil.deleteNode(parametersForUpdateMethod[0]);
        BACUtil.deleteNode(responseNode);
    }

    /**
     * Send the udpate request to the OLEDB processor and doesn't return the response.
     *
     * @param   params_updateMetod  DOCUMENTME
     * @param   doc                 DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void sendOleDBUpdateRequest(int[] params_updateMetod, Document doc)
                                throws SAPConnectorException
    {
        int responseNode = m_ldapInterface.executeMethod(METHOD_UPDATE,
                                                         SAPConnectorConstants.NS_SAP_IDOC_DB,
                                                         params_updateMetod, doc, m_xmi);
        BACUtil.deleteNode(responseNode);
    }

    /**
     * This method sends the idoc XML to the specified method attached to a specified SOAP node and
     * doesn't return the response.
     *
     * @param   soapNodeDN   DOCUMENTME
     * @param   methodName   DOCUMENTME
     * @param   namespace    DOCUMENTME
     * @param   idocXMLNode  DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void sendRequestToSOAPNode(String soapNodeDN, String methodName, String namespace,
                                      int idocXMLNode, Document doc)
                               throws SAPConnectorException
    {
        int responseNode = 0;
        int[] param_method = new int[1];
        param_method[0] = idocXMLNode;

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", namespace);
        xmi.addNamespaceBinding("SOAP", NamespaceDefinitions.XMLNS_SOAP_1_1);

        if ((soapNodeDN == null) || soapNodeDN.equals(""))
        {
            responseNode = m_ldapInterface.executeMethod(methodName, namespace, param_method, doc,
                                                         xmi);
        }
        else
        {
            responseNode = m_ldapInterface.executeMethod(methodName, namespace, soapNodeDN,
                                                         param_method, doc, xmi);
        }

        BACUtil.deleteNode(responseNode);
    }

    /**
     * This method updates the DestinationStatus of an IDOC with the given number in the IDOCTable.
     *
     * @param   idocNumber  DOCUMENTME
     * @param	soapNodeDN	SOAPNodeDn of the service group
     * @param   idocStatus  DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public boolean updateIDOCStatus(String idocNumber, String soapNodeDN, String idocStatus, Document doc)
                             throws SAPConnectorException
    {
        int[] param_method = new int[1];
        int tupleNode = doc.createElement("tuple");
        int oldNode = doc.createElement("old", tupleNode);
        int old_tableNode = doc.createElement(tableName, oldNode);
        String sixteenCharIDocNumber = getSixteenCharIDOCNum(idocNumber);
        /* IDOCNumber + SoapNodeDN become the key for the IDOC table This is to handle cases there Multiple SAP systems are configured in Cordys.
        as there is only one Single IDOC Table to avoid duplicate IDOC numbers this is done. It can be a rare exception scenario */
        doc.createTextElement(IDOCTABLE_IDOCNUM_TAG, sixteenCharIDocNumber, old_tableNode);
        doc.createTextElement(IDOCTABLE_SOAPNODEDN_TAG, soapNodeDN, old_tableNode);

        int newNode = doc.createElement("new", tupleNode);
        int new_tableNode = doc.createElement(tableName, newNode);
        doc.createTextElement(IDOCTABLE_IDOCNUM_TAG, sixteenCharIDocNumber, new_tableNode);
        doc.createTextElement(IDOCTABLE_DESTINATIONSTATUS_TAG, idocStatus, new_tableNode);
        param_method[0] = tupleNode;

        int responseNode = m_ldapInterface.executeMethod(METHOD_UPDATE,
                                                         SAPConnectorConstants.NS_SAP_IDOC_DB,
                                                         param_method, doc, m_xmi);
        Node.delete(tupleNode);
        Node.delete(responseNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Status of the IDOC with number: " + idocNumber + " is updated to " +
                      idocStatus + "in IDOCTable.");
        }
        return true;
    }

    /**
     * This method just frames the new tuple to be sent in the update request to store the IDOC in
     * the database. All the request parameters are passed as Strings in an array in the specific
     * order. The order shouldn't change. 0 - Direction 1 - tid 2 - idoc number 3 - creation date
     * time 4 - mes type 5 - idoc type 6 - cim type 7 - sender LS 8 - receiver LS 9 - Local status
     * 10- Error text 11- Target System 12- Destination Status 13- SOAPNode DN 14- Control Record
     * 15- Data Record
     *
     * @param   params  DOCUMENTME
     * @param   doc     DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private int frameInsertXMLRequest(String[] params, Document doc)
    {
        int tupleNode = doc.createElement("tuple");
        int newNode = doc.createElement("new", tupleNode);
        int tableNode = doc.createElement(tableName, newNode);
        doc.createTextElement(IDOCTABLE_DIRECTION_TAG, params[0], tableNode);
        doc.createTextElement(IDOCTABLE_TID_TAG, params[1], tableNode);
        doc.createTextElement(IDOCTABLE_IDOCNUM_TAG, params[2], tableNode);
        doc.createTextElement(IDOCTABLE_CREATIONDATE_TAG, params[3], tableNode);
        doc.createTextElement(IDOCTABLE_MESTYPE_TAG, params[4], tableNode);
        doc.createTextElement(IDOCTABLE_IDOCTYPE_TAG, params[5], tableNode);
        doc.createTextElement(IDOCTABLE_CIMTYPE_TAG, params[6], tableNode);
        doc.createTextElement(IDOCTABLE_SENDERLS_TAG, params[7], tableNode);
        doc.createTextElement(IDOCTABLE_RECEIVERLS_TAG, params[8], tableNode);
        doc.createTextElement(IDOCTABLE_LOCALSTATUS_TAG, params[9], tableNode);
        doc.createTextElement(IDOCTABLE_ERRORTEXT_TAG, params[10], tableNode);
        doc.createTextElement(IDOCTABLE_TARGETSYSTEM_TAG, params[11], tableNode);
        doc.createTextElement(IDOCTABLE_DESTINATIONSTATUS_TAG.toUpperCase(), params[12], tableNode);
        doc.createTextElement(IDOCTABLE_SOAPNODEDN_TAG.toUpperCase(), params[13], tableNode);
        doc.createTextElement(IDOCTABLE_CONTROLRECORD_TAG, params[14], tableNode);
        doc.createTextElement(IDOCTABLE_DATARECORD_TAG, params[15], tableNode);
        return tupleNode;
    }

    /**
     * This method just frames the new tuple to be sent in the update request to store the IDOC in
     * the database. All the request parameters are passed as Strings.
     *
     * @param   direction          DOCUMENTME
     * @param   tid                DOCUMENTME
     * @param   idocNum            DOCUMENTME
     * @param   creationDateTime   DOCUMENTME
     * @param   mesType            DOCUMENTME
     * @param   idocType           DOCUMENTME
     * @param   cimType            DOCUMENTME
     * @param   senderLS           DOCUMENTME
     * @param   receiverLS         DOCUMENTME
     * @param   localStatus        DOCUMENTME
     * @param   errorText          DOCUMENTME
     * @param   targetSystem       DOCUMENTME
     * @param   destinationStatus  DOCUMENTME
     * @param   controlRecord      DOCUMENTME
     * @param   dataRecord         DOCUMENTME
     * @param   doc                DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private int frameInsertXMLRequest1(String direction, String tid, String idocNum,
                                       String creationDateTime, String mesType, String idocType,
                                       String cimType, String senderLS, String receiverLS,
                                       String localStatus, String errorText, String targetSystem,
                                       String destinationStatus, String controlRecord,
                                       String dataRecord, Document doc)
                                throws SAPConnectorException
    {
        int tupleNode = doc.createElement("tuple");
        int newNode = doc.createElement("new", tupleNode);
        int tableNode = doc.createElement(tableName.toUpperCase(), newNode);
        doc.createTextElement("Direction", direction, tableNode);
        doc.createTextElement("Tid", tid, tableNode);
        doc.createTextElement("IDOCNum", idocNum, tableNode);
        doc.createTextElement("CreationDate", creationDateTime, tableNode);
        doc.createTextElement("MESType", mesType, tableNode);
        doc.createTextElement("IDOCType", idocType, tableNode);
        doc.createTextElement("CIMType", cimType, tableNode);
        doc.createTextElement("SenderLS", senderLS, tableNode);
        doc.createTextElement("ReceiverLS", receiverLS, tableNode);
        doc.createTextElement("LocalStatus", localStatus, tableNode);
        doc.createTextElement("ErrorText", errorText, tableNode);
        doc.createTextElement("TargetSystem", targetSystem, tableNode);
        doc.createTextElement("DestinationStatus", destinationStatus, tableNode);
        doc.createTextElement("ControlRecord", controlRecord, tableNode);
        doc.createTextElement("DataRecord", dataRecord, tableNode);
        return tupleNode;
    }

    /**
     * IDOC Number is of length 16 in SAP. If the IDOC number being stored is of length less than
     * 16, then this method appends 0's at the beginning of the string to make it a string of length
     * 16. If the length of the string is more than 16, the same string is returned.
     *
     * @param   idocNumber  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private String getSixteenCharIDOCNum(String idocNumber)
    {
        int lengthOfIDOCNum = idocNumber.length();

        if (idocNumber.length() >= 16)
        {
            return idocNumber;
        }
        else
        {
            StringBuffer temp = new StringBuffer();
            int differenceinLength = 16 - lengthOfIDOCNum;

            for (int i = 0; i < differenceinLength; i++)
            {
                temp.append("0");
            }
            return temp + idocNumber;
        }
    }
}
