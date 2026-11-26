package com.codacy.client.stash

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class Commit(
    hash: String,
    authorName: String,
    parents: Option[List[Parent]],
    authorTimestamp: DateTime,
    message: String,
    author: Option[CommitAuthor] = None
)

object Commit {
  implicit val reader: Reads[Commit] = (
    (__ \ "id").read[String] and
      (__ \ "author" \ "name").read[String] and
      (__ \ "parents").readNullable[List[Parent]] and
      (__ \ "authorTimestamp").read[Long].map(new DateTime(_)) and
      (__ \ "message").read[String] and
      (__ \ "author").readNullable[CommitAuthor]
  )(Commit.apply _)
}

/** The author of a commit. `id` and `slug` are only there when the author's email belongs to a Bitbucket user. */
final case class CommitAuthor(
    name: String,
    emailAddress: Option[String],
    displayName: Option[String],
    slug: Option[String],
    id: Option[Long]
)

object CommitAuthor {
  implicit val reader: Reads[CommitAuthor] = Json.reads[CommitAuthor]
}

final case class Parent(id: String, displayId: String)

object Parent {
  implicit val fmt: Format[Parent] = Json.format[Parent]
}
