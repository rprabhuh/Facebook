import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http

object Main extends App {

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[GraphAPI], name = "handler")
  
  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
}
