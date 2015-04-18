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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Web socket.
 *
 * <p>
 * Instances of {@code WebSocket} are created by calling one of {@code
 * createSocket} methods of a {@link WebSocketFactory} instance. Below
 * is the simplest example to create a {@code WebSocket} instance.
 * </p>
 *
 * <blockquote>
 * <pre> WebSocket ws = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()}.{@link WebSocketFactory#createSocket(String)
 * createWebSocket}("ws://localhost/endpoint");</pre>
 * </blockquote>
 *
 * <p>
 * After creating a {@code WebSocket} instance, you should call {@link
 * #addListener(WebSocketListener)} method to register a {@link
 * WebSocketListener} that receives web socket events. {@link
 * WebSocketAdapter} is an empty implementation of {@link
 * WebSocketListener} interface.
 * </p>
 *
 * <blockquote>
 * <pre> ws.{@link #addListener(WebSocketListener) addListener}(new {@link
 * WebSocketAdapter#WebSocketAdapter() WebSocketAdapter()} {
 *     {@code @}Override
 *     public void {@link WebSocketListener#onTextMessage(WebSocket, String)
 *     onTextMessage}(WebSocket websocket, String text) {
 *         ......
 *     }
 * });</pre>
 * </blockquote>
 *
 * <p>
 * By calling {@link #connect()} method, an actual connection to the server
 * is made and the opening handshake is performed as described in "<a href=
 * "https://tools.ietf.org/html/rfc6455#section-4">4. Opening Handshake</a>"
 * in <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>. Before
 * calling {@code connect()} method, you may call {@link #addProtocol(String)}
 * method, {@link #addExtension(WebSocketExtension)} method and {@link
 * #addHeader(String, String)} method to adjust the opening handshake.
 * {@link #setUserInfo(String, String)} method can be used to set credentials
 * for Basic Authentication (<a href="https://tools.ietf.org/html/rfc2617"
 * >RFC 2617</a>). You can even configure the underlying {@link Socket}
 * instance after getting it by {@link #getSocket()} method.
 * </p>
 *
 * <p>
 * {@code connect()} performs the opening handshake synchronously. When a
 * connection could not be made or a protocol error was detected in the
 * handshake, a {@link WebSocketException} exception is thrown. When the
 * handshake succeeds, the {@code connect()} implementation creates threads
 * and starts them to read and write web socket frames from the server
 * asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre> try
 * {
 *     // Connect to the server and perform the handshake.
 *     ws.{@link #connect()};
 * }
 * catch ({@link WebSocketException} e)
 * {
 *     // The opening handshake failed.
 * }</pre>
 * </blockquote>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455 (The WebSocket Protocol)</a>
 */
public class WebSocket implements Closeable
{
    private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final Socket mSocket;
    private final HandshakeBuilder mHandshakeBuilder;
    private final ListenerManager mListenerManager;
    private WebSocketState mState;
    private WebSocketInputStream mInput;
    private WebSocketOutputStream mOutput;
    private ReadingThread mReadingThread;
    private List<WebSocketExtension> mAgreedExtensions;
    private String mAgreedProtocol;
    private boolean mExtended;


    WebSocket(String userInfo, String host, String path, Socket socket)
    {
        mSocket           = socket;
        mHandshakeBuilder = new HandshakeBuilder(userInfo, host, path);
        mListenerManager  = new ListenerManager(this);
        mState            = WebSocketState.CREATED;
    }


    /**
     * Get the current state of this web socket.
     *
     * <p>
     * The initial state is {@link WebSocketState#CREATED CREATED}.
     * When {@link #connect()} is called, the state is changed to
     * {@link WebSocketState#CONNECTING CONNECTING}, and then to
     * {@link WebSocketState#OPEN OPEN} after a successful handshake.
     * If the handshake fails, the state is set to {@link
     * WebSocketState#CLOSED CLOSED}.
     * </p>
     *
     * @return
     *         The current state.
     */
    public WebSocketState getState()
    {
        synchronized (this)
        {
            return mState;
        }
    }


    /**
     * Change the state of this web socket.
     *
     * @param state
     *         The new state.
     */
    void setState(WebSocketState state)
    {
        synchronized (this)
        {
            mState = state;
        }
    }


    /**
     * Add a value for {@code Sec-WebSocket-Protocol}.
     *
     * @param protocol
     *         A protocol name.
     *
     * @throws IllegalArgumentException
     *         The protocol name is invalid. A protocol name must be
     *         a non-empty string with characters in the range U+0021
     *         to U+007E not including separator characters.
     */
    public WebSocket addProtocol(String protocol)
    {
        mHandshakeBuilder.addProtocol(protocol);

        return this;
    }


    /**
     * Add a value for {@code Sec-WebSocket-Extension}.
     *
     * @param extension
     *         An extension. {@code null} is silently ignored.
     */
    public WebSocket addExtension(WebSocketExtension extension)
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
        mHandshakeBuilder.setUserInfo(userInfo);

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
        mHandshakeBuilder.setUserInfo(id, password);

        return this;
    }


    /**
     * Check if extended use of web socket frames are allowed.
     *
     * <p>
     * When extended use is allowed, values of RSV1/RSV2/RSV3 bits
     * and opcode of frames are not checked. On the other hand,
     * if not allowed (default), non-zero values for RSV1/RSV2/RSV3
     * bits and unknown opcodes cause an error. In such a case,
     * {@link WebSocketListener#onFrameError(WebSocket, WebSocketFrame,
     * WebSocketException) onFrameError} method of listeners are
     * called and the web socket is eventually closed.
     * </p>
     *
     * @return
     *         {@code true} if extended use of web socket frames
     *         are allowed.
     */
    public boolean isExtended()
    {
        return mExtended;
    }


    /**
     * Allow or disallow extended use of web socket frames.
     *
     * @param extended
     *         {@code true} to allow extended use of web socket frames.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket setExtended(boolean extended)
    {
        mExtended = extended;

        return this;
    }


    /**
     * Add a listener to receive events on this web socket.
     */
    public WebSocket addListener(WebSocketListener listener)
    {
        mListenerManager.addListener(listener);

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
     * As necessary, {@link #addProtocol(String)}, {@link #addExtension(WebSocketExtension)}
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
     * Note that if the URI passed to {@link WebSocketFactory}{@code
     * .createSocket} method contains the user-info part, you don't have to
     * call {@code setUserInfo} method.
     * </p>
     *
     * @throws WebSocketException
     *         <ul>
     *           <li>The current state of the web socket is not {@link
     *               WebSocketState#CREATED CREATED}
     *           <li>Connecting the server failed.
     *           <li>The opening handshake failed.
     *         </ul>
     */
    public void connect() throws WebSocketException
    {
        // Change the state to CONNECTING. If the state before
        // the change is not CREATED, an exception is thrown.
        changeStateOnConnect();

        // HTTP headers from the server.
        Map<String, List<String>> headers;

        try
        {
            // Perform WebSocket handshake.
            headers = shakeHands();
        }
        catch (WebSocketException e)
        {
            // Change the state to CLOSED.
            setState(WebSocketState.CLOSED);

            // The handshake failed.
            throw e;
        }

        // Change the state to OPEN.
        setState(WebSocketState.OPEN);

        // Start threads that communicate with the server.
        startThreads(headers);
    }


    /**
     * Get the agreed extensions.
     *
     * <p>
     * This method works correctly only after {@link #connect()} succeeds
     * (= after the opening handshake succeeds).
     * </p>
     *
     * @return
     *         The agreed extensions.
     */
    public List<WebSocketExtension> getAgreedExtensions()
    {
        return mAgreedExtensions;
    }


    /**
     * Get the agreed protocol.
     *
     * <p>
     * This method works correctly only after {@link #connect()} succeeds
     * (= after the opening handshake succeeds).
     * </p>
     *
     * @return
     *         The agreed protocol.
     */
    public String getAgreedProtocol()
    {
        return mAgreedProtocol;
    }


    /**
     * Send a web socket frame to the server.
     *
     * @param frame
     *         A web socket frame to be sent to the server.
     */
    public void sendFrame(WebSocketFrame frame)
    {
        // TODO
    }


    /**
     * Send a text message to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createTextFrame(String)
     * WebSocketFrame.createTextFrame}(message)).
     * </p>
     *
     * @param message
     *         A text message to be sent to the server.
     */
    public void sendText(String message)
    {
        sendFrame(WebSocketFrame.createTextFrame(message));
    }


    /**
     * Send a binary message to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createBinaryFrame(byte[])
     * WebSocketFrame.createBinaryFrame}(message)).
     * </p>
     *
     * @param message
     *         A binary message to be sent to the server.
     */
    public void sendBinary(byte[] message)
    {
        sendFrame(WebSocketFrame.createBinaryFrame(message));
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createCloseFrame()}).
     * </p>
     */
    public void sendClose()
    {
        sendFrame(WebSocketFrame.createCloseFrame());
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createCloseFrame(int)
     * WebSocketFrame.createCloseFrame}(closeCode)).
     * </p>
     *
     * @param closeCode
     *         The close code.
     *
     * @see WebSocketCloseCode
     */
    public void sendClose(int closeCode)
    {
        sendFrame(WebSocketFrame.createCloseFrame(closeCode));
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createCloseFrame(int, String)
     * WebSocketFrame.createCloseFrame}(closeCode, reason)).
     * </p>
     *
     * @param closeCode
     *         The close code.
     *
     * @param reason
     *         The close reason.
     *
     * @see WebSocketCloseCode
     */
    public void sendClose(int closeCode, String reason)
    {
        sendFrame(WebSocketFrame.createCloseFrame(closeCode, reason));
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPingFrame()}).
     * </p>
     */
    public void sendPing()
    {
        sendFrame(WebSocketFrame.createPingFrame());
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPingFrame(byte[])
     * WebSocketFrame.createPingFrame}(payload)).
     * </p>
     *
     * @param payload
     *         The payload for a ping frame.
     */
    public void sendPing(byte[] payload)
    {
        sendFrame(WebSocketFrame.createPingFrame(payload));
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPingFrame(String)
     * WebSocketFrame.createPingFrame}(payload)).
     * </p>
     *
     * @param payload
     *         The payload for a ping frame.
     */
    public void sendPing(String payload)
    {
        sendFrame(WebSocketFrame.createPingFrame(payload));
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPongFrame()}).
     * </p>
     */
    public void sendPong()
    {
        sendFrame(WebSocketFrame.createPongFrame());
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPongFrame(byte[])
     * WebSocketFrame.createPongFrame}(payload)).
     * </p>
     *
     * @param payload
     *         The payload for a pong frame.
     */
    public void sendPong(byte[] payload)
    {
        sendFrame(WebSocketFrame.createPongFrame(payload));
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}({@link WebSocketFrame#createPongFrame(String)
     * WebSocketFrame.createPongFrame}(payload)).
     * </p>
     *
     * @param payload
     *         The payload for a pong frame.
     */
    public void sendPong(String payload)
    {
        sendFrame(WebSocketFrame.createPongFrame(payload));
    }


    private void changeStateOnConnect() throws WebSocketException
    {
        synchronized (this)
        {
            // If the current state is not CREATED.
            if (mState != WebSocketState.CREATED)
            {
                throw new WebSocketException(
                    WebSocketError.NOT_IN_CREATED_STATE,
                    "The current state of the web socket is not CREATED.");
            }

            // Change the state to CONNECTING.
            mState = WebSocketState.CONNECTING;
        }
    }


    private Map<String, List<String>> shakeHands() throws WebSocketException
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

        return headers;
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
        Misc.nextBytes(data);

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

        // Validate the value of Sec-WebSocket-Protocol.
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
        String name = pair[0].trim().toUpperCase();

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

        if (values == null || values.size() == 0)
        {
            // The opening handshake response does not contain 'Upgrade' header.
            throw new WebSocketException(
                WebSocketError.NO_UPGRADE_HEADER,
                "The opening handshake response does not contain 'Upgrade' header.");
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
        throw new WebSocketException(
            WebSocketError.NO_WEBSOCKET_IN_UPGRADE_HEADER,
            "'websocket' was not found in 'Upgrade' header.");
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

        if (values == null || values.size() == 0)
        {
            // The opening handshake response does not contain 'Connection' header.
            throw new WebSocketException(
                WebSocketError.NO_CONNECTION_HEADER,
                "The opening handshake response does not contain 'Connection' header.");
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
        throw new WebSocketException(
            WebSocketError.NO_UPGRADE_IN_CONNECTION_HEADER,
            "'Upgrade' was not found in 'Connection' header.");
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
            throw new WebSocketException(
                WebSocketError.UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,
                "The value of 'Sec-WebSocket-Accept' header is different from the expected one.");
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
    private void validateExtensions(Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Extensions.
        List<String> values = headers.get("SEC-WEBSOCKET-EXTENSIONS");

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
                    throw new WebSocketException(
                        WebSocketError.EXTENSION_PARSE_ERROR,
                        "The value in 'Sec-WebSocket-Extensions' failed to be parsed: " + element);
                }

                // The extension name.
                String name = extension.getName();

                // If the extension is not contained in the original request from this client.
                if (mHandshakeBuilder.containsExtension(name) == false)
                {
                    // The extension contained in the Sec-WebSocket-Extensions header is not supported.
                    throw new WebSocketException(
                        WebSocketError.UNSUPPORTED_EXTENSION,
                        "The extension contained in the Sec-WebSocket-Extensions header is not supported: " + name);
                }

                // The extension has been agreed.
                extensions.add(extension);
            }
        }

        mAgreedExtensions = extensions;
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
    private void validateProtocol(Map<String, List<String>> headers) throws WebSocketException
    {
        // Get the values of Sec-WebSocket-Protocol.
        List<String> values = headers.get("SEC-WEBSOCKET-PROTOCOL");

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
        if (mHandshakeBuilder.containsProtocol(protocol) == false)
        {
            // The protocol contained in the Sec-WebSocket-Protocol header is not supported.
            throw new WebSocketException(
                WebSocketError.UNSUPPORTED_PROTOCOL,
                "The protocol contained in the Sec-WebSocket-Protocol header is not supported: " + protocol);
        }

        mAgreedProtocol = protocol;
    }


    private void startThreads(Map<String, List<String>> headers)
    {
        ReadingThread thread = new ReadingThread(this, headers);

        synchronized (this)
        {
            if (mReadingThread != null)
            {
                mReadingThread.requestStop();
            }

            mReadingThread = thread;
        }

        thread.start();
    }


    private void stopThread()
    {
        synchronized (this)
        {
            if (mReadingThread != null)
            {
                mReadingThread.requestStop();
                mReadingThread = null;
            }
        }
    }


    /**
     * Close the web socket.
     */
    @Override
    public void close()
    {
        // TODO
        stopThread();
    }


    WebSocketInputStream getInput()
    {
        return mInput;
    }


    WebSocketOutputStream getOutput()
    {
        return mOutput;
    }


    ListenerManager getListenerManager()
    {
        return mListenerManager;
    }
}
