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

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class is the base class for handling specific methods.
 *
 * @author  pgussow
 */
public abstract class BaseMethod
{
    /**
     * Holds the configuration of the connector.
     */
    private ISAPConfiguration m_config;
    /**
     * Holds the request bodyblock.
     */
    private BodyBlock m_request;
    /**
     * Holds the response bodyblock.
     */
    private BodyBlock m_response;
    /**
     * Holds the namespace/prefix mappings.
     */
    protected XPathMetaInfo m_xmi = new XPathMetaInfo();

    /**
     * Constructor.
     *
     * @param  request   The request bodyblock.
     * @param  response  The response bodyblock.
     * @param  config    The configuration of the connector.
     */
    public BaseMethod(BodyBlock request, BodyBlock response, ISAPConfiguration config)
    {
        m_request = request;
        m_response = response;
        m_config = config;

        // Add the ns prefix to the object. ns holds the namespace of the current method.
        m_xmi.addNamespaceBinding("ns", Node.getNamespaceURI(request.getXMLNode()));
    }

    /**
     * This method executed the requested SOAP method.
     *
     * @throws  SAPConnectorException  In case of any processing errors.
     */
    public abstract void execute()
                          throws SAPConnectorException;

    /**
     * This method gets the configuration for the connector.
     *
     * @return  The configuration for the connector.
     */
    public ISAPConfiguration getConfiguration()
    {
        return m_config;
    }

    /**
     * This method gets the request bodyblock.
     *
     * @return  The request bodyblock.
     */
    public BodyBlock getRequest()
    {
        return m_request;
    }

    /**
     * This method gets the response bodyblock.
     *
     * @return  The response bodyblock.
     */
    public BodyBlock getResponse()
    {
        return m_response;
    }
}
