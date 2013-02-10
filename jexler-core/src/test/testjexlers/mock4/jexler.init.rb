import Java::net.jexler.core.MockHandler
import Java::net.jexler.core.JexlerMessageFactory

$description = "Submits and handles"

h1 = MockHandler.new("h1", "Mock handler 1")
h2 = MockHandler.new("h2", "Mock handler 2")
h3 = MockHandler.new("h3", "Mock handler 3")

h1.handleAction = "pass" # pass message on
h2.handleAction = "null" # done, do not pass message on<
h3.handleAction = "pass" # pass message on (but should not get there)
h3.submitMessageAtStart = JexlerMessageFactory.create.set("info", "msg")

$handlers.add h1
$handlers.add h2
$handlers.add h3
