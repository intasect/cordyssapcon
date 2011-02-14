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
 package com.eibus.applicationconnector.sap;

import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.general.ExceptionUtil;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.connection.ISAPRequestHandler;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestHandler;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.soap.BaseMethod;
import com.eibus.applicationconnector.sap.soap.EDynamicAction;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This object is created for every SOAP transaction received by the SOAP Processor. Based on the
 * middleware, requests are passed on to the respective handler.
 */
public class SAPTransaction
    implements ApplicationTransaction
{
    /**
     * Holds the type for the SAP utility methodset.
     */
    private static final String TYPE_SAP_UTIL = "SAPUtil";
    /**
     * Holds the type for the SAP BAPI execution method.
     */
    private static final String TYPE_SAPBAPI = "SAPBAPI";
    /**
     * Holds the type for the SAP RFC execution method.
     */
    private static final String TYPE_SAPRFC = "SAPRFC";
    /**
     * Holds the type for the SAP IDOC execution method.
     */
    private static final String TYPE_SAPIDOC = "SAPIDOC";
    /**
     * Holds the type for the methods which use the tuple format.
     */
    private static final String TYPE_SAP_TUPLE = "SAPTuple";
    /**
     * Holds the type for the SAP metadata.
     */
    private static final String TYPE_SAP_METADATA = "SAPMetadata";
    /**
     * Holds the type for the SAP connector new style.
     */
    private static final String TYPE_SAP_CONNECTOR = "SAP_CONNECTOR";
    /**
     * Holds the type for the SAP publishing/creation of methods.
     */
    private static final String TYPE_SAP_PUBLISH = "SAPPublish";
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPTransaction.class);
    /**
     * Holds all the types that are supported by this connector.
     */
    private static List<String> s_types;

    static
    {
        s_types = Collections.synchronizedList(new ArrayList<String>());

        s_types.add(TYPE_SAP_METADATA);
        s_types.add(TYPE_SAP_CONNECTOR);
        s_types.add(TYPE_SAP_PUBLISH);
        s_types.add(TYPE_SAP_TUPLE);
        s_types.add(TYPE_SAP_UTIL);
        s_types.add(TYPE_SAPBAPI);
        s_types.add(TYPE_SAPIDOC);
        s_types.add(TYPE_SAPRFC);
    }

    /**
     * Holds the configuration that can be used.
     */
    private ISAPConfiguration m_config;

    /**
     * Holds the SAP request handler.
     */
    private ISAPRequestHandler requestHandler;

    /**
     * Creates a new SAPTransaction object.
     *
     * @param   config           The current configuration.
     * @param   soapTransaction  The current SOAP transaction.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    SAPTransaction(ISAPConfiguration config, SOAPTransaction soapTransaction)
            throws SAPConnectorException
    {
        m_config = config;

        // Get the document to use.
        Document document = Node.getDocument(soapTransaction.getResponseEnvelope());

        // Create the request handler.
        requestHandler = new SAPJCoRequestHandler(m_config, document);
    }

    /**
     * @see  com.eibus.soap.ApplicationTransaction#abort()
     */
    public void abort()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("In Application Transaction's abort method.");
        }

        try
        {
            requestHandler.abort();
        }
        catch (SAPConnectorException sce)
        {
            throw new IllegalStateException(sce.getLocalizedMessage(), sce);
        }
    }

    /**
     * Returns true if the type is either SAPBAPI or SAPRFC or SAPIDOC or SAPUtil or SAPMetadata or
     * SAPPublish.
     *
     * @param   type  The type to check.
     *
     * @return  Whether or not the given type can be processed.
     *
     * @see     com.eibus.soap.ApplicationTransaction#canProcess(java.lang.String)
     */
    public boolean canProcess(String type)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Can process type " + type + ": " + (s_types.contains(type)));
        }

        return s_types.contains(type);
    }

    /**
     * @see  com.eibus.soap.ApplicationTransaction#commit()
     */
    public void commit()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("In Application Transaction's commit method.");
        }

        try
        {
            requestHandler.commit();
        }
        catch (SAPConnectorException sce)
        {
            throw new IllegalStateException(sce.getLocalizedMessage(), sce);
        }
    }

    /**
     * @see  com.eibus.soap.ApplicationTransaction#process(com.eibus.soap.BodyBlock, com.eibus.soap.BodyBlock)
     */
    public boolean process(BodyBlock request, BodyBlock response)
    {
        boolean bReturn = true;

        String requestType = request.getMethodDefinition().getType();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Request type: " + requestType + "\nIncoming SOAP response:\n" +
                      Node.writeToString(Node.getRoot(request.getXMLNode()), false));
        }

        try
        {
            if (requestType.equalsIgnoreCase(TYPE_SAPBAPI))
            {
                return requestHandler.handleBAPIRequest(request, response);
                       // return requestHandler.handleRFCReqeust(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAPRFC))
            {
                return requestHandler.handleRFCReqeust(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAPIDOC))
            {
                return requestHandler.handleIDOCRequest(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAP_TUPLE))
            {
                return requestHandler.handleTupleRequest(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAP_METADATA))
            {
                return requestHandler.handleMetaDataRequest(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAP_PUBLISH))
            {
                return requestHandler.handleMethodPublishRequest(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAP_UTIL))
            {
                return requestHandler.handleUtilRequest(request, response);
            }
            else if (requestType.equalsIgnoreCase(TYPE_SAP_CONNECTOR))
            {
                String sAction = getActionFromImplementation(request);
                EDynamicAction daAction = null;

                try
                {
                    daAction = EDynamicAction.valueOf(sAction.toUpperCase());
                }
                catch (Exception e)
                {
                    // Most likely the enum could not be parsed. So the action is invalid.
                    // Just to be sure we'll log the exception when debug is enabled. Otherwise
                    // we just return the invalid action message
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Error determining the action type", e);
                    }
                    throw new SAPConnectorException(SAPConnectorExceptionMessages.INVALID_ACTION_TYPE0,
                                                    sAction);
                }

                // Instantiate the proper class.
                Class<? extends BaseMethod> cClass = daAction.getImplementationClass();
                Constructor<? extends BaseMethod> cConstructor = cClass.getConstructor(BodyBlock.class,
                                                                                       BodyBlock.class,
                                                                                       ISAPConfiguration.class);

                BaseMethod bmMethod = cConstructor.newInstance(request, response, m_config);
                bmMethod.execute();
            }
        }
        catch (Throwable tException)
        {
            ServerLocalizableException sle = null;

            if (!(tException instanceof ServerLocalizableException))
            {
                sle = new SAPConnectorException(tException, Messages.ERROR_EXECUTING_REQUEST,
                                                ExceptionUtil.getSimpleErrorTrace(tException,
                                                                                  true));
            }
            else
            {
                sle = (ServerLocalizableException) tException;
            }

            // Create the proper SOAP fault.
            sle.toSOAPFault(response);

            LOG.error(sle, Messages.ERROR_EXECUTING_REQUEST,
                      ExceptionUtil.getSimpleErrorTrace(tException, true));

            if (response.isAsync())
            {
                response.continueTransaction();
                bReturn = false;
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Returning SOAP response:\n" +
                      Node.writeToString(Node.getRoot(response.getXMLNode()), false));
        }

        return bReturn;
    }

    /**
     * This method gets the desired action from the method's implementation.
     *
     * @param   bbRequest  The request that was received.
     *
     * @return  The action in the implementation.
     *
     * @throws  SAPConnectorException  EmailIOException In case the action was not found.
     */
    private String getActionFromImplementation(BodyBlock bbRequest)
                                        throws SAPConnectorException
    {
        String sAction;

        int iImplNode = bbRequest.getMethodDefinition().getImplementation();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_IMPLEMENTATION);

        sAction = XPathHelper.getStringValue(iImplNode, "ns:sapconnector/ns:action/text()", xmi,
                                             "");

        if (sAction.length() == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.NO_ACTION_FOUND_IN_THE_METHOD_IMPLEMENTATION);
        }

        return sAction;
    }
}
