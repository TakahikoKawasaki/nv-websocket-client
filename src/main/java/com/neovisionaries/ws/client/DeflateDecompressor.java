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


class DeflateDecompressor
{
    private static int[] INDICES_FROM_CODE_LENGTH_ORDER =
        { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15 };


    public static void decompress(ByteArray input, int index, ByteArray output) throws FormatException
    {
        // The data is compressed on a bit basis, so use a bit index.
        int[] bitIndex = new int[1];
        bitIndex[0] = index * 8;

        // Process all blocks one by one until the end.
        // inflateBlock() returns false if no more block exists.
        while (inflateBlock(input, bitIndex, output)) {}
    }


    private static boolean getBit(ByteArray input, int bitIndex)
    {
        int index = bitIndex / 8;
        int shift = bitIndex % 8;
        int value = input.get(index);

        // Return true if the bit pointed to by bitIndex is set.
        return ((value & (1 << shift)) != 0);
    }


    private static int getBits(ByteArray input, int bitIndex, int nBits)
    {
        int number = 0;
        int weight = 1;

        // Convert consecutive bits into a number.
        for (int i = 0; i < nBits; ++i, weight *= 2)
        {
            // getBit() returns true if the bit is set.
            if (getBit(input, bitIndex + i))
            {
                number += weight;
            }
        }

        return number;
    }


    private static boolean readBit(ByteArray input, int[] bitIndex)
    {
        boolean result = getBit(input, bitIndex[0]);

        ++bitIndex[0];

        return result;
    }


    private static int readBits(ByteArray input, int[] bitIndex, int nBits)
    {
        int number = getBits(input, bitIndex[0], nBits);

        bitIndex[0] += nBits;

        return number;
    }


    private static boolean inflateBlock(
            ByteArray input, int[] bitIndex, ByteArray output) throws FormatException
    {
        // Each block has a block header which consists of 3 bits.
        // See 3.2.3. of RFC 1951.

        // The first bit indicates whether the block is the last one or not.
        boolean last = readBit(input, bitIndex);

        // The combination of the second and the third bits indicate the
        // compression type of the block. Compression types are as follows:
        //
        //     00: No compression.
        //     01: Compressed with fixed Huffman codes
        //     10: Compressed with dynamic Huffman codes
        //     11: Reserved (error)
        int type = readBits(input, bitIndex, 2);

        switch (type)
        {
            // No compression
            case 0:
                last |= inflatePlainBlock(input, bitIndex, output);
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

        // Return true if this block is not the last one.
        return !last;
    }


    private static boolean inflatePlainBlock(ByteArray input, int[] bitIndex, ByteArray output)
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

        // A zero-length 'deflate' non-compressed block is a termination mark.
        //
        //   From RFC 7692, 1. Introduction, The 4th paragraph:
        //
        //     To align the end of compressed data to an octet boundary,
        //     this extension uses the algorithm described in Section 2.1
        //     of [RFC1979].
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
        boolean last = (len == 0);

        if (last == false)
        {
            // Copy the data to the output.
            output.put(input, index, len);

            // Make the bitIndex point to the bit next to
            // the end of the copied data.
            bitIndex[0] = (index + len) * 8;
        }

        return last;
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

        // 5 Bits: HLIT, The number of Literal/Length codes - 257 (257 - 286)
        int hlit = readBits(input, bitIndex, 5) + 257;

        // 5 Bits: HDIST, The number of Distance codes - 1 (1 - 32)
        int hdist = readBits(input, bitIndex, 5) + 1;

        // 4 Bits: HCLEN, The number of Code Length codes - 4 (4 - 19)
        int hclen = readBits(input, bitIndex, 4) + 4;

        // (hclen * 3) bits: code lengths of "values of code length".
        //
        // Note that "values of code lengths" (which ranges from 0 to 18)
        // themselves are compressed using Huffman code. In addition,
        // the order here is strange.
        int[] codeLengthsFromCodeLengthValue = new int[19];
        for (int i = 0; i < hclen; ++i)
        {
            byte codeLengthOfCodeLengthValue = (byte)readBits(input, bitIndex, 3);

            // The strange order is converted into a normal index here.
            int index = codeLengthOrderToIndex(i);

            codeLengthsFromCodeLengthValue[index] = codeLengthOfCodeLengthValue;
        }

        // Create a table to convert "code value of code length value" into
        // "code length value".
        Huffman codeLengthHuffman = new Huffman(codeLengthsFromCodeLengthValue);

        // hlit code lengths for literal/length alphabet. The code lengths are
        // encoded using the code length Huffman code that was parsed above.
        int[] codeLengthsFromLiteralLengthCode = new int[hlit];
        readCodeLengths(input, bitIndex, codeLengthsFromLiteralLengthCode, codeLengthHuffman);

        // Create a table to convert "code value of literal/length alphabet"
        // into "literal/length symbol".
        Huffman literalLengthHuffman = new Huffman(codeLengthsFromLiteralLengthCode);

        // hdist code lengths for the distance alphabet. The code lengths are
        // encoded using the code length Huffman code that was parsed above.
        int[] codeLengthsFromDistanceCode = new int[hdist];
        readCodeLengths(input, bitIndex, codeLengthsFromDistanceCode, codeLengthHuffman);

        // Create a table to convert "code value of distance alphabet" into
        // "distance symbol".
        Huffman distanceHuffman = new Huffman(codeLengthsFromDistanceCode);

        // The actual compressed data of this block. The data are encoded using
        // the literal/length and distance Huffman codes that were parsed above.
        inflateData(input, bitIndex, output, literalLengthHuffman, distanceHuffman);
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
                // As is.
                output.put(literalLength);
                continue;
            }

            // Symbol values from 257 to 285 represent <length,distance> pairs.
            // Depending on symbol values, some extra bits in the input may be
            // consumed to compute the length.
            int length = readLength(input, bitIndex, literalLength);

            // Read the distance from the input.
            int distance = readDistance(input, bitIndex, distanceHuffman);

            // Extract some data from the output buffer and copy them.
            duplicate(length, distance, output);
        }
    }


    private static int codeLengthOrderToIndex(int order)
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)
        //
        // See the description about "(HCLEN + 4) x 3 bits" in the
        // specification.
        return INDICES_FROM_CODE_LENGTH_ORDER[order];
    }


    private static void readCodeLengths(
            ByteArray input, int bitIndex[], int[] codeLengths,
            Huffman codeLengthHuffman) throws FormatException
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)

        for (int i = 0; i < codeLengths.length; ++i)
        {
            // Read a symbol value of code length.
            int codeLength = codeLengthHuffman.readSym(input, bitIndex);

            // Code lengths from 0 to 15 represent 0 to 15, respectively,
            // meaning no more extra interpretation is needed.
            if (0 <= codeLength && codeLength <= 15)
            {
                // As is.
                codeLengths[i] = codeLength;
                continue;
            }

            int repeatCount;

            switch (codeLength)
            {
                case 16:
                    // Copy the previous code length for 3 - 6 times.
                    // The next 2 bits (+3) indicate repeat count.
                    codeLength = codeLengths[i - 1];
                    repeatCount = readBits(input, bitIndex, 2) + 3;
                    break;

                case 17:
                    // Copy a code length of 0 for 3 - 10 times.
                    // The next 3 bits (+3) indicate repeat count.
                    codeLength = 0;
                    repeatCount = readBits(input, bitIndex, 3) + 3;
                    break;

                case 18:
                    // Copy a code length of 0 for 11 - 138 times.
                    // The next 7 bits (+11) indicate repeat count.
                    codeLength = 0;
                    repeatCount = readBits(input, bitIndex, 7) + 11;
                    break;

                default:
                    // Bad code length.
                    String message = String.format(
                            "[%s] Bad code length '%d' at the bit index '%d'.",
                            DeflateDecompressor.class.getSimpleName(), codeLength, bitIndex);

                    throw new FormatException(message);
            }

            // Copy the code length as many times as specified.
            for (int j = 0; j < repeatCount; ++j)
            {
                codeLengths[i + j] = codeLength;
            }

            // Skip the range filled by the above copy.
            i += repeatCount - 1;
        }
    }


    private static int readLength(
            ByteArray input, int[] bitIndex, int literalLength) throws FormatException
    {
        // 3.2.5 Compressed blocks (length and distance code)

        int baseValue;
        int nBits;

        switch (literalLength)
        {
            case 257:
            case 258:
            case 259:
            case 260:
            case 261:
            case 262:
            case 263:
            case 264:
                return (literalLength - 254);

            case 265: baseValue =  11; nBits = 1; break;
            case 266: baseValue =  13; nBits = 1; break;
            case 267: baseValue =  15; nBits = 1; break;
            case 268: baseValue =  17; nBits = 1; break;
            case 269: baseValue =  19; nBits = 2; break;
            case 270: baseValue =  23; nBits = 2; break;
            case 271: baseValue =  27; nBits = 2; break;
            case 272: baseValue =  31; nBits = 2; break;
            case 273: baseValue =  35; nBits = 3; break;
            case 274: baseValue =  43; nBits = 3; break;
            case 275: baseValue =  51; nBits = 3; break;
            case 276: baseValue =  59; nBits = 3; break;
            case 277: baseValue =  67; nBits = 4; break;
            case 278: baseValue =  83; nBits = 4; break;
            case 279: baseValue =  99; nBits = 4; break;
            case 280: baseValue = 115; nBits = 4; break;
            case 281: baseValue = 131; nBits = 5; break;
            case 282: baseValue = 163; nBits = 5; break;
            case 283: baseValue = 195; nBits = 5; break;
            case 284: baseValue = 227; nBits = 5; break;
            case 285: return 258;
            default:
                // Bad literal/length code.
                String message = String.format(
                        "[%s] Bad literal/length code '%d' at the bit index '%d'.",
                        DeflateDecompressor.class.getSimpleName(), literalLength, bitIndex[0]);

                throw new FormatException(message);
        }

        // Read a value to add to the base value.
        int n = readBits(input, bitIndex, nBits);

        return baseValue + n;
    }


    private static int readDistance(
            ByteArray input, int[] bitIndex, Huffman distanceHuffman) throws FormatException
    {
        // 3.2.5 Compressed blocks (length and distance code)

        // Read a distance code from the input.
        // It is expected to range from 0 to 29.
        int code = distanceHuffman.readSym(input, bitIndex);

        int baseValue;
        int nBits;

        switch (code)
        {
            case 0:
            case 1:
            case 2:
            case 3:
                return code + 1;

            case  4: baseValue =     5; nBits =  1; break;
            case  5: baseValue =     7; nBits =  1; break;
            case  6: baseValue =     9; nBits =  2; break;
            case  7: baseValue =    13; nBits =  2; break;
            case  8: baseValue =    17; nBits =  3; break;
            case  9: baseValue =    25; nBits =  3; break;
            case 10: baseValue =    33; nBits =  4; break;
            case 11: baseValue =    49; nBits =  4; break;
            case 12: baseValue =    65; nBits =  5; break;
            case 13: baseValue =    97; nBits =  5; break;
            case 14: baseValue =   129; nBits =  6; break;
            case 15: baseValue =   193; nBits =  6; break;
            case 16: baseValue =   257; nBits =  7; break;
            case 17: baseValue =   385; nBits =  7; break;
            case 18: baseValue =   513; nBits =  8; break;
            case 19: baseValue =   769; nBits =  8; break;
            case 20: baseValue =  1025; nBits =  9; break;
            case 21: baseValue =  1537; nBits =  9; break;
            case 22: baseValue =  2049; nBits = 10; break;
            case 23: baseValue =  3073; nBits = 10; break;
            case 24: baseValue =  4097; nBits = 11; break;
            case 25: baseValue =  6145; nBits = 11; break;
            case 26: baseValue =  8193; nBits = 12; break;
            case 27: baseValue = 12289; nBits = 12; break;
            case 28: baseValue = 16385; nBits = 13; break;
            case 29: baseValue = 24577; nBits = 13; break;
            default:
                // Distance codes 30-31 will never actually occur
                // in the compressed data, the specification says.

                // Bad distance code.
                String message = String.format(
                        "[%s] Bad distance code '%d' at the bit index '%d'.",
                        DeflateDecompressor.class.getSimpleName(), code, bitIndex[0]);

                throw new FormatException(message);
        }

        // Read a value to add to the base value.
        int n = readBits(input, bitIndex, nBits);

        return baseValue + n;
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
