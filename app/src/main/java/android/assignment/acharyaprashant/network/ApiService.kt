package android.assignment.acharyaprashant.network

import android.assignment.acharyaprashant.model.MediaCoverage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/v2/content/misc/media-coverages")
    fun getMediaCoverages(
        @Query("limit") limit: Int
    ): Call<List<MediaCoverage>>
}