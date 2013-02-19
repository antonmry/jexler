# autostart
import time
from net.jexler import StopService
from net.jexler import CronService

services.add(CronService(jexler, "every-minute").setCron("* * * * *").start())
services.add(CronService(jexler, "every-two-minutes").setCron("*/2 * * * *").start())

while True:
    event = events.take()
    if isinstance(event, CronService.Event):
        now = time.strftime("%b %d %Y %H:%M:%S", time.localtime())
        print "Is is now (jython): %s (%s)" % (now, event.getServiceId())
    elif isinstance(event, StopService.Event):
        break
