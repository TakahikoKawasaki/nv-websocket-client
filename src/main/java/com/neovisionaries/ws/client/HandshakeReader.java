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


import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Reader for a WebSocket opening handshake response.
 *
 * @since 1.19
 */
class HandshakeReader
{
    private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final WebSocket mWebSocket;


    public HandshakeReader(WebSocket websocket)
    {
        mWebSocket = websocket;
    }


    public Map<String, List<String>> readHandshake(WebSocketInputStream input, String key) throws WebSocketException
    {
        // Read the status line.
        StatusLine statusLine = readStatusLine(input);

        // Read HTTP headers.
        Map<String, List<String>> headers = readHttpHeaders(input);

        // Validate the status line.
        validateStatusLine(statusLine, headers, input);

        // Validate the value of Upgrade.
        validateUpgrade(statusLine, headers);

        // Validate the value of Connection.
        validateConnection(statusLine, headers);

        // Validate the value of Sec-WebSocket-Accept.
        validateAccept(statusLine, headers, key);

        // Validate the value of Sec-WebSocket-Extensions.
        validateExtensions(statusLine, headers);

        // Validate the value of Sec-WebSocket-Protocol.
        validateProtocol(statusLine, headers);

        // OK. The server has accepted the web socket request.
        return headers;
    }


    /**
     * Read a status line from an HTTP server.
     */
    private StatusLine readStatusLine(WebSocketInputStream input) throws WebSocketException
    {
        String line;

        try
        {
            // Read the status line.
            line = input.readLine();
        }
        catch (IOException e)
        {
            // Failed to read an opening handshake response from the server.
            throw new WebSocketException(
                WebSocketError.OPENING_HANDSHAKE_RESPONSE_FAILURE,
                "Failed to read an opening handshake response from the server: " + e.getMessage(), e);
        }

        if (line == null || line.length() == 0)
        {
            // The status line of the opening handshake response is empty.
            throw new WebSocketException(
                WebSocketError.STATUS_LINE_EMPTY,
                "The status line of the opening handshake response is empty.");
        }

        try
        {
            // Parse the status line.
            return new StatusLine(line);
        }
        catch (Exception e)
        {
            // The status line of the opening handshake response is badly formatted.
            throw new WebSocketException(
                WebSocketError.STATUS_LINE_BAD_FORMAT,
                "The status line of the opening handshake response is badly formatted. The status line is: " + line);
        }
    }


    private Map<String, List<String>> readHttpHeaders(WebSocketInputStream input) throws WebSocketException
    {
        // Create a map of HTTP headers. Keys are case-insensitive.
        Map<String, List<String>> headers =
            new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);

        StringBuilder builder = null;
        String line;

        while (true)
        {
            try
            {
                line = input.readLine();
            }
            catch (IOException e)
            {
                // An error occurred while HTTP header section was being read.
                throw new WebSocketException(
                    WebSocketError.HTTP_HEADER_FAILURE,
                    "An error occurred while HTTP header section was being read: " + e.getMessage(), e);
            }

            // If the end of the header section was reached.
            if (line == null || line.length() == 0)
            {
                if (builder != null)
                {
                    parseHttpHeader(headers, builder.toString());
                }

                // The end of the header section.
                break;
            }

            // The first line of the line.
            char ch = line.charAt(0);

            // If the first char is SP or HT.
            if (ch == ' ' || ch == '\t')
            {
                if (builder == null)
                {
                    // Weird. No preceding "field-name:field-value" line. Ignore this line.
                    continue;
                }

                // Replacing the leading 1*(SP|HT) to a single SP.
                line = line.replaceAll("^[ \t]+", " ");

                // Concatenate
                builder.append(line);

                continue;
            }

            if (builder != null)
            {
                parseHttpHeader(headers, builder.toString());
            }

            builder = new StringBuilder(line);
        }

        return headers;
    }


    private void parseHttpHeader(Map<String, List<String>> headers, String header)
    {
        // Split 'header' to name & value.
        String[] pair = header.split(":", 2);

        if (pair.length < 2)
        {
            // Weird. Ignore this header.
            return;
        }

        // Name. (Remove leading and trailing spaces)
        String name = pair[0].trim();

        // Value. (Remove leading and trailing spaces)
        String value = pair[1].trim();

        List<String> list = headers.get(name);

        if (list == null)
        {
            list = new ArrayList<String>();
            headers.put(name, list);
        }

        list.add(value);
    }


    /**
     * Validate the status line. {@code "101 Switching Protocols"} is expected.
     */
    private void validateStatusLine(StatusLine statusLine, Map<String, List<String>> headers, WebSocketInputStream input) throws WebSocketException
    {
        // If the status code is 101 (Switching Protocols).
        if (statusLine.getStatusCode() == 101)
        {
            // OK. The server can speak the WebSocket protocol.
            return;
        }

        // Read the response body.
        byte[] body = readBody(headers, input);

        // The status code of the opening handshake response is not Switching Protocols.
        throw new OpeningHandshakeException(
            WebSocketError.NOT_SWITCHING_PROTOCOLS,
            "The status code of the opening handshake response is not '101 Switching Protocols'. The status line is: " + statusLine,
            statusLine, headers, body);
    }


    /**
     * Read the response body
     */
    private byte[] readBody(Map<String, List<String>> headers, WebSocketInputStream input)
    {
        // Get the value of "Content-Length" header.
        int length = getContentLength(headers);

        if (length <= 0)
        {
            // Response body is not available.
            return null;
        }

        try
        {
            // Allocate a byte array of the content length.
            byte[] body = new byte[length];

            // Read the response body into the byte array.
            input.readBytes(body, length);

            // Return the content of the response body.
            return body;
        }
        catch (Throwable t)
        {
            // Response body is not available.
            return null;
        }
    }


    /**
     * Get the value of "Content-Length" header.
     */
    private int getContentLength(Map<String, List<String>> headers)
    {
        try
        {
            return Integer.parseInt(headers.get("Content-Length").get(0));
        }
        catch (Exception e)
        {
            return -1;
        }
    }


    /**
     * Validate the value of {@code Upgrade} header.
     *
     * <blockquote>
     * <p>From RFC 6455, p19.</p>
     * <p><i>
     * If the response lacks an {@code Upgrade} header field or the {@code Upgrade}
     * header field contains a value that is not an ASCII case-insensitive match for
     * the value "websocket", the client MUST Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void validateUpgrade(StatusLine statusLine, Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Upgrade.
        List<String> values = headers.get("Upgrade");

        if (values == null || values.size() == 0)
        {
            // The opening handshake response does not contain 'Upgrade' header.
            throw new OpeningHandshakeException(
                WebSocketError.NO_UPGRADE_HEADER,
                "The opening handshake response does not contain 'Upgrade' header.",
                statusLine, headers);
        }

        for (String value : values)
        {
            // Split the value of Upgrade header into elements.
            String[] elements = value.split("\\s*,\\s*");

            for (String element : elements)
            {
                if ("websocket".equalsIgnoreCase(element))
                {
                    // Found 'websocket' in Upgrade header.
                    return;
                }
            }
        }

        // 'websocket' was not found in 'Upgrade' header.
        throw new OpeningHandshakeException(
            WebSocketError.NO_WEBSOCKET_IN_UPGRADE_HEADER,
            "'websocket' was not found in 'Upgrade' header.",
            statusLine, headers);
    }


    /**
     * Validate the value of {@code Connection} header.
     *
     * <blockquote>
     * <p>From RFC 6455, p19.</p>
     * <p><i>
     * If the response lacks a {@code Connection} header field or the {@code Connection}
     * header field doesn't contain a token that is an ASCII case-insensitive match
     * for the value "Upgrade", the client MUST Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void validateConnection(StatusLine statusLine, Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Upgrade.
        List<String> values = headers.get("Connection");

        if (values == null || values.size() == 0)
        {
            // The opening handshake response does not contain 'Connection' header.
            throw new OpeningHandshakeException(
                WebSocketError.NO_CONNECTION_HEADER,
                "The opening handshake response does not contain 'Connection' header.",
                statusLine, headers);
        }

        for (String value : values)
        {
            // Split the value of Connection header into elements.
            String[] elements = value.split("\\s*,\\s*");

            for (String element : elements)
            {
                if ("Upgrade".equalsIgnoreCase(element))
                {
                    // Found 'Upgrade' in Connection header.
                    return;
                }
            }
        }

        // 'Upgrade' was not found in 'Connection' header.
        throw new OpeningHandshakeException(
            WebSocketError.NO_UPGRADE_IN_CONNECTION_HEADER,
            "'Upgrade' was not found in 'Connection' header.",
            statusLine, headers);
    }


    /**
     * Validate the value of {@code Sec-WebSocket-Accept} header.
     *
     * <blockquote>
     * <p>From RFC 6455, p19.</p>
     * <p><i>
     * If the response lacks a {@code Sec-WebSocket-Accept} header field or the
     * {@code Sec-WebSocket-Accept} contains a value other than the base64-encoded
     * SHA-1 of the concatenation of the {@code Sec-WebSocket-Key} (as a string,
     * not base64-decoded) with the string "{@code 258EAFA5-E914-47DA-95CA-C5AB0DC85B11}"
     * but ignoring any leading and trailing whitespace, the client MUST Fail the
     * WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void validateAccept(StatusLine statusLine, Map<String, List<String>> headers, String key) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Accept.
        List<String> values = headers.get("Sec-WebSocket-Accept");

        if (values == null)
        {
            // The opening handshake response does not contain 'Sec-WebSocket-Accept' header.
            throw new OpeningHandshakeException(
                WebSocketError.NO_SEC_WEBSOCKET_ACCEPT_HEADER,
                "The opening handshake response does not contain 'Sec-WebSocket-Accept' header.",
                statusLine, headers);
        }

        // The actual value of Sec-WebSocket-Accept.
        String actual = values.get(0);

        // Concatenate.
        String input = key + ACCEPT_MAGIC;

        // Expected value of Sec-WebSocket-Accept
        String expected;

        try
        {
            // Message digest for SHA-1.
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // Compute the digest value.
            byte[] digest = md.digest(Misc.getBytesUTF8(input));

            // Base64.
            expected = Base64.encode(digest);
        }
        catch (Exception e)
        {
            // This never happens.
            return;
        }

        if (expected.equals(actual) == false)
        {
            // The value of 'Sec-WebSocket-Accept' header is different from the expected one.
            throw new OpeningHandshakeException(
                WebSocketError.UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,
                "The value of 'Sec-WebSocket-Accept' header is different from the expected one.",
                statusLine, headers);
        }

        // OK. The value of Sec-WebSocket-Accept is the same as the expected one.
    }


    /**
     * Validate the value of {@code Sec-WebSocket-Extensions} header.
     *
     * <blockquote>
     * <p>From RFC 6455, p19.</p>
     * <p><i>
     * If the response includes a {@code Sec-WebSocket-Extensions} header
     * field and this header field indicates the use of an extension
     * that was not present in the client's handshake (the server has
     * indicated an extension not requested by the client), the client
     * MUST Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void validateExtensions(StatusLine statusLine, Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Extensions.
        List<String> values = headers.get("Sec-WebSocket-Extensions");

        if (values == null || values.size() == 0)
        {
            // Nothing to check.
            return;
        }

        List<WebSocketExtension> extensions = new ArrayList<WebSocketExtension>();

        for (String value : values)
        {
            // Split the value into elements each of which represents an extension.
            String[] elements = value.split("\\s*,\\s*");

            for (String element : elements)
            {
                // Parse the string whose format is supposed to be "{name}[; {key}[={value}]*".
                WebSocketExtension extension = WebSocketExtension.parse(element);

                if (extension == null)
                {
                    // The value in 'Sec-WebSocket-Extensions' failed to be parsed.
                    throw new OpeningHandshakeException(
                        WebSocketError.EXTENSION_PARSE_ERROR,
                        "The value in 'Sec-WebSocket-Extensions' failed to be parsed: " + element,
                        statusLine, headers);
                }

                // The extension name.
                String name = extension.getName();

                // If the extension is not contained in the original request from this client.
                if (mWebSocket.getHandshakeBuilder().containsExtension(name) == false)
                {
                    // The extension contained in the Sec-WebSocket-Extensions header is not supported.
                    throw new OpeningHandshakeException(
                        WebSocketError.UNSUPPORTED_EXTENSION,
                        "The extension contained in the Sec-WebSocket-Extensions header is not supported: " + name,
                        statusLine, headers);
                }

                // Let the extension validate itself.
                extension.validate();

                // The extension has been agreed.
                extensions.add(extension);
            }
        }

        // Check if extensions conflict with each other.
        validateExtensionCombination(statusLine, headers, extensions);

        // Agreed extensions.
        mWebSocket.setAgreedExtensions(extensions);
    }


    private void validateExtensionCombination(
            StatusLine statusLine, Map<String, List<String>> headers, List<WebSocketExtension> extensions) throws WebSocketException
    {
        // Currently, only duplication of per-message compression extensions is checked.

        // A per-message compression extension found in the list.
        WebSocketExtension pmce = null;

        for (WebSocketExtension extension : extensions)
        {
            // If the extension is not a per-message compression extension.
            if ((extension instanceof PerMessageCompressionExtension) == false)
            {
                continue;
            }

            // If the found per-message compression extension is the first one.
            if (pmce == null)
            {
                // Found a per-message compression extension.
                pmce = extension;
                continue;
            }

            // Found the second per-message compression extension. Conflict.
            String message = String.format(
                "'%s' extension and '%s' extension conflict with each other.",
                pmce.getName(), extension.getName());

            // The extensions conflict with each other.
            throw new OpeningHandshakeException(
                    WebSocketError.EXTENSIONS_CONFLICT, message, statusLine, headers);
        }
    }


    /**
     * Validate the value of {@code Sec-WebSocket-Protocol} header.
     *
     * <blockquote>
     * <p>From RFC 6455, p20.</p>
     * <p><i>
     * If the response includes a {@code Sec-WebSocket-Protocol} header field
     * and this header field indicates the use of a subprotocol that was
     * not present in the client's handshake (the server has indicated a
     * subprotocol not requested by the client), the client MUST Fail
     * the WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void validateProtocol(StatusLine statusLine, Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Protocol.
        List<String> values = headers.get("Sec-WebSocket-Protocol");

        if (values == null)
        {
            // Nothing to check.
            return;
        }

        // Protocol
        String protocol = values.get(0);

        if (protocol == null || protocol.length() == 0)
        {
            // Ignore.
            return;
        }

        // If the protocol is not contained in the original request
        // from this client.
        if (mWebSocket.getHandshakeBuilder().containsProtocol(protocol) == false)
        {
            // The protocol contained in the Sec-WebSocket-Protocol header is not supported.
            throw new OpeningHandshakeException(
                WebSocketError.UNSUPPORTED_PROTOCOL,
                "The protocol contained in the Sec-WebSocket-Protocol header is not supported: " + protocol,
                statusLine, headers);
        }

        // Agreed protocol.
        mWebSocket.setAgreedProtocol(protocol);
    }
}
