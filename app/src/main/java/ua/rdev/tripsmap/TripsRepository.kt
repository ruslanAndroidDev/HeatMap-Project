package ua.rdev.tripsmap

import androidx.lifecycle.MutableLiveData
import ua.rdev.tripsmap.model.SearchTripItem

class TripsRepository(var network: MainNetwork) {
    var searches = MutableLiveData<ArrayList<SearchTripItem>>()


    suspend fun loadTripSearchData() {
        var response = network.getTripSearchData()
        searches.postValue(response)
    }
}