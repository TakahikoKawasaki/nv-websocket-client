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


public enum WebSocketError
{
    /**
     * Failed to get the input stream of the raw socket.
     */
    SOCKET_INPUT_STREAM_FAILURE,


    /**
     * Failed to get the output stream of the raw socket.
     */
    SOCKET_OUTPUT_STREAM_FAILURE,


    /**
     * Failed to send an opening handshake request to the server.
     */
    OPENING_HAHDSHAKE_REQUEST_FAILURE,


    /**
     * Failed to read an opening handshake response from the server.
     */
    OPENING_HANDSHAKE_RESPONSE_FAILURE,

    /**
     * The status line of the opening handshake response is empty.
     */
    STATUS_LINE_EMPTY,


    /**
     * The status line of the opening handshake response is badly formatted.
     */
    STATUS_LINE_BAD_FORMAT,


    /**
     * The status code of the opening handshake response is not Switching Protocols.
     */
    NOT_SWITCHING_PROTOCOLS,


    /**
     * An error occurred while HTTP header section was being read.
     */
    HTTP_HEADER_FAILURE,


    /**
     * The opening handshake response does not contain 'Upgrade' header.
     */
    NO_UPGRADE_HEADER,


    /**
     * The value of 'Upgrade' header is not 'websocket'.
     */
    UNEXPECTED_UPGRADE_HEADER,


    /**
     * The opening handshake response does not contain 'Connection' header.
     */
    NO_CONNECTION_HEADER,


    /**
     * The value of 'Connection' header is not 'Upgrade'.
     */
    UNEXPECTED_CONNECTION_HEADER,


    /**
     * The opening handshake response does not contain 'Sec-WebSocket-Accept' header.
     */
    NO_SEC_WEBSOCKET_ACCEPT_HEADER,


    /**
     * The value of 'Sec-WebSocket-Accept' header is different from the expected one.
     */
    UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,


    /**
     * The end of the stream has been reached unexpectedly.
     */
    INSUFFICENT_DATA,


    /**
     * The payload length of a frame is invalid.
     */
    INVALID_PAYLOAD_LENGTH,


    /**
     * The payload length of a frame exceeds the maximum array size in Java.
     */
    TOO_LONG_PAYLOAD,


    /**
     * OutOfMemoryError occurred during a trial to allocate a memory area for a frame's payload.
     */
    INSUFFICIENT_MEMORY_FOR_PAYLOAD,
    ;
}
