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

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.Messages;
import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.connection.ISAPConnection;
import com.eibus.applicationconnector.sap.connection.jco.MetadataSAPConnection;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnection;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnectionManager;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoRequestHandler;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationExceptionMessages;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.idoc.SAPIDocServer;
import com.eibus.applicationconnector.sap.idoc.TargetMappingFinder;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;
import com.eibus.applicationconnector.sap.metadata.MetadataCacheFactory;
import com.eibus.applicationconnector.sap.metadata.storage.CacheStorageFactory;
import com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage;
import com.eibus.applicationconnector.sap.usermapping.IUserMapping;
import com.eibus.applicationconnector.sap.usermapping.UserMappingFactory;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;
import com.eibus.util.system.Native;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

import java.io.File;

import java.net.URL;

/**
 * This class holds the configuration of the processor.
 *
 * @author  pgussow
 */
class ProcessorConfig
    implements ISAPConfiguration
{
    /**
     * Key where target mappings are stored in XML store.
     */
    private static final String MAPPINGS_XMLSTORE_PATH = "/cordys/bac/sap/targetmappings";
    /**
     * Holds the default location for the RFC interface cache.
     */
    private static final String DEFAULT_INTERFACES_RFC = "Interfaces/RFC";
    /**
     * Holds the default location for the IDOC interface cache.
     */
    private static final String DEFAULT_INTERFACES_IDOC = "Interfaces/IDOC";
    /**
     * Holds the name of the tag 'gwserv'.
     */
    private static final String TAG_GATEWAY_SERVICE = "gwserv";
    /**
     * Holds the name of the tag 'progID'.
     */
    private static final String TAG_PROGRAM_ID = "progID";
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ProcessorConfig.class);
    /**
     * Holds the name of the tag 'idocservers'.
     */
    private static final String TAG_IDOCSERVERS = "idocservers";
    /**
     * Holds the name of the tag 'maxCons'.
     */
    private static final String TAG_MAX_CONNECTIONS = "maxCons";
    /**
     * Holds the name of the tag 'sysnum'.
     */
    private static final String TAG_SYSTEM_NUMBER = "sysnum";
    /**
     * Holds the name of the tag 'language'.
     */
    private static final String TAG_LANGUAGE = "language";
    /**
     * Holds the name of the tag 'userid'.
     */
    private static final String TAG_USERID = "userid";
    /**
     * Holds the name of the tag 'password'.
     */
    private static final String TAG_PASSWORD = "password";
    /**
     * Holds the default value for the maximum number of connections.
     */
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    /**
     * Holds the name of the tag 'client'.
     */
    private static final String TAG_SAP_CLIENT = "client";
    /**
     * Holds the name of the tag 'cachedirectory'.
     */
    private static final String TAG_CACHEDIRECTORY = "cachedirectory";
    /**
     * Holds the name of the tag 'sapserver'.
     */
    private static final String TAG_SAPSERVER = "sapserver";
    /**
     * Holds the name of the tag 'connection'.
     */
    private static final String TAG_CONNECTION = "connection";
    /**
     * Holds the name of the tag 'fallbackToSystemUser'.
     */
    private static final String TAG_FALLBACK_TO_SYSTEM_USER = "fallbackToSystemUser";
    /**
     * Holds the default value for the maximum number of IDOC servers.
     */
    private static final int DEFAULT_IDOC_SERVERS = 0;
    /**
     * Holds whether or not to fall back to the system user if no usermapping is defined.
     */
    boolean m_fallbackToSystemUser;
    /**
     * Holds the BAPI invoke URL.
     */
    private URL m_bapiInvokeURL;
    /**
     * Holds the BAPI invoke URL base.
     */
    private String m_bapiInvokeURLString;
    /**
     * Holds the location of the keystore.
     */
    private String m_bcKeyStoreLocation;
    /**
     * Holds the password for the key store.
     */
    private String m_bcKeyStorePassword;
    /**
     * Holds the port of the business connector.
     */
    private int m_bcPort;
    /**
     * Holds the protocol used for the business connector.
     */
    private String m_bcProtocol;
    /**
     * Holds the business connector server name.
     */
    private String m_bcServer;
    /**
     * Holds the local cache directory.
     */
    private String m_cacheDirectory;
    /**
     * Holds the gateway service name.
     */
    private String m_gatewayService;
    /**
     * Holds the relative path in the cache repository.
     */
    private String m_idocCacheRoot;
    /**
     * Holds the IDOC invoke URL.
     */
    private URL m_idocInvokeURL;
    /**
     * Holds the IDOC invoke URL base.
     */
    private String m_idocInvokeURLString;
    /**
     * Holds the repository with all the outstanding IDOCs.
     */
    private IDoc.Repository m_idocRepository = null;
    /**
     * Holds all currently active IDOC servers.
     */
    private SAPIDocServer[] m_idocServers;
    /**
     * Holds the connection manager to use.
     */
    private SAPJCoConnectionManager m_jcoConManager;
    /**
     * Holds the language to use.
     */
    private String m_language;
    /**
     * Holds the XML file containing all mappings.
     */
    private TargetMappingFinder m_mappingFinder;
    /**
     * Holds the maximum number of connections.
     */
    private int m_maxConnections;
    /**
     * Holds the metadata cache that is used for this connector.
     */
    private IMetadataCache m_metadataCache;
    /**
     * Holds the number of IDOC servers to instantiate.
     */
    private int m_nrOfIDOCServers;
    /**
     * Holds the organization under which this conenctor is running.
     */
    private String m_organization;
    /**
     * Holds the password to use for the connection.
     */
    private String m_password;
    /**
     * Holds the program ID for the connection.
     */
    private String m_programID;
    /**
     * Holds the SAP repository.
     */
    private IRepository m_repository = null;
    /**
     * Holds the relative path for the RFC cache repository.
     */
    private String m_rfcCacheRoot;
    /**
     * Holds the RFC invoke URL.
     */
    private URL m_rfcInvokeURL;
    /**
     * Holds the RFC invoke URL base.
     */
    private String m_rfcInvokeURLString;
    /**
     * Holds the SAP client number.
     */
    private String m_sapClient;
    /**
     * Holds the name of the SAP server.
     */
    private String m_sapServer;
    /**
     * Holds the DN of the Service Group hosting these services.
     */
    private String m_serviceGroup;
    /**
     * Holds the SAP system number.
     */
    private String m_systemNumber;
    /**
     * Holds the user ID to use.
     */
    private String m_userID;

    /**
     * Creates a new ProcessorConfig object. It will parse and validate the configuration XML.
     *
     * @param   configuration  The configuration XML.
     * @param   organization   The DN of the current organization.
     * @param   serviceGroup   The DN of the Service Group hosting these services.
     *
     * @throws  SAPConfigurationException  In case of any configuration errors.
     * @throws  SAPConnectorException      In case the TargetMappingFinder reports any exceptions.
     */
    public ProcessorConfig(int configuration, String organization, String serviceGroup)
                    throws SAPConfigurationException, SAPConnectorException
    {
        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_CONFIG);

        m_organization = organization;
        m_serviceGroup = serviceGroup;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Configuration to use:\n" + Node.writeToString(configuration, true));
        }

        int connection = XPathHelper.selectSingleNode(configuration, "ns:" + TAG_CONNECTION, xmi);

        if (connection == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                TAG_CONNECTION);
        }

        // Get the SAP server name
        m_sapServer = XPathHelper.getStringValue(connection, "ns:" + TAG_SAPSERVER, xmi, "");

        if (!Util.isSet(m_sapServer))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_SAPSERVER);
        }

        // Get whether or not to fall back to the system user if no user mapping is found.
        m_fallbackToSystemUser = XPathHelper.getBooleanValue(connection,
                                                             "ns:" + TAG_FALLBACK_TO_SYSTEM_USER,
                                                             xmi, true);

        // Determine the connection type.
        int jco = XPathHelper.selectSingleNode(connection, "ns:" + "jco", xmi);

        if (jco == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_UNKNOWN_MIDDLEWARE);
        }

        // Get the local cache folder.
        m_cacheDirectory = XPathHelper.getStringValue(connection, "ns:" + TAG_CACHEDIRECTORY, xmi,
                                                      "");

        if (!Util.isSet(m_cacheDirectory))
        {
            // Default it to the installation folder
            File cacheFolder = new File(EIBProperties.getInstallDir(),
                                        SAPConnectorConstants.DEPLOY_PATH);
            m_cacheDirectory = cacheFolder.getAbsolutePath();
        }

        // Now we need to do the relative paths for the cache folders.
        // TODO: make it configurable.
        m_idocCacheRoot = DEFAULT_INTERFACES_IDOC;
        m_rfcCacheRoot = DEFAULT_INTERFACES_RFC;

        // Create proper cache folders.
        createCacheDirectory(getCacheDirectory());

        // Parse middleware specific parameters.
        parseJCO(jco, xmi);

        m_jcoConManager = new SAPJCoConnectionManager(getMaxConnections() - 1);

        SAPJCoConnection connectionForRepositories = m_jcoConManager
                                                     .getUserConnectionForRepositories(this,
                                                                                       getUserID(),
                                                                                       getPassword());

        // Create the proper repositories
        String systemID = connectionForRepositories.getAttributes().getSystemID();
        m_repository = JCO.createRepository(systemID, connectionForRepositories);
        m_idocRepository = JCoIDoc.createRepository(systemID, connectionForRepositories);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Created Repositories.");
        }

        // Initialize and start the IDOC servers.
        if (getNrOfIDOCServers() > 0)
        {
            m_mappingFinder = new TargetMappingFinder(organization, MAPPINGS_XMLSTORE_PATH);

            try
            {
                m_mappingFinder.loadTargetMapppings();
            }
            catch (SAPConnectorException sfault)
            {
                LOG.warn(sfault, Messages.WRN_MAPPINGS_NOT_LOADED);
            }

            startIDOCListeners(getNrOfIDOCServers(), getSAPServer(), getGatewayService(),
                               getProgramID());
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Creating metadata cache");
        }

        // For now we'll use the default file system cache.
        ICacheStorage storage = CacheStorageFactory.createDefaultCacheStorage(organization);
        m_metadataCache = MetadataCacheFactory.createCache(systemID, getSAPConnectionForMetadata(),
                                                           storage);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#closeAllConnections()
     */
    @Override public void closeAllConnections()
                                       throws SAPConnectorException
    {
        m_jcoConManager.closeAllConnections();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBAPIInvokeURL()
     */
    public URL getBAPIInvokeURL()
    {
        return m_bapiInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBAPIInvokeURLString()
     */
    public String getBAPIInvokeURLString()
    {
        return m_bapiInvokeURLString;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBCKeyStoreLocation()
     */
    public String getBCKeyStoreLocation()
    {
        return m_bcKeyStoreLocation;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBCKeyStorePassword()
     */
    public String getBCKeyStorePassword()
    {
        return m_bcKeyStorePassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBCPort()
     */
    public int getBCPort()
    {
        return m_bcPort;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBCProtocol()
     */
    public String getBCProtocol()
    {
        return m_bcProtocol;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getBCServer()
     */
    public String getBCServer()
    {
        return m_bcServer;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getCacheDirectory()
     */
    public String getCacheDirectory()
    {
        return m_cacheDirectory;
    }

    /**
     * This method gets whether or not to fall back to the system user if no usermapping is defined.
     *
     * @return  Whether or not to fall back to the system user if no usermapping is defined.
     */
    public boolean getFallbackToSystemUser()
    {
        return m_fallbackToSystemUser;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getGatewayService()
     */
    public String getGatewayService()
    {
        return m_gatewayService;
    }

    /**
     * This method gets the relative path in the cache repository.
     *
     * @return  The relative path in the cache repository.
     */
    public String getIDOCCacheRoot()
    {
        return m_idocCacheRoot;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getIDOCInvokeURL()
     */
    public URL getIDOCInvokeURL()
    {
        return m_idocInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getIDOCInvokeURLString()
     */
    public String getIDOCInvokeURLString()
    {
        return m_idocInvokeURLString;
    }

    /**
     * This method gets the IDOC repository with all the outstanding IDOCs.
     *
     * @return  The IDOC repository with all the outstanding IDOCs.
     */
    public IDoc.Repository getIDOCRepository()
    {
        return m_idocRepository;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getJCoConnectionManager()
     */
    @Override public SAPJCoConnectionManager getJCoConnectionManager()
    {
        return m_jcoConManager;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getLanguage()
     */
    public String getLanguage()
    {
        return m_language;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getMaxConnections()
     */
    public int getMaxConnections()
    {
        return m_maxConnections;
    }

    /**
     * This method gets the metadata cache that is used for this connector.
     *
     * @return  The metadata cache that is used for this connector.
     */
    public IMetadataCache getMetadataCache()
    {
        return m_metadataCache;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getNrOfIDOCServers()
     */
    public int getNrOfIDOCServers()
    {
        return m_nrOfIDOCServers;
    }

    /**
     * This method gets the organization under which this conenctor is running.
     *
     * @return  The organization under which this conenctor is running.
     */
    public String getOrganization()
    {
        return m_organization;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getPassword()
     */
    public String getPassword()
    {
        return m_password;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getProgramID()
     */
    public String getProgramID()
    {
        return m_programID;
    }

    /**
     * This method gets the SAP repository to use for information.
     *
     * @return  The SAP repository to use for information.
     */
    public IRepository getRepository()
    {
        return m_repository;
    }

    /**
     * This method gets the relative path for the RFC cache repository.
     *
     * @return  The relative path for the RFC cache repository.
     */
    public String getRFCCacheRoot()
    {
        return m_rfcCacheRoot;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getRFCInvokeURL()
     */
    public URL getRFCInvokeURL()
    {
        return m_rfcInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getRFCInvokeURLString()
     */
    public String getRFCInvokeURLString()
    {
        return m_rfcInvokeURLString;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getSAPClient()
     */
    public String getSAPClient()
    {
        return m_sapClient;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getSAPConnectionForMetadata()
     */
    @Override public ISAPConnection getSAPConnectionForMetadata()
                                                         throws SAPConnectorException
    {
        IUserMapping userMapping = UserMappingFactory.createFixedUserMapping(this);

        SAPJCoRequestHandler requestHandler = new SAPJCoRequestHandler(this, new Document(),
                                                                       userMapping);

        return new MetadataSAPConnection(requestHandler);
    }

    /**
     * This method gets the SAP JCo connection manager.
     *
     * @return  The SAP JCo connection manager.
     */
    public SAPJCoConnectionManager getSAPJCoConnectionManager()
    {
        return m_jcoConManager;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getSAPServer()
     */
    public String getSAPServer()
    {
        return m_sapServer;
    }

    /**
     * This method gets the DN of the Service Group hosting these services.
     *
     * @return  The DN of the Service Group hosting these services.
     */
    public String getServiceGroup()
    {
        return m_serviceGroup;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getSystemNumber()
     */
    public String getSystemNumber()
    {
        return m_systemNumber;
    }

    /**
     * This method gets the TargetMappingFinder.
     *
     * @return  The TargetMappingFinder.
     */
    public TargetMappingFinder getTargetMappingFinder()
    {
        return m_mappingFinder;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#getUserID()
     */
    public String getUserID()
    {
        return m_userID;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBAPIInvokeURL(java.net.URL)
     */
    public void setBAPIInvokeURL(URL bapiInvokeURL)
    {
        m_bapiInvokeURL = bapiInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBAPIInvokeURLString(java.lang.String)
     */
    public void setBAPIInvokeURLString(String bapiInvokeURLString)
    {
        m_bapiInvokeURLString = bapiInvokeURLString;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBCKeyStoreLocation(java.lang.String)
     */
    public void setBCKeyStoreLocation(String bcKeyStoreLocation)
    {
        m_bcKeyStoreLocation = bcKeyStoreLocation;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBCKeyStorePassword(java.lang.String)
     */
    public void setBCKeyStorePassword(String bcKeyStorePassword)
    {
        m_bcKeyStorePassword = bcKeyStorePassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBCPort(int)
     */
    public void setBCPort(int bcPort)
    {
        m_bcPort = bcPort;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBCProtocol(java.lang.String)
     */
    public void setBCProtocol(String bcProtocol)
    {
        m_bcProtocol = bcProtocol;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setBCServer(java.lang.String)
     */
    public void setBCServer(String bcServer)
    {
        m_bcServer = bcServer;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setCacheDirectory(java.lang.String)
     */
    public void setCacheDirectory(String cacheDirectory)
    {
        m_cacheDirectory = cacheDirectory;
    }

    /**
     * This method sets whether or not to fall back to the system user if no usermapping is defined.
     *
     * @param  fallbackToSystemUser  Whether or not to fall back to the system user if no
     *                               usermapping is defined.
     */
    public void setFallbackToSystemUser(boolean fallbackToSystemUser)
    {
        m_fallbackToSystemUser = fallbackToSystemUser;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setGatewayService(java.lang.String)
     */
    public void setGatewayService(String gatewayService)
    {
        m_gatewayService = gatewayService;
    }

    /**
     * This method sets the relative path in the cache repository.
     *
     * @param  idocCacheRoot  The relative path in the cache repository.
     */
    public void setIDOCCacheRoot(String idocCacheRoot)
    {
        m_idocCacheRoot = idocCacheRoot;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setIDOCInvokeURL(java.net.URL)
     */
    public void setIDOCInvokeURL(URL idocInvokeURL)
    {
        m_idocInvokeURL = idocInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setIDOCInvokeURLString(java.lang.String)
     */
    public void setIDOCInvokeURLString(String idocInvokeURLString)
    {
        m_idocInvokeURLString = idocInvokeURLString;
    }

    /**
     * This method sets the IDOC repository with all the outstanding IDOCs.
     *
     * @param  idocRepository  The IDOC repository with all the outstanding IDOCs.
     */
    public void setIDOCRepository(IDoc.Repository idocRepository)
    {
        m_idocRepository = idocRepository;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setLanguage(java.lang.String)
     */
    public void setLanguage(String language)
    {
        m_language = language;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setMaxConnections(int)
     */
    public void setMaxConnections(int maxConnections)
    {
        m_maxConnections = maxConnections;
    }

    /**
     * This method sets the metadata cache that is used for this connector.
     *
     * @param  metadataCache  The metadata cache that is used for this connector.
     */
    public void setMetadataCache(IMetadataCache metadataCache)
    {
        m_metadataCache = metadataCache;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setNrOfIDOCServers(int)
     */
    public void setNrOfIDOCServers(int nrOfIDOCServers)
    {
        m_nrOfIDOCServers = nrOfIDOCServers;
    }

    /**
     * This method sets the organization under which this conenctor is running.
     *
     * @param  organization  The organization under which this conenctor is running.
     */
    public void setOrganzation(String organization)
    {
        m_organization = organization;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setPassword(java.lang.String)
     */
    public void setPassword(String password)
    {
        m_password = password;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setProgramID(java.lang.String)
     */
    public void setProgramID(String programID)
    {
        m_programID = programID;
    }

    /**
     * This method sets the SAP repository to use for information.
     *
     * @param  repository  The SAP repository to use for information.
     */
    public void setRepository(IRepository repository)
    {
        m_repository = repository;
    }

    /**
     * This method sets the relative path for the RFC cache repository.
     *
     * @param  rfcCacheRoot  The relative path for the RFC cache repository.
     */
    public void setRFCCacheRoot(String rfcCacheRoot)
    {
        m_rfcCacheRoot = rfcCacheRoot;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setRFCInvokeURL(java.net.URL)
     */
    public void setRFCInvokeURL(URL rfcInvokeURL)
    {
        m_rfcInvokeURL = rfcInvokeURL;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setRFCInvokeURLString(java.lang.String)
     */
    public void setRFCInvokeURLString(String rfcInvokeURLString)
    {
        m_rfcInvokeURLString = rfcInvokeURLString;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setSAPClient(java.lang.String)
     */
    public void setSAPClient(String sapClient)
    {
        m_sapClient = sapClient;
    }

    /**
     * This method sets the SAP JCo connection manager.
     *
     * @param  jcoConManager  The SAP JCo connection manager.
     */
    public void setSAPJCoConnectionManager(SAPJCoConnectionManager jcoConManager)
    {
        m_jcoConManager = jcoConManager;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setSAPServer(java.lang.String)
     */
    public void setSAPServer(String sapServer)
    {
        m_sapServer = sapServer;
    }

    /**
     * This method sets the DN of the Service Group hosting these services.
     *
     * @param  serviceGroup  The DN of the Service Group hosting these services.
     */
    public void setServiceGroup(String serviceGroup)
    {
        m_serviceGroup = serviceGroup;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setSystemNumber(java.lang.String)
     */
    public void setSystemNumber(String systemNumber)
    {
        m_systemNumber = systemNumber;
    }

    /**
     * This method sets the TargetMappingFinder.
     *
     * @param  mappingFinder  The TargetMappingFinder.
     */
    public void setTargetMappingFinder(TargetMappingFinder mappingFinder)
    {
        m_mappingFinder = mappingFinder;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.config.ISAPConfiguration#setUserID(java.lang.String)
     */
    public void setUserID(String userID)
    {
        m_userID = userID;
    }

    /**
     * This method starts the IDOC servers or listeners.
     *
     * @param  number_IDOCServers  DOCUMENTME
     * @param  sapServer           DOCUMENTME
     * @param  gatewayService      DOCUMENTME
     * @param  programID           DOCUMENTME
     */
    public void startIDOCListeners(int number_IDOCServers, String sapServer, String gatewayService,
                                   String programID)
    {
        m_idocServers = new SAPIDocServer[number_IDOCServers];

        try
        {
            for (int i = 0; i < number_IDOCServers; i++)
            {
                m_idocServers[i] = new SAPIDocServer(sapServer, gatewayService, programID,
                                                     m_repository, m_idocRepository,
                                                     (ISAPConfiguration) this);
                m_idocServers[i].start();
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" IDOC Servers started.");
            }
        }
        catch (SAPConnectorException sce)
        {
            throw new IllegalStateException(sce.getLocalizedMessage(), sce);
        }
    }

    /**
     * This method stops the IDOC servers or listeners.
     */
    public void stopIDOCListeners()
    {
        if (m_idocServers != null)
        {
            int number_IDOCServers = m_idocServers.length;

            for (int i = 0; i < number_IDOCServers; i++)
            {
                m_idocServers[i].stop();
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC Servers stopped.");
            }
        }
    }

    /**
     * This method checks if the cache directory already exists or not. If not, a new directory is
     * created. It creates sub directories to store interfaces of RFCs and IDOCs also. The naming
     * convention followed would be cacheDir\Interfaces\RFC and cacheDir\Interfaces\IDOC.
     *
     * @param   directoryPath  The main directory for the cache.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void createCacheDirectory(String directoryPath)
                               throws SAPConnectorException
    {
        // Create proper file objects.
        File cacheFolder = new File(directoryPath);
        File idocInterfaceDir = new File(cacheFolder, getIDOCCacheRoot());
        File rfcInterfaceDir = new File(cacheFolder, getRFCCacheRoot());

        // Check if the IDOC folder exists.
        if (!idocInterfaceDir.isDirectory())
        {
            if (!idocInterfaceDir.mkdirs()) // couldn't create directory.
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_CREATE_FOLDER,
                                                idocInterfaceDir.getAbsolutePath());
            }
        }

        // Check if the RFC folder exists.
        if (!rfcInterfaceDir.isDirectory())
        {
            if (!rfcInterfaceDir.mkdirs())
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_CREATE_FOLDER,
                                                rfcInterfaceDir.getAbsolutePath());
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Created cache directories.\nFor IDOCs: " +
                      idocInterfaceDir.getAbsolutePath() + "\nFor RFCs: " +
                      rfcInterfaceDir.getAbsolutePath());
        }
    }

    /**
     * This method parses the JCO connection parameters.
     *
     * @param   jco  The JCO configuration.
     * @param   xmi  The namespace-prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    private void parseJCO(int jco, XPathMetaInfo xmi)
                   throws SAPConfigurationException
    {
        // Get the sap client id.
        m_sapClient = XPathHelper.getStringValue(jco, "ns:" + TAG_SAP_CLIENT, xmi, "");

        if (!Util.isSet(m_sapClient))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_SAP_CLIENT);
        }

        // Get the system number
        m_systemNumber = XPathHelper.getStringValue(jco, "ns:" + TAG_SYSTEM_NUMBER, xmi, "");

        if (!Util.isSet(m_systemNumber))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_SYSTEM_NUMBER);
        }

        // Get the language
        m_language = XPathHelper.getStringValue(jco, "ns:" + TAG_LANGUAGE, xmi, "");

        if (!Util.isSet(m_language))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_LANGUAGE);
        }

        // Get the user ID
        m_userID = XPathHelper.getStringValue(jco, "ns:" + TAG_USERID, xmi, "");

        if (!Util.isSet(m_userID))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_USERID);
        }

        // Get the password
        m_password = XPathHelper.getStringValue(jco, "ns:" + TAG_PASSWORD, xmi, "");

        if (!Util.isSet(m_password))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_PASSWORD);
        }

        byte[] bytes = m_password.getBytes();
        m_password = new String(Native.decodeBinBase64(bytes, bytes.length));

        // Get the maximum number of connections
        m_maxConnections = XPathHelper.getIntegerValue(jco, "ns:" + TAG_MAX_CONNECTIONS, xmi,
                                                       DEFAULT_MAX_CONNECTIONS);

        // Get the number of IDOC servers
        m_nrOfIDOCServers = XPathHelper.getIntegerValue(jco, "ns:" + TAG_IDOCSERVERS, xmi,
                                                        DEFAULT_IDOC_SERVERS);

        // Gets the gateway service
        m_gatewayService = XPathHelper.getStringValue(jco, "ns:" + TAG_GATEWAY_SERVICE, xmi, "");

        if ((m_nrOfIDOCServers > 0) && !Util.isSet(m_gatewayService))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_GATEWAY_SERVICE);
        }

        // Get the program ID
        m_programID = XPathHelper.getStringValue(jco, "ns:" + TAG_PROGRAM_ID, xmi, "");

        if ((m_nrOfIDOCServers > 0) && !Util.isSet(m_programID))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_TAG_HAS_NO_VALUE,
                                                TAG_PROGRAM_ID);
        }
    }
}
