ThisBuild / versionScheme := Some("early-semver")

val doclibUtilVersion = "2.0.0"

val configVersion = "1.4.3"
val scalaLoggingVersion = "3.9.5"
val logbackClassicVersion = "1.5.6"
val scalaTestVersion = "3.2.19"
val mongoVersion = "4.11.1"
val nettyVersion = "4.1.108.Final"
val scalaCollectionCompatVersion = "2.12.0"

lazy val scala_2_13 = "2.13.14"

lazy val packageRepoOwner = sys.env.getOrElse("GITHUB_USERNAME", "")

lazy val root = (project in file("."))
  .settings(
    name                := "mongo",
    organization        := "io.doclib",
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
    githubOwner := packageRepoOwner,
    githubRepository := sys.env.getOrElse("GITHUB_PACKAGE_REPO", "scala-packages"),
    resolvers += Resolver.githubPackages(packageRepoOwner),
    libraryDependencies ++= {
      Seq(
        "io.doclib" %% "common-util" % doclibUtilVersion,

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
  .dependsOn(root % "compile->compile")
