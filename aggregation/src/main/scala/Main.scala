import org.apache.kafka.streams.KafkaStreams
import topologies.MainTopology

object Main extends App {
  val streams = new KafkaStreams(MainTopology.getTopology, MainTopology.getProps)

  streams.start()

  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
}