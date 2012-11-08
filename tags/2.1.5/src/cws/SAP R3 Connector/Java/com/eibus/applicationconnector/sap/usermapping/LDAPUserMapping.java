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

import com.eibus.applicationconnector.sap.Messages;
import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.util.Util;

import com.eibus.security.ac.AccessControlObject;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.system.Native;

/**
 * This class implements LDAP based mapped username.
 *
 * @author  pgussow
 */
class LDAPUserMapping
    implements IUserMapping
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(LDAPUserMapping.class);
    /**
     * Holds the mapped password.
     */
    private String m_mappedPassword;
    /**
     * Holds the mapped user name.
     */
    private String m_mappedUsername;
    /**
     * Holds the original user name.
     */
    private String m_originalUsername;

    /**
     * Creates a new LDAPUserMapping object.
     *
     * @param  aco     The access control object for the current user.
     * @param  config  The configuration of the SAP connector.
     */
    public LDAPUserMapping(AccessControlObject aco, ISAPConfiguration config)
    {
        // Get the original username.
        m_originalUsername = aco.getOrganizationalUser();

        // Get the mapped user name
        m_mappedUsername = aco.getMappedUserName();

        // Get the mapped user password.
        m_mappedPassword = "";

        String mappedUserPassword = aco.getMappedUserPassword();
        byte[] ba = mappedUserPassword.getBytes();

        if ((ba != null) && (ba.length > 0))
        {
            m_mappedPassword = new String(Native.decodeBinBase64(ba, ba.length));
        }

        // If no usermapping was given, we'll use the generic user.
        if (!Util.isSet(m_mappedUsername))
        {
            if (config.getFallbackToSystemUser())
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Reverting to the system user, since no user mapping was defined.");
                }

                m_mappedUsername = config.getUserID();
                m_mappedPassword = config.getPassword();
            }
            else
            {
                LOG.warn(null, Messages.WRN_NO_USERMAPPING_FOUND_FOR_USER,
                         aco.getOrganizationalUser());
            }
        }
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getMappedPassword()
     */
    public String getMappedPassword()
    {
        return m_mappedPassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getMappedUsername()
     */
    public String getMappedUsername()
    {
        return m_mappedUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getOriginalUsername()
     */
    public String getOriginalUsername()
    {
        return m_originalUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setMappedPassword(java.lang.String)
     */
    public void setMappedPassword(String mappedPassword)
    {
        m_mappedPassword = mappedPassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setMappedUsername(java.lang.String)
     */
    public void setMappedUsername(String mappedUsername)
    {
        m_mappedUsername = mappedUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setOriginalUsername(java.lang.String)
     */
    public void setOriginalUsername(String originalUsername)
    {
        m_originalUsername = originalUsername;
    }
}
