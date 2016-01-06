/*
 * Copyright (C) 2016 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.neovisionaries.ws.client;


import java.util.List;
import java.util.Map;


/**
 * An exception raised due to a violation against the WebSocket protocol.
 *
 * @since 1.19
 */
public class OpeningHandshakeException extends WebSocketException
{
    private static final long serialVersionUID = 1L;


    private final StatusLine mStatusLine;
    private final Map<String, List<String>> mHeaders;
    private final byte[] mBody;


    OpeningHandshakeException(
            WebSocketError error, String message,
            StatusLine statusLine, Map<String, List<String>> headers)
    {
        this(error, message, statusLine, headers, null);
    }


    OpeningHandshakeException(
            WebSocketError error, String message,
            StatusLine statusLine, Map<String, List<String>> headers, byte[] body)
    {
        super(error, message);

        mStatusLine = statusLine;
        mHeaders    = headers;
        mBody       = body;
    }


    /**
     * Get the status line contained in the WebSocket opening handshake
     * response from the server.
     *
     * @return
     *         The status line.
     */
    public StatusLine getStatusLine()
    {
        return mStatusLine;
    }


    /**
     * Get the HTTP headers contained in the WebSocket opening handshake
     * response from the server.
     *
     * @return
     *         The HTTP headers. The returned map is an instance of
     *         {@link java.util.TreeMap TreeMap} with {@link
     *         String#CASE_INSENSITIVE_ORDER} comparator.
     */
    public Map<String, List<String>> getHeaders()
    {
        return mHeaders;
    }


    /**
     * Get the response body contained in the WebSocket opening handshake
     * response from the server.
     *
     * <p>
     * This method returns a non-null value only when (1) the status code
     * is not 101 (Switching Protocols), (2) the response from the server
     * has a response body, (3) the response has "Content-Length" header,
     * and (4) no error occurred during reading the response body. In other
     * cases, this method returns {@code null}.
     * </p>
     *
     * @return
     *         The response body.
     */
    public byte[] getBody()
    {
        return mBody;
    }
}
