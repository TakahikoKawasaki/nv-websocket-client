/*
 * Copyright (C) 2017 Neo Visionaries Inc.
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


abstract class WebSocketThread extends Thread
{
    protected final WebSocket mWebSocket;
    private final ThreadType mThreadType;


    WebSocketThread(String name, WebSocket ws, ThreadType type)
    {
        super(name);

        mWebSocket  = ws;
        mThreadType = type;
    }


    @Override
    public void run()
    {
        ListenerManager lm = mWebSocket.getListenerManager();

        if (lm != null)
        {
            // Execute onThreadStarted() of the listeners.
            lm.callOnThreadStarted(mThreadType, this);
        }

        runMain();

        if (lm != null)
        {
            // Execute onThreadStopping() of the listeners.
            lm.callOnThreadStopping(mThreadType, this);
        }
    }


    public void callOnThreadCreated()
    {
        ListenerManager lm = mWebSocket.getListenerManager();

        if (lm != null)
        {
            lm.callOnThreadCreated(mThreadType, this);
        }
    }


    protected abstract void runMain();
}
