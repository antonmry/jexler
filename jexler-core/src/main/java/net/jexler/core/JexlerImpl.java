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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The jexler implementation.
 *
 * @author $(whois jexler.net)
 */
public class JexlerImpl implements Jexler {

    static final Logger log = LoggerFactory.getLogger(JexlerImpl.class);

    private final String id;
    private final String description;
    private final File configDir;
    private final File scriptFile;
    private final String scriptFileExtension;
    private SimpleMessageProcessor processor;
    private List<JexlerHandler> handlers;
    private boolean isRunning;

    /**
     * Constructor.
     * @param id
     * @param configDir
     * @param scriptFile constructs handlers
     */
    public JexlerImpl(String id, String description, File configDir, File scriptFile) {
        log.info("Creating jexler '" + id + "'...");
        this.id = id;
        this.description = description;
        this.configDir = configDir;
        this.scriptFile = scriptFile;
        // LATER handle case where no extension is present
        String[] split = scriptFile.getName().split("\\.");
        scriptFileExtension = split[split.length-1];
        handlers = new LinkedList<JexlerHandler>();
        processor = new SimpleMessageProcessor(this, handlers);
    }

    @Override
    public void startup() {
        if (isRunning()) {
            return;
        }
        log.info("Starting jexler '" + id + "'...");

        // create handlers from config script
        addHandlersFromScript();

        log.info("Handlers:");
        for (JexlerHandler handler : handlers) {
            log.info("*" + handler.getClass().getName() + ":" + handler.getId()
                    + " -- " + handler.getDescription());
        }

        for (JexlerHandler handler : handlers) {
            handler.startup(processor);
        }

        isRunning = true;
        processor.startProcessing();

        log.info("Jexler '" + id + "' has started up.");
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public List<JexlerHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void shutdown() {
        if (!isRunning()) {
            return;
        }
        log.info("Shutting down jexler '" + id + "'...");

        processor.stopProcessing();
        for (JexlerHandler handler : handlers) {
            handler.shutdown();
        }
        handlers.clear();

        log.info("Jexler '" + id + "' has shutdown.");
        synchronized (this) {
            isRunning = false;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    private void addHandlersFromScript() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByExtension(scriptFileExtension);
        engine.put("handlers", handlers);
        engine.put("configDir", configDir.getAbsolutePath());
        FileReader fileReader;
        try {
            fileReader = new FileReader(scriptFile);
        } catch (FileNotFoundException e) {
            log.error("file '" + scriptFile.getAbsolutePath() + "' not found");
            // TODO
            throw new RuntimeException(e);
        }
        // TODO look at returned object?
        try {
            engine.eval(fileReader);
        } catch (ScriptException e) {
            log.error("script failed: " + e);
            // TODO
            throw new RuntimeException(e);
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                log.warn("could not close file reader: " + e);
            }
        }
    }

}
