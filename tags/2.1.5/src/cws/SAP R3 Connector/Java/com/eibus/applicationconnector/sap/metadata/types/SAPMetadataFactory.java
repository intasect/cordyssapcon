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
 package com.eibus.applicationconnector.sap.metadata.types;

import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.metadata.ESAPObjectType;

import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This factory can create objects of different types.
 *
 * @author  pgussow
 */
public class SAPMetadataFactory
{
    /**
     * This method creates a new type container.
     *
     * @param   type  The type to contain.
     *
     * @return  The created container.
     */
    public static ITypeContainer createContainer(ESAPObjectType type)
    {
        ITypeContainer returnValue = new TypeContainerImpl();

        returnValue.setType(type);

        return returnValue;
    }

    /**
     * This method will parse the container from XML. It automatically detects which type of objects
     * are in the container.
     *
     * @param   sapObject  The object to parse.
     * @param   xmi        The namespace/prefix mappings.
     *
     * @return  The container that was parsed.
     *
     * @throws  SAPConfigurationException  In case of any exceptions
     */
    public static ITypeContainer parseContainer(int sapObject, XPathMetaInfo xmi)
                                         throws SAPConfigurationException
    {
        return new TypeContainerImpl(sapObject, xmi);
    }

    /**
     * This method parses the item into the proper container. The XML structures that can be parsed
     * are described in the schema namespace WSDL.
     *
     * @param   type  The object type.
     * @param   item  The item XML.
     * @param   xmi   The namespace/prefix mappings.
     *
     * @return  The parsed data.
     *
     * @throws  SAPConfigurationException  In case of any exceptions
     */
    public static ITypeMetadata parseObject(ESAPObjectType type, int item, XPathMetaInfo xmi)
                                     throws SAPConfigurationException
    {
        switch (type)
        {
            case BAPI:
                return new BAPIMetadataImpl(item, xmi);

            case IDOC:
                return new IDOCMetadataImpl(item, xmi);

            case RFC:
                return new RFCMetadataImpl(item, xmi);
        }

        return null;
    }

    /**
     * This method parses the SAP information available for the BAPI.
     *
     * <p>The SAP structure looks like this:</p>
     *
     * <pre>
       <item>
            <OBJTYPE>ADDREMPAU</OBJTYPE>
            <METHOD>CHANGE</METHOD>
            <METHODNAME>Change</METHODNAME>
            <DESCRIPT>Change address</DESCRIPT>
            <SHORTTEXT>Change employee address</SHORTTEXT>
            <FUNCTION>BAPI_ADDREMPAU_CHANGE</FUNCTION>
            <CLASSVERB/>
            <APITYPE/>
            <OBJECTNAME>EmployeePrivateAdrAU</OBJECTNAME>
       </item>
     * </pre>
     *
     * @param   node  The SAP item node.
     *
     * @return  The parsed object.
     *
     * @throws  SAPConfigurationException  In case of any object validation errors.
     */
    public static IBAPIMetadata parseSAPBAPIInformation(int node)
                                                 throws SAPConfigurationException
    {
        IBAPIMetadata returnValue = new BAPIMetadataImpl();

        returnValue.parseSAPItem(node);

        return returnValue;
    }

    /**
     * This method parses the SAP information available for the RFC.
     *
     * <p>The SAP structure looks like this:</p>
     *
     * <pre>
       <item>
            <FUNCNAME>/BI0/QI2LIS_02_SCL</FUNCNAME>
            <GROUPNAME>/BI0/QI2LIS_02_SCL</GROUPNAME>
            <APPL/>
            <HOST/>
            <STEXT/>
       </item>
     * </pre>
     *
     * @param   node  The SAP item node.
     *
     * @return  The parsed object.
     *
     * @throws  SAPConfigurationException  In case of any object validation errors.
     */
    public static IIDOCMetadata parseSAPIDOCInformation(int node)
                                                 throws SAPConfigurationException
    {
        IIDOCMetadata returnValue = new IDOCMetadataImpl();

        returnValue.parseSAPItem(node);

        return returnValue;
    }

    /**
     * This method parses the SAP information available for the RFC.
     *
     * <p>The SAP structure looks like this:</p>
     *
     * <pre>
       <item>
            <FUNCNAME>/BI0/QI2LIS_02_SCL</FUNCNAME>
            <GROUPNAME>/BI0/QI2LIS_02_SCL</GROUPNAME>
            <APPL/>
            <HOST/>
            <STEXT/>
       </item>
     * </pre>
     *
     * @param   node  The SAP item node.
     *
     * @return  The parsed object.
     *
     * @throws  SAPConfigurationException  In case of any object validation errors.
     */
    public static IRFCMetadata parseSAPRFCInformation(int node)
                                               throws SAPConfigurationException
    {
        IRFCMetadata returnValue = new RFCMetadataImpl();

        returnValue.parseSAPItem(node);

        return returnValue;
    }
}
