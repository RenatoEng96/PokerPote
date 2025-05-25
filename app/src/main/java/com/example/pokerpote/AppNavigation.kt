// main/java/com/example/pokerpote/AppNavigation.kt
package com.example.pokerpote

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pokerpote.data.GameStateRepository
import com.example.pokerpote.data.gameStateDataStore
import com.example.pokerpote.tournament.TournamentDashboardScreen
import com.example.pokerpote.tournament.TournamentSetupScreen
import com.example.pokerpote.tournament.TournamentViewModel
import kotlinx.coroutines.launch

/**
 * Composable principal que define o Scaffold e o grafo de navegação (NavHost) do app.
 */
@Composable
fun AppNavigation() {
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "home") {

                // Rota: Tela Inicial
                composable("home") {
                    HomeScreen(navController = navController, viewModel = tournamentViewModel)
                }

                // Rota: Configuração do Cash Game
                composable("cash_game_setup") {
                    LaunchedEffect(cashGameViewModel.activePokerGame) {
                        if (cashGameViewModel.activePokerGame != null) {
                            navController.navigate("cash_game_screen") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }
                    if (!cashGameViewModel.isLoading && cashGameViewModel.activePokerGame == null) {
                        SetupScreen(
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            onStartGame = { multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate ->
                                cashGameViewModel.startGame(multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate)
                            }
                        )
                    } else if (cashGameViewModel.isLoading) {
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
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onGameUpdated = { cashGameViewModel.saveCurrentGame() }
                        )
                    } else {
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
                        viewModel = tournamentViewModel,
                        showSnackbar = ::showSnackbar
                    )
                }

                // Rota: Dashboard do Torneio
                composable("tournament_dashboard") {
                    TournamentDashboardScreen(
                        navController = navController,
                        viewModel = tournamentViewModel,
                        showSnackbar = ::showSnackbar
                    )
                }
            }
        }
    }
}