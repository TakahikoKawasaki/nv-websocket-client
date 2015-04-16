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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.neovisionaries.ws.client.WebSocketFrame.Opcode;


class WebSocketThread extends Thread
{
    private final WebSocket mWebSocket;
    private final Map<String, List<String>> mHeaders;
    private boolean mStopRequested;
    private List<WebSocketFrame> mContinuation = new ArrayList<WebSocketFrame>();


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
    private void callOnFrameError(final WebSocketFrame frame, final WebSocketException exception)
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
    private void callOnFrame(final WebSocketFrame frame)
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
    private void callOnContinuationFrame(final WebSocketFrame frame)
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
    private void callOnTextFrame(final WebSocketFrame frame)
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
    private void callOnBinaryFrame(final WebSocketFrame frame)
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
    private void callOnCloseFrame(final WebSocketFrame frame)
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
    private void callOnPingFrame(final WebSocketFrame frame)
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
    private void callOnPongFrame(final WebSocketFrame frame)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onPongFrame(websocket, frame);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onTextMessage(WebSocket, String)
     * onTextMessage} method of the listeners.
     */
    private void callOnTextMessage(byte[] data)
    {
        try
        {
            // Interpret the byte array as a UTF-8 string.
            String text = (data == null) ? null : new String(data, "UTF-8");

            // Call onTextMessage() method of the listeners.
            callOnTextMessage(text);
        }
        catch (UnsupportedEncodingException e)
        {
            // This never happens.
        }
    }


    /**
     * Call {@link WebSocketListener#onTextMessage(WebSocket, String)
     * onTextMessage} method of the listeners.
     */
    private void callOnTextMessage(final String text)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onTextMessage(websocket, text);
            }
        });
    }


    /**
     * Call {@link WebSocketListener#onBinaryMessage(WebSocket, String)
     * onBinaryMessage} method of the listeners.
     */
    private void callOnBinaryMessage(final byte[] binary)
    {
        mWebSocket.callListenerMethod(new WebSocketListenerMethodCaller() {
            @Override
            public void call(WebSocket websocket, WebSocketListener listener)
            {
                listener.onBinaryMessage(websocket, binary);
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
        // Verify RSV1, RSV2 and RSV3.
        verifyReservedBits(frame);

        // The opcode of the frame must be known.
        verifyFrameOpcode(frame);

        // Frames from the server must not be masked.
        verifyFrameMask(frame);

        // Verify fragmentation conditions.
        verifyFrameFragmentation(frame);

        // Verify the size of the payload.
        verifyFrameSize(frame);
    }


    private void verifyReservedBits(WebSocketFrame frame) throws WebSocketException
    {
        // RSV1, RSV2, RSV3
        //
        // The specification requires that these bits "be 0 unless an extension
        // is negotiated that defines meanings for non-zero values".
        //
        // However, this implementation does not check these bits here intentionally
        // until this library is improved to provide a mechanism to delegate the
        // task to external extensions.
    }


    /**
     * Ensure that the opcode of the give frame is a known one.
     *
     * <blockquote>
     * <p>From RFC 6455, 5.2. Base Framing Protocol</p>
     * <p><i>
     * If an unknown opcode is received, the receiving endpoint MUST
     * Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     */
    private void verifyFrameOpcode(WebSocketFrame frame) throws WebSocketException
    {
        switch (frame.getOpcode())
        {
            case Opcode.CONTINUATION:
            case Opcode.TEXT:
            case Opcode.BINARY:
            case Opcode.CLOSE:
            case Opcode.PING:
            case Opcode.PONG:
                // Known opcode
                return;

            default:
                // A frame has an unknown opcode.
                throw new WebSocketException(
                    WebSocketError.UNKNOWN_OPCODE,
                    "A frame has an unknown opcode: 0x" + Integer.toHexString(frame.getOpcode()));
        }
    }


    /**
     * Ensure that the given frame is not masked.
     *
     * <blockquote>
     * <p>From RFC 6455, 5.1. Overview:</p>
     * <p><i>
     * A server MUST NOT mask any frames that it sends to the client.
     * A client MUST close a connection if it detects a masked frame.
     * </i></p>
     * </blockquote>
     */
    private void verifyFrameMask(WebSocketFrame frame) throws WebSocketException
    {
        // If the frame is masked.
        if (frame.isMasked())
        {
            // A frame from the server is masked.
            throw new WebSocketException(
                WebSocketError.FRAME_MASKED,
                "A frame from the server is masked.");
        }
    }


    private void verifyFrameFragmentation(WebSocketFrame frame) throws WebSocketException
    {
        // Control frames (see Section 5.5) MAY be injected in the
        // middle of a fragmented message. Control frames themselves
        // MUST NOT be fragmented.
        if (frame.isControlFrame())
        {
            // If fragmented.
            if (frame.getFin() == false)
            {
                // A control frame is fragmented.
                throw new WebSocketException(
                    WebSocketError.FRAGMENTED_CONTROL_FRAME,
                    "A control frame is fragmented.");
            }

            // No more requirements on a control frame.
            return;
        }

        // True if a continuation has already started.
        boolean continuationExists = (mContinuation.size() == 0);

        // If the frame is a continuation frame.
        if (frame.getOpcode() == Opcode.CONTINUATION)
        {
            // There must already exist a continuation sequence.
            if (continuationExists == false)
            {
                // A continuation frame was detected although a continuation had not started.
                throw new WebSocketException(
                    WebSocketError.UNEXPECTED_CONTINUATION_FRAME,
                    "A continuation frame was detected although a continuation had not started.");
            }

            // No more requirements on a continuation frame.
            return;
        }

        // A non-control frame.

        if (continuationExists)
        {
            // A non-control frame was detected although the existing continuation had not been closed.
            throw new WebSocketException(
                WebSocketError.CONTINUATION_NOT_CLOSED,
                "A non-control frame was detected although the existing continuation had not been closed.");
        }
    }


    private void verifyFrameSize(WebSocketFrame frame) throws WebSocketException
    {
        // If the frame is not a control frame.
        if (frame.isControlFrame() == false)
        {
            // Nothing to check.
            return;
        }

        // RFC 6455, 5.5. Control Frames.
        //
        //   All control frames MUST have a payload length of 125 bytes or less
        //   and MUST NOT be fragmented.
        //

        byte[] payload = frame.getPayload();

        if (payload == null)
        {
            // The frame does not have payload.
            return;
        }

        if (125 < payload.length)
        {
            // The payload size of a control frame exceeds the maximum size (125 bytes).
            throw new WebSocketException(
                WebSocketError.TOO_LONG_CONTROL_FRAME_PAYLOAD,
                "The payload size of a control frame exceeds the maximum size (125 bytes): " + payload.length);
        }
    }


    private void handleFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a frame was received.
        callOnFrame(frame);

        // Dispatch based on the opcode.
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

        // Append the continuation frame to the existing continuation sequence.
        mContinuation.add(frame);

        // If the frame is not the last one for the continuation.
        if (frame.getFin() == false)
        {
            return;
        }

        // TODO: build a message, call onXxxMessage, then clear the continuation.
    }


    private void handleTextFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a text frame was received.
        callOnTextFrame(frame);

        // If the frame indicates the start of fragmentation.
        if (frame.getFin() == false)
        {
            // Start a continuation sequence.
            mContinuation.add(frame);
            return;
        }

        // Notify the listeners that a text message was received.
        callOnTextMessage(frame.getPayload());
    }


    private void handleBinaryFrame(WebSocketFrame frame)
    {
        // Notify the listeners that a binary frame was received.
        callOnBinaryFrame(frame);

        // If the frame indicates the start of fragmentation.
        if (frame.getFin() == false)
        {
            // Start a continuation sequence.
            mContinuation.add(frame);
            return;
        }

        // Notify the listeners that a binary message was received.
        callOnBinaryMessage(frame.getPayload());
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
