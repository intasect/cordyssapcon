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
 * This interface describes the available metadata for a BAPI.
 *
 * @author  pgussow
 */
public interface IBAPIMetadata extends ITypeMetadata
{
    /**
     * Holds the name of the XML tag for the 'apitype' field.
     */
    String TAG_API_TYPE = "apitype";
    /**
     * Holds the name of the XML tag for the 'classverb' field.
     */
    String TAG_CLASS_VERB = "classverb";
    /**
     * Holds the name of the XML tag for the 'description' field.
     */
    String TAG_DESCRIPTION = "description";
    /**
     * Holds the name of the XML tag for the 'method' field.
     */
    String TAG_METHOD = "method";
    /**
     * Holds the name of the XML tag for the 'methodname' field.
     */
    String TAG_METHOD_NAME = "methodname";
    /**
     * Holds the name of the XML tag for the 'function' field.
     */
    String TAG_RFC_FUNCTION = "function";
    /**
     * Holds the root tag for this metadata type.
     */
    String TAG_ROOT = "bapi";
    /**
     * Holds the name of the XML tag for the 'shorttext' field.
     */
    String TAG_SHORT_TEXT = "shorttext";

    /**
     * This method gets the API type.
     *
     * @return  The API type.
     */
    String getAPIType();

    /**
     * This method gets the class verb.
     *
     * @return  The class verb.
     */
    String getClassVerb();

    /**
     * This method gets the description of the method.
     *
     * @return  The description of the method.
     */
    String getDescription();

    /**
     * This method gets the method id.
     *
     * @return  The method id.
     */
    String getMethod();

    /**
     * This method gets the name of the method.
     *
     * @return  The name of the method.
     */
    String getMethodName();

    /**
     * This method gets the RFC function name.
     *
     * @return  The RFC function name.
     */
    String getRFCFuntion();

    /**
     * This method gets the short text for the method.
     *
     * @return  The short text for the method.
     */
    String getShortText();

    /**
     * This method sets the API type.
     *
     * @param  apiType  The API type.
     */
    void setAPIType(String apiType);

    /**
     * This method sets the class verb.
     *
     * @param  classVerb  The class verb.
     */
    void setClassVerb(String classVerb);

    /**
     * This method sets the description of the method.
     *
     * @param  description  The description of the method.
     */
    void setDescription(String description);

    /**
     * This method sets the method id.
     *
     * @param  methodid  The method id.
     */
    void setMethod(String methodid);

    /**
     * This method sets the name of the method.
     *
     * @param  methodName  The name of the method.
     */
    void setMethodName(String methodName);

    /**
     * This method sets the RFC function name.
     *
     * @param  rfcFuntion  The RFC function name.
     */
    void setRFCFuntion(String rfcFuntion);

    /**
     * This method sets the short text for the method.
     *
     * @param  shortText  The short text for the method.
     */
    void setShortText(String shortText);
}
