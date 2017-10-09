// Jexler { autostart = false}

@Grab(group='org.ajoberstar', module='grgit', version='1.3.0')
import org.ajoberstar.grgit.*

services.add(new CronService(jexler, 'once-immediately').setCron('now'))
services.start()

def jexlersDir = new File('/var/lib/jetty/webapps/jexler/WEB-INF/jexlers')
def jexlersGitDir = new File('/var/lib/jetty/webapps/jexler/WEB-INF/jexlers/.git')

while (true) {
    event = events.take()
    if (event instanceof CronEvent) {
        if( !jexlersGitDir.exists() ) {
            assert jexlersDir.deleteDir()  // Returns true if all goes well, false otherwise.
            def grgit = Grgit.clone(dir: jexlersDir, uri: "https://github.com/antonmry/jexler-automation-script.git")
        } else {
            def grgit = Grgit.open(dir: jexlersDir)
            grgit.pull()
        }
    } else if (event instanceof StopEvent) {
        return
    }
}


