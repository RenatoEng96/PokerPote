// main/java/com/example/pokerpote/tournament/TournamentComponents.kt
package com.example.pokerpote.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokerpote.formatCurrency
import java.util.concurrent.TimeUnit

@Composable
fun TournamentClock(tournament: Tournament) {
    val level = tournament.blindStructure.find { it.level == tournament.currentLevel }
    val nextLevel = tournament.blindStructure.find { it.level == tournament.currentLevel + 1 }

    val millis = tournament.timeRemainingInMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nível de blind atual
            Text(
                text = "Nível ${tournament.currentLevel} de ${tournament.blindStructure.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nível Atual", style = MaterialTheme.typography.labelMedium)
                    if (level != null) {
                        Text(
                            "${level.smallBlind} / ${level.bigBlind}" + if(level.ante > 0) " (${level.ante})" else "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Próximo Nível", style = MaterialTheme.typography.labelMedium)
                    if (nextLevel != null) {
                        Text(
                            "${nextLevel.smallBlind} / ${nextLevel.bigBlind}" + if(nextLevel.ante > 0) " (${nextLevel.ante})" else "",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Gray
                        )
                    } else {
                        Text("-", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun TournamentStats(tournament: Tournament) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Prêmio Total", formatCurrency(tournament.totalPrizePool))
            StatItem("Jogadores", "${tournament.playersRemaining}/${tournament.players.size}")
            StatItem("Média Fichas", tournament.averageStack.toString())
        }
        // Informações adicionais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            StatItem("Buy-in", "${formatCurrency(tournament.buyIn)}\n(Taxa: ${formatCurrency(tournament.feeContribution)})")
            if (tournament.rebuyEnabled) {
                StatItem("Rebuy", "${formatCurrency(tournament.rebuyPrice)}\n(${tournament.rebuyChips} fichas)")
            }
            if (tournament.addonEnabled) {
                StatItem("Add-on", "${formatCurrency(tournament.addonPrice)}\n(${tournament.addonChips} fichas)")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(IntrinsicSize.Max)) {
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}


//package com.example.pokerpote.tournament
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.util.concurrent.TimeUnit
//
//@Composable
//fun TournamentClock(tournament: Tournament) {
//    val level = tournament.blindStructure.find { it.level == tournament.currentLevel }
//    val nextLevel = tournament.blindStructure.find { it.level == tournament.currentLevel + 1 }
//
//    val millis = tournament.timeRemainingInMillis
//    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
//    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.surfaceVariant)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = String.format("%02d:%02d", minutes, seconds),
//                fontSize = 64.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.primary
//            )
//            Spacer(Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceAround
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("Nível Atual", style = MaterialTheme.typography.labelMedium)
//                    if (level != null) {
//                        Text(
//                            "${level.smallBlind} / ${level.bigBlind}" + if(level.ante > 0) " (${level.ante})" else "",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text("Próximo Nível", style = MaterialTheme.typography.labelMedium)
//                    if (nextLevel != null) {
//                        Text(
//                            "${nextLevel.smallBlind} / ${nextLevel.bigBlind}" + if(nextLevel.ante > 0) " (${nextLevel.ante})" else "",
//                            style = MaterialTheme.typography.titleLarge,
//                            color = Color.Gray
//                        )
//                    } else {
//                        Text("-", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TournamentStats(tournament: Tournament) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        StatItem("Prêmio Total", "R$ ${"%.2f".format(tournament.totalPrizePool)}")
//        StatItem("Jogadores", "${tournament.playersRemaining}/${tournament.players.size}")
//        StatItem("Média Fichas", tournament.averageStack.toString())
//    }
//}
//
//@Composable
//private fun StatItem(label: String, value: String) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(label, style = MaterialTheme.typography.labelMedium)
//        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
//    }
//}