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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import net.jexler.JexlerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for running shell commands, just a thin wrapper around
 * the java runtime exec calls.
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
        @Override
        public String toString() {
        	StringBuilder builder = new StringBuilder();
        	builder.append("[rc=");
        	builder.append(rc);
        	builder.append(",stdout='");
        	builder.append(JexlerUtil.toSingleLine(stdout));
        	builder.append("',stderr='");
        	builder.append(JexlerUtil.toSingleLine(stderr));
        	builder.append("']");
        	return builder.toString();
        }
    }

    static final Logger log = LoggerFactory.getLogger(ShellTool.class);

    private File workingDirectory;
    private Map<String,String> env;

    /**
     * Constructor.
     */
    public ShellTool() {
    }

    /**
     * Set working directory for the command.
     * If not set or set to null, inherit from parent process.
     * @return this (for chaining calls)
     */
    public ShellTool setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * Set environment variables for the command.
     * Key is variable name, value is variable value.
     * If not set or set to null, inherit from parent process.
     * @return this (for chaining calls)
     */
    public ShellTool setEnvironment(Map<String,String> env) {
        this.env = env;
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
            Process proc = Runtime.getRuntime().exec(command, toEnvArray(env), workingDirectory);
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
     * @param cmdList list containing the command and its arguments
     * @return result, never null
     */
    public Result run(List<String> cmdList) {
    	String[] cmdArray = new String[cmdList.size()];
    	cmdList.toArray(cmdArray);
        Result result = new Result();
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray, toEnvArray(env), workingDirectory);
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
        Scanner scanner = new Scanner(is);
        scanner.useDelimiter("\\A");
        String data = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return data;
    }
    
    /**
     * Convert map of name and value to array of name=value.
     */
    private String[] toEnvArray(Map<String,String> env) {
    	if (env == null) {
    		return null;
    	}
    	List<String> envList = new LinkedList<String>();
    	for (Entry<String, String> entry : env.entrySet()) {
    		envList.add(entry.getKey() + "=" + entry.getValue());
    	}
    	String[] envArray = new String[envList.size()];
    	return envList.toArray(envArray);
    }

}
