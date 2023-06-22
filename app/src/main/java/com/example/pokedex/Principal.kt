package com.example.pokedex

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class Principal : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pokemonAdapter: PokemonAdapter
    private lateinit var pokeApiService: PokeApiService
    private val pokemonList: MutableList<PokeApiService.Pokemon> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    private var currentPage = 1
    private var isLoading = false

    private var originalPokemonList: List<PokeApiService.Pokemon> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        pokemonAdapter = PokemonAdapter()
        recyclerView.adapter = pokemonAdapter

        // Configurar PokeApiService
        pokeApiService = PokeApiService.create()

        // Obtener y mostrar todos los Pokémon de la primera página
        getAllPokemon(currentPage)

        // Configurar el SearchView
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchPokemon(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Realizar acciones adicionales mientras se escribe en el campo de búsqueda (opcional)
                return false
            }
        })

        searchView.setOnCloseListener {
            resetSearch()
            false
        }

        // Agregar el Listener de Scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Si el usuario ha alcanzado el final de la lista y no hay cargas en progreso
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                    loadNextPage()
                }
            }
        })

        // Establecer el ItemClickListener del adaptador
        pokemonAdapter.setItemClickListener(object : PokemonAdapter.OnItemClickListener {
            override fun onItemClicked(pokemon: PokeApiService.Pokemon) {
                addToTeam(pokemon)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            R.id.menu_item -> {
                val intent = Intent(this, TeamActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun getAllPokemon(page: Int) {
        isLoading = true
        progressBar.visibility = View.VISIBLE
        val offset = (page - 1) * PAGE_SIZE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonResponse = pokeApiService.getAllPokemon(offset, PAGE_SIZE)
                val pokemonList = pokemonResponse.results

                // Obtener los detalles de cada pokémon y asignar las URLs de las imágenes
                for (pokemon in pokemonList) {
                    val detailedPokemon = pokeApiService.getPokemonByName(pokemon.name)
                    pokemon.imageUrl = detailedPokemon.sprites.frontDefault
                }

                runOnUiThread {
                    this@Principal.pokemonList.addAll(pokemonList)
                    originalPokemonList = pokemonList
                    pokemonAdapter.submitList(this@Principal.pokemonList)
                    isLoading = false
                    progressBar.visibility = View.GONE
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Principal, "Error al obtener los Pokémon: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    isLoading = false
                }
            }
        }
    }

    private fun searchPokemon(query: String) {
        val pokemonName = query.lowercase(Locale.ROOT)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val detailedPokemon = pokeApiService.getPokemonByName(pokemonName)
                val detailedPokemonList = mutableListOf<PokeApiService.Pokemon>()

                detailedPokemon.imageUrl = detailedPokemon.sprites.frontDefault
                detailedPokemonList.add(detailedPokemon)

                runOnUiThread {
                    pokemonAdapter.submitList(detailedPokemonList)
                    Toast.makeText(
                        this@Principal,
                        "Nombre del Pokémon: ${detailedPokemon.name}\nID del Pokémon: ${detailedPokemon.id}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Agrega aquí más código para mostrar la información adicional del Pokémon
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Principal, "Error al obtener el Pokémon: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadNextPage() {
        isLoading = true
        progressBar.visibility = View.VISIBLE
        currentPage++
        val offset = currentPage * 20 // Obtén el valor de offset para la siguiente página
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pokemonResponse = pokeApiService.getAllPokemon(offset, 20)
                val pokemonList = pokemonResponse.results

                // Obtener los detalles de cada pokémon y asignar las URLs de las imágenes
                val detailedPokemonList = mutableListOf<PokeApiService.Pokemon>()

                for (pokemon in pokemonList) {
                    val detailedPokemon = pokeApiService.getPokemonByName(pokemon.name)
                    detailedPokemon.imageUrl = detailedPokemon.sprites.frontDefault
                    detailedPokemonList.add(detailedPokemon)
                }

                runOnUiThread {
                    this@Principal.pokemonList.addAll(detailedPokemonList)
                    pokemonAdapter.notifyDataSetChanged()
                    isLoading = false
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Principal, "Error al cargar más Pokémon: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    isLoading = false
                }
            }
        }
    }

    private fun resetSearch() {
        pokemonAdapter.submitList(originalPokemonList)
    }

    private fun addToTeam(pokemon: PokeApiService.Pokemon) {
        // Agrega el código necesario para guardar el Pokémon en el equipo
        // y mostrarlo en la actividad de equipo (TeamActivity)

        // Por ejemplo, puedes guardar el Pokémon en SharedPreferences
        val sharedPreferences = getSharedPreferences("Team", Context.MODE_PRIVATE)
        val teamString = sharedPreferences.getString("team", "")
        val teamList = teamString?.split(",")?.toMutableList() ?: mutableListOf()

        if (!teamList.contains(pokemon.name)) {
            teamList.add(pokemon.name)
            sharedPreferences.edit().putString("team", teamList.joinToString(",")).apply()
            Toast.makeText(this, "Pokémon agregado al equipo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El Pokémon ya está en el equipo", Toast.LENGTH_SHORT).show()
        }

        // Luego, puedes iniciar la actividad TeamActivity para mostrar el equipo actualizado
        val intent = Intent(this, TeamActivity::class.java)
        startActivity(intent)
    }


    companion object {
        private const val PAGE_SIZE = 20
    }
}
