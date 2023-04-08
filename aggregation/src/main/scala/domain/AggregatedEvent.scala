package domain

import com.sksamuel.avro4s.kafka.GenericSerde
import domain.AircraftCapacity.AircraftType

case class AggregatedEvent(number: String, aircraftType: AircraftType, capacity: Option[Int], country: String, airportIata: String)

object AggregatedEvent {
  implicit val serde: GenericSerde[AggregatedEvent] = new GenericSerde[AggregatedEvent]

  def from(event: AircraftEvent, capacity: Option[Int]): AggregatedEvent = {
    event match {
      case Departure(number, aircraftType, _, arrivalCountry, arrivalIata, _) => AggregatedEvent(number, aircraftType, capacity, arrivalCountry, arrivalIata)
      case Arrival(number,  aircraftType, _, departureCountry, departureIata, _) => AggregatedEvent(number, aircraftType, capacity, departureCountry, departureIata)
    }
  }
}