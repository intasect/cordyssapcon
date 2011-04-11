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

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.applicationconnector.sap.exception.SAPConfigurationException;
import com.eibus.applicationconnector.sap.exception.SAPConfigurationExceptionMessages;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * This class implements the BAPI metadata wrapper.
 *
 * @author  pgussow
 */
class BAPIMetadataImpl extends AbstractTypeMetadata
    implements IBAPIMetadata
{
    /**
     * Holds the name of the SAP tag 'METHOD'.
     */
    private static final String TAG_SAP_METHOD = "METHOD";
    /**
     * Holds the name of the SAP tag 'METHODNAME'.
     */
    private static final String TAG_SAP_METHOD_NAME = "METHODNAME";
    /**
     * Holds the name of the SAP tag 'FUNCTION'.
     */
    private static final String TAG_SAP_FUNCTION = "FUNCTION";
    /**
     * Holds the name of the SAP tag 'DESCRIPT'.
     */
    private static final String TAG_SAP_DESCRIPTION = "DESCRIPT";
    /**
     * Holds the name of the SAP tag 'SHORTTEXT'.
     */
    private static final String TAG_SAP_SHORT_TEXT = "SHORTTEXT";
    /**
     * Holds the name of the SAP tag 'CLASSVERB'.
     */
    private static final String TAG_SAP_CLASS_VERB = "CLASSVERB";
    /**
     * Holds the name of the SAP tag 'APITYPE'.
     */
    private static final String TAG_SAP_API_TYPE = "APITYPE";
    /**
     * The API type.
     */
    private String m_apiType;
    /**
     * The class verb.
     */
    private String m_classVerb;
    /**
     * The description of the method.
     */
    private String m_description;
    /**
     * The method.
     */
    private String m_method;
    /**
     * The name of the method.
     */
    private String m_methodName;
    /**
     * The actual RFC function.
     */
    private String m_rfcFunction;
    /**
     * The short descriptive text.
     */
    private String m_shortText;

    /**
     * Creates a new BAPIMetadataImpl object.
     */
    public BAPIMetadataImpl()
    {
    }

    /**
     * Creates a new BAPIMetadataImpl object.
     *
     * @param   node  The XML node to parse
     * @param   xmi   The namespace/prefix mappings.
     *
     * @throws  SAPConfigurationException  In case of any exceptions.
     */
    public BAPIMetadataImpl(int node, XPathMetaInfo xmi)
                     throws SAPConfigurationException
    {
        super(node, xmi);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getAPIType()
     */
    @Override public String getAPIType()
    {
        return m_apiType;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getClassVerb()
     */
    @Override public String getClassVerb()
    {
        return m_classVerb;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getDescription()
     */
    @Override public String getDescription()
    {
        return m_description;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getMethod()
     */
    @Override public String getMethod()
    {
        return m_method;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getMethodName()
     */
    @Override public String getMethodName()
    {
        return m_methodName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getRFCFuntion()
     */
    @Override public String getRFCFuntion()
    {
        return m_rfcFunction;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#getShortText()
     */
    @Override public String getShortText()
    {
        return m_shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata#parseSAPItem(int)
     */
    @Override public void parseSAPItem(int itemDetails)
                                throws SAPConfigurationException
    {
        if (itemDetails == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.MISSING_BAPI_DATA);
        }

        // Get the method ID
        m_method = XPathHelper.getStringValue(itemDetails, TAG_SAP_METHOD, "");
        setValue(m_method);

        // Get the method name.
        m_methodName = XPathHelper.getStringValue(itemDetails, TAG_SAP_METHOD_NAME, "");
        setDisplayName(m_methodName);

        // Get the RFC function.
        m_rfcFunction = XPathHelper.getStringValue(itemDetails, TAG_SAP_FUNCTION, "");

        // Get the optional fields.
        m_description = XPathHelper.getStringValue(itemDetails, TAG_SAP_DESCRIPTION, "");
        m_shortText = XPathHelper.getStringValue(itemDetails, TAG_SAP_SHORT_TEXT, "");
        m_apiType = XPathHelper.getStringValue(itemDetails, TAG_SAP_API_TYPE, "");
        m_classVerb = XPathHelper.getStringValue(itemDetails, TAG_SAP_CLASS_VERB, "");

        // Make sure the object is valid.
        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setAPIType(java.lang.String)
     */
    @Override public void setAPIType(String apiType)
    {
        m_apiType = apiType;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setClassVerb(java.lang.String)
     */
    @Override public void setClassVerb(String classVerb)
    {
        m_classVerb = classVerb;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setDescription(java.lang.String)
     */
    @Override public void setDescription(String description)
    {
        m_description = description;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setMethod(java.lang.String)
     */
    @Override public void setMethod(String methodid)
    {
        m_method = methodid;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setMethodName(java.lang.String)
     */
    @Override public void setMethodName(String methodName)
    {
        m_methodName = methodName;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setRFCFuntion(java.lang.String)
     */
    @Override public void setRFCFuntion(String rfcFuntion)
    {
        m_rfcFunction = rfcFuntion;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.IBAPIMetadata#setShortText(java.lang.String)
     */
    @Override public void setShortText(String shortText)
    {
        m_shortText = shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#validate()
     */
    @Override public void validate()
                            throws SAPConfigurationException
    {
        if (!Util.isSet(m_method))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IBAPIMetadata.TAG_ROOT + "/" + TAG_METHOD);
        }

        if (!Util.isSet(m_methodName))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IBAPIMetadata.TAG_ROOT + "/" + TAG_METHOD_NAME);
        }

        if (!Util.isSet(m_rfcFunction))
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IBAPIMetadata.TAG_ROOT + "/" + TAG_RFC_FUNCTION);
        }
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#fillClone(com.eibus.applicationconnector.sap.metadata.types.ITypeMetadata)
     */
    @Override protected void fillClone(ITypeMetadata clone)
    {
        BAPIMetadataImpl bmi = (BAPIMetadataImpl) clone;

        bmi.m_apiType = m_apiType;
        bmi.m_classVerb = m_classVerb;
        bmi.m_description = m_description;
        bmi.m_method = m_method;
        bmi.m_methodName = m_methodName;
        bmi.m_rfcFunction = m_rfcFunction;
        bmi.m_shortText = m_shortText;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#parseXML(int, com.eibus.xml.xpath.XPathMetaInfo)
     */
    @Override protected void parseXML(int detail, XPathMetaInfo xmi)
                               throws SAPConfigurationException
    {
        int bapi = XPathHelper.selectSingleNode(detail, "ns:" + IBAPIMetadata.TAG_ROOT, xmi);

        if (bapi == 0)
        {
            throw new SAPConfigurationException(SAPConfigurationExceptionMessages.ERR_COULD_NOT_FIND_TAG,
                                                IBAPIMetadata.TAG_ROOT);
        }

        // Get the method ID
        m_method = XPathHelper.getStringValue(bapi, "ns:" + TAG_METHOD, xmi, "");

        // Get the method name.
        m_methodName = XPathHelper.getStringValue(bapi, "ns:" + TAG_METHOD_NAME, xmi, "");

        // Get the RFC function.
        m_rfcFunction = XPathHelper.getStringValue(bapi, "ns:" + TAG_RFC_FUNCTION, xmi, "");

        // Get the optional fields.
        m_description = XPathHelper.getStringValue(bapi, "ns:" + TAG_DESCRIPTION, xmi, "");
        m_shortText = XPathHelper.getStringValue(bapi, "ns:" + TAG_SHORT_TEXT, xmi, "");
        m_apiType = XPathHelper.getStringValue(bapi, "ns:" + TAG_API_TYPE, xmi, "");
        m_classVerb = XPathHelper.getStringValue(bapi, "ns:" + TAG_CLASS_VERB, xmi, "");

        // Make sure the object is valid.
        validate();
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.types.AbstractTypeMetadata#toXMLDetail(int)
     */
    @Override protected void toXMLDetail(int detail)
    {
        int idoc = Node.createElementWithParentNS(IBAPIMetadata.TAG_ROOT, null, detail);

        if (Util.isSet(m_method))
        {
            Node.createElementWithParentNS(TAG_METHOD, m_method, idoc);
        }

        if (Util.isSet(m_methodName))
        {
            Node.createElementWithParentNS(TAG_METHOD_NAME, m_methodName, idoc);
        }

        if (Util.isSet(m_description))
        {
            Node.createElementWithParentNS(TAG_DESCRIPTION, m_description, idoc);
        }

        if (Util.isSet(m_shortText))
        {
            Node.createElementWithParentNS(TAG_SHORT_TEXT, m_shortText, idoc);
        }

        if (Util.isSet(m_rfcFunction))
        {
            Node.createElementWithParentNS(TAG_RFC_FUNCTION, m_rfcFunction, idoc);
        }

        if (Util.isSet(m_classVerb))
        {
            Node.createElementWithParentNS(TAG_CLASS_VERB, m_classVerb, idoc);
        }

        if (Util.isSet(m_apiType))
        {
            Node.createElementWithParentNS(TAG_API_TYPE, m_apiType, idoc);
        }
    }
}
