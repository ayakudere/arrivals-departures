package dto

import dto.AircraftCapacity.AircraftType

trait AircraftEvent {
  val number: String
  val aircraftType: AircraftType
}
