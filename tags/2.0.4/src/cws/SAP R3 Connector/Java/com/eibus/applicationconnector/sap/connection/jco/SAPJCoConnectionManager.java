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
package com.eibus.applicationconnector.sap.connection.jco;

import com.eibus.applicationconnector.sap.config.ISAPConfiguration;
import com.eibus.applicationconnector.sap.exception.SAPConnectorException;
import com.eibus.applicationconnector.sap.exception.SAPConnectorExceptionMessages;

import com.eibus.util.logger.CordysLogger;

import com.sap.mw.jco.*;

import java.util.LinkedList;

/**
 * This class acts a connection pool for SAP JCo connections. It implement the least recently used
 * Algorithm to enable controlling of Connections used..
 *
 * @author  ygopal
 */
public class SAPJCoConnectionManager
{
    /**
     * Holds the logger to use.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(SAPJCoConnectionManager.class);
    /**
     * Connection used to create the repositories.
     */
    private SAPJCoConnection connectionForRepositories = null;
    /**
     * Holds all the currently active connections.
     */
    private LinkedList<SAPJCoConnection> conUssageList;
    /**
     * Holds the maximum number of connections to create
     */
    private int maxConnections;

    /**
     * Creates a new SAPJCoConnectionManager object.
     *
     * @param  maxConnections  The maximum number of connections to create.
     */
    public SAPJCoConnectionManager(int maxConnections)
    {
        this.conUssageList = new LinkedList<SAPJCoConnection>();
        this.maxConnections = maxConnections;
    }

    /**
     * This method closes the all opened connections and clears the linked list.
     *
     * @throws  SAPConnectorException  SAPConnectorException In case of any exceptions
     */
    public void closeAllConnections()
                             throws SAPConnectorException
    {
        try
        {
            connectionForRepositories.disconnect();

            int noConnections = conUssageList.size();
            SAPJCoConnection connection;

            for (int i = 0; i < noConnections; i++)
            {
                connection = conUssageList.get(i);
                connection.disconnect();
            }
        }
        catch (JCO.Exception jce)
        {
            throw new SAPConnectorException(jce,
                                            SAPConnectorExceptionMessages.ERR_COULD_NOT_DISCONNECT,
                                            jce.getMessage());
        }
        conUssageList.clear();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Closed all connections to SAP server.");
        }
    }

    /**
     * returns the JCOConnection assosiated with given user from cached connection pool if exists
     * otherwise it will create and return a new connection with given user name and cache it for
     * future reference.
     *
     * @param   config    The current configuration of the connector.
     * @param   user      The username for the connection.
     * @param   password  The password for the user.
     *
     * @return  The connection to use.
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public synchronized SAPJCoConnection getUserConnection(ISAPConfiguration config, String user,
                                                           String password)
                                                    throws SAPConnectorException
    {
        int indexOfLastFreeConnection = -1; // stores the index of the last free connection
        int size = conUssageList.size();
        SAPJCoConnection jcoConnection;

        for (int i = 0; i < size; i++)
        {
            jcoConnection = conUssageList.get(i);

            if (jcoConnection.isAlive())
            {
                // Connection is alive
                if (!jcoConnection.getUseStatus())
                {
                    indexOfLastFreeConnection = i;

                    if (user.equals(jcoConnection.getUser()))
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Got cached Connectionobject");
                        }

                        conUssageList.remove(i);
                        conUssageList.addFirst(jcoConnection);
                        jcoConnection.setUseStatus(true);
                        return jcoConnection;
                    }
                }
            }
            else
            {
                // Connection is terminated. so remove it from the list
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Connection object in the cache is found terminated.");
                }
                conUssageList.remove(i);
                i--;
                size--;
            }
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Could not get cached Connection object");
        }

        if (size < maxConnections)
        {
            return getNewConnection(config, user, password);
        }
        else
        {
            if (indexOfLastFreeConnection != -1)
            {
                // get last free connection and remove it from linked list
                jcoConnection = conUssageList.get(indexOfLastFreeConnection);
                jcoConnection.disconnect();
                conUssageList.remove(indexOfLastFreeConnection);
                return getNewConnection(config, user, password);
            }
            else
            {
                try
                {
                    wait();
                }
                catch (InterruptedException ie)
                {
                    throw new SAPConnectorException(SAPConnectorExceptionMessages.ERROR_WAITING_FOR_FREE_CONNECTION);
                }

                return getUserConnection(config, user, password);
            }
        }
    }

    /**
     * This method returns the connection used for creating the repositories.
     *
     * @param   config    DOCUMENTME
     * @param   user      DOCUMENTME
     * @param   password  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    public SAPJCoConnection getUserConnectionForRepositories(ISAPConfiguration config, String user,
                                                             String password)
                                                      throws SAPConnectorException
    {
        if (connectionForRepositories == null)
        {
            connectionForRepositories = createNewConnection(config, user, password);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Connection to create repositories is established.");
        }

        return connectionForRepositories;
    }

    /**
     * To change the status of a connection when it is freed.
     *
     * @param  jcoConnection  The connection to free.
     */
    public synchronized void putUserConnection(SAPJCoConnection jcoConnection)
    {
        jcoConnection.setUseStatus(false);
        notify();
    }

    /**
     * This method creates a new connection to the given SAP server with the given user id and
     * password.
     *
     * @param   config    DOCUMENTME
     * @param   user      DOCUMENTME
     * @param   password  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exception.
     */
    private SAPJCoConnection createNewConnection(ISAPConfiguration config, String user,
                                                 String password)
                                          throws SAPConnectorException
    {
        SAPJCoConnection jcoConnection = new SAPJCoConnection(config, user, password);

        try
        {
            jcoConnection.connect();
        }
        catch (JCO.Exception je)
        {
            // We need to detect if the connection failed because of wrong username and password. If
            // so we'll throw a different exception with a diffirent code so that we could do
            // fallback if needed.
            String key = je.getKey();

            if (key.equals("RFC_ERROR_LOGON_FAILURE"))
            {
                throw new SAPConnectorException(je,
                                                SAPConnectorExceptionMessages.LOGIN_FAILED_FOR_USER,
                                                user);
            }

            throw new SAPConnectorException(je,
                                            SAPConnectorExceptionMessages.ERR_CREATING_CONNECTION,
                                            user);
        }

        return jcoConnection;
    }

    /**
     * To create a connection with the given parameters and add it to the linked list.
     *
     * @param   config    DOCUMENTME
     * @param   user      DOCUMENTME
     * @param   password  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  SAPConnectorException  In case of any exceptions.
     */
    private SAPJCoConnection getNewConnection(ISAPConfiguration config, String user,
                                              String password)
                                       throws SAPConnectorException
    {
        SAPJCoConnection jcoConnection = createNewConnection(config, user, password);

        jcoConnection.setUseStatus(true);
        conUssageList.addFirst(jcoConnection);

        return jcoConnection;
    }
}
