import Java::net.jexler.handler.CronHandler

if $method == "handle"
  if $message.get("sender").is_a? CronHandler
    if $message.get("id") == $config.get("id")
      puts "It is now " + Time.new.inspect
      return $message
    end
  end
end
