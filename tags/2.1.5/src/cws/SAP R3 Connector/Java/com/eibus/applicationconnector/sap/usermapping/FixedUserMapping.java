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

/**
 * This class uses fixed user mapping.
 *
 * @author  pgussow
 */
public class FixedUserMapping
    implements IUserMapping
{
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
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getMappedPassword()
     */
    @Override public String getMappedPassword()
    {
        return m_mappedPassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getMappedUsername()
     */
    @Override public String getMappedUsername()
    {
        return m_mappedUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#getOriginalUsername()
     */
    @Override public String getOriginalUsername()
    {
        return m_originalUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setMappedPassword(java.lang.String)
     */
    @Override public void setMappedPassword(String mappedPassword)
    {
        m_mappedPassword = mappedPassword;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setMappedUsername(java.lang.String)
     */
    @Override public void setMappedUsername(String mappedUsername)
    {
        m_mappedUsername = mappedUsername;
    }

    /**
     * @see  com.eibus.applicationconnector.sap.usermapping.IUserMapping#setOriginalUsername(java.lang.String)
     */
    @Override public void setOriginalUsername(String originalUsername)
    {
        m_originalUsername = originalUsername;
    }
}
