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
import static org.junit.Assert.fail;

import javax.xml.bind.DatatypeConverter;

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
    public void testBasic() throws Exception {
        ObfuscatorTool tool = new ObfuscatorTool();
        
        String plain = "test";
        String obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "F5F2F40096B4155B27AD8B3B275C2864", obfus);
        String plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
    
        plain = "longer string with unicode \u03D4";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", 
        		"E0A8C1D1014069BA856A2FF0062F8F5D42025391EA8F302014D88E963E2A3492",
        		obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        plain = "";
        obfus = tool.obfuscate(plain);
        //System.out.println(obfus);
        assertEquals("must be same", "C955BFA82004AC1472234D84D11639C1", obfus);
        plainAgain = tool.deobfuscate(obfus);
        assertEquals("must be same", plain, plainAgain);
        
        try {
        	tool.obfuscate(null);
        	fail("must throw");
        } catch (NullPointerException e) {
        	// expected
        }
    }

	@Test
    public void testCustom() throws Exception {
        ObfuscatorTool tool = new ObfuscatorTool();
        
        tool.setKey("DES", DatatypeConverter.parseHexBinary("62e0c45a20dfe429"));
        tool.setIv(DatatypeConverter.parseHexBinary("b42de953243ab9ed"));
        tool.setCipher("DES/CBC/PKCS5Padding");
        
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
}
