package no.arktekk

import sbt._
import sbt.classpath.ClasspathUtilities
import sbt.Keys._
import java.io.File

object CxfPlugin extends AutoPlugin {
  override val trigger = allRequirements

  import autoImport._

  override val requires = sbt.plugins.IvyPlugin

  override def projectSettings = Seq(
    ivyConfigurations += Cxf,
    cxfVersion := "3.1.6",
    libraryDependencies <++= (cxfVersion)(version => Seq[ModuleID](
      "org.apache.cxf" % "cxf-tools-wsdlto-core" % version % "cxf->default",
      "org.apache.cxf" % "cxf-tools-wsdlto-databinding-jaxb" % version % "cxf->default",
      "org.apache.cxf" % "cxf-tools-wsdlto-frontend-jaxws" % version % "cxf->default"
    )),
    wsdls := Nil,
    managedClasspath in wsdl2java <<= (classpathTypes in wsdl2java, update) map { (ct, report) =>
        Classpaths.managedJars(Cxf, ct, report)
    },
    sourceManaged in wsdl2java <<= sourceManaged(_ / "cxf"),
    managedSourceDirectories in Compile <++= (wsdls, sourceManaged in wsdl2java) { (wsdls, basedir) =>
      wsdls map { _.outputDirectory(basedir) }
    },
    wsdl2java <<= (wsdls, sourceManaged in wsdl2java, managedClasspath in wsdl2java, streams in Compile) map { (wsdls, basedir, cp, strm) =>
      val classpath = cp.files
      val x =
      for (wsdl <- wsdls) yield {
        val output = wsdl.outputDirectory(basedir)
        if(wsdl.file.lastModified() > output.lastModified()) {
          val args = Seq("-d", output.getAbsolutePath) ++ wsdl.args :+ wsdl.file.getAbsolutePath
          callWsdl2java(wsdl.id, output, args, classpath, strm.log)
        }

        val files = (output ** "*.java").get
        files
      }
      val files = x.flatten
      files
    },
    sourceGenerators in Compile <+= wsdl2java
  )

  def callWsdl2java(id: String, output: File, args: Seq[String], classpath: Seq[File], logger: Logger) {
    // TODO: Use the built-in logging mechanism from SBT when I figure out how that work - trygve
    logger.info("WSDL: id=" + id + ", args=" + args)
    logger.info("Removing output directory...")
    IO.delete(output)

    logger.info("Compiling WSDL...")
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
      case e: Throwable =>
        logger.error("Failed to compile wsdl with exception: " + e.getMessage)
        logger.trace(e)
        // TODO: Figure out if there is a better way to signal errors to SBT.
        // Some of the CXF exceptions contain output that's proper to show to
        // the user as it explains the error that occurred.
        //e.printStackTrace
        //throw e
    } finally {
      val end = System.currentTimeMillis()
      logger.info("Compiled WSDL in " + (end - start) + "ms.");
      Thread.currentThread.setContextClassLoader(oldContextClassLoader)
    }
  }

  object autoImport {
    val Cxf = config("cxf").hide

    val wsdl2java = TaskKey[Seq[File]]("wsdl2java", "Generates java files from wsdls")
    val wsdls = SettingKey[Seq[Wsdl]]("wsdls", "wsdls to generate java files from")
    val cxfVersion = SettingKey[String]("cxfVersion", "Use this version of cxf")

    case class Wsdl(file: File, args: Seq[String], key: Option[String] = None) {
      val id = key.getOrElse(args.mkString.hashCode.abs).toString

      def outputDirectory(basedir: File) =
        new File(basedir, id).getAbsoluteFile
    }
  }
}
