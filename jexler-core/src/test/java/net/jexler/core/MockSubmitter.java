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
 * Mock jexler submitter for unit tests.
 *
 * @author $(whois jexler.net)
 */
public class MockSubmitter implements JexlerSubmitter {

    public List<String> calls = new LinkedList<>();

    public MockSubmitter() {
    }

    public void submit(JexlerMessage message) {
        calls.add("submit " + message.get("info"));
    }

    public void printCalls() {
        for (String call : calls) {
            System.out.println(call);
        }
    }

}
