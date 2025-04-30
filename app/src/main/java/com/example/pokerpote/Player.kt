package com.example.pokerpote

/**
 * Representa um jogador no jogo de poker.
 * É uma data class, o que significa que o Kotlin gera automaticamente
 * funções úteis como equals(), hashCode(), toString(), e copy().
 *
 * @property name O nome do jogador (imutável após criação).
 * @property totalBuyIn O valor total em dinheiro que o jogador colocou na mesa (mutável, pois pode aumentar com rebuys).
 */
data class Player(
    val name: String,
    var totalBuyIn: Double // 'var' permite que o valor seja atualizado
)