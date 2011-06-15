import sbt._

class CxfPluginProject(info: ProjectInfo) extends PluginProject(info) with IdeaProject {
  val cxfVersion = "2.3.0"

  val wsdlto = "org.apache.cxf" % "cxf-tools-wsdlto-core" % cxfVersion
  val jaxb = "org.apache.cxf" % "cxf-tools-wsdlto-databinding-jaxb" % cxfVersion
  val frontend = "org.apache.cxf" % "cxf-tools-wsdlto-frontend-jaxws" % cxfVersion
}
