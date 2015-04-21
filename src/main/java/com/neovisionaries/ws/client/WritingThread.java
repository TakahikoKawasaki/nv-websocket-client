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


import static com.neovisionaries.ws.client.WebSocketState.CLOSED;
import static com.neovisionaries.ws.client.WebSocketState.CLOSING;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.neovisionaries.ws.client.StateManager.CloseInitiator;


class WritingThread extends Thread
{
    private static final int SHOULD_SEND     = 0;
    private static final int SHOULD_STOP     = 1;
    private static final int SHOULD_CONTINUE = 2;
    private final WebSocket mWebSocket;
    private final List<WebSocketFrame> mFrames;
    private boolean mStopRequested;
    private WebSocketFrame mCloseFrame;


    public WritingThread(WebSocket websocket)
    {
        super("WritingThread");

        mWebSocket = websocket;
        mFrames    = new LinkedList<WebSocketFrame>();
    }


    @Override
    public void run()
    {
        try
        {
            main();
        }
        catch (Throwable t)
        {
            // An uncaught throwable was detected in the writing thread.
            WebSocketException cause = new WebSocketException(
                WebSocketError.UNEXPECTED_ERROR_IN_WRITING_THREAD,
                "An uncaught throwable was detected in the writing thread", t);

            // Notify the listeners.
            ListenerManager manager = mWebSocket.getListenerManager();
            manager.callOnError(cause);
            manager.callOnUnexpectedError(cause);
        }
    }


    private void main()
    {
        while (true)
        {
            // Wait for frames to be queued.
            int result = waitForFrames();

            if (result == SHOULD_STOP)
            {
                break;
            }
            else if (result == SHOULD_CONTINUE)
            {
                continue;
            }

            try
            {
                // Send frames.
                sendFrames();
            }
            catch (WebSocketException e)
            {
                // An I/O error occurred.
                break;
            }
        }

        try
        {
            // Send remaining frames, if any.
            sendFrames();
        }
        catch (WebSocketException e)
        {
            // An I/O error occurred.
        }

        // Notify this writing thread finished.
        notifyFinished();
    }


    public void requestStop()
    {
        synchronized (this)
        {
            // Schedule stopping.
            mStopRequested = true;

            // Wake up this thread.
            notifyAll();
        }
    }


    public void queueFrame(WebSocketFrame frame)
    {
        synchronized (this)
        {
            // Append the frame to the list of web socket frames
            // which are to be sent to the server.
            mFrames.add(frame);

            // Wake up this thread.
            notifyAll();
        }
    }


    private int waitForFrames()
    {
        synchronized (this)
        {
            // If this thread has been requested to stop.
            if (mStopRequested)
            {
                return SHOULD_STOP;
            }

            // If a close frame has already been sent.
            if (mCloseFrame != null)
            {
                return SHOULD_STOP;
            }

            // If the list of web socket frames
            if (mFrames.size() == 0)
            {
                try
                {
                    // Wait until a new frame is added to the list
                    // or this thread is requested to stop.
                    wait();
                }
                catch (InterruptedException e)
                {
                }
            }

            if (mStopRequested)
            {
                return SHOULD_STOP;
            }

            if (mFrames.size() == 0)
            {
                // Spurious wakeup.
                return SHOULD_CONTINUE;
            }
        }

        return SHOULD_SEND;
    }


    private void sendFrames() throws WebSocketException
    {
        List<WebSocketFrame> frames;

        synchronized (this)
        {
            // Move the frames from mFrames to frames.
            frames = new ArrayList<WebSocketFrame>(mFrames.size());
            frames.addAll(mFrames);
            mFrames.clear();
        }

        if (frames.size() == 0)
        {
            return;
        }

        for (WebSocketFrame frame : frames)
        {
            // Send the frame to the server.
            sendFrame(frame);
        }

        try
        {
            // Flush
            mWebSocket.getOutput().flush();
        }
        catch (IOException e)
        {
            // Flushing frames to the server failed.
            WebSocketException cause = new WebSocketException(
                WebSocketError.FLUSH_ERROR,
                "Flushing frames to the server failed", e);

            // Notify the listeners.
            ListenerManager manager = mWebSocket.getListenerManager();
            manager.callOnError(cause);
            manager.callOnSendError(cause, null);

            throw cause;
        }
    }


    private void sendFrame(WebSocketFrame frame) throws WebSocketException
    {
        boolean unsent = false;

        synchronized (this)
        {
            // If a close frame has already been sent.
            if (mCloseFrame != null)
            {
                // Frames should not be sent to the server.
                unsent = true;
            }
            // If the frame is a close frame.
            else if (frame.isCloseFrame())
            {
                mCloseFrame = frame;
            }
        }

        if (unsent)
        {
            // Notify the listeners that the frame was not sent.
            mWebSocket.getListenerManager().callOnFrameUnsent(frame);
            return;
        }

        // If the frame is a close frame.
        if (frame.isCloseFrame())
        {
            // Change the state to closing if its current value is
            // neither CLOSING nor CLOSED.
            changeToClosing();
        }

        try
        {
            // Send the frame to the server.
            mWebSocket.getOutput().write(frame);
        }
        catch (IOException e)
        {
            // An I/O error occurred when a frame was tried to be sent.
            WebSocketException cause = new WebSocketException(
                WebSocketError.IO_ERROR_IN_WRITING,
                "An I/O error occurred when a frame was tried to be sent.", e);

            // Notify the listeners.
            ListenerManager manager = mWebSocket.getListenerManager();
            manager.callOnError(cause);
            manager.callOnSendError(cause, frame);

            throw cause;
        }

        // Notify the listeners that the frame was sent.
        mWebSocket.getListenerManager().callOnFrameSent(frame);
    }


    private void changeToClosing()
    {
        StateManager manager = mWebSocket.getStateManager();

        synchronized (manager)
        {
            // The current state of the web socket.
            WebSocketState state = manager.getState();

            // If the current state is neither CLOSING nor CLOSED.
            if (state != CLOSING && state != CLOSED)
            {
                // Change the state to CLOSING.
                manager.changeToClosing(CloseInitiator.CLIENT);
            }
        }
    }


    private void notifyFinished()
    {
        mWebSocket.onWritingThreadFinished(mCloseFrame);
    }
}
