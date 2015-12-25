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


class FixedDistanceHuffman extends Huffman
{
    private static final FixedDistanceHuffman INSTANCE = new FixedDistanceHuffman();


    private FixedDistanceHuffman()
    {
        super(buildCodeLensFromSym());
    }


    private static int[] buildCodeLensFromSym()
    {
        // 3.2.6. Compression with fixed Huffman codes (BTYPE=01)
        //
        // "Distance codes 0-31 are represented by (fixed-length)
        // 5-bit codes", the specification says.

        int[] codeLengths = new int[32];

        for (int symbol = 0; symbol < 32; ++symbol)
        {
            codeLengths[symbol] = 5;
        }

        // Let Huffman class generate code values from code lengths.
        // Note that "code lengths are sufficient to generate the
        // actual codes". See 3.2.2. of RFC 1951.
        return codeLengths;
    }


    public static FixedDistanceHuffman getInstance()
    {
        return INSTANCE;
    }
}
