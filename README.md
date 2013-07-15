[![jexler logo](http://www.jexler.net/jexler.jpg)](http:www.jexler.net/)

jexler
======

Jexler is a simple relaxed Java 7 framework for starting/stopping
Groovy scripts as services and enabling them to react to events
of their choice.

Comes as a library (JAR) plus a simple relaxed web GUI (WAR).

* Java 7 (with Groovy), Eclipse/Gradle, Apache License
* Web GUI: Java 7, Servlet 2.5 (Tomcat 6 or later)

Build
-----

* Quick build: `gradle clean build`
* Full build: `gradle clean build pom verySlowTests`
* Create eclipse project: `gradle eclipse`

Try web GUI
-----------

* Demo: `gradle demo`
* Go to http://localhost:9080/
* or run JexlerJettyTest in eclipse
* or deploy jexler/build/libs/jexler-*.war
* or deploy jexler-*.war from sourceforge (link below)

Resources
---------

* Website: [http://www.jexler.net/](http://www.jexler.net/)
* jexler-core (JAR): Maven Central (soon)
* jexler (WAR): [Sourceforge](https://sourceforge.net/projects/jexler/)
* Javadoc (jexler-core): [http://www.jexler.net/javadoc/](http://www.jexler.net/javadoc/)

Screenshots
-----------

![web gui](http://a.fsdn.com/con/app/proj/jexler/screenshots/jexler-sf-screenshot.jpg)
