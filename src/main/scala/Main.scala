import akka.actor._
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
        println("REST RestInterface could not bind to " +
          s"$host:$port, ${cmd.failureMessage}")
        system.shutdown()
    }

  // Create actors for simulation
  var i = 0
  var network_size = 10
  var fbUsers: Array[ActorRef] = new Array[ActorRef](network_size)
  for (i <- 0 until network_size)
    fbUsers(i) = system.actorOf(Props(new UserSimulator(system)), name = i.toString)
    
  for (i <- 0 to 5) {
     fbUsers(i+2) ? CreateAlbum
  }
  
  fbUsers(0) ! GetAlbum("1")
  fbUsers(0) ? UploadPhoto("1.png", "1")
  fbUsers(0) ? UploadPhoto("4.png", "4")
  fbUsers(0) ? AddFriend("5")
}
