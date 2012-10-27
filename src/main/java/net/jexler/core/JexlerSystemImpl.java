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

import net.jexler.handler.CommandLineHandler;
import net.jexler.handler.JexlerSystemHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The jexler system.
 * 
 * @author $(whois jexler.net)
 */
public class JexlerSystemImpl implements JexlerSystem {

    static final Logger log = LoggerFactory.getLogger(JexlerSystemImpl.class);

    private MessageHandler messageHandler;
    private List<JexlerHandler> handlers;
    private boolean isRunning;
    
    /**
     * Constructor.
     */
    public JexlerSystemImpl() {
    }
    
    @Override
    public void startup() {
        log.info("Startup...");
        
        // always create the following handlers (part of jexler)
        handlers = new LinkedList<JexlerHandler>();
        handlers.add(new JexlerSystemHandler("jexler", this));
        handlers.add(new CommandLineHandler("jexler"));
        
        // create handlers from config script(s)
        // TODO current dir now for inside eclipse (not maven)
        // TODO inject how? properties file?
        addHandlersFromScript("ruby", new File("scripts/config.rb"));
        
        log.info("Handlers:");
        for (JexlerHandler handler : handlers) {
            log.info("*" + handler.getInfo());
        }
        
        messageHandler = new MessageHandler(handlers);
        for (JexlerHandler handler : handlers) {
            handler.startup(messageHandler);
        }
                
        isRunning = true;
        messageHandler.startHandling();

        log.info("Started up.");
    }
        
    @Override
    public void shutdown() {
        log.info("Shutting down...");
        for (JexlerHandler handler : handlers) {
            handler.shutdown();
        }
        log.info("Shutdown.");
        synchronized (this) {
            isRunning = false;
            this.notify();
        }
    }
    
    @Override
    public void waitForShutdown() {
        log.info("Waiting for shutdown...");
        synchronized (this) {
            while (isRunning) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    log.info("interrupted");
                }
            }
        }
        log.info("Has shut down.");
    }
    
    private void addHandlersFromScript(String scriptLanguage, File scriptFile) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("ruby");
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
