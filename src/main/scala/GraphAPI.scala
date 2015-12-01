import spray.http.StatusCodes
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
import scala.collection.concurrent.TrieMap

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
    var experienceMap = new TrieMap[String, Experience]
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

    implicit val timeout = Timeout(10 seconds)
    val format = new java.text.SimpleDateFormat()
    import FBJsonProtocol._
    import scala.collection.mutable.ArrayBuffer

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
          parameter("id") { id =>
            println("ALBUM: GET request received for id " + id)
            if(albumMap.contains(id))
              complete {albumMap(id)}
            else 
              // Error description in album.description
            complete(Album("-1", "-1", 0, "", "", "The requested album cannot be found.", "", "", "", "", "", "", "", Array(""), "-1"))
          }
        } ~
        post {
          entity(as[Album]) { album =>
            if (album.id == "null") {
              if(album.auth != album.from) {
                complete("You are not allowed to create this album")
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

                var newAlbum = new Album(album.auth, (numalbums).toString,
                  album.count, album.cover_photo, album.created_time,
                  album.description, album.from, album.link, album.location,
                  album.name, album.place, album.privacy, format.format(new java.util.Date()),
                  Array(album.cover_photo), numOC.toString)
                albumMap(numalbums.toString) = newAlbum
                println("-> User " + album.from + ": Album created with ID = " + newAlbum.id)
                complete("Album created!")
              }
              } else {
                // Update an existing album
                if(albumMap.contains(album.id)) {
                  if(albumMap(album.id).from != album.auth) {
                    complete("You are not allowed to create this album")
                  } else { 
                    var A = album
                    A.OCid = albumMap(album.id).OCid
                    albumMap(album.id) = A
                    complete("Album updated!")
                  }
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

              complete("Album with id = " + del_id + " was deleted!")
            }
            else 
              complete("Album with id = " + del_id + " was not found")
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
                comment.user_comments, comment.user_likes)

              if (objectCommentsMap.contains(comment.object_id)) {
                commentMap(numComments.toString) = newComment

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments :+ newComment.id
                objectCommentsMap(comment.object_id) = currObj

                println("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
                complete("Comment created!")
              }
              else
                complete("COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
              } else {// Update an existing comment
                if(commentMap.contains(comment.id) && objectCommentsMap.contains(comment.object_id)) {
                  commentMap(comment.id) = comment
                  complete("Comment with id = " + comment.id + " updated!")
                } else {
                  complete("Comment with id = " + comment.id + " DOES NOT EXIST!")
                }
              }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("COMMENT: DELETE request received for del_id = " + del_id)

            if (objectCommentsMap.contains(del_id)) {              

              if(commentMap.contains(del_id)) {

                var comment = commentMap(del_id)

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments.filter(! _.contains(del_id))
                objectCommentsMap(comment.object_id) = currObj

                commentMap.remove(del_id)
                complete("Comment with id = " + del_id + " was deleted!")
              }
              else
                complete("Comment with id = " + del_id + " was not found")
            }
            else
              complete("COMMENT: Parent Object with id = " + del_id + " DOES NOT EXIST!")
          }
        }
      }        
    } ~
    pathPrefix("Comment") {
      get {
        parameter("id") { id =>
          if (objectCommentsMap.contains(id))
            complete(objectCommentsMap(id))
          else
            complete("Not found")
        }
      }
    } ~
    pathPrefix("FriendList") {
      get {
        parameter("id") { id =>
          if(friendlistMap.contains(id)) {
           complete(friendlistMap(id)) 
          } else {
            complete("FriendList not found for id " + id)
          }
        }
      }
    } ~
    pathPrefix("Page") {
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
            if(page.auth != page.from) {
              complete("You are not allowed to create this page")
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
                page.cover, page.description, page.emails, page.is_community_page,
                page.is_permanently_closed, page.is_published, page.like_count, page.link,
                page.location, page.from, page.name,
                page.parent_page, page.posts, page.phone, format.format(new java.util.Date()),
                page.likes, page.members, numOC.toString)

              pageMap(numPages.toString) = newPage
              println("-> User " + page.from + ": Page created with ID = " + newPage.id)
              complete("Page created!")
            }
          }
          else {

            // Update an exising page
            if(pageMap.contains(page.id)) {
              if(pageMap(page.id).from != page.auth) {
                complete("You are not allowed to edit this page")
              } else {
                var P = page
                P.OCid = pageMap(page.id).OCid
                pageMap(page.id) = P
                complete("Page updated!")
              }
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
            complete("Page with id = " + del_id + " was deleted!")
          }
          else 
            complete("Page with id = " + del_id + " was not found")
        }
      }
    } ~
    pathPrefix("Photo") {
      get {
        parameter("id") { id =>
          println("GET request received for id " + id)
          if(photoMap.contains(id))
            complete {photoMap(id)}
          else 
            // Error description in photo.name
          complete(Photo("-1","-1", "", "", "", Array(-1), "",
            "The requested photo cannot be found.", "", "", Array(""), Array(""), ""))
        }
      }~
      post {
        println("Got a post for photos")
        entity(as[Photo]) { photo =>

          if(photo.id == "null") {
            if(photo.auth != photo.from) {
              complete("You are not allowed to post this picture")
            } else {
              if(albumMap.contains(photo.album)) {
                if(albumMap(photo.album).from != photo.auth) {
                  complete("You are not allowed to post to this album")
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
                    photo.user_comments, photo.user_likes, numOC.toString)
                  photoMap(numphotos.toString) = newPhoto
                  var tempObj = albumMap(photo.album)
                  tempObj.photos = tempObj.photos :+ newPhoto.id
                  albumMap(photo.album) = tempObj
                  println("Photo with id "+ newPhoto.id + " Uploaded!")
                  complete("Photo with id "+ newPhoto.id + " Uploaded!")
                }
                } else {
                  println("Couldn't upload photo to Album " + photo.album +". No such album")
                  complete("Couldn't upload photo to Album " + photo.album +". No such album")
                }
            }
            } else {
              complete("The requested photo was not found")
            }
        }
      }~
      delete {
        parameter("del_id") { del_id =>
          println("Photo: DELETE request received for id = " + del_id)
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
            complete("Photo with id = " + del_id + " was deleted!")
          } else {
            complete("Profile with id = " + del_id + " was not found")
          }
        }
      }
    }~
    pathPrefix("Status") {
      pathEnd {
        get {
          parameter("id") { id =>
            println("STATUS: GET request received for id " + id)
            if(statusMap.contains(id))
              complete {statusMap(id)}
            else 
              complete("The requested Status was not found")
          }
        }~
        post {
          entity(as[Status]) { status =>
            if(status.id == "null") {
              if(status.auth != status.from) {
                complete("You are not allowed to post this status")
              } else {
                numstatus += 1
                //Add status to statusMap
                if(!objectCommentsMap.contains(status.OCid)) {
                  numOC += 1;
                  val OC = new ObjectComments(numOC.toString, STATUS, numstatus.toString, Array(""))
                  objectCommentsMap(numOC.toString) = OC
                  println("-> User " + status.from + ": Status" + numalbums.toString + " added to objectCommentsMap")
                }
                var newStatus = new Status(status.auth, (numstatus).toString,
                  format.format(new java.util.Date()),
                  status.from, status.location, status.message,
                  format.format(new java.util.Date()),
                  numOC.toString())
                statusMap(numstatus.toString) = newStatus
                println("-> Status" + status.from + ":Status Changed= " + status.message) 
                complete("Status Posted")
              }
              } else {
                //Update a status
                if(statusMap.contains(status.id)) {
                  if(statusMap(status.id).from != status.auth) {
                    complete("You are not allowed to update this status")
                  } else {
                    var S = status
                    S.OCid = statusMap(status.id).OCid
                    statusMap(status.id) = S
                    complete("Status Posted")
                  }
                  } else {
                    complete("The requested status was not found.")
                  }
              }
          }
        } ~
        delete {
          parameter("del_id") { del_id =>
            println("Status: DELETE request received for del_id = " + del_id)
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
              complete("Status " + del_id + " deleted")
            } else {
              complete("Requested status not found")
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
                comment.user_comments, comment.user_likes)

              if (objectCommentsMap.contains(comment.object_id)) {
                commentMap(numComments.toString) = newComment

                // Update the objectCommentMap with this comment
                var currObj = objectCommentsMap(comment.object_id)
                currObj.comments = currObj.comments :+ newComment.id
                objectCommentsMap(comment.object_id) = currObj

                println("-> Comment created for Object " + newComment.object_id + " with ID = " + newComment.id)
                complete("Comment created!")
              } else { 
                complete("COMMENT: Parent Object with id = " + comment.object_id + " DOES NOT EXIST!")
              }
              } else {
                if(commentMap.contains(comment.id) && objectCommentsMap.contains(comment.object_id)) {
                  commentMap(comment.id) = comment
                  complete("Comment with id = " + comment.id + " updated!")
                } else {
                  complete("Comment with id = " + comment.id + " DOES NOT EXIST!")
                }

              }
          }
        }
      }
    } ~
    pathPrefix("Profile") {
      pathEnd {
        get {
          parameter("id") { id =>
            println("PROFILE: GET request received for id " + id)
            if(profileMap.contains(id))
              complete {profileMap(id)}
            else
              // Error in profile.bio
            complete(Profile( 
              "-1", 
              "-1", 
              "The requested profile was not found", 
              "", "", "", "", "", Array(""), Array(""), "", "", "", "", 
              "", "", "", "", "", "", Array(""), ""))
            //complete("The requested profile was not found")
          }
        } ~
        post {
          entity(as[Profile]) { profile =>

            // Existing user
            if(profileMap.contains(profile.id)) {
              if(profileMap(profile.id).auth != profile.auth) {
                complete("You are not allowed to update this profile")
              } else {
                profileMap(profile.id) = profile
                complete("Profile with id " + profile.id + " updated!")
              }
            }

            // Creating a user profile
          else {
            val newProfile = new Profile(profile.auth, profile.id, profile.bio, profile.birthday,
              profile.email, profile.first_name, profile.gender,
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
    }~
    pathPrefix("AddFriend") {
      post{
        entity(as[FriendReqest]) { fr =>
          //Add from
          if(fr.fromid != fr.auth && fr.toid != fr.auth) {
            complete("You are not allowed to make friendship between " + fr.fromid +" and " + fr.toid)
          } else {
            if(profileMap.contains(fr.toid) && profileMap.contains(fr.fromid)) {
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
                println(fr.fromid + " is friends with " + fr.toid)
                complete {"Done"}
                /*          val tempUser = context.actorFor("akka://facebook/user/" + toid)
                  tempUser ! AddFriend(fromid)
                  "Done !!"*/
               } else {
                 println("The requested user was not found")
                 complete("The requested user was not found")
               }
          }
        }   

      } 
    }
}

