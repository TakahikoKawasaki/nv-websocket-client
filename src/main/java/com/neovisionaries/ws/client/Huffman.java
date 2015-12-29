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
 * Huffman coding for DEFLATE format (<a href="http://tools.ietf.org/html/rfc1951"
 * >RFC 1951</a>).
 */
class Huffman
{
    private final int mMinCodeLen;
    private final int mMaxCodeLen;
    private final int[] mMaxCodeValsFromCodeLen;
    private final int[] mSymsFromCodeVal;


    public Huffman(int[] codeLensFromSym)
    {
        // Remember the minimum and maximum code lengths.
        mMinCodeLen = Math.max(Misc.min(codeLensFromSym), 1);
        mMaxCodeLen = Misc.max(codeLensFromSym);

        // Count the number of entries for each code length.
        int[] countsFromCodeLen = createCountsFromCodeLen(codeLensFromSym, mMaxCodeLen);

        // Create an array holding the maximum code values for each code length.
        Object[] out = new Object[2];
        mMaxCodeValsFromCodeLen = createMaxCodeValsFromCodeLen(countsFromCodeLen, mMaxCodeLen, out);

        // Create a table to convert code values int symbols.
        int[] codeValsFromCodeLen = (int[])out[0];
        int maxCodeVal = ((Integer)out[1]).intValue();
        mSymsFromCodeVal = createSymsFromCodeVal(codeLensFromSym, codeValsFromCodeLen, maxCodeVal);
    }


    /**
     * Create an array whose elements have the given initial value.
     */
    private static int[] createIntArray(int size, int initialValue)
    {
        int[] array = new int[size];

        for (int i = 0; i < size; ++i)
        {
            array[i] = initialValue;
        }

        return array;
    }


    private static int[] createCountsFromCodeLen(int[] codeLensFromSym, int maxCodeLen)
    {
        int[] countsFromCodeLen = new int[maxCodeLen + 1];

        // Count the number of entries for each code length.
        // This corresponds to the step 1 in 3.2.2. of RFC 1951.
        for (int symbol = 0; symbol < codeLensFromSym.length; ++symbol)
        {
            int codeLength = codeLensFromSym[symbol];
            ++countsFromCodeLen[codeLength];
        }

        return countsFromCodeLen;
    }


    private static int[] createMaxCodeValsFromCodeLen(int[] countsFromCodeLen, int maxCodeLen, Object[] out)
    {
        // Initialize an array that holds the maximum code values
        // for each code length. '-1' indicates that there is no
        // code value for the code length.
        int[] maxCodeValsFromCodeLen = createIntArray(maxCodeLen + 1, -1);

        // Compute the smallest code value for each code length.
        // This corresponds to the step 2 in 3.2.2. of RFC 1951.
        int minCodeVal = 0;
        int maxCodeVal = 0;
        countsFromCodeLen[0] = 0;
        int[] codeValsFromCodeLen = new int[maxCodeLen + 1];
        for (int codeLen = 1; codeLen < countsFromCodeLen.length; ++codeLen)
        {
            // Compute the minimum code value for each code length.
            int prevCount = countsFromCodeLen[codeLen - 1];
            minCodeVal = (minCodeVal + prevCount) << 1;
            codeValsFromCodeLen[codeLen] = minCodeVal;

            // Compute the maximum code value for each code length.
            maxCodeVal = minCodeVal + countsFromCodeLen[codeLen] - 1;
            maxCodeValsFromCodeLen[codeLen] = maxCodeVal;
        }

        out[0] = codeValsFromCodeLen;
        out[1] = Integer.valueOf(maxCodeVal);

        return maxCodeValsFromCodeLen;
    }


    private static int[] createSymsFromCodeVal(int[] codeLensFromSym, int[] codeValsFromCodeLen, int maxCodeVal)
    {
        int[] symsFromCodeVal = new int[maxCodeVal + 1];

        // Set up a table to convert code values into symbols.
        // This corresponds to the step 3 in 3.2.2. of RFC 1951.

        for (int sym = 0; sym < codeLensFromSym.length; ++sym)
        {
            int codeLen = codeLensFromSym[sym];

            if (codeLen == 0)
            {
                continue;
            }

            int codeVal = codeValsFromCodeLen[codeLen]++;
            symsFromCodeVal[codeVal] = sym;
        }

        return symsFromCodeVal;
    }


    public int readSym(ByteArray data, int[] bitIndex) throws FormatException
    {
        for (int codeLen = mMinCodeLen; codeLen <= mMaxCodeLen; ++codeLen)
        {
            // Get the maximum one from among the code values
            // whose code length is 'codeLen'.
            int maxCodeVal = mMaxCodeValsFromCodeLen[codeLen];

            if (maxCodeVal < 0)
            {
                // There is no code value whose code length is 'codeLen'.
                continue;
            }

            // Read a code value from the input assuming its code length is 'codeLen'.
            int codeVal = data.getHuffmanBits(bitIndex[0], codeLen);

            if (maxCodeVal < codeVal)
            {
                // The read code value is bigger than the maximum code value
                // among the code values whose code length is 'codeLen'.
                //
                // Considering the latter rule of the two added for DEFLATE format
                // (3.2.2. Use of Huffman coding in the "deflate" format),
                //
                //     * All codes of a given bit length have lexicographically
                //       consecutive values, in the same order as the symbols
                //       they represent;
                //
                //     * Shorter codes lexicographically precede longer codes.
                //
                // We can expect that the code length of the code value we are
                // parsing is longer than the current 'codeLen'.
                continue;
            }

            // Convert the code value into a symbol value.
            int sym = mSymsFromCodeVal[codeVal];

            // Consume the bits of the code value.
            bitIndex[0] += codeLen;

            return sym;
        }

        // Bad code at the bit index.
        String message = String.format(
                "[%s] Bad code at the bit index '%d'.",
                getClass().getSimpleName(), bitIndex[0]);

        throw new FormatException(message);
    }
}
