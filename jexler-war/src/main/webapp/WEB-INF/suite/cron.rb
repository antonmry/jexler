import Java::net.jexler.StopSensor
import Java::net.jexler.CronSensor

$sensors.add CronSensor.new($jexler, "every-minute").setCron("* * * * *").start
$sensors.add CronSensor.new($jexler, "every-two-minutes").setCron("*/2 * * * *").start

begin
  event = $events.take
  if event.is_a? CronSensor::Event
    puts "It is now " + Time.new.inspect + " (" + event.getSensorId + ")"
  elsif event.is_a? StopSensor::Event
    return
  end
end while true
