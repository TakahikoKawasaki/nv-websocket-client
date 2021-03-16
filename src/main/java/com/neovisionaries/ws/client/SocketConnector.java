/*
 * Copyright (C) 2016-2017 Neo Visionaries Inc.
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


import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Comparator;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


/**
 * A class to connect to the server.
 *
 * @since 1.20
 *
 * @author Takahiko Kawasaki
 */
class SocketConnector
{
    private final SocketFactory mSocketFactory;
    private final Address mAddress;
    private final int mConnectionTimeout;
    private final int mSocketTimeout;
    private final String[] mServerNames;
    private final ProxyHandshaker mProxyHandshaker;
    private final SSLSocketFactory mSSLSocketFactory;
    private final String mHost;
    private final int mPort;
    private DualStackMode mDualStackMode = DualStackMode.BOTH;
    private int mDualStackFallbackDelay = 250;
    private boolean mVerifyHostname;
    private Socket mSocket;

    SocketConnector(SocketFactory socketFactory, Address address, int timeout, String[] serverNames, int socketTimeout)
    {
        this(socketFactory, address, timeout, socketTimeout, serverNames, null, null, null, 0);
    }


    SocketConnector(
            SocketFactory socketFactory, Address address, int timeout, int socketTimeout, String[] serverNames,
            ProxyHandshaker handshaker, SSLSocketFactory sslSocketFactory,
            String host, int port)
    {
        mSocketFactory     = socketFactory;
        mAddress           = address;
        mConnectionTimeout = timeout;
        mSocketTimeout     = socketTimeout;
        mServerNames       = serverNames;
        mProxyHandshaker   = handshaker;
        mSSLSocketFactory  = sslSocketFactory;
        mHost              = host;
        mPort              = port;
    }


    public int getConnectionTimeout()
    {
        return mConnectionTimeout;
    }


    public Socket getSocket()
    {
        return mSocket;
    }


    public Socket getConnectedSocket() throws WebSocketException
    {
        // Connect lazily.
        if (mSocket == null)
        {
            connectSocket();
        }

        return mSocket;
    }


    private void connectSocket() throws WebSocketException
    {
        // Create socket initiator.
        SocketInitiator socketInitiator = new SocketInitiator(
                mSocketFactory, mAddress, mConnectionTimeout, mServerNames,
                mDualStackMode, mDualStackFallbackDelay);

        // Resolve hostname to IP addresses
        InetAddress[] addresses = resolveHostname();

        // Let the sockets race until one has been established, following
        // RFC 6555 (*happy eyeballs*).
        try
        {
            mSocket = socketInitiator.establish(addresses);
        }
        catch (Exception e)
        {
            // True if a proxy server is set.
            boolean proxied = mProxyHandshaker != null;

            // Failed to connect the server.
            String message = String.format("Failed to connect to %s'%s': %s",
                    (proxied ? "the proxy " : ""), mAddress, e.getMessage());

            // Raise an exception with SOCKET_CONNECT_ERROR.
            throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, e);
        }
    }


    private InetAddress[] resolveHostname() throws WebSocketException
    {
        InetAddress[] addresses = null;
        UnknownHostException exception = null;

        try
        {
            // Resolve hostname to IP addresses.
            addresses = InetAddress.getAllByName(mAddress.getHostname());

            // Sort addresses: IPv6 first, then IPv4.
            Arrays.sort(addresses, new Comparator<InetAddress>() {
                public int compare(InetAddress left, InetAddress right) {
                    if (left.getClass() == right.getClass())
                    {
                        return 0;
                    }
                    if (left instanceof Inet6Address)
                    {
                        return -1;
                    }
                    else
                    {
                        return 1;
                    }
                }
            });
        }
        catch (UnknownHostException e)
        {
            exception = e;
        }

        // Return the ordered IP addresses (if any), otherwise raise the exception.
        if (addresses != null && addresses.length > 0)
        {
            return addresses;
        }

        if (exception == null)
        {
            exception = new UnknownHostException("No IP addresses found");
        }

        // Failed to resolve hostname to IP address.
        String message = String.format("Failed to resolve hostname %s: %s",
                mAddress, exception.getMessage());

        // Raise an exception with SOCKET_CONNECT_ERROR.
        throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, exception);
    }


    public Socket connect() throws WebSocketException
    {
        try
        {
            // Connect to the server (either a proxy or a WebSocket endpoint).
            doConnect();
            assert mSocket != null;
            return mSocket;
        }
        catch (WebSocketException e)
        {
            // Failed to connect the server.

            if (mSocket != null)
            {
                try
                {
                    // Close the socket.
                    mSocket.close();
                }
                catch (IOException ioe)
                {
                    // Ignore any error raised by close().
                }
            }

            throw e;
        }
    }


    SocketConnector setDualStackSettings(DualStackMode mode, int fallbackDelay)
    {
        mDualStackMode          = mode;
        mDualStackFallbackDelay = fallbackDelay;

        return this;
    }


    SocketConnector setVerifyHostname(boolean verifyHostname)
    {
        mVerifyHostname = verifyHostname;

        return this;
    }


    private void doConnect() throws WebSocketException
    {
        // True if a proxy server is set.
        boolean proxied = mProxyHandshaker != null;

        // Establish a socket associated to one of the resolved IP addresses
        connectSocket();
        assert mSocket != null;

        if (mSocketTimeout > 0)
        {
            // Set SO_TIMEOUT before starting to read/write anything
            setSoTimeout(mSocketTimeout);
        }

        if (mSocket instanceof SSLSocket)
        {
            // Verify that the hostname matches the certificate here since
            // this is not automatically done by the SSLSocket.
            verifyHostname((SSLSocket)mSocket, mAddress.getHostname());
        }

        // If a proxy server is set.
        if (proxied)
        {
            // Perform handshake with the proxy server.
            // SSL handshake is performed as necessary, too.
            handshake();
        }
    }


    private void setSoTimeout(int timeout) throws WebSocketException
    {
        // This should only be called when the socket is already connected
        assert mSocket != null;
        try
        {
            mSocket.setSoTimeout(timeout);
        }
        catch (SocketException e)
        {
            // For some reason we cannot set a timeout
            String message = String.format("Failed to set SO_TIMEOUT: %s",
                    e.getMessage());
            throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, e);
        }
    }


    private void verifyHostname(SSLSocket socket, String hostname) throws HostnameUnverifiedException
    {
        if (mVerifyHostname == false)
        {
            // Skip hostname verification.
            return;
        }

        // Hostname verifier.
        OkHostnameVerifier verifier = OkHostnameVerifier.INSTANCE;

        // The SSL session.
        SSLSession session = socket.getSession();

        // Verify the hostname.
        if (verifier.verify(hostname, session))
        {
            // Verified. No problem.
            return;
        }

        // The certificate of the peer does not match the expected hostname.
        throw new HostnameUnverifiedException(socket, hostname);
    }


    /**
     * Perform proxy handshake and optionally SSL handshake.
     */
    private void handshake() throws WebSocketException
    {
        // Sanity check
        assert mSocket != null;

        try
        {
            // Perform handshake with the proxy server.
            mProxyHandshaker.perform(mSocket);
        }
        catch (IOException e)
        {
            // Handshake with the proxy server failed.
            String message = String.format(
                "Handshake with the proxy server (%s) failed: %s", mAddress, e.getMessage());

            // Raise an exception with PROXY_HANDSHAKE_ERROR.
            throw new WebSocketException(WebSocketError.PROXY_HANDSHAKE_ERROR, message, e);
        }

        if (mSSLSocketFactory == null)
        {
            // SSL handshake with the WebSocket endpoint is not needed.
            return;
        }

        try
        {
            // Overlay the existing socket.
            mSocket = mSSLSocketFactory.createSocket(mSocket, mHost, mPort, true);
        }
        catch (IOException e)
        {
            // Failed to overlay an existing socket.
            String message = "Failed to overlay an existing socket: " + e.getMessage();

            // Raise an exception with SOCKET_OVERLAY_ERROR.
            throw new WebSocketException(WebSocketError.SOCKET_OVERLAY_ERROR, message, e);
        }

        try
        {
            // Start the SSL handshake manually. As for the reason, see
            // http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/samples/sockets/client/SSLSocketClient.java
            ((SSLSocket)mSocket).startHandshake();

            // Verify that the proxied hostname matches the certificate here since
            // this is not automatically done by the SSLSocket.
            verifyHostname((SSLSocket)mSocket, mProxyHandshaker.getProxiedHostname());
        }
        catch (IOException e)
        {
            // SSL handshake with the WebSocket endpoint failed.
            String message = String.format(
                "SSL handshake with the WebSocket endpoint (%s) failed: %s", mAddress, e.getMessage());

            // Raise an exception with SSL_HANDSHAKE_ERROR.
            throw new WebSocketException(WebSocketError.SSL_HANDSHAKE_ERROR, message, e);
        }
    }


    void closeSilently()
    {
        if (mSocket != null)
        {
            try
            {
                mSocket.close();
            }
            catch (Throwable t)
            {
                // Ignored.
            }
        }
    }
}
