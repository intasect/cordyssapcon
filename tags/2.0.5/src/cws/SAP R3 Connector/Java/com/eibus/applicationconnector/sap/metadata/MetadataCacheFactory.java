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
import com.eibus.applicationconnector.sap.metadata.storage.CacheStorageFactory;
import com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage;

/**
 * This class can create new instances of the metadata caches.
 *
 * @author  pgussow
 */
public class MetadataCacheFactory
{
    /**
     * This method creates the default metadata cache. This cache will use the file system based
     * cache storage.
     *
     * @param   id            The ID for this cache repository.
     * @param   connection    The SAP connection that should be used to access SAP.
     * @param   organization  The current organization.
     *
     * @return  The metadata cache.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static IMetadataCache createCache(String id, ISAPConnection connection,
                                             String organization)
                                      throws SAPConnectorException
    {
        ICacheStorage storage = CacheStorageFactory.createDefaultCacheStorage(organization);

        IMetadataCache returnValue = new MetadataCache(id, storage, connection);

        return returnValue;
    }

    /**
     * This method creates the metadata cache using the specified cache storage.
     *
     * @param   id          The ID for this cache repository.
     * @param   connection  The SAP connection that should be used to access SAP.
     * @param   storage     The cache storage provider.
     *
     * @return  The metadata cache.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static IMetadataCache createCache(String id, ISAPConnection connection,
                                             ICacheStorage storage)
                                      throws SAPConnectorException
    {
        IMetadataCache returnValue = new MetadataCache(id, storage, connection);

        return returnValue;
    }
}
