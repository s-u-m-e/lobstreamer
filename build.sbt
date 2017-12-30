lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion    = "2.5.4"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.excella",
      scalaVersion    := "2.12.3"
    )),
    name := "lobstreamer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "0.11",

      "io.swagger" % "swagger-jaxrs" % "1.5.17",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.2",
      "co.pragmati" %% "swagger-ui-akka-http" % "1.1.0",


      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )
