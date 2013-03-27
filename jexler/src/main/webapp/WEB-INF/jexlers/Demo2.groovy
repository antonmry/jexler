// autostart
import groovy.grape.Grape

@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.2.3')
@Grab(group='commons-net', module='commons-net', version='2.0')

import net.jexler.StopService
import net.jexler.CronService
import net.jexler.ShellTool
		
import org.apache.http.client.ResponseHandler
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient

import org.apache.commons.net.ftp.FTPClient

services.add(new CronService(jexler, "every-minute").setCron("* * * * *").start())

while (true) {
  event = events.take()
  if (event instanceof CronService.Event) {
    
    // run shell command
    shellTool = new ShellTool()
    result = shellTool.run("echo 'hello world'")
    log.trace("rc = " + result.rc)
    log.trace("stdout = " + result.stdout)
    log.trace("stderr = " + result.stderr)
    
    httpclient = new DefaultHttpClient()
    httpget = new HttpGet("http://www.google.com/")
    responseHandler = new BasicResponseHandler()
    responseBody = httpclient.execute(httpget, responseHandler)
    println(responseBody)
    
    throw new RuntimeException("Demo RuntimeException")

	/*
    new FTPClient().with {
      connect "some-server.some-domain.com"
      enterLocalPassiveMode()
      login "your-username", "your-password"
      changeWorkingDirectory "/var/appl/some/remote/dir/"
      def incomingFile = new File("some-file-to-retrieve.log")
      incomingFile.withOutputStream { ostream -> retrieveFile "some-file-to-retrieve.log", ostream }
      disconnect()
    }*/
        
  } else if (event instanceof StopService.Event) {
    return
  }
}
