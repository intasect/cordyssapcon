/**
 * Copyright 2009 Cordys R&D B.V. 
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
package com.cordys.test.connection;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.config.SAPConfigurationFactory;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnectionManager;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoMetadataLoader;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestHandler;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.util.BACUtil;

import com.eibus.util.logger.config.LoggerConfigurator;

import com.eibus.xml.nom.Document;

import java.io.File;

/**
 * This class tests the metadata store.
 *
 * @author  pgussow
 */
public class TestMetadata
{
    /**
     * Holds the configuration.
     */
    private ISAPConfiguration m_config;
    /**
     * Holds the NOM document.
     */
    private Document m_doc;

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        TestMetadata tm = new TestMetadata();

        try
        {
            tm.setup();

            // Create the configuration
            tm.createConnection();

            tm.loadMetadata();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (tm.m_config != null)
            {
                try
                {
                    tm.m_config.getJCoConnectionManager().closeAllConnections();
                }
                catch (SAPConnectorException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Initialize the logger.
     */
    public void setup()
    {
        LoggerConfigurator.initLogger("./test/Log4jConfiguration.xml");
        System.setProperty("java.library.path",
                           "./docs/internal/sapdlls" + File.pathSeparator +
                           System.getProperty("java.library.path"));
        m_doc = new Document();
    }

    /**
     * This method loads the configuration.
     *
     * @throws  Exception  In case of any exceptions
     */
    private void createConnection()
                           throws Exception
    {
        int node = m_doc.load(".\\test\\java\\com\\cordys\\test\\connection\\config.xml");

        try
        {
            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
                                                                      "o=system,cn=cordys,cn=main,o=gussow.com",
                                                                      "cn=SAP Group,cn=soap nodes,o=system,cn=cordys,cn=main,o=gussow.com");
        }
        finally
        {
            BACUtil.deleteNode(node);
        }
    }

    /**
     * DOCUMENTME.
     */
    private void loadMetadata() throws Exception
    {
        SAPJCoMetadataLoader metadataLoader = new SAPJCoMetadataLoader(m_config,
                                                                       new SAPJCoRequestHandler(m_config,
                                                                                                m_doc));
        
    }
}
