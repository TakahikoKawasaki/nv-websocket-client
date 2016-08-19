/*
 * Copyright (C) 2016 Neo Visionaries Inc.
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


import static org.junit.Assert.assertEquals;
import java.net.URI;
import org.junit.Test;


public class MiscTest
{
    private static void extractHostTest(String expected, String input)
    {
        URI uri = URI.create(input);

        String result = Misc.extractHost(uri);

        assertEquals(expected, result);
    }


    private static void extractHostFromAuthorityPartTest(String expected, String input)
    {
        String result = Misc.extractHostFromAuthorityPart(input);

        assertEquals(expected, result);
    }


    private static void extractHostFromEntireUriTest(String expected, String input)
    {
        String result = Misc.extractHostFromEntireUri(input);

        assertEquals(expected, result);
    }


    @Test
    public void test01()
    {
        extractHostFromAuthorityPartTest("example.com", "example.com");
    }


    @Test
    public void test02()
    {
        extractHostFromAuthorityPartTest("example.com", "example.com:8080");
    }


    @Test
    public void test03()
    {
        extractHostFromAuthorityPartTest("example.com", "id:password@example.com");
    }


    @Test
    public void test04()
    {
        extractHostFromAuthorityPartTest("example.com", "id:password@example.com:8080");
    }


    @Test
    public void test05()
    {
        extractHostFromAuthorityPartTest("example.com", "id@example.com");
    }


    @Test
    public void test06()
    {
        extractHostFromAuthorityPartTest("example.com", "id:@example.com");
    }


    @Test
    public void test07()
    {
        extractHostFromAuthorityPartTest("example.com", ":@example.com");
    }


    @Test
    public void test08()
    {
        extractHostFromAuthorityPartTest("example.com", ":password@example.com");
    }


    @Test
    public void test09()
    {
        extractHostFromAuthorityPartTest("example.com", "@example.com");
    }


    @Test
    public void test10()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com");
    }


    @Test
    public void test11()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com:8080");
    }


    @Test
    public void test12()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com");
    }


    @Test
    public void test13()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080");
    }


    @Test
    public void test14()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com/");
    }


    @Test
    public void test15()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com:8080/");
    }


    @Test
    public void test16()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com/");
    }


    @Test
    public void test17()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080/");
    }


    @Test
    public void test18()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com/path?key=@value");
    }


    @Test
    public void test19()
    {
        extractHostFromEntireUriTest("example.com", "ws://example.com:8080/path?key=@value");
    }


    @Test
    public void test20()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com/path?key=@value");
    }


    @Test
    public void test21()
    {
        extractHostFromEntireUriTest("example.com", "ws://id:password@example.com:8080/path?key=@value");
    }


    @Test
    public void test22()
    {
        extractHostTest("example.com", "ws://example.com");
    }


    @Test
    public void test23()
    {
        extractHostTest("example.com", "ws://example.com:8080");
    }


    @Test
    public void test24()
    {
        extractHostTest("example.com", "ws://id:password@example.com");
    }


    @Test
    public void test25()
    {
        extractHostTest("example.com", "ws://id:password@example.com:8080");
    }


    @Test
    public void test26()
    {
        extractHostTest("example.com", "ws://id:password@example.com:8080/path?key=@value");
    }
}
