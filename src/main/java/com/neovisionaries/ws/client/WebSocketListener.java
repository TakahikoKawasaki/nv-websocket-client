/*
 * Copyright (C) 2015-2016 Neo Visionaries Inc.
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
 * Listener interface to receive web socket events.
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
     * Called after the state of the web socket changed.
     *
     * @param websocket
     *         The web socket.
     *
     * @param newState
     *         The new state of the web socket.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.1
     */
    void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception;


    /**
     * Called after the opening handshake of the web socket connection succeeded.
     *
     * @param websocket
     *         The web socket.
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
     *         The web socket.
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
     * Called after the web socket connection was closed.
     *
     * @param websocket
     *         The web socket.
     *
     * @param serverCloseFrame
     *         The close frame which the server sent to this client.
     *         This may be {@code null}.
     *
     * @param clientCloseFrame
     *         The close frame which this client sent to the server.
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
     *         The web socket.
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
     *         The web socket.
     *
     * @param frame
     *         The frame.
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
     *         The web socket.
     *
     * @param frame
     *         The frame.
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
     *         The web socket.
     *
     * @param frame
     *         The frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a close frame (opcode = 0x8) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a ping frame (opcode = 0x9) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a pong frame (opcode = 0xA) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     */
    void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a text message was received.
     *
     * @param websocket
     *         The web socket.
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
     * Called when a binary message was received.
     *
     * @param websocket
     *         The web socket.
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
     * Called before a web socket frame is sent.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame to be sent.
     *
     * @throws Exception
     *         An exception thrown by an implementation of this method.
     *         The exception is passed to {@link #handleCallbackError(WebSocket, Throwable)}.
     *
     * @since 1.15
     */
    void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception;


    /**
     * Called when a web socket frame was sent to the server
     * (but not flushed yet).
     *
     * @param websocket
     *         The web socket.
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
     * Called when a web socket frame was not sent to the server
     * because a close frame has already been sent.
     *
     * <p>
     * Note that {@code onFrameUnsent} is not called when {@link
     * #onSendError(WebSocket, WebSocketException, WebSocketFrame)
     * onSendError} is called.
     * </p>
     *
     * @param websocket
     *         The web socket.
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
     * Call when an error occurred. This method is called before
     * an <code>on<i>Xxx</i>Error</code> method is called.
     *
     * @param websocket
     *         The web socket.
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
     * Called when a frame failed to be read from the web socket.
     *
     * @param websocket
     *         The web socket.
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
     *         The socket frame. If this is not {@code null}, it means that
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
     *         The web socket.
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
     *         The web socket.
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
     *         The web socket.
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
     *         The web socket.
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
     *         The web socket.
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
     *         The web socket.
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
     *         The web socket.
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
