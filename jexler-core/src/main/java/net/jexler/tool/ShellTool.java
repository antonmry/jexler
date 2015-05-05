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

import groovy.lang.Closure;

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
 * Note that there are already at least two standard ways of doing this
 * with Groovy APIs, which may or may not be more convenient depending
 * on your use case.
 *
 * @author $(whois jexler.net)
 */
public class ShellTool {

    private static final Logger log = LoggerFactory.getLogger(ShellTool.class);

    /**
	 * Simple bean for the result of executing a shell command.
	 */
    public class Result {
        public int rc;
        public String stdout;
        public String stderr;
        @Override
        public String toString() {
        	return "[rc=" + rc + ",stdout='" + JexlerUtil.toSingleLine(stdout) +
                    "',stderr='" + JexlerUtil.toSingleLine(stderr) + "']";
        }
    }
    
    /**
     * Helper class for collecting stdout and stderr.
     */
    public static class OutputCollector extends Thread {
		private final InputStream is;
		private final Closure<?> lineHandler;
        private final String name;
		private String output;
		OutputCollector(InputStream is, Closure<?> lineHandler, String name) {
			this.is = is;
			this.lineHandler = lineHandler;
            this.name = name;
		}
	    @Override
		public void run() {
            Thread.currentThread().setName(name);
	    	StringBuilder out = new StringBuilder();
	    	// (assume default platform character encoding)
	    	Scanner scanner = new Scanner(is);
        	while (scanner.hasNext()) {
        		String line = scanner.nextLine();
        		out.append(line);
                out.append(System.lineSeparator());
        		if (lineHandler != null) {
	    			lineHandler.call(line);
	    		}
        	}
	    	scanner.close();
	    	output = out.toString();
		}
		public String getOutput() {
			return output;
		}
	}

    private File workingDirectory;
    private Map<String,String> env;
    private Closure<?> stdoutLineHandler;
    private Closure<?> stderrLineHandler;

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
     * Set a closure that will be called to handle each line of stdout.
     * If not set or set to null, do nothing.
     * @return this (for chaining calls)
     */
    public ShellTool setStdoutLineHandler(Closure<?> handler) {
        stdoutLineHandler = handler;
        return this;
    }
    
    /**
     * Set a closure that will be called to handle each line of stderr.
     * If not set or set to null, do nothing.
     * @return this (for chaining calls)
     */
    public ShellTool setStderrLineHandler(Closure<?> handler) {
        stderrLineHandler = handler;
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
        try {
            Process proc = Runtime.getRuntime().exec(command, toEnvArray(env), workingDirectory);
            return getResult(proc);
        } catch (Exception e ) {
        	return getExceptionResult(JexlerUtil.getStackTrace(e));
        }
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
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray, toEnvArray(env), workingDirectory);
            return getResult(proc);
        } catch (Exception e ) {
            return getExceptionResult(JexlerUtil.getStackTrace(e));
        }
    }
    
    /**
     * Get result of given process.
     */
    private Result getResult(Process proc) throws Exception {
        OutputCollector outCollector = new OutputCollector(proc.getInputStream(), stdoutLineHandler, "stdout collector");
        OutputCollector errCollector = new OutputCollector(proc.getErrorStream(), stderrLineHandler, "stderr collector");
        outCollector.start();
        errCollector.start();
        Result result = new Result();
        result.rc = proc.waitFor();
        outCollector.join();
        errCollector.join();
        result.stdout = outCollector.getOutput();
        result.stderr = errCollector.getOutput();
		return result;
	}
	
	/**
	 * Get result in case where an exception occurred.
	 */
	private Result getExceptionResult(String stackTrace) {
		Result result = new Result();
		result.rc = -1;
        result.stdout = "";
        result.stderr = stackTrace;
        return result;
    }

    /**
     * Convert map of name and value to array of name=value.
     */
    private String[] toEnvArray(Map<String,String> env) {
    	if (env == null) {
    		return null;
    	}
    	List<String> envList = new LinkedList<>();
    	for (Entry<String, String> entry : env.entrySet()) {
    		envList.add(entry.getKey() + "=" + entry.getValue());
    	}
    	String[] envArray = new String[envList.size()];
    	return envList.toArray(envArray);
    }

}
