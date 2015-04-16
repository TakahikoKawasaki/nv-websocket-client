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
 * instance before calling {@link WebSocket#open()}.
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
     * Called after the opening handshake of the web socket connection succeeded.
     *
     * @param websocket
     *         The web socket.
     *
     * @param headers
     *         HTTP headers received from the server. Keys of the map are
     *         capitalized HTTP header names such as "SEC-WEBSOCKET-ACCEPT".
     */
    void onOpen(WebSocket websocket, Map<String, List<String>> headers);


    /**
     * Called when a frame failed to be read from the web socket.
     *
     * @param websocket
     *         The web socket.
     *
     * @param frame
     *         The socket frame. If this is not {@code null}, it means that
     *         verification of the frame failed.
     *
     * @param exception
     *         An exception that represents the error. When the error occurred
     *         because of {@link java.io.InterruptedIOException InterruptedIOException},
     *         {@code exception.getError()} returns {@link WebSocketError#INTERRUPTED_IN_READING}.
     *         For other IO errors, {@code exception.getError()} returns {@link
     *         WebSocketError#IO_ERROR_IN_READING}. Other error codes denote
     *         protocol errors, which imply that some bugs may exist in either
     *         or both of the client-side and the server-side implementations.
     */
    void onFrameError(WebSocket websocket, WebSocketFrame frame, WebSocketException exception);


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
}
