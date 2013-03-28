// autostart
import net.jexler.service.StopService
import net.jexler.service.CronService

services.add(new CronService(jexler, "every-minute").setCron("* * * * *").start())
services.add(new CronService(jexler, "every-two-minutes").setCron("*/2 * * * *").start())

while (true) {
  event = events.take();
  if (event instanceof CronService.Event) {
    println ("It is now: " + new Date() + " (" + event.serviceId + ")")
	println ("Util.hello(): " + Util.hello())
  } else if (event instanceof StopService.Event) {
    return
  }
}
