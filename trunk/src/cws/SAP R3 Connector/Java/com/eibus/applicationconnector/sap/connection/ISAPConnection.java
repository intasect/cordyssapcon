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
import com.eibus.applicationconnector.sap.metadata.types.ITypeContainer;

import java.util.List;

/**
 * This interface describes the methods that can be applied to a SAP connection.
 *
 * @author  pgussow
 */
public interface ISAPConnection
{
    /**
     * This method will search the SAP system to return all BAPIs that can be found.
     *
     * @return  All BAPIs found.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> getAllBAPIs()
                              throws SAPConnectorException;

    /**
     * This method will search the SAP system to return all IDOCs that can be found.
     *
     * @return  All IDOCs found.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> getAllIDOCs()
                              throws SAPConnectorException;

    /**
     * This method will search the SAP system to return all RFCs that can be found.
     *
     * @return  All RFCs found.
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    List<ITypeContainer> getAllRFCs()
                             throws SAPConnectorException;
}
