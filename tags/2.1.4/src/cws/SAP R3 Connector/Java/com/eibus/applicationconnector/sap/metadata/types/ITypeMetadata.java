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

import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;

import com.eibus.xml.nom.Document;

/**
 * This interface describes the SAP types possible.
 *
 * @author  pgussow
 */
public interface ITypeMetadata extends Cloneable
{
    /**
     * Holds the name of the XML tag for the 'detail' field.
     */
    String TAG_DETAIL = "detail";
    /**
     * Holds the name of the XML tag for the 'displayname' field.
     */
    String TAG_DISPLAY_NAME = "displayname";
    /**
     * Holds the name of the XML tag for the root.
     */
    String TAG_ROOT = "item";
    /**
     * Holds the name of the XML tag for the 'value' field.
     */
    String TAG_VALUE = "value";

    /**
     * This method copies the object.
     *
     * @return  A copy of the object.
     */
    ITypeMetadata copy();

    /**
     * This method gets the display name for the item.
     *
     * @return  The display name for the item.
     */
    String getDisplayName();

    /**
     * This method gets the differentiating value for the item.
     *
     * @return  The differentiating value for the item.
     */
    String getValue();

    /**
     * This method parses the object based on the SAP data.
     *
     * @param   itemDetails  The SAP XML describing the item.
     *
     * @throws  SAPConfigurationException  In case of any exceptions
     */
    void parseSAPItem(int itemDetails)
               throws SAPConfigurationException;

    /**
     * This method sets the display name for the item.
     *
     * @param  displayName  The display name for the item.
     */
    void setDisplayName(String displayName);

    /**
     * This method sets the differentiating value for the item.
     *
     * @param  value  The differentiating value for the item.
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
     * @return  The created XML.
     */
    int toXML(Document doc);
}
