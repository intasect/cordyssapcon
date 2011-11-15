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

import com.cordys.coe.util.xml.NamespaceDefinitions;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.applicationconnector.sap.util.LDAPInterface;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class is used to publish method sets and methods in the LDAP.
 */
public class WSDLPublisher
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(WSDLPublisher.class);
    /**
     * DOCUMENTME.
     */
    private static final String ldapNameSpace = "http://schemas.cordys.com/1.0/ldap";
    /**
     * DOCUMENTME.
     */
    private static final String sapImplementationClass = "com.eibus.applicationconnector.sap.SAPMapper";
    /**
     * DOCUMENTME.
     */
    private Document doc;
    /**
     * DOCUMENTME.
     */
    private LDAPInterface ldapInterface;
    /**
     * DOCUMENTME.
     */
    private XPathMetaInfo m_xmi;
    /**
     * DOCUMENTME.
     */
    private String organization;

    /**
     * Creates a new WSDLPublisher object.
     *
     * @param   org  user: user name
     * @param   doc  : Document object to be used
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public WSDLPublisher(String org, Document doc)
                  throws SAPConnectorException
    {
        this.organization = org;
        ldapInterface = new LDAPInterface(org);
        this.doc = doc;

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("SOAP", NamespaceDefinitions.XMLNS_SOAP_1_1);
        m_xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_LDAP);
    }

    /**
     * This method attaches the given method set to the given SOAPNode if it is not already
     * attached.
     *
     * @param   soapNodeDN  DN of the SOAP node to which the method set needs to be attached
     * @param   methodSet   Method set name
     * @param   namespace   Method set namespace
     *
     * @throws  SAPConnectorException
     */
    public void attachToSOAPNode(String soapNodeDN, String methodSet, String namespace)
                          throws SAPConnectorException
    {
        boolean isMethodSetAlreadyAttached = false;
        // boolean isNamespaceAlreadyAttached = false;
        String methodSetDN = "cn=" + methodSet + ",cn=method sets," + organization;
        int soapNodeEntry = 0;

        try
        {
        	//Get the LDAP details for the current SOAP Node
            soapNodeEntry = ldapInterface.getEntry(soapNodeDN, doc);

            int busMethodSetsNode = Find.firstMatch(soapNodeEntry, "<entry><busmethodsets>");

            if (busMethodSetsNode == 0)
            {
            }
            else
            {
                int[] methodSetNameNodes = Find.match(busMethodSetsNode, "<busmethodsets><string>");
                int noOfMethodSetsAttached = methodSetNameNodes.length;

                for (int i = 0; i < noOfMethodSetsAttached; i++)
                {
                    String attachedMethodSetName = Node.getDataWithDefault(methodSetNameNodes[i],
                                                                           "");

                    if (attachedMethodSetName.equals(methodSetDN))
                    {
                        isMethodSetAlreadyAttached = true;
                        break;
                    }
                }
            }

            if (!isMethodSetAlreadyAttached)
            {
                int tupleNode = doc.createElement("tuple");
                int oldNode = doc.createElement("old", tupleNode);
                // Duplicate it before appending to oldNode. As the reponse from LDAP(WCP) is coming
                // with a different document object as root, soapNodeEntry node is becoming 0 after
                // appending to oldNode.
                int duplicateSoapNodeEntry = Node.duplicate(soapNodeEntry);
                Node.appendToChildren(soapNodeEntry, oldNode);

                int newNode = doc.createElement("new", tupleNode);
                Node.appendToChildren(duplicateSoapNodeEntry, newNode);

                // int duplicateBusMethodSetsNode = Find.firstMatch(duplicateSoapNodeEntry,
                // "<entry><busmethodsets>");
                int duplicateBusMethodSetsNode = Find.firstMatch(tupleNode,
                                                                 "<tuple><new><entry><busmethodsets>");

                if (duplicateBusMethodSetsNode == 0)
                {
                    duplicateBusMethodSetsNode = doc.createElement("busmethodsets",
                                                                   duplicateSoapNodeEntry);
                }
                doc.createTextElement("string", methodSetDN, duplicateBusMethodSetsNode);

                // int duplicateLabeledUriNode = Find.firstMatch(duplicateSoapNodeEntry,
                // "<entry><labeleduri>");
                int duplicateLabeledUriNode = Find.firstMatch(tupleNode,
                                                              "<tuple><new><entry><labeleduri>");

                if (duplicateLabeledUriNode == 0)
                {
                    duplicateLabeledUriNode = doc.createElement("labeleduri",
                                                                duplicateSoapNodeEntry);
                }
                doc.createTextElement("string", namespace, duplicateLabeledUriNode);

                int[] parameters = new int[1];
                parameters[0] = tupleNode;

                int responseNode = ldapInterface.executeMethod("Update", ldapNameSpace, parameters,
                                                               doc, m_xmi);
                BACUtil.deleteNode(tupleNode);
                BACUtil.deleteNode(responseNode);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Method Set successfully attached to the SOAPNode.");
                }
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Method Set was already attached to the SOAPNode.");
                }
            }
        }
        finally
        {
            BACUtil.deleteNode(soapNodeEntry);
        }
    }

    /**
     * This method is used to publish in the ldap the Busmethod.
     *
     * @param   busMethodSet          The method set under which the method is published
     * @param   methodName            Bus method name
     * @param   methodImplementation  The busimplementation property for the method
     * @param   methodInterface       The busInterface preperty for the method
     * @param   overwrite             overwrite the LDAP entry
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void publishBusMethod(String busMethodSet, String methodName,
                                 String methodImplementation, String methodInterface,
                                 boolean overwrite)
                          throws SAPConnectorException
    {
        String methodDN = "cn=" + methodName + ",cn=" + busMethodSet + ",cn=method sets," +
                          organization;
        int oldentry = ldapInterface.getEntry(methodDN, doc);

        if ((oldentry != 0) && (overwrite == false))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_METHOD_ALREADY_EXISTS);
        }

        int tupleNode = doc.createElement("tuple");

        if (oldentry != 0)
        {
            int oldNode = doc.createElement("old", tupleNode);
            Node.appendToChildren(oldentry, oldNode);
        }

        int newNode = doc.createElement("new", tupleNode);
        int entryNode = doc.createElement("entry", newNode);
        Node.setAttribute(entryNode, "dn", methodDN);

        int cnNode = doc.createElement("cn", entryNode);
        doc.createTextElement("string", methodName, cnNode);

        int objectclassNode = doc.createElement("objectclass", entryNode);
        doc.createTextElement("string", "top", objectclassNode);
        doc.createTextElement("string", "busmethod", objectclassNode);

        int impNode = doc.createElement("busmethodimplementation", entryNode);
        doc.createTextElement("string", methodImplementation, impNode);

        if (methodName.endsWith(".xsd"))
        {
            doc.createTextElement("string", "busmethodtype", objectclassNode);

            int xsdNode = doc.createElement("busmethodtypexsd", entryNode); // changed from wsdl to
                                                                            // XSD
            doc.createTextElement("string", methodInterface, xsdNode);
        }
        else
        {
            int wsdlNode = doc.createElement("busmethodwsdl", entryNode); // changed from XSD to
                                                                          // wsdl
            doc.createTextElement("string", methodInterface, wsdlNode);
        }

        int[] parameters = new int[1];
        parameters[0] = tupleNode;

        int responseNode = ldapInterface.executeMethod("Update", ldapNameSpace, parameters, doc,
                                                       m_xmi);
        BACUtil.deleteNode(responseNode);
        BACUtil.deleteNode(tupleNode);
    }

    /**
     * This method is used to publish the Bus method set.
     *
     * @param   busMethodSet       The method set under which the method is published
     * @param   methodSetNamepace  The namespace for the method set
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void publishBusMethodSet(String busMethodSet, String methodSetNamepace)
                             throws SAPConnectorException
    {
        String dn = "cn=" + busMethodSet + ",cn=method sets," + organization;
        int oldentry = ldapInterface.getEntry(dn, doc);

        if (oldentry != 0)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Method Set already exists.");
            }
            BACUtil.deleteNode(oldentry);
        }
        else
        {
            int tupleNode = doc.createElement("tuple");
            int newNode = doc.createElement("new", tupleNode);
            int entryNode = doc.createElement("entry", newNode);
            Node.setAttribute(entryNode, "dn", dn);

            int cnNode = doc.createElement("cn", entryNode);
            doc.createTextElement("string", busMethodSet, cnNode);

            int objectclassNode = doc.createElement("objectclass", entryNode);
            doc.createTextElement("string", "top", objectclassNode);
            doc.createTextElement("string", "busmethodset", objectclassNode);

            int labeleduriNode = doc.createElement("labeleduri", entryNode);
            doc.createTextElement("string", methodSetNamepace, labeleduriNode);

            int implementationClassNode = doc.createElement("implementationclass", entryNode);
            doc.createTextElement("string", sapImplementationClass, implementationClassNode);

            int[] parameters = new int[1];
            parameters[0] = tupleNode;

            int responseNode = ldapInterface.executeMethod("Update", ldapNameSpace, parameters, doc,
                                                           m_xmi);
            BACUtil.deleteNode(tupleNode);
            BACUtil.deleteNode(responseNode);
        }
    }
}
