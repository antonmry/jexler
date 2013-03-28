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

package net.jexler.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jexler.AbstractEvent;
import net.jexler.AbstractService;
import net.jexler.Jexler;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File tailer service.
 *
 * @author $(whois jexler.net)
 */
public class FileTailerService extends AbstractService<FileTailerService> {

    public class Event extends AbstractEvent {
        private String line;
        public Event(FileTailerService service, String line) {
            super(service);
            this.line = line;
        }
        public String getLine() {
            return line;
        }
    }

    static final Logger log = LoggerFactory.getLogger(FileTailerService.class);

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
            Thread.currentThread().setName(getJexler().getId() + "|" + getId());
            log.trace("passed: " + line);
            getJexler().handle(new Event(thisService, line));
        }
    }

    private FileTailerService thisService;
    private File file;
    private List<Filter> filters;
    private Tailer tailer;

    /**
     * Constructor.
     */
    public FileTailerService(Jexler jexler, String id) {
        super(jexler, id);
        thisService = this;
        filters = new LinkedList<Filter>();
    }

    /**
     * Set file to tail by name.
     * @param fileName name of file to tail
     */
    public FileTailerService setFile(String fileName) {
        this.file = new File(fileName);
        return this;
    }

    /**
     * Add pattern to filter.
     * @param pattern regex pattern string, prefix with "!" to negate
     */
    public FileTailerService addFilterPattern(String pattern) {
        filters.add(new Filter(pattern));
        return this;
    }

    @Override
    public FileTailerService start() {
        if (isRunning()) {
            return this;
        }
        TailerListener listener = new MyTailerListener();
        tailer = Tailer.create(file, listener);
        setRunning(true);
        return this;
    }

    @Override
    public void stop(long timeout) {
        if (!isRunning()) {
            return;
        }
        tailer.stop();
        setRunning(false);
    }

}
