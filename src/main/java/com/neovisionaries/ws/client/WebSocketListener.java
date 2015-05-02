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
     * @since 1.1
     */
    void onStateChanged(WebSocket websocket, WebSocketState newState);


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
     */
    void onConnected(WebSocket websocket, Map<String, List<String>> headers);


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
     */
    void onDisconnected(WebSocket websocket,
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer);


    /**
     * Called when a frame was received. This method is called before
     * an <code>on<i>Xxx</i>Frame</code> method is called.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a continuation frame (opcode = 0x0) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onContinuationFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a text frame (opcode = 0x1) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onTextFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a binary frame (opcode = 0x2) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onBinaryFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a close frame (opcode = 0x8) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onCloseFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a ping frame (opcode = 0x9) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onPingFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a pong frame (opcode = 0xA) was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The frame.
     */
    void onPongFrame(WebSocket websocket, WebSocketFrame frame);


    /**
     * Called when a text message was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param text
     *         The text message.
     */
    void onTextMessage(WebSocket websocket, String text);


    /**
     * Called when a binary message was received.
     *
     * @param websocket
     *         The web socket.
     *
     * @param binary
     *         The binary message.
     */
    void onBinaryMessage(WebSocket websocket, byte[] binary);


    /**
     * Called when a web socket frame was sent to the server
     * (but not flushed yet).
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The sent frame.
     */
    void onFrameSent(WebSocket websocket, WebSocketFrame frame);


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
     */
    void onFrameUnsent(WebSocket websocket, WebSocketFrame frame);


    /**
     * Call when an error occurred. This method is called before
     * an <code>on<i>Xxx</i>Error</code> method is called.
     *
     * @param websocket
     *         The web socket.
     *
     * @param cause
     *         An exception that represents the error.
     */
    void onError(WebSocket websocket, WebSocketException cause);


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
     */
    void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame);


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
     */
    void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames);


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
     */
    void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data);


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
     */
    void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame);


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
     */
    void onUnexpectedError(WebSocket websocket, WebSocketException cause);
}
