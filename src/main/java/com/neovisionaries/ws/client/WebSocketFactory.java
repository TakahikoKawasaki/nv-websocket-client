/*
 * Copyright (C) 2015 Neo Visionaries Inc.
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
import java.net.URI;
import java.net.URL;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


public class WebSocketFactory
{
    private SocketFactory mSocketFactory;
    private SSLSocketFactory mSSLSocketFactory;
    private SSLContext mSSLContext;


    public SocketFactory getSocketFactory()
    {
        return mSocketFactory;
    }


    public WebSocketFactory setSocketFactory(SocketFactory factory)
    {
        mSocketFactory = factory;

        return this;
    }


    public SSLSocketFactory getSSLSocketFactory()
    {
        return mSSLSocketFactory;
    }


    public WebSocketFactory setSSLSocketFactory(SSLSocketFactory factory)
    {
        mSSLSocketFactory = factory;

        return this;
    }


    public SSLContext getSSLContext()
    {
        return mSSLContext;
    }


    public WebSocketFactory setSSLContext(SSLContext context)
    {
        mSSLContext = context;

        return this;
    }


    public WebSocket createSocket(String uri) throws IOException
    {
        return createSocket(URI.create(uri));
    }


    public WebSocket createSocket(URI uri) throws IOException
    {
        if (uri == null)
        {
            throw new IllegalArgumentException("URI is not specified.");
        }

        // Split the URI.
        String scheme   = uri.getScheme();
        String userInfo = uri.getUserInfo();
        String host     = uri.getHost();
        int port        = uri.getPort();
        String path     = uri.getPath();

        return createSocket(scheme, userInfo, host, port, path);
    }


    public WebSocket createSocket(URL url) throws IOException
    {
        if (url == null)
        {
            throw new IllegalArgumentException("URL is not specified.");
        }

        // Split the URL.
        String scheme   = url.getProtocol();
        String userInfo = url.getUserInfo();
        String host     = url.getHost();
        int port        = url.getPort();
        String path     = url.getPath();

        return createSocket(scheme, userInfo, host, port, path);
    }


    public WebSocket createSocket(String scheme, String userInfo, String host, int port, String path) throws IOException
    {
        // True if 'scheme' is 'wss' or 'https'.
        boolean secure = isSecureConnectionRequired(scheme);

        // Check if 'host' is specified.
        if (host == null || host.length() == 0)
        {
            throw new IllegalArgumentException("Host is not specified.");
        }

        // Determine the port number.
        port = determinePort(port, secure);

        // Determine the path.
        path = determinePath(path);

        // Create a Socket instance.
        Socket socket = createRawSocket(host, port, secure);

        // Create a WebSocket instance.
        return new WebSocket(userInfo, host, path, socket);
    }



    private static boolean isSecureConnectionRequired(String scheme)
    {
        if (scheme == null || scheme.length() == 0)
        {
            throw new IllegalArgumentException("Scheme is not specified.");
        }

        if ("wss".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
        {
            return true;
        }

        if ("ws".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
        {
            return false;
        }

        throw new IllegalArgumentException("Bad scheme: " + scheme);
    }


    private static int determinePort(int port, boolean secure)
    {
        if (0 <= port)
        {
            return port;
        }

        if (secure)
        {
            return 443;
        }
        else
        {
            return 80;
        }
    }


    private static String determinePath(String path)
    {
        if (path == null || path.length() == 0)
        {
            return "/";
        }

        if (path.startsWith("/"))
        {
            return path;
        }
        else
        {
            return "/" + path;
        }
    }


    private Socket createRawSocket(String host, int port, boolean secure) throws IOException
    {
        SocketFactory factory = determineSocketFactory(secure);

        return factory.createSocket(host, port);
    }


    private SocketFactory determineSocketFactory(boolean secure)
    {
        if (secure)
        {
            if (mSSLContext != null)
            {
                return mSSLContext.getSocketFactory();
            }

            if (mSSLSocketFactory != null)
            {
                return mSSLSocketFactory;
            }

            return SSLSocketFactory.getDefault();
        }

        if (mSocketFactory != null)
        {
            return mSocketFactory;
        }

        return SocketFactory.getDefault();
    }
}
