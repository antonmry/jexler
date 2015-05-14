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

package net.jexler.internal

import net.jexler.service.MockService
import net.jexler.test.FastTests
import org.junit.experimental.categories.Category

/**
 * Tests the respective class.
 *
 * @author $(whois jexler.net)
 */
@Category(FastTests.class)
final class BasicIssueSpec extends spock.lang.Specification {

    def "construct and get without cause"() {
        expect:
        def issue = new BasicIssue(service, message, cause)
        issue.service == service
        issue.message == message
        issue.cause == cause
        issue.toString() == string

        where:
        service | message | cause | string
        null | null | null |
                "Issue: [message=null,service=null,cause=null,stackTrace='']"
        MockService.setTestInstance(null,"mockid") | 'hi \r a \n b \r\n c \r\n\r\n' | null |
                "Issue: [message='hi %n a %n b %n c %n%n',service='${MockService.class.name}:mockid',cause=null,stackTrace='']"
    }

    def "construct and get with cause"() {
        expect:
        def issue = new BasicIssue(service, message, cause)
        issue.service == service
        issue.message == message
        issue.cause == cause
        issue.toString().startsWith(string)
        !issue.toString().contains("\r")
        !issue.toString().contains("\n")

        where:
        service | message | cause | string
        MockService.setTestInstance(null,"mockid") | 'hi' | new RuntimeException('run') |
                "Issue: [message='hi',service='${MockService.class.name}:mockid'" +
                ",cause='java.lang.RuntimeException: run',stackTrace='java.lang.RuntimeException: run"
    }

    def "compare"() {
        expect:
        def issueEarlier = new BasicIssue(null, null, null)
        Thread.sleep(10)
        def issueLater = new BasicIssue(null, null, null)
        issueEarlier.compareTo(issueLater) > 0
        issueLater.compareTo(issueEarlier) < 0
    }

}
