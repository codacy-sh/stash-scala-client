package com.codacy.client.stash

import play.api.libs.functional.syntax._
import play.api.libs.json._

/** A file changed between two commits.
  *
  * @param path
  *   the file's path after the change (before it, for a deleted file)
  * @param srcPath
  *   the file's path before a move or a copy
  * @param changeType
  *   ADD, COPY, DELETE, MODIFY, MOVE or UNKNOWN
  * @param nodeType
  *   FILE, DIRECTORY or SUBMODULE
  */
final case class Change(path: String, srcPath: Option[String], changeType: String, nodeType: Option[String])

object Change {

  // A path is {"components": ["src", "Main.scala"], "toString": "src/Main.scala", ...}
  private val pathReads: Reads[String] =
    (__ \ "components").read[Seq[String]].map(_.mkString("/")).orElse((__ \ "toString").read[String])

  implicit val reader: Reads[Change] = (
    (__ \ "path").read(pathReads) and
      (__ \ "srcPath").readNullable(pathReads) and
      (__ \ "type").read[String] and
      (__ \ "nodeType").readNullable[String]
  )(Change.apply _)
}
