package dto

import java.sql.Timestamp

import com.sksamuel.avro4s.BinaryFormat
import com.sksamuel.avro4s.kafka.GenericSerde
import dto.AircraftCapacity.AircraftType

case class Departure(number: String,
                     aircraftType: AircraftType,
                     arrivalIata: String,
                     arrivalCountry: String,
                     departureIata: String,
                     departureTime: Timestamp) extends AircraftEvent

object Departure {
  type DepartureKey = String

  implicit val serde: GenericSerde[Departure] = new GenericSerde[Departure](BinaryFormat)
}