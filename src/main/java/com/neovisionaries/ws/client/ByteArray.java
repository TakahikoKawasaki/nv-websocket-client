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


import java.nio.ByteBuffer;


class ByteArray
{
    private static final int ADDITIONAL_BUFFER_SIZE = 1024;

    // The buffer.
    private ByteBuffer mBuffer;

    // The current length.
    private int mLength;


    public ByteArray(int capacity)
    {
        mBuffer = ByteBuffer.allocate(capacity);
        mLength = 0;
    }


    public ByteArray(byte[] data)
    {
        mBuffer = ByteBuffer.wrap(data);
        mLength = data.length;
    }


    public int length()
    {
        return mLength;
    }


    public byte get(int index) throws IndexOutOfBoundsException
    {
        if (index < 0 || mLength <= index)
        {
            // Bad index.
            throw new IndexOutOfBoundsException(
                    String.format("Bad index: index=%d, length=%d", index, mLength));
        }

        return mBuffer.get(index);
    }


    private void expandBuffer(int newBufferSize)
    {
        // Allocate a new buffer.
        ByteBuffer newBuffer = ByteBuffer.allocate(newBufferSize);

        // Copy the content of the current buffer to the new buffer.
        mBuffer.position(0);
        newBuffer.put(mBuffer);

        // Replace the buffers.
        mBuffer = newBuffer;
    }


    public void put(int data)
    {
        // If the buffer is small.
        if (mBuffer.capacity() < (mLength + 1))
        {
            expandBuffer(mLength + ADDITIONAL_BUFFER_SIZE);
        }

        mBuffer.put((byte)data);
        ++mLength;
    }


    public void put(byte[] data)
    {
        // If the buffer is small.
        if (mBuffer.capacity() < (mLength + data.length))
        {
            expandBuffer(mLength + data.length + ADDITIONAL_BUFFER_SIZE);
        }

        mBuffer.put(data);
        mLength += data.length;
    }


    public void put(ByteArray data, int index, int length)
    {
        // If the buffer is small.
        if (mBuffer.capacity() < (mLength + length))
        {
            expandBuffer(mLength + length + ADDITIONAL_BUFFER_SIZE);
        }

        byte[] bytes = data.mBuffer.array();

        mBuffer.put(bytes, index, length);
        mLength += length;
    }


    public byte[] toBytes(int beginIndex)
    {
        return toBytes(beginIndex, length());
    }


    public byte[] toBytes(int beginIndex, int endIndex)
    {
        int len = endIndex - beginIndex;

        if (len < 0 || beginIndex < 0 || mLength < endIndex)
        {
            throw new IllegalArgumentException(
                    String.format("Bad range: beginIndex=%d, endIndex=%d, length=%d",
                            beginIndex, endIndex, mLength));
        }

        byte[] bytes = new byte[len];

        if (len != 0)
        {
            System.arraycopy(mBuffer.array(), beginIndex, bytes, 0, len);
        }

        return bytes;
    }


    public void clear()
    {
        mBuffer.clear();
        mLength = 0;
    }


    public void shrink(int size)
    {
        if (mBuffer.capacity() <= size)
        {
            return;
        }

        int endIndex   = mLength;
        int beginIndex = mLength - size;

        byte[] bytes = toBytes(beginIndex, endIndex);

        mBuffer = ByteBuffer.wrap(bytes);
        mLength = bytes.length;
    }
}
