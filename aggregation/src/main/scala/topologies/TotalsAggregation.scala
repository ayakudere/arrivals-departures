package topologies

import domain.AggregatedEvent
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes._

object TotalsAggregation {
  def aggregate[K](stream: KStream[K, AggregatedEvent], outputKey: String)(implicit keySerde: Serde[K]): KStream[String, Int] = {
    stream.flatMapValues(_.capacity.toList)
      .selectKey((_, _) => outputKey)
      .groupByKey
      .reduce(_ + _)
      .toStream
      .peek((t, c) => println(s"$t: $c"))
  }
}
