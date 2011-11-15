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
 * This interface describes the usermapping that can be used.
 *
 * @author pgussow
 */
public interface IUserMapping
{
    /**
     * This method gets the mapped password.
     *
     * @return  The mapped password.
     */
    String getMappedPassword();

    /**
     * This method gets the mapped user name.
     *
     * @return  The mapped user name.
     */
    String getMappedUsername();

    /**
     * This method gets the original user name.
     *
     * @return  The original user name.
     */
    String getOriginalUsername();

    /**
     * This method sets the mapped password.
     *
     * @param  mappedPassword  The mapped password.
     */
    void setMappedPassword(String mappedPassword);

    /**
     * This method sets the mapped user name.
     *
     * @param  mappedUsername  The mapped user name.
     */
    void setMappedUsername(String mappedUsername);

    /**
     * This method sets the original user name.
     *
     * @param  originalUsername  The original user name.
     */
    void setOriginalUsername(String originalUsername);
}
