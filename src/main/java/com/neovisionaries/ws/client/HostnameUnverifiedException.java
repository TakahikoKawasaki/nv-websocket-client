/*
 * Copyright (C) 2017 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neovisionaries.ws.client;


import javax.net.ssl.SSLSocket;


/**
 * The certificate of the peer does not match the expected hostname.
 *
 * <p>
 * {@link #getError()} of this class returns {@link WebSocketError#HOSTNAME_UNVERIFIED
 * HOSTNAME_UNVERIFIED}.
 * </p>
 *
 * <p>
 * See <a href='https://github.com/TakahikoKawasaki/nv-websocket-client/pull/107'
 * >Verify that certificate is valid for server hostname (#107)</a>.
 * </p>
 *
 * @since 2.1
 */
public class HostnameUnverifiedException extends WebSocketException
{
    private static final long serialVersionUID = 1L;


    private final SSLSocket mSSLSocket;
    private final String mHostname;


    /**
     * Constructor with the SSL socket and the expected hostname.
     *
     * @param socket
     *         The SSL socket against which the hostname verification failed.
     *
     * @param hostname
     *         The expected hostname.
     */
    public HostnameUnverifiedException(SSLSocket socket, String hostname)
    {
        super(WebSocketError.HOSTNAME_UNVERIFIED,
                String.format("The certificate of the peer%s does not match the expected hostname (%s)",
                        stringifyPrincipal(socket), hostname));

        mSSLSocket = socket;
        mHostname  = hostname;
    }


    private static String stringifyPrincipal(SSLSocket socket)
    {
        try
        {
            return String.format(" (%s)", socket.getSession().getPeerPrincipal().toString());
        }
        catch (Exception e)
        {
            // Principal information is not available.
            return "";
        }
    }


    /**
     * Get the SSL socket against which the hostname verification failed.
     *
     * @return
     *         The SSL socket.
     */
    public SSLSocket getSSLSocket()
    {
        return mSSLSocket;
    }


    /**
     * Get the expected hostname.
     *
     * @return
     *         The expected hostname.
     */
    public String getHostname()
    {
        return mHostname;
    }
}
