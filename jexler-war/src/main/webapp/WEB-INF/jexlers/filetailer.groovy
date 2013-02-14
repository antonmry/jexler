import net.jexler.StopSensor;
import net.jexler.FileTailerSensor;

s = new FileTailerSensor(jexler, "selftailer");
s.setFile(file.getAbsolutePath());
s.addFilterPattern("^import");
s.start();
sensors.add(s);

while (true) {
  event = events.take();
  if (event instanceof FileTailerSensor.Event) {
    println ("Got line (groovy) " + event.getLine());
  } else if (event instanceof StopSensor.Event) {
    return;
  }
}
