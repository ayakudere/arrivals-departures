package topologies

import java.util.Properties

import config.Constants.Kafka
import domain.AircraftCapacity.AircraftType
import domain.Arrival.ArrivalKey
import domain.Departure.DepartureKey
import domain._
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.StreamsConfig.{APPLICATION_ID_CONFIG, BOOTSTRAP_SERVERS_CONFIG, DEFAULT_KEY_SERDE_CLASS_CONFIG}
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.serialization.Serdes._
import transformers.StreamFilters.filterDuplicates24HoursWindow

object MainTopology {

  def getProps: Properties = {
    val properties = new Properties()

    val keySerde = new StringSerde

    properties.put(BOOTSTRAP_SERVERS_CONFIG, Kafka.brokerList.mkString(","));
    properties.put(APPLICATION_ID_CONFIG, Kafka.applicationId)
    properties.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerde.getClass)

    properties
  }

  def getTopology: Topology = {
    val builder = new StreamsBuilder

    val departures = builder.stream[DepartureKey, Departure](Kafka.Topics.rawDepartures)
    val arrivals = builder.stream[ArrivalKey, Arrival](Kafka.Topics.rawArrivals)
    val aircraftCapacities = builder.globalTable[AircraftType, AircraftCapacity](Kafka.Topics.aircraftCapacities)

    // В суточном промежутке наш кривой парсер может кидать один и тот же рейс несколько раз
    val filteredDepartures = filterDuplicates24HoursWindow(departures)
    val filteredArrivals = filterDuplicates24HoursWindow(arrivals)

    val aggregatedArrivals = CapacityJoiner.aggregate(filteredArrivals, aircraftCapacities, Kafka.totalArrivalsKey)
    val aggregatedDepartures = CapacityJoiner.aggregate(filteredDepartures, aircraftCapacities, Kafka.totalDeparturesKey)

    CountryAggregation.aggregate(aggregatedArrivals).to(Kafka.Topics.arrivalsByCountry)
    CountryAggregation.aggregate(aggregatedDepartures).to(Kafka.Topics.departuresByCountry)

    AircraftTypeAggregation.aggregate(aggregatedArrivals.merge(aggregatedDepartures), Kafka.Topics.aircraftTypeCounts)

    TotalsAggregation.aggregate(aggregatedArrivals, Kafka.totalArrivalsKey).to(Kafka.Topics.totalPassengerCount)
    TotalsAggregation.aggregate(aggregatedDepartures, Kafka.totalDeparturesKey).to(Kafka.Topics.totalPassengerCount)

    builder.build()
  }
}
