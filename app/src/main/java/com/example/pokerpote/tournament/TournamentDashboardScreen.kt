// main/java/com/example/pokerpote/tournament/TournamentDashboardScreen.kt
package com.example.pokerpote.tournament

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pokerpote.MainActivity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TournamentDashboardScreen(
    navController: NavController,
    viewModel: TournamentViewModel,
    showSnackbar: (String) -> Unit
) {
    val tournament by viewModel.tournamentState.collectAsState()

    // --- State for Dialogs ---
    var showEndTournamentDialog by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showRebuyDialog by remember { mutableStateOf(false) }
    var showAddonDialog by remember { mutableStateOf(false) }
    var showEliminateDialog by remember { mutableStateOf(false) }
    var playerToAction by remember { mutableStateOf<TournamentPlayer?>(null) }


    if (tournament == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum torneio em andamento.")
        }
        return
    }

    val currentTournament = tournament!!
    val playersByTable = remember(currentTournament.players) {
        currentTournament.players
            .filter { it.status == PlayerStatus.ACTIVE }
            .groupBy { it.table }
            .toSortedMap()
    }

    // --- Dialogs ---
    if (showAddPlayerDialog) {
        AddLatePlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onConfirm = { playerName ->
                val success = viewModel.addLateRegistrationPlayer(playerName)
                if (!success) {
                    showSnackbar("Não foi possível adicionar o jogador. O nome pode já existir.")
                }
                showAddPlayerDialog = false
            }
        )
    }

    playerToAction?.let { player ->
        if (showRebuyDialog) {
            ActionConfirmationDialog(
                onDismiss = { showRebuyDialog = false },
                onConfirm = {
                    viewModel.performRebuy(player.id)
                    showRebuyDialog = false
                },
                title = "Confirmar Rebuy",
                text = "Deseja confirmar o rebuy para ${player.name} no valor de ${formatCurrency(currentTournament.rebuyPrice)}?"
            )
        }
        if (showAddonDialog) {
            ActionConfirmationDialog(
                onDismiss = { showAddonDialog = false },
                onConfirm = {
                    viewModel.performAddon(player.id)
                    showAddonDialog = false
                },
                title = "Confirmar Add-on",
                text = "Deseja confirmar o add-on para ${player.name} no valor de ${formatCurrency(currentTournament.addonPrice)}?"
            )
        }
        if (showEliminateDialog) {
            ActionConfirmationDialog(
                onDismiss = { showEliminateDialog = false },
                onConfirm = {
                    viewModel.eliminatePlayer(player.id)
                    showEliminateDialog = false
                },
                title = "Confirmar Eliminação",
                text = "Tem certeza que deseja eliminar ${player.name} do torneio?",
                isDestructive = true
            )
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTournament.name) },
                actions = {
                    IconButton(onClick = { showEndTournamentDialog = true }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Encerrar Torneio")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.pauseOrResume() }) {
                Icon(
                    if (currentTournament.tournamentState == TournamentState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Pausar/Retomar"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TournamentClock(currentTournament)
                    TournamentStats(currentTournament)
                    Spacer(Modifier.height(8.dp))
                    if (currentTournament.rebuyEnabled && currentTournament.currentLevel <= currentTournament.lateRegistrationLevel) {
                        Button(
                            onClick = { showAddPlayerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adicionar Jogador (Registro Tardio)")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            stickyHeader {
                Surface(modifier = Modifier.fillParentMaxWidth(), shadowElevation = 2.dp) {
                    Text(text = "Jogadores Ativos (${currentTournament.playersRemaining})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(playersByTable.entries.toList(), key = { (tableNumber, _) -> tableNumber }) { (tableNumber, playersOnTable) ->
                        TableColumn(
                            tableNumber = tableNumber,
                            players = playersOnTable,
                            tournament = currentTournament,
                            onRebuyClick = { player ->
                                playerToAction = player
                                showRebuyDialog = true
                            },
                            onAddonClick = { player ->
                                playerToAction = player
                                showAddonDialog = true
                            },
                            onEliminateClick = { player ->
                                playerToAction = player
                                showEliminateDialog = true
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            stickyHeader {
                Surface(modifier = Modifier.fillParentMaxWidth(), shadowElevation = 2.dp) {
                    Text(text = "Jogadores Eliminados", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            }
            items(currentTournament.players.filter { it.status == PlayerStatus.ELIMINATED }.sortedByDescending { it.eliminatedPosition }, key = { it.id }) { player ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EliminatedPlayerRow(player)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showEndTournamentDialog) {
        AlertDialog(
            onDismissRequest = { showEndTournamentDialog = false },
            title = { Text("Encerrar Torneio") },
            text = { Text("Você tem certeza que deseja encerrar e apagar este torneio? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndClearTournament()
                        navController.navigate("tournament_setup") {
                            popUpTo("home") { inclusive = false }
                        }
                        showEndTournamentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Encerrar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTournamentDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TableColumn(
    tableNumber: Int,
    players: List<TournamentPlayer>,
    tournament: Tournament,
    onRebuyClick: (TournamentPlayer) -> Unit,
    onAddonClick: (TournamentPlayer) -> Unit,
    onEliminateClick: (TournamentPlayer) -> Unit
) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Mesa $tableNumber",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
            )
            players.sortedByDescending { it.chipCount }.forEach { player ->
                PlayerRow(
                    player = player,
                    tournament = tournament,
                    onRebuyClick = onRebuyClick,
                    onAddonClick = onAddonClick,
                    onEliminateClick = onEliminateClick
                )
            }
        }
    }
}

@Composable
fun PlayerRow(
    player: TournamentPlayer,
    tournament: Tournament,
    onRebuyClick: (TournamentPlayer) -> Unit,
    onAddonClick: (TournamentPlayer) -> Unit,
    onEliminateClick: (TournamentPlayer) -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(player.name, fontWeight = FontWeight.Bold)
                    // Text("Fichas: ${player.chipCount}")
                    if(player.seat > 0) Text("Assento: ${player.seat}")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onEliminateClick(player) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Eliminar")
                }
            }

            val isRebuyPeriod = tournament.rebuyEnabled && tournament.currentLevel <= tournament.lateRegistrationLevel

            val isAddonPeriod = if (tournament.rebuyEnabled) {
                tournament.addonEnabled && tournament.currentLevel > tournament.lateRegistrationLevel && tournament.currentLevel <= tournament.addonLevel
            } else {
                tournament.addonEnabled && tournament.currentLevel <= tournament.addonLevel
            }

            val canRebuy = isRebuyPeriod
            val canAddon = isAddonPeriod && !player.addon

            if (canRebuy || canAddon) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canRebuy) {
                        Button(
                            onClick = { onRebuyClick(player) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rebuy")
                        }
                    }
                    if (canAddon) {
                        Button(
                            onClick = { onAddonClick(player) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add-on")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EliminatedPlayerRow(player: TournamentPlayer) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
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

@Composable
fun formatCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
}


//// main/java/com/example/pokerpote/tournament/TournamentDashboardScreen.kt
//package com.example.pokerpote.tournament
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Stop
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import java.text.NumberFormat
//import java.util.Locale
//
//@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun TournamentDashboardScreen(
//    navController: NavController,
//    viewModel: TournamentViewModel
//) {
//    val tournament by viewModel.tournamentState.collectAsState()
//    val showEndTournamentDialog = remember { mutableStateOf(false) }
//    var showAddPlayerDialog by remember { mutableStateOf(false) }
//
//    if (tournament == null) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text("Nenhum torneio em andamento.")
//        }
//        return
//    }
//
//    val currentTournament = tournament!!
//    val playersByTable = remember(currentTournament.players) {
//        currentTournament.players
//            .filter { it.status == PlayerStatus.ACTIVE }
//            .groupBy { it.table }
//            .toSortedMap()
//    }
//
//    if (showAddPlayerDialog) {
//        AddLatePlayerDialog(
//            onDismiss = { showAddPlayerDialog = false },
//            onConfirm = { playerName ->
//                viewModel.addLateRegistrationPlayer(playerName)
//                showAddPlayerDialog = false
//            }
//        )
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(currentTournament.name) },
//                actions = {
//                    IconButton(onClick = { showEndTournamentDialog.value = true }) {
//                        Icon(Icons.Default.Stop, contentDescription = "Encerrar Torneio")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { viewModel.pauseOrResume() }) {
//                Icon(
//                    if (currentTournament.tournamentState == TournamentState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
//                    contentDescription = "Pausar/Retomar"
//                )
//            }
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//        ) {
//            item {
//                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//                    TournamentClock(currentTournament)
//                    TournamentStats(currentTournament)
//                    Spacer(Modifier.height(8.dp))
//                    // Botão para Adicionar Jogador (Registro Tardio)
//                    if (currentTournament.rebuyEnabled && currentTournament.currentLevel <= currentTournament.lateRegistrationLevel) {
//                        Button(
//                            onClick = { showAddPlayerDialog = true },
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Text("Adicionar Jogador (Registro Tardio)")
//                        }
//                    }
//                    Spacer(Modifier.height(16.dp))
//                }
//            }
//
//            stickyHeader {
//                Surface(modifier = Modifier.fillParentMaxWidth(), shadowElevation = 2.dp) {
//                    Text(
//                        text = "Jogadores Ativos (${currentTournament.playersRemaining})",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
//                    )
//                }
//            }
//
//            item {
//                LazyRow(
//                    contentPadding = PaddingValues(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    items(playersByTable.entries.toList()) { (tableNumber, playersOnTable) ->
//                        TableColumn(
//                            tableNumber = tableNumber,
//                            players = playersOnTable,
//                            viewModel = viewModel,
//                            tournament = currentTournament
//                        )
//                    }
//                }
//            }
//
//            item { Spacer(Modifier.height(16.dp)) }
//
//            stickyHeader {
//                Surface(modifier = Modifier.fillParentMaxWidth(), shadowElevation = 2.dp) {
//                    Text(
//                        text = "Jogadores Eliminados",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
//                    )
//                }
//            }
//            items(currentTournament.players.filter { it.status == PlayerStatus.ELIMINATED }.sortedByDescending { it.eliminatedPosition }) { player ->
//                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
//                    EliminatedPlayerRow(player)
//                }
//            }
//
//            item { Spacer(modifier = Modifier.height(16.dp)) }
//        }
//    }
//
//    if (showEndTournamentDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showEndTournamentDialog.value = false },
//            title = { Text("Encerrar Torneio") },
//            text = { Text("Você tem certeza que deseja encerrar e apagar este torneio? Esta ação não pode ser desfeita.") },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        viewModel.resetAndClearTournament()
//                        navController.navigate("tournament_setup") {
//                            popUpTo("home") { inclusive = false }
//                        }
//                        showEndTournamentDialog.value = false
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                ) { Text("Encerrar") }
//            },
//            dismissButton = {
//                TextButton(onClick = { showEndTournamentDialog.value = false }) { Text("Cancelar") }
//            }
//        )
//    }
//}
//
//@Composable
//fun TableColumn(
//    tableNumber: Int,
//    players: List<TournamentPlayer>,
//    viewModel: TournamentViewModel,
//    tournament: Tournament
//) {
//    Card(
//        modifier = Modifier
//            .width(320.dp)
//            .padding(bottom = 8.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(modifier = Modifier.padding(8.dp)) {
//            Text(
//                text = "Mesa $tableNumber",
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
//            )
//            players.sortedByDescending { it.chipCount }.forEach { player ->
//                PlayerRow(player = player, viewModel = viewModel, tournament = tournament)
//            }
//        }
//    }
//}
//
//@Composable
//fun PlayerRow(player: TournamentPlayer, viewModel: TournamentViewModel, tournament: Tournament) {
//    Card(modifier = Modifier
//        .fillMaxWidth()
//        .padding(vertical = 4.dp)) {
//        Column(modifier = Modifier.padding(12.dp)) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(player.name, fontWeight = FontWeight.Bold)
//                    // A linha abaixo foi comentada conforme solicitado
//                    // Text("Fichas: ${player.chipCount}")
//                    if(player.seat > 0) Text("Assento: ${player.seat}")
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = { viewModel.eliminatePlayer(player.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
//                    Text("Eliminar")
//                }
//            }
//
//            // Lógica para determinar se Rebuy e Add-on estão disponíveis
//            val isRebuyPeriod = tournament.rebuyEnabled && tournament.currentLevel <= tournament.lateRegistrationLevel
//
//            val isAddonPeriod = if (tournament.rebuyEnabled) {
//                // Se rebuy está ativo, addon é SÓ depois do período de rebuy
//                tournament.addonEnabled && tournament.currentLevel > tournament.lateRegistrationLevel && tournament.currentLevel <= tournament.addonLevel
//            } else {
//                // Senão, addon é desde o início
//                tournament.addonEnabled && tournament.currentLevel <= tournament.addonLevel
//            }
//
//            val canRebuy = isRebuyPeriod
//            val canAddon = isAddonPeriod && !player.addon
//
//            if (canRebuy || canAddon) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    if (canRebuy) {
//                        Button(
//                            onClick = { viewModel.performRebuy(player.id) },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Text("Rebuy")
//                        }
//                    }
//                    if (canAddon) {
//                        Button(
//                            onClick = { viewModel.performAddon(player.id) },
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Text("Add-on")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EliminatedPlayerRow(player: TournamentPlayer) {
//    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
//
//    Card(modifier = Modifier
//        .fillMaxWidth()
//        .padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("${player.eliminatedPosition}º - ${player.name}", fontWeight = FontWeight.Bold)
//            if(player.prizeWon > 0) {
//                Text(currencyFormat.format(player.prizeWon), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
