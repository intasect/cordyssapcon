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
 package com.eibus.applicationconnector.sap.usermapping;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;

import com.eibus.soap.SOAPTransaction;

/**
 * This factory can create the user mapping that can be used.
 *
 * @author  pgussow
 */
public class UserMappingFactory
{
    /**
     * This method returns a user mapping object based on the system user.
     *
     * @param   config  The SAP connector configuration to use.
     *
     * @return  The created user mapping.
     */
    public static IUserMapping createFixedUserMapping(ISAPConfiguration config)
    {
        FixedUserMapping returnValue = new FixedUserMapping();

        returnValue.setMappedPassword(config.getPassword());
        returnValue.setMappedUsername(config.getUserID());
        returnValue.setOriginalUsername(config.getUserID());

        return returnValue;
    }

    /**
     * This method returns a user mapping object based on the current SOAP transaction.
     *
     * @param   transaction  The current SOAP transaction.
     * @param   config       The SAP connector configuration to use.
     *
     * @return  The created user mapping.
     */
    @SuppressWarnings("deprecation")
    public static IUserMapping createLDAPBasedUserMapping(SOAPTransaction transaction,
                                                          ISAPConfiguration config)
    {
        return new LDAPUserMapping(transaction.getUserCredentials(), config);
    }

    /**
     * This method returns the proper user mapping that should be used. If ran from the SOAP
     * transaction it will use the SOAP mapped user. Otherwise it will use the system user.
     *
     * @param   config  The SAP connector configuration to use.
     *
     * @return  The created user mapping.
     */
    public static IUserMapping createUserMapping(ISAPConfiguration config)
    {
        IUserMapping returnValue = null;

        SOAPTransaction transaction = SOAPTransaction.getCurrentSOAPTransaction();

        if (transaction != null)
        {
            returnValue = createLDAPBasedUserMapping(transaction, config);
        }
        else
        {
            returnValue = createFixedUserMapping(config);
        }

        return returnValue;
    }
}
