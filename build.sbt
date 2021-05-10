val zioVersion = "1.0.3"
val calibanVersion = "0.10.0"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val graalLocalBuild =
  settingKey[Boolean]("Whether to build locally or with docker")

// 2.13 has issues with graal atm
//TODO: try inline on 2.13.2 with jdk 14
//scalaVersion in ThisBuild := "2.12.13"
scalaVersion in ThisBuild := "2.13.5"

lazy val streams = project
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion
    )
  )

lazy val client = project
  .settings(
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
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      // this was removed in the latest version
      //"com.github.ghostdogpr" %% "caliban-uzhttp" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-http4s" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-client" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-zio-http" % calibanVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    graalLocalBuild := true,
    graalVMNativeImageGraalVersion := {
      if (graalLocalBuild.value) None
      else Some("20.2.0")
    },
    graalVMNativeImageCommand :=
      sys.env
        .get("GRAAL_NATIVE_IMAGE")
        .getOrElse(graalVMNativeImageCommand.value),
    graalVMNativeImageOptions ++= Seq(
      "-H:+AddAllCharsets",
      "--no-fallback",
      "-H:+ReportExceptionStackTraces",
      "-H:EnableURLProtocols=http,https",
      "--initialize-at-build-time",
      "--initialize-at-build-time=scala.runtime.Statics$VM",
      "-Dio.netty.leakDetection.level=DISABLED",
      "--allow-incomplete-classpath",
      s"-H:ResourceConfigurationFiles=${(GraalVMNativeImage / resourceDirectory).value / "resources-config.json"}",
      "--features=org.graalvm.home.HomeFinderFeature"
      //"-H:-SpawnIsolates",
      // options https://github.com/oracle/graal/blob/b254ed5cde7c0aa582092b71372b4e063fd8f8e2/sdk/src/org.graalvm.nativeimage/src/META-INF/services/org.graalvm.nativeimage.Platform
      //"-Dsvm.platform=org.graalvm.nativeimage.Platform$IOS_AARCH64",
      //"-Dsvm.platform=org.graalvm.nativeimage.Platform$ANDROID_AARCH64",
      //"-Dsvm.targetName=iOS",
      //"-Dsvm.targetArch=arm64",
      //"-Dsvm.platform=org.graalvm.nativeimage.Platform$LINUX_AARCH64",
      //"-H:CompilerBackend=llvm"
    ) ++
      // needed for zio-http
      Seq(
        "io.netty.util.internal.logging.Log4JLogger",
        "io.netty.channel.DefaultFileRegion",
        "io.netty.channel.epoll.EpollEventArray",
        "io.netty.channel.unix.Errors",
        "io.netty.channel.unix.IovArray",
        "io.netty.channel.unix.Socket",
        "io.netty.channel.epoll.Native",
        "io.netty.channel.epoll.EpollEventLoop",
        "io.netty.channel.kqueue.KQueue",
        "io.netty.channel.kqueue.KQueueEventArray",
        "io.netty.channel.kqueue.KQueueEventLoop",
        "io.netty.channel.kqueue.Native",
        "io.netty.channel.unix.Limits",
        "io.netty.util.AbstractReferenceCounted"
      ).map(className => s"--initialize-at-run-time=$className") ++ {
      if (scala.util.Properties.isMac)
        Seq.empty
      else
        Seq("--static")
    },
    //Compile / mainClass := Some("com.joprice.UzHttpApp")
    //Compile / mainClass := Some("com.joprice.Http4sApp"),
    Compile / mainClass := Some("com.joprice.ZIOHttpApp"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
  .enablePlugins(GraalVMNativeImagePlugin, CodegenPlugin)

//addCompilerPlugin("io.tryp" % "splain" % "0.5.3" cross CrossVersion.patch)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("printSchema", "runMain com.joprice.PrintSchema")
addCommandAlias(
  "generateClient",
  "calibanGenClient schema.graphql client/src/main/scala/Client.scala"
)
addCommandAlias(
  "generateSchema",
  "calibanGenSchema schema.graphql client/src/main/scala/Schema.scala"
)
addCommandAlias("updateCodegen", ";printSchema ;generateClient; generateSchema")
