useGpg := true

sbtPlugin := true

name := "sbt-cxf"

organization := "no.arktekk.sbt"

version := "0.1-SNAPSHOT"

aetherCredentials := {
  val file = Path.userHome / ".sbt" / "arktekk-credentials"
  if (file.exists()) Some(Credentials(file)) else None
}

deployRepository <<= (version) { version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }

seq(aetherSettings: _*)

publish <<= aether.AetherKeys.deploy

homepage := Some(new URL("http://github.com/arktekk/sbt-cxf"))

startYear := Some(2011)

licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")))

pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ xml.Group(
  <scm>
    <url>http://github.com/arktekk/sbt-cxf</url>
    <connection>scm:git:git://github.com/arktekk/sbt-cxf.git</connection>
    <developerConnection>scm:git:git@github.com:arktekk/sbt-cxf.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>trygvis</id>
      <name>Trygve Laugstøl</name>
      <url>http://twitter.com/trygvis</url>
    </developer>
    <developer>
      <id>jteigen</id>
      <name>Jon Anders Teigen</name>
      <url>http://twitter.com/jteigen</url>
    </developer>
    <developer>
      <id>thoraage</id>
      <name>Thor Åge Eldby</name>
      <url>https://twitter.com/thoraageeldby</url>
    </developer>
  </developers>
)}
