// main/java/com/example/pokerpote/tournament/TournamentSetupComponents.kt
package com.example.pokerpote.tournament

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BlindLevelRow(level: BlindLevel, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nível ${level.level}: ${level.smallBlind}/${level.bigBlind}" +
                    (if (level.ante > 0) " (ante ${level.ante})" else "") +
                    " - ${level.durationMinutes} min",
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remover Nível")
        }
    }
}

@Composable
fun PrizeTierRow(tier: PrizeTier, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${tier.position}º Lugar: ${(tier.percentage * 100).toInt()}%",
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remover Premiação")
        }
    }
}


@Composable
fun AddBlindLevelDialog(
    onDismiss: () -> Unit,
    onConfirm: (level: Int, smallBlind: String, bigBlind: String, ante: String, duration: String) -> Unit,
    nextLevel: Int
) {
    var smallBlind by remember { mutableStateOf("") }
    var bigBlind by remember { mutableStateOf("") }
    var ante by remember { mutableStateOf("0") }
    var duration by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Nível de Blind") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nível $nextLevel", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = smallBlind,
                    onValueChange = { smallBlind = it },
                    label = { Text("Small Blind") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = bigBlind,
                    onValueChange = { bigBlind = it },
                    label = { Text("Big Blind") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = ante,
                    onValueChange = { ante = it },
                    label = { Text("Ante (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duração (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(nextLevel, smallBlind, bigBlind, ante, duration)
            }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddPrizeTierDialog(
    onDismiss: () -> Unit,
    onConfirm: (position: String, percentage: String) -> Unit
) {
    var position by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Faixa de Prêmio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Posição (ex: 1 para 1º)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = percentage,
                    onValueChange = { percentage = it },
                    label = { Text("Porcentagem (ex: 50 para 50%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(position, percentage) }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
    }
}