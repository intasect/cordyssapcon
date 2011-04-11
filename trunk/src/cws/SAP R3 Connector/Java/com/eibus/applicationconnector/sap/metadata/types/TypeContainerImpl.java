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
import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements the type container.
 *
 * @author  pgussow
 */
class TypeContainerImpl
    implements ITypeContainer
{
    /**
     * Holds all items in this list.
     */
    Map<String, ITypeMetadata> m_items = new LinkedHashMap<String, ITypeMetadata>();
    /**
     * Holds the description of the container.
     */
    private String m_description;
    /**
     * Holds the display name for the container.
     */
    private String m_displayName;
    /**
     * Holds the object types of the items in this container.
     */
    private ESAPObjectType m_type;
    /**
     * Holds the value for the container (basically the name).
     */
    private String m_value;

    /**
     * Creates a new TypeContainerImpl object.
     */
    public TypeContainerImpl()
    {
    }

    /**
     * Creates a new TypeContainerImpl object.
     *
     * @param   node  The container root node.
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    public TypeContainerImpl(int node, XPathMetaInfo xmi)
                      throws SAPConfigurationException
    {
        m_displayName = XPathHelper.getStringValue(node, "ns:" + TAG_DISPLAY_NAME, xmi, "");
        m_value = XPathHelper.getStringValue(node, "ns:" + TAG_VALUE, xmi, "");
        m_description = XPathHelper.getStringValue(node, "ns:" + TAG_DESCRIPTION, xmi, "");

        String type = XPathHelper.getStringValue(node, "ns:" + TAG_TYPE, xmi, "");
        m_type = ESAPObjectType.valueOf(type.toUpperCase());

        int[] items = XPathHelper.selectNodes(node, "ns:" + TAG_ITEMS + "/ns:" + TAG_ITEM, xmi);

        for (int item : items)
        {
            ITypeMetadata metadata = SAPMetadataFactory.parseObject(m_type, item, xmi);

            addType(metadata);
        }
    }

    /**
     * @see  ITypeContainer#addType(ITypeMetadata)
     */
    public void addType(ITypeMetadata type)
    {
        synchronized (m_items)
        {
            if (!m_items.containsKey(type.getValue()))
            {
                m_items.put(type.getValue(), type);
            }
        }
    }

    /**
     * @see  ITypeContainer#copy()
     */
    @Override public ITypeContainer copy()
    {
        TypeContainerImpl returnValue = new TypeContainerImpl();

        returnValue.m_description = m_description;
        returnValue.m_displayName = m_displayName;
        returnValue.m_type = m_type;
        returnValue.m_value = m_value;

        for (ITypeMetadata type : m_items.values())
        {
            returnValue.addType(type.copy());
        }

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#getDescription()
     */
    @Override public String getDescription()
    {
        return m_description;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#getDisplayName()
     */
    @Override public String getDisplayName()
    {
        return m_displayName;
    }

    /**
     * @see  ITypeContainer#getItems()
     */
    public Map<String, ITypeMetadata> getItems()
    {
        return new LinkedHashMap<String, ITypeMetadata>(m_items);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#getType()
     */
    @Override public ESAPObjectType getType()
    {
        return m_type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#getValue()
     */
    @Override public String getValue()
    {
        return m_value;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#setDescription(java.lang.String)
     */
    @Override public void setDescription(String description)
    {
        m_description = description;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#setDisplayName(java.lang.String)
     */
    @Override public void setDisplayName(String displayName)
    {
        m_displayName = displayName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#setType(com.eibus.applicationconnector.sap.metadata.ESAPObjectType)
     */
    @Override public void setType(ESAPObjectType type)
    {
        m_type = type;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#setValue(java.lang.String)
     */
    @Override public void setValue(String value)
    {
        m_value = value;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#toXML(com.eibus.xml.nom.Document)
     */
    @Override public int toXML(Document doc)
    {
        int returnValue = doc.createElementNS(TAG_ROOT, null, "",
                                              SAPConnectorConstants.NS_SAP_SCHEMA, 0);

        toXML(returnValue);

        return returnValue;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeContainer#toXML(int)
     */
    @Override public void toXML(int parent)
    {
        Node.createElementWithParentNS(TAG_TYPE, m_type.name().toLowerCase(), parent);

        if (Util.isSet(m_displayName))
        {
            Node.createElementWithParentNS(TAG_DISPLAY_NAME, m_displayName, parent);
        }

        if (Util.isSet(m_value))
        {
            Node.createElementWithParentNS(TAG_VALUE, m_value, parent);
        }

        if (Util.isSet(m_description))
        {
            Node.createElementWithParentNS(TAG_DESCRIPTION, m_description, parent);
        }

        if (m_items.size() > 0)
        {
            int items = Node.createElementWithParentNS(TAG_ITEMS, null, parent);

            for (ITypeMetadata type : m_items.values())
            {
                int item = Node.createElementWithParentNS(TAG_ITEM, null, items);

                type.toXML(item);
            }
        }
    }
}
