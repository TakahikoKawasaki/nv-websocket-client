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
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.util.concurrent.CountDownLatch;


/**
 * A class to connect to the server.
 *
 * @since 1.20
 *
 * @author Takahiko Kawasaki
 */
class SocketConnector
{
    private Socket mSocket;
    private final Address mAddress;
    private final int mConnectionTimeout;
    private final SocketFactory mSocketFactory;
    private int mResolvesInProgress;
    private Exception mConnectException;
    private CountDownLatch mSocketConnectionCompleteEvent;

    SocketConnector(SocketFactory socketFactory, Address address, int timeout)
    {
        this.mSocketFactory = socketFactory;
        this.mAddress = address;
        this.mConnectionTimeout = timeout;
    }

    public Socket getSocket()
    {
        return mSocket;
    }


    public int getConnectionTimeout()
    {
        return mConnectionTimeout;
    }


    public void connect() throws WebSocketException
    {
        try
        {
            // Connect to the server (either a proxy or a WebSocket endpoint).
            doConnect();
        }
        catch (WebSocketException e)
        {
            // Failed to connect the server.

            try
            {
                // Close the socket.
                mSocket.close();
            }
            catch (IOException ioe)
            {
                // Ignore any error raised by close().
            }

            throw e;
        }
    }


    private void doConnect() throws WebSocketException
    {
        try
        {
            InetAddress[] inetAddresses = InetAddress.getAllByName(mAddress.getHostname());
            mResolvesInProgress = inetAddresses.length;

            mSocketConnectionCompleteEvent = new CountDownLatch(1);
            for (InetAddress inetAddress : inetAddresses) {
                new ConnectSocketThread(inetAddress).start();
            }
            mSocketConnectionCompleteEvent.await();

            if (mConnectException != null) {
                throw mConnectException;
            }

            if (mSocket instanceof SSLSocket)
            {
                // Verify that the hostname matches the certificate here since
                // this is not automatically done by the SSLSocket.
                verifyHostname((SSLSocket)mSocket, mAddress.getHostname());
            }
        }
        catch (Exception e)
        {
            // Failed to connect the server.
            String message = String.format("Failed to connect to %s: %s", mAddress, e.getMessage());

            // Raise an exception with SOCKET_CONNECT_ERROR.
            throw new WebSocketException(WebSocketError.SOCKET_CONNECT_ERROR, message, e);
        }
    }


    private void verifyHostname(SSLSocket socket, String hostname) throws HostnameUnverifiedException
    {
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

    void closeSilently()
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

    private class ConnectSocketThread extends Thread {
        InetAddress mInetAddress;

        ConnectSocketThread(InetAddress inetAddress) {
            this.mInetAddress = inetAddress;
        }

        public void run() {

            Socket socket = null;
            Exception exception = null;

            try {
                socket = mSocketFactory.createSocket();
                socket.connect(new InetSocketAddress(mInetAddress, mAddress.getPort()), mConnectionTimeout);
            } catch (Exception e) {
                exception = e;
            }

            synchronized (SocketConnector.this) {
                mResolvesInProgress -= 1;

                if (exception != null) {
                    if (mResolvesInProgress <= 0) {
                        mConnectException = exception;
                        mSocketConnectionCompleteEvent.countDown();
                    }

                    return;
                }

                if (mSocket != null) {
                    try {
                        socket.close();
                    } catch (Throwable e) {}

                    return;
                }

                mSocket = socket;
                mSocketConnectionCompleteEvent.countDown();
            }
        }
    }
}
