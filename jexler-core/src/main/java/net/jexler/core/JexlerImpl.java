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

    private static final String JEXLER_INIT_FILE_PREFIX = "jexler.init.";

    private final String id;
    private String description;
    private File jexlerDir;
    private File jexlerInitFile;
    private String jexlerInitFileExtension;
    private SimpleMessageProcessor processor;
    private List<JexlerHandler> handlers;
    private boolean isRunning;

    /**
     * Constructor.
     * The id is set to the name of the jexler directory,
     * the description is set in the jexler.init.* script
     * at startup, here only a default is set.
     * @param jexlerDir
     */
    public JexlerImpl(File jexlerDir) {
        if (!isJexlerDir(jexlerDir)) {
            // TODO handle better
            throw new RuntimeException("File '" + jexlerDir.getAbsolutePath() +
                    "' is not a jexler directory");
        }

        this.jexlerDir = jexlerDir;
        this.id = jexlerDir.getName();
        log.info("Creating jexler '" + id + "'...");
        description = "jexler '" + id + "'";
        String[] filenames = jexlerDir.list();
        for (String filename : filenames) {
            if (filename.startsWith(JEXLER_INIT_FILE_PREFIX)) {
                jexlerInitFile = new File(jexlerDir, filename);
                jexlerInitFileExtension = filename.substring(JEXLER_INIT_FILE_PREFIX.length());
                break;
            }
        }
        handlers = new LinkedList<JexlerHandler>();
        processor = new SimpleMessageProcessor(this, handlers);
    }

    public static boolean isJexlerDir(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        String[] filenames = dir.list();
        int nFound = 0;
        for (String filename : filenames) {
            if (filename.startsWith(JEXLER_INIT_FILE_PREFIX)) {
                nFound++;
            }
        }
        return (nFound == 1);
    }

    @Override
    public void startup() {
        if (isRunning()) {
            return;
        }
        log.info("Starting jexler '" + id + "'...");

        runJexlerInitScript();

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

    private void runJexlerInitScript() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByExtension(jexlerInitFileExtension);
        engine.put("jexlerDir", jexlerDir.getAbsolutePath());
        engine.put("handlers", handlers);
        engine.put("description", description);
        FileReader fileReader;
        try {
            fileReader = new FileReader(jexlerInitFile);
        } catch (FileNotFoundException e) {
            log.error("file '" + jexlerInitFile.getAbsolutePath() + "' not found");
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
        // need to read back since strings are immutable
        description = (String)engine.get("description");
    }

}
