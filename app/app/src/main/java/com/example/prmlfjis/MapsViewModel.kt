package com.example.prmlfjis

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prmlfjis.network.NominatimApi
import com.example.prmlfjis.network.NominatimJsonData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _status = MutableLiveData<String>()
    private val regije: List<String> = listOf(
        "osrednjeslovenska",
        "Obalno - kraška",
        "Primorsko - notranjska",
        "jugovzhodna Slovenija",
        "posavska",
        "zasavska",
        "savinjska",
        "podravska",
        "koroška",
        "pomurska",
        "goriška",
        "gorenjska"
    )
    private var obcine: List<String> = listOf("Ajdovščina","Ankaran","Apače","Beltinci","Benedikt","Bistrica ob Sotli","Bled","Bloke","Bohinj","Borovnica","Bovec","Braslovče","Brda","Brezovica","Brežice","Cankova","Celje","Cerklje na Gorenjskem","Cerknica","Cerkno","Cerkvenjak","Cirkulane","Črenšovci","Črna na Koroškem","Črnomelj","Destrnik","Divača","Dobje","Dobrepolje","Dobrna","Dobrova - Polhov Gradec","Dobrovnik","Dol pri Ljubljani","Dolenjske Toplice","Domžale","Dornava","Dravograd","Duplek","Gorenja vas - Poljane","Gorišnica","Gorje","Gornja Radgona","Gornji Grad","Gornji Petrovci","Grad","Grosuplje","Hajdina","Hoče - Slivnica","Hodoš","Horjul","Hrastnik","Hrpelje - Kozina","Idrija","Ig","Ilirska Bistrica","Ivančna Gorica","Izola","Jesenice","Jezersko","Juršinci","Kamnik","Kanal ob Soči","Kidričevo","Kobarid","Kobilje","Kočevje","Komen","Komenda","Koper","Kostanjevica na Krki","Kostel","Kozje","Kranj","Kranjska Gora","Križevci","Krško","Kungota","Kuzma","Laško","Lenart","Lendava",
        "Litija","Ljubljana","Ljubno","Ljutomer","Log - Dragomer","Logatec","Loška dolina","Loški Potok","Lovrenc na Pohorju","Luče","Lukovica","Majšperk","Makole","Maribor","Markovci","Medvode","Mengeš","Metlika","Mežica","Miklavž na Dravskem polju",
        "Miren - Kostanjevica","Mirna","Mirna Peč","Mislinja","Mokronog - Trebelno","Moravče","Moravske Toplice","Mozirje","Murska Sobota","Muta","Naklo","Nazarje","Nova Gorica","Novo mesto","Odranci","Oplotnica","Ormož","Osilnica","Pesnica","Piran","Pivka","Podčetrtek","Podlehnik","Podvelka","Poljčane","Polzela","Postojna","Prebold","Preddvor","Prevalje","Ptuj","Puconci","Rače - Fram","Radeče","Radenci","Radlje ob Dravi","Radovljica","Ravne na Koroškem","Razkrižje","Rečica ob Savinji","Renče - Vogrsko","Ribnica","Ribnica na Pohorju","Rogaška Slatina","Rogašovci","Rogatec","Ruše","Selnica ob Dravi","Semič","Sevnica","Sežana","Slovenj Gradec","Slovenska Bistrica","Slovenske Konjice","Sodražica","Solčava","Središče ob Dravi","Starše","Straža","Sveta Ana","Sveta Trojica v Slovenskih goricah","Sveti Andraž v Slovenskih goricah","Sveti Jurij ob Ščavnici","Sveti Jurij v Slovenskih goricah","Sveti Tomaž","Šalovci","Šempeter - Vrtojba","Šenčur","Šentilj","Šentjernej","Šentjur","Šentrupert","Škocjan","Škofja Loka","Škofljica","Šmarje pri Jelšah","Šmarješke Toplice","Šmartno pri Litiji","Šmartno ob Paki","Šoštanj","Štore","Tabor","Tišina","Tolmin","Trbovlje","Trebnje","Trnovska vas","Trzin","Tržič","Turnišče","Velenje","Velika Polana","Velike Lašče","Veržej","Videm","Vipava","Vitanje","Vodice","Vojnik","Vransko","Vrhnika","Vuzenica","Zagorje ob Savi","Zavrč","Zreče","Žalec","Železniki","Žetale","Žiri","Žirovnica","Žužemberk")

    val status: LiveData<String> = _status

    private var _areaRegionOutlines : MutableList<Pair<String, PolygonOptions>> = mutableListOf()
    private var _areaCityOutlines : MutableList<Pair<String, PolygonOptions>> = mutableListOf()
    private var _areaOutlines = MutableLiveData<MutableList<Pair<String, PolygonOptions>>>()
    var areaOutlines : LiveData<MutableList<Pair<String, PolygonOptions>>> = _areaOutlines


    init {
       makeAreaRegionOutlines()
       makeAreaCityOutlines()
    }

    fun chooseAreaOutlines(s: String) {
        if (s == "region") {
            _areaOutlines.value = _areaRegionOutlines
        } else if (s == "city") {
            _areaOutlines.value = _areaCityOutlines
        }
    }

    private fun getAreaOutline(country: String="Slovenija", city: String="") {
        viewModelScope.launch {
            try {
                val result: List<NominatimJsonData> = NominatimApi.retrofitService.getAreaOutline(country, city)
                _status.value = result[0].geotext
                Log.i("GEOTEXT: ", _status.value!!)
            } catch (e: Exception) {
                Log.i("Nemska sestka ERROR:", "${e.message}")
                _status.value = "Nemska sestka ERROR: ${e.message}"
            }
        }
    }

    private fun makeAreaRegionOutlines() {
        val result = mutableListOf<Pair<String, PolygonOptions>>()
        viewModelScope.launch {
            for (regija in regije) {
                try {
//                    Log.i("REGIJA: ", regija)
                    val polygonText = NominatimApi.retrofitService.searchQuery(query="${regija} slovenija")[0].geotext
//                    Log.i("POLY  for ${regija}: ", polygonText)
                    result.add(Pair(regija, parsePolygonText(polygonText)))
                }
                catch (e: Exception) {
                    Log.i("FAIL REGIJA: ", regija)
                    Log.i("API Request error: ", e.stackTraceToString())
                }

            }
            _areaRegionOutlines = result
            _areaOutlines.value = result
        }
    }

    private fun makeAreaCityOutlines() {
        val result = mutableListOf<Pair<String, PolygonOptions>>()
        viewModelScope.launch {
            for (obcina in obcine) {
                try {
                    val polygonText = NominatimApi.retrofitService.getAreaOutline(city="${obcina}", country="slovenija")[0].geotext
                    result.add(Pair(obcina, parsePolygonText(polygonText)))
                }
                catch (e: Exception) {
                    Log.i("FAIL OBCINA: ", obcina)
                    Log.i("API Request error: ", e.stackTraceToString())
                }

            }
            _areaCityOutlines = result
        }
    }


    private fun parsePolygonText(polygonText: String) : PolygonOptions {
        val polygonOptions = PolygonOptions()
            .strokeColor(Color.BLACK)
            .clickable(true)
        val latLngStrList = polygonText.replace("POLYGON((", "").replace("))", "").split(",")
        for (latLngStr in latLngStrList) {
            val latLng: MutableList<String> = latLngStr.split(" ") as MutableList<String>
            latLng[0] = latLng[0].replace(Regex("[^0-9.\n)]"), "")
            latLng[1] = latLng[1].replace(Regex("[^0-9.\n)]"), "")
            if (")" in latLng[1]) {
                latLng[1] = latLng[1].replace(")", "")
                polygonOptions.add(LatLng(latLng[1].toDouble(), latLng[0].toDouble()))
                break
            }
            polygonOptions.add(LatLng(latLng[1].toDouble(), latLng[0].toDouble()))
        }

        return polygonOptions
    }
}