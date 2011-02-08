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
import com.eibus.applicationconnector.sap.metadata.MethodGenerator;
import com.eibus.applicationconnector.sap.metadata.SAPMethodGenerator;

import com.eibus.soap.BodyBlock;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

/**
 * DOCUMENTME .
 *
 * @author  ygopal
 *
 *          <p>This class generates the WSDL for BAPI, RFC and IDOC methods specific to the
 *          middleware JCo.</p>
 */
public class SAPJCoMethodGenerator extends SAPMethodGenerator
{
    /**
     * DOCUMENTME.
     */
    private IDoc.Repository IDOCRepository;
    /**
     * DOCUMENTME.
     */
    private IRepository repository;

    /**
     * Constructor that takes JCo repository and IDOC repository as parameters. These are used to
     * get the metadata of RFCs and IDOCs respectively.
     *
     * @param  repository      DOCUMENTME
     * @param  IDOCRepository  DOCUMENTME
     */
    SAPJCoMethodGenerator(IRepository repository, IDoc.Repository IDOCRepository)
    {
        this.repository = repository;
        this.IDOCRepository = IDOCRepository;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.metadata.SAPMethodGenerator#createMethodGenerator(com.eibus.soap.BodyBlock,
     *       com.eibus.soap.BodyBlock)
     */
    @Override public MethodGenerator createMethodGenerator(BodyBlock request, BodyBlock response)
                                                    throws SAPConnectorException
    {
        return new JCoMethodGenerator(request, response, repository, IDOCRepository);
    }

    /**
     * Implemenataion for the abstract method in the super class. This method generates the input
     * and output schema elements for an IDOC.
     *
     * @param   schemaNode     DOCUMENTME
     * @param   idocType       DOCUMENTME
     * @param   cimType        DOCUMENTME
     * @param   inputElement   DOCUMENTME
     * @param   outputElement  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.SAPMethodGenerator#createInputOutputElementsForIDOC(int,
     *          java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     *          com.eibus.xml.nom.Document)
     */
    @Override protected void createInputOutputElementsForIDOC(int schemaNode, String idocType,
                                                              String cimType, String inputElement,
                                                              String outputElement,
                                                              Document doc)
                                                       throws SAPConnectorException
    {
        int sequenceNode = createCommonPartOfIDOCInputElement(schemaNode, inputElement, doc);
        
        // Adding segments
        IDoc.Document idoc = JCoIDoc.createDocument(IDOCRepository, idocType, cimType);
        IDoc.Segment rootSegment = idoc.getRootSegment();
        IDoc.SegmentMetaData rootSegmentMetadata = rootSegment.getSegmentMetaData();
        
        handleSegment(sequenceNode, rootSegmentMetadata, doc);
        
        createOutputSchemaElementforIDOC(schemaNode, outputElement, doc);
    }

    /**
     * Implemenataion for the abstract method in the super class. This method generates the input
     * and output schema elements for an RFC.
     *
     * @param   schemaNode     DOCUMENTME
     * @param   rfmName        DOCUMENTME
     * @param   inputElement   DOCUMENTME
     * @param   outputElement  DOCUMENTME
     * @param   doc            DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     *
     * @see     com.eibus.applicationconnector.sap.metadata.SAPMethodGenerator#createInputOutputElementsForRFC(int,
     *          java.lang.String, java.lang.String, java.lang.String, com.eibus.xml.nom.Document)
     */
    @Override protected void createInputOutputElementsForRFC(int schemaNode, String rfmName,
                                                             String inputElement,
                                                             String outputElement,
                                                             Document doc)
                                                      throws SAPConnectorException
    {
        JCO.Function function = getFunctionObject(rfmName);
        JCO.ParameterList importParameterList = function.getImportParameterList();
        JCO.ParameterList tableParameterList = function.getTableParameterList();
        createInputOrOutputElementForRFC(schemaNode, inputElement, importParameterList,
                                         tableParameterList, doc);

        JCO.ParameterList exportParameterList = function.getExportParameterList();
        createInputOrOutputElementForRFC(schemaNode, outputElement, exportParameterList,
                                         tableParameterList, doc);
    }

    /**
     * This method is called two times Once to generate the schema element for input, with import
     * parameters and table parameters. Second to generate the schema element for output, with
     * export parameters and table parameters.
     *
     * @param  schemaNode          DOCUMENTME
     * @param  elementName         DOCUMENTME
     * @param  parameterList       DOCUMENTME
     * @param  tableParameterList  DOCUMENTME
     * @param  doc                 DOCUMENTME
     */
    private void createInputOrOutputElementForRFC(int schemaNode, String elementName,
                                                  JCO.ParameterList parameterList,
                                                  JCO.ParameterList tableParameterList,
                                                  Document doc)
    {
        int elementNodeInTheSchema = Find.firstMatch(schemaNode, "<schema><element>");
        int elementNode = 0;

        // This is to ensure that element nodes come first in the schema and then the complex type
        // nodes
        if (elementNodeInTheSchema == 0)
        {
            elementNode = doc.createElement("element", schemaNode);
        }
        else
        {
            elementNode = doc.createElement("element");
            Node.add(elementNode, elementNodeInTheSchema);
        }
        Node.setAttribute(elementNode, "name", elementName);

        int complexTypeNode = doc.createElement("complexType", elementNode);
        int allNode = doc.createElement("all", complexTypeNode);

        if (parameterList != null)
        {
            int noOfParams = parameterList.getNumFields();

            for (int i = 0; i < noOfParams; i++)
            {
                JCO.Field field = parameterList.getField(i);

                if (field.isStructure())
                {
                    String referenceType = parameterList.getTabName(i);
                    handleStructureParameter(schemaNode, allNode, field, referenceType, doc);
                }
                else
                {
                    int fieldElement = handleScalarField(field, doc, false);
                    Node.appendToChildren(fieldElement, allNode);
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
                handleTableParameter(schemaNode, allNode, tableField, refType, doc);
            }
        }
    }

    /**
     * This method creates the schema definition for a structre or a row in a table. It creates a
     * complex node and appends to it the schema definitions of all the fiels in the structure or
     * table.
     *
     * @param  schemaNode    DOCUMENTME
     * @param  field         DOCUMENTME
     * @param  refereceType  DOCUMENTME
     * @param  doc           DOCUMENTME
     * @param  isStructure   DOCUMENTME
     */
    private void generateComplexType(int schemaNode, JCO.Field field, String refereceType,
                                     Document doc, boolean isStructure)
    {
        JCO.Record recordField;
        JCO.Field oneField;
        // To avoid repetition of complex types
        int sameComplexNodeInTheSchema = Find.firstMatch(schemaNode,
                                                         "<schema><complexType name=\"" +
                                                         refereceType + "\" >");

        if (sameComplexNodeInTheSchema != 0)
        {
            return;
        }

        int complexTypeNode = doc.createElement("complexType", schemaNode);
        Node.setAttribute(complexTypeNode, "name", refereceType);

        int sequenceNode = doc.createElement("sequence", complexTypeNode);

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
                handleStructureParameter(schemaNode, sequenceNode, oneField, referenceType, doc);
            }
            else if (oneField.isTable())
            {
                String refType = recordField.getTabName(i);
                handleTableParameter(schemaNode, sequenceNode, oneField, refType, doc);
            }
            else
            {
                int fieldElement = handleScalarField(oneField, doc, false);
                Node.appendToChildren(fieldElement, sequenceNode);
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
     * @param  elementNode     DOCUMENTME
     * @param  dicitonaryType  DOCUMENTME
     * @param  doc             DOCUMENTME
     * @param  fieldLength     DOCUMENTME
     * @param  decimalLength   DOCUMENTME
     */
    private void generateSimpleDataTypeNodeForFieldsInIDOCSegments(int elementNode,
                                                                   String dicitonaryType,
                                                                   Document doc, int fieldLength,
                                                                   int decimalLength)
    {
        if (dicitonaryType.equals("DATS")) // TYPE IS DATE
        {
            int simpleTypeNode = doc.createElement("simpleType", elementNode);
            int restrictionNode = doc.createElement("restriction", simpleTypeNode);
            Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");

            int maxLengthNode = doc.createElement("maxLength", restrictionNode);
            Node.setAttribute(maxLengthNode, "value", "8");
        }
        else // TYPE is not DATE
        {
            generateSimpleDataTypeNode(elementNode, dicitonaryType, doc, fieldLength,
                                       decimalLength);
        }
    }

    /**
     * This method gives the JCO.Function object for the given RFM.
     *
     * @param   functionName  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    private JCO.Function getFunctionObject(String functionName)
                                    throws SAPConnectorException
    {
        try
        {
            IFunctionTemplate functionTemplate = repository.getFunctionTemplate(functionName);

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
     * @param   field               DOCUMENTME
     * @param   doc                 DOCUMENTME
     * @param   inTableORStructure  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    private int handleScalarField(JCO.Field field, Document doc, boolean inTableORStructure)
    {
        int fieldLength;
        int simpleTypeNode = 0;
        int maxLengthNode = 0;
        int lengthNode = 0;
        int restrictionNode = 0;
        int patternNode = 0;
        int elementNode = doc.createElement("element");
        Node.setAttribute(elementNode, "name", field.getName());

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            int annotationNode = doc.createElement("annotation", elementNode);
            // "description" is WCP specific
            int descriptionNode = doc.createTextElement("documentation", fieldDescription,
                                                        annotationNode);
        }

        if (inTableORStructure || field.isOptional())
        {
            Node.setAttribute(elementNode, "minOccurs", "0");
        }

        int fieldType = field.getType();

        switch (fieldType)
        {
            case JCO.TYPE_NUM:
                fieldLength = field.getLength();
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
                patternNode = doc.createElement("pattern", restrictionNode);
                Node.setAttribute(patternNode, "value", "\\d+");
                maxLengthNode = doc.createElement("maxLength", restrictionNode);
                Node.setAttribute(maxLengthNode, "value", Integer.toString(fieldLength));
                break;

            case JCO.TYPE_BCD:
                fieldLength = field.getLength();

                int noOfDecimals = field.getDecimals();
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "decimal");

                int totalDigitsNode = doc.createElement("totalDigits", restrictionNode);
                Node.setAttribute(totalDigitsNode, "value", Integer.toString(fieldLength));

                int fractionalDigitsNode = doc.createElement("fractionalDigits", restrictionNode);
                Node.setAttribute(fractionalDigitsNode, "value", Integer.toString(noOfDecimals));
                break;

            case JCO.TYPE_DATE:
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
                patternNode = doc.createElement("pattern", restrictionNode);
                Node.setAttribute(patternNode, "value", "....-..-..");
                break;

            case JCO.TYPE_TIME:
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
                patternNode = doc.createElement("pattern", restrictionNode);
                Node.setAttribute(patternNode, "value", "..-..-..");
                break;

            case JCO.TYPE_INT:
            case JCO.TYPE_INT2:
                Node.setAttribute(elementNode, "type", xsdPrefix + "int");
                break;

            case JCO.TYPE_INT1:
                Node.setAttribute(elementNode, "type", xsdPrefix + "unsignedByte");
                break;

            case JCO.TYPE_FLOAT:
                Node.setAttribute(elementNode, "type", xsdPrefix + "double");
                break;

            case JCO.TYPE_BYTE:
            case JCO.TYPE_XSTRING:
                fieldLength = field.getLength();
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "base64Binary");
                lengthNode = doc.createElement("length", restrictionNode);
                Node.setAttribute(lengthNode, "value", Integer.toString(fieldLength));
                break;

            case JCO.TYPE_CHAR:
            case JCO.TYPE_STRING:
                fieldLength = field.getLength();
                simpleTypeNode = doc.createElement("simpleType", elementNode);
                restrictionNode = doc.createElement("restriction", simpleTypeNode);
                Node.setAttribute(restrictionNode, "base", xsdPrefix + "string");
                maxLengthNode = doc.createElement("maxLength", restrictionNode);
                Node.setAttribute(maxLengthNode, "value", Integer.toString(fieldLength));
                break;
        }
        return elementNode;
    }

    /**
     * This method generates the schema element for a given segment. First this method is called for
     * the root segment, which has no siblings. Scheme element for fields in a segment are generated
     * based on the data type of a field. For each segment, all its child segment elements are
     * generated recursively.
     *
     * @param  parentNode       DOCUMENTME
     * @param  segmentMetadata  DOCUMENTME
     * @param  doc              DOCUMENTME
     */
    private void handleSegment(int parentNode, IDoc.SegmentMetaData segmentMetadata, Document doc)
    {
        int noOfChildSegments = segmentMetadata.getNumChildren();

        // For each fhild segment
        for (int i = 0; i < noOfChildSegments; i++)
        {
            IDoc.SegmentMetaData childSegmentMetadata = segmentMetadata.getChild(i);
            String segmentType = childSegmentMetadata.getType();
            String segmentDescription = childSegmentMetadata.getDescription();
            long minOccurs = childSegmentMetadata.getMinOccurrence();
            long maxOccurs = childSegmentMetadata.getMaxOccurrence();
            int elementNode = doc.createElement("element", parentNode);
            Node.setAttribute(elementNode, "name", segmentType);
            Node.setAttribute(elementNode, "minOccurs", Long.toString(minOccurs));
            Node.setAttribute(elementNode, "maxOccurs", Long.toString(maxOccurs));

            if (segmentDescription != null)
            {
                int annotationNode = doc.createElement("annotation", elementNode);
                doc.createTextElement("documentation", segmentDescription, annotationNode);
            }

            int complexTypeNode = doc.createElement("complexType", elementNode);
            int sequenceNode = doc.createElement("sequence", complexTypeNode);
            createAttributeNode(complexTypeNode, "SEGMENT", doc);

            // To handle the fields in a segment.
            IDoc.RecordMetaData recordMetada = childSegmentMetadata.getRecordMetaData();
            int noOfFields = recordMetada.getNumFields();

            for (int j = 0; j < noOfFields; j++)
            {
                IDoc.FieldMetaData fieldMetadata = recordMetada.getFieldMetaData(j);
                String fieldType = fieldMetadata.getDataTypeName();
                String fieldName = fieldMetadata.getFieldName();
                String fieldDescription = fieldMetadata.getDescription();
                int fieldElementNode = doc.createElement("element", sequenceNode);
                Node.setAttribute(fieldElementNode, "name", fieldName);
                Node.setAttribute(fieldElementNode, "minOccurs", "0");

                if (fieldDescription != null)
                {
                    int fieldAnnotationNode = doc.createElement("annotation", fieldElementNode);
                    doc.createTextElement("documentation", fieldDescription, fieldAnnotationNode);
                }

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
                        // System.out.println(fieldName + " " +  fieldType +" "+decimalLength);
                    }
                }
                generateSimpleDataTypeNodeForFieldsInIDOCSegments(fieldElementNode, fieldType, doc,
                                                                  fieldLength, decimalLength);
            }
            // To handle its child segments.
            handleSegment(sequenceNode, childSegmentMetadata, doc);
        }
    }

    /**
     * This method generates the element for a structure and its corresponding complex type.
     *
     * @param  schemaNode     DOCUMENTME
     * @param  parentNode     DOCUMENTME
     * @param  field          DOCUMENTME
     * @param  referenceType  DOCUMENTME
     * @param  doc            DOCUMENTME
     */
    private void handleStructureParameter(int schemaNode, int parentNode, JCO.Field field,
                                          String referenceType, Document doc)
    {
        String structName = field.getName();
        // Not to show RETURN parameter in the schema as we are not showing it in the response
        // if(structName.equals("RETURN")) return;
        int fieldElement = doc.createElement("element", parentNode);
        Node.setAttribute(fieldElement, "name", structName);
        Node.setAttribute(fieldElement, "type", referenceType);

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            int annotationNode = doc.createElement("annotation", fieldElement);
            // "description" is WCP specific
            int descriptionNode = doc.createTextElement("documentation", fieldDescription,
                                                        annotationNode);
        }
        generateComplexType(schemaNode, field, referenceType, doc, true);
    }

    /**
     * This method generates the element for a table and its corresponding complex type for a row in
     * the table.
     *
     * @param  schemaNode     DOCUMENTME
     * @param  parentNode     DOCUMENTME
     * @param  field          DOCUMENTME
     * @param  referenceType  DOCUMENTME
     * @param  doc            DOCUMENTME
     */
    private void handleTableParameter(int schemaNode, int parentNode, JCO.Field field,
                                      String referenceType, Document doc)
    {
        String tableName = field.getName();
        // Not to show RETURN parameter in the schema as we are not showing it in the response
        // if(tableName.equals("RETURN")) return;
        int fieldElement = doc.createElement("element", parentNode);
        Node.setAttribute(fieldElement, "name", tableName);

        String fieldDescription = field.getDescription();

        if (fieldDescription != null)
        {
            int annotationNode = doc.createElement("annotation", fieldElement);
            // "description" is WCP specific
            int descriptionNode = doc.createTextElement("documentation", fieldDescription,
                                                        annotationNode);
        }

        int complexTypeNode = doc.createElement("complexType", fieldElement);
        int sequenceNode = doc.createElement("sequence", complexTypeNode);
        int itemNode = doc.createElement("element", sequenceNode);
        Node.setAttribute(itemNode, "name", "item");
        Node.setAttribute(itemNode, "type", referenceType);
        Node.setAttribute(itemNode, "minOccurs", "0");
        Node.setAttribute(itemNode, "maxOccurs", "unbounded");
        generateComplexType(schemaNode, field, referenceType, doc, false);
    }
}
