name := "hamill"

organization := "de.vorb"

version := "0.1.0"

scalaVersion := "2.10.0"


homepage := Some(url("https://github.com/pvorb/hamill"))

licenses := Seq("MIT License" -> url("http://vorb.de/license/mit.html"))

mainClass := None


// Dependencies
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.0"


publishArtifact in Test := false

publishTo <<= version { (version: String) =>
  val repo = "/dev/web/repo.vorb.de/public/"
  if (version.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("file", new File(repo + "snapshots")))
  else
    Some(Resolver.file("file", new File(repo + "releases")))
}

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:pvorb/hamill.git</url>
    <connection>scm:git:git@github.com:pvorb/hamill.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pvorb</id>
      <name>Paul Vorbach</name>
      <email>paul@vorb.de</email>
      <url>http://paul.vorba.ch</url>
      <timezone>+1</timezone>
    </developer>
  </developers>)

