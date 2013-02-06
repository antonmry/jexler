import Java::net.jexler.handler.CronHandler
import Java::net.jexler.handler.ScriptHandler

$description = "Cron handler demo jexler"

id = "cron" # handler id and id in sent messages
h = CronHandler.new(id, "Sends every minute a cron message")
h.setCron("* * * * *")
$handlers.add h

h = ScriptHandler.new("cron-demo", "Handles cron message by script")
h.setScriptFile($jexlerDir + "/handler_cron.rb")
h.set("id", id) # message id to handle
$handlers.add h
