package transformers

import java.time.Duration

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig
import org.apache.kafka.streams.kstream.{Suppressed, TimeWindows}
import org.apache.kafka.streams.scala.kstream.KStream

import org.apache.kafka.streams.scala.ImplicitConversions._

object StreamFilters {
  def filterDuplicates24HoursWindow[K, V](stream: KStream[K, V])(implicit keySerde: Serde[K], valueSerde: Serde[V]): KStream[K, V] = {
    stream.groupByKey
      .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(24)))
      .reduce((_, v) => v)
      .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
      .toStream
      .map((k, v) => (k.key(), v))
  }
}
