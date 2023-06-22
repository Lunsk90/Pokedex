package com.example.pokedex

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonAdapter : ListAdapter<PokeApiService.Pokemon, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    private var itemClickListener: OnItemClickListener? = null

    fun setItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_pokemon_adapter, parent, false)
        return PokemonViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)
        val apiService = PokeApiService.create()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fullPokemon = apiService.getPokemonByName(pokemon.name)
                withContext(Dispatchers.Main) {
                    holder.bind(fullPokemon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClicked(pokemon)
        }

        holder.itemView.setOnLongClickListener {
            removeItem(position)
            true
        }
    }

    private fun removeItem(position: Int) {
        val removedPokemon = getItem(position)
        showConfirmationDialog(removedPokemon)
        val newList = currentList.toMutableList()
        newList.removeAt(position)
        submitList(newList)
    }

    private fun showConfirmationDialog(pokemon: PokeApiService.Pokemon) {
        val message = "¿Estás seguro de eliminar a ${pokemon.name} de tu equipo?"
    }

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val regionTextView: TextView = itemView.findViewById(R.id.regionTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.pokemonImageView)

        @SuppressLint("DefaultLocale")
        fun bind(pokemon: PokeApiService.Pokemon) {
            val number = "No. ${pokemon.id}"
            val types = pokemon.types?.joinToString(", ") { it.type.name.capitalize() } ?: ""
            val region = "Region: ${getRegionName(pokemon.id)}"

            numberTextView.text = number
            nameTextView.text = pokemon.name
            typeTextView.text = types
            regionTextView.text = region

            if (pokemon.sprites != null) {
                Glide.with(itemView)
                    .load(pokemon.imageUrl)
                    .centerCrop()
                    .into(imageView)
            }
            else {
                imageView.setImageResource(R.drawable.logo) // Establecer una imagen de marcador de posición
                Toast.makeText(itemView.context, "Imagen no disponible", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getRegionName(pokemonId: Int): String {
            return when (pokemonId) {
                in 1..151 -> "Kanto"
                in 152..251 -> "Johto"
                in 252..386 -> "Hoenn"
                in 387..493 -> "Sinnoh"
                in 494..649 -> "Unova"
                in 650..721 -> "Kalos"
                in 722..809 -> "Alola"
                in 810..898 -> "Galar"
                else -> "Unknown"
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(pokemon: PokeApiService.Pokemon)
    }

    private class PokemonDiffCallback : DiffUtil.ItemCallback<PokeApiService.Pokemon>() {
        override fun areItemsTheSame(oldItem: PokeApiService.Pokemon, newItem: PokeApiService.Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PokeApiService.Pokemon, newItem: PokeApiService.Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}
