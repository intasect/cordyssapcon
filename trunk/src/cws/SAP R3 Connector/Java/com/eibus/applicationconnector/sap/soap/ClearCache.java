/**
 * Copyright 2011 Cordys R&D B.V.
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
package com.eibus.applicationconnector.sap.soap;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;
import com.eibus.soap.BodyBlock;
import com.eibus.util.system.EIBProperties;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This method clears the cache data and reloads again.
 *
 * @author srkrishnan
 */
public class ClearCache extends BaseMethod{

    ISAPConfiguration config;

    /**
     * Constructor.
     *
     * @param  request   The request bodyblock.
     * @param  response  The response bodyblock.
     * @param  config    The configuration of the connector.
     */
    public ClearCache(BodyBlock request, BodyBlock response, ISAPConfiguration config) {
        super(request, response, config);
        this.config = config;
    }

    public void deleteCacheFiles() {
        try {
            String cachedir;
            File m_rootFolder = new File(EIBProperties.getInstallDir(),
                                SAPConnectorConstants.ROOT_CACHE_FOLDER);
            
            deleteFile(m_rootFolder.getAbsolutePath());
            cachedir = config.getCacheDirectory() + "\\" + config.getIDOCCacheRoot();
            deleteFile(cachedir);
            cachedir = config.getCacheDirectory() + "\\" + config.getRFCCacheRoot();
            deleteFile(cachedir);
            IMetadataCache cache = getConfiguration().getMetadataCache();
            cache.reloadAllData();
        } catch (SAPConnectorException ex) {
            Logger.getLogger(ClearCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean deleteFile(String sFilePath) {
        File oFile = new File(sFilePath);
        if (oFile.isDirectory()) {
            File[] aFiles = oFile.listFiles();
            for (File oFileCur : aFiles) {
                deleteFile(oFileCur.getAbsolutePath());
            }
        }
        return oFile.delete();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.soap.BaseMethod#execute()
     */
    @Override
    public void execute() throws SAPConnectorException {
        deleteCacheFiles();
    }

    
}
