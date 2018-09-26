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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class ListenerManager
{
    private final WebSocket mWebSocket;
    private final List<WebSocketListener> mListeners = new ArrayList<WebSocketListener>();

    private boolean mSyncNeeded = true;
    private List<WebSocketListener> mCopiedListeners;


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
            mSyncNeeded = true;
        }
    }


    public void addListeners(List<WebSocketListener> listeners)
    {
        if (listeners == null)
        {
            return;
        }

        synchronized (mListeners)
        {
            for (WebSocketListener listener : listeners)
            {
                if (listener == null)
                {
                    continue;
                }

                mListeners.add(listener);
                mSyncNeeded = true;
            }
        }
    }


    public void removeListener(WebSocketListener listener)
    {
        if (listener == null)
        {
            return;
        }

        synchronized (mListeners)
        {
            if (mListeners.remove(listener))
            {
                mSyncNeeded = true;
            }
        }
    }


    public void removeListeners(List<WebSocketListener> listeners)
    {
        if (listeners == null)
        {
            return;
        }

        synchronized (mListeners)
        {
            for (WebSocketListener listener : listeners)
            {
                if (listener == null)
                {
                    continue;
                }

                if (mListeners.remove(listener))
                {
                    mSyncNeeded = true;
                }
            }
        }
    }


    public void clearListeners()
    {
        synchronized (mListeners)
        {
            if (mListeners.size() == 0)
            {
                return;
            }

            mListeners.clear();
            mSyncNeeded = true;
        }
    }


    private List<WebSocketListener> getSynchronizedListeners()
    {
        synchronized (mListeners)
        {
            if (mSyncNeeded == false)
            {
                return mCopiedListeners;
            }

            // Copy mListeners to copiedListeners.
            List<WebSocketListener> copiedListeners =
                    new ArrayList<WebSocketListener>(mListeners.size());

            for (WebSocketListener listener : mListeners)
            {
                copiedListeners.add(listener);
            }

            // Synchronize.
            mCopiedListeners = copiedListeners;
            mSyncNeeded      = false;

            return copiedListeners;
        }
    }


    public void callOnStateChanged(WebSocketState newState)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onStateChanged(mWebSocket, newState);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnConnected(Map<String, List<String>> headers)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onConnected(mWebSocket, headers);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnConnectError(WebSocketException cause)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onConnectError(mWebSocket, cause);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnDisconnected(
        WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
        boolean closedByServer)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onDisconnected(
                    mWebSocket, serverCloseFrame, clientCloseFrame, closedByServer);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnContinuationFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onContinuationFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnTextFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onTextFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnBinaryFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onBinaryFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnCloseFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onCloseFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnPingFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onPingFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnPongFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onPongFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnTextMessage(String message)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onTextMessage(mWebSocket, message);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnTextMessage(byte[] data)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onTextMessage(mWebSocket, data);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnBinaryMessage(byte[] message)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onBinaryMessage(mWebSocket, message);
            }
            catch (Throwable t)
            {
                    callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnSendingFrame(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onSendingFrame(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnFrameSent(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onFrameSent(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnFrameUnsent(WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onFrameUnsent(mWebSocket, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnThreadCreated(ThreadType threadType, Thread thread)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onThreadCreated(mWebSocket, threadType, thread);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnThreadStarted(ThreadType threadType, Thread thread)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onThreadStarted(mWebSocket, threadType, thread);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnThreadStopping(ThreadType threadType, Thread thread)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onThreadStopping(mWebSocket, threadType, thread);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnError(WebSocketException cause)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onError(mWebSocket, cause);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnFrameError(WebSocketException cause, WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onFrameError(mWebSocket, cause, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnMessageError(WebSocketException cause, List<WebSocketFrame> frames)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onMessageError(mWebSocket, cause, frames);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnMessageDecompressionError(WebSocketException cause, byte[] compressed)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onMessageDecompressionError(mWebSocket, cause, compressed);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnTextMessageError(WebSocketException cause, byte[] data)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onTextMessageError(mWebSocket, cause, data);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnSendError(WebSocketException cause, WebSocketFrame frame)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onSendError(mWebSocket, cause, frame);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    public void callOnUnexpectedError(WebSocketException cause)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onUnexpectedError(mWebSocket, cause);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }


    private void callHandleCallbackError(WebSocketListener listener, Throwable cause)
    {
        try
        {
            listener.handleCallbackError(mWebSocket, cause);
        }
        catch (Throwable t)
        {
        }
    }


    public void callOnSendingHandshake(String requestLine, List<String[]> headers)
    {
        for (WebSocketListener listener : getSynchronizedListeners())
        {
            try
            {
                listener.onSendingHandshake(mWebSocket, requestLine, headers);
            }
            catch (Throwable t)
            {
                callHandleCallbackError(listener, t);
            }
        }
    }
}
