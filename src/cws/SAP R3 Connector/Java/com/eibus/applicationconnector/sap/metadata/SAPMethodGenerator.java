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

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * This is an abstract class to generate LDAP methods for BAPIs, RFCs and IDOCs. It contains the
 * common code, where are its subclasses contain the specifics based on the middleware used.
 *
 * @author  ygopal
 */
public abstract class SAPMethodGenerator
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPMethodGenerator.class);
    /**
     * DOCUMENTME.
     */
    protected static String cordysNamespace = "http://schemas.cordys.com/";
    /**
     * DOCUMENTME.
     */
    protected static String xmlSchemaNameSpace = "http://www.w3.org/2000/10/XMLSchema";
    /**
     * DOCUMENTME.
     */
    protected static String wsdlNamespace = "http://schemas.xmlsoap.org/wsdl/";
    /**
     * DOCUMENTME.
     */
    protected static String tnsPrefix = "tns";
    /**
     * DOCUMENTME.
     */
    protected static String wcpPrefix = "wcp";
    /**
     * DOCUMENTME.
     */
    protected static String xsdPrefix = "xsd:";
    /**
     * DOCUMENTME.
     */
    private static String controlRecordSchemaString = null;
    /**
     * DOCUMENTME.
     */
    protected String methodSet;
    /**
     * DOCUMENTME.
     */
    protected String namespace;
    /**
     * DOCUMENTME.
     */
    protected WSDLPublisher publisher;

    /**
     * This method creates the proper method generator.
     *
     * @param   request   The request for generation.
     * @param   response  The response to hold the WSDL and realizations.
     *
     * @return  The method generator to use.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public abstract MethodGenerator createMethodGenerator(BodyBlock request, BodyBlock response)
                                                   throws SAPConnectorException;

    /**
     * This method published LDAP methods for BAPIs, RFCs and IDOCs.
     *
     * @param   request   request bodyblock
     * @param   response  request bodyblock
     *
     * @return  Returns true if successful.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public boolean publishMethod(BodyBlock request, BodyBlock response)
                          throws SAPConnectorException
    {
        int requestNode = request.getXMLNode();
        Document doc = Node.getDocument(requestNode);
        String methodName = Node.getLocalName(requestNode);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Called method Name is " + methodName);
        }

        if (methodName.equals("GenerateMethods"))
        {
            generateMethods(request, response);
        }
        else
        {
            publishMethodSetAndAttachToSOAPNode(requestNode, methodName, doc);

            if (methodName.equalsIgnoreCase("PublishBAPI"))
            {
                publishBAPI(requestNode, doc);
            }
            else if (methodName.equalsIgnoreCase("PublishRFC"))
            {
                publishRFC(requestNode, doc);
            }
            else
            {
                publishIDOC(requestNode, doc);
            }
        }

        return true;
    }

    /**
     * To generate input and output elements for an RFC method interface.
     *
     * @param   schemaNode     DOCUMENTME
     * @param   idocType       DOCUMENTME
     * @param   cimType        DOCUMENTME
     * @param   inputElement   DOCUMENTME
     * @param   outputElement  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract void createInputOutputElementsForIDOC(int schemaNode, String idocType,
                                                             String cimType, String inputElement,
                                                             String outputElement,
                                                             Document doc)
                                                      throws SAPConnectorException;

    /**
     * To be implemented by subclasses. To generate input and output elements for an RFC method
     * interface
     *
     * @param   schemaNode     DOCUMENTME
     * @param   rfmName        DOCUMENTME
     * @param   inputElement   DOCUMENTME
     * @param   outputElement  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected abstract void createInputOutputElementsForRFC(int schemaNode, String rfmName,
                                                            String inputElement,
                                                            String outputElement,
                                                            Document doc)
                                                     throws SAPConnectorException;

    /**
     * This method appends the common control structure EDI_DC40 to the schema of and IDOC while
     * publishing. As this is common across all IDOCs, it is hardcoded.
     *
     * @param   parentNode  DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void appendIDOCControlStructure(int parentNode, Document doc)
                                       throws SAPConnectorException
    {
        // This is the string corrsponding to the XML of the schema.
        if (controlRecordSchemaString == null)
        {
            controlRecordSchemaString = new String("<element name=\"EDI_DC40\">" +
                                                   "<annotation><documentation>IDoc Control Record for Interface to External System</documentation></annotation>" +
                                                   "<complexType><sequence><element name=\"TABNAM\" type=\"" +
                                                   xsdPrefix + "string\" fixed=\"EDI_DC40\">" +
                                                   "<annotation><documentation>Name of table structure</documentation></annotation></element>" +
                                                   "<element name=\"MANDT\" minOccurs=\"0\"><annotation><documentation>Client</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"3\"/></restriction></simpleType></element>" +
                                                   "<element name=\"DOCNUM\" minOccurs=\"0\"><annotation><documentation>IDoc number</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"16\"/></restriction></simpleType></element>" +
                                                   "<element name=\"DOCREL\" minOccurs=\"0\"><annotation><documentation>SAP Release for IDoc</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"4\"/></restriction></simpleType></element>" +
                                                   "<element name=\"STATUS\" minOccurs=\"0\"><annotation><documentation>Status of IDoc</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"2\"/></restriction></simpleType></element>" +
                                                   "<element name=\"DIRECT\"><annotation><documentation>Direction</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><enumeration value=\"1\"><annotation><documentation>Outbound</documentation>" +
                                                   "</annotation></enumeration><enumeration value=\"2\"><annotation><documentation>Inbound</documentation></annotation>" +
                                                   "</enumeration></restriction></simpleType></element><element name=\"OUTMOD\" minOccurs=\"0\"><annotation>" +
                                                   "<documentation>Output mode</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix + "string\"><maxLength value=\"1\"/>" +
                                                   "</restriction></simpleType></element><element name=\"EXPRSS\" minOccurs=\"0\"><annotation><documentation>Overriding in " +
                                                   "inbound processing</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"1\"/></restriction>" +
                                                   "</simpleType></element><element name=\"TEST\" minOccurs=\"0\"><annotation><documentation>Test flag</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"1\"/></restriction></simpleType></element><element name=\"IDOCTYP\"" +
                                                   " type=\"" + xsdPrefix +
                                                   "string\" fixed=\"MATMAS02\"><annotation><documentation>Name of basic type</documentation></annotation></element>" +
                                                   "<element name=\"CIMTYP\" minOccurs=\"0\"><annotation><documentation>Extension (defined by customer)</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"30\"/></restriction></simpleType></element><element name=\"MESTYP\" minOccurs=\"0\">" +
                                                   "<annotation><documentation>Message type</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"30\"/>" +
                                                   "</restriction></simpleType></element><element name=\"MESCOD\" minOccurs=\"0\"><annotation><documentation>Message code</documentation>" +
                                                   "</annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"3\"/></restriction></simpleType></element>" +
                                                   "<element name=\"MESFCT\" minOccurs=\"0\"><annotation><documentation>Message function</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"3\"/></restriction></simpleType></element><element name=\"STD\" minOccurs=\"0\"><annotation>" +
                                                   "<documentation>EDI standard, flag</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix + "string\"><maxLength value=\"1\"/>" +
                                                   "</restriction></simpleType></element><element name=\"STDVRS\" minOccurs=\"0\"><annotation><documentation>EDI standard, version and release" +
                                                   "</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"6\"/></restriction></simpleType></element>" +
                                                   "<element name=\"STDMES\" minOccurs=\"0\"><annotation><documentation>EDI message type</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"6\"/></restriction></simpleType></element><element name=\"SNDPOR\"><annotation>" +
                                                   "<documentation>Sender port (SAP System, external subsystem)</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix + "string\">" +
                                                   "<maxLength value=\"10\"/></restriction></simpleType></element><element name=\"SNDPRT\"><annotation><documentation>Partner type of sender" +
                                                   "</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"2\"/></restriction></simpleType></element>" +
                                                   "<element name=\"SNDPFC\" minOccurs=\"0\"><annotation><documentation>Partner Function of Sender</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"2\"/></restriction></simpleType></element><element name=\"SNDPRN\"><annotation>" +
                                                   "<documentation>Partner number of sender</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"10\"/>" +
                                                   "</restriction></simpleType></element><element name=\"SNDSAD\" minOccurs=\"0\"><annotation><documentation>Sender address (SADR)</documentation>" +
                                                   "</annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"21\"/></restriction></simpleType></element><element " +
                                                   "name=\"SNDLAD\" minOccurs=\"0\"><annotation><documentation>Logical address of sender</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"RCVPOR\"><annotation>" +
                                                   "<documentation>Receiver port</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"10\"/></restriction>" +
                                                   "</simpleType></element><element name=\"RCVPRT\" minOccurs=\"0\"><annotation><documentation>Partner type of recipient</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"2\"/></restriction></simpleType></element><element name=\"RCVPFC\" minOccurs=\"0\">" +
                                                   "<annotation><documentation>Partner function of recipient</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix + "string\"><maxLength value=\"2\"/>" +
                                                   "</restriction></simpleType></element><element name=\"RCVPRN\"><annotation><documentation>Partner number of recipient</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"10\"/></restriction></simpleType></element><element name=\"RCVSAD\" minOccurs=\"0\"><annotation>" +
                                                   "<documentation>Recipient address (SADR)</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"21\"/></restriction>" +
                                                   "</simpleType></element><element name=\"RCVLAD\" minOccurs=\"0\"><annotation><documentation>Logical address of recipient</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"CREDAT\" minOccurs=\"0\">" +
                                                   "<annotation><documentation>Created on</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"8\"/></restriction>" +
                                                   "</simpleType></element><element name=\"CRETIM\" minOccurs=\"0\"><annotation><documentation>Time created</documentation></annotation><simpleType>" +
                                                   "<restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"6\"/></restriction></simpleType></element><element name=\"REFINT\" minOccurs=\"0\"><annotation>" +
                                                   "<documentation>Transmission file (EDI Interchange)</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"14\"/>" +
                                                   "</restriction></simpleType></element><element name=\"REFGRP\" minOccurs=\"0\"><annotation><documentation>Message group (EDI Message Group)</documentation>" +
                                                   "</annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"14\"/></restriction></simpleType></element><element name=\"REFMES\" minOccurs=\"0\">" +
                                                   "<annotation><documentation>Message (EDI Message)</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"14\"/></restriction>" +
                                                   "</simpleType></element><element name=\"ARCKEY\" minOccurs=\"0\"><annotation><documentation>Key for external message archive</documentation></annotation>" +
                                                   "<simpleType><restriction base=\"" + xsdPrefix +
                                                   "string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"SERIAL\" minOccurs=\"0\"><annotation>" +
                                                   "<documentation>Serialization</documentation></annotation><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><maxLength value=\"20\"/></restriction></simpleType>" +
                                                   "</element></sequence><attribute name=\"SEGMENT\" use=\"required\"><simpleType><restriction base=\"" +
                                                   xsdPrefix +
                                                   "string\"><enumeration value=\"1\"/></restriction></simpleType>" +
                                                   "</attribute></complexType></element>");
        }

        try
        {
            int controlRecordNode = doc.parseString(controlRecordSchemaString);
            Node.appendToChildren(controlRecordNode, parentNode);
        }
        catch (Exception xe)
        {
            throw new SAPConnectorException(xe,
                                            SAPConnectorExceptionMessages.ERROR_PARSING_CONTROL_RECORD_SCHEMA,
                                            controlRecordSchemaString);
        }
    }

    /**
     * This method creates an attribute node with the given name.
     *
     * @param  parentNode     DOCUMENTME
     * @param  attributeName  DOCUMENTME
     * @param  doc            DOCUMENTME
     */
    protected void createAttributeNode(int parentNode, String attributeName, Document doc)
    {
        int attributeNode = doc.createElement("attribute", parentNode);
        Node.setAttribute(attributeNode, "name", attributeName);
        Node.setAttribute(attributeNode, "use", "required");

        int simpleTypeNode = doc.createElement("simpleType", attributeNode);
        int restrictionNode = doc.createElement("restriction", simpleTypeNode);
        Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");

        int enumerationNode = doc.createElement("enumeration", restrictionNode);
        Node.setAttribute(enumerationNode, "value", "1");
    }

    /**
     * This method generates the common part of the schema that every IDOC has. And it returns the
     * sequence node in the common element with name IDOC.
     *
     * @param   schemaNode    DOCUMENTME
     * @param   inputElement  DOCUMENTME
     * @param   doc           DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected int createCommonPartOfIDOCInputElement(int schemaNode, String inputElement,
                                                     Document doc)
                                              throws SAPConnectorException
    {
        int elementNode = doc.createElement("element", schemaNode);
        Node.setAttribute(elementNode, "name", inputElement);

        int complexTypeNode = doc.createElement("complexType", elementNode);
        int sequenceNode = doc.createElement("sequence", complexTypeNode);
        elementNode = doc.createElement("element", sequenceNode);
        Node.setAttribute(elementNode, "name", "IDOC");
        complexTypeNode = doc.createElement("complexType", elementNode);
        sequenceNode = doc.createElement("sequence", complexTypeNode);
        createAttributeNode(complexTypeNode, "BEGIN", doc);
        // We are adding control structure element EDI_DC40
        appendIDOCControlStructure(sequenceNode, doc);
        return sequenceNode;
    }

    /**
     * To create the definitions node in the interface.
     *
     * @param   methodName  DOCUMENTME
     * @param   doc         DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    protected int createDefinitionsNode(String methodName, Document doc)
    {
        int definitionsNode = doc.createElement("definitions");
        Node.setAttribute(definitionsNode, "name", methodName);
        Node.setAttribute(definitionsNode, "targetNamespace", namespace);
        Node.setAttribute(definitionsNode, "xmlns", wsdlNamespace);
        Node.setAttribute(definitionsNode, "xmlns:" + tnsPrefix, namespace);
        return definitionsNode;
    }

    /**
     * To create message nodes, one for input element and one for output element.
     *
     * @param  definitionsNode  DOCUMENTME
     * @param  inputMessage     DOCUMENTME
     * @param  inputElement     DOCUMENTME
     * @param  outputMessage    DOCUMENTME
     * @param  outputElement    DOCUMENTME
     * @param  doc              DOCUMENTME
     */
    protected void createMessageNodes(int definitionsNode, String inputMessage, String inputElement,
                                      String outputMessage, String outputElement, Document doc)
    {
        int inputMessageNode = doc.createElement("message", definitionsNode);
        Node.setAttribute(inputMessageNode, "name", tnsPrefix + ":" + inputMessage);

        int inputPartNode = doc.createElement("part", inputMessageNode);
        Node.setAttribute(inputPartNode, "name", "body");
        Node.setAttribute(inputPartNode, "element", tnsPrefix + ":" + inputElement);

        int outputMessageNode = doc.createElement("message", definitionsNode);
        Node.setAttribute(outputMessageNode, "name", tnsPrefix + ":" + outputMessage);

        int outputPartNode = doc.createElement("part", outputMessageNode);
        Node.setAttribute(outputPartNode, "name", "body");
        Node.setAttribute(outputPartNode, "element", tnsPrefix + ":" + outputElement);
    }

    /**
     * This method generates the output schema element for the IDOC methods. <tid>...</tid>
     *
     * @param  schemaNode     DOCUMENTME
     * @param  outputElement  DOCUMENTME
     * @param  doc            DOCUMENTME
     */
    protected void createOutputSchemaElementforIDOC(int schemaNode, String outputElement,
                                                    Document doc)
    {
        int elementNode = doc.createElement("element", schemaNode);
        Node.setAttribute(elementNode, "name", outputElement);

        int complexTypeNode = doc.createElement("complexType", elementNode);
        int sequenceNode = doc.createElement("sequence", complexTypeNode);
        elementNode = doc.createElement("element", sequenceNode);
        Node.setAttribute(elementNode, "name", "tid");

        int annotationNode = doc.createElement("annotation", elementNode);
        doc.createTextElement("documentation", "Transaction ID of the IDOC", annotationNode);

        int simpleTypeNode = doc.createElement("simpleType", elementNode);
        int restrictionNode = doc.createElement("restriction", simpleTypeNode);
        Node.setAttribute(restrictionNode, "base", xsdPrefix + "int");
    }

    /**
     * To create the portType node of the interface and append to the defintions node.
     *
     * @param  definitonsNode  DOCUMENTME
     * @param  methodName      DOCUMENTME
     * @param  inputMessage    DOCUMENTME
     * @param  outputMessage   DOCUMENTME
     * @param  doc             DOCUMENTME
     */
    protected void createPortTypeNode(int definitonsNode, String methodName, String inputMessage,
                                      String outputMessage, Document doc)
    {
        int portTypeNode = doc.createElement("portType", definitonsNode);
        Node.setAttribute(portTypeNode, "name", methodSet + " PortType");

        int operationNode = doc.createElement("operation", portTypeNode);
        Node.setAttribute(operationNode, "name", methodName);

        int inputNode = doc.createElement("input", operationNode);
        Node.setAttribute(inputNode, "message", tnsPrefix + ":" + inputMessage);

        int outputNode = doc.createElement("output", operationNode);
        Node.setAttribute(outputNode, "message", tnsPrefix + ":" + outputMessage);
    }

    /**
     * To create the types node of the interface. If for IDOC, isIDOC is true and mesType,cimType
     * and released should be provided. If for RFC, isIDoc is false. mesType, cimType, released are
     * null.
     *
     * @param   definitionsNode  DOCUMENTME
     * @param   sapItemName      DOCUMENTME
     * @param   inputElement     DOCUMENTME
     * @param   outputElement    DOCUMENTME
     * @param   doc              DOCUMENTME
     * @param   isIDoc           DOCUMENTME
     * @param   extension        : cimType in case of IDOCs. Otherwise null.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void createTypesNode(int definitionsNode, String sapItemName, String inputElement,
                                   String outputElement, Document doc, boolean isIDoc,
                                   String extension)
                            throws SAPConnectorException
    {
        int typesNode = doc.createElement("types", definitionsNode);
        int schemaNode = doc.createElement("schema", typesNode);
        Node.setAttribute(schemaNode, "targetNamespace", namespace);
        Node.setAttribute(schemaNode, "xmlns", xmlSchemaNameSpace);
        Node.setAttribute(schemaNode, "xmlns:" + wcpPrefix, cordysNamespace);

        if (isIDoc)
        {
            createInputOutputElementsForIDOC(schemaNode, sapItemName, extension, inputElement,
                                             outputElement, doc);
        }
        else
        {
            createInputOutputElementsForRFC(schemaNode, sapItemName, inputElement, outputElement,
                                            doc);
        }
    }

    /**
     * To generate the implementation for an RFC method.
     *
     * @param   type  DOCUMENTME
     * @param   doc   DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    protected int generateImplementation(String type, Document doc)
    {
        int implNode = doc.createElement("implementation");
        Node.setAttribute(implNode, "type", type);
        return implNode;
    }

    /**
     * To generate the implementation for a BAPI method.
     *
     * @param   type     DOCUMENTME
     * @param   rfmName  DOCUMENTME
     * @param   doc      DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    protected int generateImplementation(String type, String rfmName, Document doc)
    {
        int implNode = generateImplementation(type, doc);
        doc.createTextElement("RFMName", rfmName, implNode);
        return implNode;
    }

    /**
     * To generate the implementation for an IDOC method.
     *
     * @param   type     DOCUMENTME
     * @param   mesType  DOCUMENTME
     * @param   cimType  DOCUMENTME
     * @param   doc      DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    protected int generateImplementation(String type, String mesType, String cimType, Document doc)
    {
        int implNode = generateImplementation(type, doc);
        // doc.createTextElement("IDOCType", idocType, implNode);
        doc.createTextElement("MESType", mesType, implNode);
        doc.createTextElement("CIMType", cimType, implNode);
        return implNode;
    }

    /**
     * To generate the interface for a method. for both RFCs and IDOCs.
     *
     * @param   methodName   : Name of the method to be published
     * @param   sapItemName  : Name of the RFC or IDOC Type
     * @param   doc          : Document object to be used
     * @param   isIDoc       : True if interface is for IDOC else false.
     * @param   extension    : cimType in case of IDOCs. Otherwise null.
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected int generateInterface(String methodName, String sapItemName, Document doc,
                                    boolean isIDoc, String extension)
                             throws SAPConnectorException
    {
        int interfaceNode = createDefinitionsNode(methodName, doc);
        String inputMessage = methodName + "input";
        String outputMessage = methodName + "output";
        String inputElement = methodName;
        String outputElement = methodName + "Response";

        createTypesNode(interfaceNode, sapItemName, inputElement, outputElement, doc, isIDoc,
                        extension);

        createMessageNodes(interfaceNode, inputMessage, inputElement, outputMessage, outputElement,
                           doc);

        createPortTypeNode(interfaceNode, methodName, inputMessage, outputMessage, doc);
        return interfaceNode;
    }

    /**
     * This method generates the simple type node for the primitive data type for the data
     * dictionary type of a given SAP data type and appends it to the element Node. The mappings are
     * given below. CHAR, UNIT, CUKY, LANG, CLNT, STRING - C NUMC, ACCP, PREC -N DATS - D TIMS - T
     * QUAN, CURR, DEC - P FLTP - F INT1 - b INT2, INT4 - I LRAW, RAW, RAWSTRING - X If nothing
     * matches, assume C.( string type) fieldLength - integer to be assingned to restriction first
     * child, if needed. If not needed it is 0.Either maxLength or length. decimalLength - integer
     * to be assingned to restriction second child, if needed. If not needed it is 0. no of
     * fractional digits for decimals.
     *
     * @param  elementNode     DOCUMENTME
     * @param  dicitonaryType  DOCUMENTME
     * @param  doc             DOCUMENTME
     * @param  fieldLength     DOCUMENTME
     * @param  decimalLength   DOCUMENTME
     */
    protected void generateSimpleDataTypeNode(int elementNode, String dicitonaryType, Document doc,
                                              int fieldLength, int decimalLength)
    {
        int simpleTypeNode = 0;
        int maxLengthNode = 0;
        int lengthNode = 0;
        int restrictionNode = 0;
        int patternNode = 0;

        if (dicitonaryType.equals("N") || dicitonaryType.equals("NUMC") ||
                dicitonaryType.equals("ACCP") || dicitonaryType.equals("PREC"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
            patternNode = doc.createElement("pattern", restrictionNode);
            Node.setAttribute(patternNode, "value", "\\d+");
            maxLengthNode = doc.createElement("maxLength", restrictionNode);
            Node.setAttribute(maxLengthNode, "value", Integer.toString(fieldLength));
        }
        else if (dicitonaryType.equals("P") || dicitonaryType.equals("QUAN") ||
                     dicitonaryType.equals("CURR") || dicitonaryType.equals("DEC"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "decimal");

            int totalDigitsNode = doc.createElement("totalDigits", restrictionNode);
            Node.setAttribute(totalDigitsNode, "value", Integer.toString(fieldLength));

            int fractionalDigitsNode = doc.createElement("fractionalDigits", restrictionNode);
            Node.setAttribute(fractionalDigitsNode, "value", Integer.toString(decimalLength));
        }
        else if (dicitonaryType.equals("D") || dicitonaryType.equals("DATS"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
            patternNode = doc.createElement("pattern", restrictionNode);
            Node.setAttribute(patternNode, "value", "....-..-..");
        }
        else if (dicitonaryType.equals("T") || dicitonaryType.equals("TIMS"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
            patternNode = doc.createElement("pattern", restrictionNode);
            Node.setAttribute(patternNode, "value", "..-..-..");
        }
        else if (dicitonaryType.equals("I") || dicitonaryType.equals("INT2") ||
                     dicitonaryType.equals("INT4"))
        {
            Node.setAttribute(elementNode, "type", xsdPrefix + "int");
        }
        else if (dicitonaryType.equals("b") || dicitonaryType.equals("INT1"))
        {
            Node.setAttribute(elementNode, "type", xsdPrefix + "unsignedByte");
        }
        else if (dicitonaryType.equals("F") || dicitonaryType.equals("FLTP"))
        {
            Node.setAttribute(elementNode, "type", xsdPrefix + "double");
        }
        else if (dicitonaryType.equals("X") || dicitonaryType.equals("RAW") ||
                     dicitonaryType.equals("LRAW") || dicitonaryType.equals("RAWSTRING"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "base64Binary");
            lengthNode = doc.createElement("length", restrictionNode);
            Node.setAttribute(lengthNode, "value", Integer.toString(fieldLength));
        }
        else if (dicitonaryType.equals("C") || dicitonaryType.equals("CHAR") ||
                     dicitonaryType.equals("UNIT") || dicitonaryType.equals("CUKY") ||
                     dicitonaryType.equals("LANG") || dicitonaryType.equals("CLNT") ||
                     dicitonaryType.equals("STRING"))
        {
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
            maxLengthNode = doc.createElement("maxLength", restrictionNode);
            Node.setAttribute(maxLengthNode, "value", Integer.toString(fieldLength));
        }
        else
        { // If the data type is unknown
            simpleTypeNode = doc.createElement("simpleType", elementNode);
            restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
        }
    }

    /**
     * Method to publish methods of type BAPI Method name would be BOname.BAPIname.
     *
     * @param   requestNode  DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void publishBAPI(int requestNode, Document doc)
                        throws SAPConnectorException
    {
        String businessObject = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                        "<PublishBAPI><BO>"), "");
        String bapiName = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                  "<PublishBAPI><BAPI>"), "");
        String rfmName = Node.getDataWithDefault(Find.firstMatch(requestNode, "<PublishBAPI><RFM>"),
                                                 "");

        if (businessObject.equals("") || bapiName.equals("") || rfmName.equals(""))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        String overwrite = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                   "<PublishBAPI><overwrite>"), "");
        boolean boolOverwrite = new Boolean(overwrite).booleanValue();
        String methodName = businessObject + "." + bapiName;
        int implementationNode = generateImplementation("SAPBAPI", rfmName, doc);
        int interfaceNode = generateInterface(methodName, rfmName, doc, false, null);
        publishMethod(methodName, implementationNode, interfaceNode, boolOverwrite);
    }

    /**
     * Method to publish methods of type IDOC Method Name would be (MESType_IDOCType) IDOCType.
     *
     * @param   requestNode  DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void publishIDOC(int requestNode, Document doc)
                        throws SAPConnectorException
    {
        String idocType = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                  "<PublishIDOC><IDOCType>"), "");
        String mesType = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                 "<PublishIDOC><MESType>"), "");
        // Can be empty for an IDOCType. So it is not validated.
        String cimType = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                 "<PublishIDOC><CIMType>"), "");

        // String released =
        // Node.getDataWithDefault(Find.firstMatch(requestNode,"<PublishIDOC><Released>"),"");
        if (idocType.equals("") || mesType.equals(""))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        String overwrite = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                   "<PublishIDOC><overwrite>"), "");
        boolean boolOverwrite = new Boolean(overwrite).booleanValue();
        // String methodName = mesType + "_" + idocType ;
        String methodName = idocType;
        int implementationNode = generateImplementation("SAPIDOC", mesType, cimType, doc);
        int interfaceNode = generateInterface(methodName, idocType, doc, true, cimType);
        publishMethod(methodName, implementationNode, interfaceNode, boolOverwrite);
    }

    /**
     * This publishes the given method with the given implementation and interface.
     *
     * @param   method              DOCUMENTME
     * @param   implementationNode  DOCUMENTME
     * @param   interfaceNode       DOCUMENTME
     * @param   overwrite           DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void publishMethod(String method, int implementationNode, int interfaceNode,
                                 boolean overwrite)
                          throws SAPConnectorException
    {
        // Convert implementation node and interface node to string with 'Enter' character.
        String methodImplementation = Node.writeToString(implementationNode, false);
        // String methodInterface = "<definitions/>";
        String methodInterface = Node.writeToString(interfaceNode, false);
        publisher.publishBusMethod(methodSet, method, methodImplementation, methodInterface,
                                   overwrite);
        Node.delete(implementationNode);
        Node.delete(interfaceNode);
    }

    /**
     * This method publishes the method set if it is new, and attaches it to the given SOAP node if
     * it is not already attached.
     *
     * @param   requestNode  DOCUMENTME
     * @param   methodName   DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void publishMethodSetAndAttachToSOAPNode(int requestNode, String methodName,
                                                       Document doc)
                                                throws SAPConnectorException
    {
        // System.out.println(methodName);
        // System.out.println(Node.writeToString(requestNode, true));
        methodSet = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                            "<" + methodName + "><methodSet>"), "");

        String organization = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                      "<" + methodName +
                                                                      "><organization>"), "");
        namespace = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                            "<" + methodName + "><namespace>"), "");

        // System.out.println("MS" +methodSet +"ORG"+ organization +"NS" + namespace);
        if (organization.equals("") || methodSet.equals("") || namespace.equals(""))
        {
            String requestString = Node.writeToString(requestNode, true);

            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        if (publisher == null)
        {
            publisher = new WSDLPublisher(organization, doc);
        }

        String isNew = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                               "<" + methodName + "><isNew>"), "");
        // System.out.println("isNew"+ isNew);
        boolean boolIsNew = new Boolean(isNew).booleanValue();

        // System.out.println("boolIsNew"+ boolIsNew);
        if (boolIsNew)
        {
            publisher.publishBusMethodSet(methodSet, namespace);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Method Set successfully published.");
            }
        }

        String attachToSOAPNode = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                          "<" + methodName +
                                                                          "><attachToSOAPNode>"),
                                                          "");
        boolean boolAttach = new Boolean(attachToSOAPNode).booleanValue();

        if (boolAttach)
        {
            String soapNodeDN = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                        "<" + methodName +
                                                                        "><SOAPNodeDN>"), "");

            if (soapNodeDN.equals(""))
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_COULD_NOT_FIND_SERVICE_GROUP_DN_IN_REQUEST);
            }
            publisher.attachToSOAPNode(soapNodeDN, methodSet, namespace);
        }
    }

    /**
     * Method to publish methods of type RFC. Method name will be the RFM name.
     *
     * @param   requestNode  DOCUMENTME
     * @param   doc          DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected void publishRFC(int requestNode, Document doc)
                       throws SAPConnectorException
    {
        String rfmName = Node.getDataWithDefault(Find.firstMatch(requestNode, "<PublishRFC><RFM>"),
                                                 "");

        if (rfmName.equals(""))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_INVALID_REQUEST_PARAMETERS);
        }

        String overwrite = Node.getDataWithDefault(Find.firstMatch(requestNode,
                                                                   "<PublishRFC><overwrite>"), "");
        boolean boolOverwrite = new Boolean(overwrite).booleanValue();
        String methodName = rfmName;
        int implementationNode = generateImplementation("SAPRFC", doc);
        int interfaceNode = generateInterface(methodName, rfmName, doc, false, null);
        publishMethod(methodName, implementationNode, interfaceNode, boolOverwrite);
    }

    /**
     * This method generates a single WSDL and the realizations for each RFC/BAPI that is requested.
     *
     * @param   request   The request for generation.
     * @param   response  The response to hold the WSDL and realizations.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void generateMethods(BodyBlock request, BodyBlock response)
                          throws SAPConnectorException
    {
        MethodGenerator mg = createMethodGenerator(request, response);
        mg.execute();
    }
}
