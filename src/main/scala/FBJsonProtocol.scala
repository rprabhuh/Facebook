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
    with_user: List[String]
)

case class Page (
    id: String,
    about: String,
    can_post: Boolean,
    cover: String,
    description: String,
    emails: List[String],
    is_community_page: Boolean,
    is_permanently_closed: Boolean,
    is_published: Boolean,
    like_count: Int ,
    link: String,
    location: String,
    messages: List[String],
    message_count: Int,
    name: String,
    parent_page: String,
    posts: List[String],
    phone: String,
    last_used_time: String,
    likes: Int ,
    members: List[String]
)

case class Profile (
    id: String,
    //about: String,
    bio: String,
    birthday: String,
    education: List[String],
    email: String,
    //favorite_athletes: List[String],
    //favorite_teams: List[String],
    first_name: String,
    gender: String,
    hometown: String,
    //inspirational_people: List[String],
    interested_in: List[String],
    //is_verified: Boolean,
    languages: List[String],
    last_name: String,
    link: String,
    location: String,
    middle_name: String,
    political: String,
    relationship_status: String,
    religion: String,
    significant_other: String,
    //sports: List[String],
    //quotes: String,
    updated_time: String,
    website: String,
    work: List[String],
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
    picture: List[String],
    privacy: String,
    shares: String,
    status_type: String,
    to: List[String],
    post_type: String,
    updated_time: String
  )


case class FriendList (
    id: String,
    name: String,
    owner: String,
    members: List[String]
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
    updated_time: String,
    owner: String
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
    user_comments: List[String],
    user_likes: List[String]
)

case class Comment (
    id: String,
    created_time: String,
    from: String,
    message: String,
    parent: String,
    user_comments: List[String],
    user_likes: List[String]
)

case class ObjectComments (
    id: String,
    object_type: String,
    object_id: String,
    comments: List[String]
)


object FBJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

    implicit val albumFormat = jsonFormat13(Album)
    implicit val pageFormat = jsonFormat21(Page.apply)
    implicit val profileFormat = jsonFormat22(Profile.apply)
    implicit val postFormat = jsonFormat19(PostClass.apply)
    implicit val friendListFormat = jsonFormat4(FriendList.apply)
    implicit val experienceFormat = jsonFormat5(Experience.apply)
    implicit val photoFormat = jsonFormat11(Photo.apply)
    implicit val commentFormat = jsonFormat7(Comment.apply)
    implicit val objectCommentsFormat = jsonFormat4(ObjectComments.apply)

}
