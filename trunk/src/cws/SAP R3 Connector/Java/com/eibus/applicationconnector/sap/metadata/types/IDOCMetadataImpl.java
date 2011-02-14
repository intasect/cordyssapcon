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
 * This class implements the.
 *
 * @author  pgussow
 */
class IDOCMetadataImpl extends AbstractTypeMetadata
    implements IIDOCMetadata
{
    /**
     * Holds the name of the SAP tag 'IDOCTYPE'.
     */
    private static final String TAG_SAP_IDOCTYPE = "IDOCTYP";
    /**
     * Holds the name of the SAP tag 'CIMTYPE'.
     */
    private static final String TAG_SAP_CIM_TYPE = "CIMTYPE";
    /**
     * Holds the name of the SAP tag 'RELEASED'.
     */
    private static final String TAG_SAP_RELEASED = "RELEASED";
    /**
     * Holds the CIM type.
     */
    private String m_cimType;
    /**
     * Holds the released data.
     */
    private String m_released;
    /**
     * Holds the IDOC type.
     */
    private String m_type;

    /**
     * Creates a new IDOCMetadataImpl object.
     */
    public IDOCMetadataImpl()
    {
    }

    /**
     * Creates a new IDOCMetadataImpl object.
     *
     * @param   node  The XML node to parse
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    public IDOCMetadataImpl(int node, XPathMetaInfo xmi)
                     throws SAPConfigurationException
    {
        super(node, xmi);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#getCIMType()
     */
    @Override public String getCIMType()
    {
        return m_cimType;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#getDisplayName()
     */
    @Override public String getDisplayName()
    {
        return null;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#getReleased()
     */
    @Override public String getReleased()
    {
        return m_released;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#getType()
     */
    @Override public String getType()
    {
        return m_type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#parseSAPItem(int)
     */
    @Override public void parseSAPItem(int itemDetails)
                                throws SAPConfigurationException
    {
        if (itemDetails == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.MISSING_BAPI_DATA);
        }

        // Get the IDOC type
        m_type = XPathHelper.getStringValue(itemDetails, TAG_SAP_IDOCTYPE, "");
        setValue(m_type);
        setDisplayName(m_type);

        // Get the CIM type.
        m_cimType = XPathHelper.getStringValue(itemDetails, TAG_SAP_CIM_TYPE, "");

        // Get the RFC function.
        m_released = XPathHelper.getStringValue(itemDetails, TAG_SAP_RELEASED, "");

        // Make sure the object is valid.
        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#setCIMType(java.lang.String)
     */
    @Override public void setCIMType(String cimType)
    {
        m_cimType = cimType;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#setReleased(java.lang.String)
     */
    @Override public void setReleased(String released)
    {
        m_released = released;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata#setType(java.lang.String)
     */
    @Override public void setType(String type)
    {
        m_type = type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#validate()
     */
    @Override public void validate()
                            throws SAPConfigurationException
    {
        if (!Util.isSet(m_type))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IIDOCMetadata.TAG_ROOT + "/" + TAG_TYPE);
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
        IDOCMetadataImpl obj = (IDOCMetadataImpl) clone;

        obj.m_cimType = m_cimType;
        obj.m_released = m_released;
        obj.m_type = m_type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#parseXML(int, com.eibus.xml.xpath.XPathMetaInfo)
     */
    @Override protected void parseXML(int detail, XPathMetaInfo xmi)
                               throws SAPConfigurationException
    {
        int idoc = XPathHelper.selectSingleNode(detail, "ns:" + IIDOCMetadata.TAG_ROOT, xmi);

        if (idoc == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IIDOCMetadata.TAG_ROOT);
        }

        // Get the type.
        m_type = XPathHelper.getStringValue(idoc, "ns:" + TAG_TYPE, xmi, "");

        // Get the released information
        m_released = XPathHelper.getStringValue(idoc, "ns:" + TAG_RELEASED, xmi, "");

        // Get the CIM type
        m_cimType = XPathHelper.getStringValue(idoc, "ns:" + TAG_RELEASED, xmi, "");

        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#toXMLDetail(int)
     */
    @Override protected void toXMLDetail(int detail)
    {
        int idoc = Node.createElementWithParentNS(IIDOCMetadata.TAG_ROOT, null, detail);

        if (Util.isSet(m_type))
        {
            Node.createElementWithParentNS(TAG_TYPE, m_type, idoc);
        }

        if (Util.isSet(m_cimType))
        {
            Node.createElementWithParentNS(TAG_CIM_TYPE, m_cimType, idoc);
        }

        if (Util.isSet(m_released))
        {
            Node.createElementWithParentNS(TAG_RELEASED, m_released, idoc);
        }
    }
}
