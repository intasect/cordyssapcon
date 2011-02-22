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

import com.eibus.util.logger.CordysLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.security.KeyStore;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.Enumeration;

import sun.misc.BASE64Decoder;

/**
 * This class provides functionality to create a key store and manipulate it's content. It provides
 * API methods for creating,loading certifates,deleting an existing aliases, retrieving certificate
 * details present in key store..etc.
 */

public class SAPKeyStore
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPKeyStore.class);

    /**
     * This method will create a new keystore at given location.
     *
     * @param   type          key store type
     * @param   provider      : provider name
     * @param   keystorePath  keystore path
     * @param   passwd        encrypted keystore password
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static void createKeyStore(String type, String provider, String keystorePath,
                                      String passwd)
                               throws SAPConnectorException
    {
        KeyStore ks = null;

        try
        {
            ks = KeyStore.getInstance(type, provider);

            if (ks != null)
            {
                String storePass = new String(new BASE64Decoder().decodeBuffer(passwd));
                ks.load(null, storePass.toCharArray());

                File directory = new File(keystorePath.substring(0,
                                                                 keystorePath.lastIndexOf("\\")));

                if (!directory.exists())
                {
                    directory.mkdirs();
                }
                ks.store(new FileOutputStream(new File(keystorePath)), passwd.trim().toCharArray());

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("key store created successfully");
                }
            }
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.ERROR_LOADING_KEYSTORE,
                                            type, provider, keystorePath);
        }
    }

    /**
     * deletes the keystore entry.
     *
     * @param   type          key store type
     * @param   provider      : provider name
     * @param   keystorePath  keystore path
     * @param   passwd        encrypted keystore password
     * @param   alias         alias to be deleted
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static void deleteAlias(String type, String provider, String keystorePath, String passwd,
                                   String alias)
                            throws SAPConnectorException
    {
        KeyStore ks = null;

        try
        {
            ks = KeyStore.getInstance(type, provider);

            if (ks != null)
            {
                String storePass = new String(new BASE64Decoder().decodeBuffer(passwd), "UTF8");
                ks.load(new FileInputStream(keystorePath), storePass.toCharArray());
                ks.deleteEntry(alias);
                ks.store(new FileOutputStream(new File(keystorePath)), storePass.toCharArray());
            }
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.ERROR_DELETING_ALIAS,
                                            alias, type, provider, keystorePath);
        }
    }

    /**
     * Returns the details of the certificate.
     *
     * @param   type          key store type
     * @param   provider      : provider name
     * @param   keystorePath  keystore path
     * @param   passwd        encrypted keystore password
     * @param   alias         Entry alias name
     *
     * @return  xml node containing the entry details
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static String getCertificateDetails(String type, String provider, String keystorePath,
                                               String passwd, String alias)
                                        throws SAPConnectorException
    {
        KeyStore ks = null;
        StringBuffer xml = null;

        try
        {
            ks = KeyStore.getInstance(type, provider);

            if (ks != null)
            {
                String storePass = new String(new BASE64Decoder().decodeBuffer(passwd), "UTF8");
                ks.load(new FileInputStream(keystorePath), storePass.toCharArray());

                java.security.cert.X509Certificate certificate = (X509Certificate)
                                                                     ks.getCertificate(alias);

                if (certificate != null)
                {
                    xml = new StringBuffer();
                    xml.append("<certdetails>");
                    xml.append("<version>V" + certificate.getVersion() + "</version>");
                    xml.append("<serialno>" + certificate.getSerialNumber().longValue() +
                               "</serialno>");
                    xml.append("<type>" + certificate.getType() + "</type>");
                    xml.append("<issuer>" + certificate.getIssuerDN().toString() + "</issuer>");
                    xml.append("<sigalg>" + certificate.getSigAlgName() + "</sigalg>");
                    xml.append("<validfrom>" + certificate.getNotBefore().toString() +
                               "</validfrom>");
                    xml.append("<validto>" + certificate.getNotAfter().toString() + "</validto>");
                    xml.append("<subject>" + certificate.getSubjectDN().toString() + "</subject>");
                    xml.append("<keyalg>" + certificate.getPublicKey().getAlgorithm() +
                               "</keyalg>");
                    xml.append("</certdetails>");
                }
            }
            return xml.toString();
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.ERROR_GETTING_ALIAS_DETAILS,
                                            alias, type, provider, keystorePath);
        }
    }

    /**
     * This method returns the list of aliases for the entries in the keyStore.
     *
     * @param   type          key store type
     * @param   provider      : provider name
     * @param   keystorePath  keystore path
     * @param   passwd        keystore password
     *
     * @return  a Vector of allias of the entries
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static String getEntries(String type, String provider, String keystorePath,
                                    String passwd)
                             throws SAPConnectorException
    {
        KeyStore ks = null;
        StringBuffer xml = null;

        try
        {
            ks = KeyStore.getInstance(type, provider);

            if (ks != null)
            {
                String storePass = new String(new BASE64Decoder().decodeBuffer(passwd), "UTF8");
                ks.load(new FileInputStream(keystorePath), storePass.toCharArray());

                Enumeration<?> en = ks.aliases();

                if (en != null)
                {
                    xml = new StringBuffer();
                    xml.append("<menu>");
                    xml.append("<description>" + "Aliases" + "</description>");

                    while (en.hasMoreElements())
                    {
                        xml.append("<alias><description>" + en.nextElement().toString() +
                                   "</description></alias>");
                    }
                    xml.append("</menu>");
                }
            }

            return xml.toString();
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e, SAPConnectorExceptionMessages.ERROR_GETTING_ENTRIES,
                                            type, provider, keystorePath);
        }
    }

    /**
     * loads a certificate in to the keystore.
     *
     * @param   type          key store type
     * @param   provider      : provider name.
     * @param   keystorePath  keystore path
     * @param   passwd        encrypted keystore password
     * @param   certPath      path where the certificate is present
     * @param   alias         alias to be used for this certificate
     *
     * @throws  SAPConnectorException  In case of any exceptions
     */
    public static void loadCertificate(String type, String provider, String keystorePath,
                                       String passwd, String certPath, String alias)
                                throws SAPConnectorException
    {
        KeyStore ks = null;

        try
        {
            ks = KeyStore.getInstance(type, provider);

            if (ks != null)
            {
                String storePass = new String(new BASE64Decoder().decodeBuffer(passwd), "UTF8");
                ks.load(new FileInputStream(keystorePath), storePass.toCharArray());

                FileInputStream fis = new FileInputStream(certPath);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                java.security.cert.Certificate cer = null;
                cer = cf.generateCertificate(fis);

                if (cer != null)
                {
                    ks.setCertificateEntry(alias, cer);
                    ks.store(new FileOutputStream(new File(keystorePath)), storePass.toCharArray());
                }
            }
        }
        catch (Exception e)
        {
            throw new SAPConnectorException(e,
                                            SAPConnectorExceptionMessages.ERROR_LOADING_CERTIFICATES,
                                            type, provider, keystorePath, certPath, alias);
        }
    }
}
