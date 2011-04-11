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

import com.eibus.applicationconnector.sap.connection.ISAPConnection;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;
import com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata;
import com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata;
import com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;
import com.eibus.applicationconnector.sap.metadata.types.SAPMetadataFactory;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.util.logger.CordysLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a thin wrapper around JCO based metadata connections.
 *
 * @author  pgussow
 */
public class MetadataSAPConnection
    implements ISAPConnection
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MetadataSAPConnection.class);
    /**
     * Holds the name of the SAP bapi tag 'OBJECTNAME'.
     */
    private static final String TAG_SAP_OBJECT_NAME = "OBJECTNAME";
    /**
     * Holds the name of the SAP bapi tag 'OBJTYPE'.
     */
    private static final String TAG_SAP_OBJTYPE = "OBJTYPE";
    /**
     * Holds the name of the SAP IDOC tag 'MESTYP'.
     */
    private static final String TAG_SAP_MESSAGE_TYPE = "MESTYP";
    /**
     * Holds the name of the SAP IDOC tag 'DESCRP'.
     */
    private static final String TAG_SAP_DESCRIPTION = "DESCRP";
    /**
     * Request XMLs to get the metadata from the SAP server.
     */
    private static final String GET_ALL_BAPIS_REQUEST = "<SWO_QUERY_API_METHODS xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\">" +
                                                        "<LANGUAGE></LANGUAGE>" +
                                                        "<METHOD></METHOD>" +
                                                        "<OBJTYPE></OBJTYPE>" +
                                                        "<WITH_IMPL_METHODS></WITH_IMPL_METHODS>" +
                                                        "<WITH_INTERNAL_API_METHODS></WITH_INTERNAL_API_METHODS>" +
                                                        "<WITH_OBJECT_NAMES>X</WITH_OBJECT_NAMES>" +
                                                        "<WITH_TEXTS>X</WITH_TEXTS>" +
                                                        "</SWO_QUERY_API_METHODS>";
    /**
     * Holds the template XML to search for RFCs.
     */
    private static final String GET_ALL_RFCS_REQUEST = "<RFC_FUNCTION_SEARCH xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\">" +
                                                       "<FUNCNAME>*</FUNCNAME>" +
                                                       "</RFC_FUNCTION_SEARCH>";
    /**
     * Holds the template XML to search for IDOCs.
     */
    private static final String GET_ALL_IDOCS_REQUEST = "<IDOCTYPES_FOR_MESTYPE_READ xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\">" +
                                                        "<P_MESTYP>*</P_MESTYP>" +
                                                        "</IDOCTYPES_FOR_MESTYPE_READ>";

    /**
     * Holds the JCO request handler.
     */
    private SAPJCoRequestHandler m_requestHandler = null;

    /**
     * Creates a new MetadataSAPConnection object.
     *
     * @param   requestHandler  The JCO request handler to use.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public MetadataSAPConnection(SAPJCoRequestHandler requestHandler)
                          throws SAPConnectorException
    {
        m_requestHandler = requestHandler;

        // Make sure the connection is allocated.
        m_requestHandler.initializeClientAndRequestSender();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPConnection#getAllBAPIs()
     */
    @Override public List<ITypeContainer> getAllBAPIs()
                                               throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Getting all BAPIs from SAP");
        }

        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        SAPJCoRequestSender sender = getRequestSender();

        // First we need to execute the RFC to SAP to return all BAPIs that are in the system.
        int allBAPIs = sender.sendRFCRequestForInternalPurpose(GET_ALL_BAPIS_REQUEST,
                                                               m_requestHandler.getConnection(),
                                                               "SWO_QUERY_API_METHODS",
                                                               "rfc:SWO_QUERY_API_METHODS",
                                                               m_requestHandler.getDocument());

        try
        {
            // All SAP RFC calls are unqualified. So no need for the XPathMetaInfo.
            int[] items = XPathHelper.selectNodes(allBAPIs, "API_METHODS/item");

            // This list is flat with all methods. It is sorted by object type though.
            ITypeContainer container = null;

            for (int item : items)
            {
                // First create the data.
                IBAPIMetadata bapi = SAPMetadataFactory.parseSAPBAPIInformation(item);

                // Get the objectType and objectName (basically the name and display name for the
                // BAPI
                String value = XPathHelper.getStringValue(item, TAG_SAP_OBJTYPE);
                String display = XPathHelper.getStringValue(item, TAG_SAP_OBJECT_NAME);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found BAPI method " + display + "." + bapi.getDisplayName());
                }

                if (!Util.isSet(value) || !Util.isSet(display))
                {
                    throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_THE_NAME_OF_THE_BAPI);
                }

                // Now check if this operation is for the current bapi, or that it is a new one.
                if ((container == null) || !container.getValue().equals(value))
                {
                    container = SAPMetadataFactory.createContainer(ESAPObjectType.BAPI);
                    returnValue.add(container);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Creating container for BAPI object " + value);
                    }
                    container.setValue(value);
                    container.setDisplayName(display);
                }

                // Add the parsed BAPi to the container.
                container.addType(bapi);
            }
        }
        catch (SAPConfigurationException e)
        {
            throw new SAPConnectorException(e, e.getMessageObject(), e.getMessageParameters());
        }
        finally
        {
            allBAPIs = BACUtil.deleteNode(allBAPIs);
        }

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPConnection#getAllIDOCs()
     */
    @Override public List<ITypeContainer> getAllIDOCs()
                                               throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Getting all IDOCs from SAP");
        }

        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        SAPJCoRequestSender sender = getRequestSender();

        // First we need to execute the RFC to SAP to return all BAPIs that are in the system.
        int allIDOCs = sender.sendRFCRequestForInternalPurpose(GET_ALL_IDOCS_REQUEST,
                                                               m_requestHandler.getConnection(),
                                                               "IDOCTYPES_FOR_MESTYPE_READ",
                                                               "rfc:IDOCTYPES_FOR_MESTYPE_READ",
                                                               m_requestHandler.getDocument());

        try
        {
            // All SAP RFC calls are unqualified. So no need for the XPathMetaInfo.
            int[] items = XPathHelper.selectNodes(allIDOCs, "PT_MESSAGES/item");

            // This list is flat with all methods. It is sorted by object type though.
            ITypeContainer container = null;

            for (int item : items)
            {
                // First create the data.
                IIDOCMetadata idoc = SAPMetadataFactory.parseSAPIDOCInformation(item);

                // Get the objectType and objectName (basically the name and display name for the
                // IDOC
                String value = XPathHelper.getStringValue(item, TAG_SAP_MESSAGE_TYPE, "");
                String display = XPathHelper.getStringValue(item, TAG_SAP_DESCRIPTION, "");

                if (!Util.isSet(display))
                {
                    display = value;
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Found IDOC method " + value + "." + idoc.getValue());
                }

                if (!Util.isSet(value) || !Util.isSet(display))
                {
                    throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_THE_NAME_OF_THE_IDOC);
                }

                // Now check if this operation is for the current bapi, or that it is a new one.
                if ((container == null) || !container.getValue().equals(value))
                {
                    container = SAPMetadataFactory.createContainer(ESAPObjectType.IDOC);
                    returnValue.add(container);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Creating container for IDOC object " + value);
                    }

                    container.setValue(value);
                    container.setDisplayName(display);
                    container.setDescription(display);
                }

                // Add the parsed BAPi to the container.
                container.addType(idoc);
            }
        }
        catch (SAPConfigurationException e)
        {
            throw new SAPConnectorException(e, e.getMessageObject(), e.getMessageParameters());
        }
        finally
        {
            allIDOCs = BACUtil.deleteNode(allIDOCs);
        }

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.connection.ISAPConnection#getAllRFCs()
     */
    @Override public List<ITypeContainer> getAllRFCs()
                                              throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Getting all RFCs from SAP");
        }

        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        SAPJCoRequestSender sender = getRequestSender();

        // First we need to execute the RFC to SAP to return all BAPIs that are in the system.
        int allRFCs = sender.sendRFCRequestForInternalPurpose(GET_ALL_RFCS_REQUEST,
                                                              m_requestHandler.getConnection(),
                                                              "RFC_FUNCTION_SEARCH",
                                                              "rfc:RFC_FUNCTION_SEARCH",
                                                              m_requestHandler.getDocument());

        try
        {
            // All SAP RFC calls are unqualified. So no need for the XPathMetaInfo.
            int[] items = XPathHelper.selectNodes(allRFCs, "FUNCTIONS/item");

            // This list is flat with all methods. It is sorted by object type though.
            ITypeContainer container = null;

            for (int item : items)
            {
                // First create the data.
                IRFCMetadata rfc = SAPMetadataFactory.parseSAPRFCInformation(item);

                // Get the objectType and objectName (basically the name and displayname for the
                // BAPI
                String value = rfc.getValue();
                String display = rfc.getDisplayName();

                if (!Util.isSet(value) || !Util.isSet(display))
                {
                    throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_THE_NAME_OF_THE_RFC);
                }

                container = SAPMetadataFactory.createContainer(ESAPObjectType.RFC);
                returnValue.add(container);

                container.setValue(value);
                container.setDisplayName(display);

                // Add the parsed RFC to the container.
                container.addType(rfc);
            }
        }
        catch (SAPConfigurationException e)
        {
            throw new SAPConnectorException(e, e.getMessageObject(), e.getMessageParameters());
        }
        finally
        {
            allRFCs = BACUtil.deleteNode(allRFCs);
        }

        return returnValue;
    }

    /**
     * This method gets the request sender.
     *
     * @return  The request sender.
     */
    public SAPJCoRequestSender getRequestSender()
    {
        return m_requestHandler.getRequestSender();
    }
}
