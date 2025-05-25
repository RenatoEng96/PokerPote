// main/java/com/example/pokerpote/tournament/TournamentDashboardComponents.kt
package com.example.pokerpote.tournament

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction

@Composable
fun AddLatePlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro Tardio") },
        text = {
            Column {
                Text("Digite o nome do novo jogador.")
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Nome do Jogador") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (playerName.isNotBlank()) {
                            onConfirm(playerName)
                        }
                    })
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(playerName) },
                enabled = playerName.isNotBlank()
            ) {
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
fun ActionConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


//// main/java/com/example/pokerpote/tournament/TournamentDashboardComponents.kt
//package com.example.pokerpote.tournament
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.*
//import androidx.compose.ui.text.input.ImeAction
//
//@Composable
//fun AddLatePlayerDialog(
//    onDismiss: () -> Unit,
//    onConfirm: (String) -> Unit
//) {
//    var playerName by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Registro Tardio") },
//        text = {
//            Column {
//                Text("Digite o nome do novo jogador.")
//                OutlinedTextField(
//                    value = playerName,
//                    onValueChange = { playerName = it },
//                    label = { Text("Nome do Jogador") },
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                    keyboardActions = KeyboardActions(onDone = {
//                        if (playerName.isNotBlank()) {
//                            onConfirm(playerName)
//                        }
//                    })
//                )
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = { onConfirm(playerName) },
//                enabled = playerName.isNotBlank()
//            ) {
//                Text("Adicionar")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancelar")
//            }
//        }
//    )
//}