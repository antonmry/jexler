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

package net.jexler.impl;

import java.io.File;

import net.jexler.Jexler;
import net.jexler.Jexlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jexler factory.
 *
 * @author $(whois jexler.net)
 */
public class JexlerFactory  {

    static final Logger log = LoggerFactory.getLogger(JexlerFactory.class);
    
    static private boolean giveMock = false;
    
    static Jexler newJexler(File file, Jexlers jexlers) {
    	if (giveMock) {
    		return new MockJexler(file, jexlers);
    	} else {
    		return new JexlerImpl(file, jexlers);
    	}
    }
    
    static void setGiveMock(boolean giveMock) {
        JexlerFactory.giveMock = giveMock;
    }

}
