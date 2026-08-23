package com.appvendor.core.network

import com.appvendor.feature_auth.data.remote.AuthApiService
import com.appvendor.feature_dashboard.data.remote.OrderApiService
import com.appvendor.feature_items.data.remote.ItemApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(userPreferences: com.appvendor.core.datastore.UserPreferences): AuthInterceptor {
        return AuthInterceptor {
            runBlocking {
                userPreferences.userToken.firstOrNull()
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.appvendor.core.utils.Constants.BASE_URL) // Use central Constants.BASE_URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ── API Services ─────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideOrderApiService(retrofit: Retrofit): OrderApiService =
        retrofit.create(OrderApiService::class.java)

    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): com.appvendor.feature_dashboard.data.remote.DashboardApiService =
        retrofit.create(com.appvendor.feature_dashboard.data.remote.DashboardApiService::class.java)

    @Provides
    @Singleton
    fun provideItemApiService(retrofit: Retrofit): ItemApiService =
        retrofit.create(ItemApiService::class.java)

    @Provides
    @Singleton
    fun provideVendorApiService(retrofit: Retrofit): com.appvendor.feature_dashboard.data.remote.VendorApiService =
        retrofit.create(com.appvendor.feature_dashboard.data.remote.VendorApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideCloudinaryApiService(loggingInterceptor: HttpLoggingInterceptor): CloudinaryApiService {
        val cloudinaryClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val cloudinaryRetrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/")
            .client(cloudinaryClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return cloudinaryRetrofit.create(CloudinaryApiService::class.java)
    }
}
