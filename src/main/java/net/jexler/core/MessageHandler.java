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
 * Jexler message handler, gets submitted messages
 * and handles them in separate threads.
 *
 * @author $(whois jexler.net)
 */
public class MessageHandler implements JexlerSubmitter {

    static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    /**
     * Context for processing message.
     */
    private static final class Context {
        private final JexlerMessage message;
        private int pos = 0;
        private boolean canHandle = false;
        private Context(JexlerMessage message) {
            this.message = message;
        }
    }

    private List<JexlerHandler> handlers;
    private BlockingQueue<Context> canHandleQueue;
    private BlockingQueue<Context> handleQueue;

    /**
     * Thread for canHandle() of messages.
     */
    private class CanHandleThread extends Thread {
            public void run() {
                setName("jexler-can-handle");
                while (true) {
                    try {
                        Context context = canHandleQueue.take();
                        JexlerMessage message = context.message;
                        for ( ; context.pos < handlers.size(); context.pos++) {
                            JexlerHandler handler = handlers.get(context.pos);
                            if (handler.canHandle(message)) {
                                context.canHandle = true;
                                log.info("CAN HANDLE " + message.get("info")
                                        + " => " + handler.getInfo());
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
                setName("jexler-handle");
                while (true) {
                    try {
                        Context context = handleQueue.take();
                        JexlerMessage message = context.message;
                        JexlerHandler handler = handlers.get(context.pos);
                        log.info("HANDLE " + message.get("info")
                                + " => " + handler.getInfo());
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
    public MessageHandler(List<JexlerHandler> handlers) {
        this.handlers = handlers;
        canHandleQueue = new LinkedBlockingQueue<Context>();
        handleQueue = new LinkedBlockingQueue<Context>();
    }

    /**
     * Start handling submitted messages.
     */
    public void startHandling() {
        new CanHandleThread().start();
        new HandleThread().start();
    }

    @Override
    public void submit(JexlerMessage message) {
        log.info("SUBMIT " + message.get("info"));
        canHandleQueue.add(new Context(message));
    }

}
