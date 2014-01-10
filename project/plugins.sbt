addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
