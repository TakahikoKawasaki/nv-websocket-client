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


import java.util.concurrent.ThreadFactory;

class ConnectThread extends WebSocketThread
{
    public ConnectThread(ThreadFactory factory, WebSocket ws)
    {
        super(factory, "ConnectThread", ws, ThreadType.CONNECT_THREAD);
    }


    @Override
    public void runMain()
    {
        try
        {
            mWebSocket.connect();
        }
        catch (WebSocketException e)
        {
            handleError(e);
        }
    }


    private void handleError(WebSocketException cause)
    {
        ListenerManager manager = mWebSocket.getListenerManager();

        manager.callOnError(cause);
        manager.callOnConnectError(cause);
    }
}
