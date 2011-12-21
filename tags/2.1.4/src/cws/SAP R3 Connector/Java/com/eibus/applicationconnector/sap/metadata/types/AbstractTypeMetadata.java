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

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This base class holds shared functionality for the different types.
 *
 * @author  pgussow
 */
abstract class AbstractTypeMetadata
    implements ITypeMetadata
{
    /**
     * holds the display name for this type.
     */
    private String m_displayName;
    /**
     * Holds the value for this type.
     */
    private String m_value;

    /**
     * Creates a new AbstractTypeMetadata object.
     */
    public AbstractTypeMetadata()
    {
    }

    /**
     * Creates a new AbstractTypeMetadata object.
     *
     * @param   node  The XML node to parse
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    public AbstractTypeMetadata(int node, XPathMetaInfo xmi)
                         throws SAPConfigurationException
    {
        m_displayName = XPathHelper.getStringValue(node, "ns:" + TAG_DISPLAY_NAME, xmi, "");
        m_value = XPathHelper.getStringValue(node, "ns:" + TAG_VALUE, xmi, "");

        int detail = XPathHelper.selectSingleNode(node, "ns:" + TAG_DETAIL, xmi);

        parseXML(detail, xmi);
    }

    /**
     * This method validates the data. If something is wrong, the exception is thrown.
     *
     * @throws  SAPConfigurationException  In case the object is not valid.
     */
    public abstract void validate()
                           throws SAPConfigurationException;

    /**
     * @see  ITypeMetadata#copy()
     */
    @Override public ITypeMetadata copy()
    {
        ITypeMetadata returnValue = null;

        try
        {
            returnValue = getClass().newInstance();

            returnValue.setDisplayName(m_displayName);
            returnValue.setValue(m_value);

            fillClone(returnValue);
        }
        catch (Exception e)
        {
            // This will never happen, since the default constructor must be there.
        }

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#getDisplayName()
     */
    @Override public String getDisplayName()
    {
        return m_displayName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#getValue()
     */
    @Override public String getValue()
    {
        return m_value;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#setDisplayName(java.lang.String)
     */
    @Override public void setDisplayName(String displayName)
    {
        m_displayName = displayName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#setValue(java.lang.String)
     */
    @Override public void setValue(String value)
    {
        m_value = value;
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override public String toString()
    {
        return m_value + "(" + m_displayName + ")";
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#toXML(com.eibus.xml.nom.Document)
     */
    @Override public int toXML(Document doc)
    {
        int returnValue = doc.createElementNS(TAG_ROOT, null, "ns",
                                              SAPConnectorConstants.NS_SAP_SCHEMA, 0);

        toXML(returnValue);

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#toXML(int)
     */
    @Override public void toXML(int parent)
    {
        if (Util.isSet(m_displayName))
        {
            Node.createElementWithParentNS(TAG_DISPLAY_NAME, m_displayName, parent);
        }

        if (Util.isSet(m_value))
        {
            Node.createElementWithParentNS(TAG_VALUE, m_value, parent);
        }

        int detail = Node.createElementWithParentNS(TAG_DETAIL, null, parent);

        toXMLDetail(detail);
    }

    /**
     * This method must fill the cloned object with the proper data.
     *
     * @param  clone  The clone to fill.
     */
    protected abstract void fillClone(ITypeMetadata clone);

    /**
     * This method should parse the XML for the details.
     *
     * @param   detail  The details node.
     * @param   xmi     The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions
     */
    protected abstract void parseXML(int detail, XPathMetaInfo xmi)
                              throws SAPConfigurationException;

    /**
     * This method creates the XML details for the rest of the information.
     *
     * @param  detail  The detail node.
     */
    protected abstract void toXMLDetail(int detail);
}
