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

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.io.File;

import java.util.Arrays;
import java.util.List;

/**
 * This abstract class is the super class which is used to get the metadata of SAP. This class has
 * methods to read metadata from cache directory and return. Loading the cache directory has to be
 * handled separately for BC and JCo. This is achieved by having two subclasses, one specific to BC
 * and one specific to JCo.
 *
 * @author  ygopal
 */
public abstract class MetadataLoader
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MetadataLoader.class);
    /**
     * Indicates whether or not the component metadata is already being loaded.
     */
    protected static volatile boolean loadingComponentMetadata = false;
    /**
     * Indicates whether or not the BAPI metadata is already being loaded.
     */
    protected static volatile boolean m_loadingBOMetadata = false;
    /**
     * Indicates whether or not the RFC metadata is already being loaded.
     */
    protected static volatile boolean loadingRFCMetadata = false;
    /**
     * Indicates whether or not the IDOC metadata is already being loaded.
     */
    protected static volatile boolean m_loadingIDOCMetadata = false;
    /**
     * Indicates whether or not the IDOC interface metadata is already being loaded.
     */
    protected static volatile boolean loadingIDOCInterface = false;
    /**
     * Indicates whether or not the RFC interface metadata is already being loaded.
     */
    protected static volatile boolean loadingRFCInterface = false;
    /**
     * Holds the name of the file in which the BAPI metadata information is cached.
     */
    protected static final String FILE_BO_METADATA = "BOMetadata.xml";
    /**
     * Holds the name of the file in which the RFC metadata information is cached.
     */
    protected static final String FILE_RFC_METADATA = "RFCMetadata.xml";
    /**
     * Holds the name of the file in which the IDOC metadata information is cached.
     */
    protected static final String FILE_IDOC_METADATA = "IDOCMetadata.xml";
    /**
     * Holds the name of the file in which the Component metadata information is cached.
     */
    protected static final String FILE_COMPONENT_METADATA = "ComponentMetadata.xml";
    /**
     * Prefix for RFC interface metadata.
     */
    protected static final String PREFIX_RFC_INTERFACE = "RFCInterface_";
    /**
     * Prefix for IDOC interface metadata.
     */
    protected static final String PREFIX_IDOC_INTERFACE = "IDOCInterface_";
    /**
     * Request XMLs to get the metadata from the SAP server.
     */
    protected static final String TEMPLATE_BO_REQUEST = new String("<SWO_QUERY_API_METHODS xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><LANGUAGE></LANGUAGE><METHOD></METHOD><OBJTYPE></OBJTYPE><WITH_IMPL_METHODS></WITH_IMPL_METHODS><WITH_INTERNAL_API_METHODS></WITH_INTERNAL_API_METHODS><WITH_OBJECT_NAMES>X</WITH_OBJECT_NAMES><WITH_TEXTS>X</WITH_TEXTS></SWO_QUERY_API_METHODS>");
    /**
     * Holds the template XML to search for RFCs.
     */
    protected static final String TEMPLATE_RFC_REQUEST = new String("<RFC_FUNCTION_SEARCH xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><FUNCNAME>*</FUNCNAME></RFC_FUNCTION_SEARCH>");
    /**
     * Holds the template XML to search for IDOCs.
     */
    protected static final String TEMPLATE_IDOC_REQUEST = new String("<IDOCTYPES_FOR_MESTYPE_READ xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><P_MESTYP>*</P_MESTYP></IDOCTYPES_FOR_MESTYPE_READ>");
    /**
     * Holds the template XML to search for Components.
     */
    protected static final String TEMPLATE_COMPONENT_REQUEST = new String("<RPY_BOR_TREE_INIT xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><FILTER_MISCELLANEOUS><COMPHIER>X</COMPHIER></FILTER_MISCELLANEOUS><FILTER_OBJECT_TYPES><ALLOBJTYPS>X</ALLOBJTYPS></FILTER_OBJECT_TYPES></RPY_BOR_TREE_INIT>");
    /**
     * Holds the template XML to search for RFC Interfaces.
     */
    protected static final String TEMPLATE_RFC_INTERFACE_REQUEST = new String("<BDL_FUNCTION_INTERFACE_GET xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><FUNCNAME/><CHANGING_PARAMETER/><DFIES_TAB/><EXCEPTION_LIST/><EXPORT_PARAMETER/><IMPORT_PARAMETER/><NAMETAB_INFO/><P_DOCU/><TABLES_PARAMETER/><TABLE_HEADERS/></BDL_FUNCTION_INTERFACE_GET>");
    /**
     * Holds the template XML to search for IDOC Interfaces.
     */
    protected static final String TEMPLATE_IDOC_INTERFACE_REQUEST = new String("<IDOCTYPE_READ_COMPLETE xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><PI_IDOCTYP/><PI_CIMTYP/><PI_RELEASE/><PI_VERSION/><PT_FIELDS/><PT_FVALUES/><PT_MESSAGES/><PT_SEGMENTS/></IDOCTYPE_READ_COMPLETE>");
    /**
     * Holds the root cache folder.
     */
    protected String m_cacheDir;
    /**
     * Holds the relative path for the IDOC interfaces.
     */
    protected String m_idocInterfaceRelativePath;
    /**
     * Holds the relative path for the RFC interfaces.
     */
    protected String m_rfcInterfaceRelativePath;
    /**
     * Holds the XPath metadata information.
     */
    protected XPathMetaInfo m_xmi = new XPathMetaInfo();

    /**
     * Creates a new MetadataLoader object.
     *
     * @param  cacheDir                   DOCUMENTME
     * @param  RFCInterfaceRelativePath   DOCUMENTME
     * @param  IDOCInterfaceRelativePath  DOCUMENTME
     */
    protected MetadataLoader(String cacheDir, String RFCInterfaceRelativePath,
                             String IDOCInterfaceRelativePath)
    {
        this.m_cacheDir = cacheDir;
        this.m_rfcInterfaceRelativePath = RFCInterfaceRelativePath;
        this.m_idocInterfaceRelativePath = IDOCInterfaceRelativePath;

        m_xmi.addNamespaceBinding("rfc", "urn:sap-com:document:sap:rfc:functions");
        m_xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_SCHEMA);
    }

    /**
     * DOCUMENTME.
     *
     * @param   functionName  DOCUMENTME
     * @param   doc           DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public abstract int getFunctionInterface(String functionName, Document doc)
                                      throws SAPConnectorException;

    /**
     * This method returns the interface of a given IDOC type. This method can be called from
     * outside classes also. Will be useful to get the interface of an IDOC while publishing methods
     * in LDAP. If cacheNode is 0, it loads the interface from SAP server.
     *
     * @param   idocType   : Name of the idocType
     * @param   cimType    : Name of the idoc Type extension
     * @param   overwrite  : String . If true, metadata is fetched afresh from SAP.
     * @param   doc        : Document object to be used.
     *
     * @return  : interface as XML with "IDOCInterface" as root node
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int getIDOCInterface(String idocType, String cimType, boolean overwrite,
                                Document doc)
                         throws SAPConnectorException
    {
        if ("".equals(idocType))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        String IDOCInterfaceFileName = m_idocInterfaceRelativePath + "\\" + PREFIX_IDOC_INTERFACE +
                                       idocType + ".xml";
        int cacheNode = getRequiredMetadataRoot(overwrite, "IDOC Interface", idocType,
                                                IDOCInterfaceFileName, loadingIDOCInterface, doc,
                                                cimType);
        int interfaceNode = doc.createElement("IDOCInterface");
        Node.duplicateAndAppendToChildren(Node.getFirstChild(cacheNode),
                                          Node.getLastChild(cacheNode), interfaceNode);
        Node.delete(cacheNode);
        return interfaceNode;
    }

    /**
     * This method is called from the handler, if the implementation type is SAPMetadata Based on
     * the LDAP method name, this method calls the appropriate method to return the metadata.
     *
     * @param   request   The request XML.
     * @param   response  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public boolean getMetadata(BodyBlock request, BodyBlock response)
                        throws SAPConnectorException
    {
        int requestNode = request.getXMLNode();
        int responseNode = response.getXMLNode();
        
        getMetadata(requestNode, responseNode);
        
        return true;
    }

	/**
	 * @param requestNode
	 * @param responseNode
	 * @throws SAPConnectorException
	 */
	public void getMetadata(int requestNode, int responseNode)
			throws SAPConnectorException
	{
		Document doc = Node.getDocument(requestNode);
        String methodName = Node.getLocalName(requestNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Method Name is " + methodName);
        }

        if (methodName.equalsIgnoreCase("GetComponents"))
        {
             getComponentMetadata(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetBOs"))
        {
             getBOMetadata(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetBAPIs"))
        {
             getBAPIMetadata(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetRFCs"))
        {
             getRFCMetadata(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetIDOCMessageTypes"))
        {
             getIDOCMessageTypes(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetIDOCTypesForMesType"))
        {
            getIDOCTypesForMessageType(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetRFCInterface"))
        {
            getRFCInterface(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("GetIDOCInterface"))
        {
            getIDOCInterface(requestNode, responseNode);
        }
        else if (methodName.equalsIgnoreCase("LoadComponentMetadata"))
        {
            int componentMetadata = loadComponentMetadata(doc);
            BACUtil.deleteNode(componentMetadata);
        }
        else if (methodName.equalsIgnoreCase("LoadBOMetadata"))
        {
            int BOMetadata = loadBOMetadata(doc);
            BACUtil.deleteNode(BOMetadata);
        }
        else if (methodName.equalsIgnoreCase("LoadRFCMetadata"))
        {
            int RFCMetadata = loadRFCMetadata(doc);
            BACUtil.deleteNode(RFCMetadata);
        }
        else if (methodName.equalsIgnoreCase("LoadIDOCMetadata"))
        {
            int IDOCMetadata = loadIDOCMetadata(doc);
            BACUtil.deleteNode(IDOCMetadata);
        }
        else if (methodName.equalsIgnoreCase("GetSerializedMetadataObjectForIDOC"))
        {
            int IDOCMetadata = loadIDOCMetadata(doc);
            BACUtil.deleteNode(IDOCMetadata);
        }
        else
        {
        	throw new SAPConnectorException(SAPConnectorExceptionMessages.UNSUPPORTED_METADATA_METHOD, methodName);
        }
	}

    /**
     * This method returns the interface of a given RFC. This method can be called from outside
     * classes also. Will be useful to get the interface of an RFC while publishing methods in LDAP.
     * If cacheNode is 0, it loads the interface from SAP server.
     *
     * @param   rfcName    : Name of the rfc
     * @param   overwrite  : String . If true, metadata is fetched afresh from SAP.
     * @param   doc        : Document object to be used.
     *
     * @return  : interface as XML with "RFCInterface" as root node
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int getRFCInterface(String rfcName, boolean overwrite, Document doc)
                        throws SAPConnectorException
    {
        if ("".equals(rfcName))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        String RFCInterfaceFileName = m_rfcInterfaceRelativePath + "\\" + PREFIX_RFC_INTERFACE +
                                      rfcName + ".xml";
        int cacheNode = getRequiredMetadataRoot(overwrite, "RFC Interface", rfcName,
                                                RFCInterfaceFileName, loadingRFCInterface, doc,
                                                null);
        int interfaceNode = doc.createElement("RFCInterface");
        Node.duplicateAndAppendToChildren(Node.getFirstChild(cacheNode),
                                          Node.getLastChild(cacheNode), interfaceNode);
        Node.delete(cacheNode);
        return interfaceNode;
    }

    /**
     * DOCUMENTME.
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadBOMetadata(Document doc)
                                   throws SAPConnectorException;

    /**
     * DOCUMENTME.
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadComponentMetadata(Document doc)
                                          throws SAPConnectorException;

    /**
     * DOCUMENTME.
     *
     * @param   idocType  DOCUMENTME
     * @param   cimType   DOCUMENTME
     * @param   doc       DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadIDOCInterface(String idocType, String cimType, Document doc)
                                      throws SAPConnectorException;

    /**
     * DOCUMENTME.
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadIDOCMetadata(Document doc)
                                     throws SAPConnectorException;

    /**
     * DOCUMENTME.
     *
     * @param   rfcName  DOCUMENTME
     * @param   doc      DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadRFCInterface(String rfcName, Document doc)
                                     throws SAPConnectorException;

    /**
     * DOCUMENTME.
     *
     * @param   doc  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract int loadRFCMetadata(Document doc)
                                    throws SAPConnectorException;

    /**
     * This method sets the flags to false in case if the handle to a thread is lost while it is
     * loading metadata from SAP.
     * 
     * @see  java.lang.Object#finalize()
     */
    @Override protected void finalize()
    {
        loadingComponentMetadata = false;
        m_loadingBOMetadata = false;
        loadingRFCMetadata = false;
        m_loadingIDOCMetadata = false;
        loadingIDOCInterface = false;
        loadingRFCInterface = false;
    }

    /**
     * This method gets the metadata of all the BAPIs in an object type, reading from the cache
     * directory.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getBAPIMetadata(int requestNode, int responseNode)
                               throws SAPConnectorException
    {
        // Read the request details.
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String businessObject = XPathHelper.getStringValue(requestNode, "ns:BO", m_xmi, "")
                                           .toUpperCase();

        // Check the input variables.
        if (!Util.isSet(businessObject))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_OBJECT_TYPE_IS_EMPTY);
        }

        // Create tuple/old/BAPIs structure.
        int bapis = createTupleStructure(responseNode, "BAPIs");

        // Get the actual definitions.
        return getBAPIMetadata(bapis, businessObject, overwrite);
    }

    /**
     * This method retrieves the metadata of object types from the cache directory. It gets the all
     * the BOs in between the fromBO and toBO parameters.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getBOMetadata(int requestNode, int responseNode)
                             throws SAPConnectorException
    {
        // Get the information from the request.
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String fromBO = XPathHelper.getStringValue(requestNode, "ns:fromBO", m_xmi, "");
        String toBO = XPathHelper.getStringValue(requestNode, "ns:toBO", m_xmi, "");

        // Convert to upper case as names have only capital letters in SAP
        // When fromBO and toBO are sent empty
        if (!Util.isSet(fromBO))
        {
            fromBO = new String("/");
        }

        if (!Util.isSet(toBO))
        {
            toBO = new String("zzzzzzzzzzz");
        }

        // Create the tuple/old structure
        int businessObjectsNode = createTupleStructure(responseNode, "BusinessObjects");

        return getBOMetadata(businessObjectsNode, fromBO, toBO, overwrite);
    }

    /**
     * To get the component metadata from the cache directory. Retrieves the components based on
     * PARENT, i.e all the child components and BOs of a component.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getComponentMetadata(int requestNode, int responseNode)
                                    throws SAPConnectorException
    {
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String parent = XPathHelper.getStringValue(requestNode, "ns:parent", m_xmi, "");
        String tempLevel = XPathHelper.getStringValue(requestNode, "ns:level", m_xmi, "");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Parent id is " + parent + " Level is " + tempLevel);
        }

        if (!Util.isSet(parent) || !Util.isSet(tempLevel))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        // Parse the level.
        int level = 0;

        try
        {
            level = Integer.parseInt(tempLevel);
        }
        catch (NumberFormatException nfe)
        {
            throw new SAPConnectorException(nfe, SAPConnectorExceptionMessages.INVALID_NUMBER,
                                            tempLevel);
        }

        // Create response structure.
        int componentsNode = createTupleStructure(responseNode, "Components");

        // Get the actual components data.
        return getComponentMetadata(componentsNode, parent, level, overwrite);
    }

    /**
     * This method gets the complete interface of an IDOCType. First it checks in the cache
     * directory. If not found in the cache directory, it gets the interface from SAP server.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getIDOCInterface(int requestNode, int responseNode)
                                throws SAPConnectorException
    {
        // Read the request parameters
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String idocType = XPathHelper.getStringValue(requestNode, "ns:IDOCType", m_xmi, "");
        String cimType = XPathHelper.getStringValue(requestNode, "ns:CIMType", m_xmi, "");

        // Generate the interface
        int interfaceNode = getIDOCInterface(idocType, cimType, overwrite,
                                             Node.getDocument(responseNode));

        // Create response structure.
        int tuple = Node.createElementWithParentNS("tuple", null, responseNode);
        int old = Node.createElementWithParentNS("old", null, tuple);
        Node.appendToChildren(interfaceNode, old);

        return true;
    }

    /**
     * This method gets the metadata of all the IDOC message types between the fromMessageType and
     * toMessageType parameters reading from the cache directory.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getIDOCMessageTypes(int requestNode, int responseNode)
                                   throws SAPConnectorException
    {
        // Read the request parameters
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String fromMsgType = XPathHelper.getStringValue(requestNode, "ns:fromMSGTYPE", m_xmi, "")
                                        .toUpperCase();
        String toMsgType = XPathHelper.getStringValue(requestNode, "ns:toMSGTYPE", m_xmi, "")
                                      .toUpperCase();

        // When fromMsgType and toMsgType are sent empty
        if (!Util.isSet(fromMsgType))
        {
            fromMsgType = new String("/");
        }

        if (!Util.isSet(toMsgType))
        {
            toMsgType = new String("zzzzzzz");
        }

        // Create response structure.
        int messageTypesNode = createTupleStructure(responseNode, "MessageTypes");

        // Create the message types.
        return getIDOCMessageTypes(messageTypesNode, fromMsgType, toMsgType, overwrite);
    }

    /**
     * This method gets all the IDOCTypes for a given MessageType , by reading from the cache
     * directory.
     *
     * @param   requestNode   The request node.
     * @param   responseNode  The response node.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getIDOCTypesForMessageType(int requestNode, int responseNode)
                                          throws SAPConnectorException
    {
        // Read the request parameters
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String mesType = XPathHelper.getStringValue(requestNode, "ns:MESTYPE", m_xmi, "")
                                    .toUpperCase();

        // Create response structure.
        int idocsNode = createTupleStructure(responseNode, "IDOCTypes");

        // Create the message types.
        return getIDOCTypesForMessageType(idocsNode, mesType, overwrite);
    }

    /**
     * This method gets the corresponding RFC Name for a given BAPI. It finds the RFC name from
     * BOMetadata.xml in the cache directory. If overwrite is true or not found in the cache
     * directory, it loads the BO Metadata from the SAP server.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getMappedRFC(int requestNode, int responseNode)
                            throws SAPConnectorException
    {
        // Read the request parameters.
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String bapiName = XPathHelper.getStringValue(requestNode, "ns:BAPIName", m_xmi, "");
        String objType = XPathHelper.getStringValue(requestNode, "ns:ObjectType", m_xmi, "");

        if (!Util.isSet(bapiName) || !Util.isSet(objType))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_BAPI_NAME_OR_BO_NAME_IS_EMPTY);
        }

        // Get the mapped RFC
        String mappedRFC = getMappedRFC(objType, bapiName, overwrite,
                                        Node.getDocument(responseNode));

        // Create the response structure
        int tuple = Node.createElementWithParentNS("tuple", null, responseNode);
        int old = Node.createElementWithParentNS("old", null, tuple);
        Node.createElementWithParentNS("MappedRFCName", mappedRFC, old);

        return true;
    }

    /**
     * This method returns the corresponding RFC name for a given BO and BAPI. This can be called
     * from other classes also. Will be useful while publishing BAPI methods in LDAP.
     *
     * @param   objType    Object Type Name
     * @param   bapiName   BAPI Name
     * @param   overwrite  If true, metadata is fetched afresh from SAP.
     * @param   doc        Document object to be used
     *
     * @return  the mapped RFC Name as string
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected String getMappedRFC(String objType, String bapiName, boolean overwrite,
                                  Document doc)
                           throws SAPConnectorException
    {
        String returnValue = null;
        int cacheNode = getRequiredMetadataRoot(overwrite, "BO Metadata", "", FILE_BO_METADATA,
                                                m_loadingBOMetadata, doc, null);

        try
        {
            String xpath = "API_METHODS/item[@OBJTYPE=\"" + objType + "\"]/item[@METHOD=\"" +
                           bapiName + "\"]";

            int bapiItem = XPathHelper.selectSingleNode(cacheNode, xpath, m_xmi);

            returnValue = Node.getAttribute(bapiItem, "FUNCTION");
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return returnValue;
    }

    /**
     * This method gets the complete interface of an RFC. First it checks in the cache directory. If
     * not found in the cache directory, it gets the interface from SAP server.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getRFCInterface(int requestNode, int responseNode)
                               throws SAPConnectorException
    {
        // Read the request parameters.
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String rfcName = XPathHelper.getStringValue(requestNode, "ns:RFCName", m_xmi, "");

        if (!Util.isSet(rfcName))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.RFC_NAME_NOT_SET);
        }

        // Get the RFC interface
        int interfaceNode = getRFCInterface(rfcName, overwrite, Node.getDocument(responseNode));

        // Create the response structure
        int tuple = Node.createElementWithParentNS("tuple", null, responseNode);
        int old = Node.createElementWithParentNS("old", null, tuple);
        Node.appendToChildren(interfaceNode, old);

        return true;
    }

    /**
     * This method gets the metadata of all the RFC between the fromRFC and toRFC parameters reading
     * from the cache directory.
     *
     * @param   requestNode   The request XML.
     * @param   responseNode  The response XML.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected boolean getRFCMetadata(int requestNode, int responseNode)
                              throws SAPConnectorException
    {
        // Read the request parameters.
        boolean overwrite = XPathHelper.getBooleanValue(requestNode, "ns:overwrite", m_xmi, true);
        String fromRFC = XPathHelper.getStringValue(requestNode, "ns:fromRFC", m_xmi, "")
                                    .toUpperCase();
        String toRFC = XPathHelper.getStringValue(requestNode, "ns:toRFC", m_xmi, "");

        // When fromRFC and toRFC are sent empty. RFC names may start with characters other than
        // a-z, A-Z.
        if (!Util.isSet(fromRFC))
        {
            fromRFC = new String("/");
        }

        if (!Util.isSet(toRFC))
        {
            toRFC = new String("zzzzzzzz");
        }

        int rfcsNode = createTupleStructure(responseNode, "RFCs");

        return getRFCMetadata(rfcsNode, fromRFC, toRFC, overwrite);
    }

    /**
     * This method realigns the component metadata into a tree structure. And this method also
     * checks if a leaf node is a BO in the BO metadata or not. If BO, adds an attribute isBO =
     * "true". For all its parents adds an attribute hasBO = "true" . This makes it easier to read
     * metadata from the cache.
     *
     * @param   compRootNode  The definition of the component.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void realignComponentMetadata(int compRootNode)
                                     throws SAPConnectorException
    {
        // Load BO Metadata. Every time component metadata is fetched, BO Metadata is overwritten.
        int boMetadataNode = getRequiredMetadataRoot(true, "BO Metadata", "", FILE_BO_METADATA,
                                                     m_loadingBOMetadata,
                                                     Node.getDocument(compRootNode), null);

        try
        {
            int borTreeNode = XPathHelper.selectSingleNode(compRootNode, "BOR_TREE", m_xmi);
            // Node with no parent
            int headItem = XPathHelper.selectSingleNode(borTreeNode, "item[PARENT[.=\"000000\"]]",
                                                        m_xmi);
            getChildren(headItem, borTreeNode, boMetadataNode);
        }
        finally
        {
            boMetadataNode = BACUtil.deleteNode(boMetadataNode);
        }
    }

    /**
     * This method realigns the metadata of BOs and IDOCs into a tree structrue, thus making it
     * easier to read. The array of attributes should contain the attributes of the parent in the
     * tree. The first element of the array should always be the attribute distinguishing the
     * parent.
     *
     * @param  rootNode           DOCUMENTME
     * @param  attributesForHead  DOCUMENTME
     * @param  commonXPath        DOCUMENTME
     */
    protected void realignMetadataIntoTree(int rootNode, String[] attributesForHead,
                                           String commonXPath)
    {
    	if (LOG.isDebugEnabled())
		{
			LOG.debug("Executing XPath " + commonXPath + "/item");
		}
    	
        int eachItem = XPathHelper.selectSingleNode(rootNode, commonXPath + "/item", m_xmi);

        int noOfAttributes = attributesForHead.length;
        String firstAttribute = attributesForHead[0];

        while (eachItem != 0)
        {
            int nextItem = Node.getNextSibling(eachItem);
            int firstAttrForHeadNode = XPathHelper.selectSingleNode(eachItem, firstAttribute);

            String firstAttrForHeadValue = Node.getData(firstAttrForHeadNode);
            int sameItemInTree = XPathHelper.selectSingleNode(rootNode,
                                                              commonXPath + "/item[@" +
                                                              firstAttribute + "=\"" +
                                                              firstAttrForHeadValue + "\"]");

            if (sameItemInTree == 0)
            {
                Node.setAttribute(eachItem, firstAttribute, firstAttrForHeadValue);

                for (int i = 1; i < noOfAttributes; i++)
                {
                    Node.setAttribute(eachItem, attributesForHead[i],
                                      XPathHelper.getStringValue(eachItem, attributesForHead[i]));
                }

                int childItemNode = getChildItemNode(eachItem, attributesForHead);
                Node.delete(Node.getFirstChild(eachItem), Node.getLastChild(eachItem));
                Node.appendToChildren(childItemNode, eachItem);
            }
            else
            {
                Node.appendToChildren(getChildItemNode(eachItem, attributesForHead),
                                      sameItemInTree);
                Node.delete(eachItem);
            }
            eachItem = nextItem;
        }
    }

    /**
     * This method returns all the BAPIs for a certain business object.
     *
     * @param   bapisNode       The parent node for attaching the information.
     * @param   businessObject  The name of the businessObject.
     * @param   overwrite       Whether or not to reload the information from SAP.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getBAPIMetadata(int bapisNode, String businessObject, boolean overwrite)
                     throws SAPConnectorException
    {
        // Get the actual metadata (either from SAP or from the file system.
        int cacheNode = getRequiredMetadataRoot(overwrite, "BO Metadata", "", FILE_BO_METADATA,
                                                m_loadingBOMetadata, Node.getDocument(bapisNode),
                                                null);

        try
        {
            // The BO tag is expected to contain OBJTYPE
            String xpath = "API_METHODS/item[@OBJTYPE=\"" + businessObject + "\"]";
            int boItem = XPathHelper.selectSingleNode(cacheNode, xpath, m_xmi);

            if ((boItem == 0) && LOG.isDebugEnabled())
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Could not find the definition for businessObject " + businessObject +
                              " in the cache.");
                }
            }

            Node.duplicateAndAppendToChildren(Node.getFirstChild(boItem), Node.getLastChild(boItem),
                                              bapisNode);
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return true;
    }

    /**
     * DOCUMENTME.
     *
     * @param   BOsNode    DOCUMENTME
     * @param   fromBO     DOCUMENTME
     * @param   toBO       DOCUMENTME
     * @param   overwrite  DOCUMENTME
     *
     * @return  Always true
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getBOMetadata(int BOsNode, String fromBO, String toBO, boolean overwrite)
                   throws SAPConnectorException
    {
        int cacheNode = getRequiredMetadataRoot(overwrite, "BO Metadata", "", FILE_BO_METADATA,
                                                m_loadingBOMetadata, Node.getDocument(BOsNode),
                                                null);

        try
        {
            // If the filter is based on object name
            int[] itemNodes = XPathHelper.selectNodes(cacheNode, "API_METHODS/item", m_xmi);
            int noOfItemNodes = itemNodes.length;

            for (int i = 0; i < noOfItemNodes; i++)
            {
                String objName = Node.getAttribute(itemNodes[i], "OBJECTNAME");

                // Comparison is case sensitive , as names in SAP include some special characters
                // also.
                int isToValueCrossed = objName.compareToIgnoreCase(toBO);

                if ((objName.compareToIgnoreCase(fromBO) >= 0) && (isToValueCrossed <= 0))
                {
                    int duplicateItem = Node.duplicateElement(itemNodes[i]);
                    Node.appendToChildren(duplicateItem, BOsNode);
                }
            }
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return true;
    }

    /**
     * DOCUMENTME.
     *
     * @param   componentsNode  DOCUMENTME
     * @param   parent          DOCUMENTME
     * @param   level           DOCUMENTME
     * @param   overwrite       DOCUMENTME
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getComponentMetadata(int componentsNode, String parent, int level, boolean overwrite)
                          throws SAPConnectorException
    {
        int cacheNode = getRequiredMetadataRoot(overwrite, "Component Metadata", "",
                                                FILE_COMPONENT_METADATA, loadingComponentMetadata,
                                                Node.getDocument(componentsNode), null);

        try
        {
            String itemXpath = new String("BOR_TREE");

            for (int i = 1; i < level; i++)
            {
                itemXpath = itemXpath + "/item";
            }

            if (parent.equals("000000")) // For the root component with no parent
            {
                int parentNode = XPathHelper.selectSingleNode(cacheNode,
                                                              itemXpath + "/item[@PARENT=\"" +
                                                              parent + "\"]", m_xmi);
                int duplicateItem = Node.duplicateElement(parentNode);
                Node.appendToChildren(duplicateItem, componentsNode);
            }
            else
            {
                int parentNode = XPathHelper.selectSingleNode(cacheNode,
                                                              itemXpath + "/item[@ID=\"" + parent +
                                                              "\"]", m_xmi);
                int aChild = Node.getFirstChild(parentNode);

                while (aChild != 0)
                {
                    if ((Node.getAttribute(aChild, "hasBO") != null) ||
                            (Node.getAttribute(aChild, "isBO") != null))
                    {
                        int duplicateItem = Node.duplicateElement(aChild);
                        Node.appendToChildren(duplicateItem, componentsNode);
                    }
                    aChild = Node.getNextSibling(aChild);
                }
            }
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }
        return true;
    }

    /**
     * This method gets all the message types for the IDOC.
     *
     * @param   messageTypeNode  The parent XML node.
     * @param   fromMsgType      The from message type.
     * @param   toMsgType        The to message type.
     * @param   overwrite        Whether or not to read the data from the cache.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getIDOCMessageTypes(int messageTypeNode, String fromMsgType, String toMsgType,
                                boolean overwrite)
                         throws SAPConnectorException
    {
        // Get the meta data.
        int cacheNode = getRequiredMetadataRoot(overwrite, "IDOC Metadata", "", FILE_IDOC_METADATA,
                                                m_loadingIDOCMetadata,
                                                Node.getDocument(messageTypeNode), null);

        try
        {
            int[] msgNodes = XPathHelper.selectNodes(cacheNode, "PT_MESSAGES/item", m_xmi);
            int noOfMsgNodes = msgNodes.length;

            for (int i = 0; i < noOfMsgNodes; i++)
            {
                String mesType = Node.getAttribute(msgNodes[i], "MESTYP");
                // Comparison is case sensitive , as names in SAP include some special characters
                // also.
                int isToValueCrossed = mesType.compareTo(toMsgType);

                if (isToValueCrossed > 0)
                {
                    break;
                }

                if ((mesType.compareTo(fromMsgType) >= 0) && (mesType.compareTo(toMsgType) <= 0))
                {
                    Node.duplicateAndAppendToChildren(msgNodes[i], msgNodes[i], messageTypeNode);
                }
            }
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return true;
    }

    /**
     * This method returns the IDOCs types.
     *
     * @param   idocsNode  The parent response node.
     * @param   mesType    The message type.
     * @param   overwrite  Whether or not to read the data directly from SAP.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getIDOCTypesForMessageType(int idocsNode, String mesType, boolean overwrite)
                                throws SAPConnectorException
    {
        //
        int cacheNode = getRequiredMetadataRoot(overwrite, "IDOC Metadata", "", FILE_IDOC_METADATA,
                                                m_loadingIDOCMetadata, Node.getDocument(idocsNode),
                                                null);

        try
        {
            int mesTypeNode = XPathHelper.selectSingleNode(cacheNode,
                                                           "PT_MESSAGES/item[@MESTYP=\"" + mesType +
                                                           "\"]", m_xmi);

            Node.duplicateAndAppendToChildren(Node.getFirstChild(mesTypeNode),
                                              Node.getLastChild(mesTypeNode), idocsNode);
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return true;
    }

    /**
     * This method returns the metadata for a list of RFCs.
     *
     * @param   rfcsNode   The response XML.
     * @param   fromRFC    The from name for the RFC.
     * @param   toRFC      The to name for the RFC.
     * @param   overwrite  Whether or not to read the data directly from SAP.
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean getRFCMetadata(int rfcsNode, String fromRFC, String toRFC, boolean overwrite)
                    throws SAPConnectorException
    {
        int cacheNode = getRequiredMetadataRoot(overwrite, "RFC Metadata", "", FILE_RFC_METADATA,
                                                loadingRFCMetadata, Node.getDocument(rfcsNode),
                                                null);

        try
        {
            int[] rfcNodes = XPathHelper.selectNodes(cacheNode, "FUNCTIONS/item/FUNCNAME", m_xmi);
            int noOfRFCNodes = rfcNodes.length;

            for (int i = 0; i < noOfRFCNodes; i++)
            {
                String rfcName = Node.getData(rfcNodes[i]);
                // Comparison is case sensitive , as names in SAP include some speical characters
                // also.
                int isToValueCrossed = rfcName.compareTo(toRFC);

                if (isToValueCrossed > 0)
                {
                    break;
                }

                if ((rfcName.compareTo(fromRFC) >= 0) && (isToValueCrossed <= 0))
                {
                    int itemNode = Node.getParent(rfcNodes[i]);
                    Node.duplicateAndAppendToChildren(itemNode, itemNode, rfcsNode);
                }
            }
        }
        finally
        {
            cacheNode = BACUtil.deleteNode(cacheNode);
        }

        return true;
    }

    /**
     * This method checks if the cache directory exists and the required metadata xml file is in the
     * cache directory.
     *
     * @param   fileName  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private boolean checkIfCacheExists(String fileName)
                                throws SAPConnectorException
    {
        File cacheDirectory = new File(m_cacheDir);

        if (!cacheDirectory.isDirectory())
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_CACHE_FOLDER_NOT_FOUND,
                                            m_cacheDir);
        }

        File metadataFile = new File(cacheDirectory, fileName);
        return metadataFile.isFile();
    }

    /**
     * This method creates the tuple/old/tagName structure.
     *
     * @param   parentNode     The parent node.
     * @param   objectTagName  The name of the object tag.
     *
     * @return  The creates object tag.
     */
    private int createTupleStructure(int parentNode, String objectTagName)
    {
        int returnValue = 0;

        int tuple = Node.createElementWithParentNS("tuple", null, parentNode);
        int old = Node.createElementWithParentNS("old", null, tuple);
        returnValue = Node.createElementWithParentNS(objectTagName, null, old);

        return returnValue;
    }

    /**
     * For each parent item in the realigned metadata tree, this method creates the children with
     * attributes not in the given array of attributes.
     *
     * @param   mesTypeNode        DOCUMENTME
     * @param   attributesForHead  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private int getChildItemNode(int mesTypeNode, String[] attributesForHead)
    {
        List<String> attributeList = Arrays.asList(attributesForHead); // convert the array into a
                                                                       // List to make search easier
        Document doc = Node.getDocument(mesTypeNode);
        int childItem = doc.createElement("item");
        int aChild = Node.getFirstChild(mesTypeNode);

        while (aChild != 0)
        {
            String childName = Node.getLocalName(aChild);

            if (!attributeList.contains(childName))
            {
                Node.setAttribute(childItem, childName, Node.getData(aChild));
            }
            aChild = Node.getNextSibling(aChild);
        }
        return childItem;
    }

    /**
     * To get the children of a given node, and children of each child recursively.
     *
     * @param  parentItem      DOCUMENTME
     * @param  treeNode        DOCUMENTME
     * @param  boMetadataNode  DOCUMENTME
     */
    private void getChildren(int parentItem, int treeNode, int boMetadataNode)
    {
        // Make all children into attributes
        int aChild = Node.getFirstChild(parentItem);

        while (aChild != 0)
        {
            String childName = Node.getLocalName(aChild);
            Node.setAttribute(parentItem, childName, Node.getDataWithDefault(aChild, ""));

            int anotherChild = Node.getNextSibling(aChild);
            Node.delete(aChild);
            aChild = anotherChild;
        }

        String idOfChildItem = Node.getAttribute(parentItem, "CHILD");

        if (idOfChildItem != null)
        {
            if (idOfChildItem.equals("000000"))
            {
                // Add isBO, hasBO attributes
                String itemName = Node.getAttribute(parentItem, "NAME");
                int itemInBOMetadata = XPathHelper.selectSingleNode(boMetadataNode,
                                                                    "API_METHODS/item[@OBJTYPE=\"" +
                                                                    itemName + "\"]", m_xmi);

                if (itemInBOMetadata != 0)
                {
                    Node.setAttribute(parentItem, "isBO", "X");

                    int parentNode = Node.getParent(parentItem);

                    while (Node.getLocalName(parentNode).equals("item"))
                    {
                        if (Node.getAttribute(parentNode, "hasBO") == null)
                        {
                            Node.setAttribute(parentNode, "hasBO", "X");
                        }
                        parentNode = Node.getParent(parentNode);
                    }
                }
            }
            else
            {
                int firstChild = XPathHelper.selectSingleNode(treeNode,
                                                              "item[ID[.=\"" + idOfChildItem +
                                                              "\"]]", m_xmi);
                if ((firstChild == 0 || parentItem == 0) && LOG.isDebugEnabled())
				{
					LOG.debug("Child: " + firstChild + ", parent: " + parentItem + ". XPath " + "item[ID[.=\"" + idOfChildItem +
                            "\"]] on XML:\n" + Node.writeToString(treeNode, false));
				}
                
            	Node.appendToChildren(firstChild, parentItem);
                getChildren(firstChild, treeNode, boMetadataNode);
                getNextSiblings(firstChild, treeNode, boMetadataNode);
            }
        }
    }

    /**
     * To get the next siblings of each child. For each next child, its next siblings are found
     * recursively
     *
     * @param  aChild          DOCUMENTME
     * @param  treeNode        DOCUMENTME
     * @param  boMetadataNode  DOCUMENTME
     */
    private void getNextSiblings(int aChild, int treeNode, int boMetadataNode)
    {
        String idOfNextItem = Node.getAttribute(aChild, "NEXT");

        if ((idOfNextItem != null) && !idOfNextItem.equals("000000"))
        {
            int nextSibling = XPathHelper.selectSingleNode(treeNode,
                                                           "item[ID[.=\"" + idOfNextItem + "\"]]",
                                                           m_xmi);
            Node.appendToChildren(nextSibling, Node.getParent(aChild));
            getChildren(nextSibling, treeNode, boMetadataNode);
            getNextSiblings(nextSibling, treeNode, boMetadataNode);
        }
    }

    /**
     * This method returns the root node of the required metadata. If overwrite is true, it fetches
     * the metadata afresh from SAP. Else If found in the cache directory, it loads the metadata
     * from the cache directory. else it fetches the metadata from SAP and stores it in the cache
     * directory. While fectching metadata from SAP, it will wait if another thread is fetching the
     * same metadata from SAP server and then checks the cache directory. parameter extension
     * contains the cimType of an IDOC. In other case, it is passed as null.
     *
     * @param   overwrite        DOCUMENTME
     * @param   metadataType     DOCUMENTME
     * @param   itemName         DOCUMENTME
     * @param   cacheFileName    DOCUMENTME
     * @param   loadingMetadata  DOCUMENTME
     * @param   doc              DOCUMENTME
     * @param   extension        DOCUMENTME
     *
     * @return  Always true.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private int getRequiredMetadataRoot(boolean overwrite, String metadataType, String itemName,
                                        String cacheFileName, boolean loadingMetadata, Document doc,
                                        String extension)
                                 throws SAPConnectorException
    {
        int cacheNode = 0;

        if (overwrite)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Overwrite is true.");
            }

            cacheNode = loadMetadata(metadataType, itemName, doc, extension);
        }

        if (!checkIfCacheExists(cacheFileName))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(metadataType + " not found in the cache directory.");

                if (loadingMetadata)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Will wait as another thread is fetching " + metadataType +
                                  " from SAP server.");
                    }
                }
            }

            // Waits till metadata is loaded
            while (loadingMetadata)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException ie)
                {
                    throw new SAPConnectorException(ie,
                                                    SAPConnectorExceptionMessages.ERROR_WAITING_FOR_OTHER_THREAD_TO_LOAD_METADATA);
                }
                loadingMetadata = refreshLoadingMetadataFlag(metadataType);
            }

            if (!checkIfCacheExists(cacheFileName))
            {
                cacheNode = loadMetadata(metadataType, itemName, doc, extension);
            }
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Loading " + metadataType + " from cache directory.");
            }

            try
            {
                cacheNode = doc.load(m_cacheDir + "\\" + cacheFileName);
            }
            catch (XMLException xe)
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_LOADING_METADATA_CACHE,
                                                metadataType, m_cacheDir);
            }
        }
        return cacheNode;
    }

    /**
     * This method calls the appropriate loadMetadata method based on the metadataType string
     * passed. parameter extension contains the cimType of an IDOC. In other case, it is passed as
     * null.
     *
     * @param   metadataType  DOCUMENTME
     * @param   itemName      DOCUMENTME
     * @param   doc           DOCUMENTME
     * @param   extension     DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private int loadMetadata(String metadataType, String itemName, Document doc,
                             String extension)
                      throws SAPConnectorException
    {
        if (metadataType.equalsIgnoreCase("Component Metadata"))
        {
            return loadComponentMetadata(doc);
        }
        else if (metadataType.equalsIgnoreCase("BO Metadata"))
        {
            return loadBOMetadata(doc);
        }
        else if (metadataType.equalsIgnoreCase("RFC Metadata"))
        {
            return loadRFCMetadata(doc);
        }
        else if (metadataType.equalsIgnoreCase("IDOC Metadata"))
        {
            return loadIDOCMetadata(doc);
        }
        else if (metadataType.equalsIgnoreCase("RFC Interface"))
        {
            return loadRFCInterface(itemName, doc);
        }
        else // if(metadataType.equalsIgnoreCase("IDOC Interface"))
        {
            return loadIDOCInterface(itemName, extension, doc);
        }
    }

    /**
     * This method refreshes the loadingMetadata flag. Returns the current value of the flag based
     * on the meatadta type being loaded.
     *
     * @param   metadataType  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private boolean refreshLoadingMetadataFlag(String metadataType)
    {
        if (metadataType.equalsIgnoreCase("Component Metadata"))
        {
            return loadingComponentMetadata;
        }
        else if (metadataType.equalsIgnoreCase("BO Metadata"))
        {
            return m_loadingBOMetadata;
        }
        else if (metadataType.equalsIgnoreCase("RFC Metadata"))
        {
            return loadingRFCMetadata;
        }
        else if (metadataType.equalsIgnoreCase("IDOC Metadata"))
        {
            return m_loadingIDOCMetadata;
        }
        else if (metadataType.equalsIgnoreCase("RFC Interface"))
        {
            return loadingRFCInterface;
        }
        else // if(metadataType.equalsIgnoreCase("IDOC Interface"))
        {
            return loadingIDOCInterface;
        }
    }
}
