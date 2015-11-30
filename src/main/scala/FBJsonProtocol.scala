import spray.routing._
import spray.json._
import spray.httpx._
import spray.http._

/*sealed trait Status_Type { def sType: String }
case object added_photos extends Status_Type  { val sType = "ADDED_PHOTOS" }
case object wall_post extends Status_Type { val sType =  "WALL_POST" }
case object approved_friend extends Status_Type { val sType = "APPROVED_FRIEND" }

sealed trait Post_Type { def pType: String }
case object link_post extends Post_Type { val pType =  "LINK" }
case object status_post extends Post_Type { val pType = "STATUS" }
case object photo_post extends Post_Type  { val pType = "PHOTO" }

sealed trait Object_Type { def oType: String }
case object page_type extends Object_Type { val oType =  "PAGE" }
case object profile_type extends Object_Type { val oType = "PROFILE" }
case object album_type extends Object_Type  { val oType = "ALBUM" }
case object photo_type extends Object_Type  { val oType = "PHOTO" }
case object post_type extends Object_Type  { val oType = "POST" }
*/

case class Experience (
    id: String,
    description: String,
    name: String,
    from: String,
    var with_user: Array[String]
)

case class Page (
    id: String,
    var about: String,
    var can_post: Boolean,
    var cover: String,
    var description: String,
    var emails: Array[String],
    var is_community_page: Boolean,
    var is_permanently_closed: Boolean,
    var is_published: Boolean,
    var like_count: Int,
    var link: String,
    var location: String,
    var from: String,
    var name: String,
    var parent_page: String,
    var posts: Array[String],
    var phone: String,
    var last_used_time: String,
    var likes: Array[String],
    var members: Array[String],
    var OCid: String
)

case class Profile (
    id: String,
    //about: String,
    var bio: String,
    var birthday: String,
    var education: Array[String],
    var email: String,
    //favorite_athletes: Array[String],
    //favorite_teams: Array[String],
    var first_name: String,
    var gender: String,
    var hometown: String,
    //inspirational_people: Array[String],
    var interested_in: Array[String],
    //is_verified: Boolean,
    var languages: Array[String],
    var last_name: String,
    var link: String,
    var location: String,
    var middle_name: String,
    var political: String,
    var relationship_status: String,
    var religion: String,
    var significant_other: String,
    //sports: Array[String],
    //quotes: String,
    var updated_time: String,
    var website: String,
    var work: Array[String],
    //var public_key: String,
    var cover: String
  )

case class Status (
    id: String,
    var created_time: String,
    var from: String,
    var location: String,
    var message: String,
    var updated_time: String,
    var OCid: String
  )


case class FriendList (
    id: String,
    var members: Array[String]
  )


case class Album (
    id: String,
    var count: Int,
    var cover_photo: String,
    var created_time: String,
    var description: String,
    var from: String,
    var link: String,
    var location: String,
    var name: String,
    var place: String,
    var privacy: String,
    var updated_time: String,
    var photos: Array[String],
    var OCid: String
  )

case class Photo (
    id: String,
    var album: String,
    var created_time: String,
    var from: String,
    var image: Array[Byte],
    var link: String,
    var name: String,
    var updated_time: String,
    var place: String,
    var user_comments: Array[String],
    var user_likes: Array[String],
    var OCid: String
)

case class Comment (
    id: String,
    var object_id: String,
    var created_time: String,
    var from: String,
    var message: String,
    var parent: String,
    var user_comments: Array[String],
    var user_likes: Array[String]
)

//import ObjectType._
case class ObjectComments (
    id: String,
    var object_type: String,
    var object_id: String,
    var comments: Array[String]
)

case class FriendReqest( 
  fromid: String,
  toid: String
)

object FBJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

    implicit val albumFormat = jsonFormat14(Album)
    implicit val pageFormat = jsonFormat21(Page.apply)
    implicit val profileFormat = jsonFormat22(Profile.apply)
    implicit val statusFormat = jsonFormat7(Status.apply)
    implicit val friendListFormat = jsonFormat2(FriendList.apply)
    implicit val experienceFormat = jsonFormat5(Experience.apply)
    implicit val photoFormat = jsonFormat12(Photo.apply)
    implicit val commentFormat = jsonFormat8(Comment.apply)
    implicit val FriendReqestFormat = jsonFormat2(FriendReqest.apply)
    implicit val objectCommentsFormat = jsonFormat4(ObjectComments.apply)

}
