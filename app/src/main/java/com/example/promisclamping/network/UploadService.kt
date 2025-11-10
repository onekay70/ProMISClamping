// UploadService.kt
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponseForm(
    val bucketname: String? = null,
    val pathId: String? = null
    // You can add extra fields if your server returns them
)

interface UploadService {
    @Multipart
    @POST(".") // post to the baseUrl path (e.g., /upload/)
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,                         // MUST be named "file"
        @Part("bucketname") bucketName: RequestBody             // MUST be "bucketname"
    ): Response<UploadResponseForm>
}
