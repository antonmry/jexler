# test

import Java::java.util.HashMap
import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

$handlers.add CronHandler.new("jexler", "Sends every minute a cron message",
  "* * * * *", "test-cron-id")

config = HashMap.new
config.put("cronid", "test-cron-id")
# TODO set current dir to webapp directory
$handlers.add ScriptHandler.new("jexler", "Handles cron message by ruby script",
  "ruby", "src/main/webapp/WEB-INF/scripts/handler.rb", config)
