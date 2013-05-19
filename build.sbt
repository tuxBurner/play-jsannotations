name := "play-jsannotations"

version := "1.0.0"

organization := "com.github.tuxBurner"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
   "play" %% "play" % "2.1.1",
   "play" %% "play-java" % "2.1.1"
)

publishTo <<= version {
  case v if v.trim.endsWith("SNAPSHOT") => Some(Resolver.file("Github Pages", Path.userHome / "workspace_play" / "tuxburner.github.io" / "repo-snapshots" asFile))
  case _ => Some(Resolver.file("Github Pages", Path.userHome / "workspace_play" / "tuxburner.github.io" / "repo" asFile))
}

javacOptions ++= Seq("-source", "1.6")
