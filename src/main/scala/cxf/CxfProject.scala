package cxf

import org.apache.cxf.tools.wsdlto.WSDLToJava
import org.apache.cxf.tools.common.ToolContext
import sbt._

trait CxfProject extends BasicScalaProject {
  /* otherwise jaxb will throw a tantrum */
  private def jaxbFriendlyClassLoader[A](f: => A): A = {
    val old = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(getClass.getClassLoader)
      f
    } finally {
      Thread.currentThread.setContextClassLoader(old)
    }
  }

  private def arg(pre: String, a: Seq[String]) = if (a.isEmpty) Nil else pre :: a.toList

  private def arg(pre: String, a: Option[String]) = a.toList.flatMap {
    pre :: _ :: Nil
  }

  private def arg(pre: String, a: Boolean) = if (a) List(pre) else Nil

  protected def wsdlTojavaTask(wsdls: Seq[WSDL]) = task {
    FileTasks.runOption("cxf", generatedCxf from wsdls.map(_.wsdl), log) {
      jaxbFriendlyClassLoader {
        try {
          for (wsdl <- wsdls) {
            val args = wsdl.toArgs
            log.info("wsdl2java " + args.mkString(" "))
            new WSDLToJava(args.toArray).run(new ToolContext)
          }
          None
        } catch {
          case e: Exception => Some(e.getMessage)
        }
      }
    }
  }

  class WSDL(val wsdl: Path) {
    def wsdlLocation: Option[String] = None

    def packageNames: Seq[String] = Nil

    def client = false

    def exsh = false

    def toArgs: List[String] =
      arg("-d", Some(generatedCxf.absolutePath)) :::
        arg("-wsdlLocation", wsdlLocation) :::
        arg("-p", packageNames) :::
        arg("-client", client) :::
        arg("-exsh", if (exsh) Some("true") else None) :::
        List(wsdl.absolutePath)
  }

  def wsdls: Seq[WSDL]

  def generatedCxf = outputRootPath / "generated-cxf"

  def wsdl2javaAction = wsdlTojavaTask(wsdls) describedAs "Generates java from wsdl. see http://cxf.apache.org/docs/wsdl-to-java.html for more info"

  def cleanWsdl2javaAction = cleanTask(generatedCxf, cleanOptions) describedAs "Deletes all java files generated from wsdl"

  lazy val wsdl2java = wsdl2javaAction
  lazy val cleanWsdl2java = cleanWsdl2javaAction

  override def compileAction = super.compileAction dependsOn wsdl2javaAction.named("wsdl2java") describedAs "Generates java from wsdl, then compiles"

  override def cleanAction = super.cleanAction dependsOn cleanWsdl2javaAction.named("clean-wsdl2java") describedAs "Deletes all generated files (the target directory + java files generated from wsdl)"

  override def outputDirectories = generatedCxf :: super.outputDirectories.toList

  abstract override def mainSourceRoots = super.mainSourceRoots +++ generatedCxf
}
