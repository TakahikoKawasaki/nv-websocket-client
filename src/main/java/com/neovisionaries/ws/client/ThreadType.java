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


/**
 * Types of threads which are created internally in the implementation.
 *
 * @since 2.0
 */
public enum ThreadType
{
    /**
     * A thread which reads WebSocket frames from the server
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/ReadingThread.java'>ReadingThread</a></code>).
     */
    READING_THREAD,


    /**
     * A thread which sends WebSocket frames to the server
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/WritingThread.java'>WritingThread</a></code>).
     */
    WRITING_THREAD,


    /**
     * A thread which calls {@link WebSocket#connect()} asynchronously
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/ConnectThread.java'>ConnectThread</a></code>).
     */
    CONNECT_THREAD,


    /**
     * A thread which does finalization of a {@link WebSocket} instance.
     * (<code><a href='https://github.com/TakahikoKawasaki/nv-websocket-client/blob/master/src/main/java/com/neovisionaries/ws/client/FinishThread.java'>FinishThread</a></code>).
     */
    FINISH_THREAD,
}
