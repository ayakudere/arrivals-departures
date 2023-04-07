package domain

import java.sql.Timestamp

import com.sksamuel.avro4s.BinaryFormat
import com.sksamuel.avro4s.kafka.GenericSerde
import domain.AircraftCapacity.AircraftType

sealed trait AircraftEvent {
  val number: String
  val aircraftType: AircraftType
}

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