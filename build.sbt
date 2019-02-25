lazy val Scala212 = "2.12.8"
lazy val Scala211 = "2.11.12"
lazy val Scala210 = "2.10.7"

lazy val mongoVersion = "2.5.0"
lazy val configVersion = "1.3.2"

lazy val root = (project in file(".")).
  settings(
    name                := "mongo",
    organization        := "io.mdcatapult.klein",
    scalaVersion        := Scala212,
    crossScalaVersions  := Scala212 :: Scala211 :: Scala210 :: Nil,
    version             := "0.0.1",
    scalacOptions += "-Ypartial-unification",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "org.mongodb.scala" %% "mongo-scala-driver" % mongoVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.typesafe" % "config"                   % configVersion,
    )
  ).
  settings(
    publishSettings: _*
  )

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Artifactory Realm" at "https://artifactory.mdcatapult.io/artifactory/sbt-release;build.timestamp=" + new java.util.Date().getTime)
    else
      Some("Artifactory Realm" at "https://artifactory.mdcatapult.io/artifactory/sbt-release")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)

