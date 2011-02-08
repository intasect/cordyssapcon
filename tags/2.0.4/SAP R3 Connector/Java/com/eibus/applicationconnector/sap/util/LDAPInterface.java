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
 package com.eibus.applicationconnector.sap.util;

import com.cordys.coe.util.xml.NamespaceDefinitions;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessage;

import com.eibus.directory.soap.LDAPDirectory;

import com.eibus.transport.Middleware;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

/**
 * This class is used to for interacting with LDAP for -executing a method with given namespace and
 * parameters -updating the LDAP entries -Geting the old entry if any for given dn -Authenticate a
 * user if he/she is a BCP authenticated user -Find the organizational user for a given
 * authenticated user in the given organizational context.
 *
 * @author  ygopal
 */

public class LDAPInterface
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(LDAPInterface.class);
    /**
     * DOCUMENTME.
     */
    private Connector connector = null;
    /**
     * DOCUMENTME.
     */
    private LDAPDirectory directory;
    /**
     * DOCUMENTME.
     */
    private String organizationalUser;

    /**
     * Constructor with no parameters. In this case, current organization is considered. Sender of
     * requests is always the current user.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public LDAPInterface()
                  throws SAPConnectorException
    {
        initialize();
        organizationalUser = directory.getOrganizationalUser();

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" In LDAPInterface, Current organizational user DN is " + organizationalUser);
        }
    }

    /**
     * Constructor with organization as parameter. * The given organization is set as the current
     * organizational context before sending the request. Sender of requests is always the current
     * user within the given organzational context.
     *
     * @param   organization  : DN Of the organization to be used to send requests.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public LDAPInterface(String organization)
                  throws SAPConnectorException
    {
        initialize();
        directory.setOrganization(organization);
        organizationalUser = directory.getOrganizationalUser();

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" In LDAPInterface, Current organizational user DN is " + organizationalUser);
        }
    }

    /**
     * This method checks if the user with the given id is an authenticated user of BCP or not.If
     * YES, returns the CN of the user, else throws an Exception.
     *
     * @param   userid  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public LDAPEntry authenticateUser(String userid)
                               throws SAPConnectorException
    {
        LDAPEntry userEntry = null;
        LDAPSearchResults res;

        try
        {
            res = directory.getConnection().search(directory.getDirectorySearchRoot(),
                                                   LDAPConnection.SCOPE_SUB,
                                                   "&(objectclass=busauthenticationuser)(osidentity=" +
                                                   userid + ")", null, false);
        }
        catch (LDAPException le)
        {
            throw new SAPConnectorException(le,
                                            SAPConnectorExceptionMessages.UNABLE_TO_GET_LDAP_CONNECTION);
        }

        if (res.hasMore())
        {
            try
            {
                userEntry = res.next();
            }
            catch (LDAPException le)
            {
                throw new SAPConnectorException(le,
                                                SAPConnectorExceptionMessages.ERROR_GETTING_NEXT_ENTRY_FROM_LDAP);
            }
        }

        return userEntry;
    }

    /**
     * This method executes given method and returns the MethodResponse node in the response SOAP
     * Message.
     *
     * @param   methodName
     * @param   nameSpace   of the method
     * @param   parameters  XML nodes that are appended to the SOAP body as they are.
     * @param   doc         : Document object to be used. For update request, it expects 1 parameter
     *                      tuple,old and new For other methods all parameters can be sent as
     *                      separate nodes
     * @param   xmi         The namespace prefix mappings.
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int executeMethod(String methodName, String nameSpace, int[] parameters, Document doc,
                             XPathMetaInfo xmi)
                      throws SAPConnectorException
    {
        String receiver;

        try
        {
            receiver = directory.findSOAPNode(directory.getOrganization(), nameSpace, methodName);
            return executeMethod(methodName, nameSpace, receiver, parameters, doc, xmi);
        }
        catch (LDAPException le)
        {
            throw new SAPConnectorException(le,
                                            SAPConnectorExceptionMessages.CANNOT_FIND_SERVICE_GROUP_FOR_METHOD,
                                            methodName);
        }
    }

    /**
     * This method executes given method and returns the MethodResponse node in the response SOAP
     * Message.
     *
     * @param   methodName
     * @param   nameSpace   of the method
     * @param   receiver    DN of the SOAP Node where the request should be sent
     * @param   parameters  XML nodes that are appended to the SOAP body as they are.
     * @param   doc         : Document object to be used. For update request, it expects 1 parameter
     *                      tuple,old and new For other methods all parameters can be sent as
     *                      separate nodes
     * @param   xmi         The namespace prefix mappings.
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int executeMethod(String methodName, String nameSpace, String receiver, int[] parameters,
                             Document doc, XPathMetaInfo xmi)
                      throws SAPConnectorException
    {
        int request = 0;
        int response = 0;
        int methodNode;

        try
        {
            request = createSOAPMessage(doc, connector.getMiddleware(), receiver, null);
            methodNode = doc.createElement(methodName, request);
            Node.setAttribute(methodNode, "xmlns", nameSpace);

            if (parameters != null)
            {
                for (int i = 0; i < parameters.length; i++)
                {
                    Node.appendToChildren(Node.duplicate(parameters[i]), methodNode);
                }
            }

            // Send the request
            int mesEnve = Node.getRoot(request);
            response = connector.sendAndWait(mesEnve);

            // Check for SOAP faults.
            checkErrorInResponse(xmi, response);

            // Get the method response
            int oldNode = XPathHelper.selectSingleNode(response,
                                                       "SOAP:Body/ns:" + methodName + "Response",
                                                       xmi);

            int clone_oldNode = Node.duplicate(oldNode);

            return (clone_oldNode);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_EXECUTING_METHOD,
                                            methodName);
        }
        finally
        {
            deleteRequestResponseNodes(Node.getRoot(request), response);
        }
    }

    /**
     * This method finds the organizational user for the authentication user with the given CN,
     * under the organizational context with the given DN.
     *
     * @param   userCN                 DOCUMENTME
     * @param   organizationalContext  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public LDAPEntry findOrganizationalUser(String userCN, String organizationalContext)
                                     throws SAPConnectorException
    {
        LDAPEntry orgUserEntry = null;
        LDAPSearchResults res;

        try
        {
            res = directory.getConnection().search(organizationalContext, LDAPConnection.SCOPE_SUB,
                                                   "(authenticationuser=" + userCN + ")", null,
                                                   false);
        }
        catch (LDAPException le)
        {
            throw new SAPConnectorException(le,
                                            SAPConnectorExceptionMessages.COULD_NOT_FIND_AUTHENTICATED_USER,
                                            userCN);
        }

        if (res.hasMore())
        {
            try
            {
                orgUserEntry = res.next();
            }
            catch (LDAPException le)
            {
                throw new SAPConnectorException(le,
                                                SAPConnectorExceptionMessages.ERROR_GETTING_NEXT_ENTRY_FROM_LDAP);
            }
        }
        return orgUserEntry;
    }

    /**
     * Finds and returns the LDAP object with the given distinguished name.
     *
     * @param   dn   distinguished name of the LDAP entry
     * @param   doc  Doucment object to be used
     *
     * @return  The LDAP details of the entry.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public int getEntry(String dn, Document doc)
                 throws SAPConnectorException
    {
        int returnValue = 0;

        int response = 0;
        int dnParameter = 0;

        try
        {
        	//Create the parameter request
            dnParameter = doc.createTextElement("dn", dn);

            XPathMetaInfo xmi = new XPathMetaInfo();
            xmi.addNamespaceBinding("SOAP", NamespaceDefinitions.XMLNS_SOAP_1_1);
            xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_LDAP);

            response = executeMethod("GetLDAPObject", SAPConnectorConstants.NS_LDAP,
                                     new int[] { dnParameter }, doc, xmi);

            int oldNode = XPathHelper.selectSingleNode(response,
                                                       "//ns:GetLDAPObjectResponse/ns:tuple/ns:old",
                                                       xmi);

            int entryNode = Node.getFirstChild(oldNode);
            returnValue = Node.unlink(entryNode);
        }
        finally
        {
            BACUtil.deleteNode(response);
            BACUtil.deleteNode(dnParameter);
        }
        return (returnValue);
    }

    /**
     * This method deletes the response nodes if there are any errors.
     *
     * @param  request   DOCUMENTME
     * @param  response  DOCUMENTME
     */
    private static void deleteRequestResponseNodes(int request, int response)
    {
        BACUtil.deleteNode(request);
        BACUtil.deleteNode(response);
    }

    /**
     * This method checks if a SOAP Fault is returned as response.
     *
     * @param   xmi       TODO
     * @param   response  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void checkErrorInResponse(XPathMetaInfo xmi, int response)
                               throws SAPConnectorException
    {
        int faultStringNode = XPathHelper.selectSingleNode(response, "//SOAP:faultstring", xmi);

        if (faultStringNode != 0)
        {
            String faultString = Node.getDataWithDefault(faultStringNode, "");
            String faultCode = XPathHelper.getStringValue(response, "//SOAP:faultcode", xmi);

            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_METHOD_RETURNED_ASOAP_FAULT,
                                            faultCode, faultString,
                                            Node.writeToString(response, false));
        }
    }

    /**
     * This method creates a skeleton SOAP Message with the given parameters.
     *
     * @param   doc         : Document to be used to parse XML
     * @param   middleware  : Middleware of the connector
     * @param   receiver    : DN of the receiver SOAP Node
     * @param   uri         : uri of the connection point
     *
     * @return  DOCUMENTME
     */
    private int createSOAPMessage(Document doc, Middleware middleware, String receiver, String uri)
    {
        int envelope = doc.createElement("SOAP:Envelope");
        Node.setAttribute(envelope, "xmlns:SOAP", "http://schemas.xmlsoap.org/soap/envelope/");
        doc.createElement("SOAP:Header", envelope);
        SOAPMessage.setReceiver(envelope, receiver, uri);
        SOAPMessage.setSender(envelope, middleware.getName(), middleware.getAddress(),
                              organizationalUser);
        return SOAPMessage.createBodyNode(envelope);
    }

    /**
     * This method is called from the constructors to instantiate LDAP directory and connector to
     * LDAP.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void initialize()
                     throws SAPConnectorException
    {
        try
        {
            connector = Connector.getInstance("LDAP");
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.ERROR_GETTING_LDAP_INTERNAL_CONNECTOR);
        }

        if (!connector.isOpen())
        {
            connector.open();
        }

        directory = connector.getMiddleware().getDirectory();
    }
}
