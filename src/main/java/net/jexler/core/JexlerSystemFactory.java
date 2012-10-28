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

import java.io.File;



/**
 * Jexler system factory.
 *
 * @author $(whois jexler.net)
 */
public class JexlerSystemFactory {

    /**
     * Get system for given config script.
     * @param configScriptLanguage
     * @param configScriptFile
     * @return system
     */
    public static JexlerSystem getSystem(String configScriptLanguage, File configScriptFile) {
        return new JexlerSystemImpl(configScriptLanguage, configScriptFile);
    }

}
