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

package net.jexler.tool

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import java.util.Map.Entry

import net.jexler.JexlerUtil

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
@CompileStatic
class ShellTool {

    /**
     * Simple bean for the result of executing a shell command.
     *
     * @author $(whois jexler.net)
     */
    @CompileStatic
    static class Result {
        int rc
        String stdout
        String stderr
        Result(int rc, String stdout, String stderr) {
            this.rc = rc
            this.stdout = stdout
            this.stderr = stderr
        }
        @Override
        String toString() {
            return "[rc=$rc,stdout='${JexlerUtil.toSingleLine(stdout)}'," +
                    "stderr='${JexlerUtil.toSingleLine(stderr)}']"
        }
    }
    
    /**
     * Helper class for collecting stdout and stderr.
     */
    @CompileStatic
    @PackageScope
    static class OutputCollector extends Thread {
        private final InputStream is
        private final Closure lineHandler
        private final String threadName
        private String output
        OutputCollector(InputStream is, Closure lineHandler, String threadName) {
            this.is = is
            this.lineHandler = lineHandler
            this.threadName = threadName
        }
        @Override
        void run() {
            currentThread().name = threadName
            StringBuilder out = new StringBuilder()
            // (assume default platform character encoding)
            Scanner scanner = new Scanner(is)
            while (scanner.hasNext()) {
                String line = scanner.nextLine()
                out.append(line)
                out.append(System.lineSeparator())
                if (lineHandler != null) {
                    lineHandler.call(line)
                }
            }
            scanner.close()
            output = out.toString()
        }
        public String getOutput() {
            return output
        }
    }

    private File workingDirectory
    private Map<String,String> env
    private Closure stdoutLineHandler
    private Closure stderrLineHandler

    /**
     * Constructor.
     */
    ShellTool() {
    }

    /**
     * Set working directory for the command.
     * If not set or set to null, inherit from parent process.
     * @return this (for chaining calls)
     */
    ShellTool setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory
        return this
    }

    /**
     * Set environment variables for the command.
     * Key is variable name, value is variable value.
     * If not set or set to null, inherit from parent process.
     * @return this (for chaining calls)
     */
    ShellTool setEnvironment(Map<String,String> env) {
        this.env = env
        return this
    }
    
    /**
     * Set a closure that will be called to handle each line of stdout.
     * If not set or set to null, do nothing.
     * @return this (for chaining calls)
     */
    ShellTool setStdoutLineHandler(Closure handler) {
        stdoutLineHandler = handler
        return this
    }
    
    /**
     * Set a closure that will be called to handle each line of stderr.
     * If not set or set to null, do nothing.
     * @return this (for chaining calls)
     */
    ShellTool setStderrLineHandler(Closure handler) {
        stderrLineHandler = handler
        return this
    }

    /**
     * Run the given shell command and return the result.
     * If an exception occurs, the return code of the result is set to -1,
     * stderr of the result is set to the stack trace of the exception and
     * stdout of the result is set to an empty string.
     * @param command command to run
     * @return result, never null
     */
    Result run(String command) {
        try {
            Process proc = Runtime.runtime.exec(command, toEnvArray(env), workingDirectory)
            return getResult(proc)
        } catch (Exception e ) {
            return getExceptionResult(JexlerUtil.getStackTrace(e))
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
    Result run(List<String> cmdList) {
        String[] cmdArray = new String[cmdList.size()]
        cmdList.toArray(cmdArray)
        try {
            Process proc = Runtime.runtime.exec(cmdArray, toEnvArray(env), workingDirectory)
            return getResult(proc)
        } catch (Exception e ) {
            return getExceptionResult(JexlerUtil.getStackTrace(e))
        }
    }
    
    /**
     * Get result of given process.
     */
    private Result getResult(Process proc) throws Exception {
        OutputCollector outCollector = new OutputCollector(proc.inputStream, stdoutLineHandler, 'stdout collector')
        OutputCollector errCollector = new OutputCollector(proc.errorStream, stderrLineHandler, 'stderr collector')
        outCollector.start()
        errCollector.start()
        int rc = proc.waitFor()
        outCollector.join()
        errCollector.join()
        return new Result(rc, outCollector.output, errCollector.output)
    }

    /**
     * Get result in case where an exception occurred.
     */
    private static Result getExceptionResult(String stackTrace) {
        return new Result(-1, '', stackTrace)
    }

    /**
     * Convert map of name and value to array of name=value.
     */
    private static String[] toEnvArray(Map<String,String> env) {
        List envList = []
        env?.each { key, value ->
            envList.add("$key=$value")
        }
        return envList as String[]
    }

}
