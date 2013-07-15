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

package net.jexler.tool;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import net.jexler.JexlerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for running shell commands.
 *
 * @author $(whois jexler.net)
 */
public class ShellTool {

	/**
	 * Simple bean for the result of executing a shell command.
	 */
    public class Result {
        public int rc;
        public String stdout;
        public String stderr;
    }

    static final Logger log = LoggerFactory.getLogger(ShellTool.class);

    private File workingDirectory;
    private String[] environment;

    /**
     * Constructor.
     */
    public ShellTool() {
    }

    /**
     * Set working directory for the command.
     * If not set inherit from parent process.
     * @return this (for chaining calls)
     */
    public ShellTool setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * Set environment variables for the command.
     * Each item has the form "name=value".
     * If not set inherit from parent process.
     * @return this (for chaining calls)
     */
    public ShellTool setEnvironment(String[] environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Run the given shell command and return the result.
     * If an exception occurs, the return code of the result is set to -1,
     * stderr of the result is set to the stack trace of the exception and
     * stdout of the result is set to an empty string.
     * @param command command to run
     * @return result, never null
     */
    public Result run(String command) {
        Result result = new Result();
        try {
            Process proc = Runtime.getRuntime().exec(command, environment, workingDirectory);
            result.rc = proc.waitFor();
            result.stdout = readInputStream(proc.getInputStream());
            result.stderr = readInputStream(proc.getErrorStream());
        } catch (Exception e ) {
            result.rc = -1;
            result.stdout = "";
            result.stderr = JexlerUtil.getStackTrace(e);
        }
        return result;
    }

    /**
     * Run the given shell command and return the result.
     * If an exception occurs, the return code of the result is set to -1,
     * stderr of the result is set to the stack trace of the exception and
     * stdout of the result is set to an empty string.
     * @param cmdarray array containing the command and its arguments
     * @return result, never null
     */
    public Result run(String[] cmdarray) {
        Result result = new Result();
        try {
            Process proc = Runtime.getRuntime().exec(cmdarray, environment, workingDirectory);
            result.rc = proc.waitFor();
            result.stdout = readInputStream(proc.getInputStream());
            result.stderr = readInputStream(proc.getErrorStream());
        } catch (Exception e ) {
            result.rc = -1;
            result.stdout = "";
            result.stderr = JexlerUtil.getStackTrace(e);
        }
        return result;
    }

    private String readInputStream(InputStream is) {
    	// (assume default platform character encoding)
        try (Scanner s = new Scanner(is)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

}
