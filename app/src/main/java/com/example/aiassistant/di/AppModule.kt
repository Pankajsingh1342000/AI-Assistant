package com.example.aiassistant.di

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.aiassistant.data.manager.CameraManager
import com.example.aiassistant.data.manager.ImageProcessor
import com.example.aiassistant.data.manager.TTSManager
import com.example.aiassistant.data.manager.VoiceInputManager
import com.example.aiassistant.data.remote.GeminiApiService
import com.example.aiassistant.data.repository.GeminiRepositoryImpl
import com.example.aiassistant.domain.repository.GeminiRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttp)
            .build()

    @Provides
    @Singleton
    fun provideGeminiApi(retrofit: Retrofit): GeminiApiService =
        retrofit.create(GeminiApiService::class.java)

    @Provides
    @Singleton
    fun provideVoiceInputManager(context: Context): VoiceInputManager =
        VoiceInputManager(context)

    @Provides
    @Singleton
    fun provideCameraManager(context: Context): CameraManager =
        CameraManager(context)

    @Provides
    @Singleton
    fun provideImageProcessor(): ImageProcessor = ImageProcessor()

    @Provides
    @Singleton
    fun provideGeminiRepository(
        api: GeminiApiService
    ): GeminiRepository = GeminiRepositoryImpl(api)
}
