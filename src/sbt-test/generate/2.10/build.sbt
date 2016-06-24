name := "generate"

scalaVersion := "2.10.5"

version := "1.0"

wsdls := Seq(Wsdl(file("src/main/wsdl/PingPong.wsdl"), Nil, None))
