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
 * An empty implementation of {@link WebSocketListener} interface.
 *
 * @see WebSocketListener
 */
public class WebSocketAdapter implements WebSocketListener
{
    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState)
    {
    }


    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
    }


    @Override
    public void onDisconnected(WebSocket websocket,
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer)
    {
    }


    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onTextMessage(WebSocket websocket, String text)
    {
    }


    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary)
    {
    }


    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame)
    {
    }


    @Override
    public void onError(WebSocket websocket, WebSocketException cause)
    {
    }


    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame)
    {
    }


    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames)
    {
    }


    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data)
    {
    }


    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame)
    {
    }


    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause)
    {
    }
}
