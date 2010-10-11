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
 package com.eibus.applicationconnector.sap.soap.impl;

import com.eibus.applicationconnector.sap.soap.EDynamicAction;

/**
 * This method describes the parsed implementation of methods.
 *
 * @author  pgussow
 */
public interface IMethodImplementation
{
    /**
     * This method gets the dynamic action for this method implementation.
     *
     * @return  The dynamic action for this method implementation.
     */
    EDynamicAction getAction();

    /**
     * This method sets the dynamic action for this method implementation.
     *
     * @param  action  The dynamic action for this method implementation.
     */
    void setAction(EDynamicAction action);
}
