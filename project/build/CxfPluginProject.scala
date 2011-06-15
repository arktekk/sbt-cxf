import sbt._

class CxfPluginProject(info: ProjectInfo) extends PluginProject(info) with IdeaProject {	
	val wsdlto = "org.apache.cxf" % "cxf-tools-wsdlto-core" % "2.3.0"
	val jaxb = "org.apache.cxf" % "cxf-tools-wsdlto-databinding-jaxb" % "2.3.0"
	val frontend = "org.apache.cxf" % "cxf-tools-wsdlto-frontend-jaxws" % "2.3.0"
}
