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

package net.jexler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shell actor.
 *
 * @author $(whois jexler.net)
 */
public class ShellUtil {

    public class Result {
        public int rc;
        public String stdout;
        public String stderr;
    }

    static final Logger log = LoggerFactory.getLogger(ShellUtil.class);

    private File workingDirectory;
    private String[] environment;

    /**
     * Constructor.
     */
    public ShellUtil() {
    }

    /**
     * Set working directory for command.
     * If not set inherit from parent process.
     * @param workingDirectory
     */
    public ShellUtil setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * Set environment variables for command.
     * Each item has the form "<name>=<value>".
     * If not set inherit from parent process.
     * @param environment
     */
    public ShellUtil setEnvironment(String[] environment) {
        // LATER use map, allow to add to system environment
        this.environment = environment;
        return this;
    }

    /**
     * Run shell command and return result.
     * @param command command to run
     * @return result
     */
    public Result run(String command) {
        Result result = new Result();
        try {
            Process proc = Runtime.getRuntime().exec(command, environment, workingDirectory);
            result.rc = proc.waitFor();
            result.stdout = readInputStream(proc.getInputStream());
            result.stderr = readInputStream(proc.getErrorStream());
        } catch (IOException | InterruptedException | RuntimeException e ) {
            result.rc = -1;
            result.stdout = "";
            result.stderr = getStackTrace(e);
        }
        return result;
    }

    private String readInputStream(InputStream is) {
        try (Scanner s = new Scanner(is, "UTF-8")) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    private String getStackTrace(Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();
    }

}
