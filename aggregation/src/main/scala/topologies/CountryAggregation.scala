package topologies

import domain.AggregatedEvent
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.kstream.KStream

import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes._

object CountryAggregation {

  def aggregate[K](stream: KStream[K, AggregatedEvent])(implicit keySerde: Serde[K]): KStream[String, Int] = {
    stream.groupBy((_, aggregatedCapacity) => aggregatedCapacity.country)
      .aggregate(0)((_, aggregatedCapacity, current) => aggregatedCapacity.capacity.getOrElse(0) + current)
      .toStream.peek((k, v) => s"Country aggregation: $k:$v")
  }
}
