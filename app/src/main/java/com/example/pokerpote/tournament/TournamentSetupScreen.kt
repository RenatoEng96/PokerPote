// main/java/com/example/pokerpote/tournament/TournamentSetupScreen.kt
package com.example.pokerpote.tournament

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.roundToInt

@Composable
fun TournamentSetupScreen(
    navController: NavController,
    viewModel: TournamentViewModel,
    showSnackbar: (String) -> Unit // Para exibir mensagens de erro
) {
    var tournamentName by remember { mutableStateOf("Torneio Semanal") }
    var buyIn by remember { mutableStateOf("120") }
    var prizePoolContribution by remember { mutableStateOf("100") }
    var startingStack by remember { mutableStateOf("200") }

    var rebuyEnabled by remember { mutableStateOf(true) }
    var rebuyPrice by remember { mutableStateOf("120") }
    var rebuyChips by remember { mutableStateOf("200") }
    var lateRegistrationLevel by remember { mutableStateOf("2") } // Novo

    var addonEnabled by remember { mutableStateOf(true) }
    var addonPrice by remember { mutableStateOf("170") }
    var addonChips by remember { mutableStateOf("300") }
    var addonLevel by remember { mutableStateOf("4") } // Novo

    val blindStructure = remember { mutableStateListOf<BlindLevel>() }
    val prizeStructure = remember { mutableStateListOf<PrizeTier>() }
    val players = remember { mutableStateListOf<TournamentPlayer>() }

    var showBlindDialog by remember { mutableStateOf(false) }
    var showPrizeDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    var newPlayerName by remember { mutableStateOf("") }

    val fee by remember {
        derivedStateOf {
            (buyIn.toDoubleOrNull() ?: 0.0) - (prizePoolContribution.toDoubleOrNull() ?: 0.0)
        }
    }
    val prizeSum by remember {
        derivedStateOf { prizeStructure.sumOf { it.percentage.toDouble() } * 100 }
    }
    val isStartEnabled by remember {
        derivedStateOf {
            tournamentName.isNotBlank() &&
                    (buyIn.toDoubleOrNull() ?: 0.0) > 0 &&
                    (startingStack.toIntOrNull() ?: 0) > 0 &&
                    blindStructure.isNotEmpty() &&
                    prizeStructure.isNotEmpty() &&
                    players.size >= 2 &&
                    prizeSum.roundToInt() == 100
        }
    }

    if (showBlindDialog) {
        AddBlindLevelDialog(
            onDismiss = { showBlindDialog = false },
            onConfirm = { level, sb, bb, ante, duration ->
                val newLevel = BlindLevel(
                    level = level,
                    smallBlind = sb.toIntOrNull() ?: 0,
                    bigBlind = bb.toIntOrNull() ?: 0,
                    ante = ante.toIntOrNull() ?: 0,
                    durationMinutes = duration.toIntOrNull() ?: 15
                )
                blindStructure.add(newLevel)
                showBlindDialog = false
            },
            nextLevel = blindStructure.size + 1
        )
    }

    if (showPrizeDialog) {
        AddPrizeTierDialog(
            onDismiss = { showPrizeDialog = false },
            onConfirm = { position, percentage ->
                val newTier = PrizeTier(
                    position = position.toIntOrNull() ?: 0,
                    percentage = (percentage.toFloatOrNull() ?: 0f) / 100f
                )
                prizeStructure.add(newTier)
                prizeStructure.sortBy { it.position }
                showPrizeDialog = false
            }
        )
    }

    fun handleAddPlayer() {
        val trimmedName = newPlayerName.trim()
        if (trimmedName.isNotBlank()) {
            if (players.any { it.name.equals(trimmedName, ignoreCase = true) }) {
                showSnackbar("Erro: Jogador '$trimmedName' já existe.")
            } else {
                players.add(TournamentPlayer(name = trimmedName, chipCount = 0))
                newPlayerName = ""
                focusManager.clearFocus()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Text("Voltar para Home")
                }
            }

            Text("Configurar Torneio", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(vertical = 16.dp))
            OutlinedTextField(
                value = tournamentName,
                onValueChange = { tournamentName = it },
                label = { Text("Nome do Torneio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        item {
            SectionHeader("Buy-in e Fichas")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = buyIn, onValueChange = { buyIn = it }, label = { Text("Buy-in (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.weight(1f))
                OutlinedTextField(value = prizePoolContribution, onValueChange = { prizePoolContribution = it }, label = { Text("P/ Prêmio (R$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = startingStack, onValueChange = { startingStack = it }, label = { Text("Fichas Iniciais (Stack)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth())
            Text("Taxa (Fee): R$ ${"%.2f".format(fee)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
        }

        item {
            SectionHeader("Rebuy e Add-on")
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar Rebuy", modifier = Modifier.weight(1f))
                    Switch(checked = rebuyEnabled, onCheckedChange = { rebuyEnabled = it })
                }
                if (rebuyEnabled) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = rebuyPrice, onValueChange = { rebuyPrice = it }, label = {Text("Preço Rebuy (R$)")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = rebuyChips, onValueChange = { rebuyChips = it }, label = {Text("Fichas Rebuy")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = lateRegistrationLevel, onValueChange = { lateRegistrationLevel = it }, label = {Text("Nível Limite para Rebuy/Registro")}, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
            Spacer(Modifier.height(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar Add-on", modifier = Modifier.weight(1f))
                    Switch(checked = addonEnabled, onCheckedChange = { addonEnabled = it })
                }
                if (addonEnabled) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = addonPrice, onValueChange = { addonPrice = it }, label = {Text("Preço Add-on (R$)")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = addonChips, onValueChange = { addonChips = it }, label = {Text("Fichas Add-on")}, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = addonLevel, onValueChange = { addonLevel = it }, label = {Text("Nível Limite para Add-on")}, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        }

        item {
            SectionHeader("Estrutura de Blinds")
        }
        items(blindStructure.size) { index ->
            BlindLevelRow(level = blindStructure[index], onRemove = { blindStructure.removeAt(index) })
        }
        item {
            Button(onClick = { showBlindDialog = true }) {
                Text("Adicionar Nível de Blind")
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            SectionHeader("Estrutura de Premiação")
            Text("Soma atual: ${prizeSum.roundToInt()}%", color = if (prizeSum.roundToInt() != 100) MaterialTheme.colorScheme.error else LocalContentColor.current)
        }
        items(prizeStructure.size) { index ->
            PrizeTierRow(tier = prizeStructure[index], onRemove = { prizeStructure.removeAt(index) })
        }
        item {
            Button(onClick = { showPrizeDialog = true }) {
                Text("Adicionar Faixa de Prêmio")
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
            }
        }

        item {
            SectionHeader("Jogadores (${players.size})")
        }
        items(players.size) { index ->
            Text(players[index].name)
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Nome do Jogador") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleAddPlayer() })
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { handleAddPlayer() }) { Text("Add") }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val finalTournament = Tournament(
                        name = tournamentName,
                        buyIn = buyIn.toDoubleOrNull() ?: 0.0,
                        prizePoolContribution = prizePoolContribution.toDoubleOrNull() ?: 0.0,
                        feeContribution = fee,
                        startingStack = startingStack.toIntOrNull() ?: 0,
                        rebuyEnabled = rebuyEnabled,
                        rebuyPrice = rebuyPrice.toDoubleOrNull() ?: 0.0,
                        rebuyChips = rebuyChips.toIntOrNull() ?: 0,
                        lateRegistrationLevel = lateRegistrationLevel.toIntOrNull() ?: 0,
                        addonEnabled = addonEnabled,
                        addonPrice = addonPrice.toDoubleOrNull() ?: 0.0,
                        addonChips = addonChips.toIntOrNull() ?: 0,
                        addonLevel = addonLevel.toIntOrNull() ?: 0,
                        players = players.toList(),
                        blindStructure = blindStructure.toList(),
                        prizeStructure = prizeStructure.toList()
                    )
                    viewModel.createAndStartTournament(finalTournament)
                    navController.navigate("tournament_dashboard") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isStartEnabled
            ) {
                Text("INICIAR TORNEIO")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}