object Config {
  object Kafka {
    object Topics {
      val rawDepartures = "raw-departures"
      val rawArrivals = "raw-arrivals"
      val totalPassengerCount = "total-passenger-count"
      val aircraftCapacities = "aircraft-capacities"
      val missingAircraftTypes = "missing-aircraft-types"
    }

    val applicationId = "arrivals-departures-stream"
    val brokerList: Seq[String] = Seq("")

    val totalArrivalsKey = "total-arrivals"
    val totalDeparturesKey = "total-departures"
  }
}
