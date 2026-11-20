package com.codacy.client.stash.service

import com.codacy.client.stash.{Change, Commit, CommitComment}
import com.codacy.client.stash.client.{PageRequest, Request, RequestResponse, StashClient}
import play.api.libs.json.Json

class CommitServices(client: StashClient) {

  private def repositoryUrl(projectKey: String, repo: String) = s"/rest/api/1.0/projects/$projectKey/repos/$repo"

  /*
   * Gets a commit
   */
  def getCommit(projectKey: String, repo: String, commitId: String): RequestResponse[Commit] =
    client.execute(Request(s"${repositoryUrl(projectKey, repo)}/commits/$commitId", classOf[Commit]))()

  /*
   * Gets the best common ancestor of two commits (git merge-base)
   */
  def getMergeBase(projectKey: String, repo: String, commitId: String, otherCommitId: String): RequestResponse[Commit] =
    client.execute(Request(s"${repositoryUrl(projectKey, repo)}/commits/$commitId/merge-base", classOf[Commit]))(
      Map("otherCommitId" -> otherCommitId)
    )

  /*
   * Gets the raw (unified) diff between two commits: from `since` to `until`, comparing their trees.
   * Without `since`, `until` is compared with its first parent.
   */
  def getDiff(
      projectKey: String,
      repo: String,
      since: Option[String],
      until: String,
      contextLines: Option[Int] = None
  ): RequestResponse[String] =
    client.executeText(s"${repositoryUrl(projectKey, repo)}/diff")(
      Map("until" -> until) ++ since.map("since" -> _) ++ contextLines.map("contextLines" -> _.toString)
    )

  /*
   * Gets the files changed between two commits: from `since` to `until`, comparing their trees.
   * Without `since`, `until` is compared with its first parent.
   */
  def getChanges(
      projectKey: String,
      repo: String,
      since: Option[String],
      until: String,
      pageRequest: Option[PageRequest] = None
  ): RequestResponse[Seq[Change]] = {
    val request = Request(s"${repositoryUrl(projectKey, repo)}/changes", classOf[Seq[Change]])
    val params = Map("until" -> until) ++ since.map("since" -> _)

    pageRequest match {
      case Some(page) => client.executePaginatedWithPageRequest(request, page)(params)
      case None => client.executePaginated(request)(params)
    }
  }

  def createComment(projectKey: String, repo: String, commit: String, body: String): RequestResponse[CommitComment] = {
    val url = s"/rest/api/1.0/projects/$projectKey/repos/$repo/commits/$commit/comments"

    val values = Json.obj(
      "text" -> body
      //      , "anchor" -> Json.obj(
      //        "line" -> 1,
      //        "lineType" -> "CONTEXT",
      //        "fileType" -> "FROM",
      //        "path" -> "path/to/file",
      //        "srcPath" -> "path/to/file"
      //      )
    )

    client.postJson(Request(url, classOf[CommitComment]), values)
  }

}
