package dto

import java.sql.Timestamp

import com.sksamuel.avro4s.kafka.GenericSerde
import dto.AircraftCapacity.AircraftType

case class Arrival(number: String,
                   aircraftType: AircraftType,
                   departureIata: String,
                   departureCountry: String,
                   arrivalIata: String,
                   arrivalTime: Timestamp) extends AircraftEvent

object Arrival {
  type ArrivalKey = String

  implicit val serde: GenericSerde[Arrival] = new GenericSerde[Arrival]
}
