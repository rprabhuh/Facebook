name := "facebook"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( 
 "com.typesafe.akka" % "akka-actor_2.11" % "2.3.4",
 "com.typesafe.akka" % "akka-remote_2.11" % "2.3.4" exclude("com.typesafe.akka", "akka-remote_2.10"))

libraryDependencies += "io.spray" %% "spray-can" % "1.3.2"
libraryDependencies += "io.spray" %% "spray-routing" % "1.3.2"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"

libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.1"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies repo" at "http://nightlies.spray.io"

