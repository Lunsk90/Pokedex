package com.example.pokedex

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    @GET("pokemon/{name}")
    suspend fun getPokemonByName(@Path("name") name: String): Pokemon

    @GET("pokemon")
    suspend fun getAllPokemon(@Query("offset") offset: Int, @Query("limit") limit: Int): PokemonResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonById(@Path("id") id: Int): Pokemon

    @GET("pokemon-species/{name}")
    suspend fun getPokemonDescription(@Path("name") name: String): PokemonDescription

    data class PokemonResponse(val results: List<Pokemon>)

    data class Pokemon(
        val id: Int,
        val name: String,
        val types: List<Type>,
        val sprites: Sprites,
    ) {
        var imageUrl: String = ""
            get() = sprites.frontDefault
    }

    data class Type(
        val slot: String,
        val type: TypeInfo
    )

    data class TypeInfo(
        val name: String,
        val url: String
    )

    data class Sprites(
        @SerializedName("front_default")
        val frontDefault: String
    )

    data class PokemonDescription(
        @SerializedName("flavor_text")
        val flavorText: String,
        val language: Language,
        val version: Version
    )

    data class Language(
        val name: String,
        val url: String
    )

    data class Version(
        val name: String,
        val url: String
    )

    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2/"

        fun create(): PokeApiService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(PokeApiService::class.java)
        }
    }
}
