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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base jexler handler.
 *
 * @author $(whois jexler.net)
 */
public abstract class AbstractJexlerHandler implements JexlerHandler {

    static final Logger log = LoggerFactory.getLogger(AbstractJexlerHandler.class);

    protected final String id;
    protected final String description;
    protected JexlerSubmitter submitter;

    /**
     * Constructor from id and description.
     * @param id id
     * @param description description
     */
    public AbstractJexlerHandler(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public void start(JexlerSubmitter submitter) {
        this.submitter = submitter;
    }

    @Override
    public JexlerMessage handle(JexlerMessage message) {
        return message;
    }

    @Override
    public void stop() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
