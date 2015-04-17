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


import static com.neovisionaries.ws.client.WebSocketOpcode.BINARY;
import static com.neovisionaries.ws.client.WebSocketOpcode.CLOSE;
import static com.neovisionaries.ws.client.WebSocketOpcode.CONTINUATION;
import static com.neovisionaries.ws.client.WebSocketOpcode.PING;
import static com.neovisionaries.ws.client.WebSocketOpcode.PONG;
import static com.neovisionaries.ws.client.WebSocketOpcode.TEXT;


/**
 * Web socket frame.
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
    public boolean getMask()
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
    public WebSocketFrame setMask(boolean mask)
    {
        mMask = mask;

        return this;
    }


    /**
     * Get the payload.
     *
     * @return
     *         The payload. {@code null} may be returned.
     *         Always unmasked.
     */
    public byte[] getPayload()
    {
        return mPayload;
    }


    /**
     * Set the payload.
     *
     * @param payload
     *         The unmasked payload.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setPayload(byte[] payload)
    {
        mPayload = payload;

        return this;
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
    public static byte[] mask(byte[] maskingKey, byte[] payload)
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
}
