/**
 * Copyright 2009 Cordys R&D B.V. 
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
 package com.cordys.test;

import com.cordys.coe.util.xml.dom.NiceDOMWriter;
import com.cordys.coe.util.xml.dom.XMLHelper;

import com.eibus.applicationconnector.sap.xsd.XSDDumper;
import com.eibus.applicationconnector.sap.xsd.XSDElement;
import com.eibus.applicationconnector.sap.xsd.XSDParser;
import com.eibus.applicationconnector.sap.xsd.XSDSchema;
import com.eibus.applicationconnector.sap.xsd.XSDSequence;
import com.eibus.applicationconnector.sap.xsd.XSDType;

import java.util.ArrayList;

import javax.xml.XMLConstants;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class TestMergedSchema
{
    /**
     * DOCUMENTME.
     */
    private static String m_namespace = "http://cookie.com";

    /**
     * Main method.
     *
     * @param  saArguments  Commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            XSDSchema xs = new XSDSchema();
            String namespace = "http://test.com";
            xs.setTargetNamespace(namespace);

            XSDElement element = new XSDElement();
            element.setName(new QName(namespace, "test"));
            xs.addElement(element);

            XSDType type = new XSDType();
            type.setType(XSDType.TYPE_COMPLEX);
            element.setType(type);

            XSDSequence sequence = new XSDSequence();
            type.setSequence(sequence);

            appendElement(sequence);

            // Dump the schema.
            XSDDumper xdDumper = new XSDDumper();
            xdDumper.declareNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
            xdDumper.declareNamespace("tns", namespace);

            Element schema = xdDumper.convert(xs);
            schema.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:tns", namespace);
            schema.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Make sure that the elements are qualified.
            schema.setAttribute("attributeFormDefault", "unqualified");
            schema.setAttribute("elementFormDefault", "qualified");

            System.out.println(NiceDOMWriter.write(schema, 2, true, false, false));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method will append the EDI_DC40 XSD to the current sequence.
     *
     * @param  parentSequence  The parent sequence to append it to.
     */
    private static void appendElement(XSDSequence parentSequence)
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
    }
}
