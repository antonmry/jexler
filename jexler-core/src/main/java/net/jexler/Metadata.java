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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Jexler metadata.
 * 
 * Read from the jexler file at each call, i.e. if the jexler
 * is running and the jexler file has been modified or deleted
 * afterwards, you should use cached values instead to get
 * metadata for the running jexler.
 *
 * @author $(whois jexler.net)
 */
public class Metadata {

    private final Jexler jexler;

    public Metadata(Jexler jexler) {
        this.jexler = jexler;
    }
    
    /**
     * Get map of metadata.
     * 
     * @return map, never null, empty if could not read
     */
    public Map<String,Object> getMap() {
        Map<String,Object> empty = new HashMap<>();
        
        File file = jexler.getFile();
        if (!file.exists()) {
        	return empty;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        	// read first line of jexler script
        	String line = reader.readLine();
        	if (line == null) {
        		return empty;
        	}

        	// evaluate first line as groovy script
        	Binding binding = new Binding();
        	GroovyShell shell = new GroovyShell(binding);
        	Object o;
        	try {
        		o = shell.evaluate(line);
        	} catch (CompilationFailedException e) {
        		return empty;
        	}

        	// evaluated to a map?
        	if (o == null || !(o instanceof Map)) {
        		return empty;
        	}

        	// return map
        	@SuppressWarnings("unchecked")
        	Map<String,Object> map = (Map<String,Object>)o;
        	return map;

        } catch (IOException e) {
        	String msg = "Could not read file '" + file.getAbsolutePath() + "'.";
        	jexler.trackIssue(new Issue(null, msg, e));
        	return empty;
        }
    }
    
    /**
     * Get boolean flag value from metadata.
     * 
     * @param name flag name
     * @param defaultValue default value to use if not indicated or could not read
     * @return value if indicated and could read, otherwise default value
     */
    public boolean isOn(String name, boolean defaultValue) {
    	Map<String,Object> map = getMap();
    	Object o = map.get(name);
    	if (o != null && o instanceof Boolean) {
    		return (Boolean)o;
    	} else {
    		return defaultValue;
    	}
    }

}

