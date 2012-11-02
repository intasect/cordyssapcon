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

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;

import com.sap.mw.jco.*;

/**
 * This is a super class of JCO.Client class. This class holds an extra property to know if the
 * connection is being used by a transaction or not.
 *
 * @author  ygopal
 */
public class SAPJCoConnection extends JCO.Client
{
    /**
     * Holds whether or not the connection is being used at the moment.
     */
    private boolean isInUse;

    /**
     * This method creates the SAP connection.
     *
     * @param  config    The configuration
     * @param  user
     * @param  password
     */
    SAPJCoConnection(ISAPConfiguration config, String user, String password)
    {
        super(config.getSAPClient(), user, password, config.getLanguage(), config.getSAPServer(),
              config.getSystemNumber());
    }

    /**
     * This method returns whether or not the connection is being used at the moment.
     *
     * @return  Whether or not the connection is being used at the moment.
     */
    public boolean getUseStatus()
    {
        return isInUse;
    }

    /**
     * This method sets whether or not the connection is being used at the moment.
     *
     * @param  status  Whether or not the connection is being used at the moment.
     */
    public void setUseStatus(boolean status)
    {
        isInUse = status;
    }
    
    /**
     * @return TRUE/FALSE tells whether partner system is unicode or not
     */
    public boolean isPartnerSystemUnicode()
    {
        if (attributes == null) {
        	      return false;
        	   }
        	   String codepage = attributes.getPartnerCodepage();
        	 
        return ((codepage != null) && (codepage.length() > 0) && (codepage.charAt(0) == '4'));
    }
}
