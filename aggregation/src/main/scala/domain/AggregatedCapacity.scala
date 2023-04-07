package domain

import com.sksamuel.avro4s.kafka.GenericSerde

case class AggregatedCapacity(aircraftType: String, capacity: Option[Int], country: String, airportIata: String)

object AggregatedCapacity {
  implicit val serde: GenericSerde[AggregatedCapacity] = new GenericSerde[AggregatedCapacity]

  def from(event: AircraftEvent, capacity: Option[Int]): AggregatedCapacity = {
    event match {
      case Departure(_, aircraftType, _, arrivalCountry, arrivalIata, _) => AggregatedCapacity(aircraftType, capacity, arrivalCountry, arrivalIata)
      case Arrival(_, aircraftType, _, departureCountry, departureIata, _) => AggregatedCapacity(aircraftType, capacity, departureCountry, departureIata)
    }
  }
}