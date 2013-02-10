import Java::net.jexler.sensor.CronSensor
import Java::net.jexler.handler.ScriptHandler

$description = "Cron demo jexler"

id = "cron" # handler id and id in sent messages
h = CronSensor.new(id, "Sends every minute a cron message")
h.setCron("* * * * *")
$handlers.add h

h = ScriptHandler.new("crondemo", "Handles cron message by script")
h.setScriptFile($jexlerDir + "/handler_cron.rb")
h.set("id", id) # message id to handle
$handlers.add h
