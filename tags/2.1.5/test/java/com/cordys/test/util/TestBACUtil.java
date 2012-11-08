package com.cordys.test.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.eibus.applicationconnector.sap.util.BACUtil;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class TestBACUtil {

	@Test
	public void testFormatRFC_READ_TABLEResponse() 
	{
		  
	  
	    	Document doc = new Document() ;
	    	int request = 0;
	    	int response = 0; 
	    	 try {
				request = doc.load(".\\test\\java\\com\\cordys\\test\\util\\rfc_read_table_request.xml") ;
			
				response = doc.load(".\\test\\java\\com\\cordys\\test\\util\\rfc_read_table_response.xml") ;
			} catch (XMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("Failed to parse input xmls");
			}
	    	BACUtil.formatRFC_READ_TABLEResponse(request, response) ;
	    	System.out.println(Node.writeToString(response, true));    	
	   
		
	}

}
