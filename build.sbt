import com.gilcloud.sbt.gitlab.{GitlabCredentials,GitlabPlugin}

GitlabPlugin.autoImport.gitlabGroupId     :=  Some(73679838)
GitlabPlugin.autoImport.gitlabProjectId   :=  Some(50550924)

GitlabPlugin.autoImport.gitlabCredentials  := {
  sys.env.get("GITLAB_PRIVATE_TOKEN") match {
    case Some(token) =>
      Some(GitlabCredentials("Private-Token", token))
    case None =>
      Some(GitlabCredentials("Job-Token", sys.env.get("CI_JOB_TOKEN").get))
  }
}

lazy val scala_2_13 = "2.13.3"

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
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
    resolvers += ("gitlab" at "https://gitlab.com/api/v4/projects/50550924/packages/maven"),
    credentials += {
      sys.env.get("GITLAB_PRIVATE_TOKEN") match {
        case Some(token) =>
          Credentials("GitLab Packages Registry", "gitlab.com", "Private-Token", token)
        case None =>
          Credentials("GitLab Packages Registry", "gitlab.com", "Job-Token", sys.env.get("CI_JOB_TOKEN").get)
      }
    },
    libraryDependencies ++= {
      val kleinUtilVersion = "1.2.6"

      val configVersion = "1.4.1"
      val scalaLoggingVersion = "3.9.4"
      val logbackClassicVersion = "1.2.10"
      val scalaTestVersion = "3.2.15"
      val mongoVersion = "4.4.1"
      val nettyVersion = "4.1.73.Final"
      val scalaCollectionCompatVersion = "2.6.0"

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
