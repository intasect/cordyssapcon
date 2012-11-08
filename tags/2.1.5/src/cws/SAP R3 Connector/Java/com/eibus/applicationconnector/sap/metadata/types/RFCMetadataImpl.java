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
 package com.eibus.applicationconnector.sap.metadata.types;

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationExceptionMessages;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class holds the metadata for a RFC.
 *
 * @author  pgussow
 */
class RFCMetadataImpl extends AbstractTypeMetadata
    implements IRFCMetadata
{
    /**
     * Holds the name of the SAP tag 'FUNCNAME'.
     */
    private static final String TAG_SAP_FUNCTION = "FUNCNAME";
    /**
     * Holds the name of the SAP tag 'APPL'.
     */
    private static final String TAG_SAP_APPLICATION = "APPL";
    /**
     * Holds the name of the SAP tag 'GROUPNAME'.
     */
    private static final String TAG_SAP_GROUP_NAME = "GROUPNAME";
    /**
     * Holds the name of the SAP tag 'HOST'.
     */
    private static final String TAG_SAP_HOST = "HOST";
    /**
     * Holds the name of the SAP tag 'STEXT'.
     */
    private static final String TAG_SAP_SHORT_TEXT = "STEXT";
    /**
     * Holds the name of the application.
     */
    private String m_application;
    /**
     * Holds the name of the group.
     */
    private String m_groupName;
    /**
     * Holds the name of the host.
     */
    private String m_host;
    /**
     * Holds the name of the RFC function.
     */
    private String m_rfcFunction;
    /**
     * Holds the short descriptive text.
     */
    private String m_shortText;

    /**
     * Creates a new RFCMetadataImpl object.
     */
    public RFCMetadataImpl()
    {
    }

    /**
     * Creates a new RFCMetadataImpl object.
     *
     * @param   node  The XML node to parse
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    public RFCMetadataImpl(int node, XPathMetaInfo xmi)
                    throws SAPConfigurationException
    {
        super(node, xmi);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#getApplication()
     */
    @Override public String getApplication()
    {
        return m_application;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#getGroupName()
     */
    @Override public String getGroupName()
    {
        return m_groupName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#getHost()
     */
    @Override public String getHost()
    {
        return m_host;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#getRFCFunction()
     */
    @Override public String getRFCFunction()
    {
        return m_rfcFunction;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#getShortText()
     */
    @Override public String getShortText()
    {
        return m_shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#parseSAPItem(int)
     */
    @Override public void parseSAPItem(int itemDetails)
                                throws SAPConfigurationException
    {
        if (itemDetails == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.MISSING_RFC_DATA);
        }

        // Get the method ID
        m_rfcFunction = XPathHelper.getStringValue(itemDetails, TAG_SAP_FUNCTION, "");
        setValue(m_rfcFunction);
        setDisplayName(m_rfcFunction);

        m_application = XPathHelper.getStringValue(itemDetails, TAG_SAP_APPLICATION, "");
        m_groupName = XPathHelper.getStringValue(itemDetails, TAG_SAP_GROUP_NAME, "");
        m_host = XPathHelper.getStringValue(itemDetails, TAG_SAP_HOST, "");
        m_shortText = XPathHelper.getStringValue(itemDetails, TAG_SAP_SHORT_TEXT, "");

        // Get the RFC function.

        // Make sure the object is valid.
        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#setApplication(java.lang.String)
     */
    @Override public void setApplication(String application)
    {
        m_application = application;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#setGroupName(java.lang.String)
     */
    @Override public void setGroupName(String groupName)
    {
        m_groupName = groupName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#setHost(java.lang.String)
     */
    @Override public void setHost(String host)
    {
        m_host = host;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#setRFCFunction(java.lang.String)
     */
    @Override public void setRFCFunction(String rfcFunction)
    {
        m_rfcFunction = rfcFunction;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata#setShortText(java.lang.String)
     */
    @Override public void setShortText(String shortText)
    {
        m_shortText = shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#validate()
     */
    @Override public void validate()
                            throws SAPConfigurationException
    {
        if (!Util.isSet(m_rfcFunction))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IRFCMetadata.TAG_ROOT + "/" + TAG_FUNCTION);
        }

        if (!Util.isSet(m_groupName))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IRFCMetadata.TAG_ROOT + "/" + TAG_GROUP_NAME);
        }
    }

    /**
     * @see  java.lang.Object#clone()
     */
    @Override protected Object clone()
                              throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#fillClone(com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata)
     */
    @Override protected void fillClone(ITypeMetadata clone)
    {
        RFCMetadataImpl rmi = (RFCMetadataImpl) clone;

        rmi.m_application = m_application;
        rmi.m_groupName = m_groupName;
        rmi.m_host = m_host;
        rmi.m_rfcFunction = m_rfcFunction;
        rmi.m_shortText = m_shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#parseXML(int, com.eibus.xml.xpath.XPathMetaInfo)
     */
    @Override protected void parseXML(int detail, XPathMetaInfo xmi)
                               throws SAPConfigurationException
    {
        int idoc = XPathHelper.selectSingleNode(detail, "ns:" + IRFCMetadata.TAG_ROOT, xmi);

        if (idoc == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IRFCMetadata.TAG_ROOT);
        }

        // Get the type.
        m_rfcFunction = XPathHelper.getStringValue(idoc, "ns:" + TAG_FUNCTION, xmi, "");

        // Get the group name
        m_groupName = XPathHelper.getStringValue(idoc, "ns:" + TAG_GROUP_NAME, xmi, "");

        // Get the rest of the optional fields.
        m_application = XPathHelper.getStringValue(idoc, "ns:" + TAG_APPLICATION, xmi, "");
        m_host = XPathHelper.getStringValue(idoc, "ns:" + TAG_HOST, xmi, "");
        m_shortText = XPathHelper.getStringValue(idoc, "ns:" + TAG_SHORT_TEXT, xmi, "");

        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#toXMLDetail(int)
     */
    @Override protected void toXMLDetail(int detail)
    {
        int idoc = Node.createElementWithParentNS(IRFCMetadata.TAG_ROOT, null, detail);

        if (Util.isSet(m_rfcFunction))
        {
            Node.createElementWithParentNS(TAG_FUNCTION, m_rfcFunction, idoc);
        }

        if (Util.isSet(m_groupName))
        {
            Node.createElementWithParentNS(TAG_GROUP_NAME, m_groupName, idoc);
        }

        if (Util.isSet(m_application))
        {
            Node.createElementWithParentNS(TAG_APPLICATION, m_application, idoc);
        }

        if (Util.isSet(m_host))
        {
            Node.createElementWithParentNS(TAG_HOST, m_host, idoc);
        }

        if (Util.isSet(m_shortText))
        {
            Node.createElementWithParentNS(TAG_SHORT_TEXT, m_shortText, idoc);
        }
    }
}
