# test

import Java::java.util.HashMap
import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.CronHandler

$description = "Cron handler demo jexler"

config = HashMap.new
config.put("cronid", "nagnag")
$handlers.add ScriptHandler.new("crontest", "Handles 'nagnag' cron message by ruby script",
  $jexlerDir + "/handler_cron.rb", config)

$handlers.add CronHandler.new("nagnag", "Sends every minute a cron message",
  "* * * * *")
