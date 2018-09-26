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


import java.util.List;
import java.util.Map;


/**
 * Listener interface to receive WebSocket events.
 *
 * <p>
 * An implementation of this interface should be added by {@link
 * WebSocket#addListener(WebSocketListener)} to a {@link WebSocket}
 * instance before calling {@link WebSocket#connect()}.
 * </p>
 *
 * <p>
 * {@link WebSocketAdapter} is an empty implementation of this interface.
 * </p>
 *
 * @see WebSocket#addListener(WebSocketListener)
 * @see WebSocketAdapter
 */
public interface WebSocketListener
{
    /**
     * Called after the state of the WebSocket changed.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param newState
     *         The new state of the WebSocket.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.1
     */
    void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception;


    /**
     * Called after the opening handshake of the WebSocket connection succeeded.
     *
     * @param websocket
     *         The WebSsocket.
     *
     * @param headers
     *         HTTP headers received from the server. Keys of the map are
     *         HTTP header names such as {@code "Sec-WebSocket-Accept"}.
     *         Note that the comparator used by the map is {@link
     *         String#CASE_INSENSITIVE_ORDER}.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception;


    /**
     * Called when {@link WebSocket#connectAsynchronously()} failed.
     *
     * <p>
     * Note that this method is called only when {@code connectAsynchronously()}
     * was used and the {@link WebSocket#connect() connect()} executed in the
     * background thread failed. Neither direct synchronous {@code connect()}
     * nor {@link WebSocket#connect(java.util.concurrent.ExecutorService)
     * connect(ExecutorService)} will trigger this callback method.
     * </p>
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         The exception thrown by {@link WebSocket#connect() connect()}
     *         method.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.8
     */
    void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception;


    /**
     * Called after the WebSocket connection was closed.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param serverCloseFrame
     *         The <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
     *         >close frame</a> which the server sent to this client.
     *         This may be {@code null}.
     *
     * @param clientCloseFrame
     *         The <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
     *         >close frame</a> which this client sent to the server.
     *         This may be {@code null}.
     *
     * @param closedByServer
     *         {@code true} if the closing handshake was started by the server.
     *         {@code false} if the closing handshake was started by the client.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onDisconnected(WebSocket websocket,
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer) throws Exception;


    /**
     * Called when a frame was received. This method is called before
     * an <code>on<i>Xxx</i>Frame</code> method is called.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a continuation frame (opcode = 0x0) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The continuation frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a text frame (opcode = 0x1) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The text frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a binary frame (opcode = 0x2) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The binary frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
     * >close frame</a> (opcode = 0x8) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2"
     * >ping frame</a> (opcode = 0x9) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The <a href="https://tools.ietf.org/html/rfc6455#section-5.5.2">ping frame</a>.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3"
     * >pong frame</a> (opcode = 0xA) was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The <a href="https://tools.ietf.org/html/rfc6455#section-5.5.3">pong frame</a>.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a text message was received.
     *
     * <p>
     * When {@link WebSocket#isDirectTextMessage()} returns {@code true},
     * {@link #onTextMessage(WebSocket, byte[])} will be called instead of
     * this method (since version 2.6).
     * </p>
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param text
     *         The text message.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onTextMessage(WebSocket websocket, String text) throws Exception;


    /**
     * Called when a text message was received instead of
     * {@link #onTextMessage(WebSocket, String)} when {@link WebSocket#isDirectTextMessage()}
     * returns {@code true}.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param data
     *         The UTF-8 byte sequence of the text message.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 2.6
     */
    void onTextMessage(WebSocket websocket, byte[] data) throws Exception;


    /**
     * Called when a binary message was received.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param binary
     *         The binary message.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception;


    /**
     * Called before a WebSocket frame is sent.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The WebSocket frame to be sent.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.15
     */
    void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a WebSocket frame was sent to the server
     * (but not flushed yet).
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The sent frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a WebSocket frame was not sent to the server
     * because a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1"
     * >close frame</a> has already been sent.
     *
     * <p>
     * Note that {@code onFrameUnsent} is not called when {@link
     * #onSendError(WebSocket, WebSocketException, WebSocketFrame)
     * onSendError} is called.
     * </p>
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param frame
     *         The unsent frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called between after a thread is created and before the thread's
     * {@code start()} method is called.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param threadType
     *         The thread type.
     *
     * @param thread
     *         The newly created thread instance.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 2.0
     */
    void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception;


    /**
     * Called at the very beginning of the thread's {@code run()} method implementation.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param threadType
     *         The thread type.
     *
     * @param thread
     *         The thread instance.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 2.0
     */
    void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception;


    /**
     * Called at the very end of the thread's {@code run()} method implementation.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param threadType
     *         The thread type.
     *
     * @param thread
     *         The thread instance.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 2.0
     */
    void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception;


    /**
     * Call when an error occurred. This method is called before
     * an <code>on<i>Xxx</i>Error</code> method is called.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onError(WebSocket websocket, WebSocketException cause) throws Exception;


    /**
     * Called when a WebSocket frame failed to be read from the WebSocket.
     *
     * <p>
     * Some WebSocket server implementations close a WebSocket connection without sending
     * a <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a> to a
     * client in some cases. Strictly speaking, this behavior is a violation against the
     * specification (<a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>). However,
     * this library has allowed the behavior by default since the version 1.29. Even if
     * the end of the input stream of a WebSocket connection were reached without a
     * close frame being received, it would trigger neither {@link #onError(WebSocket,
     * WebSocketException) onError()} method nor {@link #onFrameError(WebSocket,
     * WebSocketException, WebSocketFrame) onFrameError()} method. If you want to make
     * this library report an error in the case, pass {@code false} to {@link
     * WebSocket#setMissingCloseFrameAllowed(boolean)} method.
     * </p>
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error. When the error occurred
     *         because of {@link java.io.InterruptedIOException InterruptedIOException},
     *         {@code exception.getError()} returns {@link WebSocketError#INTERRUPTED_IN_READING}.
     *         For other IO errors, {@code exception.getError()} returns {@link
     *         WebSocketError#IO_ERROR_IN_READING}. Other error codes denote
     *         protocol errors, which imply that some bugs may exist in either
     *         or both of the client-side and the server-side implementations.
     *
     * @param frame
     *         The WebSocket frame. If this is not {@code null}, it means that
     *         verification of the frame failed.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception;


    /**
     * Called when it failed to concatenate payloads of multiple frames
     * to construct a message. The reason of the failure is probably
     * out-of-memory.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error.
     *
     * @param frames
     *         The list of frames that form a message. The first element
     *         is either a text frame and a binary frame, and the other
     *         frames are continuation frames.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception;


    /**
     * Called when a message failed to be decompressed.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error.
     *
     * @param compressed
     *         The compressed message that failed to be decompressed.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.16
     */
    void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception;


    /**
     * Called when it failed to convert payload data into a string.
     * The reason of the failure is probably out-of-memory.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error.
     *
     * @param data
     *         The payload data that failed to be converted to a string.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception;


    /**
     * Called when an error occurred when a frame was tried to be sent
     * to the server.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         An exception that represents the error.
     *
     * @param frame
     *         The frame which was tried to be sent. This is {@code null}
     *         when the error code of the exception is {@link
     *         WebSocketError#FLUSH_ERROR FLUSH_ERROR}.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception;


    /**
     * Called when an uncaught throwable was detected in either the
     * reading thread (which reads frames from the server) or the
     * writing thread (which sends frames to the server).
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         The cause of the error.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception;


    /**
     * Called when an <code>on<i>Xxx</i>()</code> method threw a {@code Throwable}.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param cause
     *         The {@code Throwable} an <code>on<i>Xxx</i></code> method threw.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is just ignored.
     *
     * @since 1.9
     */
    void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception;


    /**
     * Called before an opening handshake is sent to the server.
     *
     * @param websocket
     *         The WebSocket.
     *
     * @param requestLine
     *         The request line. For example, {@code "GET /chat HTTP/1.1"}.
     *
     * @param headers
     *         The HTTP headers.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.21
     */
    void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception;
}
