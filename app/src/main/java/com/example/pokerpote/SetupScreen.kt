// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/SetupScreen.kt
package com.example.pokerpote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

// ==================================================
//         Tela de Configuração (SetupScreen)
// ==================================================

@Composable
fun SetupScreen(
    snackbarHostState: SnackbarHostState, // Recebe o estado do Snackbar para poder mostrar mensagens.
    // Função (callback) que será chamada quando o usuário clicar em "Iniciar Jogo" com dados válidos.
    onStartGame: (multiplier: Double, initialBuyIn: Double, initialBB: Int, initialStackDepth: Double, dynamicUpdates: Boolean, bbRate: Double) -> Unit
) {
    // --- Estados dos Campos de Input ---
    // Usam String para permitir digitação livre pelo usuário (incluindo pontos decimais ou erros temporários).
    // 'remember' para manter o estado entre recomposições.
    var multiplierInput by remember { mutableStateOf("4") } // Valor inicial padrão
    var initialBuyInInput by remember { mutableStateOf("20") } // Valor inicial padrão
    var initialBBInput by remember { mutableStateOf("2") } // BB inicial padrão 2
    var dynamicUpdatesEnabled by remember { mutableStateOf(false) } // Estado do Switch (ligado/desligado)
    //var minBuyInRateInput by remember { mutableStateOf("1") } // Taxa em % (valor inicial padrão)
    var bbRateInput by remember { mutableStateOf("0.05") }     // Taxa em % (valor inicial padrão)

    // --- Estados para Mensagens de Erro ---
    // Usam String? (String anulável). Se for null, não há erro. Se tiver texto, exibe o erro no TextField.
    var multiplierError by remember { mutableStateOf<String?>(null) }
    var buyInError by remember { mutableStateOf<String?>(null) }
    var initialBBError by remember { mutableStateOf<String?>(null) }
    //var minBuyInRateError by remember { mutableStateOf<String?>(null) }
    var bbRateError by remember { mutableStateOf<String?>(null) }
    var stackDepthError by remember { mutableStateOf<String?>(null) }

    // --- Utilitários ---
    val focusManager = LocalFocusManager.current // Obtém o gerenciador de foco para controlar o teclado.
    val scope = rememberCoroutineScope() // Obtém um escopo para lançar coroutines (mostrar Snackbar).
    val scrollState = rememberScrollState() // Estado para permitir rolagem vertical na Column principal.

    // --- NOVO: Cálculo Dinâmico da Profundidade do Stack ---
    val calculatedStackDepth: Double? by remember {
        derivedStateOf {
            // Tenta converter os inputs necessários
            val buyIn = initialBuyInInput.toDoubleOrNull()
            val mf = multiplierInput.toDoubleOrNull()
            val bb = initialBBInput.toIntOrNull()

            // Verifica se todos são válidos e positivos (BB não pode ser zero)
            if (buyIn != null && mf != null && bb != null && buyIn > 0 && mf > 0 && bb > 0) {
                // Calcula a profundidade
                try {
                    // Usar BigDecimal para precisão, especialmente se os números forem grandes ou pequenos
                    val bdBuyIn = BigDecimal(buyIn)
                    val bdMf = BigDecimal(mf)
                    val bdBb = BigDecimal(bb)
                    // Calcula (buyIn * mf) / bb com 2 casas decimais, arredondando para cima (HALF_UP)
                    bdBuyIn.multiply(bdMf).divide(bdBb, 2, RoundingMode.HALF_UP).toDouble()
                } catch (e: ArithmeticException) {
                    // Caso raro de erro aritmético mesmo com BigDecimal
                    null // Retorna null em caso de erro de cálculo
                }
            } else {
                null // Retorna null se algum input for inválido
            }
        }
    }

    // --- Funções Auxiliares Internas ---
    // Limpa todas as mensagens de erro. Chamada quando qualquer campo é modificado para remover erros antigos.
    fun clearErrorsOnChange() {
        multiplierError = null
        buyInError = null
        initialBBError = null
        bbRateError = null
        //minBuyInRateError = null
    }

    // Filtro para permitir apenas DÍGITOS (sem ponto decimal)
    fun filterIntegerInput(input: String): String {
        return input.filter { it.isDigit() }
    }

    // Filtra a entrada do usuário para permitir apenas dígitos e no máximo um ponto decimal.
    // Evita que o usuário digite letras ou múltiplos pontos.
    fun filterNumericInput(input: String): String {
        // 1. Filtra mantendo apenas dígitos e pontos.
        return input.filter { it.isDigit() || it == '.' }
            .let { filtered -> // 2. Processa o resultado do filtro.
                // Conta quantos pontos existem.
                if (filtered.count { it == '.' } > 1) {
                    // Se houver mais de um ponto, encontra o índice do primeiro.
                    val firstDotIndex = filtered.indexOf('.')
                    // Mantém a string até o primeiro ponto (inclusive) e remove os pontos subsequentes.
                    filtered.substring(0, firstDotIndex + 1) +
                            filtered.substring(firstDotIndex + 1).replace(".", "")
                } else {
                    // Se houver 0 ou 1 ponto, retorna a string filtrada como está.
                    filtered
                }
            }
    }

    // Valida todas as entradas dos campos e, se tudo estiver OK, chama a função 'onStartGame'.
    // Caso contrário, define as mensagens de erro e mostra um Snackbar.
    fun validateAndStart() {
        focusManager.clearFocus() // Esconde o teclado virtual.
        var hasError = false // Flag para indicar se algum erro foi encontrado.
        var errorMsg = "Verifique os campos com erro." // Mensagem genérica inicial para o Snackbar.
        stackDepthError = null // Limpa erro da profundidade antes de validar

        // --- Validação e Conversão do Multiplicador ---
        val multiplier = multiplierInput.toDoubleOrNull() // Tenta converter a String para Double. Retorna null se falhar.
        if (multiplier == null || multiplier <= 0) { // Multiplicador deve ser um número positivo.
            multiplierError = "MF inválido (> 0)" // Define a mensagem de erro para o campo.
            hasError = true // Marca que houve erro.
        }

        // --- Validação e Conversão do Buy-in Inicial ---
        val initialBuyIn = initialBuyInInput.toDoubleOrNull()
        if (initialBuyIn == null || initialBuyIn <= 0) { // Buy-in deve ser um número positivo.
            buyInError = "Buy-in inválido (> 0)"
            hasError = true
        }

        // --- VALIDAÇÃO: BB Inicial ---
        val initialBB = initialBBInput.toIntOrNull() // Tenta converter para Inteiro
        if (initialBB == null || initialBB <= 0) { // Deve ser um inteiro positivo
            initialBBError = "BB inválido (> 0 fichas)"
            hasError = true
        }

        // --- VALIDAÇÃO E CÁLCULO DA PROFUNDIDADE INICIAL ---
        var initialStackDepth: Double? = null
        if (multiplier != null && initialBuyIn != null && initialBB != null && multiplier > 0 && initialBuyIn > 0 && initialBB > 0) {
            try {
                // Recalcula aqui com os valores validados para passar adiante
                val bdBuyIn = BigDecimal(initialBuyIn)
                val bdMf = BigDecimal(multiplier)
                val bdBb = BigDecimal(initialBB)
                initialStackDepth = bdBuyIn.multiply(bdMf).divide(bdBb, 2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                // Captura erro no cálculo final (improvável com as validações anteriores, mas seguro)
                stackDepthError = "Erro ao calcular Profundidade"
                errorMsg = "Erro nos valores para calcular Profundidade."
                hasError = true
            }
        } else {
            // Se os inputs básicos já falharam, não tenta calcular
            if (!hasError) { // Só mostra erro de profundidade se os outros campos pareciam OK
                stackDepthError = "Valores inválidos para Profundidade"
                errorMsg = "Verifique os valores de Buy-in, MF e BB."
                hasError = true // Garante que não prossiga
            }
        }
        // --- FIM VALIDAÇÃO PROFUNDIDADE ---

        // Validação Taxa BB (somente se dinâmico habilitado)
        var bbRateDecimal: Double? = null
        if (dynamicUpdatesEnabled) {
            // Validação do bbRateInput (taxa do BB)
            val bbRatePercent = bbRateInput.toDoubleOrNull()
            if (bbRatePercent == null || bbRatePercent <= 0) {
                bbRateError = "Taxa inválida (> 0%)"
                hasError = true
                errorMsg = "Verifique a taxa de aumento do BB."
            } else {
                bbRateDecimal = bbRatePercent / 100.0
            }
        } else {
            // Se dinâmico desligado, usa valor padrão (será ignorado por PokerGame)
            bbRateDecimal = 0.001
        }

        // --- Ação Final ---
        // Verifica se NÃO houve erro E se todas as conversões para Double foram bem-sucedidas
        // (os valores não são null).
        if (!hasError && multiplier != null && initialBuyIn != null && initialBB != null && initialStackDepth != null && bbRateDecimal != null) {
            // Tudo OK! Chama a função 'onStartGame' (passada como parâmetro para SetupScreen)
            // com os valores validados e convertidos. Isso navegará para a tela do jogo.
            onStartGame(multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdatesEnabled, bbRateDecimal)
        } else {
            // Se houve erro ou alguma conversão falhou, mostra um Snackbar com a mensagem de erro.
            scope.launch { // Usa a coroutine scope para mostrar o Snackbar.
                snackbarHostState.showSnackbar(errorMsg, duration = SnackbarDuration.Short)
            }
        }
    }

    // --- Layout da UI (Interface do Usuário) ---
    // Column organiza os elementos verticalmente.
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda a tela.
            .padding(horizontal = 16.dp, vertical = 8.dp) // Adiciona espaçamento nas laterais e vertical.
            .verticalScroll(scrollState), // Permite rolar o conteúdo se ele não couber na tela.
        horizontalAlignment = Alignment.CenterHorizontally, // Centraliza os elementos filhos horizontalmente.
    ) {
        // Título da tela
        Text("Configurar Mesa", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
        // Espaçador vertical
        Spacer(modifier = Modifier.height(24.dp))

        // --- Campos Principais ---
        // Campo de texto para o Multiplicador (MF)
        OutlinedTextField(
            value = multiplierInput, // O valor atual do estado 'multiplierInput'.
            onValueChange = { // Função chamada sempre que o texto no campo muda.
                multiplierInput = filterNumericInput(it) // Atualiza o estado com o valor filtrado.
                clearErrorsOnChange() // Limpa os erros ao digitar.
            },
            label = { Text("Multiplicador (MF)") }, // Texto de dica (placeholder).
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, // Mostra teclado numérico.
                imeAction = ImeAction.Next // Botão de ação do teclado será "Next" (ir para o próximo campo).
            ),
            singleLine = true, // Campo de texto de linha única.
            isError = multiplierError != null, // Define visualmente se o campo está em estado de erro.
            // Texto de suporte exibido abaixo do campo (mostra a mensagem de erro se houver).
            supportingText = { if (multiplierError != null) Text(multiplierError!!) },
            modifier = Modifier.fillMaxWidth() // Ocupa toda a largura disponível.
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espaçador

        // Campo de texto para o Buy-in Inicial
        OutlinedTextField(
            value = initialBuyInInput,
            onValueChange = { initialBuyInInput = filterNumericInput(it); clearErrorsOnChange() },
            label = { Text("Buy-in Inicial (R$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), // Ação "Next"
            singleLine = true,
            isError = buyInError != null,
            supportingText = { if (buyInError != null) Text(buyInError!!) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp)) // Espaçador

        OutlinedTextField(
            value = initialBBInput,
            onValueChange = { initialBBInput = filterIntegerInput(it); clearErrorsOnChange() }, // Usa o filtro de inteiros
            label = { Text("Big Blind Inicial (fichas)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), // Tipo número, ação Next
            singleLine = true,
            isError = initialBBError != null,
            supportingText = { if (initialBBError != null) Text(initialBBError!!) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- NOVO: Exibição da Profundidade do Stack ---
        Text(
            // Exibe o valor calculado ou "--" se inválido/não calculado
            text = "Profundidade do Stack: ${calculatedStackDepth?.let { "%.2f".format(it) } ?: "--"}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 8.dp),
            // Mostra erro específico se houver
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

        // --- Configurações Dinâmicas ---
        // Row organiza elementos horizontalmente. Usada para o texto e o Switch.
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) // Divisor visual

        Row(
            modifier = Modifier.fillMaxWidth(), // Ocupa a largura toda.
            verticalAlignment = Alignment.CenterVertically, // Alinha texto e switch verticalmente no centro.
            horizontalArrangement = Arrangement.SpaceBetween // Coloca espaço máximo entre o texto e o switch.
        ) {
            // Texto descritivo para o Switch.
            Text(
                text = "Aumentar BB e Buy-in dinamicamente?",
                style = MaterialTheme.typography.bodyLarge, // Estilo de texto do tema.
                modifier = Modifier
                    .weight(1f) // Faz o texto ocupar o espaço restante na Row.
                    .padding(end = 16.dp) // Espaço à direita antes do Switch.
            )
            // Componente Switch (liga/desliga).
            Switch(
                checked = dynamicUpdatesEnabled, // O estado atual do switch (ligado/desligado).
                onCheckedChange = { dynamicUpdatesEnabled = it } // Atualiza o estado quando o switch é clicado.
                // 'it' é o novo valor booleano (true/false).
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // --- Campos de Taxa Condicionais ---
        // AnimatedVisibility mostra/esconde seu conteúdo com animação.
        AnimatedVisibility(
            visible = dynamicUpdatesEnabled, // Só é visível se o switch estiver LIGADO.
            enter = fadeIn(animationSpec = tween(300)), // Animação de entrada (aparecer suavemente).
            exit = fadeOut(animationSpec = tween(300)) // Animação de saída (desaparecer suavemente).
        ) {
            // Column para agrupar os campos de taxa (só aparece se o switch estiver ligado).
            Column {

                // Campo de texto para a Taxa de Aumento do Big Blind (%)
                OutlinedTextField(
                    value = bbRateInput,
                    onValueChange = { bbRateInput = filterNumericInput(it); clearErrorsOnChange() },
                    label = { Text("Taxa Aum. Big Blind (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), // Último campo, usa "Done".
                    // Define a ação a ser executada quando o botão "Done" do teclado for pressionado.
                    keyboardActions = KeyboardActions(onDone = { validateAndStart() }), // Tenta iniciar o jogo.
                    singleLine = true,
                    isError = bbRateError != null,
                    supportingText = { if (bbRateError != null) Text(bbRateError!!) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dynamicUpdatesEnabled
                )
            } // Fim da Column condicional
        } // Fim do AnimatedVisibility

        // Usa Spacer para empurrar o botão e o crédito para baixo se houver espaço
        //Spacer(modifier = Modifier.weight(1f)) // Ocupa espaço flexível
        Spacer(modifier = Modifier.height(32.dp))

        // Botão para iniciar o jogo
        Button(
            onClick = { validateAndStart() }, // Chama a função de validação e início ao ser clicado.
            // Ocupa 60% da largura da tela e centraliza-se.
            modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)
        ) {
            Text("Iniciar Jogo")
        }

        // --- NOVO: Crédito do Desenvolvedor ---
        Text(
            text = "Desenvolvido por Renato A M",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp) // Padding inferior
        )
        // --- FIM CRÉDITO ---
        //Spacer(modifier = Modifier.height(16.dp))
    } // Fim Column principal
}
