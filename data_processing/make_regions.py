from geopy.geocoders import Nominatim

geolocator = Nominatim(user_agent="appleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Mobile Safari/537.36")

location = geolocator.geocode("jugovzhodna Slovenija", geometry="wkt")
geometry = location.raw["geotext"].replace("POLYGON((", "").replace("))", "")
geometry = geometry.split(",")

code = "PolygonOptions()"

for i, long_lat in enumerate(geometry):
    long_lat = long_lat.split(" ")
    code += f".add(LatLng({long_lat[1]}, {long_lat[0]}))\n"
    if ")" in long_lat[0] or ")" in long_lat[1]:
        print(long_lat)
        continue


# print(location.raw["geotext"])