import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import akka.util.Timeout

object Main extends App {

  implicit val system = ActorSystem("facebook")

  // the handler actcor replies to incoming HttpRequests
  val api = system.actorOf(Props[RestInterface], name = "handler")
  
  //IO(Http) ! 	Http.Bind(handler, interface = "localhost", port = 8080)

  val host = "localhost"
  val port = 8080

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10)

  IO(Http).ask(Http.Bind(listener = api, interface = host, port = port))
    .mapTo[Http.Event]
    .map {
      case Http.Bound(address) =>
        println(s"REST interface bound to $address")
      case Http.CommandFailed(cmd) =>
        println("REST interface could not bind to " +
          s"$host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }

  // Create an actor for simulation
  //var user1 = system.actorOf(Props[UserSimulator], name = "user1")
}
