import sbt._

object Dependencies {

  // play-json per Play line: 2.7.x (Play 2.7, artifact suffix _playjson27) and 2.10.x (Play 2.9, suffix _playjson29).
  def playJson(playJsonVersion: String): Seq[ModuleID] =
    Seq("play-json", "play-json-joda").map("com.typesafe.play" %% _ % playJsonVersion)

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.8"
  val jodaTime = "joda-time" % "joda-time" % "2.9.9"
  val scalajHttp = "org.scalaj" %% "scalaj-http" % "2.4.2"
  val jsch = "com.jcraft" % "jsch" % "0.1.55"
  val mockitoScalaScalatest =
    "org.mockito" %% "mockito-scala-scalatest" % "1.5.9" // first release built for both Scala 2.12 and 2.13 (still on scalatest 3.0.8)
}
