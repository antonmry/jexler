[![jexler logo](http://www.jexler.net/jexler.jpg)](http:www.jexler.net/)

jexler releases
===============

1.0.6 (201x-xx-xx)
------------------

* ShellTool: Fixed a bug that could cause the run() methods to hang
  depending on output size and added a way to handle each line of
  stdout and stderr with closures (see user's guide).

1.0.5 (2013-07-29)
------------------

* User's guide.
* CronService: Cron string "now+stop" for a single CronEvent immediately,
  followed by a single StopEvent.
* Bugfix: Catching checked Exceptions in BasicJexler and BasicMetaInfo
  around calling Groovy scripts (because Groovy scripts may throw such
  checked Exceptions without the Java compiler being aware of the
  possibility).
* Two new context parameters in web.xml: jexler.safety.script.confirmSave 
  and jexler.safety.script.confirmDelete, see user's guide for details.

1.0.4 (2013-07-23)
------------------

* ShellTool: Methods with lists and maps instead of arrays.
* CronService: Cron string "now" for a single event immediately.
* Unit test coverage of jexler-core close to 100% (except for artefacts).
* GUI: Automatically updates status of jexlers every second.

1.0.3 (2013-07-16)
------------------

* Separated public API from internal classes.
* Added lots of unit tests.
* Javadoc.
* Maven pom and artefacts for publishing jexler-core to the
  maven central repository.

1.0.2 (2013-07-05)
------------------

* Some changes and new features.

1.0.1 (2013-06-28)
------------------

* Some changes and new features.

1.0.0 (2013-04-16)
------------------

* Initial release.

0.1.2 (early prototype, 2013-03-29)
-----------------------------------

* Some refinements after using it a bit.

0.0.3 (early prototype, 2013-03-16)
-----------------------------------

* Just Groovy.
* Webapp only.

0.0.2 (early prototype, 2013-02-24)
-----------------------------------

* Simple framework.
* Webapp that allows to start/stop jexlers, edit scripts
  (in jruby, jython or groovy), view issues and log file.
* Basic command line app that allows to start/stop jexlers.

0.0.1 (early prototype, 2013-02-13)
-----------------------------------

* Basic framework, unit tests, some handlers, command line and web app.
* Please ignore - about to be refactored and simplified completely.
