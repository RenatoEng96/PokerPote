package com.example.pokerpote.tournament

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BlindLevel(
    val id: String = UUID.randomUUID().toString(),
    val level: Int,
    var smallBlind: Int,
    var bigBlind: Int,
    var ante: Int = 0,
    var durationMinutes: Int
)

@Serializable
data class PrizeTier(
    val id: String = UUID.randomUUID().toString(),
    val position: Int,
    var percentage: Float // Ex: 0.5f para 50%
)

@Serializable
data class TournamentPlayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var chipCount: Int,
    var table: Int = 0,
    var seat: Int = 0,
    var status: PlayerStatus = PlayerStatus.ACTIVE,
    var rebuys: Int = 0,
    var addon: Boolean = false,
    var eliminatedPosition: Int? = null,
    var prizeWon: Double = 0.0
)

enum class PlayerStatus { ACTIVE, ELIMINATED }

@Serializable
data class Tournament(
    val id: String = "main_tournament", // ID fixo para salvar/carregar sempre o mesmo torneio
    var name: String = "Meu Torneio",
    var buyIn: Double = 10.0,
    var prizePoolContribution: Double = 8.0,
    var feeContribution: Double = 2.0,
    var startingStack: Int = 10000,
    var players: List<TournamentPlayer> = listOf(),
    var blindStructure: List<BlindLevel> = listOf(),
    var prizeStructure: List<PrizeTier> = listOf(),
    var rebuyEnabled: Boolean = true,
    var rebuyPrice: Double = 10.0,
    var rebuyChips: Int = 10000,
    var addonEnabled: Boolean = true,
    var addonPrice: Double = 10.0,
    var addonChips: Int = 15000,
    var currentLevel: Int = 1,
    var timeRemainingInMillis: Long = 0,
    var tournamentState: TournamentState = TournamentState.NOT_STARTED,
    var lastUpdate: Long = System.currentTimeMillis()
) {
    val totalPrizePool: Double
        get() {
            val buyIns = players.size * prizePoolContribution
            val rebuysTotal = players.sumOf { it.rebuys } * (rebuyEnabled.takeIf { it }?.let { rebuyPrice } ?: 0.0)
            val addonsTotal = players.count { it.addon } * (addonEnabled.takeIf { it }?.let { addonPrice } ?: 0.0)
            return buyIns + rebuysTotal + addonsTotal
        }

    val playersRemaining: Int
        get() = players.count { it.status == PlayerStatus.ACTIVE }

    val averageStack: Int
        get() {
            val totalChips = players.sumOf { it.chipCount }
            return if (playersRemaining > 0) totalChips / playersRemaining else 0
        }
}

enum class TournamentState { NOT_STARTED, RUNNING, PAUSED, FINISHED }