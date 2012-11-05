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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerSubmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Script handler (handles messages by a script language).
 *
 * @author $(whois jexler.net)
 */
public class ScriptHandler extends AbstractJexlerHandler {

    static final Logger log = LoggerFactory.getLogger(ScriptHandler.class);

    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();

    private final File scriptFile;
    private final String scriptFileExtension;
    private final Map<String,Object> config;

    /**
     * Constructor.
     * @param id id
     * TODO
     */
    public ScriptHandler(String id, String description, String scriptFileName,
            Map<String,Object> config) {
        super(id, description);
        scriptFile = new File(scriptFileName);
        // LATER handle case where no extension is present
        String[] split = scriptFile.getName().split("\\.");
        scriptFileExtension = split[split.length-1];
        this.config = config;
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
        // TODO handle null? or maybe rather at startup?
        ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByExtension(scriptFileExtension);
        engine.put("handler", this);
        engine.put("log", log);
        engine.put("config", config);
        engine.put("method", method);
        engine.put("message", message);
        FileReader fileReader;
        try {
            fileReader = new FileReader(scriptFile);
        } catch (FileNotFoundException e) {
            log.error("file '" + scriptFile.getAbsolutePath() + "' not found");
            return false;
        }
        Object result;
        try {
            result = engine.eval(fileReader);
        } catch (ScriptException e) {
            log.error("script failed: " + e);
            return false;
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                log.warn("could not close file reader: " + e);
            }
        }
        return result;
    }

}