sbtPlugin := true

name := "sbt-cxf"

organization := "no.arktekk"

version := "0.1-SNAPSHOT"

credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials")

publishTo <<= (version) { version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    else
      Some("Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }
