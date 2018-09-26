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
 * An empty implementation of {@link WebSocketListener} interface.
 *
 * @see WebSocketListener
 */
public class WebSocketAdapter implements WebSocketListener
{
    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception
    {
    }


    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception
    {
    }


    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception
    {
    }


    @Override
    public void onDisconnected(WebSocket websocket,
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer) throws Exception
    {
    }


    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception
    {
    }


    @Override
    public void onTextMessage(WebSocket websocket, byte[] data) throws Exception
    {
    }


    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception
    {
    }


    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception
    {
    }


    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception
    {
    }


    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception
    {
    }


    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception
    {
    }


    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception
    {
    }


    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
    {
    }


    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception
    {
    }


    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception
    {
    }


    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
    }


    @Override
    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
    }


    @Override
    public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
    }
}
