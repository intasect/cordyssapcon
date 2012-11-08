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
package com.eibus.applicationconnector.sap.connection.jco;

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.metadata.ESAPDataType;
import com.eibus.applicationconnector.sap.metadata.MethodGenerator;
import com.eibus.applicationconnector.sap.util.SpecialCharactersConvertorUtil;
import com.eibus.applicationconnector.sap.xsd.XSDAnnotation;
import com.eibus.applicationconnector.sap.xsd.XSDDumper;
import com.eibus.applicationconnector.sap.xsd.XSDElement;
import com.eibus.applicationconnector.sap.xsd.XSDRestriction;
import com.eibus.applicationconnector.sap.xsd.XSDSchema;
import com.eibus.applicationconnector.sap.xsd.XSDSequence;
import com.eibus.applicationconnector.sap.xsd.XSDType;

import com.eibus.soap.BodyBlock;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.IDoc.Repository;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

import java.util.ArrayList;

import javax.xml.XMLConstants;

import javax.xml.namespace.QName;

/**
 * This class can build up the methods based on the Java Connector.
 *
 * @author  pgussow
 */
public class JCoMethodGenerator extends MethodGenerator
{
    /**
     * Holds the name of the tag 'item'.
     */
    private static final String TAG_ITEM = "item";
    /**
     * Holds the IDOC repository.
     */
    private Repository m_idocRepository;
    /**
     * Holds the RFC repository.
     */
    private IRepository m_repository;

    /**
     * Creates a new JCoMethodGenerator object.
     *
     * @param   request         The actual request.
     * @param   response        The response.
     * @param   repository
     * @param   idocRepository
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public JCoMethodGenerator(BodyBlock request, BodyBlock response, IRepository repository,
                              Repository idocRepository)
                       throws SAPConnectorException
    {
        super(request.getXMLNode(), response.getXMLNode());

        m_repository = repository;
        m_idocRepository = idocRepository;
    }
    
    /**
     * Creates a new JCoMethodGenerator object.
     *
     * @param   request         The actual request xml node.
     * @param   response        The response xml node.
     * @param   repository
     * @param   idocRepository
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public JCoMethodGenerator(int request, int response, IRepository repository,
    		Repository idocRepository)
	     throws SAPConnectorException
	{
		super(request, response);
		
		m_repository = repository;
		m_idocRepository = idocRepository;
	}

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.MethodGenerator#createOperationSchemaForIDOC(com.eibus.applicationconnector.sap.xsd.XSDSchema,
     *       java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override public void createOperationSchemaForIDOC(XSDSchema schema, String idocType,
                                                       String cimType, String requestTag,
                                                       String responseTag)
                                                throws SAPConnectorException
    {
        XSDSequence sequence = createCommonPartOfIDOCInputElement(schema, requestTag);

        // Adding segments
        IDoc.Document idoc = JCoIDoc.createDocument(m_idocRepository, idocType, cimType);
        IDoc.Segment rootSegment = idoc.getRootSegment();
        IDoc.SegmentMetaData rootSegmentMetadata = rootSegment.getSegmentMetaData();

        // Describe the segment.
        handleSegment(sequence, rootSegmentMetadata);
//        XSDDumper xdDumper = new XSDDumper();
//        xdDumper.declareNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        xdDumper.declareNamespace("tns", "http://test");  
        // Create the output schema.
        createOutputSchemaElementforIDOC(schema, responseTag);
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.MethodGenerator#createOperationSchemaForRFC(com.eibus.applicationconnector.sap.xsd.XSDSchema,
     *       java.lang.String, java.lang.String, java.lang.String)
     */
    @Override public void createOperationSchemaForRFC(XSDSchema schema, String sapItemName,
                                                      String requestTag, String responseTag)
                                               throws SAPConnectorException
    {
        // Get the metadata from SAP
        JCO.Function function = getFunctionObject(sapItemName);
        JCO.ParameterList importParameterList = function.getImportParameterList();
        JCO.ParameterList tableParameterList = function.getTableParameterList();

        // Create the schema elements for the request.
        createInputOrOutputElementForRFC(schema, requestTag, importParameterList,
                                         tableParameterList);

        // Create the schema elements for the response.
        JCO.ParameterList exportParameterList = function.getExportParameterList();
        createInputOrOutputElementForRFC(schema, responseTag, exportParameterList,
                                         tableParameterList);
    }

    /**
     * This method generates the output schema element for the IDOC methods. <tid>...</tid> </IDOCNum>
     *
     * @param  schema         The XSD schema.
     * @param  outputElement  The name of the output element.
     */
    protected void createOutputSchemaElementforIDOC(XSDSchema schema, String outputElement)
    {
        // Create the main element
        XSDElement element = new XSDElement();
        element.setName(new QName(getNamespace(), outputElement));
        schema.addElement(element);

        XSDType type = new XSDType();
        type.setType(XSDType.TYPE_COMPLEX);
        element.setType(type);

        XSDSequence sequence = new XSDSequence();
        type.setSequence(sequence);

        // Create the tid element
        XSDElement tidElement = new XSDElement();
        tidElement.setName(new QName(getNamespace(), "tid"));
        sequence.addElement(tidElement);

        XSDAnnotation annotation = new XSDAnnotation();
        annotation.setDocumentation("Transaction ID of the IDOC");
        tidElement.setAnnotation(annotation);

        type = new XSDType();
        type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int"));
        tidElement.setType(type);
        
     // Create the idocNumber element
        XSDElement idocNumberElement = new XSDElement();
        idocNumberElement.setName(new QName(getNamespace(), "IDOCNum"));
        sequence.addElement(idocNumberElement);

        XSDAnnotation idocAnnotation = new XSDAnnotation();
        idocAnnotation.setDocumentation("IDOC Number generated by SAP");
        idocNumberElement.setAnnotation(idocAnnotation);

        XSDType type_IdocNumber = new XSDType();
        type_IdocNumber.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "String"));
        idocNumberElement.setType(type_IdocNumber);
    }

    /**
     * This method is called two times Once to generate the schema element for input, with import
     * parameters and table parameters. Second to generate the schema element for output, with
     * export parameters and table parameters.
     *
     * @param  schema              The XSD schema to use.
     * @param  elementName         The name of the parent element to create.
     * @param  parameterList       The list of parameters.
     * @param  tableParameterList  The list of fields in the table.
     */
    private void createInputOrOutputElementForRFC(XSDSchema schema, String elementName,
                                                  JCO.ParameterList parameterList,
                                                  JCO.ParameterList tableParameterList)
    {
        // Create the tag for the request element
        XSDElement xdOutput = new XSDElement();
        xdOutput.setName(new QName(getNamespace(), elementName));
        schema.addElement(xdOutput);

        if ((tableParameterList != null) || (parameterList != null))
        {
            // Create the complex type for the request.
            XSDType xtComplex = new XSDType();
            xtComplex.setType(XSDType.TYPE_COMPLEX);

            XSDSequence xsAll = new XSDSequence();
            xsAll.setAll(true);
            xtComplex.setSequence(xsAll);
            xdOutput.setType(xtComplex);

            if (parameterList != null)
            {
                int noOfParams = parameterList.getNumFields();

                for (int i = 0; i < noOfParams; i++)
                {
                    JCO.Field field = parameterList.getField(i);

                    if (field.isStructure())
                    {
                        String referenceType = parameterList.getTabName(i);
                        referenceType = SpecialCharactersConvertorUtil.encodeToHexa(referenceType) ; //hex
                        handleStructureParameter(schema, xsAll, field, referenceType);
                    }
                    else
                    {
                        XSDElement fieldElement = handleScalarField(field, false);
                        xsAll.addElement(fieldElement);
                    }
                }
            }

            if (tableParameterList != null)
            {
                int noOfTableParams = tableParameterList.getFieldCount();

                for (int i = 0; i < noOfTableParams; i++)
                {
                    JCO.Field tableField = tableParameterList.getField(i);
                    String refType = tableParameterList.getTabName(i);
                    refType = SpecialCharactersConvertorUtil.encodeToHexa(refType) ; //hex
                    handleTableParameter(schema, xsAll, tableField, refType);
                }
            }
        }
        else
        {
            xdOutput.setTypeRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));
        }
    }

    /**
     * This method creates the schema definition for a structure or a row in a table. It creates a
     * complex node and appends to it the schema definitions of all the fields in the structure or
     * table.
     *
     * @param  schema        Holds the current schema.
     * @param  field         Holds the actual field.
     * @param  refereceType  The actual type.
     * @param  isStructure   Whether or not the field is a structure.
     */
    private void generateComplexType(XSDSchema schema, JCO.Field field, String refereceType,
                                     boolean isStructure)
    {
        JCO.Record recordField;
        JCO.Field oneField;

        // To avoid repetition of complex types
        if (!schemaTypeExists(schema, refereceType))
        {
            XSDType mainType = new XSDType();
            mainType.setType(XSDType.TYPE_COMPLEX);
            mainType.setName(new QName(getNamespace(), refereceType));
            schema.addType(mainType);

            // Add the sequence
            XSDSequence xsSequence = new XSDSequence();
            mainType.setSequence(xsSequence);

            // Get the proper metadata
            if (isStructure)
            {
                recordField = field.getStructure();
            }
            else
            {
                recordField = field.getTable();
            }

            int noOfFields = recordField.getNumFields();

            for (int i = 0; i < noOfFields; i++)
            {
                oneField = recordField.getField(i);

                if (oneField.isStructure())
                {
                    String referenceType = recordField.getTabName(i);
                    handleStructureParameter(schema, xsSequence, oneField, referenceType);
                }
                else if (oneField.isTable())
                {
                    String refType = recordField.getTabName(i);
                    handleTableParameter(schema, xsSequence, oneField, refType);
                }
                else
                {
                    XSDElement fieldElement = handleScalarField(oneField, false);
                    xsSequence.addElement(fieldElement);
                }
            }
        }
    }

    /**
     * This method is to avoid a bug in the JCoIDoc API. The fromXML method called on a segments is
     * filling only the first 8 characters in a DATE field. i.e It works only if the date format is
     * YYYYMMDD but not YYYY-MM-DD. This method is called only for genetating the schema of fields
     * in IDOC segments. It just takes care of the DATE field. Its data type is a string of length
     * 8.
     *
     * @param  element         The XSD element.
     * @param  dicitonaryType  The dictionary type.
     * @param  fieldLength     The max length of the field.
     * @param  decimalLength   The decimal length.
     */
    private void generateSimpleDataTypeNodeForFieldsInIDOCSegments(XSDElement element,
                                                                   String dicitonaryType,
                                                                   int fieldLength,
                                                                   int decimalLength)
    {
        if (dicitonaryType.equals("DATS")) // TYPE IS DATE
        {
            XSDType type = new XSDType();
            element.setType(type);
            type.setBaseRef(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XSD_STRING));

            XSDRestriction restriction = new XSDRestriction();
            type.setRestriction(restriction);
            restriction.setMaxLength("8");
        }
        else // TYPE is not DATE
        {
            ESAPDataType sdt = ESAPDataType.mapIDOCType(dicitonaryType);
            generateSimpleDataType(element, sdt, fieldLength, decimalLength);
        }
    }

    /**
     * This method gives the JCO.Function object for the given RFM.
     *
     * @param   functionName  The name of the function to retrieve.
     *
     * @return  The function details.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private JCO.Function getFunctionObject(String functionName)
                                    throws SAPConnectorException
    {
        try
        {
            IFunctionTemplate functionTemplate = m_repository.getFunctionTemplate(functionName);

            if (functionTemplate == null)
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.RFM_NAME_NOT_FOUND_FOR_FUNCTION,
                                                functionName);
            }
            return functionTemplate.getFunction();
        }
        catch (JCO.Exception jce)
        {
            throw new SAPConnectorException(jce,
                                            SAPConnectorExceptionMessages.ERROR_EXECUTING_FUNCTION_CALL,
                                            jce.getLocalizedMessage());
        }
    }

    /**
     * This method creates the schema definition for a scalar field based on its type.
     *
     * @param   field               The definition of the field.
     * @param   inTableORStructure  Whether the field is in the table or the structure.
     *
     * @return  The element that was created.
     */
    private XSDElement handleScalarField(JCO.Field field, boolean inTableORStructure)
    {
        XSDElement returnValue = new XSDElement();

        // Set the name of the element and add optional documentation
        returnValue.setName(new QName(getNamespace(), field.getName()));

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            XSDAnnotation annotation = new XSDAnnotation();
            annotation.setDocumentation(fieldDescription);
            returnValue.setAnnotation(annotation);
        }

        // Set the minoccurs.
        if (inTableORStructure || field.isOptional())
        {
            returnValue.setMinOccurs(0);
        }

        // Map the type and generate proper XSd structure.
        ESAPDataType sdt = ESAPDataType.mapJCOType(field.getType());
        generateSimpleDataType(returnValue, sdt, field.getLength(), field.getDecimals());

        return returnValue;
    }

    /**
     * This method generates the schema element for a given segment. First this method is called for
     * the root segment, which has no siblings. Scheme element for fields in a segment are generated
     * based on the data type of a field. For each segment, all its child segment elements are
     * generated recursively.
     *
     * @param  parentSequence   parentNode DOCUMENTME
     * @param  segmentMetadata  DOCUMENTME
     */
    private void handleSegment(XSDSequence parentSequence, IDoc.SegmentMetaData segmentMetadata)
    {
    	 int noOfChildSegments = segmentMetadata.getNumChildren();

         // For each fhild segment
         for (int i = 0; i < noOfChildSegments; i++)
         {
             // Get the metadata for the current segment.
             IDoc.SegmentMetaData childSegmentMetadata = segmentMetadata.getChild(i);
             String segmentType = childSegmentMetadata.getType();
             String segmentDescription = childSegmentMetadata.getDescription();
             long minOccurs = childSegmentMetadata.getMinOccurrence();
             long maxOccurs = childSegmentMetadata.getMaxOccurrence();

             // Create the actual element.
             XSDElement mainElement = new XSDElement();
             mainElement.setName(new QName(getNamespace(), segmentType));
             mainElement.setMinOccurs((int) minOccurs);
             mainElement.setMaxOccurs((int) maxOccurs);

             parentSequence.addElement(mainElement);

             if (segmentDescription != null)
             {
                 XSDAnnotation annotation = new XSDAnnotation();
                 annotation.setDocumentation(segmentDescription);
                 mainElement.setAnnotation(annotation);
             }

             // Create the type for the segment.
             XSDType nestedType = new XSDType();
             nestedType.setType(XSDType.TYPE_COMPLEX);
             mainElement.setType(nestedType);
             XSDSequence sequence = new XSDSequence();
             nestedType.setSequence(sequence);

             // Add the segment attribute.
             createAttributeNode(nestedType, "SEGMENT");

             // To handle the fields in a segment.
             IDoc.RecordMetaData recordMetada = childSegmentMetadata.getRecordMetaData();
             int noOfFields = recordMetada.getNumFields();

             for (int j = 0; j < noOfFields; j++)
             {
                 // Get the field metadata.
                 IDoc.FieldMetaData fieldMetadata = recordMetada.getFieldMetaData(j);
                 String fieldType = fieldMetadata.getDataTypeName();
                 String fieldName = fieldMetadata.getFieldName();
                 String fieldDescription = fieldMetadata.getDescription();

                 // Create the field element
                 XSDElement fieldElement = new XSDElement();
                 fieldElement.setName(new QName(getNamespace(), fieldName));

                 if (fieldDescription != null)
                 {
                     XSDAnnotation annotation = new XSDAnnotation();
                     annotation.setDocumentation(fieldDescription);
                     fieldElement.setAnnotation(annotation);
                 }

                 // Do the type for the field.
                 int fieldLength = 0;
                 int decimalLength = 0;

                 if (!(fieldType.equals("DATS") || fieldType.equals("TIMS") ||
                           fieldType.equals("INT2") || fieldType.equals("INT4") ||
                           fieldType.equals("INT1") || fieldType.equals("FLTP")))
                 {
                     fieldLength = fieldMetadata.getOutputLength();

                     if (fieldType.equals("QUAN") || fieldType.equals("CURR") ||
                             fieldType.equals("DEC"))
                     {
                         decimalLength = recordMetada.getDecimals(j);
                     }
                 }
                 sequence.addElement(fieldElement) ;
                 generateSimpleDataTypeNodeForFieldsInIDOCSegments(fieldElement, fieldType,
                                                                   fieldLength, decimalLength);
             }

             // To handle its child segments.
             handleSegment(sequence, childSegmentMetadata);
         }
    }
  

    /**
     * This method generates the element for a structure and its corresponding complex type.
     *
     * @param  schema         The parent schema.
     * @param  sequence       The parent sequence.
     * @param  field          The actual field to describe.
     * @param  referenceType  The reference type.
     */
    private void handleStructureParameter(XSDSchema schema, XSDSequence sequence, JCO.Field field,
                                          String referenceType)
    {
        String structName = field.getName();
        structName = SpecialCharactersConvertorUtil.encodeToHexa(structName) ;
        // First of all we'll create the element. The element will point to a NEW complex type
        // which will be added to the schema as well.
        XSDElement element = new XSDElement();
        element.setName(new QName(getNamespace(), structName));
        element.setTypeRef(new QName(getNamespace(), referenceType));

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            XSDAnnotation annotation = new XSDAnnotation();
            annotation.setDocumentation(fieldDescription);
            element.setAnnotation(annotation);
        }

        sequence.addElement(element);

        // Now we need to figure out if there is already a complex type
        generateComplexType(schema, field, referenceType, true);
    }

    /**
     * This method generates the element for a table and its corresponding complex type for a row in
     * the table.
     *
     * @param  schema         The actual schema.
     * @param  sequence       The parent sequence.
     * @param  field          The field to describe.
     * @param  referenceType  The reference type for the field.
     */
    private void handleTableParameter(XSDSchema schema, XSDSequence sequence, JCO.Field field,
                                      String referenceType)
    {
        String tableName = field.getName();
        tableName = SpecialCharactersConvertorUtil.encodeToHexa(tableName) ;
        // Create the element and add it to the sequence
        XSDElement element = new XSDElement();
        element.setName(new QName(getNamespace(), tableName));
        sequence.addElement(element);

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            XSDAnnotation annotation = new XSDAnnotation();
            annotation.setDocumentation(fieldDescription);
            element.setAnnotation(annotation);
        }

        // Create the nested type.
        XSDType nestedType = new XSDType();
        nestedType.setType(XSDType.TYPE_COMPLEX);
        element.setType(nestedType);

        XSDSequence nestedSequence = new XSDSequence();
        nestedType.setSequence(nestedSequence);

        // Create the nested element
        XSDElement nestedElement = new XSDElement();
        nestedElement.setName(new QName(getNamespace(), TAG_ITEM));
        nestedElement.setTypeRef(new QName(getNamespace(), referenceType));
        nestedElement.setMinOccurs(0);
        nestedElement.setMaxOccurs(XSDElement.OCCURS_UNBOUNDED);
        nestedSequence.addElement(nestedElement);

        // Generate the complex type for the structure.
        generateComplexType(schema, field, referenceType, false);
    }

    /**
     * This method returns whether or not the given type already exists in the schema.
     *
     * @param   schema        The current schema.
     * @param   refereceType  The type to check.
     *
     * @return  Whether or not the type exists.
     */
    private boolean schemaTypeExists(XSDSchema schema, String refereceType)
    {
        boolean returnValue = false;

        ArrayList<XSDType> types = schema.getTypes();

        for (XSDType xsdType : types)
        {
            if (refereceType.equals(xsdType.getName().getLocalPart()))
            {
                returnValue = true;
                break;
            }
        }

        return returnValue;
    }
    
    private void serializeIDOCMetadata(IDoc.Document idoc)
    {
        //IDoc.Document idoc = JCoIDoc.createDocument(m_idocRepository, idocType, cimType);
        IDoc.Segment rootSegment = idoc.getRootSegment();
    }
}
