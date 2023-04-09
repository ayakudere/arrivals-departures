import io
import json

import avro.schema
from avro.io import DatumWriter
from kafka import KafkaProducer
from datetime import datetime

class AircraftEventsKafkaProducer:
    def __init__(self, topic_name, broker_urls, schema_file):
        self.producer = producer = KafkaProducer(bootstrap_servers=broker_urls, key_serializer=str.encode)
        self.topic_name = topic_name
        self.schema = avro.schema.parse(schema_file.read())

    def send(self, event):
        writer = DatumWriter(self.schema)
        bytes_writer = io.BytesIO()
        encoder = avro.io.BinaryEncoder(bytes_writer)
        writer.write(event, encoder)
        raw_bytes = bytes_writer.getvalue()
        self.producer.send(self.topic_name, raw_bytes, key=event["number"] + datetime.now().strftime("%y%m%d"))

    def flush(self):
        self.producer.flush()
    
        
