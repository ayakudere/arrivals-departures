import pulkovo_parser
import json
import sys
sys.path.append("..")
from common.kafka_producer import AircraftEventsKafkaProducer

config = json.load(open("../../globalConfig.json"))

TOPIC_NAME = config["topics"]["rawArrivals"]
BROKER_URLS = config["brokerUrls"]
SCHEMA_FILE = open("arrivals.avsc")

producer = AircraftEventsKafkaProducer(TOPIC_NAME, BROKER_URLS, SCHEMA_FILE)
    
flights = pulkovo_parser.get_flights()
for flight in flights:
    print("sending flight " + flight["number"])
    producer.send(flight)

producer.flush()
