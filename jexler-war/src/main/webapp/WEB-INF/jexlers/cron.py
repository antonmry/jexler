import time
from net.jexler import StopSensor
from net.jexler import CronSensor

sensors.add(CronSensor(jexler, "every-minute").setCron("* * * * *").start())
sensors.add(CronSensor(jexler, "every-two-minutes").setCron("*/2 * * * *").start())

while True:
    event = events.take()
    if isinstance(event, CronSensor.Event):
        now = time.strftime("%b %d %Y %H:%M:%S", time.localtime())
        print "Is is now (jython): %s (%s)" % (now, event.getSensorId())
    elif isinstance(event, StopSensor.Event):
        break
