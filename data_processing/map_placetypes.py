from colorama import Fore, Style, Back


supported_place_types: list = []
dataset_place_types: list = ['NASTANITVENI PROSTOR Z NEPOSREDNO OKOLICO (DVORIŠČE, VRT IPD.)',
                             'PROSTOR ZA DENARNO-FINANČNE STORITVE',
                             'UPRAVNO-ADMINISTRATIVNI PROSTOR PODJETJA ALI DRUGE ORGANIZACIJE',
                             'PROSTOR ZA ŠPORT, REKREACIJO, ZABAVO',
                             'PROSTOR ZA INDUSTRIJSKO, KMETIJSKO ALI OBRTNO PROIZVODNJO IN STORITVE',
                             'PROSTOR DRUGIH USTANOV', 'PRODAJNI PROSTOR',
                             'PROSTOR DRŽ. USTANOVE, ORGANA LOKALNE SKUP., POLIT. ALI INTERES. ORG.',
                             'VOZILA', 'OBMOČJE PROMETA', 'POSEBEJ (ZA)VAROVANO OBMOČJE',
                             'ODPRT ALI ZAPRT SKLADIŠČNI PROSTOR',
                             'PROST. ZA VZGOJNOVAR. IN IZOBR. DEJ. Z NEPOS. OKOL.(DVORIŠČE,ŠOL.IGR.)',
                             'NARAVNA POVRŠINA',
                             'PROSTOR ZA ZDRAVSTVENE STORITVE Z NEPOSRED. OKOLICO (DVORIŠČE IPD.)',
                             'PROSTOR ZA VERSKE OBREDE', 'KOMUNALNI PROSTOR', 'GRADBIŠČE',
                             'PROSTOR ZA KULTURNO DEJAVNOST', 'URADNI PROSTORI POLICIJE']

with open("./data_processing/supported_place_types.txt", "r") as f:
    supported_place_types = f.read().splitlines()
    f.close()

place_mappings: dict = dict.fromkeys(supported_place_types, "")

for place in supported_place_types:
    print(Fore.YELLOW + "Supported place types: " + Style.RESET_ALL)
    for i, dataset_place in enumerate(dataset_place_types):
        print(Fore.YELLOW + f"Press {i} for: " + dataset_place + Style.RESET_ALL)
    while True:
      key = input(Back.GREEN + "Enter the mapping for " + place + ": " + Style.RESET_ALL)
      if key.isdigit() and int(key) in range(len(dataset_place_types)):
        key = int(key)
        break
      print(Back.RED + Fore.WHITE + "Oops didn't enter valid number" + Style.RESET_ALL)
    place_mappings[place] = dataset_place_types[key]

import json
json.dump(place_mappings, open("./data_processing/data/place_mappings.json", "w"))
