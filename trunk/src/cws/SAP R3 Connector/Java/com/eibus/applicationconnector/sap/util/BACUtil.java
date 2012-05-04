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

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * This class contains many utility methods we use all across the connectors.
 *
 * @author  ygopal
 */
public class BACUtil
{
    /**
     * This method is to delete a node is created. Sometime Node.delete method can throw an
     * exception if the node to be delete is 0. To avoid, it deletes only if the node is not 0.
     *
     * @param   nodeToBeDeleted  The node that should be deleted.
     *
     * @return  Always 0.
     */
    public static int deleteNode(int nodeToBeDeleted)
    {
        if (nodeToBeDeleted > 0)
        {
            Node.delete(nodeToBeDeleted);
        }

        return 0;
    }

    /**
     * An overloaded version of the above method. The default date format is yyyy-MM-dd
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static String getCurrentDate()
                                 throws SAPConnectorException
    {
        String defaultDateFormat = "yyyy-MM-dd";
        return getCurrentDate(defaultDateFormat);
    }

    /**
     * This method returns the current date in the given date format.
     *
     * @param   dateFormat  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static String getCurrentDate(String dateFormat)
                                 throws SAPConnectorException
    {
        SimpleDateFormat formatter;

        try
        {
            formatter = new SimpleDateFormat(dateFormat);
        }
        catch (Exception ex)
        {
            throw new SAPConnectorException(ex,
                                            SAPConnectorExceptionMessages.ERROR_GETTING_CURRENT_DATE);
        }

        Date today = new Date();
        return formatter.format(today);
    }
    
    /** This method parses the RFC_READ_TABLE response using the delimiter and forms the response node
     * @param requestNode
     * @param responseNode
     */
    public static void formatRFC_READ_TABLEResponse(int requestNode, int responseNode)
    {
    	String delimiter = ";"; 
    	int itemNodes[] = XPath.getMatchingNodes("FIELDS/item/FIELDNAME", null, requestNode);
    	int responseRecords[] = XPath.getMatchingNodes("DATA/item", null, responseNode);//RFC_READ_TABLE.Response><DATA><item><WA>
    	String tableName = Node.getDataElement(requestNode, "QUERY_TABLE", "Item") ;
    	if(responseRecords!=null && responseRecords.length > 0)
    	{
    		delimiter = Node.getDataElement(requestNode, "DELIMITER", "") ;  
    		if("".equalsIgnoreCase(delimiter))
    		{// If the delimiter is empty then don't parse the response
    			return ;
    		}
    	}
    	else
    	{
    		return ;
    	}
    	
    	for(int i=0; i< responseRecords.length ; i++)
    	{
    		handlerDelimitersReadTableResponse(responseRecords[i],itemNodes,delimiter,tableName) ;	
    		
    	}
    	
    }
    
    private static void handlerDelimitersReadTableResponse(int itemNode, int[] fieldNodes, String delimiter, String tableName)
    {
    	if(itemNode <= 0)
    		return ;
    	String contentNodeData = Node.getDataElement(itemNode, "WA", "") ;
    	if("".equalsIgnoreCase(contentNodeData)) 
    		return ; // No post processing
    	Node.delete(Node.getElement(itemNode, "WA")) ;
    	String logTokens[] = contentNodeData.split(delimiter);    	
    	for(int i=0; logTokens!=null && i<logTokens.length; i++)
    	{    		
    			Node.createElementWithParentNS(Node.getDataWithDefault(fieldNodes[i],"param"), logTokens[i].trim(),itemNode) ;   		
    		
    	}
    	Node.setName(itemNode, tableName);
    	
    }
  
}
