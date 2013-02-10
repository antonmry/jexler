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

package net.jexler.sensor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerMessageFactory;
import net.jexler.core.JexlerSubmitter;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File tailer handler.
 *
 * @author $(whois jexler.net)
 */
public class FileTailerSensor extends AbstractJexlerHandler {

    static final Logger log = LoggerFactory.getLogger(FileTailerSensor.class);

    private static class Filter {
        private final boolean passesIfFound;
        private final Pattern pattern;
        private Filter(String patternString) {
            if (patternString.startsWith("!")) {
                patternString = patternString.substring(1);
                passesIfFound = false;
            } else {
                passesIfFound = true;
            }
            pattern = Pattern.compile(patternString);
        }
        private boolean passes(String line) {
            Matcher m = pattern.matcher(line);
            boolean found = m.find();
            return found && passesIfFound || !found && !passesIfFound;
        }
    }

    private class MyTailerListener extends TailerListenerAdapter {
        public void handle(String line) {
            for (Filter filter : filters) {
                if (!filter.passes(line)) {
                    return;
                }
            }
            // passed all filters
            log.info("passed: " + line);
            JexlerMessage message = JexlerMessageFactory.create().set(
                    "sender", FileTailerSensor.this,
                    "id", getId(),
                    "info", "filetailer-" + getId(),
                    "line", line);
            submitter.submit(message);
        }
        // LATER handle other tailer listener events?
    }

    private File file;
    private List<Filter> filters;
    private Tailer tailer;

    /**
     * Constructor.
     * @param id id
     * @param description description
     */
    public FileTailerSensor(String id, String description) {
        super(id, description);
        filters = new LinkedList<Filter>();
    }

    /**
     * Set file to tail by name.
     * @param fileName name of file to tail
     */
    public void setFile(String fileName) {
        this.file = new File(fileName);
    }

    /**
     * Add pattern to filter.
     * @param pattern regex pattern string, prefix with "!" to negate
     */
    public void addFilterPattern(String pattern) {
        filters.add(new Filter(pattern));
    }

    @Override
    public void start(JexlerSubmitter submitter) {
        super.start(submitter);
        TailerListener listener = new MyTailerListener();
        // LATER use configurable delay? use option to tail from end of file?
        tailer = Tailer.create(file, listener);
    }

    @Override
    public void stop() {
        tailer.stop();
    }

}
