package topologies

import config.Constants.Kafka
import domain.AircraftCapacity.AircraftType
import domain.{AggregatedCapacity, AircraftCapacity, AircraftEvent}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.{Branched, KStream}
import org.apache.kafka.streams.scala.serialization.Serdes._

object CapacityJoiner {

  def aggregate[K, V <: AircraftEvent](stream: KStream[K, V], capacityTable: GlobalKTable[AircraftType, AircraftCapacity], outputKeyName: String)(implicit keySerde: Serde[K]): KStream[K, AggregatedCapacity] = {

    stream.leftJoin(capacityTable)((_, event) => event.aircraftType, (event, aircraftCapacity) => aircraftCapacity match {
      case null => AggregatedCapacity.from(event, None)
      case value => AggregatedCapacity.from(event, Some(value.capacity))
    }).split()
      .branch(
        { case (_, capacity) => capacity.capacity.isEmpty },
        Branched.withConsumer[K, AggregatedCapacity] { stream =>
          stream.mapValues(_.aircraftType)
            .peek((_, t) => println(t))
            .selectKey((_, aircraftType) => aircraftType)
            .to(Kafka.Topics.missingAircraftTypes)  // TODO: На самом деле нужно еще ставить null для типов с известной capacity, чтобы они чистились из топика
        }                                           //       и, возможно, сам топик не нужен, но хотелось попробовать использовать branch для чего-нибудь
      )
      .defaultBranch().values.head
  }
}
