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
 * DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>)
 * decompressor implementation from scratch.
 */
class DeflateDecompressor
{
    public static void decompress(ByteArray input, ByteArray output) throws FormatException
    {
        decompress(input, 0, output);
    }


    private static void decompress(ByteArray input, int index, ByteArray output) throws FormatException
    {
        // The data is compressed on a bit basis, so use a bit index.
        int[] bitIndex = new int[1];
        bitIndex[0] = index * 8;

        // Process all blocks one by one until the end.
        // inflateBlock() returns false if no more block exists.
        while (inflateBlock(input, bitIndex, output)) {}
    }


    private static boolean inflateBlock(
            ByteArray input, int[] bitIndex, ByteArray output) throws FormatException
    {
        // Each block has a block header which consists of 3 bits.
        // See 3.2.3. of RFC 1951.

        // The first bit indicates whether the block is the last one or not.
        boolean last = input.readBit(bitIndex);

        // The combination of the second and the third bits indicate the
        // compression type of the block. Compression types are as follows:
        //
        //     00: No compression.
        //     01: Compressed with fixed Huffman codes
        //     10: Compressed with dynamic Huffman codes
        //     11: Reserved (error)
        //
        int type = input.readBits(bitIndex, 2);

        switch (type)
        {
            // No compression
            case 0:
                inflatePlainBlock(input, bitIndex, output);
                break;

            // Compressed with fixed Huffman codes
            case 1:
                inflateFixedBlock(input, bitIndex, output);
                break;

            // Compressed with dynamic Huffman codes
            case 2:
                inflateDynamicBlock(input, bitIndex, output);
                break;

            // Bad format
            default:
                // Bad compression type at the bit index.
                String message = String.format(
                        "[%s] Bad compression type '11' at the bit index '%d'.",
                        DeflateDecompressor.class.getSimpleName(), bitIndex[0]);

                throw new FormatException(message);
        }

        // If no more data are available.
        if (input.length() <= (bitIndex[0] / 8))
        {
            // Last even if BFINAL bit is false.
            last = true;
        }

        // Return true if this block is not the last one.
        return !last;
    }


    private static void inflatePlainBlock(ByteArray input, int[] bitIndex, ByteArray output)
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

        // Copy the data to the output.
        output.put(input, index, len);

        // Make the bitIndex point to the bit next to
        // the end of the copied data.
        bitIndex[0] = (index + len) * 8;
    }


    private static void inflateFixedBlock(
            ByteArray input, int[] bitIndex, ByteArray output) throws FormatException
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
        inflateData(input, bitIndex, output,
                FixedLiteralLengthHuffman.getInstance(),
                FixedDistanceHuffman.getInstance());
    }


    private static void inflateDynamicBlock(
            ByteArray input, int[] bitIndex, ByteArray output) throws FormatException
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)

        // Read 2 tables. One is a table to convert "code value of literal/length
        // alphabet" into "literal/length symbol". The other is a table to convert
        // "code value of distance alphabet" into "distance symbol".
        Huffman[] tables = new Huffman[2];
        DeflateUtil.readDynamicTables(input, bitIndex, tables);

        // The actual compressed data of this block. The data are encoded using
        // the literal/length and distance Huffman codes that were parsed above.
        inflateData(input, bitIndex, output, tables[0], tables[1]);
    }


    private static void inflateData(
            ByteArray input, int[] bitIndex, ByteArray output,
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
                output.put(literalLength);
                continue;
            }

            // Symbol values from 257 to 285 represent <length,distance> pairs.
            // Depending on symbol values, some extra bits in the input may be
            // consumed to compute the length.
            int length = DeflateUtil.readLength(input, bitIndex, literalLength);

            // Read the distance from the input.
            int distance = DeflateUtil.readDistance(input, bitIndex, distanceHuffman);

            // Extract some data from the output buffer and copy them.
            duplicate(length, distance, output);
        }
    }


    private static void duplicate(int length, int distance, ByteArray output)
    {
        // Get the number of bytes written so far.
        int sourceLength = output.length();

        // An array to finally append to the output.
        byte[] target = new byte[length];

        // The position from which to start copying data.
        int initialPosition = sourceLength - distance;
        int sourceIndex = initialPosition;

        for (int targetIndex = 0; targetIndex < length; ++targetIndex, ++sourceIndex)
        {
            if (sourceLength <= sourceIndex)
            {
                // Reached the end of the current output buffer.
                // The specification says as follows in 3.2.3.
                //
                //   Note also that the referenced string may
                //   overlap the current position; for example,
                //   if the last 2 bytes decoded have values X
                //   and Y, a string reference with <length=5,
                //   distance=2> adds X,Y,X,Y,X to the output
                //   stream.

                // repeat.
                sourceIndex = initialPosition;
            }

            target[targetIndex] = output.get(sourceIndex);
        }

        // Append the duplicated bytes to the output.
        output.put(target);
    }
}
