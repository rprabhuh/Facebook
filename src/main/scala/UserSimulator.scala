import scala.util.{Try, Success, Failure}
import spray.http.StatusCodes
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
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._


case class Start()
case class CreateAlbum()
case class UpdateAlbum()
case class GetAlbum(id: String)
case class DeleteAlbum(id: String)
case class UploadPhoto(id: String, album_id: String)
case class GetPhoto(id: String)
case class DeletePhoto(id: String)
case class AddFriend(id:String)
case class CreateProfile()
case class GetProfile(id: String)
case class UpdateProfile()
case class DeleteProfile(id: String)
case class CreateComment(objId: String)

class UserSimulator(systemArg: ActorSystem) extends Actor {

  val system = systemArg
  import system.dispatcher
  implicit val timeout = Timeout(1000000)
  val pipeline:HttpRequest => Future[HttpResponse] = sendReceive ~> unmarshal[HttpResponse]


  //val pipeline2: HttpRequest => Future[HttpResponse] = sendReceive

  def receive = { 
    case Start =>
    case CreateAlbum =>
        println("Creating an Album..")

        import FBJsonProtocol._

        val A = new Album(
            self.path.name,
            "null",
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
        //println("RESPONSE: " + response)


      case GetAlbum(id) =>
        import FBJsonProtocol._
        println("Getting an Album..")
        //val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/Album?id=" + id))
        //val pipeline:HttpRequest => Future[Album] = sendReceive ~> unmarshal[Album]
        val pipeline: HttpRequest => Future[Album] = (
          sendReceive
            ~> unmarshal[Album]
          )
        val response: Future[Album] = pipeline(Get("http://localhost:8080/Album?id=" + id))
        var A: Album = Await.result(response, timeout.duration).asInstanceOf[Album]
        if (A.id == "-1")
          println("ERROR: " + A.description)
        else {
          println("id = " + A.id + "\n" +
            "count = " + A.count + "\n" +
            "cover_photo = " + A.cover_photo + "\n" +
            "created_time = " + A.created_time + "\n" +
            "description = " + A.description + "\n" +
            "from = " + A.from + "\n" +
            "link = " + A.link + "\n" +
            "location = " + A.location + "\n" +
            "name = " + A.name + "\n" +
            "place = " + A.place + "\n" +
            "privacy = " + A.privacy + "\n" +
            "updated_time = " + A.updated_time + "\n" +
            "OCid = " + A.OCid)
          print("Photos = ")

          for (i <- 0 until A.photos.size) {
            print(A.photos(i) + "\t")
          }
          println("\n")
        }

/*        response.onComplete {
          case Success(_) =>
          case Failure(_) =>
          case StatusCodes.NotFound => println("Default")
        }*/


      case UpdateAlbum =>
        println("Updating an Album.."	)

        import FBJsonProtocol._

        var A = new Album(
            self.path.name,
            "null",
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
        //println("RESPONSE: " + response)

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
        var A = new Photo(
          "from",
          "null",
          album_id,
          "created_time",
          "from",
          bytearray,
          "link",
          id,
          "updated_time",
          "place", 
          list,
          list, "-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Photo", A))
        //println("RESPONSE: " + response)

     case GetPhoto(id) =>
        import FBJsonProtocol._
        println("Getting a Photo..")

        val pipeline: HttpRequest => Future[Photo] = (
          sendReceive
            ~> unmarshal[Photo]
          )
        val response: Future[Photo] = pipeline(Get("http://localhost:8080/Photo?id=" + id))
        val P: Photo = Await.result(response, timeout.duration).asInstanceOf[Photo]
        if (P.id == "-1")
        	println("ERROR: " + P.name)
        else {
        	println("id = " + P.id + "\n" +
			"album = " + P.album + "\n" +
			"created_time = " + P.created_time + "\n" +
			"from = " + P.from + "\n" +
			"link = " + P.link + "\n" +
			"name = " + P.name + "\n" +
			"updated_time = " + P.updated_time + "\n" +
			"place = " + P.place + "\n" +
			"OCid = " + P.OCid)
        
/*        	print("user_comments = ")
        	for (i <- 0 until P.user_comments.size)
        		print(P.user_comments(i) + "\t")

        	print("user_likes = ")
        	for (i <- 0 until P.user_likes.size)
        		print(P.user_likes(i) + "\t")*/

        	println("\n")
        }
        

      case DeletePhoto(id) => 
		println("User " + self.path.name + " deleting photo " + id)
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Photo", id))


      case AddFriend(id) =>
        import FBJsonProtocol._
        var A = new FriendReqest(self.path.name, self.path.name, id)
        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/AddFriend", A))

    case CreateProfile =>
		 println("User " + self.path.name + " creating a profile")

		 import FBJsonProtocol._
		 val P = new Profile (self.path.name, self.path.name, "bio", "birthday",
			   "email", "first_name", "gender", "hometown",
			   Array("interested_in"), Array("languages"), "last_name", "link",
			  "location", "middle_name", "political", "relationship_status",
			  "religion", "significant_other", "updated_time", "website",
			  Array("work"), "cover")

		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Profile", P))

	case GetProfile(id) =>
        import FBJsonProtocol._
        println("Getting a Profile..")

        val pipeline: HttpRequest => Future[Profile] = (
          sendReceive
            ~> unmarshal[Profile]
          )
        val response: Future[Profile] = pipeline(Get("http://localhost:8080/Profile?id=" + id))
        val P: Profile = Await.result(response, timeout.duration).asInstanceOf[Profile]
        if (P.id == "-1")
        	println("ERROR: " + P.bio)
        else {
        	println("id = " + P.id + "\n" +
				"bio = " + P.bio + "\n" +
				"birthday = " + P.birthday + "\n" +
				"email = " + P.email + "\n" +
				"first_name = " + P.first_name + "\n" +
				"gender = " + P.gender + "\n" +
				"hometown = " + P.hometown + "\n" +
				"last_name = " + P.last_name + "\n" +
				"link = " + P.link + "\n" +
				"location = " + P.location + "\n" +
				"middle_name = " + P.middle_name + "\n" +
				"political = " + P.political + "\n" +
				"relationship_status = " + P.relationship_status + "\n" +
				"religion = " + P.religion + "\n" +
				"significant_other = " + P.significant_other + "\n" +
				"updated_time = " + P.updated_time + "\n" +
				"website = " + P.website + "\n" +
				"cover = " + P.cover + "\n")
        
        }

	case UpdateProfile =>
		println("User " + self.path.name + " updating profile")

		import FBJsonProtocol._
		val P = new Profile (self.path.name, self.path.name, "bio", "birthday",
			"email", "first_name", "gender", "hometown",
			Array("interested_in"), Array("languages"), "last_name", "link",
			"location", "middle_name", "political", "relationship_status",
			"religion", "significant_other", "updated_time", "website",
			Array("work"), "cover")


		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Profile", P))


	case DeleteProfile =>
		println("User " + self.path.name + " deleting Profile")
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Profile", self.path.name))

	case CreateComment(objId) =>
	import FBJsonProtocol._
		println("User " + self.path.name + " commenting on Object " + objId)
		var C = new Comment("null", "object_id", "created_time", "from",
			"This is the comment message!", "parent", Array("user_comments"),
			Array("user_likes"))
		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Object", C))	


  }
}
