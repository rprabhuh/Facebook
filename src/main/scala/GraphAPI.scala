import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.can.Http
import spray.can.server.Stats
import spray.can.Http.RegisterChunkHandler
import spray.util._
import spray.routing._

import scala.concurrent.duration._
import scala.collection.concurrent.TrieMap
import scala.util.parsing.json._

import akka.pattern.ask
import akka.util.Timeout
import akka.actor._

import java.security.MessageDigest

object ObjectType extends Enumeration {
  type ObjectType = Value
  val ALBUM, PHOTO, STATUS, POST, PAGE = Value
}


class RestInterface extends HttpServiceActor
with GraphAPI {

  def receive = runRoute(routes)
}


trait GraphAPI extends HttpService with ActorLogging { 
  actor: Actor =>

    // Maps that will hold all the data.
    var albumMap = new TrieMap[String, Album]
    var pageMap = new TrieMap[String, Page]
    var profileMap = new TrieMap[String, Profile]
    var friendlistMap = new TrieMap[String, FriendList]
    var photoMap = new TrieMap[String, Photo]
    var commentMap = new TrieMap[String, Comment]
    var objectCommentsMap = new TrieMap[String, ObjectComments]
    var statusMap = new TrieMap[String, Status]

    val ALBUM = "ALBUM"
    val PAGE = "PAGE"
    val PHOTO = "PHOTO"
    val STATUS = "STATUS"

    var numalbums = 0
    var numstatus = 0
    var numPages = 0
    var numphotos = 0
    var numComments = 0
    var numOC = 0

    def shait(uuid: String): String =  {
      val sha = MessageDigest.getInstance("SHA-256")
      sha.update(uuid.getBytes("UTF-8"))  
      sha.digest().map("%02X" format _).mkString
    }
    implicit val timeout = Timeout(10 seconds)
    val format = new java.text.SimpleDateFormat()
    import FBJsonProtocol._
    import scala.collection.mutable.ArrayBuffer

    def routes: Route =

      path("") {
        get {
          log.info("Building get route")
          complete { "Welcome" }
        }
      } ~
    pathPrefix("Album") {
      pathEnd {
        get {
          entity(as[GetRequest]) { album =>
            println("-> ALBUM: GET request received for id " + album.id)
            var from = album.from
            if(profileMap.contains(from)) {
              var auth = shait(album.auth)
              if(auth != profileMap(from).auth) {
                  println("-> Permission Denied. Can't access album " + album.id)
                  complete(StatusCodes.Unauthorized)

              } else {
                if(albumMap.contains(album.id)){
                  if(albumMap(album.id).from == album.from) {
                    complete {albumMap(album.id)}
                  } else {
                    println("-> The requested album with id " + album.id + " cannot be found.")
                    complete(StatusCodes.Unauthorized)

                  }
                  } else{
                  // Error description in album.description
                  println("-> The requested album with id " + album.id + " cannot be found.")
                  complete(StatusCodes.NotFound)
                }
              }
            } else {
              println("-> The requested album with id " + album.id + " cannot be found.")
              complete(StatusCodes.Unauthorized)
            }
          }
        } ~
        post {
          entity(as[Album]) { album =>
            if (album.id == "null") {
              var from = album.from
              if(profileMap.contains(from)) {
                var auth = shait(album.auth)
                if(auth != profileMap(from).auth) {
                println(" -> " + album.from+ " is not allowed to create an album")
                complete(StatusCodes.Unauthorized)
                //complete(album.from + " is not allowed to create an album")
              } else {
                numalbums +=1
                // Add to objectCommentsMap
                if (!objectCommentsMap.contains(album.OCid)) {
                  ////import ObjectType._
                  numOC += 1;
                  val OC = new ObjectComments(numOC.toString, ALBUM, numalbums.toString, Array(""))
                  objectCommentsMap(numOC.toString) = OC
                  println("-> User " + album.from + ": Album " + numalbums.toString + " added to objectCommentsMap")	
                }

                var newAlbum = new Album(shait(album.auth), (numalbums).toString,
                  album.count, album.cover_photo, album.created_time,
                  album.description, album.from, album.link, album.location,
                  album.name, album.place, album.privacy, format.format(new java.util.Date()),
                  Array(album.cover_photo), numOC.toString)
                albumMap(numalbums.toString) = newAlbum
                //println("-> User " + album.from + ": Album created with ID = " + newAlbum.id)
                complete("User " + album.from + ": Album created with ID = " + newAlbum.id)
              }
              } else {
                println(album.from + " is not allowed to create an album")
                complete(StatusCodes.Unauthorized)
              }

              } else {
                // Update an existing album
                if(albumMap.contains(album.id)) {
                  var from = album.from
                  var auth = shait(album.auth)
                  if(auth != profileMap(from).auth) {
                    println(album.auth + " is not allowed to update album " + album.id)
                    complete(StatusCodes.Unauthorized)
                  } else { 
                    var A = album
                    A.OCid = albumMap(album.id).OCid
                    albumMap(album.id) = A
                    //println("-> Album " + album.id + " updated!")
                    complete("Album " + album.id + " updated!")
                  }
                  } else {
                    println("-> The requested album id " + album.id + " does not exist!")
                    complete(StatusCodes.NotFound)
                  }
              }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("-> ALBUM: DELETE request received for del_id = " + del_id)
            if(albumMap.contains(del_id)) {
              //Delete all the photos in the album
              var tempObj = albumMap(del_id)
              var i = 0
              var j = 0
              var size = tempObj.photos.size
              for (i <- 0 until size) {
                if(photoMap.contains(tempObj.photos(i))) {
                  val OCid = photoMap(tempObj.photos(i)).OCid 
                  var comments = objectCommentsMap(OCid).comments
                  var size2 = comments.size
                  if (objectCommentsMap.contains(OCid)) {
                    for(j<-0 until size2) {
                      if(commentMap.contains(comments(j)))
                        commentMap.remove(comments(j))
                    }
                    objectCommentsMap.remove(OCid)
                  }
                  photoMap.remove(tempObj.photos(i))
                }
              }

              // TODO: Delete all the comments on this album
              val OCid = albumMap(del_id).OCid
              var comments = objectCommentsMap(OCid).comments
              size = comments.size
              if (objectCommentsMap.contains(OCid)) {
                for(i<-0 until size) {
                  if(commentMap.contains(comments(i)))
                    commentMap.remove(comments(i))
                }
                objectCommentsMap.remove(OCid)
              }

              albumMap.remove(del_id)

              //println("-> Album with id = " + del_id + " was deleted!")
              complete("Album with id = " + del_id + " was deleted!")
            }
            else 
              println("-> Album with id = " + del_id + " was not found")
              complete(StatusCodes.NotFound)
          }
        } 		
      } ~
      path("comment") {
        post {
          entity(as[Comment]) { comment =>
            // Create a new comment
            if (comment.id == "null") {
              numComments += 1
              var newComment = new Comment((numComments).toString, comment.object_id,
                comment.created_time, comment.from, comment.message, comment.parent,
                comment.user_comments, comment.user_likes, comment.encKey)

              if (objectCommentsMap.contains(comment.object_id)) {
                commentMap(numComments.toString) = newComment

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments :+ newComment.id
                objectCommentsMap(comment.object_id) = currObj

                //println("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
                complete("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
              }
              else {
                //println("-> COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
                complete("COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
              }
              } else {
                // Update an existing comment
                if(commentMap.contains(comment.id) && objectCommentsMap.contains(comment.object_id)) {
                  commentMap(comment.id) = comment
                  //println("-> Comment with id = " + comment.id + " updated!")
                  complete("Comment with id = " + comment.id + " updated!")
                } else {
                  println("-> Comment with id = " + comment.id + " DOES NOT EXIST!")
                  complete(StatusCodes.NotFound)
                }
              }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("-> COMMENT: DELETE request received for del_id = " + del_id)

            if (objectCommentsMap.contains(del_id)) {              

              if(commentMap.contains(del_id)) {

                var comment = commentMap(del_id)

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments.filter(! _.contains(del_id))
                objectCommentsMap(comment.object_id) = currObj

                commentMap.remove(del_id)
                //println("-> Comment with id = " + del_id + " was deleted!")
                complete("Comment with id = " + del_id + " was deleted!")
              }
              else {
                println("-> Comment with id = " + del_id + " was not found")
                complete(StatusCodes.NotFound)
              }
            }
            else {
              //println("-> COMMENT: Parent Object with id = " + del_id + " DOES NOT EXIST!")
              complete("COMMENT: Parent Object with id = " + del_id + " DOES NOT EXIST!")
            }
          }
        }
      }        
    } ~
    pathPrefix("Comment") {
      get {
        entity(as[GetRequest]) { comment =>
          val from = comment.from
          if(profileMap.contains(from)) {
            val auth = shait(comment.auth)
            if(profileMap(from).auth != auth) {
                println("-> Comment with id " + comment.id + " Not found")
                complete(StatusCodes.NotFound)
            } else { 
              if (objectCommentsMap.contains(comment.id)) {
                complete(objectCommentsMap(comment.id))
              } else {
                println("-> Comment with id " +comment.id + " Not found")
                complete(StatusCodes.NotFound)
              }
            }
          } else {
            complete(StatusCodes.NotFound)
          }
        }
      }
    } ~
    pathPrefix("FriendList") {
      get {
        parameter("id") { id =>
          if(friendlistMap.contains(id)) {
           complete(friendlistMap(id)) 
          } else {
            println("-> FriendList not found for id " + id)
            complete(StatusCodes.NotFound)
          }
        }
      }
    } ~
    pathPrefix("Page") {
      get {
        entity(as[GetRequest]) { page =>
          var from = page.from
          if(profileMap.contains(from)) {
            var auth = shait(page.auth)
            if(auth != profileMap(from).auth) {
              println("-> Couldn't retrieve Page with id " + page.id + ". Authentication Issue ")
              complete(StatusCodes.Unauthorized)
            } else {
              println("-> PAGE: GET request received for id " + page.id)
              if(pageMap.contains(page.id)) {
                complete {pageMap(page.id)}
              } else {
                println("-> Page with id " + page.id + " not found")
                complete(StatusCodes.NotFound)
              }
            }
          } else {
              println("-> Couldn't retrieve Page with id " + page.id + ". Authentication Issue ")
              complete(StatusCodes.NotFound)

          }
        }
      } ~
      post {
        entity(as[Page]) { page =>
          if (page.id == "null") {
            var from = page.from

            if(profileMap.contains(from)) { 
              var auth = shait(page.auth)
              if(auth != profileMap(from).auth) {
                //println(page.auth + " is not allowed to create this page")
                complete(page.from + " is not allowed to create this page")
              } else {
              numPages += 1

              // Add to objectCommentsMap
              if (!objectCommentsMap.contains(page.OCid)) {
                //import ObjectType._
                numOC += 1;
                val OC = new ObjectComments(numOC.toString, PAGE, numPages.toString, Array(""))
                objectCommentsMap(numOC.toString) = OC
                println("-> User " + page.from + ": Page " + numPages.toString + " added to objectCommentsMap")	
              }

              val newPage = new Page(page.auth, (numPages).toString, page.about, page.can_post,
                page.cover, page.description, page.email, page.like_count, page.link,
                page.location, page.from, page.name, page.parent_page, page.likes,
                page.members, numOC.toString, page.encKey)

              pageMap(numPages.toString) = newPage
              //println("-> User " + page.from + ": Page created with ID = " + newPage.id)
              complete("-> User " + page.from + ": Page created with ID = " + newPage.id)
            }
            } else {
              complete(page.from + " is not allowed to create this page")
            }
            //Get the from field.
            //Look Up that actor's auth
            //Compare with the current auth
          } else {

            // Update an exising page
            if(pageMap.contains(page.id)) {
              var from = page.from
              if(profileMap.contains(from)) { 
                var auth = shait(page.auth)
                if(auth != profileMap(from).auth) {
                //println(page.auth + " is not allowed to update page "+page.id )
                complete(page.auth + " is not allowed to update page "+page.id )
                } else {
                  var P = page
                  P.OCid = pageMap(page.id).OCid
                  pageMap(page.id) = P
                  //println("-> Page" + page.id + " updated!")
                  complete("-> Page" + page.id + " updated!")
                }
              } else {
                complete(page.from + " is not allowed to edit this page")
              }
            } else {
                println("-> Page with id " + page.id + " does not exist!")
                complete(StatusCodes.NotFound)
            }
          }
        }
      } ~
      delete {
        parameter("del_id") { del_id =>
          println("-> PAGE: DELETE request received for id = " + del_id)
          if(pageMap.contains(del_id)) {
            // TODO: Delete all the comments on this page
            val OCid = pageMap(del_id).OCid
            var comments = objectCommentsMap(OCid).comments
            var size = comments.size
            if(objectCommentsMap.contains(OCid)) {
              for(i <- 0 until size) {
                if(commentMap.contains(comments(i)))
                  commentMap.remove(comments(i))
              }
              objectCommentsMap.remove(OCid)
            }
            pageMap.remove(del_id)
            //println("-> Page with id = " + del_id + " was deleted!")
            complete("Page with id = " + del_id + " was deleted!")
          }
          else {
            println("-> Page with id = " + del_id + " was not found")
            complete(StatusCodes.NotFound)
          }
        }
      }
    } ~
    pathPrefix("Photo") {
      get {
        entity(as[GetRequest]) { photo =>
          var from = photo.from
          if(profileMap.contains(from)) {
            var auth = shait(photo.auth)
            if(auth != profileMap(from).auth) {
                println("-> Cannot access Photo with id " +photo.id+ ". Authentication Failure")
                complete(StatusCodes.NotFound)
            } else { 
              println("-> GET request received for id " + photo.id)
              if(photoMap.contains(photo.id)) {
                complete {photoMap(photo.id)} 
              } else {
                // Error description in photo.name
                println("-> Photo with id " + photo.id + " cannot be found.")
                complete(StatusCodes.NotFound)
              }
            }
           } else {//else for profileMap.contains
              println("-> Photo with id " + photo.id + " cannot be found.")
              complete(StatusCodes.NotFound)
          }
        }
      }~
      post {
          entity(as[Photo]) { photo =>
          println("-> Got a post for photos")
            if(photo.id == "null") {
              val from = photo.from
              if(profileMap.contains(from)) {
                val auth = shait(photo.auth)
                
              if(auth != profileMap(from).auth) {
                println(photo.from + " is not allowed to post this picture")
                complete(StatusCodes.Unauthorized)
              } else {//This is a valid User and is who (s)he claims to be
                if(albumMap.contains(photo.album)) {//Existing album
                  if(from != albumMap(photo.album).from) {// Album doesn't belong to the User
                    println(from)
                    println(albumMap(photo.album).from)
                    //println(photo.auth+ " is not allowed to post photos to album" + photo.album)
                    println(photo.from + " is not allowed to post photos to album" + photo.album)
                    complete(StatusCodes.Unauthorized)
                  } else {

                    numphotos += 1

                    // Add to objectCommentsMap
                    if (!objectCommentsMap.contains(photo.OCid)) {
                      //import ObjectType._
                      numOC += 1;
                      val OC = new ObjectComments(numOC.toString, PHOTO, numphotos.toString, Array(""))
                      objectCommentsMap(numOC.toString) = OC
                      println("-> User " + photo.from + ": Page " + numphotos.toString + " added to objectCommentsMap")	
                    }

                    var newPhoto = new Photo(photo.auth, numphotos.toString,
                      photo.album, format.format(new java.util.Date()),
                      photo.from, photo.image, photo.link, photo.name,
                      format.format(new java.util.Date()), photo.place,
                      photo.user_comments, photo.user_likes, numOC.toString, photo.encKey)
                    photoMap(numphotos.toString) = newPhoto
                    var tempObj = albumMap(photo.album)
                    tempObj.photos = tempObj.photos :+ newPhoto.id
                    albumMap(photo.album) = tempObj
                    //println("-> Photo with id "+ newPhoto.id + " Uploaded!")
                    complete("Photo with id "+ newPhoto.id + " Uploaded!")
                  }
                  } else {
                    //println("-> Couldn't upload photo to Album " + photo.album +". No such album")
                    complete("Couldn't upload photo to Album " + photo.album +". No such album")
                  }
              }
              } else {
                println(photo.from + " is not allowed to post this picture")
                complete(StatusCodes.Unauthorized)

              }
              } else {
                println("-> Photo with id " + photo.id + " was not found")
                complete(StatusCodes.NotFound)
              }
          }
        }~
        delete {
          parameter("del_id") { del_id =>
            println("-> Photo: DELETE request received for id = " + del_id)
            if(photoMap.contains(del_id)) {
              var i = 0
              val OCid = photoMap(del_id).OCid 
              var comments = objectCommentsMap(OCid).comments
              var size = comments.size
              if (objectCommentsMap.contains(OCid)) {
                for(j<-0 until size) {
                  if(commentMap.contains(comments(j)))
                    commentMap.remove(comments(j))
                }
                objectCommentsMap.remove(OCid)
              }
              photoMap.remove(del_id)
              //println("-> Photo with id = " + del_id + " was deleted!")
              complete("Photo with id = " + del_id + " was deleted!")
            } else {
              println("-> Photo with id " + del_id + " was not found")
              complete(StatusCodes.NotFound)
            }
          }
        }
    }~
    pathPrefix("Status") {
      pathEnd {
        get {
          entity(as[GetRequest]) { status =>
            //println("-> STATUS: GET request received for id " + id)
            var from = status.from
            if(profileMap.contains(from)) {
              var auth = shait(status.auth)
              if(auth != profileMap(from).auth) {
                  println("-> Could not access status with id " + status.id + ". Authentication issue")
                  complete(StatusCodes.Unauthorized)
              } else {
                if(statusMap.contains(status.id)) {
                  complete {statusMap(status.id)}
                } else {
                  println("-> Status with id " + status.id + " was not found")
                  complete(StatusCodes.NotFound)
                }
              }
            } else {// ProfileMap.contains
                  println("-> Could not access status with id " + status.id + ". Authentication issue")
              complete(StatusCodes.NotFound)
            }
          }
        }~
        post {
          entity(as[Status]) { status =>
            if(status.id == "null") {
              var from = status.from
              var auth = shait(status.auth)
              if(auth !=  profileMap(from).auth) {
                println(status.auth + " is not allowed to post statuses to the wall of " + status.from)
                complete(status.auth + " is not allowed to post statuses to the wall of " + status.from)
              } else {
                numstatus += 1
                //Add status to statusMap
                if(!objectCommentsMap.contains(status.OCid)) {
                  numOC += 1;
                  val OC = new ObjectComments(numOC.toString, STATUS, numstatus.toString, Array(""))
                  objectCommentsMap(numOC.toString) = OC
                  println("-> User " + status.from + ": Status " + numstatus.toString + " added to objectCommentsMap")
                }
                var newStatus = new Status(auth, (numstatus).toString,
                  format.format(new java.util.Date()),
                  status.from, status.location, status.message,
                  format.format(new java.util.Date()),
                  numOC.toString(), status.encKey)
                statusMap(numstatus.toString) = newStatus
                //println("-> Status" + status.from + ":Status Changed= " + status.message) 
                complete("-> Status" + status.from + ": Status Changed = " + status.message)
              }
              } else {
                //Update a status
                if(statusMap.contains(status.id)) {
                  if(shait(status.auth) != statusMap(status.id).auth) {
                    println(status.auth + " is not allowed to update statuses of " +statusMap(status.id).from)
                    complete(status.auth + " is not allowed to update statuses of " +statusMap(status.id).from)
                  } else {
                    var S = status
                    S.OCid = statusMap(status.id).OCid
                    statusMap(status.id) = S
                    //println("-> Status with id " + id + " Posted")
                    complete("-> Status with id " + status.id + " Posted")
                  }
                  } else {
                    println("-> Status with id " + status.id + " was not found.")
                    complete(StatusCodes.NotFound)
                  }
              }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            //println("-> Status: DELETE request received for del_id = " + del_id)
            if(statusMap.contains(del_id)) {
              val OCid = statusMap(del_id).OCid
              if(objectCommentsMap.contains(OCid)) {
                var comments = objectCommentsMap(OCid).comments
                var size = comments.size
                if (objectCommentsMap.contains(OCid)) {
                  for(j<-0 until size) {
                    if(commentMap.contains(comments(j)))
                      commentMap.remove(comments(j))
                  }
                  objectCommentsMap.remove(OCid)
                } 
              }
              statusMap.remove(del_id)
              //println("-> Status " + del_id + " deleted")
              complete("Status " + del_id + " deleted")
            } else {
                println("-> Status with id " + del_id + " was not found.")
                complete(StatusCodes.NotFound)
            }
          }
        }
      } ~
      path("comment") {
        post {
          entity(as[Comment]) { comment =>
            if(comment.id == "null") {
              numComments += 1
              var newComment = new Comment((numComments).toString, comment.object_id,
                comment.created_time, comment.from, comment.message, comment.parent,
                comment.user_comments, comment.user_likes, comment.encKey)

              if (objectCommentsMap.contains(comment.object_id)) {
                commentMap(numComments.toString) = newComment

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments :+ newComment.id
                objectCommentsMap(comment.object_id) = currObj

                //println("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
                complete("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
              } else { 
                //println("-> COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
                complete("COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
              }
              } else {
                if(commentMap.contains(comment.id) && objectCommentsMap.contains(comment.object_id)) {
                  commentMap(comment.id) = comment
                  //println("-> Comment with id = " + comment.id + " updated!")
                  complete("Comment with id = " + comment.id + " updated!")
                } else {
                  println("-> Comment with id = " + comment.id + " DOES NOT EXIST!")
                  complete(StatusCodes.NotFound)
                }

              }
          }
        }
      }
    } ~
    pathPrefix("Profile") {
      pathEnd {
        get {
          entity(as[GetRequest]) { profile =>
            val from = profile.from
            if(profileMap.contains(from)) {
              val auth = shait(profile.auth)
              if(profileMap(from).auth != auth) {
                println("-> Comment with id " + profile.id + " Not found")
                complete(StatusCodes.Unauthorized)
              } else { 
                if (objectCommentsMap.contains(profile.id)) {
                  //println("-> PROFILE: GET request received for id " + id)
                  if(profileMap.contains(profile.id)) {
                    complete {profileMap(profile.id)}
                  } else {
                    // Error in profile.bio
                    println("-> Profile with id " + profile.id + " was not found")
                    complete(StatusCodes.NotFound)
                  }
                } else {
                  println("-> Profile with id " + profile.id + " was not found")
                  complete(StatusCodes.NotFound) 
                }
              }
            } else {
              println("-> Profile with id " + profile.id + " was not found")
              complete(StatusCodes.Unauthorized) 
            }
          }
        } ~
        post {
          entity(as[Profile]) { profile =>
            // Existing user
            if(profileMap.contains(profile.id)) {
              var auth = shait(profile.auth)
              if(profileMap(profile.id).auth != auth) {
                //println(profile.auth + " is not allowed to update profile of user " +profileMap(profile.id))
                complete(profile.auth + " is not allowed to update profile of user " +profileMap(profile.id))
              } else {
                profileMap(profile.id) = profile
                //println("-> Profile with id " + profile.id + " updated!")
                complete("Profile with id " + profile.id + " updated!")
              }
            } else {// Creating a user profile 
              var uuid = java.util.UUID.randomUUID.toString
              var digest = shait(uuid)
              val newProfile = new Profile(digest, profile.id, profile.bio, profile.birthday,
              profile.email, profile.first_name, profile.gender,
              profile.hometown, profile.interested_in, profile.languages, profile.last_name,
              profile.link, profile.location, profile.middle_name,
              profile.relationship_status, profile.significant_other,
              profile.updated_time, profile.website, profile.cover, profile.encKey)

            profileMap(profile.id) = newProfile
            println("-> PROFILE created with id = " + newProfile.id)
            complete(uuid)
          }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            //println("-> USER: DELETE request received for id = " + del_id)
            if(profileMap.contains(del_id)) {
              profileMap.remove(del_id)
              //println("-> Profile with id = " + del_id + " was deleted!")
              complete("Profile with id = " + del_id + " was deleted!")
            } else {
              println("-> Profile with id = " + del_id + " was not found")
              complete(StatusCodes.NotFound)
            }
          }
        }
      }
    }~
    pathPrefix("AddFriend") {
      post{
        entity(as[FriendReqest]) { fr =>
          //Add from
          var from = fr.fromid
          if(profileMap.contains(from)) {
            var auth = shait(fr.auth)
          if(auth != profileMap(from).auth) { 
            //println(fr.auth + " is not allowed to make friendship between " + fr.fromid +" and " + fr.toid)
            println(fr.fromid + " is not allowed to make friendship between " + fr.fromid +" and " + fr.toid)
            complete(StatusCodes.Unauthorized)
          } else {

          	// Flip a coin to decide if the friend request shoule be accepted
          	import scala.util.Random
          	var randObj = new Random()
          	var frAccOrRej = randObj.nextInt() % 2
          	if (frAccOrRej == 0) {
          		//println(fr.toid + " declined " + fr.fromid + " friend request.")
          		complete(fr.toid + " declined " + fr.fromid + " friend request.")
          	}
			else if(profileMap.contains(fr.toid) && profileMap.contains(fr.fromid)) {
              if(friendlistMap.contains(fr.fromid)) {
                //retrieve the Friendlistobject and add this new user to it 
                var currObj = friendlistMap(fr.fromid)
                if(!currObj.members.contains(fr.toid)) {
                  currObj.members = currObj.members :+ fr.toid
                  friendlistMap(fr.fromid)= currObj
                }  
                } else {
                  //Create a new entry
                  var tempObj = new FriendList(fr.fromid, Array[String](fr.toid))
                  friendlistMap(fr.fromid) = tempObj
                }

                //Add to
                if(friendlistMap.contains(fr.toid)) {
                  //retrieve the Friendlistobject and add this new user to it 
                  var currObj = friendlistMap(fr.toid)
                  currObj.members = currObj.members :+ fr.fromid
                  friendlistMap(fr.toid)= currObj
                } else {
                  //Create a new entry
                  var tempObj = new FriendList(fr.toid, Array[String](fr.fromid))
                  friendlistMap(fr.toid) = tempObj
                }
                //println(fr.fromid + " is friends with " + fr.toid)
                complete {fr.fromid + " is friends with " + fr.toid}
                /*          val tempUser = context.actorFor("akka://facebook/user/" + toid)
                  tempUser ! AddFriend(fromid)
                  "Done !!"*/
               } else {
                 println("-> The requested user was not found")
                 complete(StatusCodes.NotFound)
               }
          }
          } else {
          		complete("Friend Request was not sent. Authentication issue")
          }
        }   

      } 
    }
}
