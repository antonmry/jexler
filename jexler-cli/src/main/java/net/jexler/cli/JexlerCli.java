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

import java.io.File;
import java.io.IOException;

import net.jexler.core.JexlerSuite;
import net.jexler.core.JexlerSuiteFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for starting jexler from the command line.
 *
 * @author $(whois jexler.net)
 */
public final class JexlerCli
{
    static final Logger log = LoggerFactory.getLogger(JexlerCli.class);

    /**
     * Main method for starting jexler from the command line.
     *
     * @param args command line arguments, must be a single argument:
     *             <ul>
     *             <li> "-v": Show version and exit.</li>
     *             <li> &lt;dir>: Startup jexler suite using given suite directory,
     *                          wait for any key stroke, shutdown jexler and exit.</li>
     *             </ul>
     * @throws IOException if an I/O error occurs while trying to read from stdin
     */
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage:");
            System.err.println("  -v      Show version and exit.");
            System.err.println("  <dir>   Startup jexler suite using given suite directory,");
            System.err.println("          wait for any key stroke, shutdown jexler and exit.");
            System.exit(1);
        }

        if (args[0].equals("-v")) {
            String version = JexlerCli.class.getPackage().getImplementationVersion();
            System.out.println("jexler " + (version == null ? "(unknown)" : version));
            System.exit(0);
        }
        final JexlerSuite suite = JexlerSuiteFactory.getSuite(new File(args[0]));
        suite.startup();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                setName("main-shutdown");
                suite.shutdown();
            }
        });
        System.in.read();
        suite.shutdown();
    }

}
