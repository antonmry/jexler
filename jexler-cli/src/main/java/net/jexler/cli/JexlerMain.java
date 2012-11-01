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

package net.jexler.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.jexler.core.Jexler;
import net.jexler.core.JexlerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for starting jexler from the command line.
 *
 * @author $(whois jexler.net)
 */
public final class JexlerMain
{
    static final Logger log = LoggerFactory.getLogger(JexlerMain.class);

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        System.out.println(JexlerMain.class.getPackage().getImplementationVersion());
        System.out.println("Welcome to jexler.");
        File configDir = new File("config");
        Jexler jexler = JexlerFactory.getJexler("main", "Main Jexler started from command line",
                configDir, new File(configDir, "create_handlers.rb"));
        jexler.startup();

        System.out.println("Press return to stop...");
        try {
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            stdIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jexler.shutdown();

        // wait a little (just for testing in eclipse with slf4j log output)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        System.out.println("Jexler done OK.");
        System.exit(0);
    }

}
