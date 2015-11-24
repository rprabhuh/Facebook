import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler
import spray.client.pipelining._
import scala.concurrent.Future
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.AdditionalFormats

case class Start()
case class CreateAlbum()
case class UpdateAlbum()
case class GetAlbum(id: String)

class UserSimulator(systemArg: ActorSystem) extends Actor {

  val system = systemArg
  import system.dispatcher
  val pipeline:HttpRequest => Future[HttpResponse] = sendReceive ~> unmarshal[HttpResponse]
  //val pipeline2: HttpRequest => Future[HttpResponse] = sendReceive

  	def receive = {

  		case Start =>


      case CreateAlbum =>
        println("Creating an Album..")

        import FBJsonProtocol._

        var A = new Album("null",
            0,
            "33",
            "12:23:12",
            "description: String",
            "from: String",
            "link: String",
            "location: String",
            "name: String",
            "place: String",
            "privacy: String",
            "null",                           // updated_time
            self.hashCode().toString)

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Album", A))
        println("RESPONSE: " + response)


      case GetAlbum(id) =>
        import FBJsonProtocol._
        println("Getting an Album..")
        val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/Album?id=" + id))
        println("RESPONSE: " + response)


      case UpdateAlbum =>
        println("Updating an Album..")

        import FBJsonProtocol._

        var A = new Album("null",
            0,
            "33",
            "12:23:12",
            "description: String",
            "from: String",
            "link: String",
            "location: String",
            "name: String",
            "place: String",
            "privacy: String",
            "null",                           // updated_time
            self.hashCode().toString)

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Album", A))
        println("RESPONSE: " + response)


  			
  }
}
