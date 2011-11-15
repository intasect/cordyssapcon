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
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.filter.IFilter;
import com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage;
import com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata;
import com.eibus.applicationconnector.sap.metadata.types.IIDOCMetadata;
import com.eibus.applicationconnector.sap.metadata.types.IRFCMetadata;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;
import com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata;
import com.eibus.applicationconnector.sap.metadata.types.SAPMetadataFactory;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.util.logger.CordysLogger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the cache for the metadata. The cache will be persisted as well.
 *
 * <p>The cache will persist the following things:</p>
 *
 * <ul>
 *   <li>RFC metadata</li>
 *   <li>IDOC metadata</li>
 *   <li>BAPI metadata</li>
 *   <li>Component structure</li>
 *   <li>RFC method interface metadata</li>
 *   <li>IDOC interface metadata</li>
 * </ul>
 *
 * @author  pgussow
 */
public class MetadataCache
    implements IMetadataCache
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MetadataCache.class);
    /**
     * Holds all the BAPIs that are found for the current repository.
     */
    private Map<String, ITypeContainer> m_allBapis = new LinkedHashMap<String, ITypeContainer>();
    /**
     * Holds all the IDOCs that are found for the current repository.
     */
    private Map<String, ITypeContainer> m_allIDOCs = new LinkedHashMap<String, ITypeContainer>();
    /**
     * Holds all the RFCs that are found for the current repository.
     */
    private Map<String, ITypeContainer> m_allRFCs = new LinkedHashMap<String, ITypeContainer>();
    /**
     * Holds the SAP connection to use.
     */
    private ISAPConnection m_connection;
    /**
     * Holds the ID for this cache.
     */
    private String m_id;
    /**
     * Holds the storage provider.
     */
    private ICacheStorage m_storage;

    /**
     * Creates a new MetadataCache object.
     *
     * @param   id          The ID for this cache.
     * @param   storage     The storage provider.
     * @param   connection  The SAP connection to use.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public MetadataCache(String id, ICacheStorage storage, ISAPConnection connection)
                  throws SAPConnectorException
    {
        m_id = id;
        m_storage = storage;
        m_connection = connection;

        // Check the parameters
        if (!Util.isSet(id))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.THE_ID_MUST_BE_SET_FOR_THE_METADATA_CACHE);
        }

        if (storage == null)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.THE_STORAGE_MUST_BE_SET_FOR_THE_METADATA_CACHE);
        }

        if (connection == null)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.THE_CONNECTION_MUST_BE_SET_FOR_THE_METADATA_CACHE);
        }

        // Load the information from the storage
        storage.loadCache(getID(), this);

        // If nothing is there, then reload the entire cache.
        if (m_allBapis.size() == 0)
        {
            reloadAllData();
        }
    }

    /**
     * This method adds the BAPI metadata to the cache.
     *
     * @param  bapiDetails  The details of the bapi.
     */
    public void addBAPI(ITypeContainer bapiDetails)
    {
        if (m_allBapis.containsKey(bapiDetails.getValue()))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Replacing existing BAPI metada");
            }
        }

        m_allBapis.put(bapiDetails.getValue(), bapiDetails);
    }

    /**
     * This method adds the IDOC metadata to the cache.
     *
     * @param  idocDetails  The details of the IDOC.
     */
    public void addIDOC(ITypeContainer idocDetails)
    {
        if (m_allIDOCs.containsKey(idocDetails.getValue()))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Replacing existing IDOC metada");
            }
        }

        m_allIDOCs.put(idocDetails.getValue(), idocDetails);
    }

    /**
     * This method adds the RFC metadata to the cache.
     *
     * @param  rfcDetails  The details of the RFC.
     */
    public void addRFC(ITypeContainer rfcDetails)
    {
        // RFCs are a bit strange, because they will be indexed based on function name, instead of
        // group name.
        if (m_allRFCs.containsKey(rfcDetails.getValue()))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Replacing existing RFC metada");
            }
        }

        m_allRFCs.put(rfcDetails.getValue(), rfcDetails);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.IMetadataCache#getAllBAPIs()
     */
    @Override public Map<String, ITypeContainer> getAllBAPIs()
    {
        return new LinkedHashMap<String, ITypeContainer>(m_allBapis);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.IMetadataCache#getAllIDOCs()
     */
    @Override public Map<String, ITypeContainer> getAllIDOCs()
    {
        return new LinkedHashMap<String, ITypeContainer>(m_allIDOCs);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.IMetadataCache#getAllRFCs()
     */
    @Override public Map<String, ITypeContainer> getAllRFCs()
    {
        return new LinkedHashMap<String, ITypeContainer>(m_allRFCs);
    }

    /**
     * This method gets the cache storage to use.
     *
     * @return  The cache storage to use.
     */
    public ICacheStorage getCacheStorage()
    {
        return m_storage;
    }

    /**
     * This method gets the ID for this cache.
     *
     * @return  The ID for this cache.
     */
    public String getID()
    {
        return m_id;
    }

    /**
     * This method gets the SAP connection to use for reading.
     *
     * @return  The SAP connection to use for reading.
     */
    public ISAPConnection getSAPConnection()
    {
        return m_connection;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.IMetadataCache#reloadAllData()
     */
    @Override public void reloadAllData()
                                 throws SAPConnectorException
    {
        // First load all BAPIs available
        List<ITypeContainer> allBAPIs = m_connection.getAllBAPIs();

        m_allBapis.clear();

        for (ITypeContainer bapiDetails : allBAPIs)
        {
            addBAPI(bapiDetails);
        }

        // Clean up of the temp list
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Added " + allBAPIs.size() + " BAPIs");
        }

        allBAPIs = null;

        // Now do all RFCs
        List<ITypeContainer> allRFCs = m_connection.getAllRFCs();

        m_allRFCs.clear();

        for (ITypeContainer rfcDetails : allRFCs)
        {
            addRFC(rfcDetails);
        }

        // Clean up of the temp list
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Added " + allRFCs.size() + " RFCs");
        }

        allRFCs = null;

        // Now do all IDOCs
        List<ITypeContainer> allIDOCs = m_connection.getAllIDOCs();

        m_allIDOCs.clear();

        for (ITypeContainer idocDetails : allIDOCs)
        {
            addIDOC(idocDetails);
        }

        // Clean up of the temp list
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Added " + allIDOCs.size() + " IDOCs");
        }

        allIDOCs = null;

        // TODO: Now load the component structure.

        // RFC interface metadata and IDOC interface metadata will be read on the fly. We will not
        // pre-read all interface details.

        // Finally, persist the cache
        m_storage.persistCache(this);
    }

    /**
     * @see  IMetadataCache#searchBAPI(boolean,IFilter, IFilter, IFilter)
     */
    @Override public List<ITypeContainer> searchBAPI(boolean readFromSAP, IFilter object,
                                                     IFilter method, IFilter description)
                                              throws SAPConnectorException
    {
        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        // Get the main list of BAPIs
        List<ITypeContainer> allBAPIs = null;

        if (readFromSAP)
        {
            allBAPIs = m_connection.getAllBAPIs();
        }
        else
        {
            allBAPIs = new ArrayList<ITypeContainer>(m_allBapis.values());
        }

        // If no filter has been set, then return all BAPIs
        if ((object == null) && (method == null) && (description == null))
        {
            returnValue = allBAPIs;
        }
        else
        {
            // Now iterate through all BAPIs to find the ones that match the criteria.
            for (ITypeContainer container : allBAPIs)
            {
                if ((object != null) && object.match(container.getDisplayName()))
                {
                    returnValue.add(container);
                }
                else
                {
                    // Only if the type container would not have been added, it might be that it
                    // needs to be added based on the method of description. If the match is done
                    // based on these 2 criteria a new trimmed version of the container needs to be
                    // made containing only the items that actually match the citeria.
                    if ((method != null) || (description != null))
                    {
                        ITypeContainer tempContainer = null;

                        ArrayList<ITypeMetadata> items = new ArrayList<ITypeMetadata>(container
                                                                                      .getItems()
                                                                                      .values());

                        for (ITypeMetadata metadata : items)
                        {
                            IBAPIMetadata bapi = (IBAPIMetadata) metadata;
                            boolean match = false;

                            if ((method != null) && method.match(bapi.getMethodName()))
                            {
                                match = true;
                            }

                            if (description != null)
                            {
                                match = description.match(bapi.getDescription());
                            }

                            // If this method should be included, we need to add it to the temp
                            // container.
                            if (match == true)
                            {
                                if (tempContainer == null)
                                {
                                    tempContainer = SAPMetadataFactory.createContainer(ESAPObjectType.BAPI);
                                    tempContainer.setValue(container.getValue());
                                    tempContainer.setDisplayName(container.getDisplayName());
                                }

                                tempContainer.addType(bapi);
                            }
                        }

                        // If any of the criteria matched, add the container to the result.
                        if (tempContainer != null)
                        {
                            returnValue.add(tempContainer);
                        }
                    }
                }
            }
        }

        return returnValue;
    }

    /**
     * @see  IMetadataCache#searchIDOC(boolean,IFilter, IFilter, IFilter)
     */
    @Override public List<ITypeContainer> searchIDOC(boolean readFromSAP, IFilter messageType,
                                                     IFilter operation, IFilter description)
                                              throws SAPConnectorException
    {
        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        // Get the main list of IDOCs
        List<ITypeContainer> allIDOCs = null;

        if (readFromSAP)
        {
            allIDOCs = m_connection.getAllIDOCs();
        }
        else
        {
            allIDOCs = new ArrayList<ITypeContainer>(m_allIDOCs.values());
        }

        // If no filter has been set, then return all IDOCs
        if ((messageType == null) && (operation == null) && (description == null))
        {
            returnValue = allIDOCs;
        }
        else
        {
            // Now iterate through all IDOCs to find the ones that match the criteria.
            for (ITypeContainer container : allIDOCs)
            {
                if ((messageType != null) && messageType.match(container.getValue()))
                {
                    returnValue.add(container);
                }
                else if ((description != null) && description.match(container.getDescription()))
                {
                    returnValue.add(container);
                }
                else
                {
                    // Only if the type container would not have been added, it might be that it
                    // needs to be added based on the method of description. If the match is done
                    // based on these 2 criteria a new trimmed version of the container needs to be
                    // made containing only the items that actually match the citeria.
                    if ((operation != null) || (description != null))
                    {
                        ITypeContainer tempContainer = null;

                        ArrayList<ITypeMetadata> items = new ArrayList<ITypeMetadata>(container
                                                                                      .getItems()
                                                                                      .values());

                        for (ITypeMetadata metadata : items)
                        {
                            IIDOCMetadata idoc = (IIDOCMetadata) metadata;
                            boolean match = false;

                            if ((operation != null) && operation.match(idoc.getType()))
                            {
                                match = true;
                            }

                            // If this method should be included, we need to add it to the temp
                            // container.
                            if (match == true)
                            {
                                if (tempContainer == null)
                                {
                                    tempContainer = SAPMetadataFactory.createContainer(ESAPObjectType.IDOC);
                                    tempContainer.setValue(container.getValue());
                                    tempContainer.setDisplayName(container.getDisplayName());
                                }

                                tempContainer.addType(idoc);
                            }
                        }

                        // If any of the criteria matched, add the container to the result.
                        if (tempContainer != null)
                        {
                            returnValue.add(tempContainer);
                        }
                    }
                }
            }
        }

        return returnValue;
    }

    /**
     * @see  IMetadataCache#searchRFC(boolean, IFilter, IFilter, IFilter)
     */
    @Override public List<ITypeContainer> searchRFC(boolean readFromSAP, IFilter function,
                                                    IFilter group, IFilter description)
                                             throws SAPConnectorException
    {
        List<ITypeContainer> returnValue = new ArrayList<ITypeContainer>();

        // Get the main list of RFCs
        List<ITypeContainer> allRFCs = null;

        if (readFromSAP)
        {
            allRFCs = m_connection.getAllRFCs();
        }
        else
        {
            allRFCs = new ArrayList<ITypeContainer>(m_allRFCs.values());
        }

        // If no filter has been set, then return all RFCs
        if ((function == null) && (group == null) && (description == null))
        {
            returnValue = allRFCs;
        }
        else
        {
            // Now iterate through all IDOCs to find the ones that match the criteria.
            for (ITypeContainer container : allRFCs)
            {
                if ((function != null) && function.match(container.getValue()))
                {
                    returnValue.add(container);
                }
                else
                {
                    // Only if the type container would not have been added, it might be that it
                    // needs to be added based on the method of description. If the match is done
                    // based on these 2 criteria a new trimmed version of the container needs to be
                    // made containing only the items that actually match the citeria.
                    if ((group != null) || (description != null))
                    {
                        ITypeContainer tempContainer = null;

                        ArrayList<ITypeMetadata> items = new ArrayList<ITypeMetadata>(container
                                                                                      .getItems()
                                                                                      .values());

                        for (ITypeMetadata metadata : items)
                        {
                            IRFCMetadata rfc = (IRFCMetadata) metadata;
                            boolean match = false;

                            if ((group != null) && group.match(rfc.getGroupName()))
                            {
                                match = true;
                            }

                            if (description != null)
                            {
                                match = description.match(rfc.getShortText());
                            }

                            // If this method should be included, we need to add it to the temp
                            // container.
                            if (match == true)
                            {
                                if (tempContainer == null)
                                {
                                    tempContainer = SAPMetadataFactory.createContainer(ESAPObjectType.RFC);
                                    tempContainer.setValue(container.getValue());
                                    tempContainer.setDisplayName(container.getDisplayName());
                                }

                                tempContainer.addType(rfc);
                            }
                        }

                        // If any of the criteria matched, add the container to the result.
                        if (tempContainer != null)
                        {
                            returnValue.add(tempContainer);
                        }
                    }
                }
            }
        }

        return returnValue;
    }

    /**
     * This method sets the cache storage to use.
     *
     * @param  storage  The cache storage to use.
     */
    public void setCacheStorage(ICacheStorage storage)
    {
        m_storage = storage;
    }

    /**
     * This method sets the ID for this cache.
     *
     * @param  id  The ID for this cache.
     */
    public void setID(String id)
    {
        m_id = id;
    }

    /**
     * This method sets the SAP connection to use for reading.
     *
     * @param  connection  The SAP connection to use for reading.
     */
    public void setSAPConnection(ISAPConnection connection)
    {
        m_connection = connection;
    }
}
