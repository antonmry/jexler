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

    /**
     * Context for processing message.
     */
    private static final class Context {
        private final JexlerMessage message;
        private int pos = 0;
        private boolean canHandle = false;
        private boolean stopProcessing = false;
        private Context(JexlerMessage message) {
            this.message = message;
        }
    }

    private final Jexler jexler;
    private List<JexlerHandler> handlers;
    private BlockingQueue<Context> canHandleQueue;
    private BlockingQueue<Context> handleQueue;
    private Thread canHandleThread;
    private Thread handleThread;

    /**
     * Thread for canHandle() of messages.
     */
    private class CanHandleThread extends Thread {
        public void run() {
            setName(jexler.getId() + ":canHandle");
            log.info("canHandleThread running.");
            while (true) {
                try {
                    Context context = canHandleQueue.take();
                    if (context.stopProcessing) {
                        return;
                    }
                    JexlerMessage message = context.message;
                    for ( ; context.pos < handlers.size(); context.pos++) {
                        JexlerHandler handler = handlers.get(context.pos);
                        if (handler.canHandle(message)) {
                            context.canHandle = true;
                            log.info("CAN HANDLE " + message.get("info")
                                    + " => " + handler.getClass().getName() + ":" + handler.getId());
                            handleQueue.add(context);
                            break;
                        }
                    }
                    if (!context.canHandle) {
                        log.warn("CANNOT HANDLE " + message.get("info"));
                        // TODO create message? (one that is always handled by system?)
                    }
                } catch (InterruptedException e) {
                    log.error("Could not take");
                }
            }
        }
    }

    /**
     * Thread for handle() of messages.
     */
    private class HandleThread extends Thread {
        public void run() {
            setName(jexler.getId() + ":handle");
            log.info("handleThread running.");
            while (true) {
                try {
                    Context context = handleQueue.take();
                    if (context.stopProcessing) {
                        return;
                    }
                    JexlerMessage message = context.message;
                    JexlerHandler handler = handlers.get(context.pos);
                    log.info("HANDLE " + message.get("info")
                            + " => " + handler.getClass().getName() + ":" + handler.getId());
                    //boolean passOn = handler.handle(message);
                    boolean passOn = false;
                    handler.handle(message);
                    if (passOn) {
                        context.pos++;
                        canHandleQueue.add(context);
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
        canHandleQueue = new LinkedBlockingQueue<Context>();
        handleQueue = new LinkedBlockingQueue<Context>();
    }

    /**
     * Start processing submitted messages.
     */
    public void startProcessing() {
        log.info("Starting processing...");
        canHandleThread = new CanHandleThread();
        handleThread = new HandleThread();
        canHandleThread.start();
        handleThread.start();
    }

    /**
     * Stop processing submitted messages.
     */
    public void stopProcessing() {
        log.info("Stopping processing...");
        Context context = new Context(JexlerMessageFactory.create());
        context.stopProcessing = true;
        canHandleQueue.add(context);
        handleQueue.add(context);
        try {
            canHandleThread.join();
            log.info("canHandleThread joined.");
        } catch (InterruptedException e) {
            log.error("could not join", e);
        }
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
        canHandleQueue.add(new Context(message));
    }

}
