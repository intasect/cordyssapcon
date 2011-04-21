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

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory can create filters.
 *
 * @author  pgussow
 */
public class FilterFactory
{
    /**
     * This method creates the filter object based on the given details.
     *
     * @param   value  The value for the filter
     *
     * @return  The created filter.
     */
    public static IFilter createContainsFilter(String value)
    {
        Filter returnValue = new Filter();

        returnValue.setType(EFilterType.CONTAINS);
        returnValue.setValue(value);

        return returnValue;
    }

    /**
     * This method creates the filter object based on the given details.
     *
     * @param   value  The value for the filter
     *
     * @return  The created filter.
     */
    public static IFilter createEqualsFilter(String value)
    {
        Filter returnValue = new Filter();

        returnValue.setType(EFilterType.EQUALS);
        returnValue.setValue(value);

        return returnValue;
    }

    /**
     * This method creates the filter object based on the given details.
     *
     * @param   type       The filter type.
     * @param   value      The value for the filter
     * @param   fieldName  The name of the field.
     *
     * @return  The created filter.
     */
    public static IFilter createFilter(EFilterType type, String value, String fieldName)
    {
        Filter returnValue = new Filter();

        returnValue.setFieldName(fieldName);
        returnValue.setType(type);
        returnValue.setValue(value);

        return returnValue;
    }

    /**
     * This method creates the filter object based on the given details.
     *
     * @param   value  The value for the filter
     *
     * @return  The created filter.
     */
    public static IFilter createRegexFilter(String value)
    {
        Filter returnValue = new Filter();

        returnValue.setType(EFilterType.REGEX);
        returnValue.setValue(value);

        return returnValue;
    }

    /**
     * This method creates the filter object based on the given details.
     *
     * @param   value  The value for the filter
     *
     * @return  The created filter.
     */
    public static IFilter createStartsWithFilter(String value)
    {
        Filter returnValue = new Filter();

        returnValue.setType(EFilterType.STARTS_WITH);
        returnValue.setValue(value);

        return returnValue;
    }

    /**
     * This method parses the XML definition for a filter. The xmi needs to have the prefix ns
     * mapped to the actual namespace.
     *
     * @param   filter  The filter to parse.
     * @param   xmi     The namespace/prefix mappings.
     *
     * @return  The parsed filter.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static IFilter parseFilter(int filter, XPathMetaInfo xmi)
                               throws SAPConnectorException
    {
        return new Filter(filter, xmi);
    }
}
