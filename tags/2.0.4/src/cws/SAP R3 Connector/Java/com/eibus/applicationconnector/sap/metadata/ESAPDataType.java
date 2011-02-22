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
 package com.eibus.applicationconnector.sap.metadata;

import com.sap.mw.jco.JCO;

import java.util.ArrayList;

/**
 * This enumeration holds the mapping between JCO data types (used in RFC and BAPI metadata) and the
 * actual names as used in the IDOC metadata interface.
 *
 * @author  pgussow
 */
public enum ESAPDataType
{
    NUM(new Integer[] { JCO.TYPE_NUM }, new String[] { "N", "NUMC", "ACCP", "PREC" }),
    BCD(new Integer[] { JCO.TYPE_BCD }, new String[] { "P", "QUAN", "CURR", "DEC" }),
    DATE(new Integer[] { JCO.TYPE_DATE }, new String[] { "D", "DATS" }),
    TIME(new Integer[] { JCO.TYPE_TIME }, new String[] { "T", "TIMS" }),
    INT(new Integer[] { JCO.TYPE_INT, JCO.TYPE_INT2 }, new String[] { "I", "INT2", "INT4" }),
    UNSIGNED_BYTE(new Integer[] { JCO.TYPE_INT1 }, new String[] { "b", "INT1" }),
    FLOAT(new Integer[] { JCO.TYPE_FLOAT }, new String[] { "F", "FLTP" }),
    BINARY(new Integer[] { JCO.TYPE_BYTE, JCO.TYPE_XSTRING },
           new String[] { "X", "RAW", "LRAW", "RAWSTRING" }),
    STRING(new Integer[] { JCO.TYPE_CHAR, JCO.TYPE_STRING },
           new String[] { "C", "CHAR", "UNIT", "CUKY", "LANG", "CLNT", "STRING" }),
    UNKNOWN(new Integer[0], new String[0]);

    /**
     * Holds the JCO types for this data type.
     */
    private ArrayList<Integer> m_jcoTypes;
    /**
     * Holds the IDOC types for this data type.
     */
    private ArrayList<String> m_idocTypes;

    /**
     * Creates a new ESAPDataType object.
     *
     * @param  jcoTypes   The list of RFC/BAPI types for the data type.
     * @param  idocTypes  The list of IDOC types for the data type.
     */
    ESAPDataType(Integer[] jcoTypes, String[] idocTypes)
    {
        m_jcoTypes = new ArrayList<Integer>();
        m_idocTypes = new ArrayList<String>();

        for (String idocType : idocTypes)
        {
            m_idocTypes.add(idocType.toLowerCase());
        }

        for (Integer jcoType : jcoTypes)
        {
            m_jcoTypes.add(jcoType);
        }
    }

    /**
     * This method returns the ESAPDataType based on the given JCO type.
     *
     * @param   jcoType  The JCO type to check.
     *
     * @return  The mapped datatype.
     */
    public static ESAPDataType mapJCOType(Integer jcoType)
    {
        ESAPDataType[] temp = ESAPDataType.values();

        for (ESAPDataType dataType : temp)
        {
            if (dataType.matchJCO(jcoType))
            {
                return dataType;
            }
        }

        return UNKNOWN;
    }

    /**
     * This method returns whether or not the current data type matches the given jco data type.
     *
     * @param   jcoType  The jco data type.
     *
     * @return  Whether or not the current data type matches the given jco data type.
     */
    private boolean matchJCO(Integer jcoType)
    {
        return m_jcoTypes.contains(jcoType);
    }

    /**
     * This method returns the ESAPDataType based on the given IDOC type.
     *
     * @param   idocType  The IDOC type to check.
     *
     * @return  The mapped data type.
     */
    public static ESAPDataType mapIDOCType(String idocType)
    {
        ESAPDataType[] temp = ESAPDataType.values();

        for (ESAPDataType dataType : temp)
        {
            if (dataType.matchIDOC(idocType))
            {
                return dataType;
            }
        }

        return UNKNOWN;
    }

    /**
     * This method returns whether or not the current data type matches the given idoc data type.
     *
     * @param   idocType  The idoc data type.
     *
     * @return  Whether or not the current data type matches the given idoc data type.
     */
    private boolean matchIDOC(String idocType)
    {
        return m_idocTypes.contains(idocType.toLowerCase());
    }
}
