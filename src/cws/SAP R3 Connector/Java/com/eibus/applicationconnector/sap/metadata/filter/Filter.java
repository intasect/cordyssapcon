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
 package com.eibus.applicationconnector.sap.metadata.filter;

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.regex.Pattern;

/**
 * This class holds the details for a filter.
 *
 * @author  pgussow
 */
class Filter
    implements IFilter
{
    /**
     * Holds the name of the tag 'type'.
     */
    private static final String TAG_TYPE = "type";
    /**
     * Holds the name of the tag 'value'.
     */
    private static final String TAG_VALUE = "value";
    /**
     * Holds the name of the tag 'fieldname'.
     */
    private static final String TAG_FIELD_NAME = "fieldname";
    /**
     * Holds the name of the tag 'filter'.
     */
    private static final String TAG_ROOT = "filter";
    /**
     * Holds the name of the field for the filter.
     */
    private String m_fieldName;
    /**
     * Holds the filter type.
     */
    private EFilterType m_type;
    /**
     * Holds the value for the filter.
     */
    private String m_value;

    /**
     * Creates a new Filter object.
     */
    Filter()
    {
    }

    /**
     * Creates a new Filter object.
     *
     * @param   node  The definition for the filter.
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    Filter(int node, XPathMetaInfo xmi)
        throws SAPConnectorException
    {
        String type = XPathHelper.getStringValue(node, "ns:" + TAG_TYPE, xmi, "");

        if (!Util.isSet(type))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_TYPE);
        }

        try
        {
            m_type = EFilterType.valueOf(type.toUpperCase());
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.INVALID_FILTER_TYPE,
                                            type);
        }

        m_value = XPathHelper.getStringValue(node, "ns:" + TAG_VALUE, xmi, "");

        if (!Util.isSet(m_value))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_TAG, TAG_VALUE);
        }

        m_fieldName = XPathHelper.getStringValue(node, "ns:" + TAG_FIELD_NAME, xmi, "");
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#getFieldName()
     */
    public String getFieldName()
    {
        return m_fieldName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#getType()
     */
    public EFilterType getType()
    {
        return m_type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#getValue()
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#match(java.lang.String)
     */
    @Override public boolean match(String source)
    {
        String regex = null;

        switch (m_type)
        {
            case CONTAINS:
                regex = ".*" + m_value + ".*";
                break;

            case STARTS_WITH:
                regex = "^" + m_value + ".+";
                break;

            case EQUALS:
                regex = "^" + m_value + "$";

            default:
                regex = m_value;
                break;
        }

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        return pattern.matcher(source).matches();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#setFieldName(java.lang.String)
     */
    public void setFieldName(String fieldName)
    {
        m_fieldName = fieldName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#setType(com.eibus.applicationconnector.sap.metadata.filter.EFilterType)
     */
    public void setType(EFilterType type)
    {
        m_type = type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        m_value = value;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#toXML(com.eibus.xml.nom.Document)
     */
    @Override public int toXML(Document doc)
    {
        int returnValue = doc.createElementNS(TAG_ROOT, null, "",
                                              SAPConnectorConstants.NS_SAP_SCHEMA, 0);

        toXML(returnValue);

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.filter.IFilter#toXML(int)
     */
    @Override public void toXML(int parent)
    {
        Node.createElementWithParentNS(TAG_TYPE, m_type.name().toLowerCase(), parent);

        Node.createElementWithParentNS(TAG_VALUE, m_value, parent);

        if (Util.isSet(m_fieldName))
        {
            Node.createElementWithParentNS(TAG_FIELD_NAME, m_fieldName, parent);
        }
    }
}
