import org.apache.kafka.streams.KafkaStreams

object Main extends App {
  val streams = new KafkaStreams(MainTopology.getTopology, MainTopology.getProps)

  streams.start()

  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
}