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

import com.cordys.coe.util.xml.dom.XMLHelper;
import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.Util;
import com.eibus.applicationconnector.sap.wsdl.WSDLCreator;
import com.eibus.applicationconnector.sap.xsd.XSDAttribute;
import com.eibus.applicationconnector.sap.xsd.XSDDumper;
import com.eibus.applicationconnector.sap.xsd.XSDElement;
import com.eibus.applicationconnector.sap.xsd.XSDParser;
import com.eibus.applicationconnector.sap.xsd.XSDRestriction;
import com.eibus.applicationconnector.sap.xsd.XSDSchema;
import com.eibus.applicationconnector.sap.xsd.XSDSequence;
import com.eibus.applicationconnector.sap.xsd.XSDType;

import com.eibus.soap.BodyBlock;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * This class will generate the method content new style. Based on the requested methods it will
 * generate a WSDL i.c.w. a set of realizations that can be used to generate CWS based content.
 *
 * @author  pgussow
 */
public abstract class MethodGenerator
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(MethodGenerator.class);
    /**
     * Holds the name of the tag 'type'.
     */
    private static final String TAG_TYPE = "type";
    /**
     * Holds the name of the tag 'businessobject_mestype'.
     */
    private static final String TAG_BUSINESSOBJECT_MESTYPE = "businessobject_mestype";
    /**
     * Holds the name of the attribute 'cimtype'.
     */
    private static final String ATTR_CIM_TYPE = "cimtype";
    /**
     * Holds the name of the tag 'operations'.
     */
    private static final String TAG_OPERATIONS = "operations";
    /**
     * Holds the name of the tag 'operation'.
     */
    private static final String TAG_OPERATION = "operation";
    /**
     * Holds the name of the tag 'namespace'.
     */
    private static final String TAG_NAMESPACE = "namespace";
    /**
     * Holds the name of the tag 'interfacename'.
     */
    private static final String TAG_INTERFACE_NAME = "interfacename";
    /**
     * Holds the XSD definition of a base64Binary.
     */
    protected static final String XSD_BASE64_BINARY = "base64Binary";
    /**
     * Holds the XSD definition of a double.
     */
    protected static final String XSD_DOUBLE = "double";
    /**
     * Holds the XSD definition of a int.
     */
    protected static final String XSD_INT = "int";
    /**
     * Holds the XSD definition of a decimal.
     */
    protected static final String XSD_DECIMAL = "decimal";
    /**
     * Holds the XSD definition of a string.
     */
    protected static final String XSD_STRING = "string";
    /**
     * Holds the name of the business object (in case of.
     */
    private String m_businessObjectMesType;
    /**
     * Holds the name of the interface that will be generated.
     */
    private String m_interfaceName;
    /**
     * Holds the namespace for the new method.
     */
    private String m_namespace;
    /**
     * Holds all operations to generate.
     */
    private List<OperationDetail> m_operations = new ArrayList<OperationDetail>();
    /**
     * Holds the response for the request.
     */
    private int m_response;
    /**
     * Holds the type of request.
     */
    private ESAPObjectType m_type;
    /**
     * Holds the namespace/prefix mappings.
     */
    private XPathMetaInfo m_xmi = new XPathMetaInfo();

    /**
     * Creates a new MethodGenerator object.
     *
     * @param   request   The actual request.
     * @param   response  The response.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public MethodGenerator(BodyBlock request, BodyBlock response)
                    throws SAPConnectorException
    {
        m_response = response.getXMLNode();

        // Use the namespace of the request
        m_xmi.addNamespaceBinding("ns", Node.getNamespaceURI(request.getXMLNode()));

        // Parse and validate the request.
        int xmlRequest = request.getXMLNode();

        // Parse the type
        String type = XPathHelper.getStringValue(xmlRequest, "ns:" + TAG_TYPE, m_xmi, "");

        if (!Util.isSet(type))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_REQUEST,
                                            TAG_TYPE);
        }

        try
        {
            m_type = ESAPObjectType.valueOf(type.toUpperCase());
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.THE_GIVEN_TYPE_IN_INVALID,
                                            type);
        }

        m_namespace = XPathHelper.getStringValue(xmlRequest, "ns:" + TAG_NAMESPACE, m_xmi, "");

        if (!Util.isSet(type))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_REQUEST,
                                            TAG_NAMESPACE);
        }

        m_interfaceName = XPathHelper.getStringValue(xmlRequest, "ns:" + TAG_INTERFACE_NAME, m_xmi,
                                                     "");

        if (!Util.isSet(type))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_REQUEST,
                                            TAG_INTERFACE_NAME);
        }

        // Get the businessObject name
        m_businessObjectMesType = XPathHelper.getStringValue(xmlRequest,
                                                             "ns:" + TAG_BUSINESSOBJECT_MESTYPE,
                                                             m_xmi, "");

        if ((m_type != ESAPObjectType.RFC) && !Util.isSet(type))
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_REQUEST,
                                            TAG_BUSINESSOBJECT_MESTYPE);
        }

        // Now get all operations to create.
        int[] operations = XPathHelper.selectNodes(xmlRequest,
                                                   "ns:" + TAG_OPERATIONS + "/ns:" + TAG_OPERATION,
                                                   m_xmi);

        if (operations.length == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_FIND_TAG_IN_REQUEST,
                                            TAG_OPERATIONS + "/" + TAG_OPERATION);
        }

        for (int xmlOperation : operations)
        {
            String sapName = Node.getDataWithDefault(xmlOperation, "");
            String webserviceName = Node.getAttribute(xmlOperation, "servicename", "");
            String cimType = Node.getAttribute(xmlOperation, ATTR_CIM_TYPE, null);

            if (!Util.isSet(webserviceName))
            {
                webserviceName = fixMethodName(sapName);
            }

            if (Util.isSet(sapName))
            {
                OperationDetail operationDetail = new OperationDetail(sapName, webserviceName);

                if (cimType != null)
                {
                    operationDetail.setCIMType(cimType);
                }

                m_operations.add(operationDetail);
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Going to generate WSDL based on type " + m_type + ". Business Object: " +
                      m_businessObjectMesType + ".\nOperations: " + m_operations);
        }
    }

    /**
     * This method creates the request and response tags schema based on the definition.
     *
     * @param   schema       The current schema.
     * @param   sapItemName  The name of the SAP item.
     * @param   extension    The extension.
     * @param   requestTag   The name of the request tag.
     * @param   responseTag  The name of the response tag.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public abstract void createOperationSchemaForIDOC(XSDSchema schema, String sapItemName,
                                                      String extension, String requestTag,
                                                      String responseTag)
                                               throws SAPConnectorException;

    /**
     * This method creates the request and response tags schema based on the definition.
     *
     * @param   schema       The current schema.
     * @param   sapItemName  The name of the SAP item.
     * @param   requestTag   The name of the request tag.
     * @param   responseTag  The name of the response tag.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public abstract void createOperationSchemaForRFC(XSDSchema schema, String sapItemName,
                                                     String requestTag, String responseTag)
                                              throws SAPConnectorException;

    /**
     * This method handles the actual generation.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public void execute()
                 throws SAPConnectorException
    {
        // First we'll create the response tags
        int xmlWSDL = Node.createElementWithParentNS("wsdl", null, m_response);
        int xmlRealizations = Node.createElementWithParentNS("realizations", null, m_response);

        // Create the base WSDL creator.
        WSDLCreator wsdl = new WSDLCreator(fixMethodName(m_interfaceName), m_namespace,
                                           "com.eibus.web.soap.Gateway.wcp");

        // Create the base schema for the methods
        XSDSchema xs = new XSDSchema();
        xs.setTargetNamespace(m_namespace);

        for (OperationDetail operation : m_operations)
        {
            // Initialize variables.
            String methodName = operation.getWebServiceName();
            String implType = "SAPRFC";

            int xmlRealization = Node.createElementWithParentNS("realization", null,
                                                                xmlRealizations);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Generating WSDL for operation " + methodName);
            }

            // Determine the name of the operation (method) and set proper variables.
            switch (m_type)
            {
                case BAPI:
                    implType = "SAPBAPI";

                case RFC:

                    // Generate the implementation
                    generateRFMBasedImplementation(implType, operation.getSAPName(),
                                                   xmlRealization);

                    // Add the interface to the method.
                    generateInterface(methodName, operation.getSAPName(), wsdl, xs, false, null);
                    break;

                case IDOC:
                    // Generate the implementation
                    generateIDOCImplementation(m_businessObjectMesType, operation.getCIMType(),
                                               xmlRealization);

                    // Add the interface to the method. The iDoc type is the operation
                    generateInterface(methodName, operation.getSAPName(), wsdl, xs, true,
                                      operation.getCIMType());
                    break;
            }

            // Attach the proper method name to the operation.
            Node.setAttribute(xmlRealization, "operation", methodName);
        }

        // Now we can add the WSDL to the response.
        XSDDumper xdDumper = new XSDDumper();
        xdDumper.declareNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        xdDumper.declareNamespace("tns", m_namespace);

        Element schema = xdDumper.convert(xs);
        schema.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:tns", m_namespace);

        // Make sure that the elements are qualified.
        schema.setAttribute("attributeFormDefault", "unqualified");
        schema.setAttribute("elementFormDefault", "qualified");

        // Attach the schema to the WSDL.
        wsdl.setSchema(schema);

        // Generate the WSDL.
        String finalWSDL = null;

        try
        {
            finalWSDL = wsdl.createWSDL();
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.ERROR_GENERATING_WSDL);
        }

        // Attach the WSDL to the response.
        try
        {
            int temp = Node.getDocument(xmlWSDL).parseString(finalWSDL);
            Node.appendToChildren(temp, xmlWSDL);
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.ERROR_PARSING_WSDL,
                                            finalWSDL);
        }
    }

    /**
     * This method gets the namespace for this method.
     *
     * @return  The namespace for this method.
     */
    public String getNamespace()
    {
        return m_namespace;
    }

    /**
     * This method creates an attribute node with the given name.
     *
     * @param  type           The parent XSD type.
     * @param  attributeName  The name of the attribute.
     */
    protected void createAttributeNode(XSDType type, String attributeName)
    {
        XSDAttribute attr = new XSDAttribute();
        attr.setName(new QName(m_namespace, attributeName));
        attr.setUse(XSDAttribute.USE_REQUIRED);

        XSDType attrType = new XSDType();
        attrType.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));

        XSDRestriction restriction = new XSDRestriction();
        type.setRestriction(restriction);
        restriction.addEnumeration("1");
    }

    /**
     * This method generates the common part of the schema that every IDOC has. And it returns the
     * sequence node in the common element with name IDOC.
     *
     * @param   schema        The containing schema.
     * @param   inputElement  The name of the input element.
     *
     * @return  The sequence for this element.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    protected XSDSequence createCommonPartOfIDOCInputElement(XSDSchema schema, String inputElement)
                                                      throws SAPConnectorException
    {
        XSDSequence returnValue = new XSDSequence();

        // Create the base element.
        XSDElement element = new XSDElement();
        element.setName(new QName(m_namespace, inputElement));
        schema.addElement(element);

        // Create the complex type with sequence.
        XSDType type = new XSDType();
        type.setType(XSDType.TYPE_COMPLEX);
        element.setType(type);

        XSDSequence mainSequence = new XSDSequence();
        type.setSequence(mainSequence);

        // Create the IDOC root element.
        XSDElement idocElement = new XSDElement();
        idocElement.setName(new QName(m_namespace, "IDOC"));
        mainSequence.addElement(idocElement);

        type = new XSDType();
        type.setType(XSDType.TYPE_COMPLEX);
        idocElement.setType(type);

        type.setSequence(returnValue);

        // Create the needed attributes
        createAttributeNode(type, "BEGIN");

        // We are adding control structure element EDI_DC40
        appendIDOCControlStructure(returnValue);

        return returnValue;
    }

    /**
     * This method generates the XSD simple type.
     *
     * @param  element        The parent element.
     * @param  dataType       The mapped data type.
     * @param  fieldLength    The length of the field.
     * @param  decimalLength  The decimal length.
     */
    protected void generateSimpleDataType(XSDElement element, ESAPDataType dataType,
                                          int fieldLength, int decimalLength)
    {
        switch (dataType)
        {
            case NUM:

                XSDType type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));

                XSDRestriction restriction = new XSDRestriction();
                type.setRestriction(restriction);
                restriction.setPattern("\\d+");
                restriction.setMaxLength(Integer.toString(fieldLength));

                break;

            case BCD:
                type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_DECIMAL));

                restriction = new XSDRestriction();
                type.setRestriction(restriction);

                restriction.setTotalDigits(Integer.toString(fieldLength));
                restriction.setFractionDigits(Integer.toString(decimalLength));

                break;

            case DATE:
                type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));

                restriction = new XSDRestriction();
                type.setRestriction(restriction);

                restriction.setPattern("....-..-..");

                break;

            case TIME:
                type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));

                restriction = new XSDRestriction();
                type.setRestriction(restriction);

                restriction.setPattern("\\d\\d-\\d\\d-\\d\\d");

                break;

            case INT:
                element.setTypeRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_INT));
                break;

            case UNSIGNED_BYTE:
                element.setTypeRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "unsignedByte"));
                break;

            case FLOAT:
                element.setTypeRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_DOUBLE));
                break;

            case BINARY:
                type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_BASE64_BINARY));

                restriction = new XSDRestriction();
                type.setRestriction(restriction);

                restriction.setLength(Integer.toString(fieldLength));

                break;

            case STRING:
                type = new XSDType();
                element.setType(type);
                type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));

                restriction = new XSDRestriction();
                type.setRestriction(restriction);

                restriction.setMaxLength(Integer.toString(fieldLength));
                break;

            default:
                element.setTypeRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));
                break;
        }
    }

    /**
     * This method fixes the method name in case any XML-illegal characters are in the name.
     * Examples:
     *
     * <ul>
     *   <li>IK_BEN_GEK: IkBenGek</li>
     *   <li>_BEN_GEK: BenGek</li>
     *   <li>IK_BEN_GEK_: IkBenGek</li>
     *   <li>IK_BEN___GEK: IkBenGek</li>
     *   <li>IK_BEN___GEK____OP_JOU______:IkBenGekOpJou</li>
     * </ul>
     *
     * @param   source  The original name.
     *
     * @return  The fixed name of the method.
     */
    private static String fixMethodName(String source)
    {
        //String returnValue = source.replaceAll("[^a-zA-Z.0-9_]", "").toLowerCase();
        String returnValue = source.replaceAll("[^a-zA-Z.0-9_]", "_");

        returnValue=returnValue.charAt(0)=='_'?returnValue.substring(1):returnValue;
        // Now we need to fix the case of the methods. This means that CUST_BLA_BLA will become
        // CustBlaBla
       /* StringBuilder sb = new StringBuilder(1024);

        for (int count = 0; count < returnValue.length(); count++)
        {
            if (returnValue.charAt(count) == '_')
            {
                if ((count + 1) < returnValue.length())
                {
                    // Skip multiple underscores
                    count++;

                    while ((count < returnValue.length()) && (returnValue.charAt(count) == '_'))
                    {
                        count++;
                    }

                    if (count < returnValue.length())
                    {
                        sb.append(("" + returnValue.charAt(count)).toUpperCase());
                    }
                }
            }
            else if (count == 0)
            {
                sb.append(("" + returnValue.charAt(count)).toUpperCase());
            }
            else
            {
                sb.append(("" + returnValue.charAt(count)).toLowerCase());
            }
        }

        returnValue = sb.toString();*/

        return returnValue;
    }

    /**
     * This method will append the EDI_DC40 XSD to the current sequence.
     *
     * @param   parentSequence  The parent sequence to append it to.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void appendIDOCControlStructure(XSDSequence parentSequence)
                                     throws SAPConnectorException
    {
        // Fix we'll parse the dummy schema.
        String schema = "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"" +
                        m_namespace + "\" xmlns:tns=\"" + m_namespace + "\" xmlns:xsd=\"" +
                        XMLConstants.W3C_XML_SCHEMA_NS_URI + "\" xmlns=\"" +
                        XMLConstants.W3C_XML_SCHEMA_NS_URI + "\">";

        String base = schema + "<element name=\"EDI_DC40\">" +
                      "<annotation><documentation>IDoc Control Record for Interface to External System</documentation></annotation>" +
                      "<complexType><sequence><element name=\"TABNAM\" type=\"xsd:string\" fixed=\"EDI_DC40\">" +
                      "<annotation><documentation>Name of table structure</documentation></annotation></element>" +
                      "<element name=\"MANDT\" minOccurs=\"0\"><annotation><documentation>Client</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"3\"/></restriction></simpleType></element>" +
                      "<element name=\"DOCNUM\" minOccurs=\"0\"><annotation><documentation>IDoc number</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"16\"/></restriction></simpleType></element>" +
                      "<element name=\"DOCREL\" minOccurs=\"0\"><annotation><documentation>SAP Release for IDoc</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"4\"/></restriction></simpleType></element>" +
                      "<element name=\"STATUS\" minOccurs=\"0\"><annotation><documentation>Status of IDoc</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"2\"/></restriction></simpleType></element>" +
                      "<element name=\"DIRECT\"><annotation><documentation>Direction</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><enumeration value=\"1\"><annotation><documentation>Outbound</documentation>" +
                      "</annotation></enumeration><enumeration value=\"2\"><annotation><documentation>Inbound</documentation></annotation>" +
                      "</enumeration></restriction></simpleType></element><element name=\"OUTMOD\" minOccurs=\"0\"><annotation>" +
                      "<documentation>Output mode</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"1\"/>" +
                      "</restriction></simpleType></element><element name=\"EXPRSS\" minOccurs=\"0\"><annotation><documentation>Overriding in " +
                      "inbound processing</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"1\"/></restriction>" +
                      "</simpleType></element><element name=\"TEST\" minOccurs=\"0\"><annotation><documentation>Test flag</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"1\"/></restriction></simpleType></element><element name=\"IDOCTYP\"" +
                      " type=\"xsd:string\" fixed=\"MATMAS02\"><annotation><documentation>Name of basic type</documentation></annotation></element>" +
                      "<element name=\"CIMTYP\" minOccurs=\"0\"><annotation><documentation>Extension (defined by customer)</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"30\"/></restriction></simpleType></element><element name=\"MESTYP\" minOccurs=\"0\">" +
                      "<annotation><documentation>Message type</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"30\"/>" +
                      "</restriction></simpleType></element><element name=\"MESCOD\" minOccurs=\"0\"><annotation><documentation>Message code</documentation>" +
                      "</annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"3\"/></restriction></simpleType></element>" +
                      "<element name=\"MESFCT\" minOccurs=\"0\"><annotation><documentation>Message function</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><maxLength value=\"3\"/></restriction></simpleType></element><element name=\"STD\" minOccurs=\"0\"><annotation>" +
                      "<documentation>EDI standard, flag</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"1\"/>" +
                      "</restriction></simpleType></element><element name=\"STDVRS\" minOccurs=\"0\"><annotation><documentation>EDI standard, version and release" +
                      "</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"6\"/></restriction></simpleType></element>" +
                      "<element name=\"STDMES\" minOccurs=\"0\"><annotation><documentation>EDI message type</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><maxLength value=\"6\"/></restriction></simpleType></element><element name=\"SNDPOR\"><annotation>" +
                      "<documentation>Sender port (SAP System, external subsystem)</documentation></annotation><simpleType><restriction base=\"xsd:string\">" +
                      "<maxLength value=\"10\"/></restriction></simpleType></element><element name=\"SNDPRT\"><annotation><documentation>Partner type of sender" +
                      "</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"2\"/></restriction></simpleType></element>" +
                      "<element name=\"SNDPFC\" minOccurs=\"0\"><annotation><documentation>Partner Function of Sender</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><maxLength value=\"2\"/></restriction></simpleType></element><element name=\"SNDPRN\"><annotation>" +
                      "<documentation>Partner number of sender</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"10\"/>" +
                      "</restriction></simpleType></element><element name=\"SNDSAD\" minOccurs=\"0\"><annotation><documentation>Sender address (SADR)</documentation>" +
                      "</annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"21\"/></restriction></simpleType></element><element " +
                      "name=\"SNDLAD\" minOccurs=\"0\"><annotation><documentation>Logical address of sender</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"RCVPOR\"><annotation>" +
                      "<documentation>Receiver port</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"10\"/></restriction>" +
                      "</simpleType></element><element name=\"RCVPRT\" minOccurs=\"0\"><annotation><documentation>Partner type of recipient</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"2\"/></restriction></simpleType></element><element name=\"RCVPFC\" minOccurs=\"0\">" +
                      "<annotation><documentation>Partner function of recipient</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"2\"/>" +
                      "</restriction></simpleType></element><element name=\"RCVPRN\"><annotation><documentation>Partner number of recipient</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"10\"/></restriction></simpleType></element><element name=\"RCVSAD\" minOccurs=\"0\"><annotation>" +
                      "<documentation>Recipient address (SADR)</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"21\"/></restriction>" +
                      "</simpleType></element><element name=\"RCVLAD\" minOccurs=\"0\"><annotation><documentation>Logical address of recipient</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"CREDAT\" minOccurs=\"0\">" +
                      "<annotation><documentation>Created on</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"8\"/></restriction>" +
                      "</simpleType></element><element name=\"CRETIM\" minOccurs=\"0\"><annotation><documentation>Time created</documentation></annotation><simpleType>" +
                      "<restriction base=\"xsd:string\"><maxLength value=\"6\"/></restriction></simpleType></element><element name=\"REFINT\" minOccurs=\"0\"><annotation>" +
                      "<documentation>Transmission file (EDI Interchange)</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"14\"/>" +
                      "</restriction></simpleType></element><element name=\"REFGRP\" minOccurs=\"0\"><annotation><documentation>Message group (EDI Message Group)</documentation>" +
                      "</annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"14\"/></restriction></simpleType></element><element name=\"REFMES\" minOccurs=\"0\">" +
                      "<annotation><documentation>Message (EDI Message)</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"14\"/></restriction>" +
                      "</simpleType></element><element name=\"ARCKEY\" minOccurs=\"0\"><annotation><documentation>Key for external message archive</documentation></annotation>" +
                      "<simpleType><restriction base=\"xsd:string\"><maxLength value=\"70\"/></restriction></simpleType></element><element name=\"SERIAL\" minOccurs=\"0\"><annotation>" +
                      "<documentation>Serialization</documentation></annotation><simpleType><restriction base=\"xsd:string\"><maxLength value=\"20\"/></restriction></simpleType>" +
                      "</element></sequence><attribute name=\"SEGMENT\" use=\"required\"><simpleType><restriction base=\"xsd:string\"><enumeration value=\"1\"/></restriction></simpleType>" +
                      "</attribute></complexType></element>";

        base += "</schema>";

        org.w3c.dom.Document doc = XMLHelper.createDocumentFromXML(base);

        XSDParser parser = new XSDParser();
        XSDSchema xsdSchema = parser.parseSchemaElement(doc.getDocumentElement(),
                                                        XSDParser.PARSER_MERGED);

        ArrayList<XSDElement> elems = xsdSchema.getElements();

        if (elems.size() > 0)
        {
            parentSequence.addElement(elems.get(0));
        }
        else
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.COULD_NOT_PARSE_IDOC_SCHEMA);
        }
    }

    /**
     * This method generates the methods based on the RFM used (can be either a BAPI of an RFC).
     *
     * @param  mesType  type The implementation type.
     * @param  cimType  rfmName THe name of the RFM.
     * @param  parent   The parent XML node.
     */
    private void generateIDOCImplementation(String mesType, String cimType, int parent)
    {
        // Create the implementation XML and set the type.
        int xmlImplementation = Node.createElementNS("implementation", null, null, null, parent);
        Node.setAttribute(xmlImplementation, "type", "SAPIDOC");

        // Add the FRM name to the implementation.
        Node.createElementWithParentNS("MESType", mesType, xmlImplementation);
        Node.createElementWithParentNS("CIMType", cimType, xmlImplementation);
        Node.createElementWithParentNS("AutoCommit", "true", xmlImplementation);
        Node.createElementWithParentNS("AutoRollback", "true", xmlImplementation);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Generated implementation for IDOC " + mesType + "(" + cimType + "):\n" +
                      Node.writeToString(xmlImplementation, false));
        }
    }

    /**
     * This method generates the new operation for the given method. It will detect whether or not
     * the basic skeleton is already in place. If so it will just add a new operation. Otherwise it
     * will first create the skeleton.
     *
     * @param   methodName   The name of the method (thus the operation.
     * @param   sapItemName  The name of the SAP object to wrap.
     * @param   wsdl         The current XML to the WSDL.
     * @param   schema       The schema definition for the web service.
     * @param   isIDOC       Whether or not the object is an IDOC.
     * @param   extension    Only used for IDOCs.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private void generateInterface(String methodName, String sapItemName, WSDLCreator wsdl,
                                   XSDSchema schema, boolean isIDOC, String extension)
                            throws SAPConnectorException
    {
        // Add the method name to the generator.
        wsdl.addMethod(methodName);

        // Now we need to generate the proper schema for the given object.
        if (isIDOC)
        {
            createOperationSchemaForIDOC(schema, sapItemName, extension, methodName,
                                         methodName + "Response");
        }
        else
        {
            createOperationSchemaForRFC(schema, sapItemName, methodName, methodName + "Response");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Generated interface for method " + methodName);
        }
    }

    /**
     * This method generates the methods based on the RFM used (can be either a BAPI of an RFC).
     *
     * @param  type     The implementation type.
     * @param  rfmName  THe name of the RFM.
     * @param  parent   The parent XML node.
     */
    private void generateRFMBasedImplementation(String type, String rfmName, int parent)
    {
        // Create the implementation XML and set the type.
        int xmlImplementation = Node.createElementNS("implementation", null, "", "", parent);
        Node.setAttribute(xmlImplementation, "type", type);

        // Add the FRM name to the implementation.
        Node.createElementWithParentNS("RFMName", rfmName, xmlImplementation);
        Node.createElementWithParentNS("AutoCommit", "true", xmlImplementation);
        Node.createElementWithParentNS("AutoRollback", "true", xmlImplementation);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Generated implementation for " + rfmName + "(" + type + "):\n" +
                      Node.writeToString(xmlImplementation, false));
        }
    }

    /**
     * This class holds the operation details.
     *
     * @author  pgussow
     */
    private class OperationDetail
    {
        /**
         * Holds the cim type for the operation (only used for IDOCs.
         */
        private String m_cimType;
        /**
         * Holds the SAP name for the object.
         */
        private String m_sapName;
        /**
         * Holds the web service name for this operation.
         */
        private String m_webserviceName;

        /**
         * Creates a new OperationDetail object.
         *
         * @param  sapName         The name of the SAP object
         * @param  webserviceName  Th ename for the web service.
         */
        OperationDetail(String sapName, String webserviceName)
        {
            m_sapName = sapName;
            m_webserviceName = webserviceName;
        }

        /**
         * This method gets the cim type for the operation (only used for IDOCs.
         *
         * @return  The cim type for the operation (only used for IDOCs.
         */
        public String getCIMType()
        {
            return m_cimType;
        }

        /**
         * This method gets the SAP name for the object.
         *
         * @return  The SAP name for the object.
         */
        public String getSAPName()
        {
            return m_sapName;
        }

        /**
         * This method gets the web service name for this operation.
         *
         * @return  The web service name for this operation.
         */
        public String getWebServiceName()
        {
            return m_webserviceName;
        }

        /**
         * This method sets the cim type for the operation (only used for IDOCs.
         *
         * @param  cimType  The cim type for the operation (only used for IDOCs.
         */
        public void setCIMType(String cimType)
        {
            m_cimType = cimType;
        }

        /**
         * @see  java.lang.Object#toString()
         */
        @Override public String toString()
        {
            String returnValue = "[WS:" + getWebServiceName() + ";SAP:" + getSAPName();

            if (m_cimType != null)
            {
                returnValue += ";CIMTYPE:" + m_cimType;
            }

            returnValue += "]";

            return returnValue;
        }
    }
}
