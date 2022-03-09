# Analiza varnosti v Sloveniji

## Člani skupine

| Ime in priimek | Vpisna številka |
| -------------- | --------------- |
| Filip Jeretina | 63200120        |
| Mark Loboda    | 63200173        |
| Irinej Slapal  | 63200268        |

## Podatki
Podatke bomo pridobili iz strani [podatki.gov.si](https://podatki.gov.si) iz podatkovne množice [Kazniva dejanja od leta 2009 dalje](https://podatki.gov.si/dataset/mnzpkazniva-dejanja-od-leta-2009-dalje).

## Cilji
- Vpliv substanc na kazniva dejnja in vrste teh dejanj
- Vpliv državljanstva na kazniva dejnja in vrste teh dejanj
- Povezava med časovnim obdobjem in pogostostjo dejanj
- Varnost posameznih občin
  - Vrste kaznivih dejanj
- Aplikacija, ki prikazuje podatke o kaznivih dejanjih po občinah


## Opis podatkovne zbirke
Uporabili bomo podatke za leto 2019, ki se nahajajo v csv datoteki.  
Struktura baze za kazniva dejanja (KD):

- številka za štetje in ločevanje posameznega kaznivega dejanja  
- datum storitve kaznivega dejanja (MM.LLLL)  
- ura storitve kaznivega dejanja (intervali)  
- dan v tednu  
- PU storitve kaznivega dejanja  
- atribut o tem, ali je osumljenec policiji znan ali ne (povratnik)  
- klasifikacija kaznivega dejanja (zakon/člen/odstavek/točka/alinea - tekst člena)  
- poglavje zakonika  
- vrsta kriminalitete (splošna/gospodarska)  
- vrsta kriminalitete (organizirana)  
- vrsta kriminalitete (mladoletniška)  
- dokončanost kaznivega dejanja  
- kriminalistična označba 1  
- kriminalistična označba 2  
- kriminalistična označba 3  
- uporabljeno sredstvo 1  
- uporabljeno sredstvo 2  
- uporabljeno sredstvo 3  
- uporabljeno sredstvo 4  
- upravna enota, kjer je bilo storjeno kaznivo dejanje  
- podroben opis prizorišča kaznivega dejanja  
- leto zaključnega dokumenta  
- vrsta zaključnega dokumenta  
- številka za štetje in ločevanje oseb, udeleženih v kaznivih dejanjih  
- kot kaj nastopa oseba v kaznivem dejanju  
- starostni razred, ki mu oseba pripada ob storitvi kaznivega dejanja  
- spol  
- državljanstvo osebe (le slovensko in tuje)  
- poškodba osebe  
- vpliv alkohola  
- vpliv mamil  
- pripadnost organizirani združbi  
- materialna škoda v EUR  
