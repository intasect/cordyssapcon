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
package com.eibus.applicationconnector.sap.connection;

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;

import com.eibus.soap.BodyBlock;

/**
 * This is the interface to handle the requests coming to the SOAP processor. It contains abstract
 * methods to handle the different types of requests coming to the processor.
 *
 * @author  ygopal
 */
public interface ISAPRequestHandler
{
    /**
     * Request XML for RFC BAPI_TRANSACTION_COMMIT.
     */
    String XML_COMMIT_REQUEST = "<BAPI_TRANSACTION_COMMIT xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"><WAIT>X</WAIT></BAPI_TRANSACTION_COMMIT>";
    /**
     * Common attribute for all the Cordys requests.
     */
    String commonAttributeName = "xmlns";
    /**
     * Request XML for RFC BAPI_TRANSACTION_ROLLBACK.
     */
    String rollbackRequestXML = "<BAPI_TRANSACTION_ROLLBACK xmlns:rfc=\"urn:sap-com:document:sap:rfc:functions\"></BAPI_TRANSACTION_ROLLBACK>";

    /**
     * To abort a transaction.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void abort()
        throws SAPConnectorException;

    /**
     * To commit a transaction.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    void commit()
         throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPBAPI.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleBAPIRequest(BodyBlock request, BodyBlock response)
                       throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPIDOC.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleIDOCRequest(BodyBlock request, BodyBlock response)
                       throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPMetadata.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleMetaDataRequest(BodyBlock request, BodyBlock response)
                           throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPPublish.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleMethodPublishRequest(BodyBlock request, BodyBlock response)
                                throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPRFC.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleRFCReqeust(BodyBlock request, BodyBlock response)
                      throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPTuple which are in tuple format.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleTupleRequest(BodyBlock request, BodyBlock response)
                        throws SAPConnectorException;

    /**
     * To handle requests of implementation type SAPUtil which are some utillity requests.
     *
     * @param   request   DOCUMENTME
     * @param   response  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    boolean handleUtilRequest(BodyBlock request, BodyBlock response)
                       throws SAPConnectorException;
}
