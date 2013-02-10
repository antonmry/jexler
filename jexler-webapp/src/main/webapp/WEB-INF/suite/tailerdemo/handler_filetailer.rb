import Java::net.jexler.sensor.FileTailerSensor

if $method == "handle"
  if $message.get("sender").is_a? FileTailerSensor
    if $message.get("id") == $config.get("id")
      puts "got line: " + $message.get("line")
      $log.info "### got line: " + $message.get("line")
      return $message
    end
  end
end
