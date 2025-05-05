// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/Utils.kt
package com.example.pokerpote

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

// ==================================================
//         Componentes Composable Auxiliares
//         (Usados principalmente por PokerGameScreen)
// ==================================================

// --- Funções de Formatação ---
// Formata um valor Double como moeda Real (BRL).
// É 'private' porque só precisa ser acessível dentro deste arquivo (MainActivity.kt).
fun formatCurrency(value: Double): String {
    // Obtém um formatador de moeda para o Locale Português (Brasil).
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return try { // Usa try-catch para o caso de algum valor inesperado causar erro na formatação.
        format.format(value)
    } catch (e: Exception) {
        "R$ --" // Retorna um placeholder em caso de erro.
    }
}

// Formata um valor Double (representando fichas) como um número inteiro String.
// É 'private'.
fun formatChips(value: Double): String {
    // 1. Obter um formatador de número para o Locale Português (Brasil)
    val format = NumberFormat.getNumberInstance(Locale("pt", "BR"))
    // 2. Definir que não queremos casas decimais para fichas (opcional, mas comum)
    format.maximumFractionDigits = 0
    return try {
        // 3. Arredondar o valor Double para Long (para números inteiros grandes)
        val roundedValue = value.roundToLong()
        // 4. Formatar o valor arredondado (ex: 22000L -> "22.000")
        format.format(roundedValue)
    } catch (e: Exception) {
        "--" // Retorna placeholder em caso de erro
    }
}