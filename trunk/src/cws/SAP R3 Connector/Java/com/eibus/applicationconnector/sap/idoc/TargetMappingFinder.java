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

import java.util.Hashtable;
import java.util.StringTokenizer;

import com.cordys.coe.util.xml.NamespaceDefinitions;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.LDAPInterface;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class finds the target mapping for a given idoc based on any or all the three properties
 * message type,idoctype and recipient logicalsystem. Target mapping includes the methodname,
 * namespace and SOAPNode DN.
 *
 * @author  ygopal
 */
public class TargetMappingFinder
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(TargetMappingFinder.class);
    /**
     * DOCUMENTME.
     */
    private static String getXMLMethodName = "GetXMLObject";
    /**
     * If the searhc pattern is DEFAULT.
     */
    private String default_methodName;
    /**
     * DOCUMENTME.
     */
    private String default_namespace;
    /**
     * DOCUMENTME.
     */
    private String default_soapNodeDN;
    /**
     * To be used as delimiter between the property strings of mappings.
     */
    private String delimiter = "#*";
    /**
     * DOCUMENTME.
     */
    private Document doc;
    /**
     * An empty string is replaced by this, as String tokenizer doesn't count empty string as a
     * token.
     */
    private String emptyStringReplacer = "$%&";
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
    private int mappingsNode = 0;
    /**
     * Path where XML Mappings are stored.
     */
    private String mappingsPath;
    /**
     * DOCUMENTME.
     */
    private Hashtable<String, String> mappingTable;
    /**
     * Stores the SearchPattern to be used. It can be either DEFAULT or USER-ORDERED or BAC.
     */
    private String searchPatternToBeUsed;

    /**
     * Creates a new TargetMappingFinder object.
     *
     * @param   orgDN         : DN of the organization with which LDAPInterface should be
     *                        instantiated.
     * @param   mappingsPath  : path or key of the mappings object in the XML store.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public TargetMappingFinder(String orgDN, String mappingsPath)
                        throws SAPConnectorException
    {
        ldapInterface = new LDAPInterface(orgDN);
        this.mappingsPath = mappingsPath;
        doc = new Document();

        m_xmi = new XPathMetaInfo();
        m_xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_XML_STORE);
        m_xmi.addNamespaceBinding("SOAP", NamespaceDefinitions.XMLNS_SOAP_1_1);
    }

    /**
     * DOCUMENTME.
     *
     * @param   a  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static void main(String[] a)
                     throws SAPConnectorException
    {
        TargetMappingFinder tf = new TargetMappingFinder("o=GopalOrg,cn=cordys,o=vanenburg.com",
                                                         "cordys/bac/sap/targetmappings");
        tf.loadTargetMapppings();

        for (int i = 0; i < 1000; i++)
        {
            StringBuffer x = new StringBuffer();
            StringBuffer y = new StringBuffer();
            StringBuffer z = new StringBuffer();
            tf.getTargetMapping("MATMAS", "MATMAS02", "GOPAL", x, y, z);
            System.out.println("X " + x + " Y " + y + " Z " + z);
        }
        System.out.println("Done");
        tf.clear();
    }

    /**
     * This method clears the hashtable. Called when the SOAP Processor is stopped.
     */
    public void clear()
    {
        clearHashtable(mappingTable);
        deleteNode(mappingsNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Target Mapping Finder cleared.");
        }
    }

    /**
     * This method loads the target mappings from the XML Store into a hashtable. Everytime this
     * method is called, hashtable is reloaded. The target mapping XML is as follows.
     * <TargetMappings> <SearchPattern/> <Mapping> <Name/> <MESType/> <IDOCType/> <RecipientLS/>
     * <OrgDN/> <SOAPNodeDN/> <MethodSet/> <Method/> <Namespace/> </Mapping> <Mapping> ..........
     * ....... </TargetMappings>
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void loadTargetMapppings()
                             throws SAPConnectorException
    {
        // Send request to XML store SOAPProcessor
        int[] params = new int[1];
        params[0] = doc.createTextElement("key", mappingsPath);

        int responseNode = ldapInterface.executeMethod(getXMLMethodName,
                                                       SAPConnectorConstants.NS_XML_STORE, params,
                                                       doc, m_xmi);

        int targetMappingsNode = XPathHelper.selectSingleNode(responseNode,
                                                              "//ns:GetXMLObjectResponse/ns:tuple/ns:old/ns:TargetMappings",
                                                              m_xmi);

        loadTargetMappings(targetMappingsNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Target Mappings loaded.");
        }
        deleteNode(params[0]);
        deleteNode(responseNode);
    }

    /**
     * This method finds the target mapping for an IDOC based on the mestype, idoctype and receiver
     * logical system. To find the mapping, it will follow any of the 3 algorithms. default, user
     * ordered and BAC algorithm. In default, there is only one mapping for all the IDOCs. In user
     * ordered, there are multiple mappings. But the user maintains the order of mappings. Mappings
     * are scanned in the order and the first mappping found is returned. BAC algorithm tries to
     * find the mapping based on all combinations of mestype, idoctype and recipient logical system.
     * On average, it takes 4 searches to find the target mapping. BAC algorithm matches the
     * properties in the following order. MESType IDOCType RecipientLS MESType IDOCType "" MESType
     * "" RecipientLS MESType "" "" "" IDOCType RecipientLS "" IDOCType "" "" "" RecipientLS It
     * expects the targetSoapNodeDN, targetMethodName and targetNamespace as empty string buffers
     * and appends to them soapNodeDN, methodName and nameSpace respectively.
     *
     * @param   mesType           DOCUMENTME
     * @param   idocType          DOCUMENTME
     * @param   recipientLS       DOCUMENTME
     * @param   targetSoapNodeDN  DOCUMENTME
     * @param   targetMethodName  DOCUMENTME
     * @param   targetNameSpace   DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void getTargetMapping(String mesType, String idocType, String recipientLS,
                          StringBuffer targetSoapNodeDN, StringBuffer targetMethodName,
                          StringBuffer targetNameSpace)
                   throws SAPConnectorException
    {
        // System.out.println("IN get Target Mapping");
        boolean flag_mappingFound = false;
        
        if (searchPatternToBeUsed.equals("DEFAULT"))
        {
            targetSoapNodeDN.append(default_soapNodeDN);
            targetMethodName.append(default_methodName);
            targetNameSpace.append(default_namespace);
            // System.out.println("DONE");
            flag_mappingFound = true;
        }
        else if (searchPatternToBeUsed.equals("USER-ORDERED"))
        {
            int[] nodes_mapping = Find.match(mappingsNode, "<TargetMappings><Mapping>");
            int number_mappings = nodes_mapping.length;

            for (int i = 0; i < number_mappings; i++)
            {
                int node_mapping = nodes_mapping[i];
                int node_mesType = Find.firstMatch(node_mapping, "<Mapping><MESType>");
                int node_idocType = Find.firstMatch(node_mapping, "<Mapping><IDOCType>");
                int node_recipientLS = Find.firstMatch(node_mapping, "<Mapping><RecipientLS>");
                String text_mesType = Node.getDataWithDefault(node_mesType, "");
                String text_idocType = Node.getDataWithDefault(node_idocType, "");
                String text_recipientLS = Node.getDataWithDefault(node_recipientLS, "");

                // It is enough if at least one combination of them matches.
                if ((text_mesType.equalsIgnoreCase(mesType) && text_idocType.equals(idocType) &&
                         text_recipientLS.equalsIgnoreCase(recipientLS)) ||
                        (text_mesType.equalsIgnoreCase(mesType) && text_idocType.equals(idocType) &&
                             text_recipientLS.equalsIgnoreCase("")) ||
                        (text_mesType.equalsIgnoreCase(mesType) && text_idocType.equals("") &&
                             text_recipientLS.equalsIgnoreCase(recipientLS)) ||
                        (text_mesType.equalsIgnoreCase(mesType) && text_idocType.equals("") &&
                             text_recipientLS.equalsIgnoreCase("")) ||
                        (text_mesType.equalsIgnoreCase("") && text_idocType.equals(idocType) &&
                             text_recipientLS.equalsIgnoreCase(recipientLS)) ||
                        (text_mesType.equalsIgnoreCase("") && text_idocType.equals(idocType) &&
                             text_recipientLS.equalsIgnoreCase("")) ||
                        (text_mesType.equalsIgnoreCase("") && text_idocType.equals("") &&
                             text_recipientLS.equalsIgnoreCase(recipientLS)))
                {
                    int node_soapNodeDN = Find.firstMatch(node_mapping, "<Mapping><SOAPNodeDN>");
                    int node_method = Find.firstMatch(node_mapping, "<Mapping><Method>");
                    int node_namespace = Find.firstMatch(node_mapping, "<Mapping><Namespace>");
                    String text_soapNodeDN = Node.getDataWithDefault(node_soapNodeDN, "");
                    String text_method = Node.getDataWithDefault(node_method, "");
                    String text_namespace = Node.getDataWithDefault(node_namespace, "");
                    targetSoapNodeDN.append(text_soapNodeDN);
                    targetMethodName.append(text_method);
                    targetNameSpace.append(text_namespace);
                    flag_mappingFound = true;
                    // System.out.println("DONE");
                    break;
                }
            }
        }
        else
        { // BAC
            mesType = replaceEmptyString(mesType);
            idocType = replaceEmptyString(idocType);
            recipientLS = replaceEmptyString(recipientLS);

            String key = null;
            String value = null;
            int count = 0;

            do
            {
                count++;
                key = getCombinationOfStrings(mesType, idocType, recipientLS, count);
                value = mappingTable.get(key);
            }
            while (value == null);

            StringTokenizer value_tokenizer = new StringTokenizer(value, delimiter);
            String token_soapNodeDN = reverseReplaceEmptyString(value_tokenizer.nextToken());
            String token_method = reverseReplaceEmptyString(value_tokenizer.nextToken());
            String token_namespace = reverseReplaceEmptyString(value_tokenizer.nextToken());
            targetSoapNodeDN.append(token_soapNodeDN);
            targetMethodName.append(token_method);
            targetNameSpace.append(token_namespace);
            // System.out.println("DONE");
            flag_mappingFound = true;
        }

        // If no mapping is found
        if (!flag_mappingFound)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.TARGET_MAPPING_NOT_FOUND_FOR_IDOC,
                                            idocType);
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param  mappingTable  DOCUMENTME
     */
    private void clearHashtable(Hashtable<String, String> mappingTable)
    {
        if (mappingTable != null)
        {
            mappingTable.clear();
            mappingTable = null;
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param  nodeToBeDeleted  DOCUMENTME
     */
    private void deleteNode(int nodeToBeDeleted)
    {
        if (nodeToBeDeleted != 0)
        {
            Node.delete(nodeToBeDeleted);
        }
    }

    /**
     * This method returns a combination based on the count parameter.
     *
     * @param   mesType      DOCUMENTME
     * @param   idocType     DOCUMENTME
     * @param   recipientLS  DOCUMENTME
     * @param   count        DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private String getCombinationOfStrings(String mesType, String idocType, String recipientLS,
                                           int count)
    {
        switch (count)
        {
            case 1:
                return mesType + delimiter + idocType + delimiter + recipientLS;

            case 2:
                return mesType + delimiter + idocType + delimiter + emptyStringReplacer;

            case 3:
                return mesType + delimiter + emptyStringReplacer + delimiter + recipientLS;

            case 4:
                return mesType + delimiter + emptyStringReplacer + delimiter + emptyStringReplacer;

            case 5:
                return emptyStringReplacer + delimiter + idocType + delimiter + recipientLS;

            case 6:
                return emptyStringReplacer + delimiter + idocType + delimiter + emptyStringReplacer;

            case 7:
                return emptyStringReplacer + delimiter + emptyStringReplacer + delimiter +
                       recipientLS;

            default:
                return "";
        }
    }

    /**
     * This method loads the target mappings into hashtable if the searchPattern is user or BAC. If
     * the SearchPattern is default, it stores the mappings in string variables. It also sets the
     * variable algorithm. Everytime this method is called, it clears the existing hashtable and
     * reloads it.
     *
     * @param   targetMappingsNode  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void loadTargetMappings(int targetMappingsNode)
                             throws SAPConnectorException
    {
        String errorMessage;

        if (targetMappingsNode == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.TARGET_MAPPINGS_NOT_FOUND_IN_MAPPING_XML);
        }

        int searchPatternNode = Find.firstMatch(targetMappingsNode,
                                                "<TargetMappings><SearchPattern>");
        String searchPattern = Node.getDataWithDefault(searchPatternNode, "");
        searchPattern = searchPattern.toUpperCase();

        if (LOG.isDebugEnabled())
        {
            LOG.debug(" Search Pattern for target mappings is :" + searchPattern);
        }

        if (searchPattern.equals("") ||
                !(searchPattern.equals("DEFAULT") || searchPattern.equals("USER-ORDERED") ||
                      searchPattern.equals("BAC")))
        {
            errorMessage = "SearchPattern not properly specified in the TargetMapping XML.";

            throw new SAPConnectorException(SAPConnectorExceptionMessages.SEARCH_PATTERN_NOT_PROPERLY_SPECIFIED);
        }
        searchPatternToBeUsed = searchPattern;
        if (LOG.isDebugEnabled())
        {
            LOG.debug("********* searchPatternToBeUsed is :" + searchPatternToBeUsed);
        }
        if (searchPatternToBeUsed.equals("DEFAULT"))
        {
            int node_method = Find.firstMatch(targetMappingsNode,
                                              "<TargetMappings><Mapping><Method>");
            int node_namespace = Find.firstMatch(targetMappingsNode,
                                                 "<TargetMappings><Mapping><Namespace>");
            int node_soapNode = Find.firstMatch(targetMappingsNode,
                                                "<TargetMappings><Mapping><SOAPNodeDN>");
            String method = Node.getDataWithDefault(node_method, "");
            String namespace = Node.getDataWithDefault(node_namespace, "");
            String soapNodeDN = Node.getDataWithDefault(node_soapNode, "");

            if (method.equals("") || namespace.equals("") || soapNodeDN.equals(""))
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.INVALID_METHOD_DETAILS);
            }
            default_methodName = method;
            default_namespace = namespace;
            default_soapNodeDN = soapNodeDN;
            clearHashtable(mappingTable);
            deleteNode(mappingsNode);
        }
        else
        { // BAC or USER-ORDERED

            int[] mappingNodes = Find.match(targetMappingsNode, "<TargetMappings><Mapping>");
            int number_mappings = mappingNodes.length;

            if (number_mappings == 0)
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.NO_MAPPINGS_FOUND_IN_TARGET_MAPPING_XML);
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Number of target mappings is " + number_mappings);
                }
            }

            if (searchPatternToBeUsed.equals("USER-ORDERED"))
            {
                // Keep in the mappingsNode in memory.
                mappingsNode = Node.duplicate(targetMappingsNode);
                clearHashtable(mappingTable);
            }
            else
            { // BAC
                // Loading them into hashtable.
                deleteNode(mappingsNode);

                if (mappingTable == null)
                {
                    mappingTable = new Hashtable<String, String>();
                }

                for (int i = 0; i < number_mappings; i++)
                {
                    int node_mesType = Find.firstMatch(mappingNodes[i], "<Mapping><MESType>");
                    int node_idocType = Find.firstMatch(mappingNodes[i], "<Mapping><IDOCType>");
                    int node_recipientLS = Find.firstMatch(mappingNodes[i],
                                                           "<Mapping><RecipientLS>");
                    int node_method = Find.firstMatch(mappingNodes[i], "<Mapping><Method>");
                    int node_namespace = Find.firstMatch(mappingNodes[i], "<Mapping><Namespace>");
                    int node_soapNode = Find.firstMatch(mappingNodes[i], "<Mapping><SOAPNodeDN>");
                    String mesType = Node.getDataWithDefault(node_mesType, emptyStringReplacer);
                    String idocType = Node.getDataWithDefault(node_idocType, emptyStringReplacer);
                    String recipientLS = Node.getDataWithDefault(node_recipientLS,
                                                                 emptyStringReplacer);
                    String method = Node.getDataWithDefault(node_method, emptyStringReplacer);
                    String namespace = Node.getDataWithDefault(node_namespace, emptyStringReplacer);
                    String soapNodeDN = Node.getDataWithDefault(node_soapNode, emptyStringReplacer);
                    // Adding delimiter. Useful to tokenize later.
                    String key = mesType + delimiter + idocType + delimiter + recipientLS;
                    String value = soapNodeDN + delimiter + method + delimiter + namespace;
                    mappingTable.put(key, value);
                }
            }
        }
    }

    /**
     * Utility funciton.
     *
     * @param   givenString  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private String replaceEmptyString(String givenString)
    {
        if (givenString.equals(""))
        {
            return emptyStringReplacer;
        }
        else
        {
            return givenString;
        }
    }

    /**
     * Utility function.
     *
     * @param   givenString  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private String reverseReplaceEmptyString(String givenString)
    {
        if (givenString.equals(emptyStringReplacer))
        {
            return "";
        }
        else
        {
            return givenString;
        }
    }
}
