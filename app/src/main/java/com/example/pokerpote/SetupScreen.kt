// main/java/com/example/pokerpote/SetupScreen.kt
package com.example.pokerpote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun SetupScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    onStartGame: (multiplier: Double, initialBuyIn: Double, initialBB: Int, initialStackDepth: Double, dynamicUpdates: Boolean, bbRate: Double) -> Unit
) {
    var multiplierInput by remember { mutableStateOf("4") }
    var initialBuyInInput by remember { mutableStateOf("20") }
    var initialBBInput by remember { mutableStateOf("2") }
    var dynamicUpdatesEnabled by remember { mutableStateOf(false) }
    var bbRateInput by remember { mutableStateOf("0.05") }

    var multiplierError by remember { mutableStateOf<String?>(null) }
    var buyInError by remember { mutableStateOf<String?>(null) }
    var initialBBError by remember { mutableStateOf<String?>(null) }
    var bbRateError by remember { mutableStateOf<String?>(null) }
    var stackDepthError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val calculatedStackDepth: Double? by remember {
        derivedStateOf {
            val buyIn = initialBuyInInput.toDoubleOrNull()
            val mf = multiplierInput.toDoubleOrNull()
            val bb = initialBBInput.toIntOrNull()

            if (buyIn != null && mf != null && bb != null && buyIn > 0 && mf > 0 && bb > 0) {
                try {
                    val bdBuyIn = BigDecimal(buyIn)
                    val bdMf = BigDecimal(mf)
                    val bdBb = BigDecimal(bb)
                    bdBuyIn.multiply(bdMf).divide(bdBb, 2, RoundingMode.HALF_UP).toDouble()
                } catch (e: ArithmeticException) {
                    null
                }
            } else {
                null
            }
        }
    }

    fun clearErrorsOnChange() {
        multiplierError = null
        buyInError = null
        initialBBError = null
        bbRateError = null
    }

    fun filterIntegerInput(input: String): String {
        return input.filter { it.isDigit() }
    }

    fun filterNumericInput(input: String): String {
        return input.filter { it.isDigit() || it == '.' }
            .let { filtered ->
                if (filtered.count { it == '.' } > 1) {
                    val firstDotIndex = filtered.indexOf('.')
                    filtered.substring(0, firstDotIndex + 1) +
                            filtered.substring(firstDotIndex + 1).replace(".", "")
                } else {
                    filtered
                }
            }
    }

    fun validateAndStart() {
        focusManager.clearFocus()
        var hasError = false
        var errorMsg = "Verifique os campos com erro."
        stackDepthError = null

        val multiplier = multiplierInput.toDoubleOrNull()
        if (multiplier == null || multiplier <= 0) {
            multiplierError = "MF inválido (> 0)"
            hasError = true
        }

        val initialBuyIn = initialBuyInInput.toDoubleOrNull()
        if (initialBuyIn == null || initialBuyIn <= 0) {
            buyInError = "Buy-in inválido (> 0)"
            hasError = true
        }

        val initialBB = initialBBInput.toIntOrNull()
        if (initialBB == null || initialBB <= 0) {
            initialBBError = "BB inválido (> 0 fichas)"
            hasError = true
        }

        var initialStackDepth: Double? = null
        if (multiplier != null && initialBuyIn != null && initialBB != null && multiplier > 0 && initialBuyIn > 0 && initialBB > 0) {
            try {
                val bdBuyIn = BigDecimal(initialBuyIn)
                val bdMf = BigDecimal(multiplier)
                val bdBb = BigDecimal(initialBB)
                initialStackDepth = bdBuyIn.multiply(bdMf).divide(bdBb, 2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                stackDepthError = "Erro ao calcular Profundidade"
                errorMsg = "Erro nos valores para calcular Profundidade."
                hasError = true
            }
        } else {
            if (!hasError) {
                stackDepthError = "Valores inválidos para Profundidade"
                errorMsg = "Verifique os valores de Buy-in, MF e BB."
                hasError = true
            }
        }

        var bbRateDecimal: Double? = null
        if (dynamicUpdatesEnabled) {
            val bbRatePercent = bbRateInput.toDoubleOrNull()
            if (bbRatePercent == null || bbRatePercent <= 0) {
                bbRateError = "Taxa inválida (> 0%)"
                hasError = true
                errorMsg = "Verifique a taxa de aumento do BB."
            } else {
                bbRateDecimal = bbRatePercent / 100.0
            }
        } else {
            bbRateDecimal = 0.001
        }

        if (!hasError && multiplier != null && initialBuyIn != null && initialBB != null && initialStackDepth != null && bbRateDecimal != null) {
            onStartGame(multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdatesEnabled, bbRateDecimal)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(errorMsg, duration = SnackbarDuration.Short)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- CORREÇÃO APLICADA AQUI ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart) // Alinha dentro do Box
            ) {
                Text("Voltar para Home")
            }
        }

        Text("Configurar Mesa de Cash Game", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = multiplierInput,
            onValueChange = {
                multiplierInput = filterNumericInput(it)
                clearErrorsOnChange()
            },
            label = { Text("Multiplicador (MF)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            isError = multiplierError != null,
            supportingText = { if (multiplierError != null) Text(multiplierError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = initialBuyInInput,
            onValueChange = { initialBuyInInput = filterNumericInput(it); clearErrorsOnChange() },
            label = { Text("Buy-in Inicial (R$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = buyInError != null,
            supportingText = { if (buyInError != null) Text(buyInError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = initialBBInput,
            onValueChange = { initialBBInput = filterIntegerInput(it); clearErrorsOnChange() },
            label = { Text("Big Blind Inicial (fichas)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = initialBBError != null,
            supportingText = { if (initialBBError != null) Text(initialBBError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Profundidade do Stack: ${calculatedStackDepth?.let { "%.2f".format(it) } ?: "--"}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp),
            color = if (stackDepthError != null) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
        if (stackDepthError != null) {
            Text(
                text = stackDepthError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Aumentar BB e Buy-in dinamicamente?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Switch(
                checked = dynamicUpdatesEnabled,
                onCheckedChange = { dynamicUpdatesEnabled = it }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = dynamicUpdatesEnabled,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Column {
                OutlinedTextField(
                    value = bbRateInput,
                    onValueChange = { bbRateInput = filterNumericInput(it); clearErrorsOnChange() },
                    label = { Text("Taxa Aum. Big Blind (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { validateAndStart() }),
                    singleLine = true,
                    isError = bbRateError != null,
                    supportingText = { if (bbRateError != null) Text(bbRateError!!) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dynamicUpdatesEnabled
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { validateAndStart() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Iniciar Jogo")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Desenvolvido por Renato A M",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}