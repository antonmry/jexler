# autostart
from net.jexler import StopService
from net.jexler import FileTailerService

s = FileTailerService(jexler, "selftailer")
s.setFile(file.getAbsolutePath())
s.addFilterPattern("^from")
s.start()
services.add(s)

while True:
    event = events.take()
    if isinstance(event, FileTailerService.Event):
        print "Got line (jython): %s" % event.getLine()
    elif isinstance(event, StopService.Event):
        break
