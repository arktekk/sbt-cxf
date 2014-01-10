package no.arktekk

import sbt._
import sbt.classpath.ClasspathUtilities
import sbt.Keys._
import java.io.File

object CxfPlugin extends Plugin {

  object cxf {
    val Config = config("cxf").hide
    val wsdl2java = TaskKey[Seq[File]]("wsdl2java", "Generates java files from wsdls")
    val wsdls = SettingKey[Seq[Wsdl]]("wsdls", "wsdls to generate java files from")

    case class Wsdl(file: File, args: Seq[String], key: Option[String] = None) {
      val id = key.getOrElse(args.mkString.hashCode.abs).toString

      def outputDirectory(basedir: File) =
        new File(basedir, id).getAbsoluteFile
    }

    val settings = Seq(
      ivyConfigurations += Config,
      version in Config := "2.7.8",
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
      managedSourceDirectories in Compile <++= (wsdls, sourceManaged in Config) { (wsdls, basedir) =>
        wsdls map { _.outputDirectory(basedir) }
      },
      wsdl2java <<= (wsdls, sourceManaged in Config, managedClasspath in wsdl2java) map { (wsdls, basedir, cp) =>
        val classpath = cp.files
        val x =
        for (wsdl <- wsdls) yield {
          val output = wsdl.outputDirectory(basedir)
          if(wsdl.file.lastModified() > output.lastModified()) {
            val args = Seq("-d", output.getAbsolutePath) ++ wsdl.args :+ wsdl.file.getAbsolutePath
            callWsdl2java(wsdl.id, output, args, classpath)
          }

          val files = (output ** "*.java").get
//          println(wsdl.id + ", files=" + files.length)
          files
        }
        val files = x.flatten
//        println("files=" + files.length)
        files
      },
      sourceGenerators in Compile <+= wsdl2java)

    def callWsdl2java(id: String, output: File, args: Seq[String], classpath: Seq[File]) {
      // TODO: Use the built-in logging mechanism from SBT when I figure out how that work - trygve
      println("WSDL: id=" + id + ", args=" + args)
      println("Removing output directory...")
      IO.delete(output)

      println("Compiling WSDL...")
      val start = System.currentTimeMillis()
      val classLoader = ClasspathUtilities.toLoader(classpath)
      val WSDLToJava = classLoader.loadClass("org.apache.cxf.tools.wsdlto.WSDLToJava")
      val ToolContext = classLoader.loadClass("org.apache.cxf.tools.common.ToolContext")
      val constructor = WSDLToJava.getConstructor(classOf[Array[String]])
      val run = WSDLToJava.getMethod("run", ToolContext)
      val oldContextClassLoader = Thread.currentThread.getContextClassLoader
      try {
        // to satisfy the jaxb reflection madness classLoader requirements
        Thread.currentThread.setContextClassLoader(classLoader)
        val instance = constructor.newInstance(args.toArray)
        run.invoke(instance, ToolContext.newInstance().asInstanceOf[AnyRef])
      } catch {
        case e =>
          // TODO: Figure out if there is a better way to signal errors to SBT.
          // Some of the CXF exceptions contain output that's proper to show to
          // the user as it explains the error that occurred.
          e.printStackTrace
          throw e
      } finally {
        val end = System.currentTimeMillis()
        println("Compiled WSDL in " + (end - start) + "ms.");
        Thread.currentThread.setContextClassLoader(oldContextClassLoader)
      }
    }
  }
}
