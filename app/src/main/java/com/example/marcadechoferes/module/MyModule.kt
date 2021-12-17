package com.example.marcadechoferes.module

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.localDataBase.LocalDataBase
import com.example.marcadechoferes.localDataBase.LocalDataBaseDao
import com.example.marcadechoferes.network.retrofitInterfaces.RetrofitInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*
import com.example.marcadechoferes.Extra.NetworkConnectionInterceptor
import com.example.marcadechoferes.myApplication.MyApplication

import okhttp3.*


@Module
@InstallIn(SingletonComponent::class)
class MyModule {

     var context:Context? = null

    val baseUrl = "https://logicasur.com:27123/app/v1/"
    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
      context= application.applicationContext

        return context!!
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {

        return HttpLoggingInterceptor()
    }

    @Provides
    @Singleton
    fun provideNetWorkCheckInterceptor():NetworkConnectionInterceptor {

        return NetworkConnectionInterceptor(MyApplication.appContext)
    }

    @Provides
    @Singleton
    fun provideTinyDB(@ApplicationContext appContext: Context): TinyDB {
        return TinyDB(appContext)
    }


    @Provides
    @Singleton
     fun provideUnsafeOkHttpClient(   okHttpLoggingInterceptor: HttpLoggingInterceptor,networkConnectionInterceptor: NetworkConnectionInterceptor): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            //val sslContext = SSLContext.getInstance("SSL")
            // sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager

            val trustManager = trustAllCerts.get(0) as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)



            val sslSocketFactory = sslContext.socketFactory
            OkHttpClient.Builder()
//                .addInterceptor(okHttpLoggingInterceptor)
                .addInterceptor(networkConnectionInterceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory,trustManager)
                .hostnameVerifier(HostnameVerifier { hostname, session -> true })
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }


    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpLoggingInterceptor: HttpLoggingInterceptor,okHttpClient: OkHttpClient
    ): Retrofit {


        okHttpLoggingInterceptor.apply {
            okHttpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

//        val okkHttpclient = OkHttpClient.Builder()
//            .addInterceptor(okHttpLoggingInterceptor)
//            .connectTimeout(1, TimeUnit.MINUTES)
//            .readTimeout(40, TimeUnit.SECONDS)
//            .writeTimeout(30, TimeUnit.SECONDS)
//            .build()






        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideAuth(retrofit: Retrofit): RetrofitInterface {

        return retrofit.create(RetrofitInterface::class.java)
    }


    @Singleton
    @Provides
    fun provideDataBase(application: Application): LocalDataBase {

        return  Room.databaseBuilder(application,
           LocalDataBase::class.java,
            "localdataBase")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(localDataBase: LocalDataBase): LocalDataBaseDao {
        return localDataBase.localDBDao()
    }





}




