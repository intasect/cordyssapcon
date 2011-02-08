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

import com.eibus.applicationconnector.sap.xsd.XSDDumper;
import com.eibus.applicationconnector.sap.xsd.XSDElement;
import com.eibus.applicationconnector.sap.xsd.XSDRestriction;
import com.eibus.applicationconnector.sap.xsd.XSDSchema;
import com.eibus.applicationconnector.sap.xsd.XSDType;

import javax.xml.XMLConstants;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class TestXSDTypes
{
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
            element.setType(type);
            type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));

            XSDRestriction restriction = new XSDRestriction();
            type.setRestriction(restriction);
            restriction.setPattern("\\d+");
            restriction.setMaxLength("10");

            // Dump the schema.
            XSDDumper xdDumper = new XSDDumper();
            xdDumper.declareNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
            xdDumper.declareNamespace("tns", namespace);

            Element schema = xdDumper.convert(xs);
            schema.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:tns", namespace);

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
}
