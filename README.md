[![jexler logo](http://www.jexler.net/jexler.jpg)](http:www.jexler.net/)

jexler
======

Jexler is a simple Java 7 framework for starting/stopping Groovy scripts
as services and enabling them to react to events of their choice.

Comes as a library (JAR) plus a simple web GUI (WAR).

* Java 7 (with Groovy), Eclipse/Gradle, Apache License
* Web GUI: Java 7, Servlet 2.5 (Tomcat 6 or later)

Build
-----

* Quick build: `gradle clean build`
* Full build: `gradle clean build pom verySlowTests`
* Create eclipse project: `gradle eclipse`

Try web GUI
-----------

* Run JexlerJetty class in Eclipse
* Go to http://localhost:9080/
* or deploy jexler/build/libs/jexler-*.war
* or deploy jexler-*.war from sourceforge (link below)

Resources
---------

* Website: [http://www.jexler.net/](http://www.jexler.net/)
* jexler-core (JAR): Maven Central (soon)
* jexler (WAR): [Sourceforge](https://sourceforge.net/projects/jexler/)
* Online javadoc: At www.jexler.net (soon)

