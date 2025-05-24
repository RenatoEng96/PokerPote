package com.example.pokerpote.tournament

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TournamentSetupScreen(
    navController: NavController,
    viewModel: TournamentViewModel
) {
    // Para simplificar, vamos usar um objeto Tournament pré-configurado.
    // Em uma app real, esta tela teria TextFields e outras UI para popular este objeto.
    val defaultTournament = Tournament(
        name = "Torneio de Sexta",
        buyIn = 50.0,
        prizePoolContribution = 45.0,
        feeContribution = 5.0,
        startingStack = 20000,
        rebuyEnabled = true,
        rebuyPrice = 50.0,
        rebuyChips = 20000,
        addonEnabled = true,
        addonPrice = 50.0,
        addonChips = 30000,
        players = listOf(
            TournamentPlayer(name = "Jogador 1", chipCount = 0),
            TournamentPlayer(name = "Jogador 2", chipCount = 0),
            TournamentPlayer(name = "Jogador 3", chipCount = 0),
            TournamentPlayer(name = "Jogador 4", chipCount = 0),
            TournamentPlayer(name = "Jogador 5", chipCount = 0),
            TournamentPlayer(name = "Jogador 6", chipCount = 0),
        ),
        blindStructure = listOf(
            BlindLevel(level = 1, smallBlind = 100, bigBlind = 200, durationMinutes = 15),
            BlindLevel(level = 2, smallBlind = 200, bigBlind = 400, durationMinutes = 15),
            BlindLevel(level = 3, smallBlind = 300, bigBlind = 600, ante = 600, durationMinutes = 15),
            BlindLevel(level = 4, smallBlind = 500, bigBlind = 1000, ante = 1000, durationMinutes = 12),
            BlindLevel(level = 5, smallBlind = 1000, bigBlind = 2000, ante = 2000, durationMinutes = 12),
        ),
        prizeStructure = listOf(
            PrizeTier(position = 1, percentage = 0.6f), // 1st lugar - 60%
            PrizeTier(position = 2, percentage = 0.4f), // 2nd lugar - 40%
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Configurar Torneio", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Esta tela é um protótipo.", style = MaterialTheme.typography.bodyMedium)
            Text("Clique abaixo para iniciar um torneio com dados padrão.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    viewModel.createAndStartTournament(defaultTournament)
                    navController.navigate("tournament_dashboard") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("INICIAR TORNEIO PADRÃO")
            }
        }
    }
}