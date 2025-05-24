package com.example.pokerpote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pokerpote.tournament.TournamentState
import com.example.pokerpote.tournament.TournamentViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: TournamentViewModel) {
    val tournament by viewModel.tournamentState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PokerPote", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(64.dp))

        // Se houver um torneio em andamento ou pausado, mostra o botão para continuar
        if (tournament != null && tournament?.tournamentState != TournamentState.FINISHED) {
            Button(
                onClick = { navController.navigate("tournament_dashboard") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("CONTINUAR TORNEIO")
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Button(
                onClick = { navController.navigate("tournament_setup") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("NOVO TORNEIO")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            // Navega para a rota de setup do cash game
            onClick = { navController.navigate("cash_game_setup") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("CASH GAME")
        }
    }
}