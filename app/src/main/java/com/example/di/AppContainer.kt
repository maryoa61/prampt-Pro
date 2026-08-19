package com.example.di

import android.content.Context
import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.data.local.db.AppDatabase
import com.example.data.remote.AiPromptDataSource
import com.example.data.remote.GeminiApiService
import com.example.data.repository.PromptRepository
import com.example.data.repository.PromptRepositoryImpl
import com.example.domain.usecase.ClearPromptHistoryUseCase
import com.example.domain.usecase.DeletePromptUseCase
import com.example.domain.usecase.ExportPromptHistoryUseCase
import com.example.domain.usecase.GeneratePromptUseCase
import com.example.domain.usecase.GetPromptHistoryUseCase
import com.example.domain.usecase.SavePromptUseCase
import com.example.promptpro.data.repo.PromptTemplateRepository
import com.example.promptpro.domain.usecase.DeleteTemplateUseCase
import com.example.promptpro.domain.usecase.GetTemplateByIdUseCase
import com.example.promptpro.domain.usecase.ObserveTemplatesUseCase
import com.example.promptpro.domain.usecase.SaveTemplateUseCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val preferencesDataStore: UserPreferencesDataStore
    val promptRepository: PromptRepository
    val generatePromptUseCase: GeneratePromptUseCase
    val savePromptUseCase: SavePromptUseCase
    val getPromptHistoryUseCase: GetPromptHistoryUseCase
    val deletePromptUseCase: DeletePromptUseCase
    val clearPromptHistoryUseCase: ClearPromptHistoryUseCase
    val exportPromptHistoryUseCase: ExportPromptHistoryUseCase
    val promptTemplateRepository: PromptTemplateRepository
    val observeTemplatesUseCase: ObserveTemplatesUseCase
    val getTemplateByIdUseCase: GetTemplateByIdUseCase
    val saveTemplateUseCase: SaveTemplateUseCase
    val deleteTemplateUseCase: DeleteTemplateUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val geminiApiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    private val aiPromptDataSource: AiPromptDataSource by lazy {
        AiPromptDataSource(
            apiService = geminiApiService,
            okHttpClient = okHttpClient
        )
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val preferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(context)
    }

    override val promptRepository: PromptRepository by lazy {
        PromptRepositoryImpl(
            remoteDataSource = aiPromptDataSource,
            promptDao = database.promptDao()
        )
    }

    override val generatePromptUseCase: GeneratePromptUseCase by lazy {
        GeneratePromptUseCase(promptRepository)
    }

    override val savePromptUseCase: SavePromptUseCase by lazy {
        SavePromptUseCase(promptRepository)
    }

    override val getPromptHistoryUseCase: GetPromptHistoryUseCase by lazy {
        GetPromptHistoryUseCase(promptRepository)
    }

    override val deletePromptUseCase: DeletePromptUseCase by lazy {
        DeletePromptUseCase(promptRepository)
    }

    override val clearPromptHistoryUseCase: ClearPromptHistoryUseCase by lazy {
        ClearPromptHistoryUseCase(promptRepository)
    }

    override val exportPromptHistoryUseCase: ExportPromptHistoryUseCase by lazy {
        ExportPromptHistoryUseCase(promptRepository)
    }

    override val promptTemplateRepository: PromptTemplateRepository by lazy {
        PromptTemplateRepository(database.promptTemplateDao())
    }

    override val observeTemplatesUseCase: ObserveTemplatesUseCase by lazy {
        ObserveTemplatesUseCase(promptTemplateRepository)
    }

    override val getTemplateByIdUseCase: GetTemplateByIdUseCase by lazy {
        GetTemplateByIdUseCase(promptTemplateRepository)
    }

    override val saveTemplateUseCase: SaveTemplateUseCase by lazy {
        SaveTemplateUseCase(promptTemplateRepository)
    }

    override val deleteTemplateUseCase: DeleteTemplateUseCase by lazy {
        DeleteTemplateUseCase(promptTemplateRepository)
    }
}
