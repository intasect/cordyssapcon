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
 package com.eibus.applicationconnector.sap;

import java.io.File;

/**
 * This class holds constants used within the connector.
 *
 * @author  pgussow
 */
public class SAPConnectorConstants
{
    /**
     * Holds the namespace that identifies the configuration of the connector.
     */
    public static final String NS_SAP_CONFIG = "http://sapconnector.cordys.com/2.0/configuration";
    /**
     * Holds the namespace that is used for LDAP method sets.
     */
    public static final String NS_LDAP = "http://schemas.cordys.com/1.0/ldap";
    /**
     * Holds the namespace that is used for IDOC table.
     */
    public static final String NS_SAP_IDOC_DB = "http://schemas.cordys.com/sap/IDOCTable";
    /**
     * Holds the namespace that is used for the XML store.
     */
    public static final String NS_XML_STORE = "http://schemas.cordys.com/1.0/xmlstore";
    /**
     * Holds the deployment folder.
     */
    public static final String DEPLOY_PATH = "BAC" + File.separator + "SAP";
    /**
     * Holds the namespace for the schema method set.
     */
    public static final String NS_SAP_SCHEMA = "http://schemas.cordys.com/1.0/SAPSchema";
    /**
     * Holds the namespace for the schema method set 2.0.
     */
    public static final String NS_SAP_SCHEMA_2 = "http://schemas.cordys.com/2.0/SAPSchema";
    /**
     * Holds the namespace for the decription of the implementations.
     */
    public static final String NS_SAP_IMPLEMENTATION = "http://schemas.cordys.com/2.0/implementation";
    /**
     * Holds the name of the IDOC table.
     */
    public static final String IDOC_TABLE_NAME = "IDOCTABLE";
    /**
     * Holds the internal cache folder.
     */
    public static final String ROOT_CACHE_FOLDER = DEPLOY_PATH + File.separator + "cache";
}
