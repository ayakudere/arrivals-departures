package dto

import com.sksamuel.avro4s.kafka.GenericSerde

case class AggregatedCapacity(aircraftType: String, capacity: Option[Int])

object AggregatedCapacity {
  implicit val serde: GenericSerde[AggregatedCapacity] = new GenericSerde[AggregatedCapacity]
}