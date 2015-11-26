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
    with_user: Array[String]
)

case class Page (
    id: String,
    about: String,
    can_post: Boolean,
    cover: String,
    description: String,
    emails: Array[String],
    is_community_page: Boolean,
    is_permanently_closed: Boolean,
    is_published: Boolean,
    like_count: Int,
    link: String,
    location: String,
    from: String,
    name: String,
    parent_page: String,
    posts: Array[String],
    phone: String,
    last_used_time: String,
    likes: Array[String],
    members: Array[String]
)

case class Profile (
    id: String,
    //about: String,
    bio: String,
    birthday: String,
    education: Array[String],
    email: String,
    //favorite_athletes: Array[String],
    //favorite_teams: Array[String],
    first_name: String,
    gender: String,
    hometown: String,
    //inspirational_people: Array[String],
    interested_in: Array[String],
    //is_verified: Boolean,
    languages: Array[String],
    last_name: String,
    link: String,
    location: String,
    middle_name: String,
    political: String,
    relationship_status: String,
    religion: String,
    significant_other: String,
    //sports: Array[String],
    //quotes: String,
    updated_time: String,
    website: String,
    work: Array[String],
    //public_key: String,
    cover: String
  )

case class PostClass (
    id: String,
    caption: String,
    created_time: String,
    description: String,
    from: String,
    icon: String,
    is_hidden: Boolean,
    link: String,
    location: String,
    message: String,
    name: String,
    object_id: String,
    picture: Array[String],
    privacy: String,
    shares: String,
    status_type: String,
    to: Array[String],
    post_type: String,
    updated_time: String
  )


case class FriendList (
    id: String,
    name: String,
    owner: String,
    members: Array[String]
  )


case class Album (
    id: String,
    count: Int,
    cover_photo: String,
    created_time: String,
    description: String,
    from: String,
    link: String,
    location: String,
    name: String,
    place: String,
    privacy: String,
    updated_time: String
  )

case class Photo (
    id: String,
    album: String,
    created_time: String,
    from: String,
    image: Array[Byte],
    link: String,
    name: String,
    updated_time: String,
    place: String,
    user_comments: Array[String],
    user_likes: Array[String]
)

case class Comment (
    id: String,
    object_id: String,
    created_time: String,
    from: String,
    message: String,
    parent: String,
    user_comments: Array[String],
    user_likes: Array[String]
)

class ObjectComments (
    var id: String,
    var object_type: String,
    var object_id: String,
    var comments: Array[String]
)


object FBJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

    implicit val albumFormat = jsonFormat12(Album)
    implicit val pageFormat = jsonFormat20(Page.apply)
    implicit val profileFormat = jsonFormat22(Profile.apply)
    implicit val postFormat = jsonFormat19(PostClass.apply)
    implicit val friendListFormat = jsonFormat4(FriendList.apply)
    implicit val experienceFormat = jsonFormat5(Experience.apply)
    implicit val photoFormat = jsonFormat11(Photo.apply)
    implicit val commentFormat = jsonFormat8(Comment.apply)
    //implicit val objectCommentsFormat = jsonFormat4(ObjectComments.apply)

}
