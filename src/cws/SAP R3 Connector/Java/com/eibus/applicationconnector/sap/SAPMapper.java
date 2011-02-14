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

import com.cordys.coe.util.system.SystemInfo;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.config.SAPConfigurationFactory;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;

/**
 * This is the top most class of the application connector. This class extends the
 * ApplicationConnector class and implements the createTransaction method.
 *
 * @author  ygopal
 */
public class SAPMapper extends ApplicationConnector
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPMapper.class);
    /**
     * Holds the configuration for this connector.
     */
    private ISAPConfiguration m_config;

    /**
     * This method is called when the processor stops. It will close all open JCO connections.
     *
     * @param  processor  The current soap processor.
     *
     * @see    com.eibus.soap.ApplicationConnector#close(com.eibus.soap.Processor)
     */
    @Override public void close(Processor processor)
    {
        try
        {
            m_config.closeAllConnections();
            m_config.stopIDOCListeners();
            m_config.setRepository(null);
            m_config.setIDOCRepository(null);
            m_config.getTargetMappingFinder().clear();
        }
        catch (SAPConnectorException sf)
        {
            LOG.error(sf, Messages.ERROR_CLOSING_CONNECTOR);
        }
    }

    /**
     * @see  com.eibus.soap.ApplicationConnector#createTransaction(com.eibus.soap.SOAPTransaction)
     */
    @Override public ApplicationTransaction createTransaction(SOAPTransaction soapTransaction)
    {
        try
        {
            return (new SAPTransaction(m_config, soapTransaction));
        }
        catch (SAPConnectorException sce)
        {
            throw new IllegalStateException(sce.getLocalizedMessage(), sce);
        }
    }

    /**
     * This method returns the configuration for the current connector.
     *
     * @return  The configuration for the current connector.
     */
    public ISAPConfiguration getSAPConfiguration()
    {
        return m_config;
    }

    /**
     * This method called when the SOAP processor is started. This method creates a config object
     * and creates repositories if the middleware is JCo. parameter : SOAP Processor in which this
     * application connector is configured.
     *
     * @param  processor  The Service Provider holding the connector.
     *
     * @see    com.eibus.soap.ApplicationConnector#open(com.eibus.soap.Processor)
     */
    @Override public void open(Processor processor)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("System environment:\n" + SystemInfo.getSystemInformation());
        }

        int processorConfiguration = this.getConfiguration();

        try
        {
            // Create the configuration object
            m_config = SAPConfigurationFactory.createSAPConfiguration(processorConfiguration,
                                                                      processor);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Error starting processor", e);
        }
    }
}
