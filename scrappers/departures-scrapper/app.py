import pulkovo_parser
import kafka_producer
    
flights = pulkovo_parser.get_flights()
for flight in flights:
    print("sending flight " + flight["number"])
    kafka_producer.send_departure(flight)

kafka_producer.flush()
