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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
public class MockHandlerTest {

    private JexlerMessage messageToHandle =
            JexlerMessageFactory.create().set("info", "msg-to-handle");
    private JexlerMessage messsageToSubmitAtHandle =
            JexlerMessageFactory.create().set("info", "msg-to-submit-at-handle");
    private JexlerMessage messsageToSubmitAtStartup =
            JexlerMessageFactory.create().set("info", "msg-to-submit-at-startup");
    private MockSubmitter mockSubmitter = new MockSubmitter();
    private MockHandler mockHandler = new MockHandler("myid", "my description");

    @Test
    public void testNormalHandlingTrue() {
        // no throws, handle returns identical message
        MockHandler.clearCallList();
        mockHandler.startupAction = "nil";
        mockHandler.handleAction = "pass";
        mockHandler.shutdownAction = "nil";
        mockHandler.startup(mockSubmitter);
        assertEquals("must be equals", messageToHandle, mockHandler.handle(messageToHandle));
        mockHandler.shutdown();
        //MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equals", 3, callList.size());
        assertEquals("must be equals", "startup : nil", callList.get(0));
        assertEquals("must be equals", "handle msg-to-handle : pass", callList.get(1));
        assertEquals("must be equals", "shutdown : nil", callList.get(2));
    }

    @Test
    public void testThrow() {
        // all throw
        MockHandler.clearCallList();
        mockHandler.startupAction = "throw";
        mockHandler.handleAction = "throw";
        mockHandler.shutdownAction = "throw";
        try {
            mockHandler.startup(mockSubmitter);
            fail("must throw");
        } catch (RuntimeException e) {
            assertEquals("must be equals", "startup", e.getMessage());
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
        //MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equals", 3, callList.size());
        assertEquals("must be equals", "startup : throw", callList.get(0));
        assertEquals("must be equals", "handle msg-to-handle : throw", callList.get(1));
        assertEquals("must be equals", "shutdown : throw", callList.get(2));
    }

    @Test
    public void testSubmitFalse() {
        // no throws, submit, handle returns null
        MockHandler.clearCallList();
        mockHandler.startupAction = "nil";
        mockHandler.submitMessageAtStartup = messsageToSubmitAtStartup;
        mockHandler.handleAction = "null";
        mockHandler.submitMessageAtHandle = messsageToSubmitAtHandle;
        mockHandler.shutdownAction = "nil";
        mockHandler.startup(mockSubmitter);
        assertNull("must be null", mockHandler.handle(messageToHandle));
        mockHandler.shutdown();
        //MockHandler.printCallList();
        List<String> callList = MockHandler.getCallList();
        assertEquals("must be equals", 5, callList.size());
        assertEquals("must be equals", "startup : nil, submit msg-to-submit-at-startup", callList.get(0));
        assertEquals("must be equals", "submit msg-to-submit-at-startup", callList.get(1));
        assertEquals("must be equals", "handle msg-to-handle : null, submit msg-to-submit-at-handle", callList.get(2));
        assertEquals("must be equals", "submit msg-to-submit-at-handle", callList.get(3));
        assertEquals("must be equals", "shutdown : nil", callList.get(4));
    }

}
