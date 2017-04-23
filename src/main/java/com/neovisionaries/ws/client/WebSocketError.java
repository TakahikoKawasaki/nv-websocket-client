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


/**
 * WebSocket error codes.
 *
 * @see WebSocketException#getError()
 */
public enum WebSocketError
{
    /**
     * The current state of the WebSocket is not CREATED.
     *
     * <p>
     * This error occurs if {@link WebSocket#connect()} is called
     * when the state of the WebSocket is not {@link
     * WebSocketState#CREATED CREATED}.
     * </p>
     */
    NOT_IN_CREATED_STATE,


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
     * The status code of the opening handshake response is not {@code 101 Switching Protocols}.
     */
    NOT_SWITCHING_PROTOCOLS,


    /**
     * An error occurred while HTTP header section was being read.
     */
    HTTP_HEADER_FAILURE,


    /**
     * The opening handshake response does not contain {@code Upgrade} header.
     */
    NO_UPGRADE_HEADER,


    /**
     * {@code websocket} was not found in {@code Upgrade} header.
     */
    NO_WEBSOCKET_IN_UPGRADE_HEADER,


    /**
     * The opening handshake response does not contain {@code Connection} header.
     */
    NO_CONNECTION_HEADER,


    /**
     * {@code Upgrade} was not found in {@code Connection} header.
     */
    NO_UPGRADE_IN_CONNECTION_HEADER,


    /**
     * The opening handshake response does not contain {@code Sec-WebSocket-Accept} header.
     */
    NO_SEC_WEBSOCKET_ACCEPT_HEADER,


    /**
     * The value of {@code Sec-WebSocket-Accept} header is different from the expected one.
     */
    UNEXPECTED_SEC_WEBSOCKET_ACCEPT_HEADER,


    /**
     * The value in {@code Sec-WebSocket-Extensions} failed to be parsed.
     */
    EXTENSION_PARSE_ERROR,


    /**
     * The extension contained in {@code Sec-WebSocket-Extensions} header is not supported.
     */
    UNSUPPORTED_EXTENSION,


    /**
     * The combination of the extensions contained in {@code Sec-WebSocket-Extensions} header
     * causes conflicts.
     *
     * @since 1.15
     */
    EXTENSIONS_CONFLICT,


    /**
     * The protocol contained in {@code Sec-WebSocket-Protocol} header is not supported.
     */
    UNSUPPORTED_PROTOCOL,


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
     * {@link OutOfMemoryError} occurred during a trial to allocate a memory area for a frame's payload.
     */
    INSUFFICIENT_MEMORY_FOR_PAYLOAD,


    /**
     * Interruption occurred while a frame was being read from the WebSocket.
     */
    INTERRUPTED_IN_READING,


    /**
     * An I/O error occurred while a frame was being read from the WebSocket.
     */
    IO_ERROR_IN_READING,


    /**
     * An I/O error occurred when a frame was tried to be sent.
     */
    IO_ERROR_IN_WRITING,


    /**
     * Flushing frames to the server failed.
     */
    FLUSH_ERROR,


    /**
     * At least one of the reserved bits of a frame is set.
     *
     * <blockquote>
     * <p>From RFC 6455, <a href="http://tools.ietf.org/html/rfc6455#section-5.2"
     * >5.2 Base Framing Protocol</a>; RSV1, RSV2, RSV3:</p>
     * <p><i>
     * MUST be 0 unless an extension is negotiated that defines meanings
     * for non-zero values.  If a nonzero value is received and none of
     * the negotiated extensions defines the meaning of such a nonzero
     * value, the receiving endpoint MUST Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     *
     * <p>
     * By calling {@link WebSocket#setExtended(boolean) WebSocket.setExtended}{@code
     * (true)}, you can skip the validity check of the RSV1/RSV2/RSV3 bits.
     * </p>
     *
     * <p>
     * This error code is not used in version 1.15 and after.
     * </p>
     */
    NON_ZERO_RESERVED_BITS,


    /**
     * A reserved bit of a frame has an unexpected value.
     *
     * <blockquote>
     * <p>From RFC 6455, <a href="http://tools.ietf.org/html/rfc6455#section-5.2"
     * >5.2 Base Framing Protocol</a>; RSV1, RSV2, RSV3:</p>
     * <p><i>
     * MUST be 0 unless an extension is negotiated that defines meanings
     * for non-zero values.  If a nonzero value is received and none of
     * the negotiated extensions defines the meaning of such a nonzero
     * value, the receiving endpoint MUST Fail the WebSocket Connection.
     * </i></p>
     * </blockquote>
     *
     * <p>
     * By calling {@link WebSocket#setExtended(boolean) WebSocket.setExtended}{@code
     * (true)}, you can skip the validity check of the RSV1/RSV2/RSV3 bits.
     * </p>
     *
     * @since 1.15
     */
    UNEXPECTED_RESERVED_BIT,


    /**
     * A frame from the server is masked.
     *
     * <blockquote>
     * <p>From RFC 6455, <a href="http://tools.ietf.org/html/rfc6455#section-5.1"
     * >5.1. Overview</a>:</p>
     * <p><i>
     * A server MUST NOT mask any frames that it sends to the client.
     * A client MUST close a connection if it detects a masked frame.
     * </i></p>
     * </blockquote>
     */
    FRAME_MASKED,


    /**
     * A frame has an unknown opcode.
     *
     * <p>
     * By calling {@link WebSocket#setExtended(boolean) WebSocket.setExtended}{@code
     * (true)}, you can accept frames which have an unknown opcode.
     * </p>
     */
    UNKNOWN_OPCODE,


    /**
     * A control frame is fragmented.
     *
     * <blockquote>
     * <p>From RFC 6455, <a href="http://tools.ietf.org/html/rfc6455#section-5.4"
     * >5.4. Fragmentation</a>:</p>
     * <p><i>
     * Control frames (see Section 5.5) MAY be injected in the middle of
     * a fragmented message.  Control frames themselves MUST NOT be fragmented.
     * </i></p>
     * </blockquote>
     */
    FRAGMENTED_CONTROL_FRAME,


    /**
     * A continuation frame was detected although a continuation had not started.
     */
    UNEXPECTED_CONTINUATION_FRAME,


    /**
     * A non-control frame was detected although the existing continuation had not been closed.
     */
    CONTINUATION_NOT_CLOSED,


    /**
     * The payload size of a control frame exceeds the maximum size (125 bytes).
     *
     * <blockquote>
     * <p>From RFC 6455, <a href="http://tools.ietf.org/html/rfc6455#section-5.5"
     * >5.5. Control Frames</a>:</p>
     * <p><i>
     * All control frames MUST have a payload length of 125 bytes or less and
     * MUST NOT be fragmented.
     * </i></p>
     * </blockquote>
     */
    TOO_LONG_CONTROL_FRAME_PAYLOAD,


    /**
     * Failed to concatenate payloads of multiple frames to construct a message.
     */
    MESSAGE_CONSTRUCTION_ERROR,


    /**
     * Failed to convert payload data into a string.
     */
    TEXT_MESSAGE_CONSTRUCTION_ERROR,


    /**
     * An uncaught throwable was detected in the reading thread (which reads
     * frames from the server).
     */
    UNEXPECTED_ERROR_IN_READING_THREAD,


    /**
     * An uncaught throwable was detected in the writing thread (which sends
     * frames to the server).
     */
    UNEXPECTED_ERROR_IN_WRITING_THREAD,


    /**
     * {@code permessage-deflate} extension contains an unsupported parameter.
     *
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc7692#section-7">7. The
     * "permessage-deflate" Extension</a> in
     * <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a> for details.
     * </p>
     *
     * @since 1.15
     */
    PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER,


    /**
     * The value of {@code server_max_window_bits} parameter or {@code
     * client_max_window_bits} parameter of {@code permessage-deflate}
     * extension is invalid.
     *
     * <p>
     * See <a href="https://tools.ietf.org/html/rfc7692#section-7.1.2">7.1.2.
     * Limiting the LZ77 Sliding Window Size</a> in
     * <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a> for details.
     * </p>
     *
     * @since 1.15
     */
    PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS,


    /**
     * Compression failed.
     *
     * @since 1.17
     */
    COMPRESSION_ERROR,


    /**
     * Decompression failed.
     *
     * @since 1.16
     */
    DECOMPRESSION_ERROR,


    /**
     * {@link java.net.Socket#connect(java.net.SocketAddress, int)
     * Socket.connect()} failed.
     *
     * @since 1.20
     */
    SOCKET_CONNECT_ERROR,


    /**
     * Handshake with a proxy server failed.
     *
     * @since 1.20
     */
    PROXY_HANDSHAKE_ERROR,


    /**
     * Failed to overlay an existing socket.
     *
     * @since 1.20
     */
    SOCKET_OVERLAY_ERROR,


    /**
     * SSL handshake with a WebSocket endpoint failed.
     *
     * @since 1.20
     */
    SSL_HANDSHAKE_ERROR,


    /**
     * No more frame can be read because the end of the input stream has been reached.
     *
     * <p>
     * This happens when the WebSocket connection is closed without receiving a
     * <a href="https://tools.ietf.org/html/rfc6455#section-5.5.1">close frame</a>
     * from the WebSocket server. Strictly speaking, it is a violation against
     * <a href="https://tools.ietf.org/html/rfc6455">RFC 6455</a>, but it seems some
     * server implementations sometimes close a connection without sending a close
     * frame.
     * </p>
     *
     * @since 1.29
     */
    NO_MORE_FRAME,


    /**
     * The certificate of the peer does not match the expected hostname.
     *
     * <p>
     * When {@link WebSocketException#getError()} returns this error code, the
     * {@link WebSocketException} can be cast to {@link HostnameUnverifiedException}
     * through which you can get the
     * </p>
     *
     * <p>
     * See <a href='https://github.com/TakahikoKawasaki/nv-websocket-client/pull/107'
     * >Verify that certificate is valid for server hostname (#107)</a>.
     * </p>
     *
     * @since 2.1
     */
    HOSTNAME_UNVERIFIED,
    ;
}
