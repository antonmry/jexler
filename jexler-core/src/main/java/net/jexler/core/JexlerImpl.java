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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    public void start() {
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

        log.info("Jexler '" + id + "' has started.");
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
    public void stop() {
        if (!isRunning()) {
            return;
        }
        log.info("Stopping jexler '" + id + "'...");

        processor.stopProcessing();
        for (JexlerHandler handler : handlers) {
            handler.shutdown();
        }
        handlers.clear();

        log.info("Jexler '" + id + "' has stopped.");
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

    @Override
    public File getDir() {
        return jexlerDir;
    }

    private void runJexlerInitScript() {
        Map<String,Object> variables = new HashMap<>();
        variables.put("jexlerDir", jexlerDir.getAbsolutePath());
        variables.put("handlers", handlers);
        variables.put("description", description);
        Object obj = ScriptUtil.runScriptThreadSafe(variables, jexlerInitFile);
        if (obj != null && obj instanceof Boolean && !(Boolean)obj) {
            // TODO handle better
            log.error("Initialization script failed.");
        }
        description = (String)variables.get("description");
    }

}
