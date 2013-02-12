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

package net.jexler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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

    private static Jexlers suite;

    /**
     * Main method for starting jexler from the command line.
     *
     * @param args command line arguments
     * @throws IOException if an I/O error occurs while trying to read from stdin
     */
    public static void main(final String[] args) throws IOException {
        boolean silent = args.length == 2 && args[0].equals("-s");

        if (!silent) {
            String version = JexlerCli.class.getPackage().getImplementationVersion();
            // no version in eclipse/unit tests (no jar with MANIFEST.MF)
            System.out.println("Welcome to jexler. Version: " + (version == null ? "NONE" : version));
            System.out.println();
        }

        if (args.length == 1 && args[0].equals("-v")) {
            // already done...
            return;
        }

        if (!silent && args.length != 1) {
            System.err.println("Usage:");
            System.err.println("  -v        Show version and exit.");
            System.err.println("  <dir>     Start jexler suite using given suite directory");
            System.err.println("            and wait for commands.");
            System.err.println("  -s <dir>  Start in suite silent mode, use ctrl-c to stop.");
            System.exit(1);
        }

        suite = new Jexlers(new File(args[args.length-1]));
        suite.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                setName("main-shutdown");
                suite.stop();
            }
        });

        if (silent) {
            do {
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    // ignore
                }
            } while (true);
        }

        // interactive mode
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        do {
            System.out.print("jexler> ");
            String cmd = reader.readLine().trim();

            String jexlerId = null;
            if (cmd.contains(" ")) {
                int i = cmd.indexOf(' ');
                jexlerId = cmd.substring(i+1).trim();
                cmd = cmd.substring(0, i).trim();
            }
            Jexler jexler = null;
            if (jexlerId != null) {
                jexler = suite.getJexler(jexlerId);
                if (jexler == null) {
                    System.out.println("no jexler with id '" + jexlerId + "'");
                    doInfo(jexler);
                    continue;
                }
            }

            if (cmd.equals("exit")) {
                break;
            } else if (cmd.equals("info")) {
                doInfo(jexler);
            } else if (cmd.equals("start")) {
                doStart(jexler);
                doInfo(null);
            } else if (cmd.equals("stop")) {
                doStop(jexler);
                doInfo(null);
            } else if (cmd.equals("restart")) {
                doRestart(jexler);
                doInfo(null);
            } else {
                System.out.println();
                System.out.println("Commands:");
                System.out.println("> info [id]      Show info about suite or given jexler.");
                System.out.println("> start [id]     Start all jexlers in suite or given jexler.");
                System.out.println("> stop [id]      Stop all jexlers in suite or given jexler.");
                System.out.println("> restart [id]   Restart all jexlers in suite or given jexler.");
                System.out.println("> exit           Stop all jexlers and exit.");
                System.out.println();
            }
        } while (true);
        suite.stop();
    }

    public static void doInfo(Jexler jexler) {
        if (jexler == null) {
            List<Jexler> jexlers = suite.getJexlers();
            for (Jexler jex : jexlers) {
                printJexlerLine(jex, getJexlerMaxIdLen());
            }
        } else {
            printJexlerLine(jexler, jexler.getName().length());
        }
    }

    public static void doStart(Jexler jexler) {
        if (jexler == null) {
            suite.start();
        } else {
            jexler.start();
        }
    }

    public static void doStop(Jexler jexler) {
        if (jexler == null) {
            suite.stop();
        } else {
            jexler.stop();
        }
    }

    public static void doRestart(Jexler jexler) {
        doStop(jexler);
        doStart(jexler);
    }

    private static int getJexlerMaxIdLen() {
        List<Jexler> jexlers = suite.getJexlers();
        int max = 0;
        for (Jexler jexler : jexlers) {
            int len = jexler.getName().length();
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    private static void printJexlerLine(Jexler jexler, int maxIdLen) {
        System.out.printf("* %-" + maxIdLen + "s : %-7s%n",
                jexler.getName(),
                jexler.isRunning() ? "running" : "stopped");
    }

}
