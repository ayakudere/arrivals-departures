import java.sql.Timestamp
import java.time.temporal.{ChronoField, TemporalAmount, TemporalField, TemporalUnit}
import java.time.{Duration, Instant, LocalDateTime, ZoneId}

import Config.Kafka
import dto.AircraftCapacity.AircraftType
import dto.Arrival.ArrivalKey
import dto.Departure.DepartureKey
import dto._
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

class TopologySpec extends AnyFunSpec with Matchers {

  implicit def getSerializer[T](implicit serde: Serde[T]): Serializer[T] = serde.serializer()
  implicit def getDeserializer[T](implicit serde: Serde[T]): Deserializer[T] = serde.deserializer()

  describe("Aggregation process") {

    val driver = new TopologyTestDriver(MainTopology.getTopology, MainTopology.getProps)

    val now = LocalDateTime.now()
    val timestamp = Timestamp.valueOf(now)
    val processingStart = now.atZone(ZoneId.systemDefault()).toInstant

    val arrivalsTopic = driver.createInputTopic(Kafka.Topics.rawArrivals, implicitly[Serializer[ArrivalKey]], implicitly[Serializer[Arrival]], processingStart, Duration.ofMinutes(1))
    val departuresTopic = driver.createInputTopic(Kafka.Topics.rawDepartures, implicitly[Serializer[DepartureKey]], implicitly[Serializer[Departure]], processingStart, Duration.ofMinutes(1))
    val capacitiesTopic = driver.createInputTopic(Kafka.Topics.aircraftCapacities, implicitly[Serializer[AircraftType]], implicitly[Serializer[AircraftCapacity]])
    val missingTypesTopic = driver.createOutputTopic(Kafka.Topics.missingAircraftTypes, implicitly[Deserializer[AircraftType]], implicitly[Deserializer[String]])
    val totalsTopic = driver.createOutputTopic(Kafka.Topics.totalPassengerCount, implicitly[Deserializer[String]], implicitly[Deserializer[Int]])

    val capacities = Seq(
      AircraftCapacity(162, "B738"),
      AircraftCapacity(140, "A319")
    )

    val arrivals = Seq(
      Arrival("TK 403", "B738", "GNJ", "AZ", "LED", timestamp),
      Arrival("TK 403", "B738", "GNJ", "AZ", "LED", timestamp),
      Arrival("TK 403", "B738", "GNJ", "AZ", "LED", timestamp),
      Arrival("TK 403", "B738", "GNJ", "AZ", "LED", timestamp),
      Arrival("TK 403", "B738", "GNJ", "AZ", "LED", timestamp),
      Arrival("U6 288", "A319", "GNJ", "AZ", "LED", timestamp)
    )

    val departures = Seq(
      Departure("HY 633", "missing", "GNJ", "AZ", "LED", timestamp),
      Departure("FV 6972", "A319", "GNJ", "AZ", "LED", timestamp),
      Departure("PC 396", "B738", "GNJ", "AZ", "LED", timestamp),
      Departure("B2 939", "B738", "GNJ", "AZ", "LED", timestamp),
    )

    capacities.foreach(capacity => capacitiesTopic.pipeInput(capacity.aircraftType, capacity))
    arrivals.foreach(arrival => arrivalsTopic.pipeInput(arrival.number, arrival))
    departures.foreach(departure => departuresTopic.pipeInput(departure.number, departure))


    // Чтобы протестировать дедупликацию с суточным окном
    arrivalsTopic.pipeInput("dummy", Arrival("dummy", "dummy", "dummy", "dummy", "dummy", timestamp), Instant.now().plus(Duration.ofHours(24)))
    departuresTopic.pipeInput("dummy", Departure("dummy", "dummy", "dummy", "dummy", "dummy", timestamp), Instant.now().plus(Duration.ofHours(24)))

    val totals = totalsTopic.readKeyValuesToMap().asScala

    it("should ignore the same key events") {
      totals.getOrElse(Kafka.totalArrivalsKey, 0) should not be(162 * 5 + 140)
    }

    it("should count arrivals correctly") {
      totals.getOrElse(Kafka.totalArrivalsKey, 0) should be(162 + 140)
    }

    it("should count departures correctly") {
      totals.getOrElse(Kafka.totalDeparturesKey, 0) should be(162 * 2 + 140)
    }

    it("should put missing aircraft types into a topic") {
      missingTypesTopic.readValue() should be("missing")
    }

    driver.close()
  }
}
