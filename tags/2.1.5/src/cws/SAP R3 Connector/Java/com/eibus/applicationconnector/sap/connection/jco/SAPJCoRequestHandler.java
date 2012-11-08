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

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.ISAPRequestHandler;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.idoc.TargetMappingFinder;
import com.eibus.applicationconnector.sap.metadata.MetadataLoader;
import com.eibus.applicationconnector.sap.metadata.SAPMethodGenerator;
import com.eibus.applicationconnector.sap.usermapping.IUserMapping;
import com.eibus.applicationconnector.sap.usermapping.UserMappingFactory;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.Util;
import com.eibus.applicationconnector.sap.xmi.xbp.XBPRequestManager;
import com.eibus.applicationconnector.sap.xmi.xbp.XBPSessionContext;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * This class handles all the requests coming to the SOAP processor if the middleware is JCo API. It
 * handles requests of implementation types SAPBAPI, SAPRFC, SAPIDOC, SAPTuple,SAPMetadata,
 * SAPPublish.
 *
 * @author  ygopal
 */
public class SAPJCoRequestHandler
    implements ISAPRequestHandler
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPJCoRequestHandler.class);
    /**
     * Boolean to check whether to commit the SAP transaction or not. This becomes true only if the
     * AutoCommit tag in the implemenation contains string "true" ignoring case.
     */
    private boolean autoCommit = false;
    /**
     * This is true only for BAPI or RFC call.
     */
    private boolean autoRollBack = false;
    /**
     * Holds the configuration of the connector.
     */
    private ISAPConfiguration m_config;
    /**
     * Holds the XML document to use for creating nodes.
     */
    private Document m_doc;
    /**
     * Holds the current connection to use.
     */
    private SAPJCoConnection m_jcoCon;
    /**
     * Holds the generator for the actual methods.
     */
    private SAPMethodGenerator m_methodGenerator;
    /**
     * Holds the proper user mapping that should be used.
     */
    private IUserMapping m_userMapping;
    /**
     * Holds the repository for the SAP metadata.
     */
    private MetadataLoader metadataLoader;
    /**
     * The actual sender for the request.
     */
    private SAPJCoRequestSender requestSender = null;

    /**
     * Creates a new SAPJCoRequestHandler object.
     *
     * @param   configuration  The configuration of the processor.
     * @param   doc            The XML document to use for the metadata.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public SAPJCoRequestHandler(ISAPConfiguration configuration, Document doc)
                         throws SAPConnectorException
    {
        m_config = configuration;
        m_userMapping = UserMappingFactory.createUserMapping(configuration);
        m_doc = doc;
    }

    /**
     * Creates a new SAPJCoRequestHandler object.
     *
     * @param   configuration  The configuration of the processor.
     * @param   doc            The XML document to use for the metadata.
     * @param   userMapping    The user mapping to use for this request.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public SAPJCoRequestHandler(ISAPConfiguration configuration, Document doc,
                                IUserMapping userMapping)
                         throws SAPConnectorException
    {
        m_config = configuration;
        m_userMapping = userMapping;
        m_doc = doc;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#abort()
     */
    public void abort()
               throws SAPConnectorException
    {
        try
        {
            if (autoRollBack)
            {
                try
                {
                    int rollbackRequestNode = 0;

                    rollbackRequestNode = m_doc.parseString(rollbackRequestXML);
                    requestSender.sendRFCRequest(rollbackRequestNode, m_jcoCon,
                                                 "BAPI_TRANSACTION_ROLLBACK");
                }
                catch (SAPConnectorException sce)
                {
                    throw sce;
                }
                catch (Exception e)
                {
                    throw new SAPConnectorException(e,
                                                    SAPConnectorExceptionMessages.ERROR_ABORTING_REQUEST);
                }
            }
        }
        finally
        {
            putUserConnection();
        }
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#commit()
     */
    public void commit()
                throws SAPConnectorException
    {
        try
        {
            if (autoCommit)
            {
                int commitRequestNode = 0;

                try
                {
                    commitRequestNode = m_doc.parseString(XML_COMMIT_REQUEST);
                    requestSender.sendRFCRequest(commitRequestNode, m_jcoCon,
                                                 "BAPI_TRANSACTION_COMMIT");
                }
                catch (SAPConnectorException sce)
                {
                    throw sce;
                }
                catch (Exception e)
                {
                    throw new SAPConnectorException(e,
                                                    SAPConnectorExceptionMessages.ERROR_COMMITTING_REQUEST);
                }
            }
        }
        finally
        {
            putUserConnection();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public SAPJCoConnection getConnection()
    {
        return m_jcoCon;
    }

    /**
     * This method returns the document that is used.
     *
     * @return  The document that is used.
     */
    public Document getDocument()
    {
        return m_doc;
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public SAPJCoRequestSender getRequestSender()
    {
        return requestSender;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleBAPIRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleBAPIRequest(BodyBlock request, BodyBlock response)
                              throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int requestNode = request.getXMLNode();
        
        // To remove the namespace common to all requests in Cordys.
        Node.removeAttribute(requestNode, commonAttributeName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("RequestNode is " + Node.writeToString(requestNode, true));
        }

        int implementationNode = request.getMethodDefinition().getImplementation();

        String rfmName = XPathHelper.getStringValue(implementationNode, "RFMName", "");

        if (!Util.isSet(rfmName))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_IMPLEMENTATION,
                                            "RFMName");
        }

        autoCommit = XPathHelper.getBooleanValue(implementationNode, "AutoCommit");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("RFC Name is " + rfmName + ", AutoCommit is " + autoCommit);
        }

        autoRollBack = XPathHelper.getBooleanValue(implementationNode, "AutoRollback");

        // int requestNodeToBeSent = Node.getFirstChild(requestNode);
        // To set the root tag name to the RFM name from BO.BAPI name
        Node.setName(requestNode, rfmName);

        int sapResponse = requestSender.sendRFCRequest(requestNode, m_jcoCon, rfmName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Response node from SAP is " + Node.writeToString(sapResponse, true));
        }

        int responseNode = response.getXMLNode();
        // Node.appendToChildren(sapResponse, responseNode);
        Node.duplicateAndAppendToChildren(Node.getFirstChild(sapResponse),
                                          Node.getLastChild(sapResponse), responseNode);
        Node.delete(sapResponse);
        return true;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleIDOCRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleIDOCRequest(BodyBlock request, BodyBlock response)
                              throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int requestNode = request.getXMLNode();
        // To remove the namespace common to all requests in Cordys.
        Node.removeAttribute(requestNode, commonAttributeName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("RequestNode is " + Node.writeToString(requestNode, true));
        }

        // System.out.println("Before sending IDOC " + doc.getNumUsedNodes(true));
        MethodDefinition methodDefinition = request.getMethodDefinition();

        
        int implementationNode = methodDefinition.getImplementation();

        String mesType = XPathHelper.getStringValue(implementationNode, "MESType", "");
        
        /**
         * Getting the IDOCType information from the method implementation. If IDOCType attribute is not found in the implementation then method name is taken as IDOC Type
         */
        String idocType = XPathHelper.getStringValue(implementationNode, "IDOCType", "");
        if("".equalsIgnoreCase(idocType))
        { 
        	if (LOG.isErrorEnabled())
            {
                LOG.error("IDOC Type information is not found in the method implementation. This could be a method generated from old isvp");
            }
        	 idocType = methodDefinition.getMethodName();
        }

        if (!Util.isSet(mesType))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_IMPLEMENTATION,
                                            "Message type");
        }

        String cimType = XPathHelper.getStringValue(implementationNode, "CIMType", "");

        if (!Util.isSet(cimType))
        {
        	if (LOG.isInfoEnabled())
            {
                LOG.info("CIMType is not avaliable");
            }
            //throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_IMPLEMENTATION, "CIMType");
        }

        String fromLogicalSystem = null;
        String toLogicalSystem = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Message type is " + mesType + ", IDOC type is " + idocType +
                      ", CIMType is " + cimType);
        }
        
        int responseNode = response.getXMLNode();
        String transactionId = requestSender.sendIDOCRequest(requestNode,responseNode, m_jcoCon, idocType, mesType,
                                                        cimType, fromLogicalSystem,
                                                        toLogicalSystem);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Response node from SAP is " + Node.writeToString(responseNode, true));
        }

      
        //Node.appendToChildren(sapResponse, responseNode);
        return true;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleMetaDataRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleMetaDataRequest(BodyBlock request, BodyBlock response)
                                  throws SAPConnectorException
    {
        if (metadataLoader == null)
        {
            // metadataLoader = new SAPJCoMetadataLoader(mapper.getProcessorConfig(),
            // requestSender, jcoCon);
            metadataLoader = new SAPJCoMetadataLoader(m_config, this);
        }
        return metadataLoader.getMetadata(request, response);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleMethodPublishRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleMethodPublishRequest(BodyBlock request, BodyBlock response)
                                       throws SAPConnectorException
    {
        if (m_methodGenerator == null)
        {
            m_methodGenerator = new SAPJCoMethodGenerator(m_config.getRepository(),
                                                          m_config.getIDOCRepository());
        }

        return m_methodGenerator.publishMethod(request, response);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleRFCReqeust(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleRFCReqeust(BodyBlock request, BodyBlock response)
                             throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int requestNode = request.getXMLNode();
// To remove the namespace common to all requests in Cordys.
        Node.removeAttribute(requestNode, commonAttributeName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("RequestNode is " + Node.writeToString(requestNode, true));
        }

        MethodDefinition methodDefinition = request.getMethodDefinition();

        int implementationNode = methodDefinition.getImplementation();
        String rfmName = XPathHelper.getStringValue(implementationNode, "RFMName", "");

        if (!Util.isSet(rfmName))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_IMPLEMENTATION,
                                            "RFMName");
        }

        autoCommit = XPathHelper.getBooleanValue(implementationNode, "AutoCommit");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("AutoCommit is " + autoCommit);
        }

        autoRollBack = XPathHelper.getBooleanValue(implementationNode, "AutoRollback");

        // This eliminates the LDAP method name tag from the request.
        // int requestNodeToBeSent = Node.getFirstChild(requestNode);
        int sapResponse = requestSender.sendRFCRequest(requestNode, m_jcoCon, rfmName);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Response node from SAP is " + Node.writeToString(sapResponse, true));
        }

        int responseNode = response.getXMLNode();
        // Node.appendToChildren(sapResponse, responseNode);
        Node.duplicateAndAppendToChildren(Node.getFirstChild(sapResponse),
                                          Node.getLastChild(sapResponse), responseNode);
        Node.delete(sapResponse);
        return true;
    }
    
    
    /** Routes the XMP implementation methods to XMPManager
     * @param request
     * @param response
     * @return
     * @throws SAPConnectorException
     */
    public boolean handleXBPReqeust(BodyBlock request, BodyBlock response)
    				throws SAPConnectorException
	{
		initializeClientAndRequestSender();
		
		int requestNode = request.getXMLNode();
		//To remove the namespace common to all requests in Cordys.
		Node.removeAttribute(requestNode, commonAttributeName);		
		if (LOG.isDebugEnabled())
		{
		LOG.debug("RequestNode is " + Node.writeToString(requestNode, true));
		}
		
		MethodDefinition methodDefinition = request.getMethodDefinition();		
		int implementationNode = methodDefinition.getImplementation();
		int responseNode = response.getXMLNode() ;
		XBPSessionContext session = new XBPSessionContext();
		session.setSessionConnection(this.getConnection());
		session.setExternalUserId(this.getConnection().getUser());
		session.setExtcompany(this.m_config.getXMICompanyName()) ;
		session.setExtProduct(this.m_config.getXMIProductName());
		XBPRequestManager xbpRequestManager = new XBPRequestManager() ;
		session.setNomDocument(this.getDocument());
		xbpRequestManager.setM_config(m_config);
		
		xbpRequestManager.setSession(session);
		int xbpCallResponse = xbpRequestManager.processRequest(requestNode, responseNode, implementationNode);
		if (LOG.isDebugEnabled())
		{
				LOG.debug("Response node from SAP is " + Node.writeToString(xbpCallResponse, true));
		}
		Node.duplicateAndAppendToChildren(Node.getFirstChild(xbpCallResponse),
                Node.getLastChild(xbpCallResponse), responseNode);
		BACUtil.deleteNode(xbpCallResponse);	
		return true;
	}



    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleTupleRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleTupleRequest(BodyBlock request, BodyBlock response)
                               throws SAPConnectorException
    {
        return true;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPRequestHandler#handleUtilRequest(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    public boolean handleUtilRequest(BodyBlock request, BodyBlock response)
                              throws SAPConnectorException
    {
        int requestNode = request.getXMLNode();
        int responseNode = response.getXMLNode();
        MethodDefinition methodDefinition = request.getMethodDefinition();
        String methodName = methodDefinition.getMethodName();

        if (methodName.equalsIgnoreCase("ReloadTargetMappings"))
        {
            TargetMappingFinder mappingFinder = m_config.getTargetMappingFinder();
            mappingFinder.loadTargetMapppings();

            return true;
        }
        else if (methodName.equalsIgnoreCase("SynchronizeIDOCStatus"))
        {
            /*
             * Sample Request <SynchronizeIDOCStatus>     <IDOCNum></IDOCNum>
             * </SynchronizeIDOCStatus> This method gets the status of the IDOC with given number
             * from SAP and updates the IDOCTable in local database.
             */
            initializeClientAndRequestSender();

            int node_idocNumber = XPathHelper.selectSingleNode(requestNode,
                                                               "//SynchronizeIDOCStatus/IDOCNum");
            String idocNumber = Node.getDataWithDefault(node_idocNumber, "");

            if (!idocNumber.equals(""))
            {
            	int idocStatusNode = requestSender.synchronizeIDOCStatus(idocNumber, m_jcoCon, m_doc) ;
            	 //int responseNode = response.getXMLNode();
            	 if(idocStatusNode >0 )
            	 {
            		 Node.appendToChildren(idocStatusNode,idocStatusNode, responseNode) ;
            	 }
                return true;
            }
            else
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.IDOC_NUMBER_IS_NOT_FOUND);
            }
        }
        else if (methodName.equalsIgnoreCase("UpdateIDOCStatus"))
        {
            /*
             * Sample Request <UpdateIDOCStatus>     <IDOCNum></IDOCNum>     <Status></Status>
             * </UpdateIDOCStatus> It updates the status in both SAP and the IDOCTable in local
             * database.
             */
            initializeClientAndRequestSender();

            int node_idocNumber = XPathHelper.selectSingleNode(requestNode,
                                                               "//UpdateIDOCStatus/IDOCNum");
            int node_idocStatus = XPathHelper.selectSingleNode(requestNode,
                                                               "//UpdateIDOCStatus/Status");

            String idocNumber = Node.getDataWithDefault(node_idocNumber, "");
            String idocStatus = Node.getDataWithDefault(node_idocStatus, "");

            if (!(idocNumber.equals("") || idocStatus.equals("")))
            {
                // update status in SAP
                requestSender.updateIDOCStatusinSAP(idocNumber, idocStatus, m_jcoCon, m_doc);
                // update status in local DB
                requestSender.updateIDOCStatus(idocNumber, idocStatus, m_doc);
                return true;
            }
            else
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.INVALID_REQUEST_PARAMETERS,
                                                idocNumber, idocStatus);
            }
        }
        else if (methodName.equals("GetSystemDate"))
        {
            Document doc = Node.getDocument(requestNode);
            int dateFormatNode = XPathHelper.selectSingleNode(requestNode,
                                                              "//GetSystemDate/DateFormat");
            String dateFormat = Node.getDataWithDefault(dateFormatNode, "");
            String systemDate;

            if (dateFormat.length() > 0)
            {
                systemDate = BACUtil.getCurrentDate(dateFormat);
            }
            else
            {
                systemDate = BACUtil.getCurrentDate();
            }

            //int responseNode = response.getXMLNode();
            doc.createTextElement("SystemDate", systemDate, responseNode);
            return true;
        }
        else if (methodName.equals("GetSerializedIDOCMetadataObject"))
        {
        	if (metadataLoader == null)
            {               
                metadataLoader = new SAPJCoMetadataLoader(m_config, this);
            }
        	
        	if(metadataLoader instanceof SAPJCoMetadataLoader )
        	{
        		return ((SAPJCoMetadataLoader) metadataLoader).getSerializedIDOCMetadataObject(requestNode, responseNode);
        	}             
        }
        else if("RFC_READ_TABLE".equals(methodName))
        {// This utility method is provided as it gets mostly used.
        	this.handleRFCReqeust(request, response);
        	BACUtil.formatRFC_READ_TABLEResponse(requestNode,responseNode);
        	
        }
        
        return false;
    }

    /**
     * This method gets a client for the current user and creates an object of the
     * SAPJCoRequestSender. These are required only for BAPI, RFC,IDOC calls and to load metadata
     * from SAP server.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void initializeClientAndRequestSender()
                                          throws SAPConnectorException
    {
        initializeClient();
        initializeRequestSender();
    }

    /**
     * This method returns the connection to use for the current user. If no mapped user is
     * specified it will revert to the user configured at system level.
     *
     * @return  The connection to use.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private SAPJCoConnection getClientforCurrentUser()
                                              throws SAPConnectorException
    {
        String currentUser = m_userMapping.getMappedUsername();
        String password = m_userMapping.getMappedPassword();

        // Get the connection with the given username/password.
        SAPJCoConnectionManager jcoConManager = m_config.getJCoConnectionManager();

        try
        {
            m_jcoCon = jcoConManager.getUserConnection(m_config, currentUser, password);
        }
        catch (SAPConnectorException sce)
        {
            // Check if we need to fall back
            if (m_config.getFallbackToSystemUser() &&
                    sce.getMessageObject().getFullyQualifiedResourceID().equals(SAPConnectorExceptionMessages
                                                                                    .LOGIN_FAILED_FOR_USER
                                                                                    .getFullyQualifiedResourceID()))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Reverting to the system user, after failed login for mapped user " +
                              currentUser);
                }

                currentUser = m_config.getUserID();
                password = m_config.getPassword();

                // Try to get a connection with the system user.
                m_jcoCon = jcoConManager.getUserConnection(m_config, currentUser, password);
            }
            else
            {
                throw sce;
            }
        }

        return m_jcoCon;
    }

    /**
     * This method creates.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void initializeClient()
                           throws SAPConnectorException
    {
        if (m_jcoCon == null)
        {
            m_jcoCon = getClientforCurrentUser();
        }
    }

    /**
     * DOCUMENTME.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void initializeRequestSender()
                                  throws SAPConnectorException
    {
        if (requestSender == null)
        {
            requestSender = new SAPJCoRequestSender(m_config);
        }
    }

    /**
     * This method releases the connection back to the pool.
     */
    private void putUserConnection()
    {
        if (m_jcoCon != null)
        {
            m_config.getJCoConnectionManager().putUserConnection(m_jcoCon);
            m_jcoCon = null;
        }
    }
}
