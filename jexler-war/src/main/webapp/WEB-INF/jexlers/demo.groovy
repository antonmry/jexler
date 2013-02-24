// autostart
import net.jexler.StopService
import net.jexler.CronService
import net.jexler.ShellTool

services.add(new CronService(jexler, "every-minute").setCron("* * * * *").start())

while (true) {
  event = events.take();
  if (event instanceof CronService.Event) {
    
    // run shell command
    shellTool = new ShellTool()
    result = shellTool.run("echo 'hello world'")
    log.trace("rc = " + result.rc)
    log.trace("stdout = " + result.stdout)
    log.trace("stderr = " + result.stderr)
    
    throw new RuntimeException("Demo RuntimeException")
    
  } else if (event instanceof StopService.Event) {
    return
  }
}
