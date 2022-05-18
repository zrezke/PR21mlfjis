with open("data/obcine_slovenije.csv", "r", encoding="windows-1250") as f:
    for line in f:
        obcina = line.split(',')[0]
        print(f"\"{obcina}\",")