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

import com.eibus.applicationconnector.sap.connection.ISAPConnection;
import com.eibus.applicationconnector.sap.connection.jco.SAPJCoConnectionManager;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.idoc.TargetMappingFinder;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.jco.IRepository;

import java.net.URL;

/**
 * Holds the configuration that is used for the connector.
 *
 * @author  pgussow
 */
public interface ISAPConfiguration
{
    /**
     * This method closes all open connections.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void closeAllConnections()
                      throws SAPConnectorException;

    /**
     * This method gets the BAPI invoke URL.
     *
     * @return  The BAPI invoke URL.
     */
    URL getBAPIInvokeURL();

    /**
     * This method gets the BAPI invoke URL base.
     *
     * @return  The BAPI invoke URL base.
     */
    String getBAPIInvokeURLString();

    /**
     * This method gets the location of the keystore.
     *
     * @return  The location of the keystore.
     */
    String getBCKeyStoreLocation();

    /**
     * This method gets the password for the key store.
     *
     * @return  The password for the key store.
     */
    String getBCKeyStorePassword();

    /**
     * This method gets the port of the business connector.
     *
     * @return  The port of the business connector.
     */
    int getBCPort();

    /**
     * This method gets the protocol used for the business connector.
     *
     * @return  The protocol used for the business connector.
     */
    String getBCProtocol();

    /**
     * This method gets the business connector server name.
     *
     * @return  The business connector server name.
     */
    String getBCServer();

    /**
     * This method gets the local cache directory.
     *
     * @return  The local cache directory.
     */
    String getCacheDirectory();

    /**
     * This method gets whether or not to fall back to the system user if no usermapping is defined.
     *
     * @return  Whether or not to fall back to the system user if no usermapping is defined.
     */
    boolean getFallbackToSystemUser();

    /**
     * This method gets the gateway service name.
     *
     * @return  The gateway service name.
     */
    String getGatewayService();

    /**
     * This method gets the relative path in the cache repository.
     *
     * @return  The relative path in the cache repository.
     */
    String getIDOCCacheRoot();

    /**
     * This method gets the IDOC invoke URL.
     *
     * @return  The IDOC invoke URL.
     */
    URL getIDOCInvokeURL();

    /**
     * This method gets the IDOC invoke URL base.
     *
     * @return  The IDOC invoke URL base.
     */
    String getIDOCInvokeURLString();

    /**
     * This method gets the IDOC repository with all the outstanding IDOCs.
     *
     * @return  The IDOC repository with all the outstanding IDOCs.
     */
    IDoc.Repository getIDOCRepository();

    /**
     * This method returns the JCo connection manager.
     *
     * @return  The JCo connection manager.
     */
    SAPJCoConnectionManager getJCoConnectionManager();

    /**
     * This method gets the language to use.
     *
     * @return  The language to use.
     */
    String getLanguage();

    /**
     * This method gets the maximum number of connections.
     *
     * @return  The maximum number of connections.
     */
    int getMaxConnections();

    /**
     * This method gets the metadata cache that is used for this connector.
     *
     * @return  The metadata cache that is used for this connector.
     */
    IMetadataCache getMetadataCache();

    /**
     * This method gets the number of IDOC servers to instantiate.
     *
     * @return  The number of IDOC servers to instantiate.
     */
    int getNrOfIDOCServers();

    /**
     * This method gets the organization under which this conenctor is running.
     *
     * @return  The organization under which this conenctor is running.
     */
    String getOrganization();

    /**
     * This method gets the password to use for the connection.
     *
     * @return  The password to use for the connection.
     */
    String getPassword();

    /**
     * This method gets the program ID for the connection.
     *
     * @return  The program ID for the connection.
     */
    String getProgramID();

    /**
     * This method gets the SAP repository to use for information.
     *
     * @return  The SAP repository to use for information.
     */
    IRepository getRepository();

    /**
     * This method gets the relative path for the RFC cache repository.
     *
     * @return  The relative path for the RFC cache repository.
     */
    String getRFCCacheRoot();

    /**
     * This method gets the RFC invoke URL.
     *
     * @return  The RFC invoke URL.
     */
    URL getRFCInvokeURL();

    /**
     * This method gets the RFC invoke URL base.
     *
     * @return  The RFC invoke URL base.
     */
    String getRFCInvokeURLString();

    /**
     * This method gets the SAP client number.
     *
     * @return  The SAP client number.
     */
    String getSAPClient();

    /**
     * This method returns the SAP connection that should be used for metadata requests.
     *
     * @return  The SAP connection that should be used for metadata requests.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    ISAPConnection getSAPConnectionForMetadata()
                                        throws SAPConnectorException;

    /**
     * This method gets the SAP JCo connection manager.
     *
     * @return  The SAP JCo connection manager.
     */
    SAPJCoConnectionManager getSAPJCoConnectionManager();

    /**
     * This method gets the name of the SAP server.
     *
     * @return  The name of the SAP server.
     */
    String getSAPServer();

    /**
     * This method gets the DN of the Service Group hosting these services.
     *
     * @return  The DN of the Service Group hosting these services.
     */
    String getServiceGroup();

    /**
     * This method gets the SAP system number.
     *
     * @return  The SAP system number.
     */
    String getSystemNumber();

    /**
     * This method gets the TargetMappingFinder.
     *
     * @return  The TargetMappingFinder.
     */
    TargetMappingFinder getTargetMappingFinder();

    /**
     * This method gets the user ID to use.
     *
     * @return  The user ID to use.
     */
    String getUserID();

    /**
     * This method sets the BAPI invoke URL.
     *
     * @param  bapiInvokeURL  The BAPI invoke URL.
     */
    void setBAPIInvokeURL(URL bapiInvokeURL);

    /**
     * This method sets the BAPI invoke URL base.
     *
     * @param  bapiInvokeURLString  The BAPI invoke URL base.
     */
    void setBAPIInvokeURLString(String bapiInvokeURLString);

    /**
     * This method sets the location of the keystore.
     *
     * @param  bcKeyStoreLocation  The location of the keystore.
     */
    void setBCKeyStoreLocation(String bcKeyStoreLocation);

    /**
     * This method sets the password for the key store.
     *
     * @param  bcKeyStorePassword  The password for the key store.
     */
    void setBCKeyStorePassword(String bcKeyStorePassword);

    /**
     * This method sets the port of the business connector.
     *
     * @param  bcPort  The port of the business connector.
     */
    void setBCPort(int bcPort);

    /**
     * This method sets the protocol used for the business connector.
     *
     * @param  bcProtocol  The protocol used for the business connector.
     */
    void setBCProtocol(String bcProtocol);

    /**
     * This method sets the business connector server name.
     *
     * @param  bcServer  The business connector server name.
     */
    void setBCServer(String bcServer);

    /**
     * This method sets the local cache directory.
     *
     * @param  cacheDirectory  The local cache directory.
     */
    void setCacheDirectory(String cacheDirectory);

    /**
     * This method sets whether or not to fall back to the system user if no usermapping is defined.
     *
     * @param  fallbackToSystemUser  Whether or not to fall back to the system user if no
     *                               usermapping is defined.
     */
    void setFallbackToSystemUser(boolean fallbackToSystemUser);

    /**
     * This method sets the gateway service name.
     *
     * @param  gatewayService  The gateway service name.
     */
    void setGatewayService(String gatewayService);

    /**
     * This method sets the relative path in the cache repository.
     *
     * @param  idocCacheRoot  The relative path in the cache repository.
     */
    void setIDOCCacheRoot(String idocCacheRoot);

    /**
     * This method sets the IDOC invoke URL.
     *
     * @param  idocInvokeURL  The IDOC invoke URL.
     */
    void setIDOCInvokeURL(URL idocInvokeURL);

    /**
     * This method sets the IDOC invoke URL base.
     *
     * @param  idocInvokeURLString  The IDOC invoke URL base.
     */
    void setIDOCInvokeURLString(String idocInvokeURLString);

    /**
     * This method sets the IDOC repository with all the outstanding IDOCs.
     *
     * @param  idocRepository  The IDOC repository with all the outstanding IDOCs.
     */
    void setIDOCRepository(IDoc.Repository idocRepository);

    /**
     * This method sets the language to use.
     *
     * @param  language  The language to use.
     */
    void setLanguage(String language);

    /**
     * This method sets the maximum number of connections.
     *
     * @param  maxConnections  The maximum number of connections.
     */
    void setMaxConnections(int maxConnections);

    /**
     * This method sets the metadata cache that is used for this connector.
     *
     * @param  metadataCache  The metadata cache that is used for this connector.
     */
    void setMetadataCache(IMetadataCache metadataCache);

    /**
     * This method sets the number of IDOC servers to instantiate.
     *
     * @param  nrOfIDOCServers  number of IDOC servers to instantiate.
     */
    void setNrOfIDOCServers(int nrOfIDOCServers);

    /**
     * This method sets the organization under which this conenctor is running.
     *
     * @param  organization  The organization under which this conenctor is running.
     */
    void setOrganzation(String organization);

    /**
     * This method sets the password to use for the connection.
     *
     * @param  password  The password to use for the connection.
     */
    void setPassword(String password);

    /**
     * This method sets the program ID for the connection.
     *
     * @param  programID  The program ID for the connection.
     */
    void setProgramID(String programID);

    /**
     * This method sets the SAP repository to use for information.
     *
     * @param  repository  The SAP repository to use for information.
     */
    void setRepository(IRepository repository);

    /**
     * This method sets the relative path for the RFC cache repository.
     *
     * @param  rfcCacheRoot  The relative path for the RFC cache repository.
     */
    void setRFCCacheRoot(String rfcCacheRoot);

    /**
     * This method sets the RFC invoke URL.
     *
     * @param  rfcInvokeURL  The RFC invoke URL.
     */
    void setRFCInvokeURL(URL rfcInvokeURL);

    /**
     * This method sets the RFC invoke URL base.
     *
     * @param  rfcInvokeURLString  The RFC invoke URL base.
     */
    void setRFCInvokeURLString(String rfcInvokeURLString);

    /**
     * This method sets the SAP client number.
     *
     * @param  sapClient  The SAP client number.
     */
    void setSAPClient(String sapClient);

    /**
     * This method sets the SAP JCo connection manager.
     *
     * @param  jcoConManager  The SAP JCo connection manager.
     */
    void setSAPJCoConnectionManager(SAPJCoConnectionManager jcoConManager);

    /**
     * This method sets the name of the SAP server.
     *
     * @param  sapServer  The name of the SAP server.
     */
    void setSAPServer(String sapServer);

    /**
     * This method sets the DN of the Service Group hosting these services.
     *
     * @param  serviceGroup  The DN of the Service Group hosting these services.
     */
    void setServiceGroup(String serviceGroup);

    /**
     * This method sets the SAP system number.
     *
     * @param  systemNumber  The SAP system number.
     */
    void setSystemNumber(String systemNumber);

    /**
     * This method sets the TargetMappingFinder.
     *
     * @param  mappingFinder  The TargetMappingFinder.
     */
    void setTargetMappingFinder(TargetMappingFinder mappingFinder);

    /**
     * This method sets the user ID to use.
     *
     * @param  userID  The user ID to use.
     */
    void setUserID(String userID);

    /**
     * This method starts the IDOC servers or listeners.
     *
     * @param  number_IDOCServers  DOCUMENTME
     * @param  sapServer           DOCUMENTME
     * @param  gatewayService      DOCUMENTME
     * @param  programID           DOCUMENTME
     */
    void startIDOCListeners(int number_IDOCServers, String sapServer, String gatewayService,
                            String programID);

    /**
     * This method stops the IDOC servers or listeners.
     */
    void stopIDOCListeners();
}
