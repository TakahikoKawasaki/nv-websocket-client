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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


class HandshakeBuilder
{
    private static final String RN = "\r\n";
    private boolean mSecure;
    private String mUserInfo;
    private final String mHost;
    private final String mPath;
    private final URI mUri;
    private String mKey;
    private Set<String> mProtocols;
    private List<WebSocketExtension> mExtensions;
    private List<String[]> mHeaders;


    public HandshakeBuilder(boolean secure, String userInfo, String host, String path)
    {
        mSecure   = secure;
        mUserInfo = userInfo;
        mHost     = host;
        mPath     = path;

        // 'host' may contain ':{port}' at its end.
        // 'path' may contain '?{query}' at its end.
        mUri = URI.create(String.format("%s://%s%s",
            (secure ? "wss" : "ws"), host, path));
    }


    public HandshakeBuilder(HandshakeBuilder source)
    {
        mSecure     = source.mSecure;
        mUserInfo   = source.mUserInfo;
        mHost       = source.mHost;
        mPath       = source.mPath;
        mUri        = source.mUri;
        mKey        = source.mKey;
        mProtocols  = copyProtocols(source.mProtocols);
        mExtensions = copyExtensions(source.mExtensions);
        mHeaders    = copyHeaders(source.mHeaders);
    }


    public void addProtocol(String protocol)
    {
        if (isValidProtocol(protocol) == false)
        {
            throw new IllegalArgumentException("'protocol' must be a non-empty string with characters in the range U+0021 to U+007E not including separator characters.");
        }

        if (mProtocols == null)
        {
            // 'LinkedHashSet' is used because the elements
            // "MUST all be unique strings" and must be
            // "ordered by preference. See RFC 6455, p18, 10.
            mProtocols = new LinkedHashSet<String>();
        }

        mProtocols.add(protocol);
    }


    private static boolean isValidProtocol(String protocol)
    {
        if (protocol == null || protocol.length() == 0)
        {
            return false;
        }

        int len = protocol.length();

        for (int i = 0; i < len; ++i)
        {
            char ch = protocol.charAt(i);

            if (ch < 0x21 || 0x7E < ch || Token.isSeparator(ch))
            {
                return false;
            }
        }

        return true;
    }


    public boolean containsProtocol(String protocol)
    {
        if (mProtocols == null)
        {
            return false;
        }

        return mProtocols.contains(protocol);
    }


    public void addExtension(WebSocketExtension extension)
    {
        if (extension == null)
        {
            return;
        }

        if (mExtensions == null)
        {
            mExtensions = new ArrayList<WebSocketExtension>();
        }

        mExtensions.add(extension);
    }


    public boolean containsExtension(String extensionName)
    {
        if (mExtensions == null)
        {
            return false;
        }

        for (WebSocketExtension extension : mExtensions)
        {
            if (extension.getName().equals(extensionName))
            {
                return true;
            }
        }

        return false;
    }


    public void addHeader(String name, String value)
    {
        if (name == null || name.length() == 0)
        {
            return;
        }

        if (value == null)
        {
            value = "";
        }

        if (mHeaders == null)
        {
            mHeaders = new ArrayList<String[]>();
        }

        mHeaders.add(new String[] { name, value });
    }


    public void setUserInfo(String userInfo)
    {
        mUserInfo = userInfo;
    }


    public void setUserInfo(String id, String password)
    {
        if (id == null)
        {
            id = "";
        }

        if (password == null)
        {
            password = "";
        }

        String userInfo = String.format("%s:%s", id, password);

        setUserInfo(userInfo);
    }


    public URI getURI()
    {
        return mUri;
    }


    public void setKey(String key)
    {
        mKey = key;
    }


    public String build()
    {
        StringBuilder builder = new StringBuilder()
            .append("GET ").append(mPath).append(" HTTP/1.1").append(RN)
            .append("Host: ").append(mHost).append(RN)
            .append("Connection: Upgrade").append(RN)
            .append("Upgrade: websocket").append(RN)
            .append("Sec-WebSocket-Version: 13").append(RN)
            .append("Sec-WebSocket-Key: ").append(mKey).append(RN);

        append(builder, "Sec-WebSocket-Protocol", mProtocols);
        append(builder, "Sec-WebSocket-Extensions", mExtensions);
        append(builder, mHeaders);

        if (mUserInfo != null && mUserInfo.length() != 0)
        {
            builder
                .append("Authorization: Basic ")
                .append(Base64.encode(mUserInfo))
                .append(RN);
        }

        return builder.append(RN).toString();
    }


    private static void append(StringBuilder builder, String name, Collection<?> values)
    {
        if (values == null || values.size() == 0)
        {
            return;
        }

        builder.append(name).append(": ");

        join(builder, values, ", ");

        builder.append(RN);
    }


    private static void join(StringBuilder builder, Collection<?> values, String delimiter)
    {
        boolean first = true;

        for (Object value : values)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                builder.append(delimiter);
            }

            builder.append(value.toString());
        }
    }


    private static void append(StringBuilder builder, List<String[]> pairs)
    {
        if (pairs == null || pairs.size() == 0)
        {
            return;
        }

        for (String[] pair : pairs)
        {
            builder.append(pair[0]).append(": ").append(pair[1]).append(RN);
        }
    }


    private static Set<String> copyProtocols(Set<String> protocols)
    {
        if (protocols == null)
        {
            return null;
        }

        Set<String> newProtocols = new LinkedHashSet<String>(protocols.size());

        newProtocols.addAll(protocols);

        return newProtocols;
    }


    private static List<WebSocketExtension> copyExtensions(List<WebSocketExtension> extensions)
    {
        if (extensions == null)
        {
            return null;
        }

        List<WebSocketExtension> newExtensions =
            new ArrayList<WebSocketExtension>(extensions.size());

        for (WebSocketExtension extension : extensions)
        {
            newExtensions.add(new WebSocketExtension(extension));
        }

        return newExtensions;
    }


    private static List<String[]> copyHeaders(List<String[]> headers)
    {
        if (headers == null)
        {
            return null;
        }

        List<String[]> newHeaders = new ArrayList<String[]>(headers.size());

        for (String[] header : headers)
        {
            newHeaders.add(copyHeader(header));
        }

        return newHeaders;
    }


    private static String[] copyHeader(String[] header)
    {
        String[] newHeader = new String[2];

        newHeader[0] = header[0];
        newHeader[1] = header[1];

        return newHeader;
    }
}
