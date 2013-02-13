from net.jexler import StopSensor
from net.jexler import CronSensor

sensors.add(CronSensor(jexler, "every-minute").setCron("* * * * *").start())
sensors.add(CronSensor(jexler, "every-two-minutes").setCron("* * * * *").start())

while True:
    event = events.take()
    if isinstance(event, CronSensor.Event):
        print "Is is now (jython): %s (%s)" % ("dummy time", event.getSensorId())
    elif isinstance(event, StopSensor.Event):
        break
