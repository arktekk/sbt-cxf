resolvers <++= sbtVersion(sv => sv match {
 case v if (v.startsWith("0.11")) => Seq(Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns))
 case _ => Nil
})

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.5")

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")