// autostart
import net.jexler.StopService;
import net.jexler.CronService;

services.add(new CronService(jexler, "every-minute").setCron("* * * * *").start());
services.add(new CronService(jexler, "every-two-minutes").setCron("*/2 * * * *").start());

while (true) {
  event = events.take();
  if (event instanceof CronService.Event) {
    println ("It is now (groovy): " + new Date() + " (" + event.getServiceId() + ")");
  } else if (event instanceof StopService.Event) {
    return;
  }
}
