package com.example.pokerpote.tournament

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    navController: NavController,
    viewModel: TournamentViewModel
) {
    val tournament by viewModel.tournamentState.collectAsState()
    val showEndTournamentDialog = remember { mutableStateOf(false) }

    if (tournament == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum torneio em andamento.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(   /// TopAppBar ESTAVA AVISANDO QUE ERA EXPERIMENTAL E QUE PODERIA SER DESCONTINUADO
                title = { Text(tournament!!.name) },
                actions = {
                    IconButton(onClick = { showEndTournamentDialog.value = true }) {
                        Icon(Icons.Default.Stop, contentDescription = "Encerrar Torneio")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.pauseOrResume() }) {
                Icon(
                    if (tournament!!.tournamentState == TournamentState.RUNNING) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Pausar/Retomar"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item { TournamentClock(tournament!!) }
            item { TournamentStats(tournament!!) }
            item { Spacer(Modifier.height(16.dp)) }

            stickyHeader {
                Surface(modifier = Modifier.fillParentMaxWidth()) {
                    Text("Jogadores Ativos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            items(tournament!!.players.filter { it.status == PlayerStatus.ACTIVE }.sortedByDescending { it.chipCount }) { player ->
                PlayerRow(player, viewModel)
            }

            item { Spacer(Modifier.height(16.dp)) }

            stickyHeader {
                Surface(modifier = Modifier.fillParentMaxWidth()) {
                    Text("Jogadores Eliminados", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            items(tournament!!.players.filter { it.status == PlayerStatus.ELIMINATED }.sortedByDescending { it.eliminatedPosition }) { player ->
                EliminatedPlayerRow(player)
            }
        }
    }

    if (showEndTournamentDialog.value) {
        AlertDialog(
            onDismissRequest = { showEndTournamentDialog.value = false },
            title = { Text("Encerrar Torneio") },
            text = { Text("Você tem certeza que deseja encerrar e apagar este torneio? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndClearTournament()
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                        showEndTournamentDialog.value = false
                    }
                ) { Text("Encerrar") }
            },
            dismissButton = {
                Button(onClick = { showEndTournamentDialog.value = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PlayerRow(player: TournamentPlayer, viewModel: TournamentViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontWeight = FontWeight.Bold)
                Text("Fichas: ${player.chipCount}")
                if(player.table > 0) Text("Mesa: ${player.table} | Assento: ${player.seat}")
            }
            Button(onClick = { viewModel.eliminatePlayer(player.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Eliminar")
            }
            // Adicionar botões para Rebuy/Addon
        }
    }
}

@Composable
fun EliminatedPlayerRow(player: TournamentPlayer) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${player.eliminatedPosition}º - ${player.name}", fontWeight = FontWeight.Bold)
            if(player.prizeWon > 0) {
                Text(currencyFormat.format(player.prizeWon), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}