package topologies

import domain.AggregatedEvent
import domain.AircraftCapacity.AircraftType
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes._

object AircraftTypeAggregation {

  def aggregate[K](stream: KStream[K, AggregatedEvent])(implicit keySerde: Serde[K]): KStream[AircraftType, Long] = {
    stream.groupBy((_, aggregatedCapacity) => aggregatedCapacity.aircraftType)
      .count()
      .toStream
  }
}
