package dto

import com.sksamuel.avro4s.kafka.GenericSerde
import dto.AircraftCapacity.AircraftType

case class AircraftCapacity(capacity: Int, aircraftType: AircraftType)

object AircraftCapacity {
  type AircraftType = String

  implicit val serde: GenericSerde[AircraftCapacity] = new GenericSerde[AircraftCapacity]
}