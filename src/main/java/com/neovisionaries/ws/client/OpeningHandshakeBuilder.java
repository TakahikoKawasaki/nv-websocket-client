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


import java.util.ArrayList;
import java.util.List;


class OpeningHandshakeBuilder
{
    private static final String RN = "\r\n";
    private final String mUserInfo;
    private final String mHost;
    private final String mPath;
    private String mKey;
    private List<String> mProtocols;
    private List<String> mExtensions;
    private List<String[]> mHeaders;


    public OpeningHandshakeBuilder(String userInfo, String host, String path)
    {
        mUserInfo = userInfo;
        mHost     = host;
        mPath     = path;
    }


    public void addProtocol(String protocol)
    {
        if (protocol == null || protocol.length() == 0)
        {
            return;
        }

        if (mProtocols == null)
        {
            mProtocols = new ArrayList<String>();
        }

        mProtocols.add(protocol);
    }


    public void addExtension(String extension)
    {
        if (extension == null || extension.length() == 0)
        {
            return;
        }

        if (mExtensions == null)
        {
            mExtensions = new ArrayList<String>();
        }

        mExtensions.add(extension);
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

        append(builder, "Sec-WebSocket-Procotol", mProtocols);
        append(builder, "Sec-WebSocket-Extension", mExtensions);
        append(builder, mHeaders);

        if (mUserInfo != null && mUserInfo.length() != 0)
        {
            builder
                .append("Authorization: ")
                .append(Base64.encode(mUserInfo))
                .append(RN);
        }

        return builder.append(RN).toString();
    }


    private static void append(StringBuilder builder, String name, List<String> values)
    {
        if (values == null || values.size() == 0)
        {
            return;
        }

        builder.append(name).append(": ");

        join(builder, values, ", ");

        builder.append(RN);
    }


    private static void join(StringBuilder builder, List<String> values, String delimiter)
    {
        int size = values.size();

        for (int i = 0; i < size; ++i)
        {
            if (i != 0)
            {
                builder.append(delimiter);
            }

            builder.append(values.get(i));
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
}
