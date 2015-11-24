import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
//import spray.http._
//import HttpMethods._
//import MediaTypes._
import spray.can.Http.RegisterChunkHandler
import spray.routing._
import spray.httpx.SprayJsonSupport._
import scala.util.parsing.json._
import scala.collection.mutable.ArrayBuffer


class RestInterface extends HttpServiceActor
  with GraphAPI {

  def receive = runRoute(routes)
}

trait GraphAPI extends HttpService with ActorLogging { actor: Actor =>

  implicit val timeout = Timeout(10 seconds)
  val format = new java.text.SimpleDateFormat()
  import FBJsonProtocol._

  var albumList = ArrayBuffer[Album]()

  def routes: Route =

    path("") {
      get {
        log.info("Building get route")
        complete {
          "Welcome"
        }
      }
    } ~
    pathPrefix("Album") {
      pathEnd {
        get {
          println("GET request received")
          parameter("id") { id =>
            println("GET request received for id " + id)
            complete {
              "GET request received for id " + id
            }
          }
        }
        post {
          entity(as[Album]) { album =>

            // Create a new album
            if (album.id == "null") {
              var newAlbum = new Album((albumList.length + 1).toString,
                  album.count, album.cover_photo, album.created_time,
                  album.description, album.from, album.link, album.location,
                  album.name, album.place, album.privacy, format.format(new java.util.Date()),
                  album.owner)
              albumList += newAlbum
              println("-> User " + album.owner + ": Album created with ID = " + newAlbum.id)
            }
            // Update an exising album
            else {
              albumList += album
              println("-> User " + album.owner + ": Album created with ID = " + album.id)
            }

            complete("Album created!")
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
    pathPrefix("Group") {
      pathEnd {
        get {
          complete("GET for Group")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Group")
        }
      }
    } ~
    pathPrefix("GroupDoc") {
      pathEnd {
        get {
          complete("GET for GroupDoc")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for GroupDoc")
        }
      }
    } ~
    pathPrefix("Link") {
      pathEnd {
        get {
          complete("GET for Link")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Link")
        }
      }
    } ~
    pathPrefix("Message") {
      pathEnd {
        get {
          complete("GET for Message")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Message")
        }
      }
    } ~
    pathPrefix("Notification") {
      pathEnd {
        get {
          complete("GET for Notification")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Notification")
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
    pathPrefix("Photo") {
      pathEnd {
        get {
          complete("GET for Photo")
        }
      } ~
      path(DoubleNumber) { (id) =>
        post {
          requestContext => println(id)
          requestContext.complete("Let us POST for Photo")
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


/*    private def CreateAlbum(arg: Type) = {

    }*/
}
