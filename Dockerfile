FROM jetty:latest
MAINTAINER Antón R. Yuste "anton@galiglobal.com"
ADD ./jexler/build/jexler /var/lib/jetty/webapps/jexler
USER root
RUN chown -R jetty /var/lib/jetty/webapps/jexler
USER jetty

