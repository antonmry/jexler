![jexler logo](http://www.jexler.net/jexler.jpg)

jexler
======

Java 6, Eclipse/Gradle, Apache License

e.g.

* &gt; gradle build
* &gt; gradle eclipse

jexler webapp
-------------

* run JexlerJetty class in Eclipse
* http://localhost:8080/jexler
* or deploy jexler-webapp/build/libs/jexler-*.war

jexler command line application
-------------------------------

* &gt; cd jexler-cli
* &gt; gradle installApp
* &gt; build/install/jexler-cli/bin/jexler-cli ../jexler-webapp/src/main/webapp/WEB-INF/suite
* or run JexlerCliTest class in Eclipse
