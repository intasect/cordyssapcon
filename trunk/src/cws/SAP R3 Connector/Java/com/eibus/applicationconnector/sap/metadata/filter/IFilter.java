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

import com.eibus.xml.nom.Document;

/**
 * This interface describes the filter that can be used.
 *
 * @author  pgussow
 */
public interface IFilter
{
    /**
     * This method gets the name of the field for the filter.
     *
     * @return  The name of the field for the filter.
     */
    String getFieldName();

    /**
     * This method gets the filter type.
     *
     * @return  The filter type.
     */
    EFilterType getType();

    /**
     * This method gets the value for the filter.
     *
     * @return  The value for the filter.
     */
    String getValue();

    /**
     * This method returns whether or not the given value matches the filter.
     *
     * @param   source  The string to check.
     *
     * @return  Whether or not the given value matches the filter.
     */
    boolean match(String source);

    /**
     * This method sets the name of the field for the filter.
     *
     * @param  fieldName  The name of the field for the filter.
     */
    void setFieldName(String fieldName);

    /**
     * This method sets the filter type.
     *
     * @param  type  The filter type.
     */
    void setType(EFilterType type);

    /**
     * This method sets the value for the filter.
     *
     * @param  value  The value for the filter.
     */
    void setValue(String value);

    /**
     * This method dumps the object to XML.
     *
     * @param  parent  The parent element to create it under.
     */
    void toXML(int parent);

    /**
     * This method dumps the object to XML.
     *
     * @param   doc  The document to use.
     *
     * @return  The created container XML.
     */
    int toXML(Document doc);
}
