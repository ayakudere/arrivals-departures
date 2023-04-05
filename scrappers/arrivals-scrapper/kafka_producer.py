import io
import json

import avro.schema
from avro.io import DatumWriter
from kafka import KafkaProducer

config = json.load(open("../../globalConfig.json"))

TOPIC_NAME = config["topics"]["rawArrivals"]
BROKER_URLS = config["brokerUrls"]

producer = KafkaProducer(bootstrap_servers=BROKER_URLS, key_serializer=str.encode)

schema = avro.schema.parse(open("arrivals.avsc").read())

def send_arrival(arrival):
    writer = DatumWriter(schema)
    bytes_writer = io.BytesIO()
    encoder = avro.io.BinaryEncoder(bytes_writer)
    writer.write(arrival, encoder)
    raw_bytes = bytes_writer.getvalue()
    producer.send(TOPIC_NAME, raw_bytes, key=arrival["number"] + str(arrival["arrivalTime"]))

def flush():
     producer.flush()
