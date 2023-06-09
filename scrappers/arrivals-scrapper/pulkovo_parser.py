import random
import requests
import time
from datetime import datetime
import sys
sys.path.append("..")
from common.utils import get_country_by_iata

API_URL = "https://pulkovoairport.ru/api/?type=arrival"
ARRIVED_STATUS = "Arrived"
ARRIVAL_IATA = "LED"

def generate_response_from_row(row):
    return {
        "number": row["OA_FLIGHT_NUMBER"],
        "aircraftType": row["OA_RACT_ICAO_CODE"],
        "arrivalTime": int(datetime.fromisoformat(row["OA_ATA"]).timestamp()),
        "departureCountry": get_country_by_iata(row["OA_RAP_CODE_ORIGIN"]),
        "departureIata": row["OA_RAP_CODE_ORIGIN"],
        "arrivalIata": ARRIVAL_IATA
    }

def is_international(row):
    departure_country = get_country_by_iata(row["OA_RAP_CODE_ORIGIN"])
    return departure_country and departure_country != "RU"

def get_flights():
    params = {"when": 0, "_": str(time.time()).replace(".", "")[0:13]}
    headers = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/108.0"}
    response = requests.get(API_URL, params=params, headers=headers).json()
    arrived_flights = list(filter(lambda row: row["OA_STATUS_EN"] == ARRIVED_STATUS, response))
    international_flights = list(filter(is_international, arrived_flights))

    return list(map(generate_response_from_row, international_flights))
