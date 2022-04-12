# 1. vmesno poročilo

## Opis problema
S pomočjo baze podatkov, ki opisuje pretekla kriminalna dejanja skušamo na zemljevidu prikazati čimbolj uporabne informacije o krminalu na nekem območju.  
Poleg tega bi za poljubno destinacijo na slovenskem ozemlju podali oceno varnosti lokacije.

## Opis podatkov

### Klasifikacija kaznivega dejanja
Najbolj objektivna ocena koliko je neko kaznivo dejanje "težko", je kazen, ki jo za neko dejanje prejme oseba po zakonu.  
Klasifikacija kaznivega dejanja je stolpec, ki nam poda po katerem zakonu je bila neka oseba ovadena za kaznivo dejanje.
#### Format podatkov:
- zakon/člen/odstavek/točka/alinea - tekst člena

Primer podatka: **KD02/46/1// - UMOR**  
#### Težave
- Sam zakon nam ne pove ničesar, hočemo dobiti oceno kazni za neko kaznivo dejanje.
  - Za to lahko uporabimo:
    -  maksimalno kazen, ki je predpisana po zakonu
    -  podatke o kaznih, ki so jih v preteklosti dobile osebe, ki so bile obsojene po tem zakonu.
       -  Te bodatke bomo dobili iz podatkovne množice: [Polnoletni obsojenci (znani storilci) po spolu, kaznivem dejanju in glavni kazenski sankciji (tudi pogojno obsojeni), Slovenija, letno](https://podatki.gov.si/dataset/surs1360301s)
- Podatki vsebujejo vnose iz večih različnih kazenskih zakonikov, npr. KD02 KZ01...
  - Imena kaznivih dejanj (tekst člena) se v veliki meri ujemajo med zakoniki, tako da se bomo za določanje maksimalne kazni za neko dejanje sklicevali na člene iz [trenutno veljavnega zakonika](http://pisrs.si/Pis.web/pregledPredpisa?id=ZAKO5050).
    - Vrstice kjer se imena ne ujemajo bomo izpustili...
  - Primer neujemanja med zakoniki:
    - KD02/103/1// - spolni napad na osebo, mlajšo od 14 let.
    - KZ01/173/1// - spolni napad na osebo, mlajšo od petnajst let

## Glavne ugotovitve
