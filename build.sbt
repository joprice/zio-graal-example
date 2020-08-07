val zioVersion    = "1.0.0"
val calibanVersion = "0.7.7"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val graalLocalBuild = settingKey[Boolean]("Whether to build locally or with docker")

lazy val streams = project
  .settings(
    scalaVersion := "2.12.11",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion
    )
  )

lazy val client = project
  .settings(
    scalaVersion := "2.12.11",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-client" % calibanVersion
    )
  )

lazy val root = (project in file("."))
  .aggregate(client, streams)
  .settings(
    organization := "com.joprice",
    name := "zio-graal-example",
    version := "0.0.1",
    // 2.13 has issues with graal atm
    //TODO: try inline on 2.13.2 with jdk 14
    scalaVersion := "2.12.11",
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-uzhttp" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-http4s" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-client" % calibanVersion
    ),
    graalLocalBuild := true,
    graalVMNativeImageGraalVersion := {
      if (graalLocalBuild.value) None
      else Some("20.0.0")
    },
    graalVMNativeImageCommand :=
      sys.env.get("GRAAL_NATIVE_IMAGE")
        .getOrElse(graalVMNativeImageCommand.value),
    graalVMNativeImageOptions ++= Seq(
      "-H:+AddAllCharsets",
      "--no-fallback",
      "-H:+ReportExceptionStackTraces",
      "-H:EnableURLProtocols=http,https",
      "--initialize-at-build-time",
      "--initialize-at-build-time=scala.runtime.Statics$VM",
      "--allow-incomplete-classpath",
      //"-H:ReflectionConfigurationFiles=/opt/graalvm/stage/resources/reflection.json"
    ) ++ {
      if (scala.util.Properties.isMac)
        Seq.empty
      else
        Seq("--static")
    },
    mainClass in Compile := Some("com.joprice.UzHttpApp")
    //mainClass in Compile := Some("com.joprice.Http4sApp")
  )
    .enablePlugins(GraalVMNativeImagePlugin, CodegenPlugin)

//addCompilerPlugin("io.tryp" % "splain" % "0.5.3" cross CrossVersion.patch)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("printSchema", "runMain com.joprice.PrintSchema")
addCommandAlias("generateClient", "calibanGenClient schema.graphql client/src/main/scala/Client.scala")
addCommandAlias("generateSchema", "calibanGenSchema schema.graphql client/src/main/scala/Schema.scala")
addCommandAlias("updateCodegen", ";printSchema ;generateClient; generateSchema")
