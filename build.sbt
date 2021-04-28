lazy val scala_2_13 = "2.13.3"

lazy val mongoVersion = "[4.2.0,5["
lazy val configVersion = "[1.4.0,2["

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
    libraryDependencies ++= Seq(
      "io.mdcatapult.klein" %% "util"                 % "1.2.2",

      "org.scala-lang.modules" %% "scala-collection-compat" % "[2.1.4,3[",
      "org.scalatest" %% "scalatest"                  % "[3.1.1,4[" % Test,
      "org.mongodb.scala" %% "mongo-scala-driver"     % mongoVersion,
      "ch.qos.logback" % "logback-classic"            % "[1.2.3,2[",
      "com.typesafe.scala-logging" %% "scala-logging" % "[3.9.2,4[",
      "com.typesafe" % "config"                       % configVersion,
      "io.netty" % "netty-all" % "[4.1.48.Final,5[",
    )
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
