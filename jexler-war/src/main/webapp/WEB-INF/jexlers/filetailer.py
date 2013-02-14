from net.jexler import StopSensor
from net.jexler import FileTailerSensor

s = FileTailerSensor(jexler, "selftailer")
s.setFile(file.getAbsolutePath())
s.addFilterPattern("^from")
s.start()
sensors.add(s)

while True:
    event = events.take()
    if isinstance(event, FileTailerSensor.Event):
        print "Got line (jython): %s" % event.getLine()
    elif isinstance(event, StopSensor.Event):
        break
