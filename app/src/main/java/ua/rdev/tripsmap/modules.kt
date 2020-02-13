package ua.rdev.tripsmap

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val mainModule = module {
    viewModel { MainViewModel(get()) }
    single { TripsRepository(get()) }
    factory { provideMainNetworkService(get()) }
    factory { provideOkHttpClient() }
    factory { provideGsonConverterFactory() }
    single { provideRetrofit(get(), get()) }
}

fun provideGsonConverterFactory(): GsonConverterFactory {
    var gson = GsonBuilder().setLenient().create()
    return GsonConverterFactory.create(gson)
}

fun provideOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder().addInterceptor(interceptor).build()
}

fun provideMainNetworkService(retrofit: Retrofit) = retrofit.create(MainNetwork::class.java)

fun provideRetrofit(
    okHttpClient: OkHttpClient,
    gsonConverterFactory: GsonConverterFactory
): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://us-central1-globo-a0b72.cloudfunctions.net/")
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()
}