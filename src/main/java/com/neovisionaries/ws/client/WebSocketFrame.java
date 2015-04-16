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


/**
 * Web socket frame.
 */
public class WebSocketFrame
{
    /**
     * Opcode.
     */
    public static class Opcode
    {
        /**
         * Opcode for "frame continuation" (0x0).
         */
        public static final int CONTINUATION = 0x0;

        /**
         * Opcode for "text frame" (0x1).
         */
        public static final int TEXT = 0x1;

        /**
         * Opcode for "binary frame" (0x2).
         */
        public static final int BINARY = 0x2;

        /**
         * Opcode for "connection close" (0x8).
         */
        public static final int CLOSE = 0x8;

        /**
         * Opcode for "ping" (0x9).
         */
        public static final int PING = 0x9;

        /**
         * Opcode for "pong" (0xA).
         */
        public static final int PONG = 0xA;
    }


    private boolean mFin;
    private boolean mRsv1;
    private boolean mRsv2;
    private boolean mRsv3;
    private int mOpcode;
    private byte[] mMask;
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
     */
    public WebSocketFrame setOpcode(int opcode)
    {
        mOpcode = opcode;

        return this;
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
     * Get the masking key.
     *
     * @return
     *         The masking key. {@code null} may be returned.
     */
    public byte[] getMask()
    {
        return mMask;
    }


    /**
     * Set the masking key.
     *
     * @param mask
     *         The masking key. Giving {@code null} means that
     *         this frame is not masked.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setMask(byte[] mask)
    {
        mMask = mask;

        return this;
    }


    /**
     * Check if this frame is masked.
     *
     * <p>
     * This method returns {@code true} if a masking key is not set.
     * </p>
     *
     * @return
     *         {@code true} if this frame is masked (= if a masking
     *         key is set).
     */
    public boolean isMasked()
    {
        return mMask != null;
    }


    /**
     * Get the payload.
     *
     * @return
     *         The payload. {@code null} may be returned.
     */
    public byte[] getPayload()
    {
        return mPayload;
    }


    /**
     * Set the payload.
     *
     * @param payload
     *         The payload.
     *
     * @return
     *         {@code this} object.
     */
    public WebSocketFrame setPayload(byte[] payload)
    {
        mPayload = payload;

        return this;
    }


    static void mask(byte[] mask, byte[] payload)
    {
        for (int i = 0; i < payload.length; ++i)
        {
            payload[i] ^= mask[i%4];
        }
    }
}
