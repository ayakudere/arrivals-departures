import random
import requests
import time
from datetime import datetime
import sys
sys.path.append("..")
from common.utils import get_country_by_iata

API_URL = "https://pulkovoairport.ru/api/?type=departure"
DEPARTED_STATUS = "Departed"
DEPARTURE_IATA = "LED"

def generate_response_from_row(row):
    return {
        "number": row["OD_FLIGHT_NUMBER"],
        "aircraftType": row["OD_RACT_ICAO_CODE"],
        "departureTime": int(datetime.fromisoformat(row["OD_BOARDING_END_ACTUAL"]).timestamp()),
        "arrivalCountry": get_country_by_iata(row["OD_RAP_CODE_DESTINATION"]),
        "arrivalIata": row["OD_RAP_CODE_DESTINATION"],
        "departureIata": DEPARTURE_IATA
    }

def is_international(row):
    arrival_country = get_country_by_iata(row["OD_RAP_CODE_DESTINATION"])
    return arrival_country and arrival_country != "RU"

def get_flights():
    params = {"when": 0, "_": str(time.time()).replace(".", "")[0:13]}
    headers = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:108.0) Gecko/20100101 Firefox/108.0"}
    response = requests.get(API_URL, params=params, headers=headers).json()
    departed_flights = list(filter(lambda row: row["OD_STATUS_EN"] == DEPARTED_STATUS, response))
    international_flights = list(filter(is_international, departed_flights))
    
    return list(map(generate_response_from_row, international_flights))
