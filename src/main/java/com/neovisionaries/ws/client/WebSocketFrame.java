/*
 * Copyright (C) 2015-2016 Neo Visionaries Inc.
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


import static com.neovisionaries.ws.client.WebSocketOpcode.BINARY;
import static com.neovisionaries.ws.client.WebSocketOpcode.CLOSE;
import static com.neovisionaries.ws.client.WebSocketOpcode.CONTINUATION;
import static com.neovisionaries.ws.client.WebSocketOpcode.PING;
import static com.neovisionaries.ws.client.WebSocketOpcode.PONG;
import static com.neovisionaries.ws.client.WebSocketOpcode.TEXT;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * WebSocket frame.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5"
 *      >RFC 6455, 5. Data Framing</a>
 */
public class WebSocketFrame
{
    private boolean mFin;
    private boolean mRsv1;
    private boolean mRsv2;
    private boolean mRsv3;
    private int mOpcode;
    private boolean mMask;
    private byte[] mPayload;


    /**
     * Get the value of FIN bit.
     *
     * @return
     *         The value of FIN bit.
     */
    public boolean getFin()
    {
        return mFin;
    }


    /**
     * Set the value of FIN bit.
     *
     * @param fin
     *         The value of FIN bit.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setFin(boolean fin)
    {
        mFin = fin;

        return this;
    }


    /**
     * Get the value of RSV1 bit.
     *
     * @return
     *         The value of RSV1 bit.
     */
    public boolean getRsv1()
    {
        return mRsv1;
    }


    /**
     * Set the value of RSV1 bit.
     *
     * @param rsv1
     *         The value of RSV1 bit.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setRsv1(boolean rsv1)
    {
        mRsv1 = rsv1;

        return this;
    }


    /**
     * Get the value of RSV2 bit.
     *
     * @return
     *         The value of RSV2 bit.
     */
    public boolean getRsv2()
    {
        return mRsv2;
    }


    /**
     * Set the value of RSV2 bit.
     *
     * @param rsv2
     *         The value of RSV2 bit.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setRsv2(boolean rsv2)
    {
        mRsv2 = rsv2;

        return this;
    }


    /**
     * Get the value of RSV3 bit.
     *
     * @return
     *         The value of RSV3 bit.
     */
    public boolean getRsv3()
    {
        return mRsv3;
    }


    /**
     * Set the value of RSV3 bit.
     *
     * @param rsv3
     *         The value of RSV3 bit.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setRsv3(boolean rsv3)
    {
        mRsv3 = rsv3;

        return this;
    }


    /**
     * Get the opcode.
     *
     * <table border="1" cellpadding="5" style="table-collapse: collapse;">
     *   <caption>WebSocket opcode</caption>
     *   <thead>
     *     <tr>
     *       <th>Value</th>
     *       <th>Description</th>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>0x0</td>
     *       <td>Frame continuation</td>
     *     </tr>
     *     <tr>
     *       <td>0x1</td>
     *       <td>Text frame</td>
     *     </tr>
     *     <tr>
     *       <td>0x2</td>
     *       <td>Binary frame</td>
     *     </tr>
     *     <tr>
     *       <td>0x3-0x7</td>
     *       <td>Reserved</td>
     *     </tr>
     *     <tr>
     *       <td>0x8</td>
     *       <td>Connection close</td>
     *     </tr>
     *     <tr>
     *       <td>0x9</td>
     *       <td>Ping</td>
     *     </tr>
     *     <tr>
     *       <td>0xA</td>
     *       <td>Pong</td>
     *     </tr>
     *     <tr>
     *       <td>0xB-0xF</td>
     *       <td>Reserved</td>
     *     </tr>
     *   </tbody>
     * </table>
     *
     * @return
     *         The opcode.
     *
     * @see WebSocketOpcode
     */
    public int getOpcode()
    {
        return mOpcode;
    }


    /**
     * Set the opcode
     *
     * @param opcode
     *         The opcode.
     *
     * @return
     *         {@code this} object.
     *
     * @see WebSocketOpcode
     */
    public WebSocketFrame setOpcode(int opcode)
    {
        mOpcode = opcode;

        return this;
    }


    /**
     * Check if this frame is a continuation frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0x0 ({@link WebSocketOpcode#CONTINUATION}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a continuation frame
     *         (= if the opcode is 0x0).
     */
    public boolean isContinuationFrame()
    {
        return (mOpcode == CONTINUATION);
    }


    /**
     * Check if this frame is a text frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0x1 ({@link WebSocketOpcode#TEXT}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a text frame
     *         (= if the opcode is 0x1).
     */
    public boolean isTextFrame()
    {
        return (mOpcode == TEXT);
    }


    /**
     * Check if this frame is a binary frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0x2 ({@link WebSocketOpcode#BINARY}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a binary frame
     *         (= if the opcode is 0x2).
     */
    public boolean isBinaryFrame()
    {
        return (mOpcode == BINARY);
    }


    /**
     * Check if this frame is a close frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0x8 ({@link WebSocketOpcode#CLOSE}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a close frame
     *         (= if the opcode is 0x8).
     */
    public boolean isCloseFrame()
    {
        return (mOpcode == CLOSE);
    }


    /**
     * Check if this frame is a ping frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0x9 ({@link WebSocketOpcode#PING}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a ping frame
     *         (= if the opcode is 0x9).
     */
    public boolean isPingFrame()
    {
        return (mOpcode == PING);
    }


    /**
     * Check if this frame is a pong frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is 0xA ({@link WebSocketOpcode#PONG}).
     * </p>
     *
     * @return
     *         {@code true} if this frame is a pong frame
     *         (= if the opcode is 0xA).
     */
    public boolean isPongFrame()
    {
        return (mOpcode == PONG);
    }


    /**
     * Check if this frame is a data frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is in between 0x1 and 0x7.
     * </p>
     *
     * @return
     *         {@code true} if this frame is a data frame
     *         (= if the opcode is in between 0x1 and 0x7).
     */
    public boolean isDataFrame()
    {
        return (0x1 <= mOpcode && mOpcode <= 0x7);
    }


    /**
     * Check if this frame is a control frame.
     *
     * <p>
     * This method returns {@code true} when the value of the
     * opcode is in between 0x8 and 0xF.
     * </p>
     *
     * @return
     *         {@code true} if this frame is a control frame
     *         (= if the opcode is in between 0x8 and 0xF).
     */
    public boolean isControlFrame()
    {
        return (0x8 <= mOpcode && mOpcode <= 0xF);
    }


    /**
     * Get the value of MASK bit.
     *
     * @return
     *         The value of MASK bit.
     */
    boolean getMask()
    {
        return mMask;
    }


    /**
     * Set the value of MASK bit.
     *
     * @param mask
     *         The value of MASK bit.
     *
     * @return
     *         {@code this} object.
     */
    WebSocketFrame setMask(boolean mask)
    {
        mMask = mask;

        return this;
    }


    /**
     * Check if this frame has payload.
     *
     * @return
     *         {@code true} if this frame has payload.
     */
    public boolean hasPayload()
    {
        return mPayload != null;
    }


    /**
     * Get the payload length.
     *
     * @return
     *         The payload length.
     */
    public int getPayloadLength()
    {
        if (mPayload == null)
        {
            return 0;
        }

        return mPayload.length;
    }


    /**
     * Get the unmasked payload.
     *
     * @return
     *         The unmasked payload. {@code null} may be returned.
     */
    public byte[] getPayload()
    {
        return mPayload;
    }


    /**
     * Get the unmasked payload as a text.
     *
     * @return
     *         A string constructed by interrupting the payload
     *         as a UTF-8 bytes.
     */
    public String getPayloadText()
    {
        if (mPayload == null)
        {
            return null;
        }

        return Misc.toStringUTF8(mPayload);
    }


    /**
     * Set the unmasked payload.
     *
     * <p>
     * Note that the payload length of a <a href="http://tools.ietf.org/html/rfc6455#section-5.5"
     * >control frame</a> must be 125 bytes or less.
     * </p>
     *
     * @param payload
     *         The unmasked payload. {@code null} is accepted.
     *         An empty byte array is treated in the same way
     *         as {@code null}.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setPayload(byte[] payload)
    {
        if (payload != null && payload.length == 0)
        {
            payload = null;
        }

        mPayload = payload;

        return this;
    }


    /**
     * Set the payload. The given string is converted to a byte array
     * in UTF-8 encoding.
     *
     * <p>
     * Note that the payload length of a <a href="http://tools.ietf.org/html/rfc6455#section-5.5"
     * >control frame</a> must be 125 bytes or less.
     * </p>
     *
     * @param payload
     *         The unmasked payload. {@code null} is accepted.
     *         An empty string is treated in the same way as
     *         {@code null}.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setPayload(String payload)
    {
        if (payload == null || payload.length() == 0)
        {
            return setPayload((byte[])null);
        }

        return setPayload(Misc.getBytesUTF8(payload));
    }


    /**
     * Set the payload that conforms to the payload format of close frames.
     *
     * <p>
     * The given parameters are encoded based on the rules described in
     * "<a href="http://tools.ietf.org/html/rfc6455#section-5.5.1"
     * >5.5.1. Close</a>" of RFC 6455.
     * </p>
     *
     * <p>
     * Note that the reason should not be too long because the payload
     * length of a <a href="http://tools.ietf.org/html/rfc6455#section-5.5"
     * >control frame</a> must be 125 bytes or less.
     * </p>
     *
     * @param closeCode
     *         The close code.
     *
     * @param reason
     *         The reason. {@code null} is accepted. An empty string
     *         is treated in the same way as {@code null}.
     *
     * @return
     *         {@code this} object.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.5.1"
     *      >RFC 6455, 5.5.1. Close</a>
     *
     * @see WebSocketCloseCode
     */
    public WebSocketFrame setCloseFramePayload(int closeCode, String reason)
    {
        // Convert the close code to a 2-byte unsigned integer
        // in network byte order.
        byte[] encodedCloseCode = new byte[] {
            (byte)((closeCode >> 8) & 0xFF),
            (byte)((closeCode     ) & 0xFF)
        };

        // If a reason string is not given.
        if (reason == null || reason.length() == 0)
        {
            // Use the close code only.
            return setPayload(encodedCloseCode);
        }

        // Convert the reason into a byte array.
        byte[] encodedReason = Misc.getBytesUTF8(reason);

        // Concatenate the close code and the reason.
        byte[] payload = new byte[2 + encodedReason.length];
        System.arraycopy(encodedCloseCode, 0, payload, 0, 2);
        System.arraycopy(encodedReason, 0, payload, 2, encodedReason.length);

        // Use the concatenated string.
        return setPayload(payload);
    }


    /**
     * Parse the first two bytes of the payload as a close code.
     *
     * <p>
     * If any payload is not set or the length of the payload is less than 2,
     * this method returns 1005 ({@link WebSocketCloseCode#NONE}).
     * </p>
     *
     * <p>
     * The value returned from this method is meaningless if this frame
     * is not a close frame.
     * </p>
     *
     * @return
     *         The close code.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.5.1"
     *      >RFC 6455, 5.5.1. Close</a>
     *
     * @see WebSocketCloseCode
     */
    public int getCloseCode()
    {
        if (mPayload == null || mPayload.length < 2)
        {
            return WebSocketCloseCode.NONE;
        }

        // A close code is encoded in network byte order.
        int closeCode = (((mPayload[0] & 0xFF) << 8) | (mPayload[1] & 0xFF));

        return closeCode;
    }


    /**
     * Parse the third and subsequent bytes of the payload as a close reason.
     *
     * <p>
     * If any payload is not set or the length of the payload is less than 3,
     * this method returns {@code null}.
     * </p>
     *
     * <p>
     * The value returned from this method is meaningless if this frame
     * is not a close frame.
     * </p>
     *
     * @return
     *         The close reason.
     */
    public String getCloseReason()
    {
        if (mPayload == null || mPayload.length < 3)
        {
            return null;
        }

        return Misc.toStringUTF8(mPayload, 2, mPayload.length - 2);
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder()
            .append("WebSocketFrame(FIN=").append(mFin ? "1" : "0")
            .append(",RSV1=").append(mRsv1 ? "1" : "0")
            .append(",RSV2=").append(mRsv2 ? "1" : "0")
            .append(",RSV3=").append(mRsv3 ? "1" : "0")
            .append(",Opcode=").append(Misc.toOpcodeName(mOpcode))
            .append(",Length=").append(getPayloadLength());

        switch (mOpcode)
        {
            case TEXT:
                appendPayloadText(builder);
                break;

            case BINARY:
                appendPayloadBinary(builder);
                break;

            case CLOSE:
                appendPayloadClose(builder);
                break;
        }

        return builder.append(")").toString();
    }


    private boolean appendPayloadCommon(StringBuilder builder)
    {
        builder.append(",Payload=");

        if (mPayload == null)
        {
            builder.append("null");

            // Nothing more to append.
            return true;
        }

        if (mRsv1)
        {
            // In the current implementation, mRsv1=true is allowed
            // only when Per-Message Compression is applied.
            builder.append("compressed");

            // Nothing more to append.
            return true;
        }

        // Continue.
        return false;
    }


    private void appendPayloadText(StringBuilder builder)
    {
        if (appendPayloadCommon(builder))
        {
            // Nothing more to append.
            return;
        }

        builder.append("\"");
        builder.append(getPayloadText());
        builder.append("\"");
    }


    private void appendPayloadClose(StringBuilder builder)
    {
        builder
            .append(",CloseCode=").append(getCloseCode())
            .append(",Reason=");

        String reason = getCloseReason();

        if (reason == null)
        {
            builder.append("null");
        }
        else
        {
            builder.append("\"").append(reason).append("\"");
        }
    }


    private void appendPayloadBinary(StringBuilder builder)
    {
        if (appendPayloadCommon(builder))
        {
            // Nothing more to append.
            return;
        }

        for (int i = 0; i < mPayload.length; ++i)
        {
            builder.append(String.format("%02X ", (0xFF & mPayload[i])));
        }

        if (mPayload.length != 0)
        {
            // Remove the last space.
            builder.setLength(builder.length() - 1);
        }
    }


    /**
     * Create a continuation frame. Note that the FIN bit of the
     * returned frame is false.
     *
     * @return
     *         A WebSocket frame whose FIN bit is false, opcode is
     *         {@link WebSocketOpcode#CONTINUATION CONTINUATION} and
     *         payload is {@code null}.
     */
    public static WebSocketFrame createContinuationFrame()
    {
        return new WebSocketFrame()
            .setOpcode(CONTINUATION);
    }


    /**
     * Create a continuation frame. Note that the FIN bit of the
     * returned frame is false.
     *
     * @param payload
     *         The payload for a newly create frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is false, opcode is
     *         {@link WebSocketOpcode#CONTINUATION CONTINUATION} and
     *         payload is the given one.
     */
    public static WebSocketFrame createContinuationFrame(byte[] payload)
    {
        return createContinuationFrame().setPayload(payload);
    }


    /**
     * Create a continuation frame. Note that the FIN bit of the
     * returned frame is false.
     *
     * @param payload
     *         The payload for a newly create frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is false, opcode is
     *         {@link WebSocketOpcode#CONTINUATION CONTINUATION} and
     *         payload is the given one.
     */
    public static WebSocketFrame createContinuationFrame(String payload)
    {
        return createContinuationFrame().setPayload(payload);
    }


    /**
     * Create a text frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#TEXT TEXT} and payload is
     *         the given one.
     */
    public static WebSocketFrame createTextFrame(String payload)
    {
        return new WebSocketFrame()
            .setFin(true)
            .setOpcode(TEXT)
            .setPayload(payload);
    }


    /**
     * Create a binary frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#BINARY BINARY} and payload is
     *         the given one.
     */
    public static WebSocketFrame createBinaryFrame(byte[] payload)
    {
        return new WebSocketFrame()
            .setFin(true)
            .setOpcode(BINARY)
            .setPayload(payload);
    }


    /**
     * Create a close frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#CLOSE CLOSE} and payload is
     *         {@code null}.
     */
    public static WebSocketFrame createCloseFrame()
    {
        return new WebSocketFrame()
            .setFin(true)
            .setOpcode(CLOSE);
    }


    /**
     * Create a close frame.
     *
     * @param closeCode
     *         The close code.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#CLOSE CLOSE} and payload
     *         contains a close code.
     *
     * @see WebSocketCloseCode
     */
    public static WebSocketFrame createCloseFrame(int closeCode)
    {
        return createCloseFrame().setCloseFramePayload(closeCode, null);
    }


    /**
     * Create a close frame.
     *
     * @param closeCode
     *         The close code.
     *
     * @param reason
     *         The close reason.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#CLOSE CLOSE} and payload
     *         contains a close code and a close reason.
     *
     * @see WebSocketCloseCode
     */
    public static WebSocketFrame createCloseFrame(int closeCode, String reason)
    {
        return createCloseFrame().setCloseFramePayload(closeCode, reason);
    }


    /**
     * Create a ping frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PING PING} and payload is
     *         {@code null}.
     */
    public static WebSocketFrame createPingFrame()
    {
        return new WebSocketFrame()
            .setFin(true)
            .setOpcode(PING);
    }


    /**
     * Create a ping frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PING PING} and payload is
     *         the given one.
     */
    public static WebSocketFrame createPingFrame(byte[] payload)
    {
        return createPingFrame().setPayload(payload);
    }


    /**
     * Create a ping frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PING PING} and payload is
     *         the given one.
     */
    public static WebSocketFrame createPingFrame(String payload)
    {
        return createPingFrame().setPayload(payload);
    }


    /**
     * Create a pong frame.
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PONG PONG} and payload is
     *         {@code null}.
     */
    public static WebSocketFrame createPongFrame()
    {
        return new WebSocketFrame()
            .setFin(true)
            .setOpcode(PONG);
    }


    /**
     * Create a pong frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PONG PONG} and payload is
     *         the given one.
     */
    public static WebSocketFrame createPongFrame(byte[] payload)
    {
        return createPongFrame().setPayload(payload);
    }


    /**
     * Create a pong frame.
     *
     * @param payload
     *         The payload for a newly created frame.
     *         Note that a control frame's payload length must be 125 bytes or less
     *         (RFC 6455, <a href="https://tools.ietf.org/html/rfc6455#section-5.5"
     *         >5.5. Control Frames</a>).
     *
     * @return
     *         A WebSocket frame whose FIN bit is true, opcode is
     *         {@link WebSocketOpcode#PONG PONG} and payload is
     *         the given one.
     */
    public static WebSocketFrame createPongFrame(String payload)
    {
        return createPongFrame().setPayload(payload);
    }


    /**
     * Mask/unmask payload.
     *
     * <p>
     * The logic of masking/unmasking is described in "<a href=
     * "http://tools.ietf.org/html/rfc6455#section-5.3">5.3.
     * Client-to-Server Masking</a>" in RFC 6455.
     * </p>
     *
     * @param maskingKey
     *         The masking key. If {@code null} is given or the length
     *         of the masking key is less than 4, nothing is performed.
     *
     * @param payload
     *         Payload to be masked/unmasked.
     *
     * @return
     *         {@code payload}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6455#section-5.3">5.3. Client-to-Server Masking</a>
     */
    static byte[] mask(byte[] maskingKey, byte[] payload)
    {
        if (maskingKey == null || maskingKey.length < 4 || payload == null)
        {
            return payload;
        }

        for (int i = 0; i < payload.length; ++i)
        {
            payload[i] ^= maskingKey[i % 4];
        }

        return payload;
    }


    static WebSocketFrame compressFrame(WebSocketFrame frame, PerMessageCompressionExtension pmce)
    {
        // If Per-Message Compression is not enabled.
        if (pmce == null)
        {
            // No compression.
            return frame;
        }

        // If the frame is neither a TEXT frame nor a BINARY frame.
        if (frame.isTextFrame()   == false &&
            frame.isBinaryFrame() == false)
        {
            // No compression.
            return frame;
        }

        // If the frame is not the final frame.
        if (frame.getFin() == false)
        {
            // The compression must be applied to this frame and
            // all the subsequent continuation frames, but the
            // current implementation does not support the behavior.
            return frame;
        }

        // If the RSV1 bit is set.
        if (frame.getRsv1())
        {
            // In the current implementation, RSV1=true is allowed
            // only as Per-Message Compressed Bit (See RFC 7692,
            // 6. Framing). Therefore, RSV1=true here is regarded
            // as "already compressed".
            return frame;
        }

        // The plain payload before compression.
        byte[] payload = frame.getPayload();

        // If the payload is empty.
        if (payload == null || payload.length == 0)
        {
            // No compression.
            return frame;
        }

        // Compress the payload.
        byte[] compressed = compress(payload, pmce);

        // If the length of the compressed data is not less than
        // that of the original plain payload.
        if (payload.length <= compressed.length)
        {
            // It's better not to compress the payload.
            return frame;
        }

        // Replace the plain payload with the compressed data.
        frame.setPayload(compressed);

        // Set Per-Message Compressed Bit (See RFC 7692, 6. Framing).
        frame.setRsv1(true);

        return frame;
    }


    private static byte[] compress(byte[] data, PerMessageCompressionExtension pmce)
    {
        try
        {
            // Compress the data.
            return pmce.compress(data);
        }
        catch (WebSocketException e)
        {
            // Failed to compress the data. Ignore this error and use
            // the plain original data. The current implementation
            // does not call any listener callback method for this error.
            return data;
        }
    }


    static List<WebSocketFrame> splitIfNecessary(
            WebSocketFrame frame, int maxPayloadSize, PerMessageCompressionExtension pmce)
    {
        // If the maximum payload size is not specified.
        if (maxPayloadSize == 0)
        {
            // Not split.
            return null;
        }

        // If the total length of the payload is equal to or
        // less than the maximum payload size.
        if (frame.getPayloadLength() <= maxPayloadSize)
        {
            // Not split.
            return null;
        }

        // If the frame is a binary frame or a text frame.
        if (frame.isBinaryFrame() || frame.isTextFrame())
        {
            // Try to compress the frame. In the current implementation, binary
            // frames and text frames with the FIN bit true can be compressed.
            // The compressFrame() method may change the payload and the RSV1
            // bit of the given frame.
            frame = compressFrame(frame, pmce);

            // If the payload length of the frame has become equal to or less
            // than the maximum payload size as a result of the compression.
            if (frame.getPayloadLength() <= maxPayloadSize)
            {
                // Not split. (Note that the frame has been compressed)
                return null;
            }
        }
        else if (frame.isContinuationFrame() == false)
        {
            // Control frames (Close/Ping/Pong) are not split.
            return null;
        }

        // Split the frame.
        return split(frame, maxPayloadSize);
    }


    private static List<WebSocketFrame> split(WebSocketFrame frame, int maxPayloadSize)
    {
        // The original payload and the original FIN bit.
        byte[] originalPayload = frame.getPayload();
        boolean originalFin    = frame.getFin();

        List<WebSocketFrame> frames = new ArrayList<WebSocketFrame>();

        // Generate the first frame using the existing WebSocketFrame instance.
        // Note that the reserved bit 1 and the opcode are untouched.
        byte[] payload = Arrays.copyOf(originalPayload, maxPayloadSize);
        frame.setFin(false).setPayload(payload);
        frames.add(frame);

        for (int from = maxPayloadSize; from < originalPayload.length; from += maxPayloadSize)
        {
            // Prepare the payload of the next continuation frame.
            int to  = Math.min(from + maxPayloadSize, originalPayload.length);
            payload = Arrays.copyOfRange(originalPayload, from, to);

            // Create a continuation frame.
            WebSocketFrame cont = WebSocketFrame.createContinuationFrame(payload);
            frames.add(cont);
        }

        if (originalFin)
        {
            // Set the FIN bit of the last frame.
            frames.get(frames.size() - 1).setFin(true);
        }

        return frames;
    }
}
