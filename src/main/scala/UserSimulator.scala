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
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import java.io.File

case class Start()
case class CreateAlbum()
case class UpdateAlbum()
case class GetAlbum(id: String)
case class DeleteAlbum(id: String)
case class UploadPhoto(id: String, album_id: String)
case class DeletePhoto(id: String)
case class AddFriend(id:String)
case class CreateProfile()
case class UpdateProfile()
case class DeleteProfile(id: String)


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
            self.path.name,
            "link: String",
            "location: String",
            "name: String",
            "place: String",
            "privacy: String",
            "null",                           // updated_time
            Array("cover_photo"), "-1")

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
            self.path.name,
            "link: String",
            "location: String",
            "name: String",
            "place: String",
            "privacy: String",
            "null",                           // updated_time
            Array("cover_photo"), "1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Album", A))
        println("RESPONSE: " + response)

      case DeleteAlbum(id) =>
		println("User " + self.path.name + " deleting Album " + id)
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Album?del_id=" + id))



      case UploadPhoto(id, album_id) =>
        var image = ImageIO.read(new File("media/" + id))
        var bytearraystream = new ByteArrayOutputStream()
        ImageIO.write(image, "png", bytearraystream)
        bytearraystream.flush()
        var bytearray = bytearraystream.toByteArray()
        bytearraystream.close()

        val list = Array("a","b","c")
        
        import FBJsonProtocol._
        var A = new Photo("null",
          album_id,
          "created_time",
          "from",
          bytearray,
          "link",
          "name",
          "updated_time",
          "place", 
          list,
          list, "-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Photo", A))
        println("RESPONSE: " + response)

      case DeletePhoto(id) => 
		println("User " + self.path.name + " deleting photo " + id)
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Photo", id))


      case AddFriend(id) =>
        import FBJsonProtocol._
        var A = new FriendReqest(self.path.name, id)
        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/AddFriend", A))


      case CreateProfile =>
		println("User " + self.path.name + " creating a profile")

		import FBJsonProtocol._
		var P = new Profile (self.path.name, "bio", "birthday",
			Array("education"), "email", "first_name", "gender", "hometown",
			Array("interested_in"), Array("languages"), "last_name", "link",
			"location", "middle_name", "political", "relationship_status",
			"religion", "significant_other", "updated_time", "website",
			Array("work"), "cover")

		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Profile", P))


	case UpdateProfile =>
		println("User " + self.path.name + " updating profile")

		import FBJsonProtocol._
		var P = new Profile (self.path.name, "bio", "birthday",
			Array("education"), "email", "first_name", "gender", "hometown",
			Array("interested_in"), Array("languages"), "last_name", "link",
			"location", "middle_name", "political", "relationship_status",
			"religion", "significant_other", "updated_time", "website",
			Array("work"), "cover")


		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Profile", P))


	case DeleteProfile =>
		println("User " + self.path.name + " deleting Profile")
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Profile", self.path.name))

  }
}
