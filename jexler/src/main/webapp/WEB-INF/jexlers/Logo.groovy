// Jexler { autostart = true}

services.add(new CronService(jexler, 'once-immediately').setCron('now'))
services.start()

def favicon = 'https://www.jexler.net/favicon.ico'
def logo = 'https://www.jexler.net/jexler.jpg'

while (true) {
    event = events.take()
    if (event instanceof CronEvent) {
        def file = new File('/var/lib/jetty/webapps/jexler/favicon.ico').newOutputStream()
        file << new URL(favicon).openStream()
        file.close()

        file = new File('/var/lib/jetty/webapps/jexler/jexler.jpg').newOutputStream()
        file << new URL(logo).openStream()
        file.close()

    } else if (event instanceof StopEvent) {
        return
    }
}


