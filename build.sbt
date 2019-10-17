lazy val Scala212 = "2.12.8"
lazy val Scala211 = "2.11.12"
lazy val Scala210 = "2.10.7"

lazy val mongoVersion = "2.6.0"
lazy val configVersion = "1.3.2"

lazy val root = (project in file(".")).
  settings(
    name                := "mongo",
    organization        := "io.mdcatapult.klein",
    scalaVersion        := Scala212,
    crossScalaVersions  := Scala212 :: Scala211 :: Scala210 :: Nil,
    version             := "0.0.3",
    scalacOptions += "-Ypartial-unification",
    resolvers         ++= Seq(
      "MDC Nexus Releases" at "http://nexus.mdcatapult.io/repository/maven-releases/",
      "MDC Nexus Snapshots" at "http://nexus.mdcatapult.io/repository/maven-snapshots/"),
    credentials       += {
      val nexusPassword = sys.env.get("NEXUS_PASSWORD")
      if ( nexusPassword.nonEmpty ) {
        Credentials("Sonatype Nexus Repository Manager", "nexus.mdcatapult.io", "gitlab", nexusPassword.get)
      } else {
        Credentials(Path.userHome / ".sbt" / ".credentials")
      }
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"                  % "3.0.3" % Test,
      "org.mongodb.scala" %% "mongo-scala-driver"     % mongoVersion,
      "ch.qos.logback" % "logback-classic"            % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.typesafe" % "config"                       % configVersion,
    )
  ).
  settings(
    publishSettings: _*
  )
lazy val publishSettings = Seq(
  publishTo := {
    if (isSnapshot.value)
      Some("MDC Maven Repo" at "https://nexus.mdcatapult.io/repository/maven-snapshots/")
    else
      Some("MDC Maven Repo" at "https://nexus.mdcatapult.io/repository/maven-releases/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)

