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

package net.jexler.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerSubmitter;
import net.jexler.core.JexlerUtil;
import net.jexler.core.ScriptUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Script handler (handles messages by a script language).
 *
 * @author $(whois jexler.net)
 */
public class ScriptHandler extends AbstractJexlerHandler {

    static final Logger log = LoggerFactory.getLogger(ScriptHandler.class);

    private File scriptFile;
    private Map<String,Object> config;

    /**
     * Constructor.
     * @param id id
     * @param description description
     */
    public ScriptHandler(String id, String description) {
        super(id, description);
        config = new TreeMap<String,Object>();
    }

    /**
     * Set script file name.
     * @param scriptFileName
     */
    public void setScriptFileName(String scriptFileName) {
        scriptFile = new File(scriptFileName);
    }

    /**
     * Set key/value pairs in config for this handler.
     * Calls config.put(key,value) for each pair.
     * Example: set("id", id, "value", x.getValue())
     * @param keyValuePairs key/value pairs
     * @throws IllegalArgumentException if odd number of arguments or keys not strings
     */
    public void set(Object... keyValuePairs) {
        JexlerUtil.set(config, keyValuePairs);
    }

    @Override
    public void startup(JexlerSubmitter submitter) {
        super.startup(submitter);
        doScript("startup", null);
    }

    @Override
    public boolean canHandle(JexlerMessage message) {
        Object obj = doScript("canHandle", message);
        return (obj instanceof Boolean) ? (Boolean)obj : false;
    }

    @Override
    public boolean handle(JexlerMessage message) {
        Object obj = doScript("handle", message);
        return (obj instanceof Boolean) ? (Boolean)obj : false;
    }

    @Override
    public void shutdown() {
            doScript("shutdown", null);
    }

    private Object doScript(String method, Object message) {
        Map<String,Object> variables = new HashMap<String,Object>();
        variables.put("handler", this);
        variables.put("log", log);
        variables.put("config", config);
        variables.put("method", method);
        variables.put("message", message);
        return ScriptUtil.runScriptThreadSafe(variables, scriptFile);
    }

}
