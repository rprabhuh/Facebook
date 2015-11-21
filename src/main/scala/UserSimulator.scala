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

import argonaut._, Argonaut._

case class Start()

class UserSimulator extends Actor {

	//case class Album(id: String, description: String)
	//implicit val formats = DefaultFormats

	implicit def AlbumJson: EncodeJson[Album] =
  		EncodeJson((x: Album) =>
    		("id" := x.id) ->:
    		("description" := x.description) ->: jEmptyObject)

  	def receive = {

  		case Start =>
  			println("Creating an Album..")
  			val A = new Album("123", "First Album")
  			val jsonAlbum = A.asJson
    		var content = jsonAlbum.toString()
    		println(content)
  }
}