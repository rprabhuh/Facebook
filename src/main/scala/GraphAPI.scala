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
  var numPages = 0

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
            println("ALBUM: GET request received for id " + id)
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
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("ALBUM: DELETE request received for del_id = " + del_id)
            if(albumMap.contains(del_id)) {
              albumMap.remove(del_id)
              complete("Album with id = " + del_id + " was deleted!")
            }
            else 
              complete("Album with id = " + del_id + " was not found")
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
    path("Page") {
        get {
          parameter("id") { id =>
            println("PAGE: GET request received for id " + id)
            if(pageMap.contains(id))
              complete {pageMap(id)}
            else 
              complete("The requested page was not found")
          }
        } ~
        post {
          entity(as[Page]) { page =>

            if (page.id == "null") {
              numPages += 1
              var newPage = new Page((numPages).toString, page.about, page.can_post,
                  page.cover, page.description, page.emails, page.is_community_page,
                  page.is_permanently_closed, page.is_published, page.like_count, page.link,
                  page.location, page.from, page.name,
                  page.parent_page, page.posts, page.phone, format.format(new java.util.Date()),
                  page.likes, page.members)

              pageMap(numPages.toString) = newPage
              println("-> User " + page.from + ": Page created with ID = " + newPage.id)
              complete("Page created!")
            }
            else {
              // Update an exising album
              if(pageMap.contains(page.id)) {
                pageMap(page.id) = page
                complete("Page updated!")
              } else {
                complete("The requested page does not exist!")
              }
            }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("PAGE: DELETE request received for id = " + del_id)
            if(pageMap.contains(del_id)) {
              pageMap.remove(del_id)
              complete("Page with id = " + del_id + " was deleted!")
            }
            else 
              complete("Page with id = " + del_id + " was not found")
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
    path("Profile") {
       get {
          parameter("id") { id =>
            println("PROFILE: GET request received for id " + id)
            if(profileMap.contains(id))
              complete {profileMap(id)}
            else 
              complete("The requested profile was not found")
          }
        } ~
        post {
          entity(as[Profile]) { profile =>

            // Existing user
            if(profileMap.contains(profile.id)) {
                profileMap(profile.id) = profile
                complete("Profile with id " + profile.id + " updated!")
            }

            // Creating a user profile
            else {
              var newProfile = new Profile(profile.id, profile.bio, profile.birthday,
                profile.education, profile.email, profile.first_name, profile.gender,
                profile.hometown, profile.interested_in, profile.languages, profile.last_name,
                profile.link, profile.location, profile.middle_name, profile.political,
                profile.relationship_status, profile.religion, profile.significant_other,
                profile.updated_time, profile.website, profile.work, profile.cover)

              profileMap(profile.id) = newProfile
              println("-> PROFILE created with id = " + newProfile.id)
              complete("Profile created!")
            }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("USER: DELETE request received for id = " + del_id)
            if(profileMap.contains(del_id)) {
              profileMap.remove(del_id)
              complete("Profile with id = " + del_id + " was deleted!")
            }
            else 
              complete("Profile with id = " + del_id + " was not found")
          }
        }
    }
}