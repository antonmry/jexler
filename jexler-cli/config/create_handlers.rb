# test

import Java::java.util.HashMap
import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

config = HashMap.new
config.put("cronid", "nagnag")
$handlers.add ScriptHandler.new("crontest", "Handles 'nagnag' cron message by ruby script",
  $configDir + "/handler_cron.rb", config)

$handlers.add CronHandler.new("nagnag", "Sends every minute a cron message",
  "* * * * *")
