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


import java.util.Map;


/**
 * Per-Message Deflate Extension (<a href="https://tools.ietf.org/html/rfc7692#section-7"
 * >7&#46; The "permessage-deflate" Extension</a> in
 * <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7692#section-7"
 *      >7&#46; The "permessage-deflate" Extension in RFC 7692</a>
 */
class PerMessageDeflateExtension extends PerMessageCompressionExtension
{
    private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
    private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
    private static final String SERVER_MAX_WINDOW_BITS     = "server_max_window_bits";
    private static final String CLIENT_MAX_WINDOW_BITS     = "client_max_window_bits";
    private static final byte[] COMPRESSION_TERMINATOR     = { (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF };

    private static final int MIN_BITS = 8;
    private static final int MAX_BITS = 15;
    private static final int MIN_WINDOW_SIZE = 256;
    private static final int MAX_WINDOW_SIZE = 32768;
    private static final int INCOMING_SLIDING_WINDOW_MARGIN = 1024;

    private boolean mServerNoContextTakeover;
    private boolean mClientNoContextTakeover;
    private int mServerWindowSize = MAX_WINDOW_SIZE;
    private int mClientWindowSize = MAX_WINDOW_SIZE;
    private int mIncomingSlidingWindowBufferSize;
    private ByteArray mIncomingSlidingWindow;


    public PerMessageDeflateExtension()
    {
        super(WebSocketExtension.PERMESSAGE_DEFLATE);
    }


    public PerMessageDeflateExtension(String name)
    {
        super(name);
    }


    @Override
    void validate() throws WebSocketException
    {
        // For each parameter
        for (Map.Entry<String, String> entry : getParameters().entrySet())
        {
            validateParameter(entry.getKey(), entry.getValue());
        }

        mIncomingSlidingWindowBufferSize = mServerWindowSize + INCOMING_SLIDING_WINDOW_MARGIN;
    }


    public boolean isServerNoContextTakeover()
    {
        return mServerNoContextTakeover;
    }


    public boolean isClientNoContextTakeover()
    {
        return mClientNoContextTakeover;
    }


    public int getServerWindowSize()
    {
        return mServerWindowSize;
    }


    public int getClientWindowSize()
    {
        return mClientWindowSize;
    }


    private void validateParameter(String key, String value) throws WebSocketException
    {
        if (SERVER_NO_CONTEXT_TAKEOVER.equals(key))
        {
            mServerNoContextTakeover = true;
        }
        else if (CLIENT_NO_CONTEXT_TAKEOVER.equals(key))
        {
            mClientNoContextTakeover = true;
        }
        else if (SERVER_MAX_WINDOW_BITS.equals(key))
        {
            mServerWindowSize = computeWindowSize(key, value);
        }
        else if (CLIENT_MAX_WINDOW_BITS.equals(key))
        {
            mClientWindowSize = computeWindowSize(key, value);
        }
        else
        {
            // permessage-deflate extension contains an unsupported parameter.
            throw new WebSocketException(
                WebSocketError.PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER,
                "permessage-deflate extension contains an unsupported parameter: " + key);
        }
    }


    private int computeWindowSize(String key, String value) throws WebSocketException
    {
        int bits = extractMaxWindowBits(key, value);
        int size = MIN_WINDOW_SIZE;

        for (int i = MIN_BITS; i < bits; ++i)
        {
            size *= 2;
        }

        return size;
    }


    private int extractMaxWindowBits(String key, String value) throws WebSocketException
    {
        int bits = parseMaxWindowBits(value);

        if (bits < 0)
        {
            String message = String.format(
                    "The value of %s parameter of permessage-deflate extension is invalid: %s",
                    key, value);

            throw new WebSocketException(
                WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, message);
        }

        return bits;
    }


    private int parseMaxWindowBits(String value)
    {
        if (value == null)
        {
            return -1;
        }

        int bits;

        try
        {
            bits = Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }

        if (bits < MIN_BITS || MAX_BITS < bits)
        {
            return -1;
        }

        return bits;
    }


    @Override
    protected byte[] decompress(byte[] compressed) throws WebSocketException
    {
        // Append 0x00, 0x00, 0xFF and 0xFF.
        //
        //   From RFC 7692, 7.2.2. Decompression
        //
        //     An endpoint uses the following algorithm to decompress a message.
        //
        //     1.  Append 4 octets of 0x00 0x00 0xff 0xff to the tail end of
        //         the payload of the message.
        //
        //     2.  Decompress the resulting data using DEFLATE.
        //
        //
        //   From RFC 1979, 2.1. Packet Format, Data, The 3rd paragraph:
        //
        //     The basic format of the compressed data is precisely described by
        //     the 'Deflate' Compressed Data Format Specification[3].  Each
        //     transmitted packet must begin at a 'deflate' block boundary, to
        //     ensure synchronization when incompressible data resets the
        //     transmitter's state; to ensure this, each transmitted packet must
        //     be terminated with a zero-length 'deflate' non-compressed block
        //     (BTYPE of 00).  This means that the last four bytes of the
        //     compressed format must be 0x00 0x00 0xFF 0xFF.  These bytes MUST
        //     be removed before transmission; the receiver can reinsert them if
        //     required by the implementation.
        //
        int inputLen = compressed.length + COMPRESSION_TERMINATOR.length;

        // Wrap the compressed byte array with ByteArray.
        ByteArray input = new ByteArray(inputLen);
        input.put(compressed);
        input.put(COMPRESSION_TERMINATOR);

        if (mIncomingSlidingWindow == null)
        {
            mIncomingSlidingWindow = new ByteArray(mIncomingSlidingWindowBufferSize);
        }

        // The size of the sliding window before decompression.
        int outPos = mIncomingSlidingWindow.length();

        try
        {
            // Decompress.
            DeflateDecompressor.decompress(input, mIncomingSlidingWindow);
        }
        catch (Exception e)
        {
            // Failed to decompress the message.
            throw new WebSocketException(
                    WebSocketError.DECOMPRESSION_ERROR,
                    String.format("Failed to decompress the message: %s", e.getMessage()), e);
        }

        byte[] output = mIncomingSlidingWindow.toBytes(outPos);

        // Shrink the size of the incoming sliding window.
        mIncomingSlidingWindow.shrink(mIncomingSlidingWindowBufferSize);

        if (mServerNoContextTakeover)
        {
            // No need to remember the message for the next decompression.
            mIncomingSlidingWindow.clear();
        }

        return output;
    }


    @Override
    protected byte[] compress(byte[] plain) throws WebSocketException
    {
        if (canCompress(plain) == false)
        {
            // Compression should not be performed.
            return plain;
        }

        // From RFC 7692, 7.2.1. Compression
        //
        //   An endpoint uses the following algorithm to compress a message.
        //
        //   1.  Compress all the octets of the payload of the message using
        //       DEFLATE.
        //
        //   2.  If the resulting data does not end with an empty DEFLATE block
        //       with no compression (the "BTYPE" bits are set to 00), append an
        //       empty DEFLATE block with no compression to the tail end.
        //
        //   3.  Remove 4 octets (that are 0x00 0x00 0xff 0xff) from the tail end.
        //       After this step, the last octet of the compressed data contains
        //       (possibly part of) the DEFLATE header bits with the "BTYPE" bits
        //       set to 00.

        try
        {
            // Compress.
            byte[] compressed = DeflateCompressor.compress(plain);

            // Adjust the compressed data to comply with RFC 7692.
            return adjustCompressedData(compressed);
        }
        catch (Exception e)
        {
            // Failed to compress the message.
            throw new WebSocketException(
                    WebSocketError.COMPRESSION_ERROR,
                    String.format("Failed to compress the message: %s", e.getMessage()), e);
        }
    }


    private boolean canCompress(byte[] plain)
    {
        // The current compression implementation (DeflateCompressor)
        // cannot control the size of the internal sliding window on
        // the client side.
        //
        // Therefore, compression should not be performed if there is
        // a possibility that Huffman codes in compressed data may
        // refer to bigger distances than the agreed sliding window
        // size (which is computed based on client_max_window_bits).
        //
        //   From RFC 7692, 7.2.1. Compression
        //
        //     If the "agreed parameters" contain the "client_max_window_bits"
        //     extension parameter with a value of w, the client MUST NOT use
        //     an LZ77 sliding window longer than the w-th power of 2 bytes
        //     to compress messages to send.
        //

        // If the agreed sliding window size is the maximum value allowed
        // by the DEFLATE specification, the size of the internal sliding
        // window of the compressor does not have to be cared about.
        if (mClientWindowSize == MAX_WINDOW_SIZE)
        {
            // Can be compressed.
            return true;
        }

        // Otherwise, considering the fact that the current implementation
        // does not support context takeover on the client side, it can be
        // said that Huffman codes in compressed data will not refer to
        // bigger distances than the agreed sliding window size if the size
        // of the original plain data is less than the agreed sliding window
        // size.
        if (plain.length < mClientWindowSize)
        {
            // Can be compressed.
            return true;
        }

        // Cannot exclude the possibility that Huffman codes in compressed
        // data may refer to bigger distances than the agreed sliding window
        // size. Therefore, compression should not be performed.
        return false;
    }


    private static byte[] adjustCompressedData(byte[] compressed) throws FormatException
    {
        // Wrap the compressed data with ByteArray. '+1' here is for 3 bits,
        // '000', that may be appended at the bottom of this method.
        ByteArray data = new ByteArray(compressed.length + 1);
        data.put(compressed);

        // The data is compressed on a bit basis, so use a bit index.
        int[] bitIndex = new int[1];

        // The flag to indicate whether the last block in the original
        // compressed data is an empty block with no compression.
        boolean[] hasEmptyBlock = new boolean[1];

        // Skip all blocks one by one until the end.
        // skipBlock() returns false if no more block exists.
        while (skipBlock(data, bitIndex, hasEmptyBlock));

        // If the last block is an empty block with no compression.
        if (hasEmptyBlock[0])
        {
            // In this case, it is enough to drop the last four bytes
            // (0x00 0x00 0xFF 0xFF).
            return data.toBytes(0, ((bitIndex[0] - 1) / 8) + 1 - 4);
        }

        // Append 3 bits, '000'.
        //
        // The first bit is BFINAL and its value is '0'. Note that '1'
        // is not used here although the block being appended is the
        // last block. It's because some server-side implementations
        // fail to inflate compressed data with BFINAL=1.
        //
        // The second and the third bits are '00' and it means NO
        // COMPRESSION.
        appendEmptyBlock(data, bitIndex);

        // Convert the ByteArray to byte[].
        return data.toBytes(0, ((bitIndex[0] - 1) / 8) + 1);
    }


    private static void appendEmptyBlock(ByteArray data, int[] bitIndex)
    {
        int shift = bitIndex[0] % 8;

        // ? = used (0 or 1), x = unused (= 0).
        //
        //           | Current  | After 3 bits are appended
        // ----------+----------+---------------------------
        // shift = 1 | xxxxxxx? | xxxx000?
        // shift = 2 | xxxxxx?? | xxx000??
        // shift = 3 | xxxxx??? | xx000???
        // shift = 4 | xxxx???? | x000????
        // shift = 5 | xxx????? | 000?????
        // shift = 6 | xx?????? | 00?????? xxxxxxx0
        // shift = 7 | x??????? | 0??????? xxxxxx00
        // shift = 0 | ???????? | ???????? xxxxx000

        switch (shift)
        {
            case 6:
            case 7:
            case 0:
                data.put(0);
        }

        // Update the bit index for the 3 bits.
        bitIndex[0] += 3;
    }


    private static boolean skipBlock(
            ByteArray input, int[] bitIndex, boolean[] hasEmptyBlock) throws FormatException
    {
        // Each block has a block header which consists of 3 bits.
        // See 3.2.3. of RFC 1951.

        // The first bit indicates whether the block is the last one or not.
        boolean last = input.readBit(bitIndex);

        if (last)
        {
            // Clear the BFINAL bit because some server-side implementations
            // fail to inflate compressed data with BFINAL=1.
            input.clearBit(bitIndex[0] - 1);
        }

        // The combination of the second and the third bits indicate the
        // compression type of the block. Compression types are as follows:
        //
        //     00: No compression.
        //     01: Compressed with fixed Huffman codes
        //     10: Compressed with dynamic Huffman codes
        //     11: Reserved (error)
        //
        int type = input.readBits(bitIndex, 2);

        // This flag becomes true if skipPlainBlock() is called and it returns 0.
        boolean plain0 = false;

        switch (type)
        {
            // No compression
            case 0:
                // Skip the plain block. skipPlainBlock() returns the data length.
                plain0 = (skipPlainBlock(input, bitIndex) == 0);
                break;

            // Compressed with fixed Huffman codes
            case 1:
                skipFixedBlock(input, bitIndex);
                break;

            // Compressed with dynamic Huffman codes
            case 2:
                skipDynamicBlock(input, bitIndex);
                break;

            // Bad format
            default:
                // Bad compression type at the bit index.
                String message = String.format(
                        "[%s] Bad compression type '11' at the bit index '%d'.",
                        PerMessageDeflateExtension.class.getSimpleName(), bitIndex[0]);

                throw new FormatException(message);
        }

        // If no more data are available.
        if (input.length() <= (bitIndex[0] / 8))
        {
            // Last even if the BFINAL bit is false.
            last = true;
        }

        if (last && plain0)
        {
            // The last block is an empty block with no compression.
            hasEmptyBlock[0] = true;
        }

        // Return true if this block is not the last one.
        return !last;
    }


    private static int skipPlainBlock(ByteArray input, int[] bitIndex)
    {
        // 3.2.4 Non-compressed blocks (BTYPE=00)

        // Skip any remaining bits in current partially processed byte.
        int bi = (bitIndex[0] + 7) & ~7;

        // Data copy is performed on a byte basis, so convert the bit index
        // to a byte index.
        int index = bi / 8;

        // LEN: 2 bytes. The data length.
        int len = (input.get(index) & 0xFF) + (input.get(index + 1) & 0xFF) * 256;

        // NLEN: 2 bytes. The one's complement of LEN.

        // Skip LEN and NLEN.
        index += 4;

        // Make the bitIndex point to the bit next to
        // the end of the copied data.
        bitIndex[0] = (index + len) * 8;

        return len;
    }


    private static void skipFixedBlock(ByteArray input, int[] bitIndex) throws FormatException
    {
        // 3.2.6 Compression with fixed Huffman codes (BTYPE=01)

        // Inflate the compressed data using the pre-defined
        // conversion tables. The specification says in 3.2.2
        // as follows.
        //
        //   The only differences between the two compressed
        //   cases is how the Huffman codes for the literal/
        //   length and distance alphabets are defined.
        //
        // The "two compressed cases" in the above sentence are
        // "fixed Huffman codes" and "dynamic Huffman codes".
        skipData(input, bitIndex,
                FixedLiteralLengthHuffman.getInstance(),
                FixedDistanceHuffman.getInstance());
    }


    private static void skipDynamicBlock(ByteArray input, int[] bitIndex) throws FormatException
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)

        // Read 2 tables. One is a table to convert "code value of literal/length
        // alphabet" into "literal/length symbol". The other is a table to convert
        // "code value of distance alphabet" into "distance symbol".
        Huffman[] tables = new Huffman[2];
        DeflateUtil.readDynamicTables(input, bitIndex, tables);

        skipData(input, bitIndex, tables[0], tables[1]);
    }


    private static void skipData(
            ByteArray input, int[] bitIndex,
            Huffman literalLengthHuffman, Huffman distanceHuffman) throws FormatException
    {
        // 3.2.5 Compressed blocks (length and distance codes)

        while (true)
        {
            // Read a literal/length symbol from the input.
            int literalLength = literalLengthHuffman.readSym(input, bitIndex);

            // Symbol value '256' indicates the end.
            if (literalLength == 256)
            {
                // End of this data.
                break;
            }

            // Symbol values from 0 to 255 represent literal values.
            if (0 <= literalLength && literalLength <= 255)
            {
                // Output as is.
                continue;
            }

            // Symbol values from 257 to 285 represent <length,distance> pairs.
            // Depending on symbol values, some extra bits in the input may be
            // consumed to compute the length.
            DeflateUtil.readLength(input, bitIndex, literalLength);

            // Read the distance from the input.
            DeflateUtil.readDistance(input, bitIndex, distanceHuffman);
        }
    }
}
