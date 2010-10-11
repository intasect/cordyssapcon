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
package com.eibus.applicationconnector.sap.config;

import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

import com.eibus.soap.Processor;

/**
 * This factory creates a configuration object.
 *
 * @author  pgussow
 */
public class SAPConfigurationFactory
{
    /**
     * This method creates the configuration object based on an XML structure. The structure is
     * described in src\cws\SAP_R3_M_BOP4\Static Files\BAC\SAP\schemas\configuration.xsd
     *
     * @param   configuration  The configuration XML.
     * @param   processor      The parent Service provider.
     *
     * @return  The created configuration.
     *
     * @throws  SAPConfigurationException  In case the configuration is invalid.
     */
    public static ISAPConfiguration createSAPConfiguration(int configuration,
                                                           Processor processor)
                                                    throws SAPConfigurationException
    {
        return createSAPConfiguration(configuration, processor.getOrganization(),
                                      processor.getSOAPNode());
    }

    /**
     * This method creates the configuration object based on an XML structure. The structure is
     * described in src\cws\SAP_R3_M_BOP4\Static Files\BAC\SAP\schemas\configuration.xsd
     *
     * @param   configuration   The configuration XML.
     * @param   organization    The DN of the organization.
     * @param   serviceGroupDN  The DN of the service group.
     *
     * @return  The created configuration.
     *
     * @throws  SAPConfigurationException  In case the configuration is invalid.
     */
    public static ISAPConfiguration createSAPConfiguration(int configuration, String organization,
                                                           String serviceGroupDN)
                                                    throws SAPConfigurationException
    {
        try
        {
            return new ProcessorConfig(configuration, organization, serviceGroupDN);
        }
        catch (SAPConnectorException e)
        {
            throw new SAPConfigurationException(e, e.getMessageObject(), e.getMessageParameters());
        }
    }
}
