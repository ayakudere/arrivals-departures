name := "arrival-departure-aggregation"

version := "0.1"

scalaVersion := "2.13.10"

val kafkaVersion = "3.4.0"
val avro4sVersion = "4.1.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion,
  "com.sksamuel.avro4s" % "avro4s-core_2.13" % avro4sVersion,
  "com.sksamuel.avro4s" % "avro4s-kafka_2.13" % avro4sVersion,
  "org.scalatest" %% "scalatest" % "3.2.15" % "test"
)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime