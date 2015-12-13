import akka.actor._
import akka.pattern.ask
import akka.io.IO
import spray.can.Http
import akka.util.Timeout

import ObjectType._


object Main extends App {
  override def main(args: Array[String]) { 

    //Take in the command line for the number of users.
    /*if(args.length != 1 || isAllDigits(args(0)) == false) {
      println("Error: Enter the network size");
      System.exit(1)
    }*/

    var NETWORK_SIZE = 5 //args(0).toInt

    implicit val system = ActorSystem("facebook")

    // the handler actcor replies to incoming HttpRequests
    val api = system.actorOf(Props[RestInterface], name = "handler")

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

      //Add some delay to allow for the system to be up
      println("Server starting up..")
      Thread sleep(5000)
      println("Server up!")

      var i = 0

      // Create actors for simulation
      var fbUsers: Array[ActorRef] = new Array[ActorRef](NETWORK_SIZE)

      for (i <- 0 until NETWORK_SIZE) {
        fbUsers(i) = system.actorOf(Props(new UserSimulator(system)), name = i.toString)
        
        //All the actors create their facebook profile
        fbUsers(i) ? CreateProfile
      }

      //Put in some sleep to allow for all the actors to create their facebook profiles
      Thread sleep(5000)
      println("\nProfiles created!")


      var numAlbumsCreated = 0

      //30% of users create albums
      for (i <- 0 until NETWORK_SIZE) {
        if (i%30 == 0)  { 
          fbUsers(i) ? CreateAlbum
          numAlbumsCreated += 1
        }
      }
      
      Thread sleep(500)
      fbUsers(1) ? CreateComment(ObjectType.ALBUM, "1")
      
      import scala.util.Random
      import scala.math.abs
      var R = new Random()
      var tmp: Int = 0
      for(i <- 1 until NETWORK_SIZE) {

      	/*import system.dispatcher
      	//This will schedule to send <message>
		//to the <actor> after 0ms repeating every 50ms
      	val cancellable = system.scheduler.schedule(0 milliseconds,
      		50 milliseconds, <actor>, <message>)*/

     //   if (i%10 == 0)
     //   Everyone Updates their statuses
          fbUsers(i) ! CreateComment(ObjectType.ALBUM, tmp.toString)

        if (numAlbumsCreated > 1)
          tmp = abs(R.nextInt()%numAlbumsCreated)
        if (tmp == 0) tmp = 1
        else
          tmp = 1

        //20% of users upload photos
        if (i%30 == 0) fbUsers(i) ? UploadPhoto("1.png", "1")

        if (i%30 == 0) fbUsers(i) ? UploadPhoto("2.png", tmp.toString)

        if (i%50 == 0) fbUsers(i) ? UploadPhoto("3.png", tmp.toString)

        //Everyone sends friend request to at least one other person
        //if (i !=0 && i%20 == 0) 
//        fbUsers(i) ? AddFriend((i-1).toString)

        if (i%30 == 0) 
          fbUsers(i) ! GetPage("1")

        //20% of users comment on stuff
        if (i%50 == 0) 
          fbUsers(i) ? CreateComment(ObjectType.ALBUM, tmp.toString)
        //10% of users comment on stuff
        if (i%1 == 0) 
          fbUsers(i) ! UpdateComment("1", ObjectType.ALBUM, "1")
        //50% of users delete their own comments
        if (i%50 == 0) 
          fbUsers(i) ! DeleteComment("2")
        //1% of users delete their own comment
        if (i%90 == 0) 
          fbUsers(i) ! DeleteComment(tmp.toString) 
        //1% of all users see albums that do not exist
        if (i%100 == 0) 
          fbUsers(i) ! GetAlbum("189783748374389")
        //10% of all users see other's albums
        if (i%50 == 0) 
          fbUsers(i) ! GetAlbum(tmp.toString) 
        //2% of users see other's photos that do not exist
        if (i%40 == 0) 
          fbUsers(i) ! GetPhoto("12379872834")
        //20% of users see other's photos
        if (i%30 == 0) 
          fbUsers(i) ! GetPhoto("1") 
        //2% of all users see other's profiles that do not exist
        if (i%50 == 0) 
          fbUsers(0) ! GetProfile("1023789748")
        //10% of users see their own profiles
        //if (i%10 == 0) 
          //fbUsers(i) ! GetProfile(((i+1)%NETWORK_SIZE).toString)
        //5% of users see other's profiles
        if (i != 0 && i%20 == 0) 
          fbUsers(i) ! GetProfile((i-10).toString)
        //10% of users update their profiles regularly
        if (i%10 == 0) 
          fbUsers(i) ! UpdateProfile
        //2% of users delete their profiles
        if (i%50 ==0) 
          fbUsers(i) ! DeleteProfile
        //All users update their statuses
          //fbUsers(3) ! GetStatus("1")
          //Thread sleep(500) 
          //fbUsers(1) ! UpdateStatus("1") 
        //2% users delete invalid status
        if (i%50 == 0) 
        fbUsers(0) ! DeleteStatus("1333987492")
        //5% of users update their statuses 
        if (i%20 == 0) 
          fbUsers(i) ! DeleteStatus("1")
        //5% of users create pages
        if (i%20 == 0) 
          fbUsers(i) ! CreatePage()
        //All users see pages
 //         fbUsers(i) ! CreatePage()
        //1% of users delete pages
        if (i%100 == 0) 
          fbUsers(i) ! DeletePage((i%100).toString)

        //Thread sleep(500)
        //fbUsers(i) ! GetProfile(((i+1)%NETWORK_SIZE).toString)
      }

      Thread sleep(1000)
      //fbUsers(2) ! GetStatus("1")
  }


  def isAllDigits(x: String) = x forall Character.isDigit
}
