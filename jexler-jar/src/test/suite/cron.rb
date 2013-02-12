import Java::net.jexler.CronSensor
import Java::net.jexler.CronEvent
import Java::net.jexler.StopEvent

cronSensor = CronSensor.new($jexler, "* * * * *")
cronSensor.start

begin
  event = $events.take
  if event.is_a? CronEvent
    puts "It is now " + Time.new.inspect
  elsif event.is_a? StopEvent
    cronSensor.stop
    return 0
  end
end while true
