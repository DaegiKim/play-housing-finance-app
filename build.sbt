name := """play-housing-finance-app"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "org.glassfish.jaxb"% "jaxb-core"% "2.3.0.1"
libraryDependencies += "org.glassfish.jaxb"% "jaxb-runtime"% "2.3.2"
libraryDependencies += "com.opencsv"% "opencsv"% "4.1"
libraryDependencies += "io.jsonwebtoken" % "jjwt-api" % "0.10.7"
libraryDependencies += "io.jsonwebtoken" % "jjwt-impl" % "0.10.7"
libraryDependencies += "io.jsonwebtoken" % "jjwt-jackson" % "0.10.7"
libraryDependencies += "com.github.signaflo" % "timeseries" % "0.4"