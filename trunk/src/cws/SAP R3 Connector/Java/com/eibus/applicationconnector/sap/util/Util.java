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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class contains utility methods.
 *
 * @author  pgussow
 */
public class Util
{
    /**
     * This method returns whether or not a string is filled.
     *
     * @param   source  The source to check.
     *
     * @return  true if the string is set. Otherwise false.
     */
    public static boolean isSet(String source)
    {
        return (source != null) && (source.trim().length() > 0);
    }
    
    /** Reads the content of the file and encode it to Base64. This is used to read the serialized IDOC Metadata object content into SOAP Response.
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readFileAndEncode(String fileName) throws IOException
	{
		File file = new File(fileName);
		byte[] byteArray = new byte[302400] ;
		FileInputStream fis = new FileInputStream(file);
		StringBuffer sBuffer = new StringBuffer() ;
		int bytesRead = 0 ;
		while((bytesRead = fis.read(byteArray))!=-1)
		{
			byte[] actualBytesRead = new byte[bytesRead];			
			String encodedString = com.eibus.util.Base64.encodeToStr(actualBytesRead) ;
			sBuffer.append(encodedString);
			System.arraycopy(byteArray, 0, actualBytesRead, 0, bytesRead);
			//System.out.println(actualBytesRead.length);
		}	
		fis.close();
		return sBuffer.toString() ;
		
	}
}
