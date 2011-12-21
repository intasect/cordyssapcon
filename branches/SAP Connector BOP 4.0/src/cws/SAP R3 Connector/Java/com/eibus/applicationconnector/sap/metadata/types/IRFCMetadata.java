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

/**
 * This interface holds the definition of data for the RFCs.
 *
 * @author  pgussow
 */
public interface IRFCMetadata extends ITypeMetadata
{
    /**
     * Holds the name of the XML tag for the 'application' field.
     */
    String TAG_APPLICATION = "application";
    /**
     * Holds the name of the XML tag for the 'function' field.
     */
    String TAG_FUNCTION = "function";
    /**
     * Holds the name of the XML tag for the 'groupname' field.
     */
    String TAG_GROUP_NAME = "groupname";
    /**
     * Holds the name of the XML tag for the 'host' field.
     */
    String TAG_HOST = "host";
    /**
     * Holds the root tag for this metadata type.
     */
    String TAG_ROOT = "rfc";
    /**
     * Holds the name of the XML tag for the 'stext' field.
     */
    String TAG_SHORT_TEXT = "stext";

    /**
     * This method gets the name of the application.
     *
     * @return  The name of the application.
     */
    String getApplication();

    /**
     * This method gets the group to which this function belongs.
     *
     * @return  The group to which this function belongs.
     */
    String getGroupName();

    /**
     * This method gets the name of the host for this RFC.
     *
     * @return  The name of the host for this RFC.
     */
    String getHost();

    /**
     * This method gets the name of the RFC function.
     *
     * @return  The name of the RFC function.
     */
    String getRFCFunction();

    /**
     * This method gets the short text for the method.
     *
     * @return  The short text for the method.
     */
    String getShortText();

    /**
     * This method sets the name of the application.
     *
     * @param  application  The name of the application.
     */
    void setApplication(String application);

    /**
     * This method sets the group to which this function belongs.
     *
     * @param  groupName  The group to which this function belongs.
     */
    void setGroupName(String groupName);

    /**
     * This method sets the name of the host for this RFC.
     *
     * @param  host  The name of the host for this RFC.
     */
    void setHost(String host);

    /**
     * This method sets the name of the RFC function.
     *
     * @param  rfcFunction  The name of the RFC function.
     */
    void setRFCFunction(String rfcFunction);

    /**
     * This method sets the short text for the method.
     *
     * @param  shortText  The short text for the method.
     */
    void setShortText(String shortText);
}
