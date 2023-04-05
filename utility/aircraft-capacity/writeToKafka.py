import json
import io

import avro.schema
from avro.io import DatumWriter
from kafka import KafkaProducer

config = json.load(open("../../globalConfig.json"))

TOPIC_NAME = config["topics"]["aircraftCapacities"]
BROKER_URLS = config["brokerUrls"]

producer = KafkaProducer(bootstrap_servers=BROKER_URLS, key_serializer=str.encode)

schema = avro.schema.parse(open("aircraftCapacity.avsc").read())

for aircraft_type, capacity in json.load(open("./aircraftTypesTable.json")).items():
    writer = DatumWriter(schema)
    bytes_writer = io.BytesIO()
    encoder = avro.io.BinaryEncoder(bytes_writer)
    writer.write({"aircraftType": aircraft_type, "capacity": capacity}, encoder)
    raw_bytes = bytes_writer.getvalue()
    producer.send(TOPIC_NAME, raw_bytes, key=aircraft_type)

producer.flush()

print("success!")
