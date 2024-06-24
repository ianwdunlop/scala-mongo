val kleinUtilVersion = "1.2.7"

val configVersion = "1.4.3"
val scalaLoggingVersion = "3.9.5"
val logbackClassicVersion = "1.5.6"
val scalaTestVersion = "3.2.15"
val mongoVersion = "4.4.1"
val nettyVersion = "4.1.73.Final"
val scalaCollectionCompatVersion = "2.12.0"

lazy val creds = {
  sys.env.get("CI_JOB_TOKEN") match {
    case Some(token) =>
      Credentials("GitLab Packages Registry", "gitlab.com", "gitlab-ci-token", token)
    case _ =>
      Credentials(Path.userHome / ".sbt" / ".credentials")
  }
}

// Registry ID is the project ID of the project where the package is published, this should be set in the CI/CD environment
val registryId = sys.env.get("REGISTRY_HOST_PROJECT_ID").getOrElse("")

lazy val scala_2_13 = "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name                := "mongo",
    organization        := "io.mdcatapult.klein",
    scalaVersion        := scala_2_13,
    crossScalaVersions  := scala_2_13 :: Nil,
    scalacOptions ++= Seq(
      "-encoding", "utf-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-Xlint",
      "-Xfatal-warnings"),
    useCoursier := false,
    resolvers ++= Seq(
      "gitlab" at s"https://gitlab.com/api/v4/projects/$registryId/packages/maven",
      "Maven Public" at "https://repo1.maven.org/maven2"),
    publishTo := {
      Some("gitlab" at s"https://gitlab.com/api/v4/projects/$registryId/packages/maven")
    },
    credentials += creds,
    libraryDependencies ++= {
      Seq(
        "io.mdcatapult.klein" %% "util" % kleinUtilVersion,

        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion,
        "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
        "org.mongodb.scala" %% "mongo-scala-driver" % mongoVersion,
        "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "com.typesafe" % "config" % configVersion,
        "io.netty" % "netty-all" % nettyVersion,
      )
    }
  )

Global / excludeLintKeys += Test / sourceDirectories

lazy val it = project
  .in(file("it"))  //it test located in a directory named "it"
  .settings(
    name := "mongo-it",
    scalaVersion := "2.13.14",
    Test / sourceDirectories ++= (root / Test / sourceDirectories).value,
    libraryDependencies ++= {
      Seq(
        "io.mdcatapult.klein" %% "util" % kleinUtilVersion,

        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionCompatVersion,
        "org.scalatest" %% "scalatest" % scalaTestVersion,
        "org.mongodb.scala" %% "mongo-scala-driver" % mongoVersion,
        "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "com.typesafe" % "config" % configVersion,
        "io.netty" % "netty-all" % nettyVersion
      )
    }
  )
  .dependsOn(root % "test->test;compile->compile")
