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
  
  import scala.collection.immutable.HashMap  
  import scala.collection.mutable.ArrayBuffer  
  // Maintain data for each user for testing and verification purposes
  class UserData(uid: String) {
  	var id: String = uid
  	var albums: ArrayBuffer[String] = ArrayBuffer("")
  	var photos: HashMap[String, ArrayBuffer[String]] = HashMap()
  }

  var network_size = 20

  var fbUsers: Array[ActorRef] = new Array[ActorRef](network_size)
  var users: Array[UserData] = new Array[UserData](network_size)
  for (i <- 0 until network_size) {
    fbUsers(i) = system.actorOf(Props(new UserSimulator(system)), name = i.toString)
    fbUsers(i) ? CreateProfile
  	users(i) = new UserData(i.toString)
  }

  var numAlbumsCreated = 0
  for (i <- 0 until network_size) {
     if (i%10 == 0)  { 
     	fbUsers(i) ? CreateAlbum
     	numAlbumsCreated += 1
     	users(i).albums.append(numAlbumsCreated.toString)
     }
  }
  
  def UpdatePhotos(userid: String, photo: String, album: String) = {
  	if (users(i).photos.contains(album)) {
  		var P = users(i).photos(album)
  		P.append(photo)
  		users(i).photos + (album -> P)
  	}
  	else {

  	}
  }

  import scala.util.Random
  import scala.math.abs
  var R = new Random()
  var tmp: Int = 0
  for(i <- 0 until network_size) {
    if (numAlbumsCreated > 1)
      tmp = abs(R.nextInt()%numAlbumsCreated)
      if (tmp == 0) tmp = 1
    else
      tmp = 1

    if (i%20 == 0) {
      fbUsers(i) ? UploadPhoto("1.png", tmp.toString)
	  	UpdatePhotos(i.toString, "1", tmp.toString)
	  }
	  
	  if (i%30 == 0) {
	  	fbUsers(i) ? UploadPhoto("2.png", tmp.toString)
	  	UpdatePhotos(i.toString, "2", tmp.toString)
	  } 
	  
	  if (i%50 == 0) {
	  	fbUsers(i) ? UploadPhoto("3.png", tmp.toString)
	  	UpdatePhotos(i.toString, "3", tmp.toString)
	  } 
	  
	  if (i%20 == 0) {
	  	fbUsers(i) ? AddFriend(i.toString)
	  }

    if (i%20 == 0) fbUsers(i) ? CreateComment("1")
  }

  Thread sleep(100)

  fbUsers(1) ! GetAlbum("1")
  fbUsers(4) ! GetAlbum("189")

  fbUsers(1) ! GetPhoto("1")
  fbUsers(1) ! GetPhoto("123")

  fbUsers(1) ! GetProfile("10")
  fbUsers(1) ! GetProfile("1023")

}
