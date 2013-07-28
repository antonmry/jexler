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

* Get the jexler source from github: [https://github.com/jexler/jexler](https://github.com/jexler/jexler)
* Install Java 7 and Gradle
* `gradle demo`
* Open [http://localhost:9080/](http://localhost:9080/) in a web browser
* See the README at github for alternatives...

Services
--------

**CronService**

This service sends a CronEvent at times configurable with a cron string:

    services.add(new CronService(jexler, "hourly").setCron("0 * * * *"))

Note that the `setCron()` method returns the CronService instance, so that setters can be chained.

There are two special cron strings that may be useful for testing:

* "now": Sends a single CronEvent immediately.
* "now+stop": Sends a single CronEvent immediately, followed by a single StopEvent.

The CronEvent class has a single getter `getCron()` to get the cron string that caused the event:

    log.trace(cronEvent.cron)
    
(Note that `cronEvent.cron` is a Groovy shortcut for `cronEvent.getCron()`.)


Implemented using the Open Source [cron4j](http://www.sauronsoftware.it/projects/cron4j/) library.

**DirWatcherService**

This service observes a directory for changes in the file system and sends events when a file is created, modified or deleted:

    services.add(new DirWatchService(jexler, "watch-jexler-dir"))
    services.start()
    
    while (true) {
      event = events.take();
      if (event instanceof DirWatchEvent) {
        log.trace("Got file change: ${event.kind} '${event.file.name}'")
      } else if (event instanceof StopEvent) {
        return
      }
    }

There are two setters:

* `setDir(File dir)`: The directory to watch,
  default if not set is the directory that contains the jexler.
* `setSleepTimeMs(long sleepTimeMs)`:
   Time to sleep between polling file system,
   default if not set is 1000ms (1 sec).

The DirWatchEvent class has the following getters:

* `File getFile()`: Get file that has been created, modified or deleted.
* `WatchEvent.Kind<?> getKind()`: Get what happened with the file,
   can be StandardWatchEventKinds.ENTRY_CREATE,
   .ENTRY_MODIFY or .ENTRY_DELETE.

Implemented using a Java 7 WatchService.

**More Services**

Writing your own services is relatively easy, since you can also write services in Groovy even from within the jexler web GUI.

The trick is that all Groovy scripts in the jexlers directory are part of the class path.

So, for example, if you wanted a more sophisticated version of CronService, you could copy the CronService.java from the jexler source to a MyCronService.groovy in the jexlers directory in the jexler webapp and do the same for CronEvent. After a few boilerplate changes, you should have a MyCronService that does the same as CronService and could start adding new features.

And if you feel that it would be great if jexler had more services out-of-the-box, feel free to write your own Java or Groovy library of service and make it available.

Side remark: If you wanted an additional service to be included with jexler itself, it would have to be something really, really, really central and generally useful and simple to manage and test, otherwise I wouldn't touch it ;)

Tools
-----

**ShellTool**

This tool allows to run shell commands:

    shellTool = new ShellTool()
    result = shellTool.run("echo 'hello world'")
    log.trace(result.toString())

There are two setters:

* `setWorkingDirectory(File dir)`:
   Set working directory for the command;
   if not set or set to null, inherit from parent process.
* `setEnvironment(Map<String,String> env)`:
  Set environment variables for the command
  (key is variable name, value is variable value);
  if not set or set to null, inherit from parent process.

Note that the setters again return the ShellTool instance, i.e. setters can be chained:

    new ShellTool().setWorkingDirectory('/tmp').run('ls')

And there are two methods for running a shell command:

* `Result run(String command)`
* `Result run(List<String> cmdList)`

The second method allows to explicitly indicate the application to run (first list element) and how to split its arguments.

Passing the right command string or list of commands can be tricky:

* On windows some common shell commands like "dir" or "echo" are not actually commands, but arguments to cmd.exe, so use e.g. `cmd /c echo hello` as a command string.
* To set the working directory for cygwin, use e.g. `c:/cygwin/bin/bash -l /my/working/dir ls -l`.
* Sometimes there is now way around splitting up arguments explicitly.

The Result contains three items:

* `int rc`: Return code of the command (0 means no error, any other value indicates that something went wrong).
* `String stdout`: The standard output of the command.
* `String stderr`: The standard error output of the command.

If an exception occurs, the return code of the result is set to -1, stderr of the result is set to the stack trace of the exception and stdout of the result is set to an empty string.

Note that the `toString()` method of Result produces a single line string suitable for logging. Line breaks in stdout and stderr are replaced by '%n'.

Implemented using `Runtime.getRuntime().exec()`.

**ObfuscatorTool**

This tool can be used to obfuscate passwords and other "minor" secret strings. It uses (single) DES, by default with a hard-coded key (plus IV).

* `String obfuscate(String plain)`:
  UTF-8 encode, encipher and hex encode given string.
* `public String deobfuscate(String encHex)`:
  Hex decode, decipher and UTF-8 decode given string.

Simple use case:

* Log obfuscated password:
  `log.trace(new ObfuscatorTool().obfuscate("mysecret"))`
* Copy obfuscated password from log file (and delete entry from log file.)
* Use: `def password = new ObfuscatorTool().deobfuscate("2A8A0F691DB78AD8DA6664D3A25DA963")`

Use your own keys:

* `public ObfuscatorTool (String hexKey, String hexIv)`

Note that all of this is not a cryptographically strong protection of secrets, just a countermeasure to fend off the simplest attacks, like e.g. "shoulder surfing". Someone with access to the running jexler with write permission for jexler scripts can easily desobfuscate secrets.

**More Tools**

With Java and Groovy plus Grape you have *thousands* of tools and libraries at your fingertips, just search the internet when you need something specific.

Note again that since almost all Java code is valid Groovy code, you can search for solutions in Java and Groovy to find something you can use in jexler scripts.

Besides, essentially the same comments as for services apply also to tools.

Web GUI
-------

TODO

Security
--------

TODO

Troubleshooting
---------------

TODO

Roadmap
-------

Well, there is none, except to keep jexler small and to keep the quality high. :)

I will gladly link third party libraries and similar additions around jexler and, if you want to take it all to a new level, feel free to do so within jexler's Apache 2 Open Source license.

Copyright 2012-now $(whois jexler.net)  
[http://www.jexler.net/](http://www.jexler.net/)

License
-------

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.

