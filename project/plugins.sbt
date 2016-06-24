addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
