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
 * Utility methods for DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>).
 */
class DeflateUtil
{
    private static int[] INDICES_FROM_CODE_LENGTH_ORDER =
        { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15 };


    public static void readDynamicTables(
            ByteArray input, int[] bitIndex, Huffman[] tables) throws FormatException
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)

        // 5 Bits: HLIT, The number of Literal/Length codes - 257 (257 - 286)
        int hlit = input.readBits(bitIndex, 5) + 257;

        // 5 Bits: HDIST, The number of Distance codes - 1 (1 - 32)
        int hdist = input.readBits(bitIndex, 5) + 1;

        // 4 Bits: HCLEN, The number of Code Length codes - 4 (4 - 19)
        int hclen = input.readBits(bitIndex, 4) + 4;

        // (hclen * 3) bits: code lengths of "values of code length".
        //
        // Note that "values of code lengths" (which ranges from 0 to 18)
        // themselves are compressed using Huffman code. In addition,
        // the order here is strange.
        int[] codeLengthsFromCodeLengthValue = new int[19];
        for (int i = 0; i < hclen; ++i)
        {
            byte codeLengthOfCodeLengthValue = (byte)input.readBits(bitIndex, 3);

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

        tables[0] = literalLengthHuffman;
        tables[1] = distanceHuffman;
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
                    repeatCount = input.readBits(bitIndex, 2) + 3;
                    break;

                case 17:
                    // Copy a code length of 0 for 3 - 10 times.
                    // The next 3 bits (+3) indicate repeat count.
                    codeLength = 0;
                    repeatCount = input.readBits(bitIndex, 3) + 3;
                    break;

                case 18:
                    // Copy a code length of 0 for 11 - 138 times.
                    // The next 7 bits (+11) indicate repeat count.
                    codeLength = 0;
                    repeatCount = input.readBits(bitIndex, 7) + 11;
                    break;

                default:
                    // Bad code length.
                    String message = String.format(
                            "[%s] Bad code length '%d' at the bit index '%d'.",
                            DeflateUtil.class.getSimpleName(), codeLength, bitIndex);

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


    private static int codeLengthOrderToIndex(int order)
    {
        // 3.2.7 Compression with dynamic Huffman codes (BTYPE=10)
        //
        // See the description about "(HCLEN + 4) x 3 bits" in the
        // specification.
        return INDICES_FROM_CODE_LENGTH_ORDER[order];
    }


    public static int readLength(
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
                        DeflateUtil.class.getSimpleName(), literalLength, bitIndex[0]);

                throw new FormatException(message);
        }

        // Read a value to add to the base value.
        int n = input.readBits(bitIndex, nBits);

        return baseValue + n;
    }


    public static int readDistance(
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
                        DeflateUtil.class.getSimpleName(), code, bitIndex[0]);

                throw new FormatException(message);
        }

        // Read a value to add to the base value.
        int n = input.readBits(bitIndex, nBits);

        return baseValue + n;
    }
}
