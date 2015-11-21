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

class RestInterface extends HttpServiceActor
  with GraphAPI {

  def receive = runRoute(routes)
}

trait GraphAPI extends HttpService with ActorLogging { actor: Actor =>

  implicit val timeout = Timeout(10 seconds)

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
        complete("GET for Album")
      } ~
      post {
        complete("Let us POST for Album")
      }
    } ~
    path("Comment") {
      get {
        complete("GET for Comment")
      } ~
      post {
        complete("Let us POST for Comment")
      }
    } ~
    path("Conversation") {
      get {
        complete("GET for Conversation")
      } ~
      post {
        complete("Let us POST for Conversation")
      }
    } ~
    path("FriendList") {
      get {
        complete("GET for FriendList")
      } ~
      post {
        complete("Let us POST for FriendList")
      }
    } ~
    path("Group") {
      get {
        complete("GET for Group")
      } ~
      post {
        complete("Let us POST for Group")
      }
    } ~
    path("GroupDoc") {
      get {
        complete("GET for GroupDoc")
      } ~
      post {
        complete("Let us POST for GroupDoc")
      }
    } ~
    path("Link") {
      get {
        complete("GET for Link")
      } ~
      post {
        complete("Let us POST for Link")
      }
    } ~
    path("Message") {
      get {
        complete("GET for Message")
      } ~
      post {
        complete("Let us POST for Message")
      }
    } ~
    path("Notification") {
      get {
        complete("GET for Notification")
      } ~
      post {
        complete("Let us POST for Notification")
      }
    } ~
    path("ObjectComments") {
      get {
        complete("GET for ObjectComments")
      } ~
      post {
        complete("Let us POST for ObjectComments")
      }
    } ~
    path("ObjectLikes") {
      get {
        complete("GET for ObjectLikes")
      } ~
      post {
        complete("Let us POST for ObjectLikes")
      }
    } ~
    path("Page") {
      get {
        complete("GET for Page")
      } ~
      post {
        complete("Let us POST for Page")
      }
    } ~
    path("Photo") {
      get {
        complete("GET for Photo")
      } ~
      post {
        complete("Let us POST for Photo")
      }
    } ~
    path("Status") {
      get {
        complete("GET for Status")
      } ~
      post {
        complete("Let us POST for Status")
      }
    } ~
    path("Thread") {
      get {
        complete("GET for Thread")
      } ~
      post {
        complete("Let us POST for Thread")
      }
    } ~
    path("User") {
      get {
        complete("GET for User")
      } ~
      post {
        complete("Let us POST for User")
      }
    }
}