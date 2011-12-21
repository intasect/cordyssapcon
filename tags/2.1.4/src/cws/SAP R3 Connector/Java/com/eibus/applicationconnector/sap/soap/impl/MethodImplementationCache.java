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
 package com.eibus.applicationconnector.sap.soap.impl;

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.SAPConnectorConstants;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;

import com.eibus.xml.xpath.XPathMetaInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This cache holds the parsed implementations for a method.
 *
 * @author  pgussow
 */
public class MethodImplementationCache
{
    /**
     * DOCUMENTME.
     */
    private static MethodImplementationCache s_cache = new MethodImplementationCache();
    /**
     * DOCUMENTME.
     */
    private Map<MethodDefinition, IMethodImplementation> m_implementations = new LinkedHashMap<MethodDefinition, IMethodImplementation>();

    /**
     * Creates a new MethodImplementationCache object.
     */
    private MethodImplementationCache()
    {
    }

    /**
     * DOCUMENTME.
     *
     * @param   bbRequest  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static IMethodImplementation getImplementation(BodyBlock bbRequest)
    {
        return null;
    }

    /**
     * This method gets the desired action from the method's implementation.
     *
     * @param   bbRequest  The request that was received.
     *
     * @return  The action in the implementation.
     *
     * @throws  SAPConnectorException  EmailIOException In case the action was not found.
     */
    private static String getActionFromImplementation(BodyBlock bbRequest)
                                               throws SAPConnectorException
    {
        String sAction;

        int iImplNode = bbRequest.getMethodDefinition().getImplementation();

        XPathMetaInfo xmi = new XPathMetaInfo();
        xmi.addNamespaceBinding("ns", SAPConnectorConstants.NS_SAP_IMPLEMENTATION);

        sAction = XPathHelper.getStringValue(iImplNode, "ns:sapconnector/ns:action/text()", xmi,
                                             "");

        if (sAction.length() == 0)
        {
            throw new SAPConnectorException(SAPConnectorExceptionMessages.NO_ACTION_FOUND_IN_THE_METHOD_IMPLEMENTATION);
        }

        return sAction;
    }
}
