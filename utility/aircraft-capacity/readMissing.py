import json

from kafka import KafkaConsumer

config = json.load(open("../../globalConfig.json"))

TOPIC_NAME = config["topics"]["missingAircraftTypes"]
BROKER_URLS = config["brokerUrls"]

processed = set()

consumer = KafkaConsumer(TOPIC_NAME, bootstrap_servers=BROKER_URLS, auto_offset_reset='earliest')

for msg in consumer:
    if msg.value not in processed:
        processed.add(msg.value)
        print(msg.value)
