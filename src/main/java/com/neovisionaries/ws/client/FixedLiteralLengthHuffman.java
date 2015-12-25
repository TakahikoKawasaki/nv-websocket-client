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


class FixedLiteralLengthHuffman extends Huffman
{
    private static final FixedLiteralLengthHuffman INSTANCE = new FixedLiteralLengthHuffman();


    private FixedLiteralLengthHuffman()
    {
        super(buildCodeLensFromSym());
    }


    private static int[] buildCodeLensFromSym()
    {
        // 3.2.6. Compression with fixed Huffman codes (BTYPE=01)
        //
        //   Lit Value   Bits   Codes
        //   ---------   ----   ---------------------------
        //     0 - 143    8      00110000 through  10111111
        //   144 - 255    9     110010000 through 111111111
        //   256 - 279    7       0000000 through   0010111
        //   280 - 287    8      11000000 through  11000111

        int[] codeLengths = new int[288];

        int symbol;

        // 0 - 143
        for (symbol = 0; symbol < 144; ++symbol)
        {
            codeLengths[symbol] = 8;
        }

        // 144 - 255
        for (; symbol < 256; ++symbol)
        {
            codeLengths[symbol] = 9;
        }

        // 256 - 279
        for (; symbol < 280; ++symbol)
        {
            codeLengths[symbol] = 7;
        }

        // 280 - 287
        for (; symbol < 288; ++symbol)
        {
            codeLengths[symbol] = 8;
        }

        // Huffman class generates code values from code lengths.
        // Note that "code lengths are sufficient to generate the
        // actual codes". See 3.2.2. of RFC 1951.
        return codeLengths;
    }


    public static FixedLiteralLengthHuffman getInstance()
    {
        return INSTANCE;
    }
}
