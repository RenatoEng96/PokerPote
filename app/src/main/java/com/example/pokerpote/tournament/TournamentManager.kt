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
        if (activePlayers.size <= SEATS_PER_TABLE) {
            // Se cabe todo mundo em uma mesa, junta todos na mesa 1
            return players.map {
                if (it.status == PlayerStatus.ACTIVE) {
                    it.copy(table = 1, seat = 0) // Seat será reatribuído
                } else {
                    it
                }
            }.let { allPlayers ->
                val finalPlayers = allPlayers.filter { it.status == PlayerStatus.ACTIVE }.mapIndexed { index, player ->
                    player.copy(seat = index + 1)
                }
                allPlayers.filter { it.status == PlayerStatus.ELIMINATED } + finalPlayers
            }
        }

        val tables = activePlayers.groupBy { it.table }
        val tableCounts = tables.mapValues { it.value.size }

        val maxPlayersOnTable = tableCounts.values.maxOrNull() ?: 0
        val minPlayersOnTable = tableCounts.values.minOrNull() ?: 0

        if (maxPlayersOnTable > minPlayersOnTable + 2) {
            val largestTableNumber = tableCounts.filterValues { it == maxPlayersOnTable }.keys.first()
            val smallestTableNumber = tableCounts.filterValues { it == minPlayersOnTable }.keys.first()

            val playerToMove = tables[largestTableNumber]?.first() // Simplificado: move o primeiro jogador

            if (playerToMove != null) {
                val updatedPlayers = players.toMutableList()
                val playerIndex = updatedPlayers.indexOfFirst { it.id == playerToMove.id }
                if (playerIndex != -1) {
                    updatedPlayers[playerIndex] = playerToMove.copy(table = smallestTableNumber)
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