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
 package com.eibus.applicationconnector.sap.idoc;

import com.eibus.applicationconnector.sap.Messages;
import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.request.OLEDBRequestSender;
import com.eibus.applicationconnector.sap.util.BACUtil;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

/**
 * This class acts as a listener to the incoming IDOC requests from SAP.
 *
 * @author  ygopal
 */

public class SAPIDocServer extends JCoIDoc.Server
    implements JCO.ServerExceptionListener, JCO.ServerErrorListener
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPIDocServer.class);
    /**
     * Default parameter name assumed.
     */
    //private static String targetMethod_parameterName = "parameter";
    private static String targetMethod_parameterName = "IDOC";
    /**
     * DOCUMENTME.
     */
    private Document doc;
    /**
     * Holds the configuration of the connector.
     */
    private ISAPConfiguration m_config;
    /**
     * DOCUMENTME.
     */
    private TargetMappingFinder mappingFinder;
    /**
     * DOCUMENTME.
     */
    private OLEDBRequestSender oleDBRequestSender;
    /**
     * DOCUMENTME.
     */
    private int[] params_updateMethod;
    /**
     * This string stores the current tid.
     */
    private volatile String tid;

    /**
     * Constructor.
     *
     * @param   gwhost          DOCUMENTME
     * @param   gwserv          DOCUMENTME
     * @param   progid          DOCUMENTME
     * @param   jcoRepository   DOCUMENTME
     * @param   idocRepository  DOCUMENTME
     * @param   configuration   m_config DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public SAPIDocServer(String gwhost, String gwserv, String progid, IRepository jcoRepository,
                         IDoc.Repository idocRepository, ISAPConfiguration configuration)
                  throws SAPConnectorException
    {
        super(gwhost, gwserv, progid, jcoRepository, idocRepository);
        this.setProperty("jco.server.unicode", "1") ;
        m_config = configuration;
        mappingFinder = m_config.getTargetMappingFinder();
        doc = new Document();
        oleDBRequestSender = new OLEDBRequestSender(SAPConnectorConstants.IDOC_TABLE_NAME,
                                                    m_config.getOrganization());
        JCO.addServerErrorListener(this);
        JCO.addServerExceptionListener(this);
    }

    /**
     * Interface method of JCO.ServerErrorListener to be notified about server errors.
     *
     * @param  server  DOCUMENTME
     * @param  er      DOCUMENTME
     */
    public void serverErrorOccurred(JCO.Server server, java.lang.Error er)
    {
        LOG.error(er, Messages.ERR_IDOC_SERVER, er.getMessage());
    }

    /**
     * Interface method of JCO.ServerExceptionListener to be notified about server exceptions.
     *
     * @param  server  DOCUMENTME
     * @param  ex      DOCUMENTME
     */
    public void serverExceptionOccurred(JCO.Server server, java.lang.Exception ex)
    {
        LOG.error(ex, Messages.ERR_IDOC_SERVER, ex.getMessage());
    }

    /**
     * Called when the SOAP processor is started. The IDOC server is started.
     */
    @Override public void start()
    {
        super.start();
    }

    /*
     *  Called when the SOAP processor is stopped. The IDOC server is stopped.
     */
    /**
     * @see  com.sap.mw.jco.JCO$Server#stop()
     */
    @Override public void stop()
    {
        super.stop();
    }

    /**
     * This method processes incoming IDocs.
     *
     * @param   idocList  DOCUMENTME
     *
     * @throws  Exception  DOCUMENTME
     */
    @Override protected synchronized void handleRequest(IDoc.DocumentList idocList)
                                                 throws Exception
    {
        int number_idocs = idocList.getNumDocuments();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Number of IDOCs in the list are " + number_idocs);
        }
        params_updateMethod = new int[number_idocs];

        // Application specific IDoc processing goes here
        IDoc.Document idoc;

        for (int i = 0; i < number_idocs; i++)
        {
            idoc = idocList.get(i);
            processIDOC(idoc, i);
        }
    }

    // Application specific TID checking goes here
    /**
     * @see  com.sap.mw.jco.JCO$Server#onCheckTID(java.lang.String)
     */
    @Override protected boolean onCheckTID(String tid)
    {
        this.tid = tid;

        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(" In onCheckTID method. Tid is " + tid);
            }
            return checkTIDInDataBase(tid);
        }
        catch (SAPConnectorException sf)
        {
            LOG.error(sf, Messages.ERR_CHECKING_TID, sf.getLocalizedMessage());
        }

        return false;
    }

    // Application specific TID committing goes here
    /**
     * @see  com.sap.mw.jco.JCO$Server#onCommit(java.lang.String)
     */
    @Override protected void onCommit(String tid)
    {
        // Store the idoc(s) in the database.
        try
        {
            oleDBRequestSender.sendOleDBUpdateRequest(params_updateMethod, doc);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC(s) stored in the database.");
            }
        }
        catch (SAPConnectorException sf)
        {
            LOG.error(sf, Messages.ERR_CHECKING_TID, sf.getLocalizedMessage());

            throw new IllegalStateException(sf.getLocalizedMessage(), sf);
        }
    }

    // Application specific TID confirmation goes here
    /**
     * @see  com.sap.mw.jco.JCO$Server#onConfirmTID(java.lang.String)
     */
    @Override protected void onConfirmTID(String tid)
    {
        // Clean up the XML Nodes created.
        if (params_updateMethod != null)
        {
            int number_params = params_updateMethod.length;

            for (int i = 0; i < number_params; i++)
            {
                BACUtil.deleteNode(params_updateMethod[i]);
            }
        }
        // System.out.println("After Confirming " + doc.getNumUsedNodes(true));
    }

    // Application specific TID rolling back goes here
    /**
     * @see  com.sap.mw.jco.JCO$Server#onRollback(java.lang.String)
     */
    @Override protected void onRollback(String tid)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("in rollback tid" + tid);
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param  parentSegment          DOCUMENTME
     * @param  parentSegmentMetadata  DOCUMENTME
     */
    private static void addChildSegments(IDoc.Segment parentSegment,
                                         IDoc.SegmentMetaData parentSegmentMetadata)
    {
        int noOfChildSegments = parentSegmentMetadata.getNumChildren();

        // For each fhild segment
        for (int i = 0; i < noOfChildSegments; i++)
        {
            IDoc.SegmentMetaData childSegmentMetadata = parentSegmentMetadata.getChild(i);
            String childSegmentType = childSegmentMetadata.getType();
            IDoc.Segment childSegment = parentSegment.addChild(childSegmentType);

            // parentSegment.addChild(childSegmentType);
            addChildSegments(childSegment, childSegmentMetadata);
            // System.out.println(childSegmentMetadata.getName()); addChildSegments(childSegment,
            // childSegmentMetadata);
        }
        // IDoc.Segment siblingSegment = parentSegment.addSibling(true);
        // IDoc.SegmentMetaData siblingMetadata = siblingSegment.getSegmentMetaData();
        // addChildSegments(siblingSegment, siblingMetadata);
    }

    /**
     * This method checks for tid in the database. Returns true if the tid is not in the database
     *
     * @param   tid  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private boolean checkTIDInDataBase(String tid)
                                throws SAPConnectorException
    {
        int responseNode = oleDBRequestSender.getTid(tid, doc);
        int tupleNode = Find.firstMatch(responseNode, "<GetTidResponse><tuple>");

        // tid not there in the database
        if (tupleNode == 0)
        {
            BACUtil.deleteNode(responseNode);
            return true;
        }
        // tid is there in the database
        else
        {
            BACUtil.deleteNode(responseNode);
            return false;
        }
    }

    /**
     * This method processes each IDOC. i.e. it does the following. 1. Tries to find the target
     * system and send the request to it. 2. Obtain the local status and store the IDOC in the
     * database.
     *
     * @param   idoc         DOCUMENTME
     * @param   indexInList  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void processIDOC(IDoc.Document idoc, int indexInList)
                      throws SAPConnectorException
    {
        String idocNum = idoc.getIDocNumber();
        String mesType = idoc.getMessageType();
        String idocType = idoc.getIDocType();
        String cimType = idoc.getIDocTypeExtension();
        String receiverLS = idoc.getRecipientPartnerNumber();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Received IDOC information: IDOCNumber = " + idocNum + ", MessageType = " +
                      mesType + ", IDOCType =" + idocType + ", CIMType = " + cimType +
                      ", Receiver LS = " + receiverLS);
        }

        String idocXMLString = idoc.toXML();
        int idocXMLNode;
        String localStatus;
        String errorText;
        String targetSystem;
        targetSystem = "";

        try
        {
            idocXMLNode = doc.parseString(idocXMLString);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe, SAPConnectorExceptionMessages.ERROR_PARSING_IDOC,
                                            idocXMLString);
        }

        StringBuffer targetSOAPNode = new StringBuffer();
        StringBuffer targetMethod = new StringBuffer();
        StringBuffer targetNameSpace = new StringBuffer();
        int parameterNode = 0;

        try
        {
            mappingFinder.getTargetMapping(mesType, idocType, receiverLS, targetSOAPNode,
                                           targetMethod, targetNameSpace);

            String str_targetSOAPNode = targetSOAPNode.substring(0).trim();
            String str_targetMethod = targetMethod.substring(0).trim();
            String str_targetNameSpace = targetNameSpace.substring(0).trim();

            /*String str_targetSOAPNode = "cn=Java Call Service,cn=soap
             * nodes,o=system,cn=cordys,o=vanenburg.com".trim(); String str_targetMethod =
             * "ReceiveIDOC".trim();String str_targetNameSpace =
             * "http://schemas.cordys.com/IDOCReceiver".trim();*/
            if (LOG.isDebugEnabled())
            {
                LOG.debug(" Target Mapping found. TargetSOAPNode: " + str_targetSOAPNode +
                          " TargetMethod: " + str_targetMethod + " TargetNamespace: " +
                          str_targetNameSpace);
            }
            // System.out.println(" Target Mapping found. TargetSOAPNode: "+ str_targetSOAPNode + "
            // TargetMethod: "+ str_targetMethod + " TargetNamespace: " + str_targetNameSpace);
            // Target system is the target SOAPNode name extracted from targetSOAPNode DN.
            targetSystem = str_targetSOAPNode.substring(3, str_targetSOAPNode.indexOf(","));
            // parameter to be used in the request to the target SOAP Node
            parameterNode = doc.createElement(targetMethod_parameterName);
            Node.appendToChildren(idocXMLNode, parameterNode);
            oleDBRequestSender.sendRequestToSOAPNode(str_targetSOAPNode, str_targetMethod,
                                                     str_targetNameSpace, parameterNode, doc);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("IDOC sent to the target system.");
            }
            // System.out.println("IDOC sent to the target system.");
            localStatus = "Dispatched";
            errorText = "";
        }
        catch (Exception e)
        {
            localStatus = "Error";
            errorText = "Exception occured while dispatching the IDOC: " + e.toString();

            LOG.error(e, Messages.ERROR_DISPATCHING_IDOC);
        }

        params_updateMethod[indexInList] = oleDBRequestSender.frameIDOCInsertTuple(idoc,
                                                                                   idocXMLNode, tid,
                                                                                   localStatus,
                                                                                   errorText,
                                                                                   targetSystem,
                                                                                   m_config
                                                                                   .getServiceGroup(),
                                                                                   doc);
        BACUtil.deleteNode(parameterNode);
    }
}
