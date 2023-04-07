package topologies

import domain.AggregatedCapacity
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.kstream.KStream

import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes._

object AircraftTypeAggregation {

  def aggregate[K](stream: KStream[K, AggregatedCapacity], outputTopic: String)(implicit keySerde: Serde[K]): Unit = {
    stream.groupBy((_, aggregatedCapacity) => aggregatedCapacity.aircraftType)
      .count()
      .toStream
      .to(outputTopic)
  }
}
