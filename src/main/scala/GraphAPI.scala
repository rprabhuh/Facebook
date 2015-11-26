import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.can.Http.RegisterChunkHandler
import spray.routing._
import spray.httpx.SprayJsonSupport._
import scala.util.parsing.json._
import scala.collection.mutable.ArrayBuffer
import scala.collection.concurrent.TrieMap

class RestInterface extends HttpServiceActor
  with GraphAPI {

  def receive = runRoute(routes)
}

trait GraphAPI extends HttpService with ActorLogging { 
  actor: Actor =>

  // Maps that will hold all the data.
  var albumMap = new TrieMap[String, Album]
  var experienceMap = new TrieMap[String, Experience]
  var pageMap = new TrieMap[String, Page]
  var profileMap = new TrieMap[String, Profile]
  var postclassMap = new TrieMap[String, PostClass]
  var friendlistMap = new TrieMap[String, FriendList]
  var photoMap = new TrieMap[String, Photo]
  var commentMap = new TrieMap[String, Comment]
  var objectcommentsMap = new TrieMap[String, ObjectComments]
  var numalbums = 0
  var numphotos = 0

  implicit val timeout = Timeout(10 seconds)
  val format = new java.text.SimpleDateFormat()
  import FBJsonProtocol._


  def routes: Route =

    path("") {
      get {
        log.info("Building get route")
        complete {
          "Welcome"
        }
      }
    } ~
    path("Album") {
        get {
          parameter("id") { id =>
            println("GET request received for id " + id)
            if(albumMap.contains(id))
              complete {albumMap(id)}
            else 
              complete("The requested album was not found")
          }
        } ~
        post {
          entity(as[Album]) { album =>

            if (album.id == "null") {
              numalbums +=1
              var newAlbum = new Album((numalbums).toString,
                  album.count, album.cover_photo, album.created_time,
                  album.description, album.from, album.link, album.location,
                  album.name, album.place, album.privacy, format.format(new java.util.Date())
                  )
              albumMap(numalbums.toString) = newAlbum
              println("-> User " + album.from + ": Album created with ID = " + newAlbum.id)
              complete("Album created!")
            }
            else {
              // Update an exising album
              if(albumMap.contains(album.id)) {
                albumMap(album.id) = album
                complete("Album updated!")
              } else {
                complete("The requested album does not exist!")
              }
            }

          }
        }
    } ~
    pathPrefix("Comment") {
      pathEnd {
        get {
          complete("GET for Comment")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Comment")
        }
      }
    } ~
    pathPrefix("Conversation") {
      pathEnd {
        get {
          complete("GET for Conversation")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Conversation")
        }
      }
    } ~
    pathPrefix("FriendList") {
      pathEnd {
        get {
          complete("GET for FriendList")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for FriendList")
        }
      }
    } ~
    pathPrefix("ObjectComments") {
      pathEnd {
        get {
          complete("GET for ObjectComments")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for ObjectComments")
        }
      }
    } ~
    pathPrefix("ObjectLikes") {
      pathEnd {
        get {
          complete("GET for ObjectLikes")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for ObjectLikes")
        }
      }
    } ~
    pathPrefix("Post") {
      pathEnd {
        get {
          complete("GET for Post")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Post")
        }
      }
    } ~
    pathPrefix("Page") {
      pathEnd {
        get {
          complete("GET for Page")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Page")
        }
      }
    } ~
    path("Photo") {
        get {
          parameter("id") { id =>
            println("GET request received for id " + id)
            if(photoMap.contains(id))
              complete {photoMap(id)}
            else 
              complete("The requested photo was not found")
          }
        }~
        post {
          println("Got a post for photos")
          entity(as[Photo]) { photo =>
 
            if(photo.id == "null") {
              numphotos += 1
              var newPhoto = new Photo(numphotos.toString,
                photo.album, format.format(new java.util.Date()), 
                photo.from, photo.image, photo.link, photo.name, 
                format.format(new java.util.Date()), photo.place, 
                photo.user_comments, photo.user_likes
                )
              photoMap(numphotos.toString) = newPhoto
              complete("Photo with id "+ newPhoto.id + " Uploaded!")
            } else {
              complete("The requested photo was not found")
            }
          }
        }
    } ~
    pathPrefix("Status") {
      pathEnd {
        get {
          complete("GET for Status")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Status")
        }
      }
    } ~
    pathPrefix("Thread") {
      pathEnd {
        get {
          complete("GET for Thread")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Thread")
        }
      }
    } ~
    pathPrefix("User") {
      pathEnd {
        get {
          complete("GET for User")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for User")
        }
      }
    }
}
