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

package net.jexler.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.jexler.core.AbstractJexlerHandler;
import net.jexler.core.JexlerMessage;
import net.jexler.core.JexlerSubmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Command line handler.
 *
 * @author $(whois jexler.net)
 */
public class CommandLineHandler extends AbstractJexlerHandler implements Runnable {

    static final Logger log = LoggerFactory.getLogger(CommandLineHandler.class);

    /**
     * Constructor from id.
     * @param id id
     */
    public CommandLineHandler(String id) {
            super(id);
    }

    @Override
    public void startup(JexlerSubmitter submitter) {
        super.startup(submitter);
        Thread thread = new Thread(this);
        thread.setName(info);
        thread.start();
    }

    public void run() {
        Thread.currentThread().setName(info);
        // wait a little (just for testing in eclipse with slf4j log output)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        do {
            String cmd = readLine("> ");
            if (cmd == null) {
                log.error("End of stream");
                return;
            } else if (cmd.equals("shutdown")) {
                System.out.println("Shutting down jexler...");
                JexlerMessage message = new JexlerMessage(
                    "destination.class", JexlerSystemHandler.class,
                    "destination.id", "jexler",
                    "destination.method", JexlerSystemHandler.Method.SHUTDOWN,
                    "info", "jexler-shutdown");
                submitter.submit(message);
                return;
            } else {
                System.out.println("Unknown command '" + cmd + "'");
                System.out.println("Known commands: shutdown");
            }
        } while (true);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

}
