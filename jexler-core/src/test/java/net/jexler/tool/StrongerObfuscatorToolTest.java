/*
   Copyright 2012-now $(whois jexler.net)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.jexler.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class StrongerObfuscatorToolTest
{
	
	@Test
    public void testDefaultInstance() throws Exception {
		StrongerObfuscatorTool tool = new StrongerObfuscatorTool();
		int byteBufferPadLen = 64;
		int blockSize = 16;
		int expectedObfuscatedLen = 2 * (byteBufferPadLen + blockSize);
        
        String plain = "test";
        String obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        //System.out.println(obfus.length());
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        String plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        String obfus2 = tool.obfuscate(plain);
        assertEquals("must be same", expectedObfuscatedLen, obfus2.length());
        assertNotSame("must not be same", obfus, obfus2);
    
        plain = "longer string with unicode \u03D4";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        obfus2 = tool.obfuscate(plain);
        assertEquals("must be same", expectedObfuscatedLen, obfus2.length());
        assertNotSame("must not be same", obfus, obfus2);
        
        plain = "";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        // maximal length
        plain = "12345678901234567890123456789012345678901234567";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        // too long in UTF-8
        plain = "12345678901234567890123456789012345678901234567\u1234";
        try {
        	tool.obfuscate(plain);
        	fail("must throw");
        } catch (IllegalArgumentException e) {
        	assertEquals("must be same", e.getMessage(),
        			"Input string too long (50 bytes UTF-8 encoded, max allowed: 47)");
        }
	}
	
	@Test
    public void testCustomInstance() throws Exception {
		StrongerObfuscatorTool tool = new StrongerObfuscatorTool();
		tool.setParameters("0011223344556677", "aabbccddeeff0011", "DES", "DES/CBC/PKCS5Padding");
		int byteBufferPadLen = 128;
		tool.setByteBufferPadLen(byteBufferPadLen);
		int blockSize = 8;
		int expectedObfuscatedLen = 2 * (byteBufferPadLen + blockSize);
        
        String plain = "test";
        String obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        //System.out.println(obfus.length());
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        String plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        String obfus2 = tool.obfuscate(plain);
        assertEquals("must be same", expectedObfuscatedLen, obfus2.length());
        assertNotSame("must not be same", obfus, obfus2);
    
        plain = "longer string with unicode \u03D4";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same",expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        obfus2 = tool.obfuscate(plain);
        assertEquals("must be same", expectedObfuscatedLen, obfus2.length());
        assertNotSame("must not be same", obfus, obfus2);
        
        plain = "";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        // maximal length
        plain = "12345678901234567890123456789012345678901234567890" +
        		"12345678901234567890123456789012345678901234567890" +
        		"12345678901";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", expectedObfuscatedLen, obfus.length());
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        // too long in UTF-8
        plain = "12345678901234567890123456789012345678901234567890" +
        		"12345678901234567890123456789012345678901234567890" +
        		"1234567890\u1234";
        try {
        	tool.obfuscate(plain);
        	fail("must throw");
        } catch (IllegalArgumentException e) {
        	assertEquals("must be same", e.getMessage(),
        			"Input string too long (113 bytes UTF-8 encoded, max allowed: 111)");
        }
	}

}
