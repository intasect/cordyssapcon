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
 package com.eibus.applicationconnector.sap;

import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;
import com.eibus.applicationconnector.sap.util.NVPair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Ths Class is used to make Http requests and encapsulates the functionality of getting the
 * response and returing.It can be used to add HTTP headers to the reqest also.
 */
public class HttpRequest
{
    /**
     * Collection of NVPairs.Each NVPair represent a HTTP request header.
     */
    private Vector<NVPair> headers;
    /**
     * URL to which request has to be made.
     */
    private URL url;

    /**
     * Creates a new HttpRequest object.
     *
     * @param  url  DOCUMENTME
     */
    public HttpRequest(URL url)
    {
        this.url = url;
        this.headers = new Vector<NVPair>();
    }

    /**
     * Creates a new HttpRequest object.
     *
     * @param  url      DOCUMENTME
     * @param  headers  DOCUMENTME
     */
    @SuppressWarnings("unchecked")
    public HttpRequest(URL url, Vector<NVPair> headers)
    {
        this.url = url;
        this.headers = (Vector<NVPair>) headers.clone();
    }

    /**
     * Add a header to the HTTP request header.
     *
     * @param  header  NVPair containing the Header name value
     */
    public void addHeader(NVPair header)
    {
        headers.add(header);
    }

    /**
     * Returns the URL for which request is being made.
     *
     * @return  DOCUMENTME
     */
    public URL getURL()
    {
        return (url);
    }

    /**
     * Makes the Http request to the URL.
     *
     * @param   request  contains the query sting value to be sent
     *
     * @return  response of the request
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public String sendRequest(String request)
                       throws SAPConnectorException
    {
        int errCode = 0;
        String errMessage = "";
        StringBuffer resText = new StringBuffer();

        try
        {
            HttpURLConnection httpCon;
            // System.out.println("url " + url.toString());
            // System.out.println("request \n"+ request);
            URLConnection urlCon = url.openConnection();
            httpCon = (HttpURLConnection) urlCon;
            urlCon.setDoInput(true);
            urlCon.setDoOutput(true);
            addHeaders(urlCon);

            OutputStream os = urlCon.getOutputStream();
            os.write(request.getBytes());
            os.close();

            errCode = httpCon.getResponseCode();
            errMessage = httpCon.getResponseMessage();

            if (!((errCode >= 200) && (errCode <= 300)))
            {
                throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_RETURNED_FROM_SAP,
                                                errCode, errMessage);
            }

            if (errCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlCon
                                                                             .getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                {
                    resText.append(inputLine);
                }
                in.close();
            }
        }
        catch (SAPConnectorException sce)
        {
            throw sce;
        }
        catch (Exception exception)
        {
            throw new SAPConnectorException(exception,
                                            SAPConnectorExceptionMessages.ERROR_SENDING_REQUEST);
        }
        return (resText.toString());
    }

    /**
     * adds headers in the collection headers to the request.
     *
     * @param  urlCon  URL Connection
     */
    private void addHeaders(URLConnection urlCon)
    {
        Enumeration<NVPair> e = headers.elements();

        while (e.hasMoreElements())
        {
            NVPair header = e.nextElement();
            urlCon.setRequestProperty(header.getName(), header.getValue());
        }
    }
}
