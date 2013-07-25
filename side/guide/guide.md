[![jexler logo](jexler.jpg)](http:www.jexler.net/)

jexler
======

Jexler is a simple relaxed Java 7 framework for starting/stopping
Groovy scripts as services and enabling them to react to events
of their choice.

Here's a first example of a jexler Groovy script:

    [ "autostart" : true ]
    
    services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
    services.start()
    
    while (true) {
          event = events.take();
      if (event instanceof CronEvent) {
        log.info("hello")
      } else if (event instanceof StopEvent) {
        return
      }
    }

The script registers a cron service that will send it a CronEvent every minute and in the event loop below the script waits for events until it receives a StopEvent.

Now instead of just logging the word "hello", let's send an email instead:

    [ "autostart" : true ]
    
    import groovy.grape.Grape
    @Grab('org.apache.commons:commons-email:1.3')
    import org.apache.commons.mail.*
    
    services.add(new CronService(jexler, "every-minute").setCron("* * * * *"))
    services.start()
    
    while (true) {
      event = events.take();
      if (event instanceof CronEvent) {
        new SimpleEmail().with {
          setFrom "jex@jexler.net"
          addTo "bugs@acme.org"
          // ...
          setSubject "hello"
          setMsg "hello from jexler script"
          send()
        }
      } else if (event instanceof StopEvent) {
        return
      }
    }

This uses Groovy *Grape*, which allows to download external libraries and use them immediately in the same script.

And since Groovy is syntactically a superset of Java, you can easily find code samples and libraries for almost anything you might want to do.

Here's how the Web GUI looks like:

![web gui](jexler-gui.jpg)

What you can see above is essentially a list of jexler Groovy scripts. In the webapp in the file system it looks like this:

    jexler/
      WEB-INF/
        jexlers/
          Cron.groovy
          Demo.groovy
          DirWatcher.groovy
          Util.groovy
          some.properties
          ...
        ...
      ...


The first three jexlers are running, the last one is off, and you could start or stop them in the GUI, or look at the logfile, edit the scripts, etc.

If you want to try it out and play with jexler immediately:

* Get the jexler source from github: [https://github.com/jexler/jexler]()
* Install Java 7 and Gradle
* `gradle demo`
* Open [http://localhost:9080/]() in a web browser
* See the README at github for alternatives...
