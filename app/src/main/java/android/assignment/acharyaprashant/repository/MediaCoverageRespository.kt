package android.assignment.acharyaprashant.repository

import android.assignment.acharyaprashant.network.RetrofitClient
import retrofit2.awaitResponse

class MediaCoverageRepository {

    suspend fun fetchImages(): List<String> {
        return try {
            val response = RetrofitClient.apiService.getMediaCoverages(limit = 100)
            response.awaitResponse().body()?.map { mediaCoverage ->
                val thumbnail = mediaCoverage.thumbnail
                "${thumbnail.domain}/${thumbnail.basePath}/0/${thumbnail.key}"
            }?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return an empty list on error
        }
    }

}

