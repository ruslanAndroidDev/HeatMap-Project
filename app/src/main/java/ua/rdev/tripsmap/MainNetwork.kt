package ua.rdev.tripsmap

import retrofit2.http.GET
import ua.rdev.tripsmap.model.SearchTripItem

interface MainNetwork {

    @GET("getFindsData")
    suspend fun getTripSearchData(): ArrayList<SearchTripItem>
}
