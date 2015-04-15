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


import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.BINARY;
import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.CLOSE;
import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.CONTINUATION;
import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.PING;
import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.PONG;
import static com.neovisionaries.ws.client.WebSocketFrame.Opcode.TEXT;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.Map;


class WebSocketThread extends Thread
{
    private final WebSocket mWebSocket;
    private final Map<String, List<String>> mHeaders;
    private boolean mStopRequested;


    public WebSocketThread(WebSocket websocket, Map<String, List<String>> headers)
    {
        mWebSocket = websocket;
        mHeaders   = headers;
    }


    @Override
    public void run()
    {
        // Notify listeners that the handshake succeeded.
        callOnOpen();

        while (true)
        {
            synchronized (this)
            {
                if (mStopRequested)
                {
                    break;
                }
            }

            // Receive a frame from the server.
            WebSocketFrame frame = readFrame();

            if (frame == null)
            {
                // Something unexpected happened.
                break;
            }

            handleFrame(frame);
        }
    }


    void close()
    {
        // TODO
    }


    /**
     * Call {@link WebSocketListener#onOpen(WebSocket, Map) onOpen} method
     * of the listeners.
     */
    private void callOnOpen()
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onOpen(websocket, mHeaders);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onFrameError(WebSocket, WebSocketFrame,
     * WebSocketException) onFrameError} method of the listeners.
     */
    private void callOnFrameError(WebSocketFrame frame, WebSocketException exception)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onFrameError(websocket, frame, exception);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onFrame(WebSocket, WebSocketFrame) onFrame}
     * method of the listeners.
     */
    private void callOnFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onContinuationFrame(WebSocket, WebSocketFrame)
     * onContinuationFrame} method of the listeners.
     */
    private void callOnContinuationFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onContinuationFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onTextFrame(WebSocket, WebSocketFrame)
     * onTextFrame} method of the listeners.
     */
    private void callOnTextFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onTextFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onBinaryFrame(WebSocket, WebSocketFrame)
     * onBinaryFrame} method of the listeners.
     */
    private void callOnBinaryFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onBinaryFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onCloseFrame(WebSocket, WebSocketFrame)
     * onCloseFrame} method of the listeners.
     */
    private void callOnCloseFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onCloseFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onPingFrame(WebSocket, WebSocketFrame)
     * onPingFrame} method of the listeners.
     */
    private void callOnPingFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onPingFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onPongFrame(WebSocket, WebSocketFrame)
     * onPongFrame} method of the listeners.
     */
    private void callOnPongFrame(WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onPongFrame(websocket, frame);
            }
        });
    }


    private WebSocketFrame readFrame()
    {
        WebSocketFrame frame = null;
        WebSocketException wse = null;

        try
        {
            // Receive a frame from the server.
            frame = mWebSocket.getInput().readFrame();

            // Verify the frame. If invalid, WebSocketException is thrown.
            verifyFrame(frame);

            // Return the verified frame.
            return frame;
        }
        catch (InterruptedIOException e)
        {
            // Interruption occurred while a frame was being read from the web socket.
            wse = new WebSocketException(
                WebSocketError.INTERRUPTED_IN_READING,
                "Interruption occurred while a frame was being read from the web socket.", e);
        }
        catch (IOException e)
        {
            // An I/O error occurred while a frame was being read from the web socket.
            wse = new WebSocketException(
                WebSocketError.IO_ERROR_IN_READING,
                "An I/O error occurred while a frame was being read from the web socket.", e);
        }
        catch (WebSocketException e)
        {
            // A protocol error.
            wse = e;
        }

        // Notify the listeners that an error occurred while a frame was being read.
        callOnFrameError(frame, wse);

        // TODO
        // Send a close frame to the server if any has not been sent yet.

        // A frame is not available.
        return null;
    }


    private void verifyFrame(WebSocketFrame frame) throws WebSocketException
    {
        // TODO
    }


    private void handleFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a frame was received.
        callOnFrame(frame);

        switch (frame.getOpcode())
        {
            case CONTINUATION:
                handleContinuationFrame(frame);
                break;

            case TEXT:
                handleTextFrame(frame);
                break;

            case BINARY:
                handleBinaryFrame(frame);
                break;

            case CLOSE:
                handleCloseFrame(frame);
                break;

            case PING:
                handlePingFrame(frame);
                break;

            case PONG:
                handlePongFrame(frame);
                break;

            default:
                // This never happens because the frame has already been verified.
                break;
        }
    }


    private void handleContinuationFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a continuation frame was received.
        callOnContinuationFrame(frame);
    }


    private void handleTextFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a text frame was received.
        callOnTextFrame(frame);
    }


    private void handleBinaryFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a binary frame was received.
        callOnBinaryFrame(frame);
    }


    private void handleCloseFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a close frame was received.
        callOnCloseFrame(frame);

        // TODO
        // Send a close frame if not yet, and close the connection.
    }


    private void handlePingFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a ping frame was received.
        callOnPingFrame(frame);

        // TODO
        // Send back a pong frame.
    }


    private void handlePongFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a pong frame was received.
        callOnPongFrame(frame);
    }
}
