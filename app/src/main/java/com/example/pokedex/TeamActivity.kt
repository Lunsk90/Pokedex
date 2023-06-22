package com.example.pokedex

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TeamActivity : AppCompatActivity() {

    private val teams: MutableList<List<PokeApiService.Pokemon>> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var teamAdapter: PokemonAdapter
    private val pokeApiService = PokeApiService.create()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)

        recyclerView = findViewById(R.id.teamRecyclerView)
        teamAdapter = PokemonAdapter()
        recyclerView.adapter = teamAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        initializeTeams()
    }

    private fun initializeTeams() {
        recyclerView = findViewById(R.id.teamRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PokemonAdapter()
        recyclerView.adapter = adapter

        val sharedPreferences = getSharedPreferences("Team", Context.MODE_PRIVATE)
        val teamString = sharedPreferences.getString("team", "")
        val teamList = teamString?.split(",")?.toMutableList() ?: mutableListOf()

        if (teamList.isNotEmpty()) {
            if (teamList.size >= 3 && teamList.size <= 6) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val teamPokemonList = mutableListOf<PokeApiService.Pokemon>()

                        for (pokemonName in teamList) {
                            val detailedPokemon = pokeApiService.getPokemonByName(pokemonName)
                            detailedPokemon.sprites?.let { sprites ->
                                sprites.frontDefault?.let { imageUrl ->
                                    detailedPokemon.imageUrl = imageUrl
                                    teamPokemonList.add(detailedPokemon)
                                }
                            }
                        }

                        runOnUiThread {
                            adapter.submitList(teamPokemonList)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@TeamActivity, "Error al obtener los Pokémon del equipo: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "El equipo debe tener entre 3 y 6 Pokémon", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "El equipo está vacío", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun getPokemonById(id: Int): PokeApiService.Pokemon {
        return try {
            val apiService = PokeApiService.create()
            val pokemon = apiService.getPokemonByName(id.toString())
            pokemon.sprites?.let { sprites ->
                pokemon.imageUrl = sprites.frontDefault
            }
            pokemon
        } catch (e: Exception) {
            e.printStackTrace()
            PokeApiService.Pokemon(id, "Unknown", emptyList(), PokeApiService.Sprites(""))
        }
    }


}
