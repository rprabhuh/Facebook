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

class GraphAPI extends Actor {
  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      sender ! HttpResponse(entity = "Facebook.com says Hi")
    
    case HttpRequest(GET, Uri.Path("/Album"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Album!")
    case HttpRequest(POST, Uri.Path("/Album"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Album!")

    case HttpRequest(GET, Uri.Path("/Comment"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Comment!")
    case HttpRequest(POST, Uri.Path("/Comment"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Comment!")

    case HttpRequest(GET, Uri.Path("/Conversation"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Conversation!")
    case HttpRequest(POST, Uri.Path("/Conversation"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Conversation!")

    case HttpRequest(GET, Uri.Path("/FriendList"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for FriendList!")
    case HttpRequest(POST, Uri.Path("/FriendList"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for FriendList!")

    case HttpRequest(GET, Uri.Path("/Group"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Group!")
    case HttpRequest(POST, Uri.Path("/Group"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Group!")

    case HttpRequest(GET, Uri.Path("/GroupDoc"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for GroupDoc!")
    case HttpRequest(POST, Uri.Path("/GroupDoc"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for GroupDoc!")

    case HttpRequest(GET, Uri.Path("/Link"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Link!")
    case HttpRequest(POST, Uri.Path("/Link"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Link!")

    case HttpRequest(GET, Uri.Path("/Message"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Message!")
    case HttpRequest(POST, Uri.Path("/Message"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Message!")

    case HttpRequest(GET, Uri.Path("/Notification"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Notification!")
    case HttpRequest(POST, Uri.Path("/Notification"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Notification!")

    case HttpRequest(GET, Uri.Path("/ObjectComments"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for ObjectComments!")
    case HttpRequest(POST, Uri.Path("/ObjectComments"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for ObjectComments!")

    case HttpRequest(GET, Uri.Path("/ObjectLikes"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for ObjectLikes!")
    case HttpRequest(POST, Uri.Path("/ObjectLikes"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for ObjectLikes!")

    case HttpRequest(GET, Uri.Path("/Post"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Post!")
    case HttpRequest(POST, Uri.Path("/Post"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Post!")

    case HttpRequest(GET, Uri.Path("/Page"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Page!")
    case HttpRequest(POST, Uri.Path("/Page"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Page!")

    case HttpRequest(GET, Uri.Path("/Photo"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Photo!")
    case HttpRequest(POST, Uri.Path("/Photo"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Photo!")

    case HttpRequest(GET, Uri.Path("/Status"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Status!")
    case HttpRequest(POST, Uri.Path("/Status"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Status!")

    case HttpRequest(GET, Uri.Path("/Thread"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for Thread!")
    case HttpRequest(POST, Uri.Path("/Thread"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for Thread!")

    case HttpRequest(GET, Uri.Path("/User"), _, _, _) =>
      sender ! HttpResponse(entity = "Requested for User!")
    case HttpRequest(POST, Uri.Path("/User"), _, _, _) =>
      sender ! HttpResponse(entity = "Post for User!")

  }

}
