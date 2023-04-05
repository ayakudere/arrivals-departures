import random
import requests
import time
from datetime import datetime

API_URL = "https://pulkovoairport.ru/api/?type=departure"
DEPARTED_STATUS = "Departed"
DEPARTURE_IATA = "LED"

def generate_response_from_row(row):
    return {
        "number": row["OD_FLIGHT_NUMBER"],
        "aircraftType": row["OD_RACT_ICAO_CODE"],
        "departureTime": int(datetime.fromisoformat(row["OD_BOARDING_END_ACTUAL"]).timestamp()),
        "arrivalCountry": row["OD_RAP_DESTINATION_RCO_CODE"],
        "arrivalIata": row["OD_RAP_CODE_DESTINATION"],
        "departureIata": DEPARTURE_IATA
    }

def get_flights():
    params = {"when": 0, "_": str(time.time()).replace(".", "")[0:13]}
    headers = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/108.0"}
    response = requests.get(API_URL, params=params, headers=headers).json()
    departed_flights = list(filter(lambda row: row["OD_STATUS_EN"] == DEPARTED_STATUS, response))
    international_flights = list(filter(lambda row: row["OD_RAP_DESTINATION_RCO_CODE"] != "RU", departed_flights))
    
    return list(map(generate_response_from_row, international_flights))
