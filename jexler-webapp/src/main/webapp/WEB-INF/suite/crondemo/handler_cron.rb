import Java::net.jexler.sensor.CronSensor

if $method == "handle"
  if $message.get("sender").is_a? CronSensor
    if $message.get("id") == $config.get("id")
      puts "It is now " + Time.new.inspect
      return $message
    end
  end
end
