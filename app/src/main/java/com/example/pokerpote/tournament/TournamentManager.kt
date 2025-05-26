// main/java/com/example/pokerpote/tournament/TournamentManager.kt
package com.example.pokerpote.tournament

import kotlin.math.ceil

object TournamentManager {

    private const val SEATS_PER_TABLE = 9

    fun seatPlayers(players: List<TournamentPlayer>): List<TournamentPlayer> {
        if (players.isEmpty()) return emptyList()

        val shuffledPlayers = players.shuffled()
        val numPlayers = shuffledPlayers.size
        val numTables = ceil(numPlayers.toFloat() / SEATS_PER_TABLE).toInt()

        return shuffledPlayers.mapIndexed { index, player ->
            val table = (index % numTables) + 1
            val seat = (index / numTables) + 1
            player.copy(table = table, seat = seat)
        }
    }

    fun balanceTables(players: List<TournamentPlayer>): List<TournamentPlayer> {
        val activePlayers = players.filter { it.status == PlayerStatus.ACTIVE }
        val eliminatedPlayers = players.filter { it.status == PlayerStatus.ELIMINATED }

        // Se todos os jogadores ativos cabem em uma única mesa, forma-se a mesa final.
        if (activePlayers.size <= SEATS_PER_TABLE) {
            return players.map {
                if (it.status == PlayerStatus.ACTIVE) {
                    it.copy(table = 1, seat = 0) // Reseta a mesa para a final
                } else {
                    it
                }
            }.let { allPlayers ->
                val finalPlayers = allPlayers.filter { it.status == PlayerStatus.ACTIVE }.shuffled().mapIndexed { index, player ->
                    player.copy(seat = index + 1) // Re-assenta os jogadores na mesa final
                }
                eliminatedPlayers + finalPlayers
            }
        }

        val tables = activePlayers.groupBy { it.table }
        val tableCounts = tables.mapValues { it.value.size }

        // --- NOVA REGRA 1: CONSOLIDAÇÃO DE MESAS ---
        // Verifica se há mais de uma mesa e se a menor mesa pode ser desfeita.
        if (tableCounts.size > 1) {
            val minPlayersOnTable = tableCounts.values.minOrNull() ?: 0
            val smallestTableNumber = tableCounts.minByOrNull { it.value }?.key

            if (smallestTableNumber != null && minPlayersOnTable > 0) {
                // Calcula o total de assentos vazios em todas as outras mesas
                val totalEmptySeatsOnOtherTables = tableCounts
                    .filterKeys { it != smallestTableNumber }
                    .values
                    .sumOf { SEATS_PER_TABLE - it }

                // Se os jogadores da menor mesa cabem nos assentos vagos, desfaz a mesa.
                if (minPlayersOnTable <= totalEmptySeatsOnOtherTables) {
                    // Re-assenta todos os jogadores ativos nas mesas restantes.
                    // A função seatPlayers recalcula o número de mesas necessárias e redistribui todos.
                    val reseatedActivePlayers = seatPlayers(activePlayers)
                    return reseatedActivePlayers + eliminatedPlayers
                }
            }
        }

        // --- LÓGICA DE BALANCEAMENTO NORMAL (se nenhuma mesa foi consolidada) ---
        val maxPlayersOnTable = tableCounts.values.maxOrNull() ?: 0
        val minPlayersOnTable = tableCounts.values.minOrNull() ?: 0

        // Condição para balancear (permite uma diferença de até 2 jogadores)
        if (maxPlayersOnTable > minPlayersOnTable + 2) {
            val largestTableNumber = tableCounts.filterValues { it == maxPlayersOnTable }.keys.first()
            val smallestTableNumber = tableCounts.filterValues { it == minPlayersOnTable }.keys.first()

            // --- NOVA REGRA 2: TRANSFERÊNCIA ALEATÓRIA ---
            // Em vez de pegar o primeiro jogador, seleciona um aleatoriamente da mesa mais cheia.
            val playerToMove = tables[largestTableNumber]?.random()

            if (playerToMove != null) {
                val updatedPlayers = players.toMutableList()
                val playerIndex = updatedPlayers.indexOfFirst { it.id == playerToMove.id }
                if (playerIndex != -1) {
                    // 1. Encontra os assentos já ocupados na mesa de destino.
                    val playersAtDestinationTable = activePlayers.filter { it.table == smallestTableNumber }
                    val occupiedSeats = playersAtDestinationTable.map { it.seat }.toSet()
                    // 2. Acha o primeiro assento vago (de 1 a 9) que não está na lista de ocupados.
                    val newSeat = (1..SEATS_PER_TABLE).first { it !in occupiedSeats }
                    // 3. Move o jogador e atribui o novo assento encontrado.
                    updatedPlayers[playerIndex] = playerToMove.copy(table = smallestTableNumber, seat = newSeat)
                    // Idealmente, a lógica de assento seria mais complexa (ex: mover o próximo big blind)
                }
                return updatedPlayers
            }
        }

        return players
    }

    fun calculatePrizes(players: List<TournamentPlayer>, totalPrizePool: Double, prizeStructure: List<PrizeTier>): List<TournamentPlayer> {
        val updatedPlayers = players.toMutableList()
        prizeStructure.forEach { tier ->
            val playerInPosition = updatedPlayers.find { it.eliminatedPosition == tier.position }
            if (playerInPosition != null) {
                val playerIndex = updatedPlayers.indexOf(playerInPosition)
                val prize = totalPrizePool * tier.percentage
                updatedPlayers[playerIndex] = playerInPosition.copy(prizeWon = prize)
            }
        }
        return updatedPlayers
    }
}