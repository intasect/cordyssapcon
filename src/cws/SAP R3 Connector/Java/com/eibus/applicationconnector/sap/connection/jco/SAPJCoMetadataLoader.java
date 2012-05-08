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
package com.eibus.applicationconnector.sap.connection.jco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;



import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.MetadataLoader;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.JCO;

/**
 * This class queries the SAP system and gets the metadata in case the middleware is SAP BC. .
 *
 * @author  ygopal
 */
public class SAPJCoMetadataLoader extends MetadataLoader
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPJCoMetadataLoader.class);
    /**
     * DOCUMENTME.
     */
    private JCO.Client client = null;
    /**
     * DOCUMENTME.
     */
    private SAPJCoRequestHandler requestHandler;
    /**
     * DOCUMENTME.
     */
    private SAPJCoRequestSender requestSender = null;
    
    private ISAPConfiguration m_config = null ;

    /**
     * Creates a new SAPJCoMetadataLoader object.
     *
     * @param  config          DOCUMENTME
     * @param  requestHandler  DOCUMENTME
     */
    public SAPJCoMetadataLoader(ISAPConfiguration config, SAPJCoRequestHandler requestHandler)
    {
        super(config.getCacheDirectory(), config.getRFCCacheRoot(), config.getIDOCCacheRoot());
        this.m_config = config;
        this.requestHandler = requestHandler;
    }

    /**
     * This method is used to get the interface of an RFC by using the RFM
     * RFC_GET_FUNCTION_INTERFACE. This is required bacause some of the import parameters and export
     * parameters are data elements and not part of any structure. In such cases, theit data type is
     * not in the response of BDL_FUNCTION_INTERFACE_GET call.
     *
     * <p>This is not required for JCo. So, this method is not implemented.</p>
     *
     * @param   functionName  DOCUMENTME
     * @param   doc           DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#getFunctionInterface(java.lang.String,
     *          com.eibus.xml.nom.Document)
     */
    @Override public int getFunctionInterface(String functionName, Document doc)
                                       throws SAPConnectorException
    {
        return 0;
    }

    /**
     * Make a call to the RFM "SWO_QUERY_API_METHODS" Call this RFM with the following inputs.
     * WITH_OBJECT_NAMES : X WITH_TEXTS : X Rest are default values Realign and write the response
     * to the cache directory. rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadBOMetadata(com.eibus.xml.nom.Document)
     */
    @Override protected int loadBOMetadata(Document doc)
                                    throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int BOMetadataNode = 0;

        try
        {
            m_loadingBOMetadata = true;

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Fetching BO Metadata");
            }
            // BOMetadataNode = requestSender.sendRFCRequestForMetadata( BORequestXML, client,
            // "SWO_QUERY_API_METHODS", cacheDir+"\\"+ BOMetadataFile, doc);
            BOMetadataNode = requestSender.sendRFCRequestForInternalPurpose(TEMPLATE_BO_REQUEST,
                                                                            client,
                                                                            "SWO_QUERY_API_METHODS",
                                                                            "rfc:SWO_QUERY_API_METHODS",
                                                                            doc);

            String[] parentAttributes = { "OBJTYPE", "OBJECTNAME" };
            String commonXPath = "API_METHODS";

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Realigning BO Metadata");
            }
            realignMetadataIntoTree(BOMetadataNode, parentAttributes, commonXPath);

            String filename = m_cacheDir + "\\" + FILE_BO_METADATA;

            try
            {
                Node.writeToFile(BOMetadataNode, BOMetadataNode, filename, 0);
            }
            catch (Exception xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
            m_loadingBOMetadata = false;
            return BOMetadataNode;
        }
        catch (SAPConnectorException sf)
        {
            m_loadingBOMetadata = false;
            BACUtil.deleteNode(BOMetadataNode);
            throw sf;
        }
    }

    /**
     * Make a call to the RFM "RPY_BOR_TREE_INIT" No inputs needed. Realign and write the response
     * to the cache directory. rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadComponentMetadata(com.eibus.xml.nom.Document)
     */
    @Override protected int loadComponentMetadata(Document doc)
                                           throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int ComponentMetadata = 0;

        try
        {
            loadingComponentMetadata = true;

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Fetching Component Metadata");
            }
            // ComponentMetadata = requestSender.sendRFCRequestForMetadata( ComponentRequestXML,
            // client, "RPY_BOR_TREE_INIT", cacheDir+"\\"+ComponentMetadataFile, doc);
            ComponentMetadata = requestSender.sendRFCRequestForInternalPurpose(TEMPLATE_COMPONENT_REQUEST,
                                                                               client,
                                                                               "RPY_BOR_TREE_INIT",
                                                                               "rfc:RPY_BOR_TREE_INIT",
                                                                               doc);

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Realigning Component Metadata");
            }
            realignComponentMetadata(ComponentMetadata);

            String filename = m_cacheDir + "\\ComponentMetadata.xml";

            try
            {
                Node.writeToFile(ComponentMetadata, ComponentMetadata, filename, 0);
            }
            catch (Exception xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
            loadingComponentMetadata = false;
            return ComponentMetadata;
        }
        catch (SAPConnectorException sf)
        {
            loadingComponentMetadata = false;
            BACUtil.deleteNode(ComponentMetadata);
            throw sf;
        }
    }

    /**
     * This method loads the interface of a given IDOCType in the cache directory. The naming
     * convention followed is IDOCInterface_<idocType>.xml. RFC called to get interface is
     * IDOCTYPE_READ_COMPLETE. rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   idocType  DOCUMENTME
     * @param   cimType   DOCUMENTME
     * @param   doc       DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadIDOCInterface(java.lang.String,java.lang.String,
     *          com.eibus.xml.nom.Document)
     */
    @Override protected int loadIDOCInterface(String idocType, String cimType, Document doc)
                                       throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int IDOCRequestNode = 0;
        int IDOCResponseNode = 0;

        try
        {
            loadingIDOCInterface = true;

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Fetching IDOC Interface.");
            }

            try
            {
                IDOCRequestNode = doc.parseString(TEMPLATE_IDOC_INTERFACE_REQUEST);
            }
            catch (Exception xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_PARSING_TEMPLATE,
                                                "IDOCInterfaceRequestXML");
            }

            int idocTypeNode = XPathHelper.selectSingleNode(IDOCRequestNode, "PI_IDOCTYP", m_xmi);
            doc.createText(idocType, idocTypeNode);

            int cimTypeNode = XPathHelper.selectSingleNode(IDOCRequestNode, "PI_CIMTYP", m_xmi);
            doc.createText(cimType, cimTypeNode);
            // IDOCResponseNode = requestSender.sendRFCRequestForMetadata( IDOCRequestNode,
            // client, "IDOCTYPE_READ_COMPLETE", cacheDir+ "\\" + IDOCInterfaceRelativePath +"\\" +
            // IDOCInterfaceCommonName +idocType+".xml", doc);
            IDOCResponseNode = requestSender.sendRFCRequestForInternalPurpose(IDOCRequestNode,
                                                                              client,
                                                                              "IDOCTYPE_READ_COMPLETE",
                                                                              "rfc:IDOCTYPE_READ_COMPLETE",
                                                                              doc);

            String filename = m_cacheDir + "\\" + m_idocInterfaceRelativePath + "\\" +
                              PREFIX_IDOC_INTERFACE + idocType + ".xml";

            try
            {
                Node.writeToFile(IDOCResponseNode, IDOCResponseNode, filename, 0);
            }
            catch (XMLException xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
            loadingIDOCInterface = false;
            return IDOCResponseNode;
        }
        catch (SAPConnectorException sf)
        {
            loadingIDOCInterface = false;
            BACUtil.deleteNode(IDOCRequestNode);
            BACUtil.deleteNode(IDOCResponseNode);
            throw sf;
        }
    }

    /**
     * Make a call to the RFM "IDOCTYPES_FOR_MESTYPE_READ" <P_MESTYP>*</P_MESTYP> Realign and write
     * the response to the cache directory. rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadIDOCMetadata(com.eibus.xml.nom.Document)
     */
    @Override protected int loadIDOCMetadata(Document doc)
                                      throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        int IDOCMetadataNode = 0;

        try
        {
            m_loadingIDOCMetadata = true;

            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Fetching IDOC Metadata");
            }
            // IDOCMetadataNode = requestSender.sendRFCRequestForMetadata( IDOCRequestXML, client,
            // "IDOCTYPES_FOR_MESTYPE_READ", cacheDir+"\\" + IDOCMetadataFile, doc);
            IDOCMetadataNode = requestSender.sendRFCRequestForInternalPurpose(TEMPLATE_IDOC_REQUEST,
                                                                              client,
                                                                              "IDOCTYPES_FOR_MESTYPE_READ",
                                                                              "rfc:IDOCTYPES_FOR_MESTYPE_READ",
                                                                              doc);

            String[] parentAttributes = { "MESTYP", "DESCRP" };
            String commonXPath = "PT_MESSAGES";

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Realigning IDOC Metadata");
            }

            realignMetadataIntoTree(IDOCMetadataNode, parentAttributes, commonXPath);

            String filename = m_cacheDir + "\\IDOCMetadata.xml";

            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Realigned data:\n" + Node.writeToString(IDOCMetadataNode, false));
                }

                Node.writeToFile(IDOCMetadataNode, IDOCMetadataNode, filename, 0);
            }
            catch (XMLException xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
            m_loadingIDOCMetadata = false;
            return IDOCMetadataNode;
        }
        catch (SAPConnectorException sf)
        {
            m_loadingIDOCMetadata = false;
            BACUtil.deleteNode(IDOCMetadataNode);
            throw sf;
        }
    }

    /**
     * This method loads the interface of a given rfc in the cache directory. The naming convention
     * followed is RFCInterface_<rfcName>.xml. RFC called to get interface is
     * BDL_FUNCTION_INTERFACE_GET rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   rfcName  The name of the RFC.
     * @param   doc      The document to use.
     *
     * @return  The response node. The caller needs to delete this XML when it's done.
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadRFCInterface(java.lang.String,
     *          com.eibus.xml.nom.Document)
     */
    @Override protected int loadRFCInterface(String rfcName, Document doc)
                                      throws SAPConnectorException
    {
        int returnValue = 0;

        initializeClientAndRequestSender();

        int rfcRequestNode = 0;

        loadingRFCInterface = true;
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Fetching RFC Interface.");
            }

            try
            {
                rfcRequestNode = doc.parseString(TEMPLATE_RFC_INTERFACE_REQUEST);
            }
            catch (Exception xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_PARSING_TEMPLATE,
                                                "RFCInterfaceRequestXML");
            }

            int funcNameNode = XPathHelper.selectSingleNode(rfcRequestNode, "FUNCNAME", m_xmi);
            doc.createText(rfcName, funcNameNode);

            returnValue = requestSender.sendRFCRequestForInternalPurpose(rfcRequestNode, client,
                                                                             "BDL_FUNCTION_INTERFACE_GET",
                                                                             "rfc:BDL_FUNCTION_INTERFACE_GET",
                                                                             doc);

            String filename = m_cacheDir + "\\" + m_rfcInterfaceRelativePath + "\\" +
                              PREFIX_RFC_INTERFACE + rfcName + ".xml";

            try
            {
                Node.writeToFile(returnValue, returnValue, filename, 0);
            }
            catch (XMLException xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
        }
        catch (SAPConnectorException sf)
        {
        	returnValue = BACUtil.deleteNode(returnValue);
            throw sf;
        }
        finally
        {
        	loadingRFCInterface = false;
        	
        	rfcRequestNode = BACUtil.deleteNode(rfcRequestNode);
        }
        
        return returnValue;
    }

    /**
     * Make a call to the RFM "RFC_FUNCTION_SEARCH" call this RFM with the following input
     * <FUNCNAME>*</FUNCNAME> rfc: prefix is added to keep it in sync with BC metadata
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.MetadataLoader#loadRFCMetadata(com.eibus.xml.nom.Document)
     */
    @Override protected int loadRFCMetadata(Document doc)
                                     throws SAPConnectorException
    {
        initializeClientAndRequestSender();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Fetching RFC Metadata");
        }

        int rfcResponseNode = 0;

        loadingRFCMetadata = true;
        
        try
        {
            rfcResponseNode = requestSender.sendRFCRequestForInternalPurpose(TEMPLATE_RFC_REQUEST,
                                                                             client,
                                                                             "RFC_FUNCTION_SEARCH",
                                                                             "rfc:RFC_FUNCTION_SEARCH",
                                                                             doc);

            String filename = m_cacheDir + "\\RFCMetadata.xml";

            try
            {
                Node.writeToFile(rfcResponseNode, rfcResponseNode, filename, 0);
            }
            catch (XMLException xe)
            {
                throw new SAPConnectorException(xe,
                                                SAPConnectorExceptionMessages.ERROR_WRITING_FILE,
                                                filename);
            }
            return rfcResponseNode;
        }
        catch (SAPConnectorException sf)
        {
            
            throw sf;
        }
        finally
        {
        	loadingRFCMetadata = false;
        }
    }

    /**
     * This method gets the JCO connection and the request sender from the RequestHandler object.
     * These are required only to load the metadata from SAP server.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void initializeClientAndRequestSender()
                                           throws SAPConnectorException
    {
        if (client == null)
        {
            requestHandler.initializeClientAndRequestSender();
            client = requestHandler.getConnection();
            requestSender = requestHandler.getRequestSender();
        }
    }
    
    
    /**This method serializes the JCO IDOC Metadata object into a file with extension ".o". The file is created in the cache directory.
     * This does not delete the file from cache directory.
     * @param request
     * @param response
     * @return
     */
    public boolean getSerializedIDOCMetadataObject(int request, int response)
    {
    	String messageType = Node.getDataElement(request, "mesgtype", "") ;
    	String idocType = Node.getDataElement(request, "idoctype", "") ;
    	String cimType = Node.getDataElement(request, "cimtype", "") ; 
    	//IDoc.Document idoc = JCoIDoc.createDocument( this.m_config.getIDOCRepository(), "DEBMAS06", "YSADEBMAS06");
    	IDoc.Document idoc = JCoIDoc.createDocument( this.m_config.getIDOCRepository(), idocType, cimType);
        IDoc.Segment rootSegment = idoc.getRootSegment();
        String fileName = "" ;
        if(Util.isSet(cimType))
        {
        	 fileName = m_cacheDir + File.separator+cimType+ ".o";
        }
        else if(Util.isSet(idocType))
        {
        	fileName = m_cacheDir + File.separator+idocType+ ".o";
        }
        else
        {        	
        	return false;
        }
        try 
        {
			FileOutputStream fos = new FileOutputStream(fileName) ;
			ObjectOutputStream oBjos = new ObjectOutputStream(fos) ;
			oBjos.writeObject(rootSegment.getSegmentMetaData()) ;
			oBjos.close();
			fos.close();
			String fileContent = Util.readFileAndEncode(fileName);
			Node.createTextElement("object", fileContent, response);
			return true;
		} catch (FileNotFoundException e) {	
			LOG.error(e.getLocalizedMessage()) ;
		
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage()) ;
		
		}  
		return false;
    }
    
    
   
}
