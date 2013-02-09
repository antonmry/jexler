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

import java.util.LinkedList;
import java.util.List;



/**
 * Mock jexler handler for unit tests.
 *
 * @author $(whois jexler.net)
 */
public class MockHandler extends AbstractJexlerHandler {

    // actions to set for controlling behavior
    public String startupAction = "nil";
    public String canHandleAction = "false";
    public String handleAction = "false";
    public JexlerMessage submitMessage = null;
    public String shutdownAction = "nil";

    public List<String> calls = new LinkedList<>();

    /**
     * Constructor from id and description.
     * @param id id
     * @param description description
     */
    public MockHandler(String id, String description) {
        super(id, description);
    }

    @Override
    public void startup(JexlerSubmitter submitter) {
        super.startup(submitter);
        calls.add("startup : " + startupAction);
        if (startupAction.equals("throw")) {
            throw new RuntimeException("startup");
        }
    }

    @Override
    public boolean canHandle(JexlerMessage message) {
        calls.add("canHandle " + message.get("info") + " : " + canHandleAction);
        if (canHandleAction.equals("throw")) {
            throw new RuntimeException("canHandle");
        }
        return Boolean.valueOf(canHandleAction);
    }

    @Override
    public boolean handle(JexlerMessage message) {
        calls.add("handle " + message.get("info") + " : " + handleAction
                + (submitMessage == null ? "" : ", submit " + submitMessage.get("info")));
        if (handleAction.equals("throw")) {
            throw new RuntimeException("handle");
        }
        if (submitMessage != null) {
            submitter.submit(submitMessage);
        }
        return Boolean.valueOf(handleAction);
    }

    @Override
    public void shutdown() {
        calls.add("shutdown : " + shutdownAction);
        if (shutdownAction.equals("throw")) {
            throw new RuntimeException("shutdown");
        }
    }

    public void printCalls() {
        for (String call : calls) {
            System.out.println(call);
        }
    }

}
