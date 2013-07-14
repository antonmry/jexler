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

package net.jexler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

import net.jexler.FastTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
public final class JexlerUtilTest
{

	@Test
    public void readTextFileTest() throws Exception {
		
		File testFile = new File("/does/not/exist/4472948");
        String lines = JexlerUtil.readTextFile(testFile);
        assertEquals("must be same", "", lines);
        String linesRev = JexlerUtil.readTextFileReversedLines(testFile);
        assertEquals("must be same", "", linesRev);

		testFile = Files.createTempFile(null, null).toFile();
        FileWriter writer = new FileWriter(testFile);
        writer.append("line1\nline2\nline3");
        writer.close();
        
        String expectedLines = "line1" + System.lineSeparator() +
        		"line2" + System.lineSeparator() +
        		"line3" + System.lineSeparator();
		String expectedLinesRev = "line3" + System.lineSeparator() +
        		"line2" + System.lineSeparator() +
        		"line1" + System.lineSeparator();

		lines = JexlerUtil.readTextFile(testFile);
        assertEquals("must be same", expectedLines, lines);
        linesRev = JexlerUtil.readTextFileReversedLines(testFile);
        assertEquals("must be same", expectedLinesRev, linesRev);
		
        writer = new FileWriter(testFile);
        writer.append("line1\rline2\rline3");
        writer.close();
        
        lines = JexlerUtil.readTextFile(testFile);
        assertEquals("must be same", expectedLines, lines);
        linesRev = JexlerUtil.readTextFileReversedLines(testFile);
        assertEquals("must be same", expectedLinesRev, linesRev);
        
        writer = new FileWriter(testFile);
        writer.append("");
        writer.close();
        
        lines = JexlerUtil.readTextFile(testFile);
        assertEquals("must be same", "", lines);
        linesRev = JexlerUtil.readTextFileReversedLines(testFile);
        assertEquals("must be same", "", linesRev);
	}
	
	@SuppressWarnings("serial")
	static class NoStackTraceException extends Exception {
		@Override
		public void printStackTrace(PrintWriter writer) {
			throw new RuntimeException();
		}
	}
	
	@Test
    public void getStackTraceTest() throws Exception {
		
		String stackTrace = JexlerUtil.getStackTrace(null);
		assertNull("must be null", stackTrace);

		stackTrace = JexlerUtil.getStackTrace(new NoStackTraceException());
		assertNull("must be null", stackTrace);

		stackTrace = JexlerUtil.getStackTrace(new Exception());
		//System.out.println(stackTrace);
		assertNotNull("must not be null", stackTrace);
		assertTrue("must be true", stackTrace.startsWith("java.lang.Exception"));
		String className = this.getClass().getSimpleName();
		assertTrue("must be true", stackTrace.contains(
				className + ".getStackTraceTest(" + className + ".java:"));
	}	

	@Test
    public void waitAtLeastTest() throws Exception {
		
		long t0 = System.currentTimeMillis();
		JexlerUtil.waitAtLeast(0);
		long dt = System.currentTimeMillis() - t0;
		assertTrue("must be true", dt >= 0);
		
		t0 = System.currentTimeMillis();
		JexlerUtil.waitAtLeast(50);
		dt = System.currentTimeMillis() - t0;
		assertTrue("must be true", dt >= 50);
	}
	
	@Test
    public void getJexlerIdForFileTest() throws Exception {
		
		String id = JexlerUtil.getJexlerIdForFile(new File("foo.groovy"));
		assertEquals("must be same", "foo", id);

		id = JexlerUtil.getJexlerIdForFile(new File("/path/to/foo.groovy"));
		assertEquals("must be same", "foo", id);
		
		id = JexlerUtil.getJexlerIdForFile(new File("foo.java"));
		assertNull("must be null", id);
		
		id = JexlerUtil.getJexlerIdForFile(new File("/path/to/foo.java"));
		assertNull("must be null", id);	
	}
	
	@Test
    public void getFilenameForJexlerIdTest() throws Exception {
		
		String filename = JexlerUtil.getFilenameForJexlerId("foo");
		assertEquals("must be same", "foo.groovy", filename);
	}
	
}
