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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for (de-)obfuscating strings.
 * 
 * By default uses AES 128 in CBC mode with a hard-coded key (and iv).
 *
 * @author $(whois jexler.net)
 */
public class ObfuscatorTool {

    static final Logger log = LoggerFactory.getLogger(ObfuscatorTool.class);
    
    private SecretKeySpec key;
    private IvParameterSpec iv;
    private Cipher cipher;
    
    /**
     * Constructor, uses hard-coded default key and iv
     * and AES 128 in CBC mode with PKCS#5 padding.
     */
    public ObfuscatorTool() throws NoSuchAlgorithmException, NoSuchPaddingException {
    	setKey("AES", DatatypeConverter.parseHexBinary("62e0c45a20dfe429543212be640c3254"));
    	setIv(DatatypeConverter.parseHexBinary("b42de953243ab9edf03bdac61344bec5"));
    	setCipher("AES/CBC/PKCS5Padding");
    }
    
    /**
     * Set custom key.
     */
    public ObfuscatorTool setKey(String algorithm, byte[] keyBytes) {
    	key = new SecretKeySpec(keyBytes, algorithm);
    	return this;
    }
    
    /**
     * Set custom iv.
     */
    public ObfuscatorTool setIv(byte[] ivBytes) {
    	iv = new IvParameterSpec(ivBytes);
    	return this;
    }
    
    /**
     * Set custom cipher
     */
    public ObfuscatorTool setCipher(String transformation)
    		throws NoSuchAlgorithmException, NoSuchPaddingException {
    	cipher = Cipher.getInstance(transformation);
    	return this;
    }
    
    /**
     * UTF-8 encode, encipher and hex encode given string.
     */
    public String obfuscate(String plain) 
    		throws InvalidKeyException, IllegalBlockSizeException,
    		BadPaddingException, UnsupportedEncodingException,
    		InvalidAlgorithmParameterException {
    	cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    	byte[] enc = cipher.doFinal(plain.getBytes("UTF-8"));
    	String encHex = DatatypeConverter.printHexBinary(enc);
    	return encHex;
    }

    /**
     * Hex decode, decipher and UTF-8 decode given string.
     */
    public String deobfuscate(String encHex)
    		throws InvalidKeyException, IllegalBlockSizeException,
    		BadPaddingException, UnsupportedEncodingException,
    		InvalidAlgorithmParameterException {
    	byte[] enc = DatatypeConverter.parseHexBinary(encHex);
    	cipher.init(Cipher.DECRYPT_MODE, key, iv);
    	byte[] plain = cipher.doFinal(enc);  
    	return new String(plain, "UTF-8");
    }

}
