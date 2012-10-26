# test

import Java::java.util.HashMap
import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

$handlers.add(CronHandler.new("jexler", "* * * * *", "test-cron-id"))

config = HashMap.new
config.put("cronid", "test-cron-id")
$handlers.add(ScriptHandler.new("jexler", "ruby", "scripts/handler.rb", config))
