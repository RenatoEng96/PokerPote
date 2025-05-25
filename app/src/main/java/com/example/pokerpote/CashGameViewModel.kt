// main/java/com/example/pokerpote/CashGameViewModel.kt
package com.example.pokerpote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pokerpote.data.GameStateRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para Gerenciar o Estado e o Repositório do modo Cash Game.
 */
class PokerAppViewModel(private val repository: GameStateRepository) : ViewModel() {

    var activePokerGame by mutableStateOf<PokerGame?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadInitialGame()
    }

    private fun loadInitialGame() {
        viewModelScope.launch {
            val loadedState = repository.loadInitialGameState()
            if (loadedState != null && loadedState.isGameActive) {
                activePokerGame = PokerGame(loadedState)
            }
            isLoading = false
        }
    }

    fun startGame(multiplier: Double, initialBuyIn: Double, initialBB: Int, initialStackDepth: Double, dynamicUpdates: Boolean, bbRate: Double) {
        val newGame = PokerGame(
            multiplier = multiplier,
            initialMinBuyIn = initialBuyIn,
            initialBB = initialBB,
            initialStackDepth = initialStackDepth,
            enableDynamicUpdates = dynamicUpdates,
            bbUpdateRate = bbRate
        )
        activePokerGame = newGame
        viewModelScope.launch {
            repository.saveGameState(newGame)
        }
    }

    fun endGame() {
        activePokerGame = null
        viewModelScope.launch {
            repository.clearGameState()
        }
    }

    fun saveCurrentGame() {
        activePokerGame?.let { game ->
            viewModelScope.launch {
                repository.saveGameState(game)
            }
        }
    }
}


/**
 * Factory para criar o PokerAppViewModel com sua dependência (repository).
 */
class PokerAppViewModelFactory(private val repository: GameStateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokerAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for PokerAppViewModel")
    }
}