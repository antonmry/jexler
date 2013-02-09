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

package net.jexler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class MockHandlerTest {

    private JexlerMessage messageToHandle =
            JexlerMessageFactory.create().set("info", "msg-to-handle");
    private JexlerMessage messsageToSubmitAtStartup =
            JexlerMessageFactory.create().set("info", "msg-to-submit-at-startup");
    private JexlerMessage messsageToSubmitAtHandle =
            JexlerMessageFactory.create().set("info", "msg-to-submit-at-handle");
    private MockSubmitter mockSubmitter = new MockSubmitter();
    private MockHandler mockHandler = new MockHandler("myid", "my description");

    @Test
    public void testNormalHandlingTrue() {
        // no throws, return true
        mockHandler.calls.clear();
        mockSubmitter.calls.clear();
        mockHandler.startupAction = "nil";
        mockHandler.canHandleAction = "true";
        mockHandler.handleAction = "true";
        mockHandler.shutdownAction = "nil";
        mockHandler.startup(mockSubmitter);
        assertTrue("must be true", mockHandler.canHandle(messageToHandle));
        assertTrue("must be true", mockHandler.handle(messageToHandle));
        mockHandler.shutdown();
        //mockHandler.printCalls();
        assertEquals("must be equals", 4, mockHandler.calls.size());
        assertEquals("must be equals", "startup : nil", mockHandler.calls.get(0));
        assertEquals("must be equals", "canHandle msg-to-handle : true", mockHandler.calls.get(1));
        assertEquals("must be equals", "handle msg-to-handle : true", mockHandler.calls.get(2));
        assertEquals("must be equals", "shutdown : nil", mockHandler.calls.get(3));
        assertEquals("must be equals", 0, mockSubmitter.calls.size());
    }

    @Test
    public void testThrow() {
        // all throw
        mockHandler.calls.clear();
        mockSubmitter.calls.clear();
        mockHandler.startupAction = "throw";
        mockHandler.canHandleAction = "throw";
        mockHandler.handleAction = "throw";
        mockHandler.shutdownAction = "throw";
        try {
            mockHandler.startup(mockSubmitter);
            fail("must throw");
        } catch (RuntimeException e) {
            assertEquals("must be equals", "startup", e.getMessage());
        }
        try {
            mockHandler.canHandle(messageToHandle);
            fail("must throw");
        } catch (RuntimeException e) {
            assertEquals("must be equals", "canHandle", e.getMessage());
        }
        try {
            mockHandler.handle(messageToHandle);
            fail("must throw");
        } catch (RuntimeException e) {
            assertEquals("must be equals", "handle", e.getMessage());
        }
        try {
            mockHandler.shutdown();
            fail("must throw");
        } catch (RuntimeException e) {
            assertEquals("must be equals", "shutdown", e.getMessage());
        }
        //mockHandler.printCalls();
        assertEquals("must be equals", 4, mockHandler.calls.size());
        assertEquals("must be equals", "startup : throw", mockHandler.calls.get(0));
        assertEquals("must be equals", "canHandle msg-to-handle : throw", mockHandler.calls.get(1));
        assertEquals("must be equals", "handle msg-to-handle : throw", mockHandler.calls.get(2));
        assertEquals("must be equals", "shutdown : throw", mockHandler.calls.get(3));
        assertEquals("must be equals", 0, mockSubmitter.calls.size());
    }

    @Test
    public void testSubmitFalse() {
        // no throws, submit, return false
        mockHandler.calls.clear();
        mockSubmitter.calls.clear();
        mockHandler.startupAction = "nil";
        mockHandler.startupSubmitMessage = messsageToSubmitAtStartup;
        mockHandler.canHandleAction = "false";
        mockHandler.handleAction = "false";
        mockHandler.handleSubmitMessage = messsageToSubmitAtHandle;
        mockHandler.shutdownAction = "nil";
        mockHandler.startup(mockSubmitter);
        assertFalse("must be false", mockHandler.canHandle(messageToHandle));
        assertFalse("must be false", mockHandler.handle(messageToHandle));
        mockHandler.shutdown();
        //mockHandler.printCalls();
        assertEquals("must be equals", 4, mockHandler.calls.size());
        assertEquals("must be equals", "startup : nil, submit msg-to-submit-at-startup", mockHandler.calls.get(0));
        assertEquals("must be equals", "canHandle msg-to-handle : false", mockHandler.calls.get(1));
        assertEquals("must be equals", "handle msg-to-handle : false, submit msg-to-submit-at-handle", mockHandler.calls.get(2));
        assertEquals("must be equals", "shutdown : nil", mockHandler.calls.get(3));
        assertEquals("must be equals", 2, mockSubmitter.calls.size());
        assertEquals("must be equals", "submit msg-to-submit-at-startup", mockSubmitter.calls.get(0));
        assertEquals("must be equals", "submit msg-to-submit-at-handle", mockSubmitter.calls.get(1));
    }

}
