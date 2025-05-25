// main/java/com/example/pokerpote/tournament/TournamentViewModel.kt
package com.example.pokerpote.tournament

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerpote.data.TournamentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TournamentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TournamentRepository(application)
    private val _tournamentState = MutableStateFlow<Tournament?>(null)
    val tournamentState = _tournamentState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val savedTournament = repository.tournamentFlow.first()
            if (savedTournament != null) {
                val timeSinceLastSave = System.currentTimeMillis() - savedTournament.lastUpdate
                val remainingTime = (savedTournament.timeRemainingInMillis - timeSinceLastSave).coerceAtLeast(0)

                val updatedTournament = savedTournament.copy(timeRemainingInMillis = remainingTime)
                _tournamentState.value = updatedTournament

                if (updatedTournament.tournamentState == TournamentState.RUNNING) {
                    startTimer()
                }
            }
        }
    }

    fun createAndStartTournament(tournament: Tournament) {
        val initialPlayers = tournament.players.map { it.copy(chipCount = tournament.startingStack) }
        val seatedPlayers = TournamentManager.seatPlayers(initialPlayers)

        val firstLevel = tournament.blindStructure.firstOrNull()
        val durationMillis = (firstLevel?.durationMinutes ?: 15) * 60 * 1000L

        val newTournament = tournament.copy(
            players = seatedPlayers,
            currentLevel = 1,
            timeRemainingInMillis = durationMillis,
            tournamentState = TournamentState.RUNNING,
            lastUpdate = System.currentTimeMillis()
        )
        _tournamentState.value = newTournament
        startTimer()
        saveState()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentTournament = _tournamentState.value ?: break
                if (currentTournament.tournamentState != TournamentState.RUNNING) break

                val newTime = (currentTournament.timeRemainingInMillis - 1000).coerceAtLeast(0)
                _tournamentState.value = currentTournament.copy(timeRemainingInMillis = newTime)

                if (newTime <= 0) {
                    increaseBlindLevel()
                }
            }
        }
    }

    fun pauseOrResume() {
        val current = _tournamentState.value ?: return
        val newState = if (current.tournamentState == TournamentState.RUNNING) {
            timerJob?.cancel()
            TournamentState.PAUSED
        } else {
            startTimer()
            TournamentState.RUNNING
        }
        _tournamentState.value = current.copy(tournamentState = newState)
        saveState()
    }

    private fun increaseBlindLevel() {
        val current = _tournamentState.value ?: return
        val nextLevelIndex = current.blindStructure.indexOfFirst { it.level == current.currentLevel } + 1

        if (nextLevelIndex < current.blindStructure.size) {
            val nextLevel = current.blindStructure[nextLevelIndex]
            _tournamentState.value = current.copy(
                currentLevel = nextLevel.level,
                timeRemainingInMillis = nextLevel.durationMinutes * 60 * 1000L
            )
        } else {
            _tournamentState.value = current.copy(tournamentState = TournamentState.FINISHED)
            timerJob?.cancel()
        }
        saveState()
    }

    fun eliminatePlayer(playerId: String) {
        val current = _tournamentState.value ?: return
        val players = current.players.toMutableList()
        val playerIndex = players.indexOfFirst { it.id == playerId }

        if (playerIndex != -1) {
            val player = players[playerIndex]
            val remainingPlayers = players.count { it.status == PlayerStatus.ACTIVE }

            players[playerIndex] = player.copy(
                status = PlayerStatus.ELIMINATED,
                eliminatedPosition = remainingPlayers,
            )

            var updatedPlayers = TournamentManager.balanceTables(players)
            updatedPlayers = TournamentManager.calculatePrizes(updatedPlayers, current.totalPrizePool, current.prizeStructure)

            _tournamentState.value = current.copy(players = updatedPlayers)
            saveState()
        }
    }

    fun performRebuy(playerId: String) {
        val current = _tournamentState.value ?: return
        if (current.currentLevel > current.lateRegistrationLevel) return

        val players = current.players.toMutableList()
        val playerIndex = players.indexOfFirst { it.id == playerId }

        if (playerIndex != -1 && current.rebuyEnabled) {
            val player = players[playerIndex]
            players[playerIndex] = player.copy(
                chipCount = player.chipCount + current.rebuyChips,
                rebuys = player.rebuys + 1
            )
            _tournamentState.value = current.copy(players = players)
            saveState()
        }
    }

    fun performAddon(playerId: String) {
        val current = _tournamentState.value ?: return
        val players = current.players.toMutableList()
        val playerIndex = players.indexOfFirst { it.id == playerId }

        if (playerIndex != -1 && current.addonEnabled && !players[playerIndex].addon) {
            val player = players[playerIndex]
            players[playerIndex] = player.copy(
                chipCount = player.chipCount + current.addonChips,
                addon = true
            )
            _tournamentState.value = current.copy(players = players)
            saveState()
        }
    }

    fun addLateRegistrationPlayer(name: String): Boolean {
        val current = _tournamentState.value ?: return false
        if (current.currentLevel > current.lateRegistrationLevel || !current.rebuyEnabled) return false

        val trimmedName = name.trim()
        if (trimmedName.isBlank() || current.players.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            return false // Retorna falso se o nome for inválido ou duplicado
        }

        val newPlayer = TournamentPlayer(name = trimmedName, chipCount = current.startingStack)
        val updatedPlayers = current.players + newPlayer

        _tournamentState.value = current.copy(
            players = TournamentManager.seatPlayers(updatedPlayers)
        )
        saveState()
        return true // Retorna verdadeiro em caso de sucesso
    }

    fun resetAndClearTournament() {
        viewModelScope.launch {
            repository.clearTournament()
            _tournamentState.value = null
            timerJob?.cancel()
        }
    }

    private fun saveState() {
        viewModelScope.launch {
            _tournamentState.value?.let {
                repository.saveTournament(it.copy(lastUpdate = System.currentTimeMillis()))
            }
        }
    }
}

//// main/java/com/example/pokerpote/tournament/TournamentViewModel.kt
//package com.example.pokerpote.tournament
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.pokerpote.data.TournamentRepository
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//class TournamentViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val repository = TournamentRepository(application)
//    private val _tournamentState = MutableStateFlow<Tournament?>(null)
//    val tournamentState = _tournamentState.asStateFlow()
//
//    private var timerJob: Job? = null
//
//    init {
//        viewModelScope.launch {
//            val savedTournament = repository.tournamentFlow.first()
//            if (savedTournament != null) {
//                val timeSinceLastSave = System.currentTimeMillis() - savedTournament.lastUpdate
//                val remainingTime = (savedTournament.timeRemainingInMillis - timeSinceLastSave).coerceAtLeast(0)
//
//                val updatedTournament = savedTournament.copy(timeRemainingInMillis = remainingTime)
//                _tournamentState.value = updatedTournament
//
//                if (updatedTournament.tournamentState == TournamentState.RUNNING) {
//                    startTimer()
//                }
//            }
//        }
//    }
//
//    fun createAndStartTournament(tournament: Tournament) {
//        val initialPlayers = tournament.players.map { it.copy(chipCount = tournament.startingStack) }
//        val seatedPlayers = TournamentManager.seatPlayers(initialPlayers)
//
//        val firstLevel = tournament.blindStructure.firstOrNull()
//        val durationMillis = (firstLevel?.durationMinutes ?: 15) * 60 * 1000L
//
//        val newTournament = tournament.copy(
//            players = seatedPlayers,
//            currentLevel = 1,
//            timeRemainingInMillis = durationMillis,
//            tournamentState = TournamentState.RUNNING,
//            lastUpdate = System.currentTimeMillis()
//        )
//        _tournamentState.value = newTournament
//        startTimer()
//        saveState()
//    }
//
//    private fun startTimer() {
//        timerJob?.cancel()
//        timerJob = viewModelScope.launch {
//            while (true) {
//                delay(1000)
//                val currentTournament = _tournamentState.value ?: break
//                if (currentTournament.tournamentState != TournamentState.RUNNING) break
//
//                val newTime = (currentTournament.timeRemainingInMillis - 1000).coerceAtLeast(0)
//                _tournamentState.value = currentTournament.copy(timeRemainingInMillis = newTime)
//
//                if (newTime <= 0) {
//                    increaseBlindLevel()
//                }
//            }
//        }
//    }
//
//    fun pauseOrResume() {
//        val current = _tournamentState.value ?: return
//        val newState = if (current.tournamentState == TournamentState.RUNNING) {
//            timerJob?.cancel()
//            TournamentState.PAUSED
//        } else {
//            startTimer()
//            TournamentState.RUNNING
//        }
//        _tournamentState.value = current.copy(tournamentState = newState)
//        saveState()
//    }
//
//    private fun increaseBlindLevel() {
//        val current = _tournamentState.value ?: return
//        val nextLevelIndex = current.blindStructure.indexOfFirst { it.level == current.currentLevel } + 1
//
//        if (nextLevelIndex < current.blindStructure.size) {
//            val nextLevel = current.blindStructure[nextLevelIndex]
//            _tournamentState.value = current.copy(
//                currentLevel = nextLevel.level,
//                timeRemainingInMillis = nextLevel.durationMinutes * 60 * 1000L
//            )
//        } else {
//            _tournamentState.value = current.copy(tournamentState = TournamentState.FINISHED)
//            timerJob?.cancel()
//        }
//        saveState()
//    }
//
//    fun eliminatePlayer(playerId: String) {
//        val current = _tournamentState.value ?: return
//        val players = current.players.toMutableList()
//        val playerIndex = players.indexOfFirst { it.id == playerId }
//
//        if (playerIndex != -1) {
//            val player = players[playerIndex]
//            val remainingPlayers = players.count { it.status == PlayerStatus.ACTIVE }
//
//            players[playerIndex] = player.copy(
//                status = PlayerStatus.ELIMINATED,
//                eliminatedPosition = remainingPlayers,
//                // A linha abaixo foi removida para manter as fichas no sistema para o cálculo da média
//                // chipCount = 0
//            )
//
//            var updatedPlayers = TournamentManager.balanceTables(players)
//            updatedPlayers = TournamentManager.calculatePrizes(updatedPlayers, current.totalPrizePool, current.prizeStructure)
//
//            _tournamentState.value = current.copy(players = updatedPlayers)
//            saveState()
//        }
//    }
//
//    fun performRebuy(playerId: String) {
//        val current = _tournamentState.value ?: return
//        // Adiciona verificação de nível
//        if (current.currentLevel > current.lateRegistrationLevel) return
//
//        val players = current.players.toMutableList()
//        val playerIndex = players.indexOfFirst { it.id == playerId }
//
//        if (playerIndex != -1 && current.rebuyEnabled) {
//            val player = players[playerIndex]
//            players[playerIndex] = player.copy(
//                chipCount = player.chipCount + current.rebuyChips,
//                rebuys = player.rebuys + 1
//            )
//            _tournamentState.value = current.copy(players = players)
//            saveState()
//        }
//    }
//
//    fun performAddon(playerId: String) {
//        val current = _tournamentState.value ?: return
//        val players = current.players.toMutableList()
//        val playerIndex = players.indexOfFirst { it.id == playerId }
//
//        if (playerIndex != -1 && current.addonEnabled && !players[playerIndex].addon) {
//            val player = players[playerIndex]
//            players[playerIndex] = player.copy(
//                chipCount = player.chipCount + current.addonChips,
//                addon = true
//            )
//            _tournamentState.value = current.copy(players = players)
//            saveState()
//        }
//    }
//
//    fun addLateRegistrationPlayer(name: String) {
//        val current = _tournamentState.value ?: return
//        if (current.currentLevel > current.lateRegistrationLevel || !current.rebuyEnabled) return
//
//        val trimmedName = name.trim()
//        if (trimmedName.isBlank() || current.players.any { it.name.equals(trimmedName, ignoreCase = true) }) {
//            // Idealmente, retornaria um erro para a UI
//            return
//        }
//
//        val newPlayer = TournamentPlayer(name = trimmedName, chipCount = current.startingStack)
//        val updatedPlayers = current.players + newPlayer
//
//        _tournamentState.value = current.copy(
//            players = TournamentManager.seatPlayers(updatedPlayers)
//        )
//        saveState()
//    }
//
//    fun resetAndClearTournament() {
//        viewModelScope.launch {
//            repository.clearTournament()
//            _tournamentState.value = null
//            timerJob?.cancel()
//        }
//    }
//
//    private fun saveState() {
//        viewModelScope.launch {
//            _tournamentState.value?.let {
//                repository.saveTournament(it.copy(lastUpdate = System.currentTimeMillis()))
//            }
//        }
//    }
//}
