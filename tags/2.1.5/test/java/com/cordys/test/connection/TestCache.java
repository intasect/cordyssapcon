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
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;
import com.eibus.applicationconnector.sap.metadata.MetadataCacheFactory;
import com.eibus.applicationconnector.sap.metadata.filter.FilterFactory;
import com.eibus.applicationconnector.sap.metadata.filter.IFilter;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;
import com.eibus.applicationconnector.sap.util.BACUtil;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.config.LoggerConfigurator;

import com.eibus.xml.nom.Document;

import java.io.File;

import java.util.List;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class TestCache
{
    /**
     * Holds the logger to use.
     */
    private CordysLogger LOG = null;
    /**
     * Holds the configuration.
     */
    private ISAPConfiguration m_config;
    /**
     * Holds the NOM document.
     */
    private Document m_doc;
    /**
     * Holds the metadata cache.
     */
    private IMetadataCache m_metadataCache;

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        TestCache tm = new TestCache();

        try
        {
            tm.setup();

            // Create the configuration
            tm.createConnection();

            tm.doSearches();
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

        LOG = CordysLogger.getCordysLogger(TestCache.class);
    }

    /**
     * This method loads the configuration.
     *
     * @throws  Exception  In case of any exceptions
     */
    private void createConnection()
                           throws Exception
    {
        int node = m_doc.load(".\\test\\java\\com\\cordys\\test\\xmi\\sapr3config.xml");

        try
        {
            m_config = SAPConfigurationFactory.createSAPConfiguration(node,
                                                                      "o=system,cn=cordys,cn=main,o=gussow.com",
                                                                      "cn=SAP Group,cn=soap nodes,o=system,cn=cordys,cn=main,o=gussow.com");

            m_metadataCache = MetadataCacheFactory.createCache("local",
                                                             m_config.getSAPConnectionForMetadata(),
                                                             "o=system,cn=cordys,cn=main,o=gussow.com");
        }
        finally
        {
            BACUtil.deleteNode(node);
        }
    }

    /**
     * Tests the searches.
     *
     * @throws  Exception  In case of any exceptions
     */
    private void doSearches()
                     throws Exception
    {
        IFilter filter = FilterFactory.createEqualsFilter("GetList");
        List<ITypeContainer> bapis = m_metadataCache.searchBAPI(false, null, filter, null);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("BAPIS: Found " + bapis.size() + " matches");
        }

        filter = FilterFactory.createEqualsFilter("ABSEN1");

        List<ITypeContainer> idocs = m_metadataCache.searchIDOC(false, filter, null, null);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("IDOCS: Found " + idocs.size() + " matches");
        }

        filter = FilterFactory.createEqualsFilter("ACL_ACTIVITIES");

        List<ITypeContainer> rfcs = m_metadataCache.searchRFC(false, null, filter, null);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("RFCS: Found " + rfcs.size() + " matches");
        }
    }
}
