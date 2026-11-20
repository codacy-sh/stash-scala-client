package com.codacy.client.stash

import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class CommitAndChangeTest extends WordSpec with Matchers {

  "Commit" should {
    "read the author's details" in {
      val json = Json.parse("""
        |{
        |  "id": "def0123abcdef4567abcdef8987abcdef6543abc",
        |  "displayId": "def0123abcd",
        |  "author": {
        |    "name": "charlie",
        |    "emailAddress": "charlie@example.com",
        |    "id": 101,
        |    "displayName": "Charlie",
        |    "slug": "charlie",
        |    "type": "NORMAL"
        |  },
        |  "authorTimestamp": 1548720847608,
        |  "committer": { "name": "charlie", "emailAddress": "charlie@example.com" },
        |  "committerTimestamp": 1548720847610,
        |  "message": "More work on feature 1",
        |  "parents": [{ "id": "abcdef0123abcdef4567abcdef8987abcdef6543", "displayId": "abcdef0" }]
        |}""".stripMargin)

      val commit = json.as[Commit]
      commit.hash shouldBe "def0123abcdef4567abcdef8987abcdef6543abc"
      commit.authorName shouldBe "charlie"
      commit.authorTimestamp shouldBe new DateTime(1548720847608L)
      commit.parents.map(_.map(_.id)) shouldBe Some(List("abcdef0123abcdef4567abcdef8987abcdef6543"))
      commit.author shouldBe Some(
        CommitAuthor("charlie", Some("charlie@example.com"), Some("Charlie"), Some("charlie"), Some(101L))
      )
    }

    "read an author that isn't a Bitbucket user" in {
      val json = Json.parse("""
        |{
        |  "id": "abc",
        |  "author": { "name": "Someone", "emailAddress": "someone@example.com" },
        |  "authorTimestamp": 1548720847608,
        |  "message": "m"
        |}""".stripMargin)

      json.as[Commit].author shouldBe Some(CommitAuthor("Someone", Some("someone@example.com"), None, None, None))
    }
  }

  "Change" should {
    "read the paths, before and after a move" in {
      val json = Json.parse("""
        |{
        |  "contentId": "abcdef0123abcdef4567abcdef8987abcdef6543",
        |  "path": { "components": ["new", "path", "to", "file.txt"], "parent": "new/path/to", "name": "file.txt",
        |            "extension": "txt", "toString": "new/path/to/file.txt" },
        |  "srcPath": { "components": ["path", "to", "file.txt"], "toString": "path/to/file.txt" },
        |  "executable": false,
        |  "percentUnchanged": 98,
        |  "type": "MOVE",
        |  "nodeType": "FILE"
        |}""".stripMargin)

      json.as[Change] shouldBe Change("new/path/to/file.txt", Some("path/to/file.txt"), "MOVE", Some("FILE"))
    }

    "fall back to the path's string" in {
      Json.parse("""{ "path": { "toString": "a b/c.txt" }, "type": "ADD" }""").as[Change] shouldBe
        Change("a b/c.txt", None, "ADD", None)
    }
  }
}
