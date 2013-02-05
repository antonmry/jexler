# test

import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

$description="command line jexler"

scriptHandler = ScriptHandler.new("crontest", "Handles 'nagnag' cron message by ruby script")
scriptHandler.setScriptFileName($jexlerDir + "/handler_cron.rb")
scriptHandler.set("cronid", "nagnag")
$handlers.add scriptHandler

cronHandler = CronHandler.new("nagnag", "Sends every minute a cron message")
cronHandler.setCron("* * * * *")
$handlers.add cronHandler
