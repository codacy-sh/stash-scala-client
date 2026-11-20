# stash-scala-client

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3c6fbd37c5ec45eeadab6e98d7c55b27)](https://www.codacy.com/gh/codacy/stash-scala-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/stash-scala-client&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/3c6fbd37c5ec45eeadab6e98d7c55b27)](https://www.codacy.com/gh/codacy/stash-scala-client?utm_source=github.com&utm_medium=referral&utm_content=codacy/stash-scala-client&utm_campaign=Badge_Coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codacy/stash-scala-client_2.11)

This is a simple library that aims to have basic functions of the Stash API.
This library was meant to support Codacy when interacting with Stash.
It is in a very early stage and all the contributions are welcome.

## Usage

### Import in SBT

There's one artifact per Play JSON line:

```scala
// Scala 2.12, Play 2.7 (play-json 2.7)
"com.codacy" %% "stash-scala-client_playjson27" % "<VERSION>"

// Scala 2.13, Play 2.9 (play-json 2.10)
"com.codacy" %% "stash-scala-client_playjson29" % "<VERSION>"
```

### Use in code

```scala
// OAuth 1.0a (an application link): the consumer key and private key, and the user's access token and secret
val authenticator = new OAuth1Authenticator("consumerKey", "consumerPrivateKey", "token", "tokenSecret")
val client = new StashClient("https://bitbucket.example.com", Some(authenticator))

val repoServices = new RepositoryServices(client)

val response = repoServices.getRepositories("projectKey", pageRequest = None)
```

## Creators

1. Rodrigo Fernandes

## Contributors

1. Pedro Rijo

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt,
helps you improve your code quality,
teaches best practices to your developers,
and helps you save time in Code Reviews.

### Among Codacy’s features

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.

### License

stash-scala-client is available under the The Apache Software License, Version 2.0.
