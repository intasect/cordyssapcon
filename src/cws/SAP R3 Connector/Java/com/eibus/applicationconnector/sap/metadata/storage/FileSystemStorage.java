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
 package com.eibus.applicationconnector.sap.metadata.storage;

import com.cordys.coe.exception.ServerLocalizableException;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;
import com.eibus.applicationconnector.sap.metadata.IMetadataCache;
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;
import com.eibus.applicationconnector.sap.metadata.types.SAPMetadataFactory;
import com.eibus.applicationconnector.sap.soap.ClearCache;
import com.eibus.applicationconnector.sap.util.BACUtil;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.EIBProperties;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;

import java.util.Map;

/**
 * This class is able to persist metadata caches.
 *
 * @author  pgussow
 */
class FileSystemStorage
    implements ICacheStorage
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(FileSystemStorage.class);
    /**
     * Holds the internal XML document that is used for the cache.
     */
    private Document m_doc = new Document();
    /**
     * Holds the current organization.
     */
    private String m_organization;
    /**
     * Holds the root folder to use.
     */
    private File m_rootFolder;

    /**
     * Creates a new FileSystemCache object.
     *
     * @param  organization  The current organization.
     */
    public FileSystemStorage(String organization)
    {
        m_organization = organization;

        m_rootFolder = new File(ClearCache.getLocal_cacheDirectory());

        if (!m_rootFolder.exists())
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating loacl cache folder: " + m_rootFolder.getAbsolutePath());
            }
            m_rootFolder.mkdirs();
        }
    }

    /**
     * This method gets the current organization.
     *
     * @return  The current organization.
     */
    public String getOrganization()
    {
        return m_organization;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage#loadCache(java.lang.String,
     *       com.eibus.applicationconnector.sap.metadata.IMetadataCache)
     */
    @Override public void loadCache(String id, IMetadataCache cache)
                             throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading cache with ID " + id);
        }

        File reposRoot = new File(m_rootFolder, id);

        if (reposRoot.exists())
        {
            fillCache(reposRoot, cache);
        }
        else if (LOG.isDebugEnabled())
        {
            LOG.debug("No cache folder is found. This means an empty cache will not be filled.");
        }
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.storage.ICacheStorage#persistCache(com.eibus.applicationconnector.sap.metadata.IMetadataCache)
     */
    @Override public void persistCache(IMetadataCache cache)
                                throws SAPConnectorException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Persisting cache with ID " + cache.getID());
        }

        File reposRoot = new File(m_rootFolder, cache.getID());

        persistCache(reposRoot, cache);
    }

    /**
     * This method sets the current organization.
     *
     * @param  organization  The current organization.
     */
    public void setOrganization(String organization)
    {
        m_organization = organization;
    }

    /**
     * This method will load all the data from the file system.
     *
     * @param   reposRoot  The root folder to load the data from.
     * @param   cache      The cache to fill with this data.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void fillCache(File reposRoot, IMetadataCache cache)
                    throws SAPConnectorException
    {
        // Load the RFCs.
        loadSAPObjects(reposRoot, ESAPObjectType.RFC, cache);

        // Load the BAPIs.
        loadSAPObjects(reposRoot, ESAPObjectType.BAPI, cache);

        // Load all IDOCs.
        loadSAPObjects(reposRoot, ESAPObjectType.IDOC, cache);

        // Load the component structure.
        loadComponentStructure(new File(reposRoot, "component"), cache);
    }

    /**
     * DOCUMENTME.
     *
     * @param  file   DOCUMENTME
     * @param  cache  DOCUMENTME
     */
    private void loadComponentStructure(File file, IMetadataCache cache)
    {
    }

    /**
     * This method will load all BAPIs from the cache.
     *
     * @param   reposRoot  The root folder where to load the objects from.
     * @param   type       The type of object to load.
     * @param   cache      The cache to fill.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void loadSAPObjects(File reposRoot, ESAPObjectType type, IMetadataCache cache)
                         throws SAPConnectorException
    {
        String rootTag = type.name().toLowerCase() + "s";

        File actualFile = new File(reposRoot, rootTag + ".xml");

        int root = 0;

        try
        {
            root = m_doc.load(actualFile.getAbsolutePath());

            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_SCHEMA);

            int[] objects = XPathHelper.selectNodes(root, "ns:sapobject", xmi);

            for (int sapObject : objects)
            {
                ITypeContainer container = SAPMetadataFactory.parseContainer(sapObject, xmi);

                switch (type)
                {
                    case BAPI:
                        cache.addBAPI(container);
                        break;

                    case IDOC:
                        cache.addIDOC(container);
                        break;

                    case RFC:
                        cache.addRFC(container);
                        break;

                    default:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            if (e instanceof ServerLocalizableException)
            {
                ServerLocalizableException sle = (ServerLocalizableException) e;
                throw new SAPConnectorException(e, sle.getMessageObject(),
                                                sle.getMessageParameters());
            }

            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.ERROR_LOADING_METADATA_FILE,
                                            actualFile.getAbsolutePath());
        }
        finally
        {
            root = BACUtil.deleteNode(root);
        }
    }

    /**
     * This method will write the current cache to the file system.
     *
     * @param   reposRoot  The root folder for the cache.
     * @param   cache      The actual cache to store.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void persistCache(File reposRoot, IMetadataCache cache)
                       throws SAPConnectorException
    {
        if (!reposRoot.exists())
        {
            reposRoot.mkdirs();
        }

        persistSAPMetadata(reposRoot, ESAPObjectType.BAPI, cache.getAllBAPIs());
        persistSAPMetadata(reposRoot, ESAPObjectType.RFC, cache.getAllRFCs());
        persistSAPMetadata(reposRoot, ESAPObjectType.IDOC, cache.getAllIDOCs());
    }

    /**
     * This method stores the type containers to a file. First an XML is built up which is then
     * written to file.
     *
     * @param   reposRoot   The root folder where to write the file.
     * @param   objectType  The type of objects.
     * @param   allObjects  The map containing the objects to persist.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void persistSAPMetadata(File reposRoot, ESAPObjectType objectType,
                                    Map<String, ITypeContainer> allObjects)
                             throws SAPConnectorException
    {
        String rootTag = objectType.name().toLowerCase() + "s";

        File actualFile = new File(reposRoot, rootTag + ".xml");

        if (LOG.isDebugEnabled())
        {
            LOG.debug((actualFile.exists() ? "Overwriting" : "Creating") + " file " +
                      actualFile.getAbsolutePath() + " already exists. It will be overwritten.");
        }

        int root = m_doc.createElementNS(rootTag, null, "", SAPConnectorConstants.NS_SAP_SCHEMA, 0);

        try
        {
            // Dump all objects to XML.
            for (ITypeContainer container : allObjects.values())
            {
                int object = Node.createElementWithParentNS("sapobject", null, root);
                container.toXML(object);
            }

            // Write the XML to a file.
            try
            {
                Node.writeToFile(root, root, actualFile.getAbsolutePath(),
                                 Node.WRITE_PRETTY | Node.WRITE_HEADER);
            }
            catch (Exception e)
            {
                throw new SAPConnectorException(e,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_CACHE_FILE,
                                                actualFile.getAbsolutePath());
            }
        }
        finally
        {
            root = BACUtil.deleteNode(root);
        }
    }
}
