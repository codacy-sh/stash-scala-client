val scala212 = "2.12.18"
// The newest 2.13 that scoverage 2.3.0 supports; binary compatible with later 2.13 (portal uses 2.13.18).
val scala213 = "2.13.16"

// play-json line per Play version: the artifact is named stash-scala-client_playjson<suffix>.
val play27 = "2.7.4" // Play 2.7 -> _playjson27 (the only artifact published until now)
val play29 = "2.10.7" // Play 2.9 -> _playjson29 (what today's portal and remote-provider-service use)

lazy val playJsonVersion = settingKey[String]("The version of play-json used for building.")
ThisBuild / playJsonVersion := play27

def playSuffix(playJsonVersion: String): String =
  if (playJsonVersion.startsWith("2.7.")) "27"
  else if (playJsonVersion.startsWith("2.10.")) "29"
  else sys.error(s"Unsupported play-json version $playJsonVersion: add its Play suffix to playSuffix")

/** Creates `cross<Command>` aliases that run a command on the whole Scala x play-json matrix (`test:compile` becomes
  * `crossTestCompile`), the same way codacy's bitbucket-scala-client does.
  */
def addCrossAlias(command: String) = {
  val matrix = Seq(scala212 -> Seq(play27), scala213 -> Seq(play29))
  addCommandAlias(
    s"cross${command.split(':').map(_.capitalize).mkString}",
    matrix
      .flatMap {
        case (scalaV, playVersions) =>
          s"""set ThisBuild / scalaVersion := "$scalaV"""" +: playVersions
            .flatMap(playV => Seq(s"""set ThisBuild / playJsonVersion := "$playV"""", command))
      }
      .mkString(";")
  )
}

addCrossAlias("update")
addCrossAlias("compile")
addCrossAlias("test:compile")
addCrossAlias("test")
addCrossAlias("it:test")
addCrossAlias("publishLocal")
addCrossAlias("publishSigned")

ThisBuild / scalaVersion := scala212

val stashScalaClient = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "stash-scala-client",
    crossScalaVersions := Seq(scala212, scala213),
    scalacOptions := Seq("-deprecation", "-feature", "-unchecked", "-Xlint") ++
      (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => Seq("-Ywarn-adapted-args")
        case _ => Seq.empty // the 2.13 equivalent is part of -Xlint
      }),
    evictionErrorLevel := Level.Info,
    libraryDependencySchemes ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
    ),
    libraryDependencies ++= Seq(Dependencies.jodaTime, Dependencies.scalajHttp) ++
      Dependencies.playJson(playJsonVersion.value),
    libraryDependencies ++= Seq(Dependencies.scalatest, Dependencies.mockitoScalaScalatest, Dependencies.jsch)
      .map(_ % "test,it"),
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    organizationName := "Codacy",
    organizationHomepage := Some(new URL("https://www.codacy.com")),
    startYear := Some(2015),
    description := "Stash Scala Client",
    licenses := Seq(
      "The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/codacy/stash-scala-client.git")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/codacy/stash-scala-client"),
        "scm:git:git@github.com:codacy/stash-scala-client.git"
      )
    ),
    pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray),
    publicMvnPublish
  )
  .settings(name := s"${name.value}_playjson${playSuffix(playJsonVersion.value)}")
