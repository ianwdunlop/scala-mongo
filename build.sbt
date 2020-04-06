lazy val scala_2_13 = "2.13.1"
lazy val scala_2_12 = "2.12.10"

lazy val mongoVersion = "2.8.0"
lazy val configVersion = "1.4.0"

lazy val root = (project in file(".")).
  settings(
    name                := "mongo",
    organization        := "io.mdcatapult.klein",
    scalaVersion        := scala_2_13,
    crossScalaVersions  := scala_2_13 :: scala_2_12 :: Nil,
    scalacOptions ++= Seq(
      "-encoding", "utf-8",
      "-unchecked",
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-Xlint"),
    resolvers         ++= Seq(
      "MDC Nexus Releases" at "https://nexus.mdcatapult.io/repository/maven-releases/",
      "MDC Nexus Snapshots" at "https://nexus.mdcatapult.io/repository/maven-snapshots/"),
    credentials       += {
      sys.env.get("NEXUS_PASSWORD") match {
        case Some(p) =>
          Credentials("Sonatype Nexus Repository Manager", "nexus.mdcatapult.io", "gitlab", p)
        case None =>
          Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.3",
      "org.scalatest" %% "scalatest"                  % "3.1.1" % Test,
      "org.mongodb.scala" %% "mongo-scala-driver"     % mongoVersion,
      "ch.qos.logback" % "logback-classic"            % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "com.typesafe" % "config"                       % configVersion,
    )
  ).
  settings(
    publishSettings: _*
  )
lazy val publishSettings = Seq(
  publishTo := {
    val version = if (isSnapshot.value) "snapshots" else "releases"
    Some("MDC Maven Repo" at s"https://nexus.mdcatapult.io/repository/maven-$version/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)
