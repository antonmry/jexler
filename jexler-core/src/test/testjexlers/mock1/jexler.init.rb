import Java::net.jexler.core.MockHandler

$description = "Simple mock that processes no messages"

$handlers.add MockHandler.new("h1", "Mock handler 1")
$handlers.add MockHandler.new("h2", "Mock handler 2")
