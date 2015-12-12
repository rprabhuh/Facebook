import spray.routing._
import spray.json._
import spray.httpx._
import spray.http._

case class Experience (
    id: String,
    description: String,
    name: String,
    from: String,
    var with_user: Array[String]
)

case class Page (
    var auth: String,
    id: String,
    var about: String,
    var can_post: Boolean,
    var cover: String,
    var description: String,
    var emails: Array[String],          //enc
    var is_community_page: Boolean,//remove
    var is_permanently_closed: Boolean,//remove
    var is_published: Boolean,//remove
    var like_count: Int,
    var link: String,
    var location: String,
    var from: String,
    var name: String,
    var parent_page: String,
    var posts: Array[String],           //enc
    var phone: String,//remove
    var last_used_time: String,
    var likes: Array[String],
    var members: Array[String],
    var OCid: String
)

case class Profile (
    var auth: String,
    id: String,
    var bio: String,
    var birthday: String,     //enc
    var email: String,        //enc
    var first_name: String,
    var gender: String,
    var hometown: String,
    var interested_in: Array[String],
    var languages: Array[String],
    var last_name: String,
    var link: String,
    var location: String,
    var middle_name: String,
    var political: String,
    var relationship_status: String,
    var religion: String,
    var significant_other: String,
    var updated_time: String,
    var website: String,
    var work: Array[String],
    var cover: String
  )

case class Status (
    var auth: String,
    id: String,
    var created_time: String,
    var from: String,
    var location: String,
    var message: String,        //enc
    var updated_time: String,
    var OCid: String
  )


case class FriendList (
    id: String,
    var members: Array[String]
  )


case class Album (
    var auth: String,
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
    var auth: String,
    id: String,
    var album: String,
    var created_time: String,
    var from: String,
    var image: Array[Byte], //enc
    var link: String,
    var name: String,
    var updated_time: String,
    var place: String,
    var user_comments: Array[String],
    var user_likes: Array[String],
    var OCid: String,
    var encKey: Array[Byte]
)

case class Comment (
    id: String,
    var object_id: String,
    var created_time: String,
    var from: String,
    var message: String,//enc
    var parent: String,
    var user_comments: Array[String],
    var user_likes: Array[String]
)

//import ObjectType._
case class ObjectComments (
    id: String,
    var object_type: String,
    var object_id: String,
    var comments: Array[String]//enc
)

case class FriendReqest( 
  var auth: String,
  fromid: String,
  toid: String
)

object FBJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

    implicit val albumFormat = jsonFormat15(Album)
    implicit val pageFormat = jsonFormat22(Page.apply)
    implicit val profileFormat = jsonFormat22(Profile.apply)
    implicit val statusFormat = jsonFormat8(Status.apply)
    implicit val friendListFormat = jsonFormat2(FriendList.apply)
    implicit val experienceFormat = jsonFormat5(Experience.apply)
    implicit val photoFormat = jsonFormat14(Photo.apply)
    implicit val commentFormat = jsonFormat8(Comment.apply)
    implicit val FriendReqestFormat = jsonFormat3(FriendReqest.apply)
    implicit val objectCommentsFormat = jsonFormat4(ObjectComments.apply)
}
