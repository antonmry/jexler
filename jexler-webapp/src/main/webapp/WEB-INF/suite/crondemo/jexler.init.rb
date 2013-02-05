# test

import Java::net.jexler.handler.ScriptHandler
import Java::net.jexler.handler.CronHandler

$description = "Cron handler demo jexler"

h = ScriptHandler.new("crontest", "Handles 'nagnag' cron message by ruby script")
h.setScriptFile($jexlerDir + "/handler_cron.rb")
h.set("cronid", "nagnag")
$handlers.add h

h = CronHandler.new("nagnag", "Sends every minute a cron message")
h.setCron("* * * * *")
$handlers.add h
