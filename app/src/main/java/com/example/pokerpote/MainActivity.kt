// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/MainActivity.kt
package com.example.pokerpote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pokerpote.data.GameStateRepository
import com.example.pokerpote.data.gameStateDataStore
import com.example.pokerpote.tournament.TournamentDashboardScreen
import com.example.pokerpote.tournament.TournamentSetupScreen
import com.example.pokerpote.tournament.TournamentViewModel
import com.example.pokerpote.ui.theme.PokerPoteTheme
import kotlinx.coroutines.launch

// Activity principal do aplicativo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokerPoteTheme {
                // O Composable AppContainer agora gerencia toda a navegação e estado.
                AppContainer()
            }
        }
    }
}

// --- ViewModel para Gerenciar Estado e Repositório do Cash Game ---
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


// --- ViewModel Factory para Cash Game ---
class PokerAppViewModelFactory(private val repository: GameStateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokerAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for PokerAppViewModel")
    }
}

@Composable
fun AppContainer() {
    // --- NavController e Estados Globais ---
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    // --- ViewModels ---
    val context = LocalContext.current
    // ViewModel para o Cash Game
    val cashGameRepository = remember { GameStateRepository(context.gameStateDataStore) }
    val cashGameViewModel: PokerAppViewModel = viewModel(factory = PokerAppViewModelFactory(cashGameRepository))
    // ViewModel para o Torneio
    val tournamentViewModel: TournamentViewModel = viewModel()

    // --- Layout Estrutural com Scaffold ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            // --- Grafo de Navegação com NavHost ---
            NavHost(navController = navController, startDestination = "home") {

                // Rota: Tela Inicial
                composable("home") {
                    HomeScreen(navController = navController, viewModel = tournamentViewModel)
                }

                // Rota: Configuração do Cash Game
                composable("cash_game_setup") {
                    // Verifica se já existe um jogo de cash game ativo ao entrar nesta rota
                    LaunchedEffect(cashGameViewModel.activePokerGame) {
                        if (cashGameViewModel.activePokerGame != null) {
                            navController.navigate("cash_game_screen") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }
                    if (!cashGameViewModel.isLoading && cashGameViewModel.activePokerGame == null) {
                        SetupScreen(
                            snackbarHostState = snackbarHostState,
                            onStartGame = { multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate ->
                                cashGameViewModel.startGame(multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate)
                            }
                        )
                    } else if(cashGameViewModel.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Rota: Tela Principal do Cash Game
                composable("cash_game_screen") {
                    val activeGame = cashGameViewModel.activePokerGame
                    if (activeGame != null) {
                        PokerGameScreen(
                            pokerGame = activeGame,
                            snackbarHostState = snackbarHostState,
                            showSnackbar = ::showSnackbar,
                            onEndGame = {
                                cashGameViewModel.endGame()
                                navController.navigate("home") { // Volta para a home ao encerrar
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onGameUpdated = { cashGameViewModel.saveCurrentGame() }
                        )
                    } else {
                        // Se o jogo for nulo por algum motivo, volta para a tela de setup
                        LaunchedEffect(Unit) {
                            showSnackbar("O jogo de cash game não está mais ativo.")
                            navController.navigate("cash_game_setup") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }
                }

                // Rota: Configuração do Torneio
                composable("tournament_setup") {
                    TournamentSetupScreen(
                        navController = navController,
                        viewModel = tournamentViewModel
                    )
                }

                // Rota: Dashboard do Torneio
                composable("tournament_dashboard") {
                    TournamentDashboardScreen(
                        navController = navController,
                        viewModel = tournamentViewModel
                    )
                }
            }
        }
    }
}