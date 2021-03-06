package com.copincomics.copinapp

import android.content.SharedPreferences
import android.util.Log
import com.copincomics.copinapp.dao.AccountDAO
import com.copincomics.copinapp.dao.PayDAO
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class ServiceRepo(private val pref: SharedPreferences) : BaseActivity() {

    companion object {
        const val TAG = "TAG : ServiceRepo"
        const val DEFAULT_API_URL = "https://sapi.copincomics.com"
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
        var t = pref.getString("t", "")!!

        if (t == "") {
            t = UUID.randomUUID().toString()
            try {
                val p = pref.edit()
                p.putString("t", t)
                p.commit()
            } catch (e: Exception) {

            }
        }
        val c = pref.getString("deviceId", "")!!
        val d = "android"
        val v = curVersion.toString()
        httpClient.addInterceptor(AuthInterceptor(t, c, d, v))
        Log.d(TAG, "okHttpclient: t=$t, c=$c, d=$d, v=$v")
        return httpClient.build()
    }

    fun getAccountDao(): AccountDAO {
        return Retrofit.Builder()
                .baseUrl(pref.getString("a", DEFAULT_API_URL)!!)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpclient())
                .build().create(AccountDAO::class.java)
    }

    fun getPayDao() : PayDAO {
        return Retrofit.Builder()
                .baseUrl(pref.getString("a", DEFAULT_API_URL)!!)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpclient())
                .build().create(PayDAO::class.java)
    }

//    val accountDAO: AccountDAO = Retrofit.Builder()
//        .baseUrl(pref.getString("a", DEFAULT_API_URL)!!)
//        .addConverterFactory(GsonConverterFactory.create())
//        .client(okHttpclient())
//        .build().create(AccountDAO::class.java)
//
//    val payDAO: PayDAO = Retrofit.Builder()
//        .baseUrl(pref.getString("a", DEFAULT_API_URL)!!)
//        .addConverterFactory(GsonConverterFactory.create())
//        .client(okHttpclient())
//        .build().create(PayDAO::class.java)
}