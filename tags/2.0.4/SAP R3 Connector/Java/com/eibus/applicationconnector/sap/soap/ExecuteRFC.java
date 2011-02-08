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

/**
 * This method will execute the given RFC.
 *
 * @author  pgussow
 */
public class ExecuteRFC extends BaseMethod
{
    /**
     * Constructor.
     *
     * @param  request   The request bodyblock.
     * @param  response  The response bodyblock.
     * @param  config    The configuration of the connector.
     */
    public ExecuteRFC(BodyBlock request, BodyBlock response, ISAPConfiguration config)
    {
        super(request, response, config);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.soap.BaseMethod#execute()
     */
    @Override public void execute()
                           throws SAPConnectorException
    {
    }
}
