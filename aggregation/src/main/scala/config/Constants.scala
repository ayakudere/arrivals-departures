package config

object Constants {
  object Kafka {
    object Topics {
      val rawDepartures = "raw-departures"
      val rawArrivals = "raw-arrivals"
      val totalPassengerCount = "total-passenger-count"
      val aircraftCapacities = "aircraft-capacities"
      val missingAircraftTypes = "missing-aircraft-types"
      val arrivalsByCountry = "arrivals-by-country"
      val departuresByCountry = "departures-by-country"
      val aircraftTypeCounts = "aircraft-type-counts"
    }

    val applicationId = "arrivals-departures-stream"
    val brokerList: Seq[String] = Seq()

    val totalArrivalsKey = "total-arrivals"
    val totalDeparturesKey = "total-departures"
  }
}
