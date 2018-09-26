/*
 * Copyright (C) 2015-2017 Neo Visionaries Inc.
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import com.neovisionaries.ws.client.StateManager.CloseInitiator;


/**
 * WebSocket.
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
 * setSSLContext} method. Note that you don't have to call a {@code
 * setSSL*} method at all if you use the default SSL configuration.
 * Also note that calling {@code setSSLSocketFactory} method has no
 * meaning if you have called {@code setSSLContext} method. See the
 * description of {@code WebSocketFactory.}{@link
 * WebSocketFactory#createSocket(URI) createSocket(URI)} method for
 * details.
 * </p>
 *
 * <p>
 * The following is an example to set a custom SSL context to a
 * {@code WebSocketFactory} instance. (Again, you don't have to call a
 * {@code setSSL*} method if you use the default SSL configuration.)
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a custom SSL context.</span>
 * SSLContext context = <a href="https://gist.github.com/TakahikoKawasaki/d07de2218b4b81bf65ac"
 * >NaiveSSLContext</a>.getInstance(<span style="color:darkred;">"TLS"</span>);
 *
 * <span style="color: green;">// Set the custom SSL context.</span>
 * factory.{@link WebSocketFactory#setSSLContext(javax.net.ssl.SSLContext)
 * setSSLContext}(context);
 *
 * <span style="color: green;">// Disable manual hostname verification for NaiveSSLContext.
 * //
 * // Manual hostname verification has been enabled since the
 * // version 2.1. Because the verification is executed manually
 * // after Socket.connect(SocketAddress, int) succeeds, the
 * // hostname verification is always executed even if you has
 * // passed an SSLContext which naively accepts any server
 * // certificate. However, this behavior is not desirable in
 * // some cases and you may want to disable the hostname
 * // verification. You can disable the hostname verification
 * // by calling WebSocketFactory.setVerifyHostname(false).</span>
 * factory.{@link WebSocketFactory#setVerifyHostname(boolean) setVerifyHostname}(false);</pre>
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
 * {@link WebSocket} class represents a WebSocket. Its instances are
 * created by calling one of {@code createSocket} methods of a {@link
 * WebSocketFactory} instance. Below is the simplest example to create
 * a {@code WebSocket} instance.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket. The scheme part can be one of the following:
 * // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
 * // part, if any, is interpreted as expected. If a raw socket failed
 * // to be created, an IOException is thrown.</span>
 * WebSocket ws = new {@link WebSocketFactory#WebSocketFactory()
 * WebSocketFactory()}
 *     .{@link WebSocketFactory#createSocket(String)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * There are two ways to set a timeout value for socket connection. The
 * first way is to call {@link WebSocketFactory#setConnectionTimeout(int)
 * setConnectionTimeout(int timeout)} method of {@code WebSocketFactory}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket factory and set 5000 milliseconds as a timeout
 * // value for socket connection.</span>
 * WebSocketFactory factory = new WebSocketFactory().{@link
 * WebSocketFactory#setConnectionTimeout(int) setConnectionTimeout}(5000);
 *
 * <span style="color: green;">// Create a WebSocket. The timeout value set above is used.</span>
 * WebSocket ws = factory.{@link WebSocketFactory#createSocket(String)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>);</pre>
 * </blockquote>
 *
 * <p>
 * The other way is to give a timeout value to a {@code createSocket} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Create a WebSocket factory. The timeout value remains 0.</span>
 * WebSocketFactory factory = new WebSocketFactory();
 *
 * <span style="color: green;">// Create a WebSocket with a socket connection timeout value.</span>
 * WebSocket ws = factory.{@link WebSocketFactory#createSocket(String, int)
 * createWebSocket}(<span style="color: darkred;">"ws://localhost/endpoint"</span>, 5000);</pre>
 * </blockquote>
 *
 * <p>
 * The timeout value is passed to {@link Socket#connect(java.net.SocketAddress, int)
 * connect}{@code (}{@link java.net.SocketAddress SocketAddress}{@code , int)}
 * method of {@link java.net.Socket}.
 * </p>
 *
 * <h3>Register Listener</h3>
 *
 * <p>
 * After creating a {@code WebSocket} instance, you should call {@link
 * #addListener(WebSocketListener)} method to register a {@link
 * WebSocketListener} that receives WebSocket events. {@link
 * WebSocketAdapter} is an empty implementation of {@link
 * WebSocketListener} interface.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Register a listener to receive WebSocket events.</span>
 * ws.{@link #addListener(WebSocketListener) addListener}(new {@link
 * WebSocketAdapter#WebSocketAdapter() WebSocketAdapter()} {
 *     <span style="color: gray;">{@code @}Override</span>
 *     public void {@link WebSocketListener#onTextMessage(WebSocket, String)
 *     onTextMessage}(WebSocket websocket, String message) throws Exception {
 *         <span style="color: green;">// Received a text message.</span>
 *         ......
 *     }
 * });</pre>
 * </blockquote>
 *
 * <p>
 * The table below is the list of callback methods defined in {@code WebSocketListener}
 * interface.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>{@code WebSocketListener} methods</caption>
 *   <thead>
 *     <tr>
 *       <th>Method</th>
 *       <th>Description</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link WebSocketListener#handleCallbackError(WebSocket, Throwable) handleCallbackError}</td>
 *       <td>Called when an <code>on<i>Xxx</i>()</code> method threw a {@code Throwable}.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onBinaryFrame(WebSocket, WebSocketFrame) onBinaryFrame}</td>
 *       <td>Called when a binary frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onBinaryMessage(WebSocket, byte[]) onBinaryMessage}</td>
 *       <td>Called when a binary message was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onCloseFrame(WebSocket, WebSocketFrame) onCloseFrame}</td>
 *       <td>Called when a close frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onConnected(WebSocket, Map) onConnected}</td>
 *       <td>Called after the opening handshake succeeded.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onConnectError(WebSocket, WebSocketException) onConnectError}</td>
 *       <td>Called when {@link #connectAsynchronously()} failed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onContinuationFrame(WebSocket, WebSocketFrame) onContinuationFrame}</td>
 *       <td>Called when a continuation frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame, boolean) onDisconnected}</td>
 *       <td>Called after a WebSocket connection was closed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onError(WebSocket, WebSocketException) onError}</td>
 *       <td>Called when an error occurred.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrame(WebSocket, WebSocketFrame) onFrame}</td>
 *       <td>Called when a frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame) onFrameError}</td>
 *       <td>Called when a frame failed to be read.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameSent(WebSocket, WebSocketFrame) onFrameSent}</td>
 *       <td>Called when a frame was sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onFrameUnsent(WebSocket, WebSocketFrame) onFrameUnsent}</td>
 *       <td>Called when a frame was not sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onMessageDecompressionError(WebSocket, WebSocketException, byte[]) onMessageDecompressionError}</td>
 *       <td>Called when a message failed to be decompressed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onMessageError(WebSocket, WebSocketException, List) onMessageError}</td>
 *       <td>Called when a message failed to be constructed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onPingFrame(WebSocket, WebSocketFrame) onPingFrame}</td>
 *       <td>Called when a ping frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onPongFrame(WebSocket, WebSocketFrame) onPongFrame}</td>
 *       <td>Called when a pong frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendError(WebSocket, WebSocketException, WebSocketFrame) onSendError}</td>
 *       <td>Called when an error occurred on sending a frame.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendingFrame(WebSocket, WebSocketFrame) onSendingFrame}</td>
 *       <td>Called before a frame is sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onSendingHandshake(WebSocket, String, List) onSendingHandshake}</td>
 *       <td>Called before an opening handshake is sent.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onStateChanged(WebSocket, WebSocketState) onStateChanged}</td>
 *       <td>Called when the state of WebSocket changed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextFrame(WebSocket, WebSocketFrame) onTextFrame}</td>
 *       <td>Called when a text frame was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextMessage(WebSocket, String) onTextMessage}</td>
 *       <td>Called when a text message was received.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onTextMessageError(WebSocket, WebSocketException, byte[]) onTextMessageError}</td>
 *       <td>Called when a text message failed to be constructed.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread) onThreadCreated}</td>
 *       <td>Called after a thread was created.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStarted(WebSocket, ThreadType, Thread) onThreadStarted}</td>
 *       <td>Called at the beginning of a thread's {@code run()} method.
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStopping(WebSocket, ThreadType, Thread) onThreadStopping}</td>
 *       <td>Called at the end of a thread's {@code run()} method.
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onUnexpectedError(WebSocket, WebSocketException) onUnexpectedError}</td>
 *       <td>Called when an uncaught throwable was detected.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <h3>Configure WebSocket</h3>
 *
 * <p>
 * Before starting a WebSocket <a href="https://tools.ietf.org/html/rfc6455#section-4"
 * >opening handshake</a> with the server, you can configure the
 * {@code WebSocket} instance by using the following methods.
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
 *     <tr>
 *       <td>{@link #setFrameQueueSize(int) setFrameQueueSize}</td>
 *       <td>Set the size of the frame queue for <a href="#congestion_control">congestion control</a>.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setMaxPayloadSize(int) setMaxPayloadSize}</td>
 *       <td>Set the <a href="#maximum_payload_size">maximum payload size</a>.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link #setMissingCloseFrameAllowed(boolean) setMissingCloseFrameAllowed}</td>
 *       <td>Set whether to allow the server to close the connection without sending a close frame.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <h3>Connect To Server</h3>
 *
 * <p>
 * By calling {@link #connect()} method, connection to the server is
 * established and a WebSocket opening handshake is performed
 * synchronously. If an error occurred during the handshake,
 * a {@link WebSocketException} would be thrown. Instead, when the
 * handshake succeeds, the {@code connect()} implementation creates
 * threads and starts them to read and write WebSocket frames
 * asynchronously.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> try
 * {
 *     <span style="color: green;">// Connect to the server and perform an opening handshake.</span>
 *     <span style="color: green;">// This method blocks until the opening handshake is finished.</span>
 *     ws.{@link #connect()};
 * }
 * catch ({@link OpeningHandshakeException} e)
 * {
 *     <span style="color: green;">// A violation against the WebSocket protocol was detected</span>
 *     <span style="color: green;">// during the opening handshake.</span>
 * }
 * catch ({@link HostnameUnverifiedException} e)
 * {
 *     <span style="color: green;">// The certificate of the peer does not match the expected hostname.</span>
 * }
 * catch ({@link WebSocketException} e)
 * {
 *     <span style="color: green;">// Failed to establish a WebSocket connection.</span>
 * }</pre>
 * </blockquote>
 *
 * <p>
 * In some cases, {@code connect()} method throws {@link OpeningHandshakeException}
 * which is a subclass of {@code WebSocketException} (since version 1.19).
 * {@code OpeningHandshakeException} provides additional methods such as
 * {@link OpeningHandshakeException#getStatusLine() getStatusLine()},
 * {@link OpeningHandshakeException#getHeaders() getHeaders()} and
 * {@link OpeningHandshakeException#getBody() getBody()} to access the
 * response from a server. The following snippet is an example to print
 * information that the exception holds.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> catch ({@link OpeningHandshakeException} e)
 * {
 *     <span style="color: green;">// Status line.</span>
 *     {@link StatusLine} sl = e.{@link OpeningHandshakeException#getStatusLine() getStatusLine()};
 *     System.out.println(<span style="color:darkred;">"=== Status Line ==="</span>);
 *     System.out.format(<span style="color:darkred;">"HTTP Version  = %s\n"</span>, sl.{@link StatusLine#getHttpVersion() getHttpVersion()});
 *     System.out.format(<span style="color:darkred;">"Status Code   = %d\n"</span>, sl.{@link StatusLine#getStatusCode() getStatusCode()});
 *     System.out.format(<span style="color:darkred;">"Reason Phrase = %s\n"</span>, sl.{@link StatusLine#getReasonPhrase() getReasonPhrase()});
 *
 *     <span style="color: green;">// HTTP headers.</span>
 *     Map&lt;String, List&lt;String&gt;&gt; headers = e.{@link OpeningHandshakeException#getHeaders() getHeaders()};
 *     System.out.println(<span style="color:darkred;">"=== HTTP Headers ==="</span>);
 *     for (Map.Entry&lt;String, List&lt;String&gt;&gt; entry : headers.entrySet())
 *     {
 *         <span style="color: green;">// Header name.</span>
 *         String name = entry.getKey();
 *
 *         <span style="color: green;">// Values of the header.</span>
 *         List&lt;String&gt; values = entry.getValue();
 *
 *         if (values == null || values.size() == 0)
 *         {
 *             <span style="color: green;">// Print the name only.</span>
 *             System.out.println(name);
 *             continue;
 *         }
 *
 *         for (String value : values)
 *         {
 *             <span style="color: green;">// Print the name and the value.</span>
 *             System.out.format(<span style="color:darkred;">"%s: %s\n"</span>, name, value);
 *         }
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * Also, {@code connect()} method throws {@link HostnameUnverifiedException}
 * which is a subclass of {@code WebSocketException} (since version 2.1) when
 * the certificate of the peer does not match the expected hostname.
 * </p>
 *
 * <h3>Connect To Server Asynchronously</h3>
 *
 * <p>
 * The simplest way to call {@code connect()} method asynchronously is to
 * use {@link #connectAsynchronously()} method. The implementation of the
 * method creates a thread and calls {@code connect()} method in the thread.
 * When the {@code connect()} call failed, {@link
 * WebSocketListener#onConnectError(WebSocket, WebSocketException)
 * onConnectError()} of {@code WebSocketListener} would be called. Note that
 * {@code onConnectError()} is called only when {@code connectAsynchronously()}
 * was used and the {@code connect()} call executed in the background thread
 * failed. Neither direct synchronous {@code connect()} nor
 * {@link WebSocket#connect(java.util.concurrent.ExecutorService)
 * connect(ExecutorService)} (described below) will trigger the callback method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Connect to the server asynchronously.</span>
 * ws.{@link #connectAsynchronously()};
 * </pre>
 * </blockquote>
 *
 * <p>
 * Another way to call {@code connect()} method asynchronously is to use
 * {@link #connect(ExecutorService)} method. The method performs a WebSocket
 * opening handshake asynchronously using the given {@link ExecutorService}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Prepare an ExecutorService.</span>
 * {@link ExecutorService} es = {@link java.util.concurrent.Executors Executors}.{@link
 * java.util.concurrent.Executors#newSingleThreadExecutor() newSingleThreadExecutor()};
 *
 * <span style="color: green;">// Connect to the server asynchronously.</span>
 * {@link Future}{@code <WebSocket>} future = ws.{@link #connect(ExecutorService) connect}(es);
 *
 * try
 * {
 *     <span style="color: green;">// Wait for the opening handshake to complete.</span>
 *     future.get();
 * }
 * catch ({@link java.util.concurrent.ExecutionException ExecutionException} e)
 * {
 *     if (e.getCause() instanceof {@link WebSocketException})
 *     {
 *         ......
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * The implementation of {@code connect(ExecutorService)} method creates
 * a {@link java.util.concurrent.Callable Callable}{@code <WebSocket>}
 * instance by calling {@link #connectable()} method and passes the
 * instance to {@link ExecutorService#submit(Callable) submit(Callable)}
 * method of the given {@code ExecutorService}. What the implementation
 * of {@link Callable#call() call()} method of the {@code Callable}
 * instance does is just to call the synchronous {@code connect()}.
 * </p>
 *
 * <h3>Send Frames</h3>
 *
 * <p>
 * WebSocket frames can be sent by {@link #sendFrame(WebSocketFrame)}
 * method. Other <code>send<i>Xxx</i></code> methods such as {@link
 * #sendText(String)} are aliases of {@code sendFrame} method. All of
 * the <code>send<i>Xxx</i></code> methods work asynchronously.
 * However, under some conditions, <code>send<i>Xxx</i></code> methods
 * may block. See <a href="#congestion_control">Congestion Control</a>
 * for details.
 * </p>
 *
 * <p>
 * Below
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
 * <p>
 * You can customize payload of ping/pong frames that are sent automatically by using
 * {@link #setPingPayloadGenerator(PayloadGenerator)} and
 * {@link #setPongPayloadGenerator(PayloadGenerator)} methods. Both methods take an
 * instance of {@link PayloadGenerator} interface. The following is an example to
 * use the string representation of the current date as payload of ping frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> ws.{@link #setPingPayloadGenerator(PayloadGenerator)
 * setPingPayloadGenerator}(new {@link PayloadGenerator} () {
 *     <span style="color: gray;">{@code @}Override</span>
 *     public byte[] generate() {
 *         <span style="color: green;">// The string representation of the current date.</span>
 *         return new Date().toString().getBytes();
 *     }
 * });</pre>
 * </blockquote>
 *
 * <p>
 * Note that the maximum payload length of control frames (e.g. ping frames) is 125.
 * Therefore, the length of a byte array returned from {@link PayloadGenerator#generate()
 * generate()} method must not exceed 125.
 * </p>
 *
 * <p>
 * You can change the names of the {@link java.util.Timer Timer}s that send ping/pong
 * frames periodically by using {@link #setPingSenderName(String)} and
 * {@link #setPongSenderName(String)} methods.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Change the Timers' names.</span>
 * ws.{@link #setPingSenderName(String)
 * setPingSenderName}(<span style="color: darkred;">"PING_SENDER"</span>);
 * ws.{@link #setPongSenderName(String)
 * setPongSenderName}(<span style="color: darkred;">"PONG_SENDER"</span>);
 * </blockquote>
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
 * <h3 id="congestion_control">Congestion Control</h3>
 *
 * <p>
 * <code>send<i>Xxx</i></code> methods queue a {@link WebSocketFrame} instance to the
 * internal queue. By default, no upper limit is imposed on the queue size, so
 * <code>send<i>Xxx</i></code> methods do not block. However, this behavior may cause
 * a problem if your WebSocket client application sends too many WebSocket frames in
 * a short time for the WebSocket server to process. In such a case, you may want
 * <code>send<i>Xxx</i></code> methods to block when many frames are queued.
 * </p>
 *
 * <p>
 * You can set an upper limit on the internal queue by calling {@link #setFrameQueueSize(int)}
 * method. As a result, if the number of frames in the queue has reached the upper limit
 * when a <code>send<i>Xxx</i></code> method is called, the method blocks until the
 * queue gets spaces. The code snippet below is an example to set 5 as the upper limit
 * of the internal frame queue.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set 5 as the frame queue size.</span>
 * ws.{@link #setFrameQueueSize(int) setFrameQueueSize}(5);</pre>
 * </blockquote>
 *
 * <p>
 * Note that under some conditions, even if the queue is full, <code>send<i>Xxx</i></code>
 * methods do not block. For example, in the case where the thread to send frames
 * ({@code WritingThread}) is going to stop or has already stopped. In addition,
 * method calls to send a <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
 * >control frame</a> (e.g. {@link #sendClose()} and {@link #sendPing()}) do not block.
 * </p>
 *
 * <h3 id="maximum_payload_size">Maximum Payload Size</h3>
 *
 * <p>
 * You can set an upper limit on the payload size of WebSocket frames by calling
 * {@link #setMaxPayloadSize(int)} method with a positive value. Text, binary and
 * continuation frames whose payload size is bigger than the maximum payload size
 * you have set will be split into multiple frames.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Set 1024 as the maximum payload size.</span>
 * ws.{@link #setMaxPayloadSize(int) setMaxPayloadSize}(1024);</pre>
 * </blockquote>
 *
 * <p>
 * Control frames (close, ping and pong frames) are never split as per the specification.
 * </p>
 *
 * <p>
 * If permessage-deflate extension is enabled and if the payload size of a WebSocket
 * frame after compression does not exceed the maximum payload size, the WebSocket
 * frame is not split even if the payload size before compression execeeds the
 * maximum payload size.
 * </p>
 *
 * <h3 id="compression">Compression</h3>
 *
 * <p>
 * The <strong>permessage-deflate</strong> extension (<a href=
 * "http://tools.ietf.org/html/rfc7692">RFC 7692</a>) has been supported
 * since the version 1.17. To enable the extension, call {@link #addExtension(String)
 * addExtension} method with {@code "permessage-deflate"}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"><span style="color: green;"> // Enable "permessage-deflate" extension (RFC 7692).</span>
 * ws.{@link #addExtension(String) addExtension}({@link WebSocketExtension#PERMESSAGE_DEFLATE});</pre>
 * </blockquote>
 *
 * <h3>Missing Close Frame</h3>
 *
 * <p>
 * Some server implementations close a WebSocket connection without sending a
 * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a> to
 * a client in some cases. Strictly speaking, this is a violation against the
 * specification (<a href=
 * "https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455</a>). However, this
 * library has allowed the behavior by default since the version 1.29. Even if the
 * end of the input stream of a WebSocket connection were reached without a close
 * frame being received, it would trigger neither {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} method nor
 * {@link WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame)
 * onFrameError()} method of {@link WebSocketListener}. If you want to make a
 * {@code WebSocket} instance report an error in the case, pass {@code false} to
 * {@link #setMissingCloseFrameAllowed(boolean)} method.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"><span style="color: green;"
 * > // Make this library report an error when the end of the input stream
 * // of the WebSocket connection is reached before a close frame is read.</span>
 * ws.{@link #setMissingCloseFrameAllowed(boolean) setMissingCloseFrameAllowed}(false);</pre>
 * </blockquote>
 *
 * <h3>Direct Text Message</h3>
 *
 * <p>
 * When a text message was received, {@link WebSocketListener#onTextMessage(WebSocket, String)
 * onTextMessage(WebSocket, String)} is called. The implementation internally converts
 * the byte array of the text message into a {@code String} object before calling the
 * listener method. If you want to receive the byte array directly without the string
 * conversion, call {@link #setDirectTextMessage(boolean)} with {@code true}, and
 * {@link WebSocketListener#onTextMessage(WebSocket, byte[]) onTextMessage(WebSocket, byte[])}
 * will be called instead.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"><span style="color: green;"
 * > // Receive text messages without string conversion.</span>
 * ws.{@link #setDirectTextMessage(boolean) setDirectTextMessage}(true);</pre>
 * </blockquote>
 *
 * <h3>Disconnect WebSocket</h3>
 *
 * <p>
 * Before a WebSocket is closed, a closing handshake is performed. A closing handshake
 * is started (1) when the server sends a close frame to the client or (2) when the
 * client sends a close frame to the server. You can start a closing handshake by calling
 * {@link #disconnect()} method (or by sending a close frame manually).
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: green;">// Close the WebSocket connection.</span>
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
 * <p>
 * There is a variant of {@code recreate()} method that takes a timeout value for
 * socket connection. If you want to use a timeout value that is different from the
 * one used when the existing {@code WebSocket} instance was created, use {@link
 * #recreate(int) recreate(int timeout)} method.
 * </p>
 *
 * <p>
 * Note that you should not trigger reconnection in {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} method
 * because {@code onError()} may be called multiple times due to one error. Instead,
 * {@link WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame,
 * boolean) onDisconnected()} is the right place to trigger reconnection.
 * </p>
 *
 * <p>
 * Also note that the reason I use an expression of <i>"to trigger reconnection"</i>
 * instead of <i>"to call <code>recreate().connect()</code>"</i> is that I myself
 * won't do it <i>synchronously</i> in <code>WebSocketListener</code> callback
 * methods but will just schedule reconnection or will just go to the top of a kind
 * of <i>application loop</i> that repeats to establish a WebSocket connection until
 * it succeeds.
 * </p>
 *
 * <h3>Error Handling</h3>
 *
 * <p>
 * {@code WebSocketListener} has some {@code onXxxError()} methods such as {@link
 * WebSocketListener#onFrameError(WebSocket, WebSocketException, WebSocketFrame)
 * onFrameError()} and {@link
 * WebSocketListener#onSendError(WebSocket, WebSocketException, WebSocketFrame)
 * onSendError()}. Among such methods, {@link
 * WebSocketListener#onError(WebSocket, WebSocketException) onError()} is a special
 * one. It is always called before any other {@code onXxxError()} is called. For
 * example, in the implementation of {@code run()} method of {@code ReadingThread},
 * {@code Throwable} is caught and {@code onError()} and {@link
 * WebSocketListener#onUnexpectedError(WebSocket, WebSocketException)
 * onUnexpectedError()} are called in this order. The following is the implementation.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void run()
 * {
 *     try
 *     {
 *         main();
 *     }
 *     catch (Throwable t)
 *     {
 *         <span style="color: green;">// An uncaught throwable was detected in the reading thread.</span>
 *         {@link WebSocketException} cause = new WebSocketException(
 *             {@link WebSocketError}.{@link WebSocketError#UNEXPECTED_ERROR_IN_READING_THREAD UNEXPECTED_ERROR_IN_READING_THREAD},
 *             <span style="color: darkred;">"An uncaught throwable was detected in the reading thread"</span>, t);
 *
 *         <span style="color: green;">// Notify the listeners.</span>
 *         ListenerManager manager = mWebSocket.getListenerManager();
 *         manager.callOnError(cause);
 *         manager.callOnUnexpectedError(cause);
 *     }
 * }</pre>
 * </blockquote>
 *
 * <p>
 * So, you can handle all error cases in {@code onError()} method. However, note
 * that {@code onError()} may be called multiple times for one error cause, so don't
 * try to trigger reconnection in {@code onError()}. Instead, {@link
 * WebSocketListener#onDisconnected(WebSocket, WebSocketFrame, WebSocketFrame, boolean)
 * onDiconnected()} is the right place to trigger reconnection.
 * </p>
 *
 * <p>
 * All {@code onXxxError()} methods receive a {@link WebSocketException} instance
 * as the second argument (the first argument is a {@code WebSocket} instance). The
 * exception class provides {@link WebSocketException#getError() getError()} method
 * which returns a {@link WebSocketError} enum entry. Entries in {@code WebSocketError}
 * enum are possible causes of errors that may occur in the implementation of this
 * library. The error causes are so granular that they can make it easy for you to
 * find the root cause when an error occurs.
 * </p>
 *
 * <p>
 * {@code Throwable}s thrown by implementations of {@code onXXX()} callback methods
 * are passed to {@link WebSocketListener#handleCallbackError(WebSocket, Throwable)
 * handleCallbackError()} of {@code WebSocketListener}.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void {@link WebSocketListener#handleCallbackError(WebSocket, Throwable)
 * handleCallbackError}(WebSocket websocket, Throwable cause) throws Exception {
 *     <span style="color: green;">// Throwables thrown by onXxx() callback methods come here.</span>
 * }</pre>
 * </blockquote>
 *
 * <h3>Thread Callbacks</h3>
 *
 * <p>
 * Some threads are created internally in the implementation of {@code WebSocket}.
 * Known threads are as follows.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Internal Threads</caption>
 *   <thead>
 *     <tr>
 *       <th>THREAD TYPE</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link ThreadType#READING_THREAD READING_THREAD}</td>
 *       <td>A thread which reads WebSocket frames from the server.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#WRITING_THREAD WRITING_THREAD}</td>
 *       <td>A thread which sends WebSocket frames to the server.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#CONNECT_THREAD CONNECT_THREAD}</td>
 *       <td>A thread which calls {@link WebSocket#connect()} asynchronously.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link ThreadType#FINISH_THREAD FINISH_THREAD}</td>
 *       <td>A thread which does finalization of a {@code WebSocket} instance.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * The following callback methods of {@link WebSocketListener} are called according
 * to the life cycle of the threads.
 * </p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" style="border-collapse: collapse;">
 *   <caption>Thread Callbacks</caption>
 *   <thead>
 *     <tr>
 *       <th>METHOD</th>
 *       <th>DESCRIPTION</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread) onThreadCreated()}</td>
 *       <td>Called after a thread was created.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStarted(WebSocket, ThreadType, Thread) onThreadStarted()}</td>
 *       <td>Called at the beginning of the thread's {@code run()} method.</td>
 *     </tr>
 *     <tr>
 *       <td>{@link WebSocketListener#onThreadStopping(WebSocket, ThreadType, Thread) onThreadStopping()}</td>
 *       <td>Called at the end of the thread's {@code run()} method.</td>
 *     </tr>
 *   </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * For example, if you want to change the name of the reading thread,
 * implement {@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread)
 * onThreadCreated()} method like below.
 * </p>
 *
 * <blockquote>
 * <pre style="border-left: solid 5px lightgray;"> <span style="color: gray;">{@code @}Override</span>
 * public void {@link WebSocketListener#onThreadCreated(WebSocket, ThreadType, Thread)
 * onThreadCreated}(WebSocket websocket, {@link ThreadType} type, Thread thread)
 * {
 *     if (type == ThreadType.READING_THREAD)
 *     {
 *         thread.setName(<span style="color: darkred;">"READING_THREAD"</span>);
 *     }
 * }</pre>
 * </blockquote>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455">RFC 6455 (The WebSocket Protocol)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7692">RFC 7692 (Compression Extensions for WebSocket)</a>
 * @see <a href="https://github.com/TakahikoKawasaki/nv-websocket-client">[GitHub] nv-websocket-client</a>
 *
 * @author Takahiko Kawasaki
 */
public class WebSocket
{
    private static final long DEFAULT_CLOSE_DELAY = 10 * 1000L;
    private final WebSocketFactory mWebSocketFactory;
    private final SocketConnector mSocketConnector;
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
    private boolean mMissingCloseFrameAllowed = true;
    private boolean mDirectTextMessage;
    private int mFrameQueueSize;
    private int mMaxPayloadSize;
    private boolean mOnConnectedCalled;
    private Object mOnConnectedCalledLock = new Object();
    private boolean mReadingThreadStarted;
    private boolean mWritingThreadStarted;
    private boolean mReadingThreadFinished;
    private boolean mWritingThreadFinished;
    private WebSocketFrame mServerCloseFrame;
    private WebSocketFrame mClientCloseFrame;
    private PerMessageCompressionExtension mPerMessageCompressionExtension;


    WebSocket(WebSocketFactory factory, boolean secure, String userInfo,
            String host, String path, SocketConnector connector)
    {
        mWebSocketFactory  = factory;
        mSocketConnector   = connector;
        mStateManager      = new StateManager();
        mHandshakeBuilder  = new HandshakeBuilder(secure, userInfo, host, path);
        mListenerManager   = new ListenerManager(this);
        mPingSender        = new PingSender(this, new CounterPayloadGenerator());
        mPongSender        = new PongSender(this, new CounterPayloadGenerator());
    }


    /**
     * Create a new {@code WebSocket} instance that has the same settings
     * as this instance. Note that, however, settings you made on the raw
     * socket are not copied.
     *
     * <p>
     * The {@link WebSocketFactory} instance that you used to create this
     * {@code WebSocket} instance is used again.
     * </p>
     *
     * <p>
     * This method calls {@link #recreate(int)} with the timeout value that
     * was used when this instance was created. If you want to create a
     * socket connection with a different timeout value, use {@link
     * #recreate(int)} method instead.
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
        return recreate(mSocketConnector.getConnectionTimeout());
    }


    /**
     * Create a new {@code WebSocket} instance that has the same settings
     * as this instance. Note that, however, settings you made on the raw
     * socket are not copied.
     *
     * <p>
     * The {@link WebSocketFactory} instance that you used to create this
     * {@code WebSocket} instance is used again.
     * </p>
     *
     * @return
     *         A new {@code WebSocket} instance.
     *
     * @param timeout
     *         The timeout value in milliseconds for socket timeout.
     *         A timeout of zero is interpreted as an infinite timeout.
     *
     * @throws IllegalArgumentException
     *         The given timeout value is negative.
     *
     * @throws IOException
     *         {@link WebSocketFactory#createSocket(URI)} threw an exception.
     *
     * @since 1.10
     */
    public WebSocket recreate(int timeout) throws IOException
    {
        if (timeout < 0)
        {
            throw new IllegalArgumentException("The given timeout value is negative.");
        }

        WebSocket instance = mWebSocketFactory.createSocket(getURI(), timeout);

        // Copy the settings.
        instance.mHandshakeBuilder = new HandshakeBuilder(mHandshakeBuilder);
        instance.setPingInterval(getPingInterval());
        instance.setPongInterval(getPongInterval());
        instance.setPingPayloadGenerator(getPingPayloadGenerator());
        instance.setPongPayloadGenerator(getPongPayloadGenerator());
        instance.mExtended = mExtended;
        instance.mAutoFlush = mAutoFlush;
        instance.mMissingCloseFrameAllowed = mMissingCloseFrameAllowed;
        instance.mDirectTextMessage = mDirectTextMessage;
        instance.mFrameQueueSize = mFrameQueueSize;

        // Copy listeners.
        List<WebSocketListener> listeners = mListenerManager.getListeners();
        synchronized (listeners)
        {
            instance.addListeners(listeners);
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
     * Get the current state of this WebSocket.
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
     * Check if the current state of this WebSocket is {@link
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


    /**
     * Check if the current state is equal to the specified state.
     */
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
     * Remove a protocol from {@code Sec-WebSocket-Protocol}.
     *
     * @param protocol
     *         A protocol name. {@code null} is silently ignored.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket removeProtocol(String protocol)
    {
        mHandshakeBuilder.removeProtocol(protocol);

        return this;
    }


    /**
     * Remove all protocols from {@code Sec-WebSocket-Protocol}.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket clearProtocols()
    {
        mHandshakeBuilder.clearProtocols();

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
     * Add a value for {@code Sec-WebSocket-Extension}. The input string
     * should comply with the format described in <a href=
     * "https://tools.ietf.org/html/rfc6455#section-9.1">9.1. Negotiating
     * Extensions</a> in <a href="https://tools.ietf.org/html/rfc6455"
     * >RFC 6455</a>.
     *
     * @param extension
     *         A string that represents a WebSocket extension. If it does
     *         not comply with RFC 6455, no value is added to {@code
     *         Sec-WebSocket-Extension}.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket addExtension(String extension)
    {
        mHandshakeBuilder.addExtension(extension);

        return this;
    }


    /**
     * Remove an extension from {@code Sec-WebSocket-Extension}.
     *
     * @param extension
     *         An extension to remove. {@code null} is silently ignored.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket removeExtension(WebSocketExtension extension)
    {
        mHandshakeBuilder.removeExtension(extension);

        return this;
    }


    /**
     * Remove extensions from {@code Sec-WebSocket-Extension} by
     * an extension name.
     *
     * @param name
     *         An extension name. {@code null} is silently ignored.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket removeExtensions(String name)
    {
        mHandshakeBuilder.removeExtensions(name);

        return this;
    }


    /**
     * Remove all extensions from {@code Sec-WebSocket-Extension}.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket clearExtensions()
    {
        mHandshakeBuilder.clearExtensions();

        return this;
    }


    /**
     * Add a pair of extra HTTP header.
     *
     * @param name
     *         An HTTP header name. When {@code null} or an empty
     *         string is given, no header is added.
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
     * Remove pairs of extra HTTP headers.
     *
     * @param name
     *         An HTTP header name. {@code null} is silently ignored.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket removeHeaders(String name)
    {
        mHandshakeBuilder.removeHeaders(name);

        return this;
    }


    /**
     * Clear all extra HTTP headers.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket clearHeaders()
    {
        mHandshakeBuilder.clearHeaders();

        return this;
    }


    /**
     * Set the credentials to connect to the WebSocket endpoint.
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
     * Set the credentials to connect to the WebSocket endpoint.
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
     * Clear the credentials to connect to the WebSocket endpoint.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket clearUserInfo()
    {
        mHandshakeBuilder.clearUserInfo();

        return this;
    }


    /**
     * Check if extended use of WebSocket frames are allowed.
     *
     * <p>
     * When extended use is allowed, values of RSV1/RSV2/RSV3 bits
     * and opcode of frames are not checked. On the other hand,
     * if not allowed (default), non-zero values for RSV1/RSV2/RSV3
     * bits and unknown opcodes cause an error. In such a case,
     * {@link WebSocketListener#onFrameError(WebSocket,
     * WebSocketException, WebSocketFrame) onFrameError} method of
     * listeners are called and the WebSocket is eventually closed.
     * </p>
     *
     * @return
     *         {@code true} if extended use of WebSocket frames
     *         are allowed.
     */
    public boolean isExtended()
    {
        return mExtended;
    }


    /**
     * Allow or disallow extended use of WebSocket frames.
     *
     * @param extended
     *         {@code true} to allow extended use of WebSocket frames.
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
     * Check if this instance allows the server to close the WebSocket
     * connection without sending a <a href=
     * "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     * to this client. The default value is {@code true}.
     *
     * @return
     *         {@code true} if the configuration allows for the server to
     *         close the WebSocket connection without sending a close frame
     *         to this client. {@code false} if the configuration requires
     *         that an error be reported via
     *         {@link WebSocketListener#onError(WebSocket, WebSocketException)
     *         onError()} method and {@link WebSocketListener#onFrameError(WebSocket,
     *         WebSocketException, WebSocketFrame) onFrameError()} method of
     *         {@link WebSocketListener}.
     *
     * @since 1.29
     */
    public boolean isMissingCloseFrameAllowed()
    {
        return mMissingCloseFrameAllowed;
    }


    /**
     * Set whether to allow the server to close the WebSocket connection
     * without sending a <a href=
     * "https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     * to this client.
     *
     * @param allowed
     *         {@code true} to allow the server to close the WebSocket
     *         connection without sending a close frame to this client.
     *         {@code false} to make this instance report an error when the
     *         end of the input stream of the WebSocket connection is reached
     *         before a close frame is read.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.29
     */
    public WebSocket setMissingCloseFrameAllowed(boolean allowed)
    {
        mMissingCloseFrameAllowed = allowed;

        return this;
    }


    /**
     * Check if text messages are passed to listeners without string conversion.
     *
     * <p>
     * If this method returns {@code true}, when a text message is received,
     * {@link WebSocketListener#onTextMessage(WebSocket, byte[])
     * onTextMessage(WebSocket, byte[])} will be called instead of
     * {@link WebSocketListener#onTextMessage(WebSocket, String)
     * onTextMessage(WebSocket, String)}. The purpose of this behavior
     * is to skip internal string conversion which is performed in the
     * implementation of {@code ReadingThread}.
     * </p>
     *
     * @return
     *         {@code true} if text messages are passed to listeners without
     *         string conversion.
     *
     * @since 2.6
     */
    public boolean isDirectTextMessage()
    {
        return mDirectTextMessage;
    }


    /**
     * Set whether to receive text messages directly as byte arrays without
     * string conversion.
     *
     * <p>
     * If {@code true} is set to this property, when a text message is received,
     * {@link WebSocketListener#onTextMessage(WebSocket, byte[])
     * onTextMessage(WebSocket, byte[])} will be called instead of
     * {@link WebSocketListener#onTextMessage(WebSocket, String)
     * onTextMessage(WebSocket, String)}. The purpose of this behavior
     * is to skip internal string conversion which is performed in the
     * implementation of {@code ReadingThread}.
     * </p>
     *
     * @param direct
     *         {@code true} to receive text messages as byte arrays.
     *
     * @return
     *         {@code this} object.
     *
     * @since 2.6
     */
    public WebSocket setDirectTextMessage(boolean direct)
    {
        mDirectTextMessage = direct;

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
        }

        // Get the reference to the instance of WritingThread.
        WritingThread wt = mWritingThread;

        // If and only if an instance of WritingThread is available.
        if (wt != null)
        {
            // Request flush.
            wt.queueFlush();
        }

        return this;
    }


    /**
     * Get the size of the frame queue. The default value is 0 and it means
     * there is no limit on the queue size.
     *
     * @return
     *         The size of the frame queue.
     *
     * @since 1.15
     */
    public int getFrameQueueSize()
    {
        return mFrameQueueSize;
    }


    /**
     * Set the size of the frame queue. The default value is 0 and it means
     * there is no limit on the queue size.
     *
     * <p>
     * <code>send<i>Xxx</i></code> methods queue a {@link WebSocketFrame}
     * instance to the internal queue. If the number of frames in the queue
     * has reached the upper limit (which has been set by this method) when
     * a <code>send<i>Xxx</i></code> method is called, the method blocks
     * until the queue gets spaces.
     * </p>
     *
     * <p>
     * Under some conditions, even if the queue is full, <code>send<i>Xxx</i></code>
     * methods do not block. For example, in the case where the thread to send
     * frames ({@code WritingThread}) is going to stop or has already stopped.
     * In addition, method calls to send a <a href=
     * "https://tools.ietf.org/html/rfc6455#section-5.5">control frame</a> (e.g.
     * {@link #sendClose()} and {@link #sendPing()}) do not block.
     * </p>
     *
     * @param size
     *         The queue size. 0 means no limit. Negative numbers are not allowed.
     *
     * @return
     *         {@code this} object.
     *
     * @throws IllegalArgumentException
     *         {@code size} is negative.
     *
     * @since 1.15
     */
    public WebSocket setFrameQueueSize(int size) throws IllegalArgumentException
    {
        if (size < 0)
        {
            throw new IllegalArgumentException("size must not be negative.");
        }

        mFrameQueueSize = size;

        return this;
    }


    /**
     * Get the maximum payload size. The default value is 0 which means that
     * the maximum payload size is not set and as a result frames are not split.
     *
     * @return
     *         The maximum payload size. 0 means that the maximum payload size
     *         is not set.
     *
     * @since 1.27
     */
    public int getMaxPayloadSize()
    {
        return mMaxPayloadSize;
    }


    /**
     * Set the maximum payload size.
     *
     * <p>
     * Text, binary and continuation frames whose payload size is bigger than
     * the maximum payload size will be split into multiple frames. Note that
     * control frames (close, ping and pong frames) are not split as per the
     * specification even if their payload size exceeds the maximum payload size.
     * </p>
     *
     * @param size
     *         The maximum payload size. 0 to unset the maximum payload size.
     *
     * @return
     *         {@code this} object.
     *
     * @throws IllegalArgumentException
     *         {@code size} is negative.
     *
     * @since 1.27
     */
    public WebSocket setMaxPayloadSize(int size) throws IllegalArgumentException
    {
        if (size < 0)
        {
            throw new IllegalArgumentException("size must not be negative.");
        }

        mMaxPayloadSize = size;

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
     * Get the generator of payload of ping frames that are sent automatically.
     *
     * @return
     *         The generator of payload ping frames that are sent automatically.
     *
     * @since 1.20
     */
    public PayloadGenerator getPingPayloadGenerator()
    {
        return mPingSender.getPayloadGenerator();
    }


    /**
     * Set the generator of payload of ping frames that are sent automatically.
     *
     * @param generator
     *         The generator of payload ping frames that are sent automatically.
     *
     * @since 1.20
     */
    public WebSocket setPingPayloadGenerator(PayloadGenerator generator)
    {
        mPingSender.setPayloadGenerator(generator);

        return this;
    }


    /**
     * Get the generator of payload of pong frames that are sent automatically.
     *
     * @return
     *         The generator of payload pong frames that are sent automatically.
     *
     * @since 1.20
     */
    public PayloadGenerator getPongPayloadGenerator()
    {
        return mPongSender.getPayloadGenerator();
    }


    /**
     * Set the generator of payload of pong frames that are sent automatically.
     *
     * @param generator
     *         The generator of payload ppng frames that are sent automatically.
     *
     * @since 1.20
     */
    public WebSocket setPongPayloadGenerator(PayloadGenerator generator)
    {
        mPongSender.setPayloadGenerator(generator);

        return this;
    }


    /**
     * Get the name of the {@code Timer} that sends ping frames periodically.
     *
     * @return
     *         The {@code Timer}'s name.
     *
     * @since 2.5
     */
    public String getPingSenderName()
    {
        return mPingSender.getTimerName();
    }


    /**
     * Set the name of the {@code Timer} that sends ping frames periodically.
     *
     * @param name
     *         A name for the {@code Timer}.
     *
     * @return
     *         {@code this} object.
     *
     * @since 2.5
     */
    public WebSocket setPingSenderName(String name)
    {
        mPingSender.setTimerName(name);

        return this;
    }


    /**
     * Get the name of the {@code Timer} that sends pong frames periodically.
     *
     * @return
     *         The {@code Timer}'s name.
     *
     * @since 2.5
     */
    public String getPongSenderName()
    {
        return mPongSender.getTimerName();
    }


    /**
     * Set the name of the {@code Timer} that sends pong frames periodically.
     *
     * @param name
     *         A name for the {@code Timer}.
     *
     * @return
     *         {@code this} object.
     *
     * @since 2.5
     */
    public WebSocket setPongSenderName(String name)
    {
        mPongSender.setTimerName(name);

        return this;
    }


    /**
     * Add a listener to receive events on this WebSocket.
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
     * Add listeners.
     *
     * @param listeners
     *         Listeners to add. {@code null} is silently ignored.
     *         {@code null} elements in the list are ignored, too.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket addListeners(List<WebSocketListener> listeners)
    {
        mListenerManager.addListeners(listeners);

        return this;
    }


    /**
     * Remove a listener from this WebSocket.
     *
     * @param listener
     *         A listener to remove. {@code null} won't cause an error.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.13
     */
    public WebSocket removeListener(WebSocketListener listener)
    {
        mListenerManager.removeListener(listener);

        return this;
    }


    /**
     * Remove listeners.
     *
     * @param listeners
     *         Listeners to remove. {@code null} is silently ignored.
     *         {@code null} elements in the list are ignored, too.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.14
     */
    public WebSocket removeListeners(List<WebSocketListener> listeners)
    {
        mListenerManager.removeListeners(listeners);

        return this;
    }


    /**
     * Remove all the listeners from this WebSocket.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.13
     */
    public WebSocket clearListeners()
    {
        mListenerManager.clearListeners();

        return this;
    }


    /**
     * Get the raw socket which this WebSocket uses internally.
     *
     * @return
     *         The underlying {@link Socket} instance.
     */
    public Socket getSocket()
    {
        return mSocketConnector.getSocket();
    }


    /**
     * Get the URI of the WebSocket endpoint. The scheme part is either
     * {@code "ws"} or {@code "wss"}. The authority part is always empty.
     *
     * @return
     *         The URI of the WebSocket endpoint.
     *
     * @since 1.1
     */
    public URI getURI()
    {
        return mHandshakeBuilder.getURI();
    }


    /**
     * Connect to the server, send an opening handshake to the server,
     * receive the response and then start threads to communicate with
     * the server.
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
     * If the WebSocket endpoint requires Basic Authentication, you can set
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
     *           <li>The current state of the WebSocket is not {@link
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
            // Connect to the server.
            mSocketConnector.connect();

            // Perform WebSocket handshake.
            headers = shakeHands();
        }
        catch (WebSocketException e)
        {
            // Close the socket.
            mSocketConnector.closeSilently();

            // Change the state to CLOSED.
            mStateManager.setState(CLOSED);

            // Notify the listener of the state change.
            mListenerManager.callOnStateChanged(CLOSED);

            // The handshake failed.
            throw e;
        }

        // HTTP headers in the response from the server.
        mServerHeaders = headers;

        // Extensions.
        mPerMessageCompressionExtension = findAgreedPerMessageCompressionExtension();

        // Change the state to OPEN.
        mStateManager.setState(OPEN);

        // Notify the listener of the state change.
        mListenerManager.callOnStateChanged(OPEN);

        // Start threads that communicate with the server.
        startThreads();

        return this;
    }


    /**
     * Execute {@link #connect()} asynchronously using the given {@link
     * ExecutorService}. This method is just an alias of the following.
     *
     * <blockquote>
     * <code>executorService.{@link ExecutorService#submit(Callable) submit}({@link #connectable()})</code>
     * </blockquote>
     *
     * @param executorService
     *         An {@link ExecutorService} to execute a task created by
     *         {@link #connectable()}.
     *
     * @return
     *         The value returned from {@link ExecutorService#submit(Callable)}.
     *
     * @throws NullPointerException
     *         If the given {@link ExecutorService} is {@code null}.
     *
     * @throws RejectedExecutionException
     *         If the given {@link ExecutorService} rejected the task
     *         created by {@link #connectable()}.
     *
     * @see #connectAsynchronously()
     *
     * @since 1.7
     */
    public Future<WebSocket> connect(ExecutorService executorService)
    {
        return executorService.submit(connectable());
    }


    /**
     * Get a new {@link Callable}{@code <}{@link WebSocket}{@code >} instance
     * whose {@link Callable#call() call()} method calls {@link #connect()}
     * method of this {@code WebSocket} instance.
     *
     * @return
     *         A new {@link Callable}{@code <}{@link WebSocket}{@code >} instance
     *         for asynchronous {@link #connect()}.
     *
     * @see #connect(ExecutorService)
     *
     * @since 1.7
     */
    public Callable<WebSocket> connectable()
    {
        return new Connectable(this);
    }


    /**
     * Execute {@link #connect()} asynchronously by creating a new thread and
     * calling {@code connect()} in the thread. If {@code connect()} failed,
     * {@link WebSocketListener#onConnectError(WebSocket, WebSocketException)
     * onConnectError()} method of {@link WebSocketListener} is called.
     *
     * @return
     *         {@code this} object.
     *
     * @since 1.8
     */
    public WebSocket connectAsynchronously()
    {
        Thread thread = new ConnectThread(this);

        // Get the reference (just in case)
        ListenerManager lm = mListenerManager;

        if (lm != null)
        {
            lm.callOnThreadCreated(ThreadType.CONNECT_THREAD, thread);
        }

        thread.start();

        return this;
    }


    /**
     * Disconnect the WebSocket.
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
     * Disconnect the WebSocket.
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
     * Disconnect the WebSocket.
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
     * Disconnect the WebSocket.
     *
     * <p>
     * This method is an alias of {@link #disconnect(int, String, long)
     * disconnect}{@code (closeCode, reason, 10000L)}.
     * </p>
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
        return disconnect(closeCode, reason, DEFAULT_CLOSE_DELAY);
    }


    /**
     * Disconnect the WebSocket.
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
     * @param closeDelay
     *         Delay in milliseconds before calling {@link Socket#close()} forcibly.
     *         This safeguard is needed for the case where the server fails to send
     *         back a close frame. The default value is 10000 (= 10 seconds). When
     *         a negative value is given, the default value is used.
     *
     *         If a very short time (e.g. 0) is given, it is likely to happen either
     *         (1) that this client will fail to send a close frame to the server
     *         (in this case, you will probably see an error message "Flushing frames
     *         to the server failed: Socket closed") or (2) that the WebSocket
     *         connection will be closed before this client receives a close frame
     *         from the server (in this case, the second argument of {@link
     *         WebSocketListener#onDisconnected(WebSocket, WebSocketFrame,
     *         WebSocketFrame, boolean) WebSocketListener.onDisconnected} will be
     *         {@code null}).
     *
     * @return
     *         {@code this} object.
     *
     * @see WebSocketCloseCode
     *
     * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">RFC 6455, 5.5.1. Close</a>
     *
     * @since 1.26
     */
    public WebSocket disconnect(int closeCode, String reason, long closeDelay)
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

        // If a negative value is given.
        if (closeDelay < 0)
        {
            // Use the default value.
            closeDelay = DEFAULT_CLOSE_DELAY;
        }

        // Request the threads to stop.
        stopThreads(closeDelay);

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
     * Send a WebSocket frame to the server.
     *
     * <p>
     * This method just queues the given frame. Actual transmission
     * is performed asynchronously.
     * </p>
     *
     * <p>
     * When the current state of this WebSocket is not {@link
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
     *         A WebSocket frame to be sent to the server.
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
        }

        // The current state is either OPEN or CLOSING. Or, CLOSED.

        // Get the reference to the writing thread.
        WritingThread wt = mWritingThread;

        // Some applications call sendFrame() without waiting for the
        // notification of WebSocketListener.onConnected() (Issue #23),
        // and/or even after the connection is closed. That is, there
        // are chances that sendFrame() is called when mWritingThread
        // is null. So, it should be checked whether an instance of
        // WritingThread is available or not before calling queueFrame().
        if (wt == null)
        {
            // An instance of WritingThread is not available.
            return this;
        }

        // Split the frame into multiple frames if necessary.
        List<WebSocketFrame> frames = splitIfNecessary(frame);

        // Queue the frame or the frames. Even if the current state is
        // CLOSED, queueing won't be a big issue.

        // If the frame was not split.
        if (frames == null)
        {
            // Queue the frame.
            wt.queueFrame(frame);
        }
        else
        {
            for (WebSocketFrame f : frames)
            {
                // Queue the frame.
                wt.queueFrame(f);
            }
        }

        return this;
    }


    private List<WebSocketFrame> splitIfNecessary(WebSocketFrame frame)
    {
        return WebSocketFrame.splitIfNecessary(frame, mMaxPayloadSize, mPerMessageCompressionExtension);
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
                    "The current state of the WebSocket is not CREATED.");
            }

            // Change the state to CONNECTING.
            mStateManager.setState(CONNECTING);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(CONNECTING);
    }


    /**
     * Perform the opening handshake.
     */
    private Map<String, List<String>> shakeHands() throws WebSocketException
    {
        // The raw socket created by WebSocketFactory.
        Socket socket = mSocketConnector.getSocket();

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

        // Keep the input stream and the output stream to pass them
        // to the reading thread and the writing thread later.
        mInput  = input;
        mOutput = output;

        // The handshake succeeded.
        return headers;
    }


    /**
     * Open the input stream of the WebSocket connection.
     * The stream is used by the reading thread.
     */
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
                "Failed to get the input stream of the raw socket: " + e.getMessage(), e);
        }
    }


    /**
     * Open the output stream of the WebSocket connection.
     * The stream is used by the writing thread.
     */
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
                "Failed to get the output stream from the raw socket: " + e.getMessage(), e);
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
     *         A randomly generated WebSocket key.
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


    /**
     * Send an opening handshake request to the WebSocket server.
     */
    private void writeHandshake(WebSocketOutputStream output, String key) throws WebSocketException
    {
        // Generate an opening handshake sent to the server from this client.
        mHandshakeBuilder.setKey(key);
        String requestLine     = mHandshakeBuilder.buildRequestLine();
        List<String[]> headers = mHandshakeBuilder.buildHeaders();
        String handshake       = HandshakeBuilder.build(requestLine, headers);

        // Call onSendingHandshake() method of listeners.
        mListenerManager.callOnSendingHandshake(requestLine, headers);

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
                "Failed to send an opening handshake request to the server: " + e.getMessage(), e);
        }
    }


    /**
     * Receive an opening handshake response from the WebSocket server.
     */
    private Map<String, List<String>> readHandshake(WebSocketInputStream input, String key) throws WebSocketException
    {
        return new HandshakeReader(this).readHandshake(input, key);
    }


    /**
     * Start both the reading thread and the writing thread.
     *
     * <p>
     * The reading thread will call {@link #onReadingThreadStarted()}
     * as its first step. Likewise, the writing thread will call
     * {@link #onWritingThreadStarted()} as its first step. After
     * both the threads have started, {@link #onThreadsStarted()} is
     * called.
     * </p>
     */
    private void startThreads()
    {
        ReadingThread readingThread = new ReadingThread(this);
        WritingThread writingThread = new WritingThread(this);

        synchronized (mThreadsLock)
        {
            mReadingThread = readingThread;
            mWritingThread = writingThread;
        }

        // Execute onThreadCreated of the listeners.
        readingThread.callOnThreadCreated();
        writingThread.callOnThreadCreated();

        readingThread.start();
        writingThread.start();
    }


    /**
     * Stop both the reading thread and the writing thread.
     *
     * <p>
     * The reading thread will call {@link #onReadingThreadFinished(WebSocketFrame)}
     * as its last step. Likewise, the writing thread will call {@link
     * #onWritingThreadFinished(WebSocketFrame)} as its last step.
     * After both the threads have stopped, {@link #onThreadsFinished()}
     * is called.
     * </p>
     */
    private void stopThreads(long closeDelay)
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
            readingThread.requestStop(closeDelay);
        }

        if (writingThread != null)
        {
            writingThread.requestStop();
        }
    }


    /**
     * Get the input stream of the WebSocket connection.
     */
    WebSocketInputStream getInput()
    {
        return mInput;
    }


    /**
     * Get the output stream of the WebSocket connection.
     */
    WebSocketOutputStream getOutput()
    {
        return mOutput;
    }


    /**
     * Get the manager that manages the state of this {@code WebSocket} instance.
     */
    StateManager getStateManager()
    {
        return mStateManager;
    }


    /**
     * Get the manager that manages registered listeners.
     */
    ListenerManager getListenerManager()
    {
        return mListenerManager;
    }


    /**
     * Get the handshake builder. {@link HandshakeReader} uses this method.
     */
    HandshakeBuilder getHandshakeBuilder()
    {
        return mHandshakeBuilder;
    }


    /**
     * Set the agreed extensions. {@link HandshakeReader} uses this method.
     */
    void setAgreedExtensions(List<WebSocketExtension> extensions)
    {
        mAgreedExtensions = extensions;
    }


    /**
     * Set the agreed protocol. {@link HandshakeReader} uses this method.
     */
    void setAgreedProtocol(String protocol)
    {
        mAgreedProtocol = protocol;
    }


    /**
     * Called by the reading thread as its first step.
     */
    void onReadingThreadStarted()
    {
        boolean bothStarted = false;

        synchronized (mThreadsLock)
        {
            mReadingThreadStarted = true;

            if (mWritingThreadStarted)
            {
                // Both the reading thread and the writing thread have started.
                bothStarted = true;
            }
        }

        // Call onConnected() method of listeners if not called yet.
        callOnConnectedIfNotYet();

        // If both the reading thread and the writing thread have started.
        if (bothStarted)
        {
            onThreadsStarted();
        }
    }


    /**
     * Called by the writing thread as its first step.
     */
    void onWritingThreadStarted()
    {
        boolean bothStarted = false;

        synchronized (mThreadsLock)
        {
            mWritingThreadStarted = true;

            if (mReadingThreadStarted)
            {
                // Both the reading thread and the writing thread have started.
                bothStarted = true;
            }
        }

        // Call onConnected() method of listeners if not called yet.
        callOnConnectedIfNotYet();

        // If both the reading thread and the writing thread have started.
        if (bothStarted)
        {
            onThreadsStarted();
        }
    }


    /**
     * Call {@link WebSocketListener#onConnected(WebSocket, Map)} method
     * of the registered listeners if it has not been called yet. Either
     * the reading thread or the writing thread calls this method.
     */
    private void callOnConnectedIfNotYet()
    {
        synchronized (mOnConnectedCalledLock)
        {
            // If onConnected() has already been called.
            if (mOnConnectedCalled)
            {
                // Do not call onConnected() twice.
                return;
            }

            mOnConnectedCalled = true;
        }

        // Notify the listeners that the handshake succeeded.
        mListenerManager.callOnConnected(mServerHeaders);
    }


    /**
     * Called when both the reading thread and the writing thread have started.
     * This method is called in the context of either the reading thread or
     * the writing thread.
     */
    private void onThreadsStarted()
    {
        // Start sending ping frames periodically.
        // If the interval is zero, this call does nothing.
        mPingSender.start();

        // Likewise, start the pong sender.
        mPongSender.start();
    }


    /**
     * Called by the reading thread as its last step.
     */
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

        // Both the reading thread and the writing thread have finished.
        onThreadsFinished();
    }


    /**
     * Called by the writing thread as its last step.
     */
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

        // Both the reading thread and the writing thread have finished.
        onThreadsFinished();
    }


    /**
     * Called when both the reading thread and the writing thread have finished.
     * This method is called in the context of either the reading thread or
     * the writing thread.
     */
    private void onThreadsFinished()
    {
        finish();
    }


    void finish()
    {
        // Stop the ping sender and the pong sender.
        mPingSender.stop();
        mPongSender.stop();

        try
        {
            // Close the raw socket.
            mSocketConnector.getSocket().close();
        }
        catch (Throwable t)
        {
            // Ignore any error raised by close().
        }

        synchronized (mStateManager)
        {
            // Change the state to CLOSED.
            mStateManager.setState(CLOSED);
        }

        // Notify the listeners of the state change.
        mListenerManager.callOnStateChanged(CLOSED);

        // Notify the listeners that the WebSocket was disconnected.
        mListenerManager.callOnDisconnected(
            mServerCloseFrame, mClientCloseFrame, mStateManager.getClosedByServer());
    }


    /**
     * Call {@link #finish()} from within a separate thread.
     */
    private void finishAsynchronously()
    {
        WebSocketThread thread = new FinishThread(this);

        // Execute onThreadCreated() of the listeners.
        thread.callOnThreadCreated();

        thread.start();
    }


    /**
     * Find a per-message compression extension from among the agreed extensions.
     */
    private PerMessageCompressionExtension findAgreedPerMessageCompressionExtension()
    {
        if (mAgreedExtensions == null)
        {
            return null;
        }

        for (WebSocketExtension extension : mAgreedExtensions)
        {
            if (extension instanceof PerMessageCompressionExtension)
            {
                return (PerMessageCompressionExtension)extension;
            }
        }

        return null;
    }


    /**
     * Get the PerMessageCompressionExtension in the agreed extensions.
     * This method returns null if a per-message compression extension
     * is not found in the agreed extensions.
     */
    PerMessageCompressionExtension getPerMessageCompressionExtension()
    {
        return mPerMessageCompressionExtension;
    }
}
