import io
import json

import avro.schema
from avro.io import DatumWriter
from kafka import KafkaProducer

config = json.load(open("../../globalConfig.json"))

TOPIC_NAME = config["topics"]["aircraftCapacities"]
BROKER_URLS = config["brokerUrls"]

producer = KafkaProducer(bootstrap_servers=BROKER_URLS, key_serializer=str.encode)

schema = avro.schema.parse(open("departure.avsc").read())

def send_departure(departure):
    writer = DatumWriter(schema)
    bytes_writer = io.BytesIO()
    encoder = avro.io.BinaryEncoder(bytes_writer)
    writer.write(departure, encoder)
    raw_bytes = bytes_writer.getvalue()
    producer.send(TOPIC_NAME, raw_bytes, key=departure["number"] + str(departure["departureTime"]))

def flush():
     producer.flush()
