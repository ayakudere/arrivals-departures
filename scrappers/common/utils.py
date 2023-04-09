import airportsdata

airports = airportsdata.load('IATA')

def get_country_by_iata(iata):
    if (iata == "GNJ"): # until they merge https://github.com/mwgg/Airports/pull/82
        iata = "KVD"
    try:
        return airports[iata]["country"]
    except:
        return False

