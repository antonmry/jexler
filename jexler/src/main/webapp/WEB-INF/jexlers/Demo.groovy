[ "autostart" : true ]

import groovy.grape.Grape
@Grab('org.apache.httpcomponents:httpclient:4.2.3')
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient

services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
services.start()

while (true) {
  event = events.take()
  if (event instanceof CronService.Event) {
    
    // run shell command
    shellTool = new ShellTool()
    result = shellTool.run("echo 'hello world'")
    log.trace("rc = " + result.rc)
    log.trace("stdout = " + result.stdout)
    log.trace("stderr = " + result.stderr)
    
	// http get request
    httpclient = new DefaultHttpClient()
    httpget = new HttpGet("http://www.google.com/")
    responseHandler = new BasicResponseHandler()
    responseBody = httpclient.execute(httpget, responseHandler)
    println(responseBody)
    
    Thread.sleep(30000)
    
    throw new RuntimeException("Demo RuntimeException")
        
  } else if (event instanceof StopService.Event) {
    return
  }
}
