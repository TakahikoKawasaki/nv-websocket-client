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
import static com.neovisionaries.ws.client.WebSocketState.CONNECTING;
import static com.neovisionaries.ws.client.WebSocketState.CREATED;
import static com.neovisionaries.ws.client.WebSocketState.OPEN;


class StateManager
{
    enum CloseInitiator
    {
        NONE,
        SERVER,
        CLIENT
    }

    private WebSocketState mState;
    private CloseInitiator mCloseInitiator = CloseInitiator.NONE;


    public StateManager()
    {
        mState = CREATED;
    }


    public WebSocketState getState()
    {
        return mState;
    }


    public boolean isCreated()
    {
        return (mState == CREATED);
    }


    public boolean isOpen()
    {
        return (mState == OPEN);
    }


    public boolean isClosing()
    {
        return (mState == CLOSING);
    }


    public boolean isClosed()
    {
        return (mState == CLOSED);
    }


    public void changeToConnecting()
    {
        mState = CONNECTING;
    }


    public void changeToOpen()
    {
        mState = OPEN;
    }


    public void changeToClosing(CloseInitiator closeInitiator)
    {
        mState = CLOSING;

        // Set the close initiator only when it has not been set yet.
        if (mCloseInitiator == CloseInitiator.NONE)
        {
            mCloseInitiator = closeInitiator;
        }
    }


    public void changeToClosed()
    {
        mState = CLOSED;
    }


    public CloseInitiator getCloseInitiator()
    {
        return mCloseInitiator;
    }
}
