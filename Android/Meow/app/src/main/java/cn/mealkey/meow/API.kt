package cn.mealkey.meow


import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part


interface API {
    @Multipart
    @POST("upload")
    fun upload(@Part("description") description: RequestBody,
               @Part file: MultipartBody.Part): Observable<ResponseBody>
}