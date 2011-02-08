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
package com.eibus.applicationconnector.sap.exception;

import com.cordys.coe.exception.ServerLocalizableException;

import com.eibus.localization.IStringResource;

/**
 * This exception is thrown in case of configuration exceptions.
 *
 * @author  pgussow
 */
public class SAPConfigurationException extends ServerLocalizableException
{
    /**
     * Creates a new SAPConfigurationException object.
     *
     * @param  srMessage     The message for the exception.
     * @param  aoParameters  The parameters for the message.
     */
    public SAPConfigurationException(IStringResource srMessage, Object... aoParameters)
    {
        super(srMessage, aoParameters);
    }

    /**
     * Creates a new SAPConfigurationException object.
     *
     * @param  tCause        Holds the root cause exception.
     * @param  srMessage     The message for the exception.
     * @param  aoParameters  The parameters for the message.
     */
    public SAPConfigurationException(Throwable tCause, IStringResource srMessage,
                                     Object... aoParameters)
    {
        super(tCause, srMessage, aoParameters);
    }
}
