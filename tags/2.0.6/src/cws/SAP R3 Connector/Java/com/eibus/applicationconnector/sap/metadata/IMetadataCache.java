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
 package com.eibus.applicationconnector.sap.metadata;

import com.eibus.applicationconnector.sap.connection.ISAPConnection;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.metadata.filter.IFilter;
import com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;

import java.util.List;
import java.util.Map;

/**
 * This class describes the metadata cache.
 *
 * @author  pgussow
 */
public interface IMetadataCache
{
    /**
     * This method adds the BAPI metadata to the cache.
     *
     * @param  bapiDetails  The details of the bapi.
     */
    void addBAPI(ITypeContainer bapiDetails);

    /**
     * This method adds the IDOC metadata to the cache.
     *
     * @param  idocDetails  The details of the IDOC.
     */
    void addIDOC(ITypeContainer idocDetails);

    /**
     * This method adds the RFC metadata to the cache.
     *
     * @param  rfcDetails  The details of the RFC.
     */
    void addRFC(ITypeContainer rfcDetails);

    /**
     * This method returns all the BAPIs that are currently in the cache.
     *
     * @return  All the BAPIs that are currently in the cache.
     */
    Map<String, ITypeContainer> getAllBAPIs();

    /**
     * This method returns all the IDOCs that are currently in the cache.
     *
     * @return  All the IDOCs that are currently in the cache.
     */
    Map<String, ITypeContainer> getAllIDOCs();

    /**
     * This method returns all the RFCs that are currently in the cache.
     *
     * @return  All the RFCs that are currently in the cache.
     */
    Map<String, ITypeContainer> getAllRFCs();

    /**
     * This method gets the cache storage to use.
     *
     * @return  The cache storage to use.
     */
    ICacheStorage getCacheStorage();

    /**
     * This method gets the ID for this cache.
     *
     * @return  The ID for this cache.
     */
    String getID();

    /**
     * This method gets the SAP connection to use for reading.
     *
     * @return  The SAP connection to use for reading.
     */
    ISAPConnection getSAPConnection();

    /**
     * This method will clean all currently cached information and retrieve everything from SAP. The
     * data that will be loaded is:
     *
     * <ul>
     *   <li>BAPIs</li>
     *   <li>RFCs</li>
     *   <li>IDOCS</li>
     *   <li>Component structure</li>
     * </ul>
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void reloadAllData()
                throws SAPConnectorException;

    /**
     * This method will search all BAPIs for the BAPIs that match the given criteria.
     *
     * @param   readFromSAP  Whether or not the BAPIs should be refreshed from SAP.
     * @param   object       The filter for the object name.
     * @param   method       The filter for the method.
     * @param   description  The filter for the description.
     *
     * @return  The list of BAPIs and operations that match the given criteria.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> searchBAPI(boolean readFromSAP, IFilter object, IFilter method,
                                    IFilter description)
                             throws SAPConnectorException;

    /**
     * This method will search all IDOCs for the IDOCs that match the given criteria.
     *
     * @param   readFromSAP  Whether or not the IDOCs should be refreshed from SAP.
     * @param   messageType  The filter for the message type.
     * @param   operation    The filter for the operation.
     * @param   description  The filter for the description.
     *
     * @return  The list of IDOCs and operations that match the given criteria.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> searchIDOC(boolean readFromSAP, IFilter messageType, IFilter operation,
                                    IFilter description)
                             throws SAPConnectorException;

    /**
     * This method will search all RFCs for the RFCs that match the given criteria.
     *
     * @param   readFromSAP  Whether or not the RFCs should be refreshed from SAP.
     * @param   function     The filter for the function name.
     * @param   group        The filter for the group name.
     * @param   description  The filter for the description.
     *
     * @return  The list of RFCs and operations that match the given criteria.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> searchRFC(boolean readFromSAP, IFilter function, IFilter group,
                                   IFilter description)
                            throws SAPConnectorException;

    /**
     * This method sets the cache storage to use.
     *
     * @param  storage  The cache storage to use.
     */
    void setCacheStorage(ICacheStorage storage);

    /**
     * This method sets the ID for this cache.
     *
     * @param  id  The ID for this cache.
     */
    void setID(String id);

    /**
     * This method sets the SAP connection to use for reading.
     *
     * @param  connection  The SAP connection to use for reading.
     */
    void setSAPConnection(ISAPConnection connection);
}
