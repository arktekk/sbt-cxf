name := "generate"

scalaVersion := "2.10.2"

version := "1.0"

seq(cxf.settings : _*)

cxf.wsdls := Seq(cxf.Wsdl(file("src/main/wsdl/PingPong.wsdl"), Nil, None))
