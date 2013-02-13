import net.jexler.StopSensor;
import net.jexler.CronSensor;

sensors.add(new CronSensor(jexler, "every-minute").setCron("* * * * *").start());
sensors.add(new CronSensor(jexler, "every-two-minutes").setCron("*/2 * * * *").start());

while (true) {
  event = events.take();
  if (event instanceof CronSensor.Event) {
    println ("It is now (groovy) " + new Date() + " (" + event.getSensorId() + ")");
  } else if (event instanceof StopSensor.Event) {
    return;
  }
}
