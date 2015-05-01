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


class PingSender extends PeriodicalFrameSender
{
    private static final String TIMER_NAME = "PingSender";


    public PingSender(WebSocket webSocket)
    {
        super(webSocket, TIMER_NAME);
    }


    @Override
    protected WebSocketFrame createFrame(long count)
    {
        return WebSocketFrame.createPingFrame(String.valueOf(count));
    }
}
