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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WebSocket implements Closeable
{
    private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final SecureRandom sRandom = new SecureRandom();
    private final Socket mSocket;
    private final HandshakeBuilder mHandshakeBuilder;
    private final List<WebSocketListener> mListeners = new ArrayList<WebSocketListener>();
    private WebSocketInputStream mInput;
    private WebSocketOutputStream mOutput;
    private WebSocketThread mWebSocketThread;


    WebSocket(String userInfo, String host, String path, Socket socket)
    {
        mSocket = socket;
        mHandshakeBuilder =
            new HandshakeBuilder(userInfo, host, path);
    }


    /**
     * Add a value for {@code Sec-WebSocket-Protocol}.
     */
    public WebSocket addProtocol(String protocol)
    {
        mHandshakeBuilder.addProtocol(protocol);

        return this;
    }


    /**
     * Add a value for {@code Sec-WebSocket-Extension}.
     */
    public WebSocket addExtension(String extension)
    {
        mHandshakeBuilder.addExtension(extension);

        return this;
    }


    /**
     * Add a pair of HTTP header.
     */
    public WebSocket addHeader(String name, String value)
    {
        mHandshakeBuilder.addHeader(name, value);

        return this;
    }


    /**
     * Set the credentials to connect to the web socket endpoint.
     *
     * @param userInfo
     *         The credentials for Basic Authentication. The format
     *         should be <code><i>id</i>:<i>password</i></code>.
     *
     * @return
     *         {@code this} instance.
     */
    public WebSocket setUserInfo(String userInfo)
    {
        return this;
    }


    /**
     * Set the credentials to connect to the web socket endpoint.
     *
     * @param id
     *         The ID.
     *
     * @param password
     *         The password.
     *
     * @return
     *         {@code this} instance.
     */
    public WebSocket setUserInfo(String id, String password)
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

        return setUserInfo(userInfo);
    }


    /**
     * Add a listener to receive events on this web socket.
     */
    public WebSocket addListener(WebSocketListener listener)
    {
        if (listener == null)
        {
            return this;
        }

        synchronized (mListeners)
        {
            mListeners.add(listener);
        }

        return this;
    }


    /**
     * Get the raw socket which this web socket uses internally.
     */
    public Socket getSocket()
    {
        return mSocket;
    }


    /**
     * Send an opening handshake to the server, receive the response and then
     * start a thread to communicate with the server.
     *
     * <p>
     * As necessary, {@link #addProtocol(String)}, {@link #addExtension(String)}
     * {@link #addHeader(String, String)} should be called before you call this
     * method. It is because the parameters set by these methods are used in the
     * opening handshake.
     * </p>
     *
     * <p>
     * Also, as necessary, {@link #getSocket()} should be used to set up socket
     * parameters before you call this method. For example, you can set the
     * socket timeout like the following.
     * </p>
     *
     * <pre>
     * WebSocket websocket = ......;
     * websocket.{@link #getSocket() getSocket()}.{@link Socket#setSoTimeout(int)
     * setSoTimeout}(5000);
     * </pre>
     *
     * <p>
     * If the web socket endpoint requires Basic Authentication, you can set
     * credentials by {@link #setUserInfo(String) setUserInfo(userInfo)} or
     * {@link #setUserInfo(String, String) setUserInfo(id, password)} before
     * you call this method.
     * Note that if the URI passed to {@link WebSocketFactory}{@link
     * .createSocket} method contains the user-info part, you don't have to
     * call {@code setUserInfo} method.
     * </p>
     *
     * @throws WebSocketException
     *         The opening handshake failed.
     */
    public void open() throws WebSocketException
    {
        // The raw socket created by WebSocketFactory.
        Socket socket = mSocket;

        // Get the input stream of the socket.
        WebSocketInputStream input = openInputStream(socket);

        // Get the output stream of the socket.
        WebSocketOutputStream output = openOutputStream(socket);

        // Generate a value for Sec-WebSocket-Key.
        String key = generateWebSocketKey();

        // Send an opening handshake to the server.
        writeHandshake(output, key);

        // Read the response from the server.
        Map<String, List<String>> headers = readHandshake(input, key);

        // The handshake succeeded.
        mInput  = input;
        mOutput = output;

        // Start a thread that communicate with the server.
        startThread(headers);
    }


    private WebSocketInputStream openInputStream(Socket socket) throws WebSocketException
    {
        try
        {
            // Get the input stream of the raw socket through which
            // this client receives data from the server.
            return new WebSocketInputStream(
                new BufferedInputStream(socket.getInputStream()));
        }
        catch (IOException e)
        {
            // Failed to get the input stream of the raw socket.
            throw new WebSocketException(
                WebSocketError.SOCKET_INPUT_STREAM_FAILURE,
                "Failed to get the input stream of the raw socket.", e);
        }
    }


    private WebSocketOutputStream openOutputStream(Socket socket) throws WebSocketException
    {
        try
        {
            // Get the output stream of the socket through which
            // this client sends data to the server.
            return new WebSocketOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
        }
        catch (IOException e)
        {
            // Failed to get the output stream from the raw socket.
            throw new WebSocketException(
                WebSocketError.SOCKET_OUTPUT_STREAM_FAILURE,
                "Failed to get the output stream from the raw socket.", e);
        }
    }


    /**
     * Generate a value for Sec-WebSocket-Key.
     *
     * <blockquote>
     * <p><i>
     * The request MUST include a header field with the name Sec-WebSocket-Key.
     * The value of this header field MUST be a nonce consisting of a randomly
     * selected 16-byte value that has been base64-encoded (see Section 4 of
     * RFC 4648). The nonce MUST be selected randomly for each connection.
     * </i></p>
     * </blockquote>
     *
     * @return
     */
    private static String generateWebSocketKey()
    {
        // "16-byte value"
        byte[] data = new byte[16];

        // "randomly selected"
        sRandom.nextBytes(data);

        // "base64-encoded"
        return Base64.encode(data);
    }


    private void writeHandshake(WebSocketOutputStream output, String key) throws WebSocketException
    {
        // Generate an opening handshake sent to the server from this client.
        mHandshakeBuilder.setKey(key);
        String handshake = mHandshakeBuilder.build();

        try
        {
            // Send the opening handshake to the server.
            output.write(handshake);
            output.flush();
        }
        catch (IOException e)
        {
            // Failed to send an opening handshake request to the server.
            throw new WebSocketException(
                WebSocketError.OPENING_HAHDSHAKE_REQUEST_FAILURE,
                "Failed to send an opening handshake request to the server.", e);
        }
    }


    private Map<String, List<String>> readHandshake(WebSocketInputStream input, String key) throws WebSocketException
    {
        // Read the status line.
        readStatusLine(input);

        // Read HTTP headers.
        Map<String, List<String>> headers = readHttpHeaders(input);

        // Validate the value of Upgrade.
        validateUpgrade(headers);

        // Validate the value of Connection.
        validateConnection(headers);

        // Validate the value of Sec-WebSocket-Accept.
        validateAccept(headers, key);

        // Validate the value of Sec-WebSocket-Extensions.
        validateExtensions(headers);

        // Validate the valeu of Sec-WebSocket-Protocol.
        validateProtocol(headers);

        // OK. The server has accepted the web socket request.
        return headers;
    }


    private void readStatusLine(WebSocketInputStream input) throws WebSocketException
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
                "Failed to read an opening handshake response from the server.", e);
        }

        if (line == null || line.length() == 0)
        {
            // The status line of the opening handshake response is empty.
            throw new WebSocketException(
                WebSocketError.STATUS_LINE_EMPTY,
                "The status line of the opening handshake response is empty.");
        }

        // Expect "HTTP/1.1 101 Switching Protocols
        String[] elements = line.split(" +", 3);

        if (elements.length < 2)
        {
            // The status line of the opening handshake response is badly formatted.
            throw new WebSocketException(
                WebSocketError.STATUS_LINE_BAD_FORMAT,
                "The status line of the opening handshake response is badly formatted.");
        }

        // The status code must be 101 (Switching Protocols).
        if ("101".equals(elements[1]) == false)
        {
            // The status code of the opening handshake response is not Switching Protocols.
            throw new WebSocketException(
                WebSocketError.NOT_SWITCHING_PROTOCOLS,
                "The status code of the opening handshake response is not Switching Protocols. The status line is: " + line);
        }

        // OK. The server can speak the WebSocket protocol.
    }


    private Map<String, List<String>> readHttpHeaders(WebSocketInputStream input) throws WebSocketException
    {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
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
                    "An error occurred while HTTP header section was being read.", e);
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

        // Name. (Capitalize)
        String name = pair[0].toUpperCase();

        // Value. (Remove leading spaces)
        String value = pair[1].replaceAll("^[ \t]+", "");

        List<String> list = headers.get(name);

        if (list == null)
        {
            list = new ArrayList<String>();
            headers.put(name, list);
        }

        list.add(value);
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
    private void validateUpgrade(Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Upgrade.
        List<String> values = headers.get("UPGRADE");

        if (values == null)
        {
            // The opening handshake response does not contain 'Upgrade' header.
            throw new WebSocketException(
                WebSocketError.NO_UPGRADE_HEADER,
                "The opening handshake response does not contain 'Upgrade' header.");
        }

        // The actual value of Upgrade.
        String actual = values.get(0);

        if ("websocket".equalsIgnoreCase(actual) == false)
        {
            // The value of 'Upgrade' header is not 'websocket'.
            throw new WebSocketException(
                WebSocketError.UNEXPECTED_UPGRADE_HEADER,
                "The value of 'Upgrade' header is not 'websocket'.");
        }
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
    private void validateConnection(Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Upgrade.
        List<String> values = headers.get("CONNECTION");

        if (values == null)
        {
            // The opening handshake response does not contain 'Connection' header.
            throw new WebSocketException(
                WebSocketError.NO_CONNECTION_HEADER,
                "The opening handshake response does not contain 'Connection' header.");
        }

        // The actual value of Connection.
        String actual = values.get(0);

        if ("Upgrade".equalsIgnoreCase(actual) == false)
        {
            // The value of 'Connection' header is not 'Upgrade'.
            throw new WebSocketException(
                WebSocketError.UNEXPECTED_CONNECTION_HEADER,
                "The value of 'Connection' header is not 'Upgrade'.");
        }
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
    private void validateAccept(Map<String, List<String>> headers, String key) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Accept.
        List<String> values = headers.get("SEC-WEBSOCKET-ACCEPT");

        if (values == null)
        {
            // The opening handshake response does not contain 'Sec-WebSocket-Accept' header.
            throw new WebSocketException(
                WebSocketError.NO_SEC_WEBSOCKET_ACCEPT_HEADER,
                "The opening handshake response does not contain 'Sec-WebSocket-Accept' header.");
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
            byte[] digest = md.digest(input.getBytes("UTF-8"));

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
            throw new WebSocketException(
                WebSocketError.UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,
                "The value of 'Sec-WebSocket-Accept' header is different from the expected one.");
        }

        // OK. The value of Sec-WebSocket-Accept is the same as the expected one.
    }


    private void validateExtensions(Map<String, List<String>> headers) throws WebSocketException
    {
        // TODO
    }


    private void validateProtocol(Map<String, List<String>> headers) throws WebSocketException
    {
        // TODO
    }


    private void startThread(Map<String, List<String>> headers)
    {
        WebSocketThread thread = new WebSocketThread(this, headers);

        synchronized (this)
        {
            mWebSocketThread = thread;
            thread.start();
        }
    }


    @Override
    public void close() throws IOException
    {
        // TODO
    }


    WebSocketInputStream getInput()
    {
        return mInput;
    }


    WebSocketOutputStream getOutput()
    {
        return mOutput;
    }


    void callListenerMethod(WebSocketListenerMethodCaller caller)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    caller.call(this, listener);
                }
                catch (Exception e)
                {
                }
            }
        }
    }
}
