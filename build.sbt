lazy val VERSION_MONGO_SCALA       = "2.3.0"
lazy val VERSION_SCALA             = "2.12.4"
lazy val VERSION_SCALA_TEST        = "3.1.2"
lazy val VERSION_CASSANDRA         = "3.5.0"
lazy val VERSION_AKKA_STREAM_KAFKA = "0.20"
lazy val VERSION_SCALA_MOCK        = "4.1.0"
lazy val VERSION_STTP              = "1.1.14"
lazy val VERSION_SCALA_ISO         = "0.1.2"
lazy val VERSION_PHANTOM           = "2.24.0"
lazy val VERSION_JODA              = "2.10"
lazy val VERSION_JODA_CONVERT      = "2.1"

lazy val meta = Seq(
  name := """services-schedule""",
  organization := "org.xalgorithms",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := VERSION_SCALA,
)

lazy val lib_deps = Seq(
  guice,
  "org.mongodb.scala"      %% "mongo-scala-driver"      % VERSION_MONGO_SCALA,
  "com.typesafe.akka"      %% "akka-stream-kafka"       % VERSION_AKKA_STREAM_KAFKA,
  "com.datastax.cassandra" %  "cassandra-driver-core"   % VERSION_CASSANDRA,
  "com.outworkers"         %% "phantom-dsl"             % VERSION_PHANTOM,     
  "com.softwaremill.sttp"  %% "core"                    % VERSION_STTP,
  "com.softwaremill.sttp"  %% "akka-http-backend"       % VERSION_STTP,
  "com.vitorsvieira"       %% "scala-iso"               % VERSION_SCALA_ISO,
  "joda-time"              %  "joda-time"               % VERSION_JODA,
  "org.joda"               %  "joda-convert"            % VERSION_JODA_CONVERT,
  "org.scalatestplus.play" %% "scalatestplus-play"      % VERSION_SCALA_TEST % Test,
  "org.scalamock"          %% "scalamock"               % VERSION_SCALA_MOCK % Test
)

lazy val root = (project in file("."))
  .settings(meta)
  .settings(libraryDependencies ++= lib_deps)
  .enablePlugins(PlayScala)
  
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.xalgorithms.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.xalgorithms.binders._"
