package com.codacy.client.stash.client

import com.codacy.client.stash.service.{CommitServices, RepositoryServices}
import com.sun.net.httpserver.{HttpExchange, HttpServer}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentLinkedQueue

class StashClientTextTest extends WordSpec with Matchers with BeforeAndAfterAll {

  private final case class Received(path: String, query: Option[String], accept: Option[String])

  private val received = new ConcurrentLinkedQueue[Received]()

  private val server = HttpServer.create(new InetSocketAddress("localhost", 0), 0)
  server.createContext(
    "/",
    (exchange: HttpExchange) => {
      val uri = exchange.getRequestURI
      received.add(
        Received(uri.getRawPath, Option(uri.getRawQuery), Option(exchange.getRequestHeaders.getFirst("Accept")))
      )
      val (status, body) =
        if (uri.getRawPath.endsWith("/missing")) (404, """{"errors":[{"message":"not found"}]}""")
        else (200, "diff --git a/f b/f\n+line\n")
      val bytes = body.getBytes(StandardCharsets.UTF_8)
      exchange.sendResponseHeaders(status, bytes.length.toLong)
      exchange.getResponseBody.write(bytes)
      exchange.close()
    }
  )

  override def beforeAll(): Unit = server.start()
  override def afterAll(): Unit = server.stop(0)

  private lazy val client = new StashClient(s"http://localhost:${server.getAddress.getPort}")

  private def lastRequest(): Received = {
    var last: Received = null
    while (!received.isEmpty) last = received.poll()
    last
  }

  "executeText" should {
    "return the body as text, asking for text" in {
      val response = client.executeText("/rest/x")(Map("a" -> "1"))

      response.hasError shouldBe false
      response.value shouldBe Some("diff --git a/f b/f\n+line\n")
      lastRequest() shouldBe Received("/rest/x", Some("a=1"), Some("text/plain"))
    }

    "report an error status" in {
      val response = client.executeText("/rest/missing")()

      response.hasError shouldBe true
      response.value shouldBe None
      response.message should include("404")
    }
  }

  "CommitServices.getDiff" should {
    "ask for the raw diff from since to until" in {
      new CommitServices(client).getDiff("PRJ", "repo", since = Some("base"), until = "head").value shouldBe
        Some("diff --git a/f b/f\n+line\n")

      val request = lastRequest()
      request.path shouldBe "/rest/api/1.0/projects/PRJ/repos/repo/diff"
      request.query.toSeq.flatMap(_.split("&")).toSet shouldBe Set("until=head", "since=base")
      request.accept shouldBe Some("text/plain")
    }

    "compare a commit with its parent without since" in {
      new CommitServices(client).getDiff("PRJ", "repo", since = None, until = "head")
      lastRequest().query shouldBe Some("until=head")
    }
  }

  "RepositoryServices.getRawFile" should {
    "encode the path and read the file at a commit" in {
      new RepositoryServices(client).getRawFile("PRJ", "repo", "src/my file.scala", at = Some("abc")).value shouldBe
        Some("diff --git a/f b/f\n+line\n")

      val request = lastRequest()
      request.path shouldBe "/rest/api/1.0/projects/PRJ/repos/repo/raw/src/my%20file.scala"
      request.query shouldBe Some("at=abc")
      request.accept shouldBe Some("*/*")
    }
  }

  "RepositoryServices.encodePath" should {
    "encode each segment and keep the slashes" in {
      RepositoryServices.encodePath("/a b/c+d/é.txt") shouldBe "a%20b/c%2Bd/%C3%A9.txt"
    }
  }
}
