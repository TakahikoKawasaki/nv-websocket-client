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


/**
 * Expandable byte array with byte-basis and bit-basis operations.
 */
class ByteArray
{
    private static final int ADDITIONAL_BUFFER_SIZE = 1024;

    // The buffer.
    private ByteBuffer mBuffer;

    // The current length.
    private int mLength;


    /**
     * Constructor with initial capacity.
     *
     * @param capacity
     *         Initial capacity for the internal buffer.
     */
    public ByteArray(int capacity)
    {
        mBuffer = ByteBuffer.allocate(capacity);
        mLength = 0;
    }


    /**
     * Constructor with initial data. The length of the data is used
     * as the initial capacity of the internal buffer.
     *
     * @param data
     *         Initial data.
     */
    public ByteArray(byte[] data)
    {
        mBuffer = ByteBuffer.wrap(data);
        mLength = data.length;
    }


    /**
     * The length of the data.
     */
    public int length()
    {
        return mLength;
    }


    /**
     * Get a byte at the index.
     */
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


    /**
     * Expand the size of the internal buffer.
     */
    private void expandBuffer(int newBufferSize)
    {
        // Allocate a new buffer.
        ByteBuffer newBuffer = ByteBuffer.allocate(newBufferSize);

        // Copy the content of the current buffer to the new buffer.
        int oldPosition = mBuffer.position();
        mBuffer.position(0);
        newBuffer.put(mBuffer);
        newBuffer.position(oldPosition);

        // Replace the buffers.
        mBuffer = newBuffer;
    }


    /**
     * Add a byte at the current position.
     */
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


    /**
     * Add data at the current position.
     *
     * @param source
     *         Source data.
     */
    public void put(byte[] source)
    {
        // If the buffer is small.
        if (mBuffer.capacity() < (mLength + source.length))
        {
            expandBuffer(mLength + source.length + ADDITIONAL_BUFFER_SIZE);
        }

        mBuffer.put(source);
        mLength += source.length;
    }


    /**
     * Add data at the current position.
     *
     * @param source
     *         Source data.
     *
     * @param index
     *         The index in the source data. Data from the index is copied.
     *
     * @param length
     *         The length of data to copy.
     */
    public void put(byte[] source, int index, int length)
    {
        // If the buffer is small.
        if (mBuffer.capacity() < (mLength + length))
        {
            expandBuffer(mLength + length + ADDITIONAL_BUFFER_SIZE);
        }

        mBuffer.put(source, index, length);
        mLength += length;
    }


    /**
     * Add data at the current position.
     *
     * @param source
     *         Source data.
     *
     * @param index
     *         The index in the source data. Data from the index is copied.
     *
     * @param length
     *         The length of data to copy.
     */
    public void put(ByteArray source, int index, int length)
    {
        put(source.mBuffer.array(), index, length);
    }


    /**
     * Convert to a byte array (<code>byte[]</code>).
     */
    public byte[] toBytes()
    {
        return toBytes(0);
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
        mBuffer.position(0);
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
        mBuffer.position(bytes.length);
        mLength = bytes.length;
    }


    public boolean getBit(int bitIndex)
    {
        int index = bitIndex / 8;
        int shift = bitIndex % 8;
        int value = get(index);

        // Return true if the bit pointed to by bitIndex is set.
        return ((value & (1 << shift)) != 0);
    }


    public int getBits(int bitIndex, int nBits)
    {
        int number = 0;
        int weight = 1;

        // Convert consecutive bits into a number.
        for (int i = 0; i < nBits; ++i, weight *= 2)
        {
            // getBit() returns true if the bit is set.
            if (getBit(bitIndex + i))
            {
                number += weight;
            }
        }

        return number;
    }


    public int getHuffmanBits(int bitIndex, int nBits)
    {
        int number = 0;
        int weight = 1;

        // Convert consecutive bits into a number.
        //
        // Note that 'i' is initialized by 'nBits - 1', not by 1.
        // This is because "3.1.1. Packing into bytes" in RFC 1951
        // says as follows:
        //
        //     Huffman codes are packed starting with the most
        //     significant bit of the code.
        //
        for (int i = nBits - 1; 0 <= i; --i, weight *= 2)
        {
            // getBit() returns true if the bit is set.
            if (getBit(bitIndex + i))
            {
                number += weight;
            }
        }

        return number;
    }


    public boolean readBit(int[] bitIndex)
    {
        boolean result = getBit(bitIndex[0]);

        ++bitIndex[0];

        return result;
    }


    public int readBits(int[] bitIndex, int nBits)
    {
        int number = getBits(bitIndex[0], nBits);

        bitIndex[0] += nBits;

        return number;
    }


    public void setBit(int bitIndex, boolean bit)
    {
        int index = bitIndex / 8;
        int shift = bitIndex % 8;
        int value = get(index);

        if (bit)
        {
            value |= (1 << shift);
        }
        else
        {
            value &= ~(1 << shift);
        }

        mBuffer.put(index, (byte)value);
    }


    public void clearBit(int bitIndex)
    {
        setBit(bitIndex, false);
    }
}
