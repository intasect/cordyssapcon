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

import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;

import com.eibus.xml.nom.Document;

import java.util.Map;

/**
 * This interface holds the description for a type container.
 *
 * @author  pgussow
 */
public interface ITypeContainer extends Cloneable
{
    /**
     * Holds the name of the XML tag for the 'description' field.
     */
    String TAG_DESCRIPTION = "description";
    /**
     * Holds the name of the XML tag for the 'displayname' field.
     */
    String TAG_DISPLAY_NAME = "displayname";
    /**
     * Holds the name of the XML tag for the 'item' field.
     */
    String TAG_ITEM = "item";
    /**
     * Holds the name of the XML tag for the 'items' field.
     */
    String TAG_ITEMS = "items";
    /**
     * Holds the name of the XML tag for the 'detail' field.
     */
    String TAG_ROOT = "sapobject";
    /**
     * Holds the name of the XML tag for the 'type' field.
     */
    String TAG_TYPE = "type";
    /**
     * Holds the name of the XML tag for the 'value' field.
     */
    String TAG_VALUE = "value";

    /**
     * This method adds the given type to the list of items.
     *
     * @param  type  The type to add.
     */
    void addType(ITypeMetadata type);

    /**
     * This method copies the object.
     *
     * @return  A copy of the object.
     */
    ITypeContainer copy();

    /**
     * This method gets the description for the container.
     *
     * @return  The description for the container.
     */
    String getDescription();

    /**
     * This method gets the display name for the item.
     *
     * @return  The display name for the item.
     */
    String getDisplayName();

    /**
     * This method gets the items in this container.
     *
     * @return  The items in this container.
     */
    Map<String, ITypeMetadata> getItems();

    /**
     * This method gets the type of objects in this container.
     *
     * @return  The type of objects in this container.
     */
    ESAPObjectType getType();

    /**
     * This method gets the differentiating value for the item.
     *
     * @return  The differentiating value for the item.
     */
    String getValue();

    /**
     * This method sets the description for the container.
     *
     * @param  description  The description for the container.
     */
    void setDescription(String description);

    /**
     * This method sets the display name for the item.
     *
     * @param  displayName  The display name for the item.
     */
    void setDisplayName(String displayName);

    /**
     * This method sets the type of objects in this container.
     *
     * @param  type  The type of objects in this container.
     */
    void setType(ESAPObjectType type);

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
     * @return  The created container XML.
     */
    int toXML(Document doc);
}
