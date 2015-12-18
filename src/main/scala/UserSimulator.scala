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
import java.io.ByteArrayInputStream
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

import ObjectType._


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
case class CreateComment(objType: ObjectType, objId: String)
case class UpdateComment(id: String, objType: ObjectType, objId: String)
case class DeleteComment(id: String)
case class GetStatus(id:String)
case class CreateStatus()
case class UpdateStatus(id: String)
case class DeleteStatus(id: String)
case class CreatePage()
case class GetPage(id: String)
case class UpdatePage(id: String)
case class DeletePage(id: String)
case class RequestPublicKey(publicKey: Array[Byte])
case class RSAPublicKey(dhKey: Array[Byte], rsaKey:Array[Byte])
case class AliceKey(enc: Array[Byte], agree: KeyAgreement)
case class Encrypted(data: Array[Byte], key: Array[Byte])


class UserSimulator(systemArg: ActorSystem) extends Actor {
  var myAuthString = ""
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
  var AESCipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
  var cipher = Cipher.getInstance("RSA")
  implicit val timeout = Timeout(5000)
  val pipeline:HttpRequest => Future[HttpResponse] = sendReceive ~> unmarshal[HttpResponse]
  var future:Future[Any]= null
  val url = "https://localhost:8080/"

  // ENCRYPT using the PRIVATE key
  def rsaencrypt(plaintext: Array[Byte], AESStringKey: String): Encrypted = {
      var keySpec = new DESKeySpec(AESStringKey.getBytes("UTF-8"))
      var keyFactory = SecretKeyFactory.getInstance("DES");
      var AESKey = keyFactory.generateSecret(keySpec)
      AESCipher.init(Cipher.ENCRYPT_MODE, AESKey)
	    var encryptedBytes = AESCipher.doFinal(plaintext)

      val encryptedData = Base64.getEncoder().encode(encryptedBytes)

      cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate())
      encryptedBytes = cipher.doFinal(AESKey.getEncoded())
      val encryptedKey = Base64.getEncoder().encode(encryptedBytes)

      return new Encrypted(encryptedData, encryptedKey)
  }  

  // DECRYPT using the PUBLIC key
  def rsadecrypt(ciphertext: Array[Byte], pubKey: Array[Byte], publicKey: PublicKey): Array[Byte] = {
        cipher.init(Cipher.DECRYPT_MODE, publicKey)
        var keyBytes = Base64.getDecoder().decode(pubKey)
        val AESKeyBytes = cipher.doFinal(keyBytes)
        
        var keySpec = new DESKeySpec(AESKeyBytes)
        var kf = SecretKeyFactory.getInstance("DES");
        var AESKey = kf.generateSecret(keySpec);

        AESCipher.init(Cipher.DECRYPT_MODE, AESKey)
        return AESCipher.doFinal(Base64.getDecoder().decode(ciphertext))
        //return AESCipher.doFinal(ciphertext)
  }

  def generatePubKey(): AliceKey= {
    var aliceKpairGen = KeyPairGenerator.getInstance("DH");
    aliceKpairGen.initialize(dhSkipParamSpec);
    var aliceKpair = aliceKpairGen.generateKeyPair();
    var aliceKeyAgree = KeyAgreement.getInstance("DH");
    aliceKeyAgree.init(aliceKpair.getPrivate());
    var alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
    AliceKey(alicePubKeyEnc, aliceKeyAgree)
  }

  def generateSecretKey(otherkey:RSAPublicKey, aliceKeyAgree:KeyAgreement): PublicKey= {
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
                
    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(recovered));
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

        val A = new Album(myAuthString, "null", 0, "33", "12:23:12", "description: String",
                          self.path.name, "link: String", "location: String", "name: String",
                          "place: String", Array(self.path.name,"1","2","3"), "null", Array("cover_photo"),"-1")

        val response: Future[HttpResponse] = pipeline(Post(url + "Album", A))
        response onComplete {
        	case Success(crAlbum) =>
              println("Successfully Created album")
        		println(crAlbum.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating album: " + error)
        }


      case GetAlbum(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " Getting an Album with id = " + id)

        val request = new GetRequest(myAuthString, self.path.name, id)
        val pipeline: HttpRequest => Future[Album] = (
          sendReceive
            ~> unmarshal[Album]
          )
        val response: Future[Album] = pipeline(Get(url + "Album",request))
        response onComplete {
        	case Success(album) =>
                println("Printed Once")
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

        var A = new Album(myAuthString, id, 12, "cover_photo", "time", "description: String",
          self.path.name, "link: String", "location: String", "name: String", "place: String",
          Array(self.path.name,"1","2","3"), "null", Array("cover_photo"), "1" )

        val response: Future[HttpResponse] = pipeline(Post(url + "Album", A))
        response onComplete {
        	case Success(upAlbum) =>
        		println(upAlbum.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating album " + id + " : " + error)
        }


    case DeleteAlbum(id) =>
    	println("User " + self.path.name + " deleting Album " + id)
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, id)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Album",request))
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

        val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
        val enc = rsaencrypt(bytearray, AESStringKey)

        import FBJsonProtocol._
        var A = new Photo(myAuthString, "null", album_id, "created_time", self.path.name, enc.data,
                          "link", id, "updated_time", "place", list, list, "-1", enc.key,
                          Array(self.path.name, "1", "2","3"))

        val response: Future[HttpResponse] = pipeline(Post(url + "Photo", A))
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
        val request = new GetRequest(myAuthString, self.path.name, id)
        val response: Future[Photo] = pipeline(Get(url + "Photo",request))
        response onComplete {
        	case Success(photo) =>
                println("GET PHOTO SUCCESS CASE")

                var publicKey: PublicKey = null
                if (photo.from != self.path.name) {
                  println(self.path.name + " requesting publicKey from " + photo.from)
                  val fromUser = context.actorFor("akka://facebook/user/" + photo.from)
                  var tempobject =  generatePubKey()
                  var alicePubKeyEnc = tempobject.enc
                  var aliceKeyAgree = tempobject.agree
                  future = fromUser ? RequestPublicKey(alicePubKeyEnc)
                  var otherkey = Await.result(future, timeout.duration).asInstanceOf[RSAPublicKey]
                  publicKey = generateSecretKey(otherkey, aliceKeyAgree)
                } else {
                  println(self.path.name + " accessing his own publicKey")
                  publicKey = keyPair.getPublic()
                }

                var decrypted = rsadecrypt(photo.image, photo.encKey, publicKey)
                publicKey = null
                //println("Decrypted Photo name is  " + new String(decrypted))  

                println("Decryption Successful")

                val in: InputStream = new ByteArrayInputStream(decrypted);
                val bImageFromConvert = ImageIO.read(in);
                ImageIO.write(bImageFromConvert, "png", new File("retrieved/" + photo.name));

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
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, id)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Photo",request))
		  response onComplete {
        	case Success(dePhoto) =>
        		println(dePhoto.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting photo " + id + " : " + error)
        }

      case AddFriend(id) =>
        println("User " + self.path.name + " sending friend request to " + id)

        import FBJsonProtocol._
        var A = new FriendReqest(myAuthString, self.path.name, id)
        val response: Future[HttpResponse] = pipeline(Post(url + "AddFriend", A))
        response onComplete {
        	case Success(addFr) =>
        		println(addFr.entity.asString)

        	case Failure(error) =>
        		println("ERROR while sending friend request from " +
        			self.path.name + " to " + id + " : " + error)
        }

    case CreateProfile =>
      val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
      var encBDay = rsaencrypt("03/04/1990".getBytes, AESStringKey)

      var emailAddr = "user" + self.path.name + "@profile.com"
      var encEmail = rsaencrypt(emailAddr.getBytes, AESStringKey)

      println("User " + self.path.name + " creating a profile with encrypted birthday " + 
              new String(encBDay.data) + " and encrypted email " + new String(encEmail.data))
      
      import FBJsonProtocol._
      val P = new Profile (myAuthString, self.path.name, "bio",
          new String(encBDay.data), new String(encEmail.data),
			    "first_name", "gender", "hometown",
			    Array("interested_in"), Array("languages"), "last_name", "link",
			    "location", "middle_name", "relationship_status", "significant_other",
          "updated_time", "website", "cover", encEmail.key, Array(self.path.name,"1","2","3"))

		  val response: Future[HttpResponse] = pipeline(Post(url + "Profile", P))
		  response onComplete {
        	case Success(crProfile) =>
              myAuthString = crProfile.entity.asString
              println("Facebook Profile successfully created for " + self.path.name)

        	case Failure(error) =>
        		println("ERROR while creating profile : " + error)
        }

	case GetProfile(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " Getting Profile = " + id)
        val request = new GetRequest(myAuthString, self.path.name, id)
        
        val pipeline: HttpRequest => Future[Profile] = (
          sendReceive
            ~> unmarshal[Profile]
          )
        val response: Future[Profile] = pipeline(Get(url + "Profile",request))
        response onComplete {
        	case Success(profile) =>
              println("GET PROFILE SUCCESS CASE")

              var publicKey: PublicKey = null
              if (profile.id != self.path.name) {
                println(self.path.name + " requesting publicKey from " + profile.id)
                val fromUser = context.actorFor("akka://facebook/user/" + profile.id)
                var tempobject =  generatePubKey()
                var alicePubKeyEnc = tempobject.enc
                var aliceKeyAgree = tempobject.agree
                future = fromUser ? RequestPublicKey(alicePubKeyEnc)
                var otherkey = Await.result(future, timeout.duration).asInstanceOf[RSAPublicKey]
                publicKey = generateSecretKey(otherkey, aliceKeyAgree)
              }
              else {
                println(self.path.name + " accessing his own publicKey")
                publicKey = keyPair.getPublic()
              }

              var decryptedBdy = rsadecrypt(profile.birthday.getBytes, profile.encKey, publicKey)
              var decryptedEmail = rsadecrypt(profile.email.getBytes, profile.encKey, publicKey)
              publicKey = null

              println("Birthday: " + profile.birthday + " --> " + new String(decryptedBdy))
              println("Email: " + profile.email + " --> " + new String(decryptedEmail))

	        	  println("id = " + profile.id + "\n" +
					             "bio = " + profile.bio + "\n" +
					             "birthday = " + new String(decryptedBdy) + "\n" +
					             "email = " + new String(decryptedEmail) + "\n" +
					             "first_name = " + profile.first_name + "\n" +
					             "gender = " + profile.gender + "\n" +
					             "hometown = " + profile.hometown + "\n" +
					             "last_name = " + profile.last_name + "\n" +
					             "link = " + profile.link + "\n" +
					             "location = " + profile.location + "\n" +
					             "middle_name = " + profile.middle_name + "\n" +
					             "relationship_status = " + profile.relationship_status + "\n" +
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
        val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
        var encBDay = rsaencrypt("04/03/1990".getBytes, AESStringKey)

        var emailAddr = "profile" + self.path.name + "@user.com"
        var encEmail = rsaencrypt(emailAddr.getBytes, AESStringKey) 
        println("User " + self.path.name + " updating a profile with encrypted birthday " + 
          new String(encBDay.data) + " and encrypted email " + new String(encEmail.data))

		import FBJsonProtocol._
		val P = new Profile (myAuthString, self.path.name, "bio", new String(encBDay.data),
			new String(encEmail.data), "first_name", "gender", "hometown",
			Array("interested_in"), Array("languages"), "last_name", "link",
			"location", "middle_name", "relationship_status", "significant_other",
      "updated_time", "website", "cover", encEmail.key, Array(self.path.name,"1","2","3"))


		val response: Future[HttpResponse] = pipeline(Post(url + "Profile", P))
		response onComplete {
        	case Success(upProfile) =>
        		println(upProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating profile " + self.path.name + 
        			" : " + error)
        }

	case DeleteProfile =>
		println("User " + self.path.name + " deleting Profile")
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, self.path.name)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Profile",request))
		response onComplete {
        	case Success(deProfile) =>
        		println(deProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting profile : " + error)
        }

	case CreateComment(objType, objId) =>
		import FBJsonProtocol._

    val comment = "This is " + self.path.name + "'s comment message!"
    val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
    var encCom = rsaencrypt(comment.getBytes, AESStringKey)

      println("User " + self.path.name + " creating a comment: " + comment +
                " --> encrypted to " + new String(encCom.data))

		println("User " + self.path.name + " commenting on Object " + objId)
		var C = new Comment("null", objId, "created_time", self.path.name,
			new String(encCom.data), "parent", Array("user_comments"),
			Array("user_likes"), encCom.key)

    var response: Future[HttpResponse] = null
    objType match {
      case ObjectType.ALBUM => 
		    response = pipeline(Post(url + "Album/comment", C))

      case ObjectType.PHOTO => 
        response = pipeline(Post(url + "Photo/comment", C))

      case ObjectType.PAGE => 
        response = pipeline(Post(url + "Page/comment", C))
    }
    

		response onComplete {
        	case Success(crComment) =>
        		println(crComment.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating comment : " + error)
        }

  	case UpdateComment(id, objType, objId) =>
  		import FBJsonProtocol._
      val comment = "This is " + self.path.name + "'s updated comment message!"
      val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
      var encCom = rsaencrypt(comment.getBytes, AESStringKey)

      println("User " + self.path.name + " updating comment" + id + ": " + comment +
                " --> encrypted to " + new String(encCom.data))
    	
      var C = new Comment(id, objId, "created_time", self.path.name,
      	new String(encCom.data), "parent", Array("user_comments"),
      	Array("user_likes"), encCom.key)

    	var response: Future[HttpResponse] = null
      objType match {
        case ObjectType.ALBUM => 
          response = pipeline(Post(url + "Album/comment", C))

        case ObjectType.PHOTO => 
          response = pipeline(Post(url + "Photo/comment", C))

        case ObjectType.PAGE => 
          response = pipeline(Post(url + "Page/comment", C))
      }
    	
      response onComplete {
        	case Success(upProfile) =>
        		println(upProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating comment " + id + " : " + error)
        }

  case DeleteComment(id) => 
      println("User " + self.path.name + " deleting comment " + id)
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, id)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Comment",request))
      response onComplete {
        	case Success(deProfile) =>
        		println(deProfile.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting comment " + id + " : " + error)
        }

  case GetStatus(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " getting status = " + id)

        val request = new GetRequest(myAuthString, self.path.name, id)
        val pipeline: HttpRequest => Future[Status] = (
          sendReceive
            ~> unmarshal[Status]
          )

        val response: Future[Status] = pipeline(Get(url + "Status", request))
        response onComplete {
          case Success(status) =>
              println("GET STATUS SUCCESS CASE")

              var publicKey: PublicKey = null
              if (status.from != self.path.name) {
                println(self.path.name + " requesting publicKey from " + status.from)
                val fromUser = context.actorFor("akka://facebook/user/" + status.from)
                var tempobject =  generatePubKey()
                var alicePubKeyEnc = tempobject.enc
                var aliceKeyAgree = tempobject.agree
                future = fromUser ? RequestPublicKey(alicePubKeyEnc)
                var otherkey = Await.result(future, timeout.duration).asInstanceOf[RSAPublicKey]
                publicKey = generateSecretKey(otherkey, aliceKeyAgree)
              }
              else {
                println(self.path.name + " accessing his own publicKey")
                publicKey = keyPair.getPublic()
              }

              var decrypted = rsadecrypt(status.message.getBytes, status.encKey, publicKey)
              publicKey = null
              println("Status: " + status.message + " --> " + new String(decrypted))

              println("id = " + status.id + "\n" +
                       "message = " + new String(decrypted) + "\n" +
                       "updated_time = " + status.updated_time + "\n")
            println("\n")

          case Failure(error) =>
            println("ERROR while retrieving status " + id + " : " + error)     
        }

  case CreateStatus =>
    	val status = "This is User " + self.path.name + "'s status"
      val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
      var encStatus = rsaencrypt(status.getBytes, AESStringKey)

      println("User " + self.path.name + " creating a status: " + status +
                " --> encrypted to " + new String(encStatus.data))

    	import FBJsonProtocol._
    	var S = new Status(myAuthString, "null", "now", self.path.name, "location",
                    new String(encStatus.data),
                  	"time again", "-1", encStatus.key, Array(self.path.name,"1","2","3"))
    	
      val response: Future[HttpResponse] = pipeline(Post(url + "Status", S))  
    	response onComplete {
        	case Success(crStatus) =>
        		println(crStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating status : " + error)
        }

  case UpdateStatus(id) =>
    val status = "User " + self.path.name + " is updating his status"
    val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
    var encStatus = rsaencrypt(status.getBytes, AESStringKey)

    println("User " + self.path.name + " updating status to: " + status +
                " --> encrypted to " + new String(encStatus.data))

    import FBJsonProtocol._
    var S = new Status(myAuthString, "null", "now", self.path.name, "location",
                    new String(encStatus.data),
                  	"time again", "-1", encStatus.key, Array(self.path.name,"1","2","3"))

    	val response: Future[HttpResponse] = pipeline(Post(url + "Status", S))
    	response onComplete {
        	case Success(upStatus) =>
        		println(upStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating status " + id + " : " + error)
        }

  case DeleteStatus(id) => 
      println("User " + self.path.name + " deleting status " + id)
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, id)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Status",request))
      response onComplete {
        	case Success(deStatus) =>
        		println(deStatus.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting status " + id + " : " + error)
        }

  case CreatePage =>
        val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
        var encEmail = rsaencrypt("email@organization.com".getBytes, AESStringKey)
        
        println("User " + self.path.name + " creating a Page with encrypted email " + encEmail.data)
        println("Sending CreatePage Request")
        import FBJsonProtocol._
        var P = new Page(myAuthString, "null", "about", true, "cover", "description",
                new String(encEmail.data),
                12, "link", "location", self.path.name, "name", "parent_page",
                Array("likes"), Array(self.path.name,"1","2","3"), "-1", encEmail.key)

        val response: Future[HttpResponse] = pipeline(Post(url + "Page", P))
        response onComplete {
        	case Success(crPage) =>
              println("Page created successfully")
        		println(crPage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while creating page : " + error)
        }

  case GetPage(id) =>
        import FBJsonProtocol._
        println("User " + self.path.name + " getting a Page with id = " + id)

        val request = new GetRequest(myAuthString, self.path.name, id)
        val pipeline: HttpRequest => Future[Page] = (
          sendReceive
            ~> unmarshal[Page]
          )
        val response: Future[Page] = pipeline(Get(url + "Page",request ))
        response onComplete {
        	case Success(page) =>
              println("GET PAGE SUCCESS CASE")

              val fromUser = context.actorFor("akka://facebook/user/" + page.from)
              var tempobject =  generatePubKey()
              var alicePubKeyEnc = tempobject.enc
              var aliceKeyAgree = tempobject.agree
              future = fromUser ? RequestPublicKey(alicePubKeyEnc)
              var otherkey = Await.result(future, timeout.duration).asInstanceOf[RSAPublicKey]
              var publicKey = generateSecretKey(otherkey, aliceKeyAgree)

              // DECRYPT EMAIL
              var decrypted = rsadecrypt(page.email.getBytes, page.encKey, publicKey)
              publicKey = null
              
		          println("auth = " + page.auth + "\n" +
		                  "id = " + page.id + "\n" +
		                  "about = " + page.about + "\n" +
		                  "can_post = " + page.can_post + "\n" +
		                  "cover = " + page.cover + "\n" +
		                  "description = " + page.description + "\n" +
                      "email = " + new String(decrypted) + "\n" +
		                  "like_count = " + page.like_count + "\n" +
		                  "link = " + page.link + "\n" +
		                  "location = " + page.location + "\n" +
		                  "from = " + page.from + "\n" +
		                  "name = " + page.name + "\n" +
		                  "parent_page = " + page.parent_page + "\n" +
		                  "OCid = " + page.OCid + "\n")

		          println("\n")

        	case Failure(error) =>
        		println("ERROR while retrieving page " + id + " : " + error)        
        }


    case UpdatePage(id) =>
        println("User " + self.path.name + " Updating an Page with id = " + id)

        val AESStringKey = scala.util.Random.alphanumeric.take(15).mkString
        var encEmail = rsaencrypt("organization@email.com".getBytes, AESStringKey)
        
        println("User " + self.path.name + " updating Page " + id + 
                " with encrypted email " + encEmail.data)

        import FBJsonProtocol._

        val P = new Page(myAuthString, id, "about", true, "cover", "description", 
                new String(encEmail.data),
                12, "link", "location", self.path.name, "name", "parent_page",
                Array("likes"), Array(self.path.name,"1","2","3"), "-1", "TODO: enc.key".getBytes)

        val response: Future[HttpResponse] = pipeline(Post(url + "Page", P))
        response onComplete {
        	case Success(upPage) =>
        		println(upPage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while updating page : " + error)
        }

    case DeletePage(id) =>
        println("User " + self.path.name + " deleting Page " + id)
        import FBJsonProtocol._
        val request = new DeleteRequest(myAuthString, self.path.name, id)
		val response: Future[HttpResponse] = pipeline(Delete(url + "Page",request))
        response onComplete {
        	case Success(dePage) =>
        		println(dePage.entity.asString)

        	case Failure(error) =>
        		println("ERROR while deleting page : " + error)
        }

  }
}
