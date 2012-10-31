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

import java.util.Map;



/**
 * Interface for jexler message.
 *
 * @author $(whois jexler.net)
 */
public interface JexlerMessage extends Map<String,Object> {

    /**
     * Put key/value pairs.
     * Example: put("id", id, "value", x.getValue())
     * @param keyValuePairs key/value pairs
     * @return this
     * @throws IllegalArgumentException if odd number of arguments or keys not strings
     */
    JexlerMessage put(Object... keyValuePairs);

}
