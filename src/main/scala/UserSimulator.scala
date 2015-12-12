import scala.util.{Try, Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
import scala.math
import BigInt._


import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import spray.http.StatusCodes
import spray.can.Http.RegisterChunkHandler
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.AdditionalFormats

import HttpMethods._
import MediaTypes._

import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import java.io._
import java.io.ByteArrayOutputStream
import java.io.File
import java.awt.image.BufferedImage
import java.util.Base64
import java.math.BigInteger
import java.security._
import java.security.spec._
import java.security.Key
import java.security.spec._
import java.security.spec.X509EncodedKeySpec
import java.security.interfaces._

import javax.imageio.ImageIO
import javax.crypto._
import javax.crypto.spec._
import javax.crypto.spec.SecretKeySpec
import com.sun.crypto.provider.SunJCE
import javax.crypto.interfaces._

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
case class RequestPublicKey(publicKey: Array[Byte])
case class RSAPublicKey(dhKey: Array[Byte], rsaKey:Array[Byte])

class UserSimulator(systemArg: ActorSystem) extends Actor {
  var skip1024Base:BigInteger = BigInteger.valueOf(2);

  var skip1024ModulusBytes:Array[Byte] = Array(
        0xF4.toByte, 0x88.toByte, 0xFD.toByte, 0x58.toByte,
        0x4E.toByte, 0x49.toByte, 0xDB.toByte, 0xCD.toByte,
        0x20.toByte, 0xB4.toByte, 0x9D.toByte, 0xE4.toByte,
        0x91.toByte, 0x07.toByte, 0x36.toByte, 0x6B.toByte,
        0x33.toByte, 0x6C.toByte, 0x38.toByte, 0x0D.toByte,
        0x45.toByte, 0x1D.toByte, 0x0F.toByte, 0x7C.toByte,
        0x88.toByte, 0xB3.toByte, 0x1C.toByte, 0x7C.toByte,
        0x5B.toByte, 0x2D.toByte, 0x8E.toByte, 0xF6.toByte,
        0xF3.toByte, 0xC9.toByte, 0x23.toByte, 0xC0.toByte,
        0x43.toByte, 0xF0.toByte, 0xA5.toByte, 0x5B.toByte,
        0x18.toByte, 0x8D.toByte, 0x8E.toByte, 0xBB.toByte,
        0x55.toByte, 0x8C.toByte, 0xB8.toByte, 0x5D.toByte,
        0x38.toByte, 0xD3.toByte, 0x34.toByte, 0xFD.toByte,
        0x7C.toByte, 0x17.toByte, 0x57.toByte, 0x43.toByte,
        0xA3.toByte, 0x1D.toByte, 0x18.toByte, 0x6C.toByte,
        0xDE.toByte, 0x33.toByte, 0x21.toByte, 0x2C.toByte,
        0xB5.toByte, 0x2A.toByte, 0xFF.toByte, 0x3C.toByte,
        0xE1.toByte, 0xB1.toByte, 0x29.toByte, 0x40.toByte,
        0x18.toByte, 0x11.toByte, 0x8D.toByte, 0x7C.toByte,
        0x84.toByte, 0xA7.toByte, 0x0A.toByte, 0x72.toByte,
        0xD6.toByte, 0x86.toByte, 0xC4.toByte, 0x03.toByte,
        0x19.toByte, 0xC8.toByte, 0x07.toByte, 0x29.toByte,
        0x7A.toByte, 0xCA.toByte, 0x95.toByte, 0x0C.toByte,
        0xD9.toByte, 0x96.toByte, 0x9F.toByte, 0xAB.toByte,
        0xD0.toByte, 0x0A.toByte, 0x50.toByte, 0x9B.toByte,
        0x02.toByte, 0x46.toByte, 0xD3.toByte, 0x08.toByte,
        0x3D.toByte, 0x66.toByte, 0xA4.toByte, 0x5D.toByte,
        0x41.toByte, 0x9F.toByte, 0x9C.toByte, 0x7C.toByte,
        0xBD.toByte, 0x89.toByte, 0x4B.toByte, 0x22.toByte,
        0x19.toByte, 0x26.toByte, 0xBA.toByte, 0xAB.toByte,
        0xA2.toByte, 0x5E.toByte, 0xC3.toByte, 0x55.toByte,
        0xE9.toByte, 0x2F.toByte, 0x78.toByte, 0xC7.toByte
      )

  var skip1024Modulus:BigInteger   = new BigInteger(1, skip1024ModulusBytes);
  var dhSkipParamSpec = new DHParameterSpec(skip1024Modulus, skip1024Base); 
  
  import system.dispatcher

  //val pr = scala.math.pow(2,1536) - scala.math.pow(2,1472) - 1 + scala.math.pow(2,64) * ( scala.math.pow(2,1406* scala.math.Pi) + 741804 )
  val prime:BigInt = BigInt(BigInteger.valueOf(23))
  val generator:BigInt = 5
  val system = systemArg
  val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
  var cipher = Cipher.getInstance("RSA")
  implicit val timeout = Timeout(5000)
  val pipeline:HttpRequest => Future[HttpResponse] = sendReceive ~> unmarshal[HttpResponse]
  var future:Future[Any]= null


  // ENCRYPT using the PRIVATE key
  def rsaencrypt(plaintext: String): String = {
	    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate())
	    val encryptedBytes = cipher.doFinal(plaintext.getBytes)
	    return new String(Base64.getEncoder().encode(encryptedBytes))
  }  

  // DECRYPT using the PUBLIC key
  def rsadecrypt(chipertext: String, publicKey: PublicKey): Array[Byte] = {
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        var ciphertextBytes = Base64.getDecoder().decode(chipertext)
        return cipher.doFinal(ciphertextBytes)
  }

  def dhencrypt(key: BigInt): String = {
    val algorithm = "DES";
    val secretKey = new SecretKeySpec(key.toByteArray, algorithm)
    val encipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey)
    new String(encipher.doFinal(keyPair.getPublic().getEncoded))
  }

  def dhdecrypt(key: BigInt, msg: String): Array[Byte]= {
    val algorithm = "DES";
    val secretKey = new SecretKeySpec(key.toByteArray, algorithm)
    val encipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey)
    encipher.doFinal(msg.getBytes)
  }

  def generateDHKey(numBits: Int):BigInt= {
    val rand = scala.util.Random
    BigInt(numBits, rand)
  }

  def generateDHSharedKey(otherskey: BigInt): BigInt = {
    generator.modPow(otherskey, prime)
  }

  def receive = { 
    case RequestPublicKey(otherkey) =>
      var bobKeyFac = KeyFactory.getInstance("DH")
      var x509KeySpec = new X509EncodedKeySpec(otherkey)
      var alicePubKey = bobKeyFac.generatePublic(x509KeySpec)
      var dhParamSpec = (alicePubKey.asInstanceOf[DHPublicKey]).getParams()
      var bobKpairGen = KeyPairGenerator.getInstance("DH")
      bobKpairGen.initialize(dhParamSpec)
      var bobKpair = bobKpairGen.generateKeyPair() 
      
      var bobKeyAgree = KeyAgreement.getInstance("DH");
      bobKeyAgree.init(bobKpair.getPrivate()); 
      var bobPubKeyEnc:Array[Byte] = bobKpair.getPublic().getEncoded(); 
      bobKeyAgree.doPhase(alicePubKey, true);

      var bobSharedKey:Array[Byte] = bobKeyAgree.generateSecret(); 
      bobKeyAgree.doPhase(alicePubKey, true); 
      
      var bobDesKey = bobKeyAgree.generateSecret("DES"); 
      var bobCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey); 
      
      var cleartext:Array[Byte] = keyPair.getPublic.getEncoded();
      var ciphertext:Array[Byte] = bobCipher.doFinal(cleartext); 
      
      sender ! RSAPublicKey(bobPubKeyEnc, ciphertext)

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
                          "link", rsaencrypt(id), "updated_time", "place", list, list, "-1")

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
                println("GET PHOTO SUCCESS CASE")
                val fromUser = context.actorFor("akka://facebook/user/" + photo.from)
                var aliceKpairGen = KeyPairGenerator.getInstance("DH");
                aliceKpairGen.initialize(dhSkipParamSpec);
                var aliceKpair = aliceKpairGen.generateKeyPair();
                var aliceKeyAgree = KeyAgreement.getInstance("DH");
                aliceKeyAgree.init(aliceKpair.getPrivate());
                var alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
                
                future = fromUser ? RequestPublicKey(alicePubKeyEnc)
                var otherkey = Await.result(future, timeout.duration).asInstanceOf[RSAPublicKey]
                var aliceKeyFac = KeyFactory.getInstance("DH");
                var x509KeySpec = new X509EncodedKeySpec(otherkey.dhKey);
                var bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
                aliceKeyAgree.doPhase(bobPubKey, true); 


                var aliceSharedSecret:Array[Byte] = aliceKeyAgree.generateSecret() 
                aliceKeyAgree.doPhase(bobPubKey, true);
                var aliceDesKey = aliceKeyAgree.generateSecret("DES");
                
                var aliceCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
                aliceCipher.init(Cipher.DECRYPT_MODE, aliceDesKey);
                var recovered:Array[Byte] = aliceCipher.doFinal(otherkey.rsaKey);


                var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(recovered));

                var decryptedname = new String (rsadecrypt(photo.name, publicKey))
                println("Decrypted Photo name is " + decryptedname)

                println("Decryption Successful")
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
