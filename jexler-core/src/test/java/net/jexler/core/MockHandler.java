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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



/**
 * Mock jexler handler for unit tests.
 *
 * @author $(whois jexler.net)
 */
public class MockHandler extends AbstractJexlerHandler {

    private static List<String> callList = Collections.synchronizedList(new LinkedList<String>());

    // actions to set for controlling behavior
    public String startupAction = "nil";
    public JexlerMessage submitMessageAtStartup = null;
    public String canHandleAction = "false";
    public String handleAction = "false";
    public JexlerMessage submitMessageAtHandle = null;
    public String shutdownAction = "nil";

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
        callList.add("startup : " + startupAction
                + (submitMessageAtStartup == null ? "" : ", submit " + submitMessageAtStartup.get("info")));
        if (startupAction.equals("throw")) {
            throw new RuntimeException("startup");
        }
        if (submitMessageAtStartup != null) {
            submitter.submit(submitMessageAtStartup);
        }
    }

    @Override
    public boolean canHandle(JexlerMessage message) {
        callList.add("canHandle " + message.get("info") + " : " + canHandleAction);
        if (canHandleAction.equals("throw")) {
            throw new RuntimeException("canHandle");
        }
        return Boolean.valueOf(canHandleAction);
    }

    @Override
    public boolean handle(JexlerMessage message) {
        callList.add("handle " + message.get("info") + " : " + handleAction
                + (submitMessageAtHandle == null ? "" : ", submit " + submitMessageAtHandle.get("info")));
        if (handleAction.equals("throw")) {
            throw new RuntimeException("handle");
        }
        if (submitMessageAtHandle != null) {
            submitter.submit(submitMessageAtHandle);
        }
        return Boolean.valueOf(handleAction);
    }

    @Override
    public void shutdown() {
        callList.add("shutdown : " + shutdownAction);
        if (shutdownAction.equals("throw")) {
            throw new RuntimeException("shutdown");
        }
    }

    public static List<String> getCallList() {
        return callList;
    }

    public static void clearCallList() {
        callList.clear();
    }

    public static void printCallList() {
        System.out.println("Call List:");
        for (String call : callList) {
            System.out.println("- " + call);
        }
    }


}
