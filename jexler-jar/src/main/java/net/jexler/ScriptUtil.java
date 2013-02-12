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

package net.jexler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Jexler scripting utilities.
 *
 * @author $(whois jexler.net)
 */
public class ScriptUtil {

    static final Logger log = LoggerFactory.getLogger(ScriptUtil.class);

    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();
    private static final String THREADING_PARAMETER = "THREADING";

    /**
     * Init scripting.
     */
    public static void initScripting() {
        // LATER similar settings for other common scripting languages?
        // NOTE for ruby the test by threading parameter seems not to work, always
        //      returned THREAD_ISOLATED even if was experimentally not so...
        System.setProperty("org.jruby.embed.localcontext.scope", "threadsafe");

        // trace all script engines
        if (log.isTraceEnabled()) {
            log.trace("Available script engines:");
            List<ScriptEngineFactory> factories = new ScriptEngineManager().getEngineFactories();
            for (ScriptEngineFactory factory : factories) {
                log.trace("EngineName      = " + factory.getEngineName());
                log.trace("EngineVersion   = " + factory.getEngineVersion());
                log.trace("LanguageName    = " + factory.getLanguageName());
                log.trace("LanguageVersion = " + factory.getLanguageVersion());
                log.trace("Extensions      = " + factory.getExtensions());
                log.trace("Threading       = " + factory.getParameter(THREADING_PARAMETER));
                StringBuilder builder = new StringBuilder("Engine Aliases  = ");
                List<String> names = factory.getNames();
                for (String name : names) {
                    builder.append(name);
                    builder.append(" ");
                }
                log.trace(builder.toString());
            }
        }
    }

    /**
     * Run given script with given variables, thread safe.
     * Serializes calls if the script engine is not multi-threaded.
     *
     * @param variables variables to set in engine and to read again after script returned
     * @param scriptFile script file, must have a corresponding file extension
     * @return script's return value, false if script evaluation failed for some reason
     */
    public static Object runScriptThreadSafe(Map<String,Object> variables, File scriptFile){
        String[] split = scriptFile.getName().split("\\.");
        if (split.length < 2) {
            log.error("Script file '{}' has no extension", scriptFile.getAbsolutePath());
        }
        String scriptFileExtension = split[split.length-1];

        ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByExtension(scriptFileExtension);
        if (engine == null) {
            log.error("No script engine for extension '{}'", scriptFileExtension);
            return false;
        }

        // LATER ensures at least "MULTITHREADED", sufficient?
        // (script languages may define their own return values, not sure if they do)
        if (engine.getFactory().getParameter(THREADING_PARAMETER) == null) {
            // have to synchronize
            synchronized(engine) {
                log.trace("Running script serialized");
                return runScript(engine, variables, scriptFile);
            }
        } else {
            log.trace("Running script parallel");
            return runScript(engine, variables, scriptFile);
        }
    }

    private static Object runScript(ScriptEngine engine,
            Map<String,Object> variables, File scriptFile) {
        for (String key : variables.keySet()) {
            engine.put(key, variables.get(key));
        }

        Object result = false;

        try (FileReader fileReader = new FileReader(scriptFile)) {
            try {
                result = engine.eval(fileReader);
            } catch (ScriptException e) {
                log.error("script failed: " + e);
                return false;
            }
        } catch (IOException e) {
            log.error("could not read from file '" + scriptFile.getAbsolutePath() + "': ", e);
            return false;
        }

        for (String key : variables.keySet()) {
            variables.put(key, engine.get(key));
        }

        return result;

    }

}
