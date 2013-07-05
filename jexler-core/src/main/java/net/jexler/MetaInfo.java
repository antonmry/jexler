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

package net.jexler;

import java.util.Map;

/**
 * Interface for meta info of a jexler,
 * stored in the first line of a jexler file as a list.
 * 
 * For example:
 * 
 * [ "autostart"=true, "custom"=new File('/').usableSpace ]
 * 
 * The first line must start with '[', except whitespace,
 * else meta info is considered empty, as well as if for
 * some reason evaluating the line fails or is not possible.
 *
 * @author $(whois jexler.net)
 */
public interface MetaInfo extends Map<String,Object> {

    /**
     * Get boolean flag value from meta info.
     * @param name flag name
     * @param defaultValue default value to use
     * @return value from meta info if indicated, otherwise default value
     */
    public boolean isOn(String name, boolean defaultValue);

}

