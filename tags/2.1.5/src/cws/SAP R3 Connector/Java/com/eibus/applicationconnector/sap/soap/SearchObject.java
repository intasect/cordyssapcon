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
 package com.eibus.applicationconnector.sap.soap;

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;
import com.eibus.applicationconnector.sap.metadata.filter.FilterFactory;
import com.eibus.applicationconnector.sap.metadata.filter.IFilter;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Node;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This method handles all searches on the metadata.
 *
 * @author  pgussow
 */
public class SearchObject extends BaseMethod
{
    /**
     * Constructor.
     *
     * @param  request   The request bodyblock.
     * @param  response  The response bodyblock.
     * @param  config    The configuration of the connector.
     */
    public SearchObject(BodyBlock request, BodyBlock response, ISAPConfiguration config)
    {
        super(request, response, config);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.soap.BaseMethod#execute()
     */
    @Override public void execute()
                           throws SAPConnectorException
    {
        int xmlRequest = getRequest().getXMLNode();

        // Make sure the information passed on in correct
        validateRequest(xmlRequest);

        // Get the parameter values
        String temp = XPathHelper.getStringValue(xmlRequest, "ns:type", m_xmi, "");
        ESAPObjectType type = ESAPObjectType.valueOf(temp.toUpperCase());
        boolean readFromSAP = XPathHelper.getBooleanValue(xmlRequest, "ns:read_from_sap", m_xmi,
                                                          false);

        // Get the filter implementations from the request and instantiate them.
        Map<String, IFilter> filters = new LinkedHashMap<String, IFilter>();
        int[] xmlFilters = XPathHelper.selectNodes(xmlRequest, "ns:filters/ns:filter", m_xmi);

        for (int xmlFilter : xmlFilters)
        {
            IFilter filter = FilterFactory.parseFilter(xmlFilter, m_xmi);

            // The filters must have the field name set.
            if (!Util.isSet(filter.getFieldName()))
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.THE_FILTER_IS_MISSING_THE_FIELD_NAME,
                                                filter.getType(), filter.getValue());
            }

            filters.put(filter.getFieldName(), filter);
        }

        // Based on the type the filters have different names. Now we'll execute the actual
        // searches.
        IMetadataCache cache = getConfiguration().getMetadataCache();

        List<ITypeContainer> list = null;

        switch (type)
        {
            case BAPI:
                list = cache.searchBAPI(readFromSAP, filters.get("object"), filters.get("method"),
                                        filters.get("description"));
                break;

            case IDOC:
                list = cache.searchIDOC(readFromSAP, filters.get("messageType"),
                                        filters.get("operation"), filters.get("description"));
                break;

            case RFC:
                list = cache.searchRFC(readFromSAP, filters.get("function"), filters.get("group"),
                                       filters.get("description"));

                break;
        }

        // Now the list needs to be returned.
        if ((list != null) && (list.size() > 0))
        {
            int xmlResponse = getResponse().getXMLNode();

            for (ITypeContainer typeContainer : list)
            {
                int xmlTuple = Node.createElementWithParentNS("tuple", null, xmlResponse);
                int xmlOld = Node.createElementWithParentNS("old", null, xmlTuple);
                int xmlSAPObject = Node.createElementWithParentNS("sapobject", null, xmlOld);

                typeContainer.toXML(xmlSAPObject);
            }
        }
    }

    /**
     * This method validates the input tyo make sure all information is there.
     *
     * @param   iRequest  The request XML.
     *
     * @throws  SAPConnectorException  In case the request is invalid.
     */
    private void validateRequest(int iRequest)
                          throws SAPConnectorException
    {
        if (XPathHelper.selectSingleNode(iRequest, "ns:type", m_xmi) == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_PARAMETER_TYPE);
        }

        if (XPathHelper.selectSingleNode(iRequest, "ns:read_from_sap", m_xmi) == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.MISSING_PARAMETER_READ_FROM_SAP);
        }
    }
}
