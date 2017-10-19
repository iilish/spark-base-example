resolvers += Resolver.mavenLocal

lazy val hello = taskKey[Unit]("An example task")

lazy val commonSettings = Seq(
  organization := "home.org",
  version := "1.0",
  scalaVersion := "2.11.8"
)
val sparkVersion = "2.1.0"

val spark = Seq(
  ("org.apache.spark" %% "spark-core" % sparkVersion withSources())
    .exclude("javax.servlet", "servlet-api"),
  "org.apache.spark" %% "spark-streaming" % sparkVersion withSources(),
  "org.apache.spark" %% "spark-mllib" % sparkVersion withSources(),
  "org.apache.spark" %% "spark-sql" % sparkVersion withSources()
)

val others = Seq(
  // not relevant, just allows me to pass command line options to spark job
  "args4j" % "args4j" % "2.33",
  "com.bizo" % "args4j-helpers_2.10" % "1.0.0",

  // other libraries
  "org.apache.hadoop" % "hadoop-client" % "2.6.0",
  "org.apache.avro" % "avro" % "1.7.7",
  "com.typesafe" % "config" % "1.2.1",
  "com.databricks" % "spark-avro_2.11" % "3.1.0"
)

val tests = Seq(
  "org.scalactic" %% "scalactic" % "3.0.4",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

logBuffered in Test := false

assemblyMergeStrategy in assembly := {
  case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
  case PathList("javax", "inject", xs@_*) => MergeStrategy.last
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// https://github.com/JetBrains/intellij-scala/wiki/%5BSBT%5D-How-to-use-provided-libraries-in-run-configurations
lazy val intellijRunner = project.in(file("intellijRunner")).dependsOn(RootProject(file("."))).settings(
  scalaVersion := "2.11.8",
  libraryDependencies ++= spark.map(_ % "compile")
).disablePlugins(sbtassembly.AssemblyPlugin)

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name in Compile := "SparkTemplateExample",
    libraryDependencies ++= spark.map(_ % "provided") ++ others ++ tests,
    dependencyOverrides += "org.scalatest" %% "scalatest" % "3.0.4",
    hello := {
      println("Hello!")
    },
    mainClass in assembly := Some("UserEvents")
  )

// https://github.com/sbt/sbt-assembly  # exclude transitive dependencies example
libraryDependencies ~= {
  _ map {
    case m if m.name == "hadoop-hdfs" =>
      m.exclude("commons-cli", "commons-cli").
        exclude("commons-codec", "commons-codec")
    case m => m
  }
}

/* including scala bloats your assembly jar unnecessarily, and may interfere with
   spark runtime */
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyJarName in assembly := "user-events-1.0.jar"

/* you need to be able to undo the "provided" annotation on the dependencies when running your spark
   programs locally i.e. from sbt; this bit re-includes the full classpaths in the compile and run tasks. */
// fullClasspath in Runtime := (fullClasspath in (Compile, run)).value
