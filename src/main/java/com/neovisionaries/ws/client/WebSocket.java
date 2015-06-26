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


import static com.neovisionaries.ws.client.WebSocketState.CLOSED;
import static com.neovisionaries.ws.client.WebSocketState.CLOSING;
import static com.neovisionaries.ws.client.WebSocketState.CONNECTING;
import static com.neovisionaries.ws.client.WebSocketState.CREATED;
import static com.neovisionaries.ws.client.WebSocketState.OPEN;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.neovisionaries.ws.client.StateManager.CloseInitiator;


/**
 * Web socket.
 *
 * <h3>Create WebSocketFactory</h3>
 *
 * <p>
 * {@link WebSocketFactory} is a factory class that creates
 * {@link WebSocket} instances. The first step is to create a
 * {@code WebSocketFactory} instance.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocketFactory instance.</span>
 * WebSocketFactory factory = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()};</pre>
 * </blockquote>
 *
 * <p>
 * By default, {@code WebSocketFactory} uses {@link
 * javax.net.SocketFactory SocketFactory}{@code .}{@link
 * javax.net.SocketFactory#getDefault() getDefault()} for
 * non-secure WebSocket connections ({@code ws:}) and {@link
 * javax.net.ssl.SSLSocketFactory SSLSocketFactory}{@code
 * .}{@link javax.net.ssl.SSLSocketFactory#getDefault()
 * getDefault()} for secure WebSocket connections ({@code
 * wss:}). You can change this default behavior by using
 * {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSocketFactory(javax.net.SocketFactory)
 * setSocketFactory} method, {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSSLSocketFactory(javax.net.ssl.SSLSocketFactory)
 * setSSLSocketFactory} method and {@code WebSocketFactory.}{@link
 * WebSocketFactory#setSSLContext(javax.net.ssl.SSLContext)
 * setSSLContext} method. The following is an example to set
 * a custom SSL context to a {@code WebSocketFactory} instance.
 * See the description of {@code WebSocketFactory.}{@link
 * WebSocketFactory#createSocket(URI) createSocket} method for details.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a custom SSL context.</span>
 * SSLContext context = <a href="https://gist.github.com/TakahikoKawasaki/d07de2218b4b81bf65ac"
 * >NaiveSSLContext</a>.getInstance(<span style="color:darkred;">"TLS"</span>);
 *
 * <span style="color: green;">// Set the custom SSL context.</span>
 * factory.{@link WebSocketFactory#setSSLContext(javax.net.ssl.SSLContext)
 * setSSLContext}(context);</pre>
 * </blockquote>
 *
 * <p>
 * <a href="https://gist.github.com/TakahikoKawasaki/d07de2218b4b81bf65ac"
 * >NaiveSSLContext</a> used in the above example is a factory class to
 * create an {@link javax.net.ssl.SSLContext SSLContext} which naively
 * accepts all certificates without verification. It's enough for testing
 * purposes. When you see an error message
 * "unable to find valid certificate path to requested target" while
 * testing, try {@code NaiveSSLContext}.
 * </p>
 *
 * <h3>HTTP Proxy</h3>
 *
 * <p>
 * If a WebSocket endpoint needs to be accessed via an HTTP proxy,
 * information about the proxy server has to be set to a {@code
 * WebSocketFactory} instance before creating a {@code WebSocket}
 * instance. Proxy settings are represented by {@link ProxySettings}
 * class. A {@code WebSocketFactory} instance has an associated
 * {@code ProxySettings} instance and it can be obtained by calling
 * {@code WebSocketFactory.}{@link WebSocketFactory#getProxySettings()
 * getProxySettings()} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Get the associated ProxySettings instance.</span>
 * {@link ProxySettings} settings = factory.{@link
 * WebSocketFactory#getProxySettings() getProxySettings()};</pre>
 * </blockquote>
 *
 * <p>
 * {@code ProxySettings} class has methods to set information about
 * a proxy server such as {@link ProxySettings#setHost(String) setHost}
 * method and {@link ProxySettings#setPort(int) setPort} method. The
 * following is an example to set a secure (<code>https</code>) proxy
 * server.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set a proxy server.</span>
 * settings.{@link ProxySettings#setServer(String)
 * setServer}(<span style="color:darkred;">"https://proxy.example.com"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * If credentials are required for authentication at a proxy server,
 * {@link ProxySettings#setId(String) setId} method and {@link
 * ProxySettings#setPassword(String) setPassword} method, or
 * {@link ProxySettings#setCredentials(String, String) setCredentials}
 * method can be used to set the credentials. Note that, however,
 * the current implementation supports only Basic Authentication.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set credentials for authentication at a proxy server.</span>
 * settings.{@link ProxySettings#setCredentials(String, String)
 * setCredentials}(id, password);
 * </pre>
 * </blockquote>
 *
 * <h3>Create WebSocket</h3>
 *
 * <p>
 * {@link WebSocket} class represents a web socket. Its instances are
 * created by calling one of {@code createSocket} methods of a {@link
 * WebSocketFactory} instance. Below is the simplest example to create
 * a {@code WebSocket} instance.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a web socket. The scheme part can be one of the following:
 * // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
 * // part, if any, is interpreted as expected. If a raw socket failed
 * // to be created, or if HTTP proxy handshake or SSL handshake failed,
 * // an IOException is thrown.</span>
 * WebSocket ws = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()}
 *     .{@link WebSocketFactory#createSocket(String)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>);</pre>
 * </blockquote>
 *
 * <h3>Register Listener</h3>
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
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Register a listener to receive web socket events.</span>
 * ws.{@link #addListener(WebSocketListener) addListener}(new {@link
 * WebSocketAdapter#WebSocketAdapter() WebSocketAdapter()} {
 *     {@code @}Override
 *     public void {@link WebSocketListener#onTextMessage(WebSocket, String)
 *     onTextMessage}(WebSocket websocket, String message) {
 *         <span style="color: green;">// Received a text message.</span>
 *         ......
 *     }
 * });</pre>
 * </blockquote>
 *
 * <h3>Configure WebSocket</h3>
 *
 * <p>
 * Before starting a WebSocket <a href="https://tools.ietf.org/html/rfc6455#section-4"
 * >opening handshake</a> with the server, you can configure the web
 * socket instance by using the following methods.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Methods for Configuration</caption>
 *   <thead>
 *     <tr>
 *       <th>METHOD</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link #addProtocol(String) addProtocol}</td>
 *       <td>Adds an element to {@code Sec-WebSocket-Protocol}</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #addExtension(WebSocketExtension) addExtension}</td>
 *       <td>Adds an element to {@code Sec-WebSocket-Extensions}</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #addHeader(String, String) addHeader}</td>
 *       <td>Adds an arbitrary HTTP header.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setUserInfo(String, String) setUserInfo}</td>
 *       <td>Adds {@code Authorization} header for Basic Authentication.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #getSocket() getSocket}</td>
 *       <td>Gets the underlying {@link Socket} instance to configure it.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setExtended(boolean) setExtended}</td>
 *       <td>Disables validity checks on RSV1/RSV2/RSV3 and opcode.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <h3>Perform Opening Handshake</h3>
 *
 * <p>
 * By calling {@link #connect()} method, a WebSocket opening handshake
 * is performed synchronously. If an error occurred during the handshake,
 * a {@link WebSocketException} would be thrown. Instead, when the
 * handshake succeeds, the {@code connect()} implementation creates
 * threads and starts them to read and write web socket frames
 * asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> try
 * {
 *     <span style="color: green;">// Perform an opening handshake.</span>
 *     <span style="color: green;">// This method blocks until the opening handshake is finished.</span>
 *     ws.{@link #connect()};
 * }
 * catch ({@link WebSocketException} e)
 * {
 *     <span style="color: green;">// Failed.</span>
 * }</pre>
 * </blockquote>
 *
 * <h3>Send Frames</h3>
 *
 * <p>
 * Web socket frames can be sent by {@link #sendFrame(WebSocketFrame)}
 * method. Other <code>send<i>Xxx</i></code> methods such as {@link
 * #sendText(String)} are aliases of {@code sendFrame} method. All of
 * the <code>send<i>Xxx</i></code> methods work asynchronously. Below
 * are some examples of <code>send<i>Xxx</i></code> methods. Note that
 * in normal cases, you don't have to call {@link #sendClose()} method
 * and {@link #sendPong()} (or their variants) explicitly because they
 * are called automatically when appropriate.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a text frame.</span>
 * ws.{@link #sendText(String) sendText}(<span style="color: darkred;">"Hello."</span>);
 *
 * <span style="color: green;">// Send a binary frame.</span>
 * byte[] binary = ......;
 * ws.{@link #sendBinary(byte[]) sendBinary}(binary);
 *
 * <span style="color: green;">// Send a ping frame.</span>
 * ws.{@link #sendPing(String) sendPing}(<span style="color: darkred;">"Are you there?"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * If you want to send fragmented frames, you have to know the details
 * of the specification (<a href="https://tools.ietf.org/html/rfc6455#section-5.4"
 * >5.4. Fragmentation</a>). Below is an example to send a text message
 * ({@code "How are you?"}) which consists of 3 fragmented frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// The first frame must be either a text frame or a binary frame.
 * // And its FIN bit must be cleared.</span>
 * WebSocketFrame firstFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createTextFrame(String)
 *     createTextFrame}(<span style="color: darkred;">"How "</span>)
 *     .{@link WebSocketFrame#setFin(boolean) setFin}(false);
 *
 * <span style="color: green;">// Subsequent frames must be continuation frames. The FIN bit of
 * // all continuation frames except the last one must be cleared.
 * // Note that the FIN bit of frames returned from
 * // WebSocketFrame.createContinuationFrame methods is cleared, so
 * // the example below does not clear the FIN bit explicitly.</span>
 * WebSocketFrame secondFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createContinuationFrame(String)
 *     createContinuationFrame}(<span style="color: darkred;">"are "</span>);
 *
 * <span style="color: green;">// The last frame must be a continuation frame with the FIN bit set.
 * // Note that the FIN bit of frames returned from
 * // WebSocketFrame.createContinuationFrame methods is cleared, so
 * // the FIN bit of the last frame must be set explicitly.</span>
 * WebSocketFrame lastFrame = WebSocketFrame
 *     .{@link WebSocketFrame#createContinuationFrame(String)
 *     createContinuationFrame}(<span style="color: darkred;">"you?"</span>)
 *     .{@link WebSocketFrame#setFin(boolean) setFin}(true);
 *
 * <span style="color: green;">// Send a text message which consists of 3 frames.</span>
 * ws.{@link #sendFrame(WebSocketFrame) sendFrame}(firstFrame)
 *   .{@link #sendFrame(WebSocketFrame) sendFrame}(secondFrame)
 *   .{@link #sendFrame(WebSocketFrame) sendFrame}(lastFrame);</pre>
 * </blockquote>
 *
 * <p>
 * Alternatively, the same as above can be done like this.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a text message which consists of 3 frames.</span>
 * ws.{@link #sendText(String, boolean) sendText}(<span style="color: darkred;">"How "</span>, false)
 *   .{@link #sendContinuation(String) sendContinuation}(<span style="color: darkred;">"are "</span>)
 *   .{@link #sendContinuation(String, boolean) sendContinuation}(<span style="color: darkred;">"you?"</span>, true);</pre>
 * </blockquote>
 *
 * <h3>Send Ping/Pong Frames Periodically</h3>
 *
 * <p>
 * You can send ping frames periodically by calling {@link #setPingInterval(long)
 * setPingInterval} method with an interval in milliseconds between ping frames.
 * This method can be called both before and after {@link #connect()} method.
 * Passing zero stops the periodical sending.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Send a ping per 60 seconds.</span>
 * ws.{@link #setPingInterval(long) setPingInterval}(60 * 1000);
 *
 * <span style="color: green;">// Stop the periodical sending.</span>
 * ws.{@link #setPingInterval(long) setPingInterval}(0);</pre>
 * </blockquote>
 *
 * <p>
 * Likewise, you can send pong frames periodically by calling {@link
 * #setPongInterval(long) setPongInterval} method. "<i>A Pong frame MAY be sent
 * <b>unsolicited</b>."</i> (<a href="https://tools.ietf.org/html/rfc6455#section-5.5.3"
 * >RFC 6455, 5.5.3. Pong</a>)
 * </p>
 *
 * <h3>Auto Flush</h3>
 *
 * <p>
 * By default, a frame is automatically flushed to the server immediately after
 * {@link #sendFrame(WebSocketFrame) sendFrame} method is executed. This automatic
 * flush can be disabled by calling {@link #setAutoFlush(boolean) setAutoFlush}{@code
 * (false)}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Disable auto-flush.</span>
 * ws.{@link #setAutoFlush(boolean) setAutoFlush}(false);</pre>
 * </blockquote>
 *
 * <p>
 * To flush frames manually, call {@link #flush()} method. Note that this method
 * works asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Flush frames to the server manually.</span>
 * ws.{@link #flush()};</pre>
 * </blockquote>
 *
 * <h3>Disconnect WebSocket</h3>
 *
 * <p>
 * Before a web socket is closed, a closing handshake is performed. A closing handshake
 * is started (1) when the server sends a close frame to the client or (2) when the
 * client sends a close frame to the server. You can start a closing handshake by calling
 * {@link #disconnect()} method (or by sending a close frame manually).
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Close the web socket connection.</span>
 * ws.{@link #disconnect()};</pre>
 * </blockquote>
 *
 * <p>
 * {@code disconnect()} method has some variants. If you want to change the close code
 * and the reason phrase of the close frame that this client will send to the server,
 * use a variant method such as {@link #disconnect(int, String)}. {@code disconnect()}
 * method itself is an alias of {@code disconnect(}{@link WebSocketCloseCode}{@code
 * .NORMAL, null)}.
 * </p>
 *
 * <h3>Reconnection</h3>
 *
 * <p>
 * {@code connect()} method can be called at most only once regardless of whether the
 * method succeeded or failed. If you want to re-connect to the WebSocket endpoint,
 * you have to create a new {@code WebSocket} instance again by calling one of {@code
 * createSocket} methods of a {@code WebSocketFactory}. You may find {@link #recreate()}
 * method useful if you want to create a new {@code WebSocket} instance that has the
 * same settings as the original instance. Note that, however, settings you made on
 * the raw socket of the original {@code WebSocket} instance are not copied.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a new WebSocket instance and connect to the same endpoint.</span>
 * ws = ws.{@link #recreate()}.{@link #connect()};</pre>
 * </blockquote>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455 (The WebSocket Protocol)</a>
 * @see <a href="https://github.com/TakahikoKawasaki/nv-websocket-client">[GitHub] nv-websocket-client</a>
 *
 * @author Takahiko Kawasaki
 */
public class WebSocket
{
    private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final WebSocketFactory mWebSocketFactory;
    private final Socket mSocket;
    private final StateManager mStateManager;
    private HandshakeBuilder mHandshakeBuilder;
    private final ListenerManager mListenerManager;
    private final PingSender mPingSender;
    private final PongSender mPongSender;
    private final Object mThreadsLock = new Object();
    private WebSocketInputStream mInput;
    private WebSocketOutputStream mOutput;
    private ReadingThread mReadingThread;
    private WritingThread mWritingThread;
    private Map<String, List<String>> mServerHeaders;
    private List<WebSocketExtension> mAgreedExtensions;
    private String mAgreedProtocol;
    private boolean mExtended;
    private boolean mAutoFlush = true;
    private boolean mOnConnectedCalled;
    private boolean mReadingThreadStarted;
    private boolean mWritingThreadStarted;
    private boolean mReadingThreadFinished;
    private boolean mWritingThreadFinished;
    private WebSocketFrame mServerCloseFrame;
    private WebSocketFrame mClientCloseFrame;


    WebSocket(WebSocketFactory factory, boolean secure, String userInfo, String host, String path, Socket socket)
    {
        mWebSocketFactory = factory;
        mSocket           = socket;
        mStateManager     = new StateManager();
        mHandshakeBuilder = new HandshakeBuilder(secure, userInfo, host, path);
        mListenerManager  = new ListenerManager(this);
        mPingSender       = new PingSender(this);
        mPongSender       = new PongSender(this);
    }


    /**
     * Create a new {@code WebSocket} instance that has the same settings
     * as this instance. Note that, however, settings you made on the raw
     * socket are not copied.
     *
     * <p>
     * The {@link WebSocketFactory} instance that you used to create this
     * {@code WebSocket} instance is used.
     * </p>
     *
     * @return
     *         A new {@code WebSocket} instance.
     *
     * @throws IOException
     *         {@link WebSocketFactory#createSocket(URI)} threw an exception.
     *
     * @since 1.6
     */
    public WebSocket recreate() throws IOException
    {
        WebSocket instance = mWebSocketFactory.createSocket(getURI());

        // Copy the settings.
        instance.mHandshakeBuilder = new HandshakeBuilder(mHandshakeBuilder);
        instance.setPingInterval(getPingInterval());
        instance.setPongInterval(getPongInterval());

        // Copy listeners.
        List<WebSocketListener> listeners = mListenerManager.getListeners();
        synchronized (listeners)
        {
            for (WebSocketListener listener : listeners)
            {
                instance.addListener(listener);
            }
        }

        return instance;
    }


    @Override
    protected void finalize() throws Throwable
    {
        if (isInState(CREATED))
        {
            // The raw socket needs to be closed.
            finish();
        }

        super.finalize();
    }


    /**
     * Get the current state of this web socket.
     *
     * <p>
     * The initial state is {@link WebSocketState#CREATED CREATED}.
     * When {@link #connect()} is called, the state is changed to
     * {@link WebSocketState#CONNECTING CONNECTING}, and then to
     * {@link WebSocketState#OPEN OPEN} after a successful opening
     * handshake. The state is changed to {@link
     * WebSocketState#CLOSING CLOSING} when a closing handshake
     * is started, and then to {@link WebSocketState#CLOSED CLOSED}
     * when the closing handshake finished.
     * </p>
     *
     * <p>
     * See the description of {@link WebSocketState} for details.
     * </p>
     *
     * @return
     *         The current state.
     *
     * @see WebSocketState
     */
    public WebSocketState getState()
    {
        synchronized (mStateManager)
        {
            return mStateManager.getState();
        }
    }


    /**
     * Check if the current state of this web socket is {@link
     * WebSocketState#OPEN OPEN}.
     *
     * @return
     *         {@code true} if the current state is OPEN.
     *
     * @since 1.1
     */
    public boolean isOpen()
    {
        return isInState(OPEN);
    }


    private boolean isInState(WebSocketState state)
    {
        synchronized (mStateManager)
        {
            return (mStateManager.getState() == state);
        }
    }


    /**
     * Add a value for {@code Sec-WebSocket-Protocol}.
     *
     * @param protocol
     *         A protocol name.
     *
     * @return
     *         {@code this} object.
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
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket addExtension(WebSocketExtension extension)
    {
        mHandshakeBuilder.addExtension(extension);

        return this;
    }


    /**
     * Add a pair of HTTP header.
     *
     * @param name
     *         An HTTP header name.
     *
     * @param value
     *         The value of the HTTP header.
     *
     * @return
     *         {@code this} object.
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
     *         {@code this} object.
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
     *         {@code this} object.
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
     * {@link WebSocketListener#onFrameError(WebSocket,
     * WebSocketException, WebSocketFrame) onFrameError} method of
     * listeners are called and the web socket is eventually closed.
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
     * Check if flush is performed automatically after {@link
     * #sendFrame(WebSocketFrame)} is done. The default value is
     * {@code true}.
     *
     * @return
     *         {@code true} if flush is performed automatically.
     *
     * @since 1.5
     */
    public boolean isAutoFlush()
    {
        return mAutoFlush;
    }


    /**
     * Enable or disable auto-flush of sent frames.
     *
     * @param auto
     *         {@code true} to enable auto-flush. {@code false} to
     *         disable it.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.5
     */
    public WebSocket setAutoFlush(boolean auto)
    {
        mAutoFlush = auto;

        return this;
    }


    /**
     * Flush frames to the server. Flush is performed asynchronously.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.5
     */
    public WebSocket flush()
    {
        synchronized (mStateManager)
        {
            WebSocketState state = mStateManager.getState();

            if (state != OPEN && state != CLOSING)
            {
                return this;
            }

            // Request flush.
            mWritingThread.queueFlush();
        }

        return this;
    }


    /**
     * Get the interval of periodical
     * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2">ping</a>
     * frames.
     *
     * @return
     *         The interval in milliseconds.
     *
     * @since 1.2
     */
    public long getPingInterval()
    {
        return mPingSender.getInterval();
    }


    /**
     * Set the interval of periodical
     * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2">ping</a>
     * frames.
     *
     * <p>
     * Setting a positive number starts sending ping frames periodically.
     * Setting zero stops the periodical sending. This method can be called
     * both before and after {@link #connect()} method.
     * </p>
     *
     * @param interval
     *         The interval in milliseconds. A negative value is
     *         regarded as zero.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.2
     */
    public WebSocket setPingInterval(long interval)
    {
        mPingSender.setInterval(interval);

        return this;
    }


    /**
     * Get the interval of periodical
     * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3">pong</a>
     * frames.
     *
     * @return
     *         The interval in milliseconds.
     *
     * @since 1.2
     */
    public long getPongInterval()
    {
        return mPongSender.getInterval();
    }


    /**
     * Set the interval of periodical
     * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3">pong</a>
     * frames.
     *
     * <p>
     * Setting a positive number starts sending pong frames periodically.
     * Setting zero stops the periodical sending. This method can be called
     * both before and after {@link #connect()} method.
     * </p>
     *
     * <blockquote>
     * <dl>
     * <dt>
     * <span style="font-weight: normal;">An excerpt from <a href=
     * "https://tools.ietf.org/html/rfc6455#section-5.5.3"
     * >RFC 6455, 5.5.3. Pong</a></span>
     * </dt>
     * <dd>
     * <p><i>
     * A Pong frame MAY be sent <b>unsolicited</b>. This serves as a
     * unidirectional heartbeat.  A response to an unsolicited Pong
     * frame is not expected.
     * </i></p>
     * </dd>
     * </dl>
     * </blockquote>
     *
     * @param interval
     *         The interval in milliseconds. A negative value is
     *         regarded as zero.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.2
     */
    public WebSocket setPongInterval(long interval)
    {
        mPongSender.setInterval(interval);

        return this;
    }


    /**
     * Add a listener to receive events on this web socket.
     *
     * @param listener
     *         A listener to add.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket addListener(WebSocketListener listener)
    {
        mListenerManager.addListener(listener);

        return this;
    }


    /**
     * Get the raw socket which this web socket uses internally.
     *
     * @return
     *         The underlying {@link Socket} instance.
     */
    public Socket getSocket()
    {
        return mSocket;
    }


    /**
     * Get the URI of the web socket endpoint. The scheme part is either
     * {@code "ws"} or {@code "wss"}. The authority part is always empty.
     *
     * @return
     *         The URI of the web socket endpoint.
     *
     * @since 1.1
     */
    public URI getURI()
    {
        return mHandshakeBuilder.getURI();
    }


    /**
     * Send an opening handshake to the server, receive the response and then
     * start threads to communicate with the server.
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
     * <p>
     * Note that this method can be called at most only once regardless of
     * whether this method succeeded or failed. If you want to re-connect to
     * the WebSocket endpoint, you have to create a new {@code WebSocket}
     * instance again by calling one of {@code createSocket} methods of a
     * {@link WebSocketFactory}. You may find {@link #recreate()} method
     * useful if you want to create a new {@code WebSocket} instance that
     * has the same settings as this instance. (But settings you made on
     * the raw socket are not copied.)
     * </p>
     *
     * @return
     *         {@code this} object.
     *
     * @throws WebSocketException
     *         <ul>
     *           <li>The current state of the web socket is not {@link
     *               WebSocketState#CREATED CREATED}
     *           <li>Connecting the server failed.
     *           <li>The opening handshake failed.
     *         </ul>
     */
    public WebSocket connect() throws WebSocketException
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
            mStateManager.setState(CLOSED);

            // Notify the listener of the state change.
            mListenerManager.callOnStateChanged(CLOSED);

            // The handshake failed.
            throw e;
        }

        // Change the state to OPEN.
        mStateManager.setState(OPEN);

        // Notify the listener of the state change.
        mListenerManager.callOnStateChanged(OPEN);

        // Start threads that communicate with the server.
        mServerHeaders = headers;
        startThreads();

        return this;
    }


    /**
     * Disconnect the web socket.
     *
     * <p>
     * This method is an alias of {@link #disconnect(int, String)
     * disconnect}{@code (}{@link WebSocketCloseCode#NORMAL}{@code , null)}.
     * </p>
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket disconnect()
    {
        return disconnect(WebSocketCloseCode.NORMAL, null);
    }


    /**
     * Disconnect the web socket.
     *
     * <p>
     * This method is an alias of {@link #disconnect(int, String)
     * disconnect}{@code (closeCode, null)}.
     * </p>
     *
     * @param closeCode
     *         The close code embedded in a <a href=
     *         "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     *         which this WebSocket client will send to the server.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.5
     */
    public WebSocket disconnect(int closeCode)
    {
        return disconnect(closeCode, null);
    }


    /**
     * Disconnect the web socket.
     *
     * <p>
     * This method is an alias of {@link #disconnect(int, String)
     * disconnect}{@code (}{@link WebSocketCloseCode#NORMAL}{@code , reason)}.
     * </p>
     *
     * @param reason
     *         The reason embedded in a <a href=
     *         "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     *         which this WebSocket client will send to the server. Note that
     *         the length of the bytes which represents the given reason must
     *         not exceed 125. In other words, {@code (reason.}{@link
     *         String#getBytes(String) getBytes}{@code ("UTF-8").length <= 125)}
     *         must be true.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.5
     */
    public WebSocket disconnect(String reason)
    {
        return disconnect(WebSocketCloseCode.NORMAL, reason);
    }


    /**
     * Disconnect the web socket.
     *
     * @param closeCode
     *         The close code embedded in a <a href=
     *         "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     *         which this WebSocket client will send to the server.
     *
     * @param reason
     *         The reason embedded in a <a href=
     *         "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     *         which this WebSocket client will send to the server. Note that
     *         the length of the bytes which represents the given reason must
     *         not exceed 125. In other words, {@code (reason.}{@link
     *         String#getBytes(String) getBytes}{@code ("UTF-8").length <= 125)}
     *         must be true.
     *
     * @return
     *         {@code this} object.
     *
     * @see WebSocketCloseCode
     *
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455, 5.5.1. Close</a>
     *
     * @since 1.5
     */
    public WebSocket disconnect(int closeCode, String reason)
    {
        synchronized (mStateManager)
        {
            switch (mStateManager.getState())
            {
                case CREATED:
                    finishAsynchronously();
                    return this;

                case OPEN:
                    break;

                default:
                    // - CONNECTING
                    //     It won't happen unless the programmer dare call
                    //     open() and disconnect() in parallel.
                    //
                    // - CLOSING
                    //     A closing handshake has already been started.
                    //
                    // - CLOSED
                    //     The connection has already been closed.
                    return this;
            }

            // Change the state to CLOSING.
            mStateManager.changeToClosing(CloseInitiator.CLIENT);

            // Create a close frame.
            WebSocketFrame frame = WebSocketFrame.createCloseFrame(closeCode, reason);

            // Send the close frame to the server.
            sendFrame(frame);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(CLOSING);

        // Request the threads to stop.
        stopThreads();

        return this;
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
     * <p>
     * This method just queues the given frame. Actual transmission
     * is performed asynchronously.
     * </p>
     *
     * <p>
     * When the current state of this web socket is not {@link
     * WebSocketState#OPEN OPEN}, this method does not accept
     * the frame.
     * </p>
     *
     * <p>
     * Sending a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
     * >close frame</a> changes the state to {@link WebSocketState#CLOSING
     * CLOSING} (if the current state is neither {@link WebSocketState#CLOSING
     * CLOSING} nor {@link WebSocketState#CLOSED CLOSED}).
     * </p>
     *
     * <p>
     * Note that the validity of the give frame is not checked.
     * For example, even if the payload length of a given frame
     * is greater than 125 and the opcode indicates that the
     * frame is a control frame, this method accepts the given
     * frame.
     * </p>
     *
     * @param frame
     *         A web socket frame to be sent to the server.
     *         If {@code null} is given, nothing is done.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendFrame(WebSocketFrame frame)
    {
        if (frame == null)
        {
            return this;
        }

        synchronized (mStateManager)
        {
            WebSocketState state = mStateManager.getState();

            if (state != OPEN && state != CLOSING)
            {
                return this;
            }

            // Queue the frame.
            mWritingThread.queueFrame(frame);
        }

        return this;
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame()
     * createContinuationFrame()}{@code )}.
     * </p>
     *
     * <p>
     * Note that the FIN bit of a frame sent by this method is {@code false}.
     * If you want to set the FIN bit, use {@link #sendContinuation(boolean)
     * sendContinuation(boolean fin)} with {@code fin=true}.
     * </p>
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation()
    {
        return sendFrame(WebSocketFrame.createContinuationFrame());
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame()
     * createContinuationFrame()}{@code .}{@link
     * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
     * </p>
     *
     * @param fin
     *         The FIN bit value.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation(boolean fin)
    {
        return sendFrame(WebSocketFrame.createContinuationFrame().setFin(fin));
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame(String)
     * createContinuationFrame}{@code (payload))}.
     * </p>
     *
     * <p>
     * Note that the FIN bit of a frame sent by this method is {@code false}.
     * If you want to set the FIN bit, use {@link #sendContinuation(String,
     * boolean) sendContinuation(String payload, boolean fin)} with {@code
     * fin=true}.
     * </p>
     *
     * @param payload
     *         The payload of a continuation frame.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation(String payload)
    {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload));
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame(String)
     * createContinuationFrame}{@code (payload).}{@link
     * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
     * </p>
     *
     * @param payload
     *         The payload of a continuation frame.
     *
     * @param fin
     *         The FIN bit value.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation(String payload, boolean fin)
    {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame(byte[])
     * createContinuationFrame}{@code (payload))}.
     * </p>
     *
     * <p>
     * Note that the FIN bit of a frame sent by this method is {@code false}.
     * If you want to set the FIN bit, use {@link #sendContinuation(byte[],
     * boolean) sendContinuation(byte[] payload, boolean fin)} with {@code
     * fin=true}.
     * </p>
     *
     * @param payload
     *         The payload of a continuation frame.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation(byte[] payload)
    {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload));
    }


    /**
     * Send a continuation frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createContinuationFrame(byte[])
     * createContinuationFrame}{@code (payload).}{@link
     * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
     * </p>
     *
     * @param payload
     *         The payload of a continuation frame.
     *
     * @param fin
     *         The FIN bit value.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendContinuation(byte[] payload, boolean fin)
    {
        return sendFrame(WebSocketFrame.createContinuationFrame(payload).setFin(fin));
    }


    /**
     * Send a text message to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createTextFrame(String)
     * createTextFrame}{@code (message))}.
     * </p>
     *
     * <p>
     * If you want to send a text frame that is to be followed by
     * continuation frames, use {@link #sendText(String, boolean)
     * setText(String payload, boolean fin)} with {@code fin=false}.
     * </p>
     *
     * @param message
     *         A text message to be sent to the server.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendText(String message)
    {
        return sendFrame(WebSocketFrame.createTextFrame(message));
    }


    /**
     * Send a text frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createTextFrame(String)
     * createTextFrame}{@code (payload).}{@link
     * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
     * </p>
     *
     * @param payload
     *         The payload of a text frame.
     *
     * @param fin
     *         The FIN bit value.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendText(String payload, boolean fin)
    {
        return sendFrame(WebSocketFrame.createTextFrame(payload).setFin(fin));
    }


    /**
     * Send a binary message to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createBinaryFrame(byte[])
     * createBinaryFrame}{@code (message))}.
     * </p>
     *
     * <p>
     * If you want to send a binary frame that is to be followed by
     * continuation frames, use {@link #sendBinary(byte[], boolean)
     * setBinary(byte[] payload, boolean fin)} with {@code fin=false}.
     * </p>
     *
     * @param message
     *         A binary message to be sent to the server.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendBinary(byte[] message)
    {
        return sendFrame(WebSocketFrame.createBinaryFrame(message));
    }


    /**
     * Send a binary frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createBinaryFrame(byte[])
     * createBinaryFrame}{@code (payload).}{@link
     * WebSocketFrame#setFin(boolean) setFin}{@code (fin))}.
     * </p>
     *
     * @param payload
     *         The payload of a binary frame.
     *
     * @param fin
     *         The FIN bit value.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendBinary(byte[] payload, boolean fin)
    {
        return sendFrame(WebSocketFrame.createBinaryFrame(payload).setFin(fin));
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createCloseFrame() createCloseFrame()}).
     * </p>
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendClose()
    {
        return sendFrame(WebSocketFrame.createCloseFrame());
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createCloseFrame(int)
     * createCloseFrame}{@code (closeCode))}.
     * </p>
     *
     * @param closeCode
     *         The close code.
     *
     * @return
     *         {@code this} object.
     *
     * @see WebSocketCloseCode
     */
    public WebSocket sendClose(int closeCode)
    {
        return sendFrame(WebSocketFrame.createCloseFrame(closeCode));
    }


    /**
     * Send a close frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createCloseFrame(int, String)
     * createCloseFrame}{@code (closeCode, reason))}.
     * </p>
     *
     * @param closeCode
     *         The close code.
     *
     * @param reason
     *         The close reason.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         {@code this} object.
     *
     * @see WebSocketCloseCode
     */
    public WebSocket sendClose(int closeCode, String reason)
    {
        return sendFrame(WebSocketFrame.createCloseFrame(closeCode, reason));
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPingFrame() createPingFrame()}).
     * </p>
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPing()
    {
        return sendFrame(WebSocketFrame.createPingFrame());
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPingFrame(byte[])
     * createPingFrame}{@code (payload))}.
     * </p>
     *
     * @param payload
     *         The payload for a ping frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPing(byte[] payload)
    {
        return sendFrame(WebSocketFrame.createPingFrame(payload));
    }


    /**
     * Send a ping frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPingFrame(String)
     * createPingFrame}{@code (payload))}.
     * </p>
     *
     * @param payload
     *         The payload for a ping frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPing(String payload)
    {
        return sendFrame(WebSocketFrame.createPingFrame(payload));
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPongFrame() createPongFrame()}).
     * </p>
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPong()
    {
        return sendFrame(WebSocketFrame.createPongFrame());
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPongFrame(byte[])
     * createPongFrame}{@code (payload))}.
     * </p>
     *
     * @param payload
     *         The payload for a pong frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPong(byte[] payload)
    {
        return sendFrame(WebSocketFrame.createPongFrame(payload));
    }


    /**
     * Send a pong frame to the server.
     *
     * <p>
     * This method is an alias of {@link #sendFrame(WebSocketFrame)
     * sendFrame}{@code (WebSocketFrame.}{@link
     * WebSocketFrame#createPongFrame(String)
     * createPongFrame}{@code (payload))}.
     * </p>
     *
     * @param payload
     *         The payload for a pong frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         {@code this} object.
     */
    public WebSocket sendPong(String payload)
    {
        return sendFrame(WebSocketFrame.createPongFrame(payload));
    }


    private void changeStateOnConnect() throws WebSocketException
    {
        synchronized (mStateManager)
        {
            // If the current state is not CREATED.
            if (mStateManager.getState() != CREATED)
            {
                throw new WebSocketException(
                    WebSocketError.NOT_IN_CREATED_STATE,
                    "The current state of the web socket is not CREATED.");
            }

            // Change the state to CONNECTING.
            mStateManager.setState(CONNECTING);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(CONNECTING);
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
     *         A randomly generated web socket key.
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

        // Expect "HTTP/1.1 101 Switching Protocols"
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
                "The status code of the opening handshake response is not '101 Switching Protocols'. The status line is: " + line);
        }

        // OK. The server can speak the WebSocket protocol.
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


    private void startThreads()
    {
        ReadingThread readingThread = new ReadingThread(this);
        WritingThread writingThread = new WritingThread(this);

        synchronized (mThreadsLock)
        {
            mReadingThread = readingThread;
            mWritingThread = writingThread;
        }

        readingThread.start();
        writingThread.start();
    }


    private void stopThreads()
    {
        ReadingThread readingThread;
        WritingThread writingThread;

        synchronized (mThreadsLock)
        {
            readingThread = mReadingThread;
            writingThread = mWritingThread;

            mReadingThread = null;
            mWritingThread = null;
        }

        if (readingThread != null)
        {
            readingThread.requestStop();
        }

        if (writingThread != null)
        {
            writingThread.requestStop();
        }
    }


    WebSocketInputStream getInput()
    {
        return mInput;
    }


    WebSocketOutputStream getOutput()
    {
        return mOutput;
    }


    StateManager getStateManager()
    {
        return mStateManager;
    }


    ListenerManager getListenerManager()
    {
        return mListenerManager;
    }


    void onReadingThreadStarted()
    {
        synchronized (mThreadsLock)
        {
            mReadingThreadStarted = true;

            // Call onConnected() method of listeners if net called yet.
            callOnConnectedIfNotYet();

            if (mWritingThreadStarted == false)
            {
                // Wait for the writing thread to start.
                return;
            }
        }

        onThreadsStarted();
    }


    void onWritingThreadStarted()
    {
        synchronized (mThreadsLock)
        {
            mWritingThreadStarted = true;

            // Call onConnected() method of listeners if not called yet.
            callOnConnectedIfNotYet();

            if (mReadingThreadStarted == false)
            {
                // Wait for the reading thread to start.
                return;
            }
        }

        onThreadsStarted();
    }


    private void callOnConnectedIfNotYet()
    {
        // This method is called in synchronized (mThreadsLock) block.

        // If onConnected() has already been called.
        if (mOnConnectedCalled)
        {
            // Do not call onConnected() twice.
            return;
        }

        // Notify the listeners that the handshake succeeded.
        mListenerManager.callOnConnected(mServerHeaders);

        mOnConnectedCalled = true;
    }


    private void onThreadsStarted()
    {
        // Start sending ping frames periodically.
        // If the interval is zero, this call does nothing.
        mPingSender.start();

        // Likewise, start the pong sender.
        mPongSender.start();
    }


    void onReadingThreadFinished(WebSocketFrame closeFrame)
    {
        synchronized (mThreadsLock)
        {
            mReadingThreadFinished = true;
            mServerCloseFrame = closeFrame;

            if (mWritingThreadFinished == false)
            {
                // Wait for the writing thread to finish.
                return;
            }
        }

        onThreadsFinished();
    }


    void onWritingThreadFinished(WebSocketFrame closeFrame)
    {
        synchronized (mThreadsLock)
        {
            mWritingThreadFinished = true;
            mClientCloseFrame = closeFrame;

            if (mReadingThreadFinished == false)
            {
                // Wait for the reading thread to finish.
                return;
            }
        }

        onThreadsFinished();
    }


    private void onThreadsFinished()
    {
        finish();
    }


    private void finish()
    {
        // Stop the ping sender and the pong sender.
        mPingSender.stop();
        mPongSender.stop();

        try
        {
            // Close the raw socket.
            mSocket.close();
        }
        catch (IOException e)
        {
        }

        synchronized (mStateManager)
        {
            // Change the state to CLOSED.
            mStateManager.setState(CLOSED);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(CLOSED);

        // Notify the listeners that the web socket was disconnected.
        mListenerManager.callOnDisconnected(
            mServerCloseFrame, mClientCloseFrame, mStateManager.getClosedByServer());
    }


    private void finishAsynchronously()
    {
        new Thread() {
            @Override
            public void run() {
                finish();
            }
        }.start();
    }
}
