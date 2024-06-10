package android.assignment.acharyaprashant.model

data class MediaCoverage(
    val id: String,
    val title: String,
    val language: String,
    val thumbnail: Thumbnail,
    val mediaType: Int,
    val coverageURL: String,
    val publishedAt: String,
    val publishedBy: String
)

data class Thumbnail(
    val id: String,
    val version: Int,
    val domain: String,
    val basePath: String,
    val key: String,
    val qualities: List<Int>,
    val aspectRatio: Float
)

