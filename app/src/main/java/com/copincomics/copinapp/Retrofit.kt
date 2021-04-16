package com.copincomics.copinapp

import android.util.Log
import com.copincomics.copinapp.api.ApiService
import com.copincomics.copinapp.api.PaymentService
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class Retrofit : BaseActivity() {

    companion object {
        const val TAG = "TAG : ServiceRepo"
    }

    private class AuthInterceptor(
        val t: String,
        val c: String,
        val d: String,
        val v: String
    ) :
        Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val url: HttpUrl = chain.request().url()
                .newBuilder()
                .addQueryParameter("t", t)
                .addQueryParameter("c", c)
                .addQueryParameter("d", d)
                .addQueryParameter("v", v)
                .build()
            val request: Request = chain.request().newBuilder().url(url).build()
            Log.d(TAG, "intercept: request = $request")
            return chain.proceed(request)
        }
    }

    private fun okHttpclient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(30, TimeUnit.SECONDS)
        val t = App.config.accessToken
        val c = App.config.deviceID
        val d = "android"
        val v = App.currentVersion.toString()
        httpClient.addInterceptor(AuthInterceptor(t, c, d, v))
        Log.d(TAG, "okHttpclient: t=$t, c=$c, d=$d, v=$v")
        return httpClient.build()
    }

    fun buildApiService(): ApiService {
        return Retrofit.Builder()
                .baseUrl(App.config.apiURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpclient())
                .build().create(ApiService::class.java)
    }

    fun buildPaymentService(): PaymentService {
        return Retrofit.Builder()
                .baseUrl(App.config.apiURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpclient())
                .build().create(PaymentService::class.java)

    }
}