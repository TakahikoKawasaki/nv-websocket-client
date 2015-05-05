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


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


/**
 * Proxy settings.
 *
 * <p>
 * If a proxy server's host name is set (= if {@link #getHost()}
 * returns a non-null value), a socket factory that creates a
 * socket to communicate with the proxy server is selected based
 * on the settings of this {@code ProxySettings} instance. The
 * following is the concrete flow to select a socket factory.
 * </p>
 *
 * <blockquote>
 * <ol>
 * <li>
 *   If {@link #isSecure()} returns {@code true},
 *   <ol type="i">
 *     <li>
 *       If an {@link SSLContext} instance has been set by {@link
 *       #setSSLContext(SSLContext)}, the value returned from {@link
 *       SSLContext#getSocketFactory()} method of the instance is used.
 *     <li>
 *       Otherwise, if an {@link SSLSocketFactory} instance has been
 *       set by {@link #setSSLSocketFactory(SSLSocketFactory)}, the
 *       instance is used.
 *     <li>
 *       Otherwise, the value returned from {@link SSLSocketFactory#getDefault()}
 *       is used.
 *   </ol>
 * <li>
 *   Otherwise (= {@link #isSecure()} returns {@code false}),
 *   <ol type="i">
 *     <li>
 *       If a {@link SocketFactory} instance has been set by {@link
 *       #setSocketFactory(SocketFactory)}, the instance is used.
 *     <li>
 *       Otherwise, the value returned from {@link SocketFactory#getDefault()}
 *       is used.
 *   </ol>
 * </ol>
 * </blockquote>
 *
 * <p>
 * Note that the current implementation supports only Basic Authentication
 * for authentication at the proxy server.
 * </p>
 *
 * @see WebSocketFactory#getProxySettings()
 *
 * @since 1.3
 */
public class ProxySettings
{
    private final WebSocketFactory mWebSocketFactory;
    private final Map<String, List<String>> mHeaders;
    private final SocketFactorySettings mSocketFactorySettings;
    private boolean mSecure;
    private String mHost;
    private int mPort;
    private String mId;
    private String mPassword;


    ProxySettings(WebSocketFactory factory)
    {
        mWebSocketFactory = factory;
        mHeaders = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        mSocketFactorySettings = new SocketFactorySettings();

        reset();
    }


    /**
     * Get the associated {@link WebSocketFactory} instance.
     */
    public WebSocketFactory getWebSocketFactory()
    {
        return mWebSocketFactory;
    }


    /**
     * Reset the proxy settings. To be concrete, parameter values are
     * set as shown below.
     *
     * <blockquote>
     * <table border="1" cellpadding="5" style="border-collapse: collapse;">
     *   <thead>
     *     <tr>
     *       <th>Name</th>
     *       <th>Value</th>
     *       <th>Description</th>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>Secure</td>
     *       <td>{@code false}</td>
     *       <td>Use TLS to connect to the proxy server or not.</td>
     *     </tr>
     *     <tr>
     *       <td>Host</td>
     *       <td>{@code null}</td>
     *       <td>The host name of the proxy server.</td>
     *     </tr>
     *     <tr>
     *       <td>Port</td>
     *       <td>{@code -1}</td>
     *       <td>The port number of the proxy server.</td>
     *     </tr>
     *     <tr>
     *       <td>ID</td>
     *       <td>{@code null}</td>
     *       <td>The ID for authentication at the proxy server.</td>
     *     </tr>
     *     <tr>
     *       <td>Password</td>
     *       <td>{@code null}</td>
     *       <td>The password for authentication at the proxy server.</td>
     *     </tr>
     *     <tr>
     *       <td>Headers</td>
     *       <td>Cleared</td>
     *       <td>Additional HTTP headers passed to the proxy server.</td>
     *     </tr>
     *   </tbody>
     * </table>
     * </blockquote>
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings reset()
    {
        mSecure   = false;
        mHost     = null;
        mPort     = -1;
        mId       = null;
        mPassword = null;
        mHeaders.clear();

        return this;
    }


    /**
     * Check whether use of TLS is enabled or disabled.
     *
     * @return
     *         {@code true} if TLS is used in the communication with
     *         the proxy server.
     */
    public boolean isSecure()
    {
        return mSecure;
    }


    /**
     * Enable or disable use of TLS.
     *
     * @param secure
     *         {@code true} to use TLS in the communication with
     *         the proxy server.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setSecure(boolean secure)
    {
        mSecure = secure;

        return this;
    }


    /**
     * Get the host name of the proxy server.
     *
     * <p>
     * The default value is {@code null}. If this method returns
     * a non-null value, it is used as the proxy server.
     * </p>
     *
     * @return
     *         The host name of the proxy server.
     */
    public String getHost()
    {
        return mHost;
    }


    /**
     * Set the host name of the proxy server.
     *
     * <p>
     * If a non-null value is set, it is used as the proxy server.
     * </p>
     *
     * @param host
     *         The host name of the proxy server.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setHost(String host)
    {
        mHost = host;

        return this;
    }


    /**
     * Get the port number of the proxy server.
     *
     * <p>
     * The default value is {@code -1}. {@code -1} means that
     * the default port number ({@code 80} for non-secure
     * connections and {@code 443} for secure connections)
     * should be used.
     * </p>
     *
     * @return
     *         The port number of the proxy server.
     */
    public int getPort()
    {
        return mPort;
    }


    /**
     * Set the port number of the proxy server.
     *
     * <p>
     * If {@code -1} is set, the default port number ({@code 80}
     * for non-secure connections and {@code 443} for secure
     * connections) is used.
     * </p>
     *
     * @param port
     *         The port number of the proxy server.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setPort(int port)
    {
        mPort = port;

        return this;
    }


    /**
     * Get the ID for authentication at the proxy server.
     *
     * <p>
     * The default value is {@code null}. If this method returns
     * a non-null value, it is used as the ID for authentication
     * at the proxy server. To be concrete, the value is used to
     * generate the value of <code><a href=
     * "http://tools.ietf.org/html/rfc2616#section-14.34"
     * >Proxy-Authorization</a></code> header.
     * </p>
     *
     * @return
     *         The ID for authentication at the proxy server.
     */
    public String getId()
    {
        return mId;
    }


    /**
     * Set the ID for authentication at the proxy server.
     *
     * <p>
     * If a non-null value is set, it is used as the ID for
     * authentication at the proxy server. To be concrete, the
     * value is used to generate the value of <code><a href=
     * "http://tools.ietf.org/html/rfc2616#section-14.34"
     * >Proxy-Authorization</a></code> header.
     * </p>
     *
     * @param id
     *         The ID for authentication at the proxy server.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setId(String id)
    {
        mId = id;

        return this;
    }


    /**
     * Get the password for authentication at the proxy server.
     *
     * @return
     *         The password for authentication at the proxy server.
     */
    public String getPassword()
    {
        return mPassword;
    }


    /**
     * Set the password for authentication at the proxy server.
     *
     * @param password
     *         The password for authentication at the proxy server.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setPassword(String password)
    {
        mPassword = password;

        return this;
    }


    /**
     * Set credentials for authentication at the proxy server.
     * This method is an alias of {@link #setId(String) setId}{@code
     * (id).}{@link #setPassword(String) setPassword}{@code
     * (password)}.
     *
     * @param id
     *         The ID.
     *
     * @param password
     *         The password.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setCredentials(String id, String password)
    {
        return setId(id).setPassword(password);
    }


    /**
     * Set the proxy server by a URI. See the description of
     * {@link #setServer(URI)} about how the parameters are updated.
     *
     * @param uri
     *         The URI of the proxy server. If {@code null} is given,
     *         none of the parameters are updated.
     *
     * @return
     *         {@code this} object.
     *
     * @throws IllegalArgumentException
     *         Failed to convert the given string to a {@link URI} instance.
     */
    public ProxySettings setServer(String uri)
    {
        if (uri == null)
        {
            return this;
        }

        return setServer(URI.create(uri));
    }


    /**
     * Set the proxy server by a URL. See the description of
     * {@link #setServer(URI)} about how the parameters are updated.
     *
     * @param url
     *         The URL of the proxy server. If {@code null} is given,
     *         none of the parameters are updated.
     *
     * @return
     *         {@code this} object.
     *
     * @throws IllegalArgumentException
     *         Failed to convert the given URL to a {@link URI} instance.
     */
    public ProxySettings setServer(URL url)
    {
        if (url == null)
        {
            return this;
        }

        try
        {
            return setServer(url.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Set the proxy server by a URI. The parameters are updated as
     * described below.
     *
     * <blockquote>
     * <dl>
     *   <dt>Secure</dt>
     *   <dd><p>
     *     If the URI contains the scheme part and its value is
     *     either {@code "http"} or {@code "https"} (case-insensitive),
     *     the {@code secure} parameter is updated to {@code false}
     *     or to {@code true} accordingly. In other cases, the parameter
     *     is not updated.
     *   </p></dd>
     *   <dt>ID &amp; Password</dt>
     *   <dd><p>
     *     If the URI contains the userinfo part and the ID embedded
     *     in the userinfo part is not an empty string, the {@code
     *     id} parameter and the {@code password} parameter are updated
     *     accordingly. In other cases, the parameters are not updated.
     *   </p></dd>
     *   <dt>Host</dt>
     *   <dd><p>
     *     The {@code host} parameter is always updated by the given URI.
     *   </p></dd>
     *   <dt>Port</dt>
     *   <dd><p>
     *     The {@code port} parameter is always updated by the given URI.
     *   </p></dd>
     * </dl>
     * </blockquote>
     *
     * @param uri
     *         The URI of the proxy server. If {@code null} is given,
     *         none of the parameters is updated.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings setServer(URI uri)
    {
        if (uri == null)
        {
            return this;
        }

        String scheme   = uri.getScheme();
        String userInfo = uri.getUserInfo();
        String host     = uri.getHost();
        int port        = uri.getPort();

        return setServer(scheme, userInfo, host, port);
    }


    private ProxySettings setServer(String scheme, String userInfo, String host, int port)
    {
        setByScheme(scheme);
        setByUserInfo(userInfo);
        mHost = host;
        mPort = port;

        return this;
    }


    private void setByScheme(String scheme)
    {
        if ("http".equalsIgnoreCase(scheme))
        {
            mSecure = false;
        }
        else if ("https".equalsIgnoreCase(scheme))
        {
            mSecure = true;
        }
    }


    private void setByUserInfo(String userInfo)
    {
        if (userInfo == null)
        {
            return;
        }

        String[] pair = userInfo.split(":", 2);
        String id;
        String pw;

        switch (pair.length)
        {
            case 2:
                id = pair[0];
                pw = pair[1];
                break;

            case 1:
                id = pair[0];
                pw = null;
                break;

            default:
                return;
        }

        if (id.length() == 0)
        {
            return;
        }

        mId       = id;
        mPassword = pw;
    }


    /**
     * Get additional HTTP headers passed to the proxy server.
     *
     * @return
     *         Additional HTTP headers passed to the proxy server.
     *         The comparator of the returned map is {@link
     *         String#CASE_INSENSITIVE_ORDER}.
     */
    public Map<String, List<String>> getHeaders()
    {
        return mHeaders;
    }


    /**
     * Add an additional HTTP header passed to the proxy server.
     *
     * @param name
     *         The name of an HTTP header (case-insensitive).
     *         If {@code null} or an empty string is given,
     *         nothing is added.
     *
     * @param value
     *         The value of the HTTP header.
     *
     * @return
     *         {@code this} object.
     */
    public ProxySettings addHeader(String name, String value)
    {
        if (name == null || name.length() == 0)
        {
            return this;
        }

        List<String> list = mHeaders.get(name);

        if (list == null)
        {
            list = new ArrayList<String>();
            mHeaders.put(name, list);
        }

        list.add(value);

        return this;
    }


    /**
     * Get the socket factory that has been set by {@link
     * #setSocketFactory(SocketFactory)}.
     *
     * @return
     *         The socket factory.
     */
    public SocketFactory getSocketFactory()
    {
        return mSocketFactorySettings.getSocketFactory();
    }


    /**
     * Set a socket factory.
     *
     * @param factory
     *         A socket factory.
     *
     * @return
     *         {@code this} instance.
     */
    public ProxySettings setSocketFactory(SocketFactory factory)
    {
        mSocketFactorySettings.setSocketFactory(factory);

        return this;
    }


    /**
     * Get the SSL socket factory that has been set by {@link
     * #setSSLSocketFactory(SSLSocketFactory)}.
     *
     * @return
     *         The SSL socket factory.
     */
    public SSLSocketFactory getSSLSocketFactory()
    {
        return mSocketFactorySettings.getSSLSocketFactory();
    }


    /**
     * Set an SSL socket factory.
     *
     * @param factory
     *         An SSL socket factory.
     *
     * @return
     *         {@code this} instance.
     */
    public ProxySettings setSSLSocketFactory(SSLSocketFactory factory)
    {
        mSocketFactorySettings.setSSLSocketFactory(factory);

        return this;
    }


    /**
     * Get the SSL context that has been set by {@link #setSSLContext(SSLContext)}.
     *
     * @return
     *         The SSL context.
     */
    public SSLContext getSSLContext()
    {
        return mSocketFactorySettings.getSSLContext();
    }


    /**
     * Set an SSL context to get a socket factory.
     *
     * @param context
     *         An SSL context.
     *
     * @return
     *         {@code this} instance.
     */
    public ProxySettings setSSLContext(SSLContext context)
    {
        mSocketFactorySettings.setSSLContext(context);

        return this;
    }


    SocketFactory selectSocketFactory()
    {
        return mSocketFactorySettings.selectSocketFactory(mSecure);
    }
}
