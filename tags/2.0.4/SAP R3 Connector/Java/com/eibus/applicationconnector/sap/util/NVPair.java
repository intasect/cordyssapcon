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
 package com.eibus.applicationconnector.sap.util;

/**
 * This class is used to represent name value pairs for the http header.
 */
public final class NVPair
{
    /**
     * the name.
     */
    private String name;

    /**
     * the value.
     */
    private String value;

    /**
     * Creates a copy of a given name/value pair.
     *
     * @param  p  the name/value pair to copy
     */
    public NVPair(NVPair p)
    {
        this(p.name, p.value);
    }

    /**
     * Creates a new name/value pair and initializes it to the specified name and value.
     *
     * @param  name   the name
     * @param  value  the value
     */
    public NVPair(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name.
     *
     * @return  the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the value.
     *
     * @return  the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Produces a string containing the name and value of this instance.
     *
     * @return  a string containing the class name and the name and value
     */
    @Override public String toString()
    {
        return getClass().getName() + "[name=" + name + ",value=" + value + "]";
    }
}
