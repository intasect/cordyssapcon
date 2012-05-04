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
package com.eibus.applicationconnector.sap.connection.jco;

import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.applicationconnector.sap.Messages;
import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.request.OLEDBRequestSender;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;
import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

/**
 * This class provides the functionality to send BAPI/RFC requests to SAP backend and retrieve the
 * corresponding responses from SAP backend. Rather than setting each field of an RFC parameters, it
 * makes used of fromXML() and toXML() methods of JCO API. It follows Request/Response model rather
 * than using parameter lists.
 *
 * @author  : ygopal
 */
public class SAPJCoRequestSender
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPJCoRequestSender.class);
    /**
     * For inbound IDOCs, target system is always SAP.
     */
    private static String IDOCTargetSystem = "SAP"; 
    /**
     * DOCUMENTME.
     */
    private static String RFCREADTABLE_reqeustXML = new String("<RFC_READ_TABLE><QUERY_TABLE></QUERY_TABLE><ROWCOUNT></ROWCOUNT><OPTIONS><item><TEXT/></item></OPTIONS><FIELDS><item><FIELDNAME></FIELDNAME><OFFSET>000000</OFFSET><LENGTH>000000</LENGTH><TYPE/><FIELDTEXT/></item></FIELDS></RFC_READ_TABLE>");
    
    private static String EDIDS_RFCREADTABLE_reqeustXML = new String("<RFC_READ_TABLE><DELIMITER>:</DELIMITER><QUERY_TABLE></QUERY_TABLE><ROWCOUNT></ROWCOUNT><OPTIONS><item><TEXT/></item></OPTIONS><FIELDS>" +
    		"<item><FIELDNAME></FIELDNAME><OFFSET>000000</OFFSET><LENGTH>000000</LENGTH><TYPE/><FIELDTEXT/></item>" +
    		"<item><FIELDNAME>STATXT</FIELDNAME></item>" +
    		"<item><FIELDNAME>STAPA1</FIELDNAME></item>" +
    		"<item><FIELDNAME>STAPA2</FIELDNAME></item>" +
    		"<item><FIELDNAME>STAPA3</FIELDNAME></item>" +
    		"<item><FIELDNAME>STAPA4</FIELDNAME></item>" +
    		"</FIELDS>" +
    		"</RFC_READ_TABLE>");
    /**
     * DOCUMENTME.
     */
    private static String WDLDEDISTATUSSET_requestXML = new String("<WDLD_EDI_STATUS_SET><PI_STATUS/><PI_T_WDLSP><item><DOCNUM/></item></PI_T_WDLSP></WDLD_EDI_STATUS_SET>");
    /**
     * DOCUMENTME.
     */
    private static String controlTableName = "EDIDC";
    /**
     * DOCUMENTME.
     */
    private static String statusTableName = "EDIDS";

    /**
     * Holds the configuration of the connector.
     */
    private ISAPConfiguration m_config;
    /**
     * DOCUMENTME.
     */
    private OLEDBRequestSender oleDBRequestSender;
    /**
     * Namespace to be added to bring the response in sync with that of BC.
     */
    private String rfcNameSpace = "urn:sap-com:document:sap:rfc:functions";
    /**
     * DOCUMENTME.
     */
    private String rfcNameSpaceAttribute = "xmlns:rfc";

    /**
     * Constructor that takes JCo.Repository and JCoIDoc.SAPRepository as parameters. They are used
     * for sending RFC requests and IDOC requests respectively.
     *
     * @param  configuration  The configuration of the connector.
     */
    public SAPJCoRequestSender(ISAPConfiguration configuration)
    {
        m_config = configuration;
    }

    /**
     * This method sends an IDOC request to the SAP server. *
     *
     * @param   requestNode        :XML request of the IDOC
     * @param   responseNode       :XML response of the IDOC request
     * @param   client             : Client for the current user
     * @param   idocType           : IDOC type
     * @param   mesType            : Message type
     * @param   cimType            : Extension of the IDOC type.
     * @param   fromLogicalSystem  DOCUMENTME
     * @param   toLogicalSystem    DOCUMENTME
     *
     * @return  : Returns the transaction id as a String and appends the transaction id in <tid/> and idoc number in <IDOCNum/> node under 
     * responseNode
     *
     * @throws  SAPConnectorException
     */
    public String sendIDOCRequest(int requestNode,int responseNode, JCO.Client client, String idocType, String mesType,
                               String cimType, String fromLogicalSystem, String toLogicalSystem)
                        throws SAPConnectorException
    {
        if (oleDBRequestSender == null)
        {
            oleDBRequestSender = new OLEDBRequestSender(SAPConnectorConstants.IDOC_TABLE_NAME,
                                                        m_config.getOrganization());
        }

        Document doc = Node.getDocument(requestNode);
        // IDOC type is the name of the parent node.
        // String idocType = Node.getLocalName(requestNode);
        boolean isPartnerUnicode = false ;
        if(client instanceof SAPJCoConnection)
        { //This method is made available in the extention class for JCOClient 
        	isPartnerUnicode = ((SAPJCoConnection) client).isPartnerSystemUnicode();
        }
        
        JCoIDoc.JCoDocument idoc = createIDOC(m_config.getIDOCRepository(), idocType, cimType,
                                              requestNode,isPartnerUnicode);      
        // Setting appropriate control data
        try
        {
            idoc.checkSyntax();
        }
        catch (IDoc.SyntaxException se)
        {
            throw new SAPConnectorException(se,
                                            SAPConnectorExceptionMessages.ERROR_CHECKING_IDOC_SYNTAX,
                                            se.getFieldName()) ;
                                         //   se.getSegment().getSegmentMetaData().getType());
        }

        String transactionID = null;

        // Creating a transaction id for this IDOC
        try
        {
            transactionID = client.createTID();
        }
        catch (JCO.Exception je)
        {
            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERROR_CREATING_TRANSACTION_ID);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Transaction ID is " + transactionID);
        }    
        // set tid in the field RCVLAD to get the IDOC number generated.
        idoc.setRecipientLogicalAddress(transactionID);

        String localStatus;
        String idocNum;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("IDOC going to SAP is:" + idoc.toXML());
        }

        // Sending the IDOC to the server
        try
        {
            client.send(idoc, transactionID);         
        }
        catch (JCO.Exception je)
        {
            String exceptionMessage = "An exception occured while sending the IDOC to the SAP server, " +
                                      je.getMessage();
            localStatus = "Error while dispatching.";
           
                LOG.error(exceptionMessage);
           
            oleDBRequestSender.saveIDOCInDataBase(idoc, requestNode, transactionID, localStatus,
                                                  exceptionMessage, IDOCTargetSystem,
                                                  m_config.getServiceGroup(), doc);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC saved in the database.");
            }

            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERROR_DISPATCHING_IDOC);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_HANDLING_IDOC_REQUEST);
        }

        // Confirming if the transaction is successful
        try
        {
            client.confirmTID(transactionID);
        }
        catch (JCO.Exception je)
        {
            String exceptionMessage = " An exception occured while confirming the transaction ID, " +
                                      je.getMessage();
            // localStatus = "Error while confirming the transaction." ;
            localStatus = "Error";
            idocNum = getIDOCNumberFromSAP(transactionID, client, doc);
            idoc.setIDocNumber(idocNum);
            oleDBRequestSender.saveIDOCInDataBase(idoc, requestNode, transactionID, localStatus,
                                                  exceptionMessage, IDOCTargetSystem,
                                                  m_config.getServiceGroup(), doc);

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" IDOC saved in the database.");
            }
            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERROR_DISPATCHING_IDOC);
        }
        idocNum = getIDOCNumberFromSAP(transactionID, client, doc);
        idoc.setIDocNumber(idocNum);
        
        localStatus = "Dispatched";
        // Store the IDOC in the database.
        oleDBRequestSender.saveIDOCInDataBase(idoc, requestNode, transactionID, localStatus, "",
                                              IDOCTargetSystem, m_config.getServiceGroup(), doc);

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" IDOC saved in the database.");
        }
       
        Node.createTextElement("tid", transactionID, responseNode);
        Node.createTextElement("IDOCNum", idocNum, responseNode);
        return transactionID;
    }

    /**
     * This method send the request to SAP backend and return the response thus it got form SAP
     * backend.It parses the incoming RFC request and appropriately set the import and table
     * parameters required by correspanding RFC and retrieve the response from export and table
     * parameters.
     *
     * @param   requestNode  : Request for BAPI or RFC
     * @param   client       clint object using which we connect to SAP backend
     * @param   rfcName      RFM name associated with BAPI. <methodName> <feild1/> <feild2/>
     *                       <feild3/> <struct1 isActive="false"> <f1/> <f2/> </struct1> <tb1
     *                       isActive="false"> <item> <f1/> <f2/> </item> <item> <f1/> <f2/> </item>
     *                       </tb1> </methodName> Table parameters can repeat. One node represent
     *                       one table row.
     *
     *                       <p>Response will also be in the same format, with multiple levels of
     *                       XML node.</p>
     *
     * @return  The response XML from SAP.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int sendRFCRequest(int requestNode, JCO.Client client, String rfcName)
                       throws SAPConnectorException
    {
        if (requestNode == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.REQUEST_NODE_IS_NOT_FILLED);
        }

        String methodTagName = Node.getName(requestNode);
        // rfcName is the name of the parent node
        // String rfcName = Node.getLocalName(requestNode);
        Document doc = Node.getDocument(requestNode);
        // To bring it in sync with BC response
        String responseNodeName = methodTagName + ".Response";

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating JCO.Request object.");
        }

        JCO.Request sapRequest = createRequest(m_config.getRepository(), rfcName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Setting Request parameters. Request XML:\n" +
                      Node.writeToString(requestNode, false));
        }

        try
        {
            // Inactivate the structures and tables whose isActive attribute is false.
            // If a field is not present
            int noOfFields = sapRequest.getNumFields();

            for (int i = 0; i < noOfFields; i++)
            {
                JCO.Field oneField = sapRequest.getField(i);

                if (oneField.isStructure() || oneField.isTable())
                {
                    boolean isActive = XPathHelper.getBooleanValue(requestNode,
                                                                   oneField.getName() +
                                                                   "/@isActive",
                                                                   new XPathMetaInfo(), true);

                    if (!isActive)
                    {
                        sapRequest.setActive(false, i);

                        // Remove the parameter from the request
                        int node = XPathHelper.selectSingleNode(requestNode, oneField.getName());

                        if (node != 0)
                        {
                            Node.unlink(node);
                            node = BACUtil.deleteNode(node);
                        }
                    }
                }
            }

            String requestString = Node.writeToString(requestNode, false);
            sapRequest.fromXML(requestString);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Request going to SAP " + sapRequest.toXML());
            }
        }
        catch (JCO.ConversionException jce)
        {
            LOG.warn(jce, Messages.WRN_CONVERT);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_HANDLING_RFC_REQUEST);
        }

        JCO.Response sapResponse = executeFunction(client, sapRequest);
        // Not checking the RETURN parameter. Returning the response as it is.
        int responseNode = convertSAPResponseToXML(sapResponse, doc, false);

        // Removing RETURN parameter from the response
        Node.setName(responseNode, responseNodeName);
        Node.setAttribute(responseNode, rfcNameSpaceAttribute, rfcNameSpace);
        return responseNode;
    }

    /**
     * This method sends and RFC request and checks the RETURN parameter also. If there is an error,
     * it throws a SOAP Fault. The methodTagName parameter is baiscally used to get the response in
     * sync with BC response. Only used for loading metadata into cache.
     *
     * @param   requestNode    DOCUMENTME
     * @param   client         DOCUMENTME
     * @param   rfcName        DOCUMENTME
     * @param   methodTagName  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int sendRFCRequestForInternalPurpose(int requestNode, JCO.Client client, String rfcName,
                                                String methodTagName, Document doc)
                                         throws SAPConnectorException
    {
        if (requestNode == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.REQUEST_NODE_IS_NOT_FILLED);
        }

        if (methodTagName == null)
        {
            methodTagName = Node.getName(requestNode);
        }

        String requestString = Node.writeToString(requestNode, false);
        return sendRFCRequestForInternalPurpose(requestString, client, rfcName, methodTagName, doc);
    }

    /**
     * This method sends and RFC request and checks the RETURN parameter also. If there is an error,
     * it throws a SOAP Fault. The methodTagName parameter is baiscally used to get the response in
     * sync with BC response. Only used for loading metadata into cache.
     *
     * @param   requestString  DOCUMENTME
     * @param   client         DOCUMENTME
     * @param   rfcName        DOCUMENTME
     * @param   methodTagName  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int sendRFCRequestForInternalPurpose(String requestString, JCO.Client client,
                                                String rfcName, String methodTagName,
                                                Document doc)
                                         throws SAPConnectorException
    {
        // To bring it in sync with BC response
        String responseNodeName = methodTagName + ".Response";
        // String requestString = Node.writeToString(requestNode,false);
        StringBuffer errorMessage = new StringBuffer();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating JCO.Request object.");
        }

        JCO.Request sapRequest = createRequest(m_config.getRepository(), rfcName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Setting Request parameters.");
        }

        try
        {
            sapRequest.fromXML(requestString);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Request going to SAP " + sapRequest.toXML());
            }
        }
        catch (JCO.ConversionException jce)
        {
            LOG.warn(jce, Messages.WRN_CONVERT);
        }
        catch (Exception rte)
        {
            throw new SAPConnectorException(rte,
                                            SAPConnectorExceptionMessages.ERROR_TRANSFORMING_REQUEST_TO_XML,
                                            requestString);
        }

        JCO.Response sapResponse;

        long start = System.currentTimeMillis();

        try
        {
            try
            {
                sapResponse = client.execute(sapRequest);
            }
            catch (Exception ex)
            {
                throw new SAPConnectorException(ex,
                                                SAPConnectorExceptionMessages.ERROR_EXECUTING_REQUEST,
                                                sapRequest.toXML());
            }

            // Not checking the RETURN parameter. Returning the response as it is.
            if (isFunctionCallSuccessful(sapResponse, errorMessage))
            {
                int responseNode = convertSAPResponseToXML(sapResponse, doc, true);
                // Removing RETURN parameter from the response
                BACUtil.deleteNode(Find.firstMatch(responseNode,
                                                   "<" + Node.getName(responseNode) + "><RETURN>"));
                // To set the name to <methodName>.Response
                Node.setName(responseNode, responseNodeName);
                Node.setAttribute(responseNode, rfcNameSpaceAttribute, rfcNameSpace);
                return responseNode;
            }
            else
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_EXECUTING_FUNCTION_CALL,
                                                errorMessage);
            }
        }
        finally
        {
            long duration = System.currentTimeMillis() - start;

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Request took " + duration + " miliseconds.");
            }
        }
    }

    /**
     * This method is only to send RFC requests to get metadata into the cache directory. RFC
     * metadata being huge is giving memory problems while converting to XML node. This method
     * directly writes to the cache directory avoiding overheads.
     *
     * @param   requestString  DOCUMENTME
     * @param   client         DOCUMENTME
     * @param   rfcName        DOCUMENTME
     * @param   cacheFileName  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    int sendRFCRequestForMetadata(String requestString, JCO.Client client, String rfcName,
                                  String cacheFileName, Document doc)
                           throws SAPConnectorException
    {
        if ((requestString == null) || "".equals(requestString))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.REQUEST_NODE_IS_NOT_FILLED);
        }

        StringBuffer errorMessage = new StringBuffer();

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" Creating JCO.Request object.");
        }

        JCO.Request sapRequest = createRequest(m_config.getRepository(), rfcName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" Setting Request parameters.");
        }

        try
        {
            sapRequest.fromXML(requestString);
        }
        catch (JCO.Exception je)
        {
            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERROR_TRANSFORMING_REQUEST_TO_XML,
                                            requestString);
        }

        JCO.Response sapResponse = executeFunction(client, sapRequest);

        if (isFunctionCallSuccessful(sapResponse, errorMessage))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Converting the response into XML.");
            }

            try
            {
                // Metadata can be huge. It gives a problem if it is converted to bytes and loaded
                // into a document. So, write it to file and load it to document from file.
                // sapResponse.writeXML(cacheFileName);
                // return doc.load(cacheFileName);
                // int responseNode = convertSAPResponseToXML(sapResponse, doc);
                String resXML = sapResponse.toXML();
                int responseNode = 0;

                try
                {
                    responseNode = doc.load(resXML.getBytes("UTF8"));
                }
                catch (Exception e)
                {
                    LOG.warn(e, Messages.ERROR_LOADING_XML, resXML);
                }
                // Removing RETURN parameter from the response
                BACUtil.deleteNode(Find.firstMatch(responseNode,
                                                   "<" + Node.getName(responseNode) + "><RETURN>"));
                return responseNode;
            }
            catch (Exception jce)
            {
                throw new SAPConnectorException(jce,
                                                SAPConnectorExceptionMessages.ERROR_TRANSFORMING_RESPONSE_TO_XML);
            }
        }
        else
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_EXECUTING_FUNCTION_CALL,
                                            errorMessage);
        }
    }

    /**
     * Overloaded version of the previous method where request is sent as a node.
     *
     * @param   requestNode    DOCUMENTME
     * @param   client         DOCUMENTME
     * @param   rfcName        DOCUMENTME
     * @param   cacheFileName  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    int sendRFCRequestForMetadata(int requestNode, JCO.Client client, String rfcName,
                                  String cacheFileName, Document doc)
                           throws SAPConnectorException
    {
        if (requestNode == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.REQUEST_NODE_IS_NOT_FILLED);
        }

        String requestString = Node.writeToString(requestNode, false);
        return sendRFCRequestForMetadata(requestString, client, rfcName, cacheFileName, doc);
    }

    /**
     * This method is called to get the latest status of an IDOC from SAP database and update it to
     * the IDOCTable.
     *
     * @param   idocNumber  DOCUMENTME
     * @param   client      DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int synchronizeIDOCStatus(String idocNumber, JCO.Client client, Document doc)
                           throws SAPConnectorException
    {
        int idocStatusNode = getIDOCStatusFromSAP(idocNumber, client, doc);
        if(idocStatusNode > 0)
        {
        	updateIDOCStatus(idocNumber, Node.getDataElement(idocStatusNode, "STATUS", ""), doc);
        }
       
        return idocStatusNode ; 
    }

    /**
     * This method updates the status of an IDOC with the given number in the IDOCTable. This method
     * is called by target systems to update the status of IDOCs after they are processed.
     *
     * @param   idocNumber  DOCUMENTME
     * @param   idocStatus  DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean updateIDOCStatus(String idocNumber, String idocStatus, Document doc)
                      throws SAPConnectorException
    {
        if (oleDBRequestSender == null)
        {
            oleDBRequestSender = new OLEDBRequestSender(SAPConnectorConstants.IDOC_TABLE_NAME,
                                                        m_config.getOrganization());
        }
        return oleDBRequestSender.updateIDOCStatus(idocNumber,m_config.getServiceGroup(), idocStatus, doc);
    }

    /**
     * This method updates the status of the idoc with the given number to the given status. It uses
     * the RFM WDLD_EDI_STATUS_SET to update the IDOC status.
     *
     * @param   idocNumber  DOCUMENTME
     * @param   idocStatus  DOCUMENTME
     * @param   client      DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void updateIDOCStatusinSAP(String idocNumber, String idocStatus, JCO.Client client,
                               Document doc)
                        throws SAPConnectorException
    {
        int requestNode = 0;

        try
        {
            requestNode = doc.parseString(WDLDEDISTATUSSET_requestXML);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_PARSING_TEMPLATE,
                                            "WDLDEDISTATUSSET_requestXML");
        }

        int statusNode = Find.firstMatch(requestNode, "<WDLD_EDI_STATUS_SET><PI_STATUS>");
        doc.createText(idocStatus, statusNode);

        int idocNumberNode = Find.firstMatch(requestNode,
                                             "<WDLD_EDI_STATUS_SET><PI_T_WDLSP><item><DOCNUM>");
        doc.createText(idocNumber, idocNumberNode);

        int responseNode = 0;

        try
        {
            responseNode = sendRFCRequestForInternalPurpose(requestNode, client,
                                                            "WDLD_EDI_STATUS_SET", null, doc);
        }
        catch (SAPConnectorException sf)
        {
            BACUtil.deleteNode(requestNode);
            BACUtil.deleteNode(responseNode);
            throw sf;
        }
        BACUtil.deleteNode(requestNode);
        BACUtil.deleteNode(responseNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Status of IDOC with number " + idocNumber + " is updated to " + idocStatus +
                      " in SAP");
        }
    }

    /**
     * This method adds child segements to an IDOC based on its metadata. For each segment added,
     * fields are filled from the request node.
     *
     * @param   parentSegment          DOCUMENTME
     * @param   parentSegmentMetadata  DOCUMENTME
     * @param   correspondingNode      DOCUMENTME
     *
     * @throws  JCO.Exception  In case of any exceptions
     */
    private void addChildSegments(IDoc.Segment parentSegment,
                                  IDoc.SegmentMetaData parentSegmentMetadata, int correspondingNode)
                           throws JCO.Exception
    {
        int noOfChildSegments = parentSegmentMetadata.getNumChildren();

        // For each fhild segment
        for (int i = 0; i < noOfChildSegments; i++)
        {
            IDoc.SegmentMetaData childSegmentMetadata = parentSegmentMetadata.getChild(i);
            String childSegmentType = childSegmentMetadata.getType();
            String name_correspondingNode = Node.getName(correspondingNode);
            int[] segmentNodes = Find.match(correspondingNode,
                                            "<" + name_correspondingNode + "><" + childSegmentType +
                                            ">");
            int number_segmentNodes = segmentNodes.length;

            for (int j = 0; j < number_segmentNodes; j++)
            {
                int segmentNode = segmentNodes[j];
                JCoIDoc.JCoSegment childSegment = (JCoIDoc.JCoSegment) parentSegment.addChild(childSegmentType);
                addChildSegments(childSegment, childSegmentMetadata, segmentNode);
                // System.out.println(Node.writeToString(segmentNode, true));
                childSegment.fromXML(Node.writeToString(segmentNode, false));
                // If a segmentXML nodes has child segment XML nodes also, fromXML method is
                // throwing an exception. i.e if the segmentXML node is nested, it is throwing an
                // exception. Delete a segmentNode after fromXML.
                BACUtil.deleteNode(segmentNode);
            }
        }
    }

    /**
     * This method adds child segements to add an IDOC based on its metadata. Only after that
     * fromXML method frames the request.
     *
     * @param  parentSegment          DOCUMENTME
     * @param  parentSegmentMetadata  DOCUMENTME
     */
    private void addChildSegments1(IDoc.Segment parentSegment,
                                   IDoc.SegmentMetaData parentSegmentMetadata)
    {
        int noOfChildSegments = parentSegmentMetadata.getNumChildren();

        // For each fhild segment
        for (int i = 0; i < noOfChildSegments; i++)
        {
            IDoc.SegmentMetaData childSegmentMetadata = parentSegmentMetadata.getChild(i);
            String childSegmentType = childSegmentMetadata.getType();
            IDoc.Segment childSegment = parentSegment.addChild(childSegmentType);
            addChildSegments1(childSegment, childSegmentMetadata);
        }
    }

    /**
     * This method converts the JCO.Response object from SAP into an XML Node and returns it.
     *
     * @param   sapResponse  DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private int convertSAPResponseToXML(JCO.Response sapResponse, Document doc, 
            boolean stripIllegalXMLChars) throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Converting the response into XML.");
        }

        try
        {
            String sapResponseXML = "";
            if (stripIllegalXMLChars == true)
            {
                sapResponseXML = stripNonValidXMLCharacters(sapResponse.toXML());
            }
            else
            {
                sapResponseXML = new String (sapResponse.toXML());
            }
            //sapResponse.writeXML("sapresponse.xml");
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Response from SAP converted to XML String:" + sapResponseXML);
            }
            int responseNode = 0;
            responseNode = doc.parseString(sapResponseXML);

            return responseNode;
        }
        catch (Exception jce)
        {
            throw new SAPConnectorException(jce,
                                            SAPConnectorExceptionMessages.ERROR_CONVERTING_SAP_RESPONSE_TO_XML);
        }
    }

    /**
     * This method creates an IDOC with the given idoctype and cimtype. And sets the values for
     * fields in the control record and data record from the requestNode. And then returns the IDOC.
     * Segments are added to the IDOC only if its corresponding node is found in the request.
     *
     * @param   IDOCRepository  DOCUMENTME
     * @param   idocType        DOCUMENTME
     * @param   cimType         DOCUMENTME
     * @param   requestNode     DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private JCoIDoc.JCoDocument createIDOC(IDoc.Repository IDOCRepository, String idocType,
                                           String cimType, int requestNode, boolean isPartnerUnicode)
                                    throws SAPConnectorException
    {
        JCoIDoc.JCoDocument idoc = (JCoIDoc.JCoDocument) JCoIDoc.createDocument(IDOCRepository,
                                                                                idocType, cimType);
        String controlStrucreName = idoc.getTableStructureName();       
        int idocNode = Node.getFirstChildElement(requestNode);
        int clone_idocNode = Node.duplicate(idocNode);    

        int controlRecordNode = Node.getElement(clone_idocNode, controlStrucreName);        

        if (controlRecordNode == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.CONTROL_RECORD_NOT_FOUND_IN_THE_REQUEST);
        }

        try
        {
            // Fill up control record
        	if(isPartnerUnicode)
        	{ // If the partner system is unicode then the control record has to be set to EDI_DC40_U other wise it remains EDI_DC40
        			Node.setName(controlRecordNode, "EDI_DC40_U") ;
        	}
        	
            idoc.fromXML(Node.writeToString(controlRecordNode, false));         
            // Add segments and fill up each segment
            IDoc.Segment rootSegment = idoc.getRootSegment();
            IDoc.SegmentMetaData rootSegmentMetadata = rootSegment.getSegmentMetaData();
            BACUtil.deleteNode(controlRecordNode);
            addChildSegments(rootSegment, rootSegmentMetadata, clone_idocNode);
        }
        catch (JCO.Exception je)
        {
            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERROR_SETTING_REQUEST_PARAMETERS_FOR_THE_IDOC);
        }
        BACUtil.deleteNode(clone_idocNode);
        return idoc;
    }

    /**
     * This method creates a reqest object for the given RFC.
     *
     * @param   repository  DOCUMENTME
     * @param   rfcName     DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private JCO.Request createRequest(IRepository repository, String rfcName)
                               throws SAPConnectorException
    {
        String rfcNameInUpperCase = rfcName.toUpperCase();

        try
        {
            IFunctionTemplate ft = repository.getFunctionTemplate(rfcNameInUpperCase);

            if (ft == null)
            {
                throw new Exception("Function Template could not be retrieved for the rfc " +
                                    rfcNameInUpperCase);
            }
            return ft.getRequest();
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.ERROR_GETTING_FUNCTION_TEMPLATE,
                                            rfcName);
        }
    }

    /**
     * This method executes a function with the given request and returns the resonse from SAP.
     *
     * @param   client      DOCUMENTME
     * @param   sapRequest  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private JCO.Response executeFunction(JCO.Client client, JCO.Request sapRequest)
                                  throws SAPConnectorException
    {
        JCO.Response sapResponse;

        try
        {
            sapResponse = client.execute(sapRequest);
        }
        catch (Exception ex)
        {
            throw new SAPConnectorException(ex,
                                            SAPConnectorExceptionMessages.ERROR_EXECUTING_REQUEST,
                                            sapRequest.toXML());
        }

        return sapResponse;
    }

    /**
     * This method returns the IDOC number generated in SAP by reading the EDIDC table. RFC Used :
     * RFC_READ_TABLE
     *
     * @param   transactionID  DOCUMENTME
     * @param   client         DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private String getIDOCNumberFromSAP(String transactionID, JCO.Client client,
                                        Document doc)
                                 throws SAPConnectorException
    {
        String returnValue = null;
        int requestNode = 0;

        try
        {
            requestNode = doc.parseString(RFCREADTABLE_reqeustXML);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_PARSING_TEMPLATE,
                                            "RFCREADTABLE_reqeustXML");
        }

        int queryTableNode = Find.firstMatch(requestNode, "<RFC_READ_TABLE><QUERY_TABLE>");
        doc.createText(controlTableName, queryTableNode);

        int fieldNameNode = Find.firstMatch(requestNode,
                                            "<RFC_READ_TABLE><FIELDS><item><FIELDNAME>");
        doc.createText("DOCNUM", fieldNameNode);

        String whereClause = "RCVLAD = '" + transactionID + "'";
        int TEXTNode = Find.firstMatch(requestNode, "<RFC_READ_TABLE><OPTIONS><item><TEXT>");
        doc.createText(whereClause, TEXTNode);

        int responseNode = 0;

        try
        {
            responseNode = sendRFCRequestForInternalPurpose(requestNode, client, "RFC_READ_TABLE",
                                                            null, doc);

            // System.out.println(Node.writeToString(responseNode, true));
            int WANode = Find.firstMatch(responseNode, "<RFC_READ_TABLE.Response><DATA><item><WA>");
            returnValue = Node.getDataWithDefault(WANode, "");

            if (returnValue.equals(""))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("IDOC Number not found in SAP.");
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC Number is " + returnValue);
            }
        }
        finally
        {
            BACUtil.deleteNode(requestNode);
            BACUtil.deleteNode(responseNode);
        }
        return returnValue;
    }

    /**
     * This method returns the IDOC number generated in SAP by reading the EDIDC table. RFC Used :
     * RFC_READ_TABLE
     *
     * @param   idocNumber  DOCUMENTME
     * @param   client      DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int getIDOCStatusFromSAP(String idocNumber, JCO.Client client, Document doc)
                                 throws SAPConnectorException
    {
        String returnValue = null;
        int statusNodeToReturn = 0 ;
        int requestNode = 0;

        try
        {
            requestNode = doc.parseString(EDIDS_RFCREADTABLE_reqeustXML);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_PARSING_TEMPLATE,
                                            "RFCREADTABLE_reqeustXML");
        }

        int queryTableNode = Find.firstMatch(requestNode, "<RFC_READ_TABLE><QUERY_TABLE>");
        doc.createText(statusTableName, queryTableNode);

        int fieldNameNode = Find.firstMatch(requestNode,
                                            "<RFC_READ_TABLE><FIELDS><item><FIELDNAME>");
        doc.createText("STATUS", fieldNameNode);

        String whereClause = "DOCNUM = '" + idocNumber + "'";
        int TEXTNode = Find.firstMatch(requestNode, "<RFC_READ_TABLE><OPTIONS><item><TEXT>");
        doc.createText(whereClause, TEXTNode);

        int responseNode = 0;

        try
        {
            responseNode = sendRFCRequestForInternalPurpose(requestNode, client, "RFC_READ_TABLE",
                                                            null, doc);

            // System.out.println(Node.writeToString(responseNode, true));
            int dataNode = Find.firstMatch(responseNode, "<RFC_READ_TABLE.Response><DATA>");
            // To get the last status record
            int lastItemNode = Node.getLastChild(dataNode);
            int WANode = Find.firstMatch(lastItemNode, "<item><WA>");
           
            if(WANode > 0)
            {
            	statusNodeToReturn = Node.duplicate(Node.getParent(WANode)) ;
            	handlerDelimiters(statusNodeToReturn) ;
            }
            
            returnValue = Node.getDataWithDefault(WANode, "");

            if (returnValue.equals(""))
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.IDOC_STATUS_NOT_FOUND_IN_SAP);
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC Status is " + returnValue);
            }
        }
        finally
        {
            BACUtil.deleteNode(requestNode);
            BACUtil.deleteNode(responseNode);
        }

        return statusNodeToReturn;
    }

    /**
     * To check if the function call was successful. This method checks the RETURN parameter. RETURN
     * can be a structure or a table parameter.
     *
     * @param   sapResponse   DOCUMENTME
     * @param   errorMessage  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private boolean isFunctionCallSuccessful(JCO.Response sapResponse, StringBuffer errorMessage)
                                      throws SAPConnectorException
    {
        if (!sapResponse.hasField("RETURN"))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(" The rfc response does not have a RETURN parameter");
            }
            return true;
        }

        JCO.Field returnField = sapResponse.getField("RETURN");

        if (returnField.isStructure())
        {
            JCO.Structure returnStructure = returnField.getStructure();

            if (returnStructure.hasField("TYPE"))
            {
                String returnType = returnStructure.getString("TYPE");

                if (!returnType.equalsIgnoreCase("S") && !returnType.equals(""))
                {
                    errorMessage.append(returnStructure.getString("MESSAGE"));
                    return false;
                }
            }
        }
        else if (returnField.isTable())
        {
            JCO.Table returnTable = returnField.getTable();
            int noOfRows = returnTable.getNumRows();

            if (noOfRows == 0)
            {
                return true;
            }

            for (int i = 0; i < noOfRows; i++)
            {
                returnTable.setRow(i);

                if (returnTable.hasField("TYPE"))
                {
                    String returnType = returnTable.getString("TYPE");

                    if (!returnType.equalsIgnoreCase("S") && !returnType.equals(""))
                    {
                        errorMessage.append(returnTable.getString("MESSAGE"));
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    

  /**
     *http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html   
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href=â€�http://www.w3.org/TR/2000/REC-xml-20001006#NT-Charâ€�>the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }   
    
    /** Splits the item data and adds them a nodes. The root node then would be retured to the calling code
     * @param itemNode
     */
    private void handlerDelimiters(int itemNode)
    {
    	if(itemNode <= 0)
    		return ;
    	String contentNodeData = Node.getDataElement(itemNode, "WA", "") ;
    	if("".equalsIgnoreCase(contentNodeData)) 
    		return ; // No post processing
    	String logTokens[] = contentNodeData.split(":");
    	for(int i=0; logTokens!=null && i<logTokens.length; i++)
    	{
    		if(i ==0)
    		{
    			Node.createElementWithParentNS("STATUS", logTokens[i],itemNode) ;
    		}
    		else if(i ==1)
    		{
    			Node.createElementWithParentNS("STATXT", logTokens[i],itemNode) ;
    		}
    		else if(i ==2)
    		{
    			Node.createElementWithParentNS("STAPA1", logTokens[i],itemNode) ;
    		}
    		else if(i ==3)
    		{
    			Node.createElementWithParentNS("STAPA2", logTokens[i],itemNode) ;
    		}
    		else if(i ==4)
    		{
    			Node.createElementWithParentNS("STAPA3", logTokens[i],itemNode) ;
    		}
    		else if(i ==5)
    		{
    			Node.createElementWithParentNS("STAPA4", logTokens[i],itemNode) ;
    		}    		
    		
    	}
    }
}
