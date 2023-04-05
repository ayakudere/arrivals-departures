import java.time.Duration
import java.util.Properties

import Config._
import dto.AircraftCapacity.AircraftType
import dto.Arrival.ArrivalKey
import dto.Departure.DepartureKey
import dto._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig._
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig
import org.apache.kafka.streams.kstream.{Suppressed, TimeWindows}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.serialization.Serdes._

object Main extends App {
  val properties = new Properties()

  val keySerde = new StringSerde

  properties.put(BOOTSTRAP_SERVERS_CONFIG, Kafka.brokerList.mkString(","));
  properties.put(APPLICATION_ID_CONFIG, Kafka.applicationId)
  properties.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerde.getClass)

  val builder = new StreamsBuilder

  val departures = builder.stream[DepartureKey, Departure](Kafka.Topics.rawDepartures)
  val arrivals = builder.stream[ArrivalKey, Arrival](Kafka.Topics.rawArrivals)
  val aircraftCapacities = builder.globalTable[AircraftType, AircraftCapacity](Kafka.Topics.aircraftCapacities)

  def filterDuplicateEvents[K, V <: AircraftEvent](stream: KStream[K, V])(implicit keySerde: Serde[K], valueSerde: Serde[V]): KStream[K, V] = {
    stream.groupByKey
      .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(24)))
      .reduce((_, v) => v)
      .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded())) // В суточном промежутке наш кривой парсер может кидать один и тот же рейс несколько раз
      .toStream
      .map((k, v) => (k.key(), v))
  }

  def aggregateEvents[K, V <: AircraftEvent](stream: KStream[K, V], totalKeyName: String)(implicit keySerde: Serde[K]): BranchedKStream[K, AggregatedCapacity] = {

    stream.leftJoin(aircraftCapacities)((_, event) => event.aircraftType, (event, aircraftCapacity) => aircraftCapacity match {
      case null => AggregatedCapacity(event.aircraftType, None)
      case value => AggregatedCapacity(aircraftCapacity.aircraftType, Some(value.capacity))
    }).split()
      .branch(
        { case (_, capacity) => capacity.capacity.isEmpty },
        Branched.withConsumer[K, AggregatedCapacity] { stream =>
          stream.mapValues(_.aircraftType)
            .peek((_, t) => println(t))
            .to(Kafka.Topics.missingAircraftTypes)  // TODO: На самом деле нужно еще ставить null для типов с известной capacity, чтобы они чистились из топика
        }                                           //       и, возможно, сам топик не нужен, но хотелось попробовать использовать branch для чего-нибудь
      )
      .branch(
        { case (_, capacity) => capacity.capacity.nonEmpty },
        Branched.withConsumer[K, AggregatedCapacity] { stream =>
          stream.flatMapValues(_.capacity.toList)
             .selectKey((_, _) => totalKeyName)
             .groupByKey
             .reduce(_ + _)
             .toStream
             .peek((t, c) => println(s"$t: $c"))
             .to(Kafka.Topics.totalPassengerCount)
        }
      )
  }

  aggregateEvents(filterDuplicateEvents(departures), Kafka.totalDeparturesKey)
  aggregateEvents(filterDuplicateEvents(arrivals), Kafka.totalArrivalsKey)

  val topology = builder.build()

  //println(topology.describe())

  val streams = new KafkaStreams(topology, properties)

  streams.start()

  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
}