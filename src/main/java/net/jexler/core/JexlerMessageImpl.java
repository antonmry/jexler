/*
   Copyright 2012 $(whois jexler.net)

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

package net.jexler.core;

import java.util.HashMap;


/**
 * Class for jexler message.
 *
 * @author $(whois jexler.net)
 */
public class JexlerMessageImpl extends HashMap<String,Object> implements JexlerMessage {
    private static final long serialVersionUID = 5540166367357174705L;

    @Override
    public JexlerMessage set(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("odd number of arguments");
        }
        for (int i = 0; i<keyValuePairs.length; i+=2) {
            if (!(keyValuePairs[i] instanceof String)) {
                throw new IllegalArgumentException("key " + i/2+1 + " is not a string");
            }
            put((String)keyValuePairs[i], keyValuePairs[i+1]);
        }
        return this;
    }

}
