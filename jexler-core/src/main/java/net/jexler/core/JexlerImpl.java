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
    private SimpleMessageProcessor processor;
    private List<JexlerHandler> handlers;
    private boolean isRunning;

    /**
     * Constructor.
     * @param id
     * @param configScriptLanguage
     * @param configScriptFile
     */
    public JexlerImpl(String id, String description, String configScriptLanguage,
            File configScriptFile) {
        log.info("Creating jexler '" + id + "'...");
        this.id = id;
        this.description = description;

        // always create the following handlers (part of jexler)
        handlers = new LinkedList<JexlerHandler>();
        //handlers.add(new JexlerControllerHandler("jexler", "Handles jexler operation", this));
        //handlers.add(new CommandLineHandler("jexler", "Handles command line input"));

        // create handlers from config script(s)
        addHandlersFromScript(configScriptLanguage, configScriptFile);

        log.info("Handlers:");
        for (JexlerHandler handler : handlers) {
            log.info("*" + handler.getClass().getName() + ":" + handler.getId()
                    + " -- " + handler.getDescription());
        }

        processor = new SimpleMessageProcessor(this, handlers);
    }

    @Override
    public void startup() {
        log.info("Starting jexler '" + id + "'...");
        for (JexlerHandler handler : handlers) {
            handler.startup(processor);
        }

        isRunning = true;
        processor.startProcessing();

        log.info("Jexler '" + id + "' has started up.");
    }

    @Override
    public void waitForShutdown() {
        log.info("Waiting for shutdown of jexler '" + id  + "'...");
        synchronized (this) {
            while (isRunning) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    log.info("interrupted");
                }
            }
        }
        log.info("Jexler '" + id + "' has shut down.");
    }

    @Override
    public List<JexlerHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void shutdown() {
        log.info("Shutting down jexler '" + id + "'...");
        for (JexlerHandler handler : handlers) {
            handler.shutdown();
        }
        log.info("Jexler '" + id + "' has shutdown.");
        synchronized (this) {
            isRunning = false;
            this.notify();
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

    private void addHandlersFromScript(String scriptLanguage, File scriptFile) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(scriptLanguage);
        engine.put("handlers", handlers);
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
