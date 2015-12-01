import akka.actor._
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import akka.util.Timeout

object Main extends App {
override def main(args: Array[String]) {
  /*if(args.length != 1 || isAllDigits(args(0)) == false) {
    println("Error: Enter the network size");
    System.exit(1)
  }*/

//  var NETWORK_SIZE = args(0).toInt

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

    println("Server starting up..")
    Thread sleep(5000)
    println("Server up!")

  // Create actors for simulation
  var i = 0
  
  import scala.collection.immutable.HashMap  
  import scala.collection.mutable.ArrayBuffer

  var NETWORK_SIZE = 100

  var fbUsers: Array[ActorRef] = new Array[ActorRef](NETWORK_SIZE)
  for (i <- 0 until NETWORK_SIZE) {
    fbUsers(i) = system.actorOf(Props(new UserSimulator(system)), name = i.toString)
    fbUsers(i) ? CreateProfile
  }

  println("\nProfiles created!")


  var numAlbumsCreated = 0
  for (i <- 0 until NETWORK_SIZE) {
     if (i%30 == 0)  { 
     	fbUsers(i) ? CreateAlbum
     	numAlbumsCreated += 1
     }
  }
  

  import scala.util.Random
  import scala.math.abs
  var R = new Random()
  var tmp: Int = 0
  for(i <- 0 until NETWORK_SIZE) {

    if (i%10 == 0)
      fbUsers(i) ! CreateStatus

    if (numAlbumsCreated > 1)
      tmp = abs(R.nextInt()%numAlbumsCreated)
      if (tmp == 0) tmp = 1
    else
      tmp = 1

    if (i%20 == 0) fbUsers(i) ? UploadPhoto("1.png", tmp.toString)
	  	  
	  if (i%30 == 0) fbUsers(i) ? UploadPhoto("2.png", tmp.toString)
	  
	  if (i%50 == 0) fbUsers(i) ? UploadPhoto("3.png", tmp.toString)
	  
	  if (i !=0 && i%20 == 0) fbUsers(i) ? AddFriend((i-10).toString)

    if (i%20 == 0) fbUsers(i) ? CreateComment(tmp.toString)
    if (i%60 == 0) fbUsers(i) ! UpdateComment("1", tmp.toString)
    if (i%90 == 0) fbUsers(i) ! DeleteComment("2")
    if (i%90 == 0) fbUsers(i) ! DeleteComment(tmp.toString)

  Thread sleep(100)

  if (i%100 == 0) fbUsers(i) ! GetAlbum("189783748374389")
  if (i%50 == 0) fbUsers(i) ! GetAlbum(tmp.toString)

  if (i%500 == 0) fbUsers(i) ! GetPhoto("12379872834")
  if (i%25 == 0) fbUsers(i) ! GetPhoto("1")

  fbUsers(0) ! GetProfile("1023789748")
  if (i%10 == 0) fbUsers(i) ! GetProfile(i.toString)
  if (i != 0 && i%15 == 0) fbUsers(i) ! GetProfile((i-10).toString)
  if (i%40 ==0) fbUsers(i) ! UpdateProfile
  if (i%80 ==0) fbUsers(i) ! DeleteProfile

  if (i%20 == 0) fbUsers(i) ! UpdateStatus("1")
  
  fbUsers(0) ! DeleteStatus("1333987492")
  if (i%60 == 0) fbUsers(i) ! DeleteStatus("1")
}

  
  println("Shutting down the server..")
  Thread sleep(10000)
  system.shutdown
}


  def isAllDigits(x: String) = x forall Character.isDigit
}
