[ "autostart" : true ]

services.add(new CronService(jexler, "once-immediately").setCron("now"))
services.start()

while (true) {
  event = events.take();
  if (event instanceof CronEvent) {
    // hangs forever resp. until eventually zapped
    while (true) {
      try {
        Thread.sleep(1000)
      } catch (InterruptedException ignore) {
      }
    }
  } else if (event instanceof StopEvent) {
    return
  }
}
