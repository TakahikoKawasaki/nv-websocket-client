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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class ListenerManager
{
    private final WebSocket mWebSocket;
    private final List<WebSocketListener> mListeners = new ArrayList<WebSocketListener>();


    public ListenerManager(WebSocket websocket)
    {
        mWebSocket = websocket;
    }


    public List<WebSocketListener> getListeners()
    {
        return mListeners;
    }


    public void addListener(WebSocketListener listener)
    {
        if (listener == null)
        {
            return;
        }

        synchronized (mListeners)
        {
            mListeners.add(listener);
        }
    }


    public void callOnStateChanged(WebSocketState newState)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onStateChanged(mWebSocket, newState);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnConnected(Map<String, List<String>> headers)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onConnected(mWebSocket, headers);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnDisconnected(
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onDisconnected(
                        mWebSocket, serverCloseFrame, clientCloseFrame, closedByServer);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnContinuationFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onContinuationFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnTextFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onTextFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnBinaryFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onBinaryFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnCloseFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onCloseFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnPingFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onPingFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnPongFrame(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onPongFrame(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnTextMessage(String message)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onTextMessage(mWebSocket, message);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnBinaryMessage(byte[] message)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onBinaryMessage(mWebSocket, message);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnFrameSent(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onFrameSent(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnFrameUnsent(WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onFrameUnsent(mWebSocket, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnError(WebSocketException cause)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onError(mWebSocket, cause);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnFrameError(WebSocketException cause, WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onFrameError(mWebSocket, cause, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnMessageError(WebSocketException cause, List<WebSocketFrame> frames)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onMessageError(mWebSocket, cause, frames);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnTextMessageError(WebSocketException cause, byte[] data)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onTextMessageError(mWebSocket, cause, data);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnSendError(WebSocketException cause, WebSocketFrame frame)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onSendError(mWebSocket, cause, frame);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }


    public void callOnUnexpectedError(WebSocketException cause)
    {
        synchronized (mListeners)
        {
            for (WebSocketListener listener : mListeners)
            {
                try
                {
                    listener.onUnexpectedError(mWebSocket, cause);
                }
                catch (Throwable t)
                {
                }
            }
        }
    }
}
