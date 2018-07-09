package cn.mealkey.meow

import android.app.Application
import android.util.Log
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import retrofit2.Retrofit
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import java.io.File
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class MeowApplication : Application() {
    var retrofit:Retrofit? = null
    var api:API? = null

    companion object {
        var instance:MeowApplication? = null
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun createClient(baseUrl:String){
        var client = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl("http://"+baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //.addConverterFactory(GsonConverterFactory.create())
                .build()
        api = retrofit?.create(API::class.java)

    }

    fun upload(filePath:String){
        api?.let {
            val file = File(filePath)
            val requestFile = RequestBody.create(MediaType.parse("application/otcet-stream"), file)

            val body = MultipartBody.Part.createFormData("file", file.getName(), requestFile)

            val descriptionString = "This is a description"
            val description = RequestBody.create(
                    MediaType.parse("multipart/form-data"), descriptionString)

            it.upload(description, body).subscribeOn(Schedulers.io()).subscribe({
                print("success")
            },{
                Log.e("meow","上传失败",it)
            })


        }

    }
}