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
 * This interface describes the metadata for the given IDOC.
 *
 * @author  pgussow
 */
public interface IIDOCMetadata extends ITypeMetadata
{
    /**
     * Holds the name of the XML tag for the 'cimtype' field.
     */
    String TAG_CIM_TYPE = "cimtype";
    /**
     * Holds the name of the XML tag for the 'released' field.
     */
    String TAG_RELEASED = "released";
    /**
     * Holds the root tag for this metadata type.
     */
    String TAG_ROOT = "idoc";
    /**
     * Holds the name of the XML tag for the 'type' field.
     */
    String TAG_TYPE = "type";

    /**
     * This method gets the cim type.
     *
     * @return  The cim type.
     */
    String getCIMType();

    /**
     * This method gets the release data for the IDOC.
     *
     * @return  The release data for the IDOC.
     */
    String getReleased();

    /**
     * This method gets the type.
     *
     * @return  The type.
     */
    String getType();

    /**
     * This method sets the cim type.
     *
     * @param  cimType  The cim type.
     */
    void setCIMType(String cimType);

    /**
     * This method sets the release data for the IDOC.
     *
     * @param  released  The release data for the IDOC.
     */
    void setReleased(String released);

    /**
     * This method sets the type.
     *
     * @param  type  The type.
     */
    void setType(String type);
}
