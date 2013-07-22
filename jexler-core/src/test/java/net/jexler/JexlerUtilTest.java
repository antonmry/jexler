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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;

import net.jexler.test.FastTests;

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

	@SuppressWarnings("serial")
	static class NoStackTraceException extends Exception {
		@Override
		public void printStackTrace(PrintWriter writer) {
			throw new RuntimeException();
		}
	}
	
	@Test
    public void testNoInstance() throws Exception {
		try {
			new JexlerUtil();
			fail("must throw");
		} catch (JexlerUtil.NoInstanceException e) {
		}
	}

	@Test
    public void testGetStackTrace() throws Exception {
		
		String stackTrace = JexlerUtil.getStackTrace(null);
		assertEquals("must be same", "", stackTrace);

		stackTrace = JexlerUtil.getStackTrace(new NoStackTraceException());
		assertEquals("must be same", "", stackTrace);

		stackTrace = JexlerUtil.getStackTrace(new Exception());
		//System.out.println(stackTrace);
		assertTrue("must be true", stackTrace.startsWith("java.lang.Exception"));
		String className = this.getClass().getSimpleName();
		assertTrue("must be true", stackTrace.contains(
				className + ".testGetStackTrace(" + className + ".java:"));
	}

	@Test
    public void testToSingleLine() throws Exception {
		assertNull("must be null", JexlerUtil.toSingleLine(null));
		assertEquals("must be same", "", JexlerUtil.toSingleLine(""));
		assertEquals("must be same", "%n 55", JexlerUtil.toSingleLine("%n 55"));
		assertEquals("must be same", "got %n this %n and %n that %n%n%n .",
				JexlerUtil.toSingleLine("got \r this \n and \r\n that \r\n\n\r ."));
	}

	@Test
    public void testWaitAtLeast() throws Exception {

		long t0 = System.currentTimeMillis();
		JexlerUtil.waitAtLeast(200);
		long t1 = System.currentTimeMillis();
		assertTrue("must be true", t1-t0 >= 200);
		assertTrue("should usually be true", t1-t0 < 2000);

		for (long ms=0; ms<=50; ms++) {
			t0 = System.currentTimeMillis();
			JexlerUtil.waitAtLeast(ms);
			t1 = System.currentTimeMillis();
			assertTrue("must be true", t1-t0 >= ms);
		}
	}
	
}
