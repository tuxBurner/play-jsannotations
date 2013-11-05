name := "play-jsannotations"

version := "1.2.0-SNAPSHOT"

organization := "com.github.tuxBurner"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
   "com.typesafe.play" %% "play" % "2.2.1",
   "com.typesafe.play" %% "play-java" % "2.2.1"
)

publishTo <<= version {
  case v if v.trim.endsWith("SNAPSHOT") => Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
  case _ => Some(Resolver.file("Github Pages",  new File("../tuxBurner.github.io/repo")))
}
