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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple message processor, gets submitted messages
 * and handles them in two separate threads for canHandle()
 * and handle().
 *
 * LATER A more sophisticated processor would maybe call handle()
 * in multiple threads from a pool?
 *
 * @author $(whois jexler.net)
 */
public class SimpleMessageProcessor implements JexlerSubmitter {

    static final Logger log = LoggerFactory.getLogger(SimpleMessageProcessor.class);

    private final Jexler jexler;
    private List<JexlerHandler> handlers;
    private BlockingQueue<JexlerMessage> handleQueue;
    private Thread handleThread;

    /**
     * Thread for handle() of messages.
     */
    private class HandleThread extends Thread {
        public void run() {
            setName(jexler.getId() + ":handle");
            log.info("handleThread running.");
            while (true) {
                try {
                    JexlerMessage message = handleQueue.take();
                    if (message.get("stopProcessing") != null) {
                        return;
                    }
                    for (JexlerHandler handler : handlers) {
                        log.info("HANDLE " + message.get("info")
                                + " => " + handler.getClass().getName() + ":" + handler.getId());
                        try {
                            message = handler.handle(message);
                        } catch (RuntimeException e) {
                            message = null;
                            // TODO log more info?
                            log.error("handle failed", e);
                        }
                        if (message == null) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("Could not take");
                }
            }
        }
    }

    /**
     * Constructor.
     */
    public SimpleMessageProcessor(Jexler jexler, List<JexlerHandler> handlers) {
        this.jexler = jexler;
        this.handlers = handlers;
        handleQueue = new LinkedBlockingQueue<JexlerMessage>();
    }

    /**
     * Start processing submitted messages.
     */
    public void startProcessing() {
        log.info("Starting processing...");
        handleThread = new HandleThread();
        handleThread.setDaemon(true);
        handleThread.start();
    }

    /**
     * Stop processing submitted messages.
     */
    public void stopProcessing() {
        log.info("Stopping processing...");
        JexlerMessage message = JexlerMessageFactory.create().set("stopProcessing", "true");
        submit(message);
        try {
            handleThread.join();
            log.info("handleThread joined.");
        } catch (InterruptedException e) {
            log.error("could not join", e);
        }

        log.info("Stopped processing.");
    }

    @Override
    public void submit(JexlerMessage message) {
        log.info("SUBMIT " + message.get("info"));
        handleQueue.add(message);
    }

}
