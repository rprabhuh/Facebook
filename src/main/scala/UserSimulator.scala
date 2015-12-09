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
import javax.crypto._
import java.util.Base64
import java.security._

case class Start()
case class CreateAlbum()
case class UpdateAlbum(id: String)
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
case class UpdateComment(id: String, objId: String)
case class DeleteComment(id: String)
case class CreateStatus()
case class UpdateStatus(id: String)
case class DeleteStatus(id: String)
case class CreatePage()
case class GetPage(id: String)
case class UpdatePage(id: String)
case class DeletePage(id: String)

class UserSimulator(systemArg: ActorSystem) extends Actor {

  import system.dispatcher

  val system = systemArg
  val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
  var cipher = Cipher.getInstance("RSA")
  implicit val timeout = Timeout(10000)
  val pipeline:HttpRequest => Future[HttpResponse] = sendReceive ~> unmarshal[HttpResponse]


  // ENCRYPT using the PRIVATE key
  def encrypt(plaintext: Array[Byte]): String = {
	    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate())
	    val encryptedBytes = cipher.doFinal(plaintext)
	    return new String(Base64.getEncoder().encode(encryptedBytes))
  }  

  // DECRYPT using the PUBLIC key
  def decrypt(chipertext: String, publicKey: PublicKey): Array[Byte] = {
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        var ciphertextBytes = Base64.getDecoder().decode(chipertext.getBytes())
        return cipher.doFinal(ciphertextBytes)
  }


  def receive = { 
    case CreateAlbum =>
        println("User " + self.path.name + " creating an Album..")

        import FBJsonProtocol._

        val A = new Album(self.path.name, "null", 0, "33", "12:23:12", "description: String",
                          self.path.name, "link: String", "location: String", "name: String",
                          "place: String", "privacy: String", "null", Array("cover_photo"),"-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Album", A))
        response onComplete {
        	case Success(crAlbum) =>
        		println(crAlbum.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating album: " + error)
        }


      case GetAlbum(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " Getting an Album with id = " + id)

        val pipeline: HttpRequest => Future[Album] = (
          sendReceive
            ~> unmarshal[Album]
          )
        val response: Future[Album] = pipeline(Get("http://localhost:8080/Album?id=" + id))
        response onComplete {
        	case Success(album) =>
        		println("[ALBUM] Retrieved:")
        		println("id = " + album.id + "\n" +
            	"count = " + album.count + "\n" +
            	"cover_photo = " + album.cover_photo + "\n" +
            	"created_time = " + album.created_time + "\n" +
            	"description = " + album.description + "\n" +
            	"from = " + album.from + "\n" +
            	"link = " + album.link + "\n" +
            	"location = " + album.location + "\n" +
            	"name = " + album.name + "\n" +
            	"place = " + album.place + "\n" +
            	"privacy = " + album.privacy + "\n" +
            	"updated_time = " + album.updated_time + "\n" +
            	"OCid = " + album.OCid)
          		print("Photos = ")

        		for (i <- 0 until album.photos.size) {
            		print(album.photos(i) + "\t")
          		}
		        println("\n")

        	case Failure(error) =>
        		println("ERROR while retrieving album " + id +
        		 ": " + error)
        }


    case UpdateAlbum(id) =>
        println("User " + self.path.name + " Updating an Album with id = " + id)

        import FBJsonProtocol._

        var A = new Album(self.path.name, id, 12, "cover_photo", "time", "description: String",
          self.path.name, "link: String", "location: String", "name: String", "place: String",
          "privacy: String", "null", Array("cover_photo"), "1" )

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Album", A))
        response onComplete {
        	case Success(upAlbum) =>
        		println(upAlbum.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating album " + id + " : " + error)
        }


    case DeleteAlbum(id) =>
    	println("User " + self.path.name + " deleting Album " + id)
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Album?del_id=" + id))
		response onComplete {
        	case Success(deAlbum) =>
        		println(deAlbum.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting album " + id + " : " + error)
        }



      case UploadPhoto(id, album_id) =>

        println("User " + self.path.name + " uploading photo with " + id + 
            " to album " + album_id)

        var image = ImageIO.read(new File("media/" + id))
        var bytearraystream = new ByteArrayOutputStream()
        ImageIO.write(image, "png", bytearraystream)
        bytearraystream.flush()
        var bytearray = bytearraystream.toByteArray()
        bytearraystream.close()

        val list = Array("a","b","c")
        
        import FBJsonProtocol._
        var A = new Photo(self.path.name, "null", album_id, "created_time", self.path.name, bytearray,
                          "link", id, "updated_time", "place", list, list, "-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Photo", A))
        response onComplete {
        	case Success(upPhoto) =>
        		println(upPhoto.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting album " + id + " : " + error)
        }


     case GetPhoto(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " Getting a Photo with id = " + id)

        val pipeline: HttpRequest => Future[Photo] = (
          sendReceive
            ~> unmarshal[Photo]
          )
        val response: Future[Photo] = pipeline(Get("http://localhost:8080/Photo?id=" + id))
        response onComplete {
        	case Success(photo) =>
	        	println("id = " + photo.id + "\n" +
				            "album = " + photo.album + "\n" +
				            "created_time = " + photo.created_time + "\n" +
				            "from = " + photo.from + "\n" +
				            "link = " + photo.link + "\n" +
				            "name = " + photo.name + "\n" +
				            "updated_time = " + photo.updated_time + "\n" +
				            "place = " + photo.place + "\n" +
				            "OCid = " + photo.OCid)

	        	println("\n")

        	case Failure(error) =>
        		println("ERROR while retrieving photo " + id + " : " + error)
        }
        

    case DeletePhoto(id) => 
		  println("User " + self.path.name + " deleting photo " + id)
		  val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Photo?del_id=" + id))
		  response onComplete {
        	case Success(dePhoto) =>
        		println(dePhoto.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting photo " + id + " : " + error)
        }

      case AddFriend(id) =>
        println("User " + self.path.name + " sending friend request to " + id)

        import FBJsonProtocol._
        var A = new FriendReqest(self.path.name, self.path.name, id)
        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/AddFriend", A))
        response onComplete {
        	case Success(addFr) =>
        		println(addFr.entity.asString)

        	case Failure(error) =>
        		println("ERROR while sending friend request from " +
        			self.path.name + " to " + id + " : " + error)
        }

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
		response onComplete {
        	case Success(crProfile) =>
        		println(crProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating profile : " + error)
        }

	case GetProfile(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " Getting Profile = " + id)

        val pipeline: HttpRequest => Future[Profile] = (
          sendReceive
            ~> unmarshal[Profile]
          )
        val response: Future[Profile] = pipeline(Get("http://localhost:8080/Profile?id=" + id))
        response onComplete {
        	case Success(profile) =>
	        	println("id = " + profile.id + "\n" +
					"bio = " + profile.bio + "\n" +
					"birthday = " + profile.birthday + "\n" +
					"email = " + profile.email + "\n" +
					"first_name = " + profile.first_name + "\n" +
					"gender = " + profile.gender + "\n" +
					"hometown = " + profile.hometown + "\n" +
					"last_name = " + profile.last_name + "\n" +
					"link = " + profile.link + "\n" +
					"location = " + profile.location + "\n" +
					"middle_name = " + profile.middle_name + "\n" +
					"political = " + profile.political + "\n" +
					"relationship_status = " + profile.relationship_status + "\n" +
					"religion = " + profile.religion + "\n" +
					"significant_other = " + profile.significant_other + "\n" +
					"updated_time = " + profile.updated_time + "\n" +
					"website = " + profile.website + "\n" +
					"cover = " + profile.cover + "\n")
        		println("\n")

        	case Failure(error) =>
        		println("ERROR while retrieving profile " + id + " : " + error)     
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
		response onComplete {
        	case Success(upProfile) =>
        		println(upProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating profile " + self.path.name + 
        			" : " + error)
        }

	case DeleteProfile =>
		println("User " + self.path.name + " deleting Profile")
		val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Profile?del_id=" + self.path.name))
		response onComplete {
        	case Success(deProfile) =>
        		println(deProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting profile : " + error)
        }

	case CreateComment(objId) =>
		import FBJsonProtocol._
		println("User " + self.path.name + " commenting on Object " + objId)
		var C = new Comment("null", "object_id", "created_time", self.path.name,
			"This is the comment message!", "parent", Array("user_comments"),
			Array("user_likes"))
		val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Object", C))
		response onComplete {
        	case Success(crComment) =>
        		println(crComment.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating comment : " + error)
        }

  	case UpdateComment(id, objId) =>
  		import FBJsonProtocol._
    	println("User " + self.path.name + " updating comment " + id + 
              	" on Object " + objId)
    	var C = new Comment(id, objId, "created_time", self.path.name,
      	"This is the UPDATED comment message!", "parent", Array("user_comments"),
      	Array("user_likes"))
    	val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Object", C))  
    	response onComplete {
        	case Success(upProfile) =>
        		println(upProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating comment " + id + " : " + error)
        }

  case DeleteComment(id) => 
      println("User " + self.path.name + " deleting comment " + id)
      val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Comment?del_id=" + id))
      response onComplete {
        	case Success(deProfile) =>
        		println(deProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting comment " + id + " : " + error)
        }

  case CreateStatus =>
    	val status = "This is User " + self.path.name + "'s status"
    	println("User " + self.path.name + " creating status " + "\"" + status + "\"")
    	import FBJsonProtocol._
    	var S = new Status(self.path.name, "null", "now", self.path.name, "location", status,
                  	"time again", "-1")
    	val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Status", S))  
    	response onComplete {
        	case Success(crStatus) =>
        		println(crStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating status : " + error)
        }

  case UpdateStatus(id) =>
    	val status = "User " + self.path.name + " is changing his status"
    	println("User " + self.path.name + " is changing his status to " + "\"" + status + "\"")
    	import FBJsonProtocol._
    	var S = new Status(self.path.name, id, "now", self.path.name, "location", status,
                  	"time again", "-1")
    	val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Status", S))
    	response onComplete {
        	case Success(upStatus) =>
        		println(upStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating status " + id + " : " + error)
        }

  case DeleteStatus(id) => 
      println("User " + self.path.name + " deleting status " + id)
      val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Status?del_id=" + id))
      response onComplete {
        	case Success(deStatus) =>
        		println(deStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting status " + id + " : " + error)
        }

  case CreatePage =>
        println("User " + self.path.name + " creating an Page..")

        import FBJsonProtocol._

        var P = new Page(self.path.name, "null", "about", true, "cover", "description", Array("emails"),
                false, false, true, 12, "link", "location", "from", "name", "parent_page", Array("posts"),
                "phone", "last_used_time", Array("likes"), Array("members"), "-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Page", P))
        response onComplete {
        	case Success(crPage) =>
        		println(crPage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating page : " + error)
        }

  case GetPage(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " getting a Page with id = " + id)

        val pipeline: HttpRequest => Future[Page] = (
          sendReceive
            ~> unmarshal[Page]
          )
        val response: Future[Page] = pipeline(Get("http://localhost:8080/Page?id=" + id))
        response onComplete {
        	case Success(crPage) =>
		          println("auth = " + crPage.auth + "\n" +
		                  "id = " + crPage.id + "\n" +
		                  "about = " + crPage.about + "\n" +
		                  "can_post = " + crPage.can_post + "\n" +
		                  "cover = " + crPage.cover + "\n" +
		                  "description = " + crPage.description + "\n" +
		                  "is_community_page = " + crPage.is_community_page + "\n" +
		                  "is_permanently_closed = " + crPage.is_permanently_closed + "\n" +
		                  "is_published = " + crPage.is_published + "\n" +
		                  "like_count = " + crPage.like_count + "\n" +
		                  "link = " + crPage.link + "\n" +
		                  "location = " + crPage.location + "\n" +
		                  "from = " + crPage.from + "\n" +
		                  "name = " + crPage.name + "\n" +
		                  "parent_page = " + crPage.parent_page + "\n" +
		                  "phone = " + crPage.phone + "\n" +
		                  "last_used_time = " + crPage.last_used_time + "\n" +
		                  "OCid = " + crPage.OCid + "\n")

		          println("\n")

        	case Failure(error) =>
        		println("ERROR while retrieving page " + id + " : " + error)        
        }


    case UpdatePage(id) =>
        println("User " + self.path.name + " Updating an Page with id = " + id)

        import FBJsonProtocol._

        val P = new Page(self.path.name, id, "about", true, "cover", "description", Array("emails"),
                false, false, true, 12, "link", "location", "from", "name", "parent_page", Array("posts"),
                "phone", "last_used_time", Array("likes"), Array("members"), "-1")

        val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/Page", P))
        response onComplete {
        	case Success(upPage) =>
        		println(upPage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating page : " + error)
        }

    case DeletePage(id) =>
        println("User " + self.path.name + " deleting Page " + id)
        val response: Future[HttpResponse] = pipeline(Delete("http://localhost:8080/Page?del_id=" + id))
        response onComplete {
        	case Success(dePage) =>
        		println(dePage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting page : " + error)
        }

  }
}
