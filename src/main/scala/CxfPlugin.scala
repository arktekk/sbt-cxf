package no.arktekk

import sbt._
import classpath.ClasspathUtilities
import sbt.Keys._
import java.io.File

object CxfPlugin extends Plugin {
  
  object cxf {
    val Config = config("cxf").hide
    val wsdl2java = TaskKey[Seq[File]]("wsdl2java", "Generates java files from wsdls")
    val wsdls = SettingKey[Seq[Wsdl]]("wsdls", "wsdls to generate java files from")
    
    case class Wsdl(file:File, args:String*)
    
    val settings = Seq(
      ivyConfigurations += Config,
      version in Config := "2.4.2",
      libraryDependencies <++= (version in Config)(version => Seq[ModuleID](
        "org.apache.cxf" % "cxf-tools-wsdlto-core" % version % Config.name,
        "org.apache.cxf" % "cxf-tools-wsdlto-databinding-jaxb" % version % Config.name,
        "org.apache.cxf" % "cxf-tools-wsdlto-frontend-jaxws" % version % Config.name
      )),
      wsdls := Nil,
      managedClasspath in wsdl2java <<= (classpathTypes in wsdl2java, update) map { (ct, report) =>
        Classpaths.managedJars(Config, ct, report)
      },
      sourceManaged in Config <<= sourceManaged(_ / "cxf"),
      managedSourceDirectories in Compile <+= (sourceManaged in Config),
      wsdl2java <<= (wsdls, sourceManaged in Config, managedClasspath in wsdl2java) map { (wsdls, output, cp) =>
        val classpath = cp.files
        for(wsdl <- wsdls){
          val args = Seq("-d", output.getAbsolutePath) ++ wsdl.args :+ wsdl.file.getAbsolutePath
          callWsdl2java(args, classpath)
        }
        (output ** "*.java").get
      },
      sourceGenerators in Compile <+= wsdl2java)

    def callWsdl2java(args:Seq[String], classpath:Seq[File]){
      val classLoader = ClasspathUtilities.toLoader(classpath)
      val WSDLToJava = classLoader.loadClass("org.apache.cxf.tools.wsdlto.WSDLToJava")
      val ToolContext = classLoader.loadClass("org.apache.cxf.tools.common.ToolContext")
      val constructor = WSDLToJava.getConstructor(classOf[Array[String]])
      val run = WSDLToJava.getMethod("run", ToolContext)
      val oldContextClassLoader = Thread.currentThread.getContextClassLoader
      try{
        // to satisfy the jaxb reflection madness classLoader requirements
        Thread.currentThread.setContextClassLoader(classLoader)
        val instance = constructor.newInstance(args.toArray)
        run.invoke(instance, ToolContext.newInstance().asInstanceOf[AnyRef])
      } finally{
        Thread.currentThread.setContextClassLoader(oldContextClassLoader)
      }
    }
  }
}