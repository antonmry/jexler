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
import net.jexler.test.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class ObfuscatorToolTest
{

	@Test
	@SuppressWarnings("deprecation")
    public void testDefaultKey() throws Exception {
        ObfuscatorTool tool = new ObfuscatorTool();
        
        String plain = "test";
        String obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "2778C3B6FAAACE3E", obfus);
        String plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
    
        plain = "longer string with unicode \u03D4";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", 
        		"EA0BF965A907A17766861F88E9C7647E03273F49BAE59317F4F9731778B3E4C2",
        		obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        plain = "";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "F1559F4F8783C61F", obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
	}

	@Test
	@SuppressWarnings("deprecation")
    public void testCustomKey() throws Exception {
        ObfuscatorTool tool = new ObfuscatorTool("1e02ab32dc0482e0","de03a21b6428bf04");
        
        String plain = "test";
        String obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "8CC6272DFA076961", obfus);
        String plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
    
        plain = "longer string with unicode \u03D4";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", 
        		"CEF9439DC881ADF3C22597CC909D026FFC908D5F81D3ECC10D3AD7E6CBE09987",
        		obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        plain = "";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "5C6C249B35974D05", obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
	}
}
