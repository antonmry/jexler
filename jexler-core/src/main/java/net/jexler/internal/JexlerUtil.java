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

package net.jexler.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Jexler utilities.
 *
 * @author $(whois jexler.net)
 */
public class JexlerUtil {

    public static String readTextFile(File file) throws IOException {
        return readTextFileInternal(file, false);
    }

    public static String readTextFileReversedLines(File file) throws IOException {
        return readTextFileInternal(file, true);
    }

    /**
     * Read text file into string.
     * @param file
     * @param reverseLines whether to reverse the lines in the file or not
     * @return file contents or empty string if file does not exist
     * @throws IOException if reading failed
     */
    private static String readTextFileInternal(File file, boolean reverseLines) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (!file.exists()) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            do {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (reverseLines) {
                    builder.insert(0, line + System.lineSeparator());
                } else {
                    builder.append(line + System.lineSeparator());
                }
            } while (true);
        }
        return builder.toString();
    }

    /**
     * Get stack trace as a string.
     * @param throwable
     * @return stack trace or null if there is none
     */
    public static String getStackTrace(Throwable throwable) {
        try {
        	Writer result = new StringWriter();
            throwable.printStackTrace(new PrintWriter(result));
            return result.toString();
        } catch (RuntimeException e) {
            // no stack trace
            return null;
        }
    }
    
    /**
     * @param timeout time to wait in ms
     */
    public static void waitAtLeast(long timeout) {
    	long t0 = System.currentTimeMillis();
    	do {
    		if (System.currentTimeMillis() - t0 >= timeout) {
    			return;
    		}
    		try {
    			Thread.sleep(10);
    		} catch (InterruptedException e) {
    		}
    	} while (true);
    }

}
