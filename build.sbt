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
    resolvers         ++= Seq(
      "MDC Nexus Releases" at "https://nexus.wopr.inf.mdc/repository/maven-releases/",
      "MDC Nexus Snapshots" at "https://nexus.wopr.inf.mdc/repository/maven-snapshots/"),
    credentials       += {
      sys.env.get("NEXUS_PASSWORD") match {
        case Some(p) =>
          Credentials("Sonatype Nexus Repository Manager", "nexus.wopr.inf.mdc", "gitlab", p)
        case None =>
          Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= {
      val kleinUtilVersion = "1.2.4"

      val configVersion = "1.4.1"
      val scalaLoggingVersion = "3.9.4"
      val logbackClassicVersion = "1.2.10"
      val scalaTestVersion = "3.2.11"
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
  ).
  settings(
    publishSettings: _*
  )
lazy val publishSettings = Seq(
  publishTo := {
    val version = if (isSnapshot.value) "snapshots" else "releases"
    Some("MDC Maven Repo" at s"https://nexus.wopr.inf.mdc/repository/maven-$version/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)
