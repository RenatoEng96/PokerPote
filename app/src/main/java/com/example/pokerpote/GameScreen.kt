// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/GameScreen.kt
package com.example.pokerpote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ==================================================
//         Tela Principal do Jogo (PokerGameScreen)
// ==================================================


@Composable
fun PokerGameScreen(
    pokerGame: PokerGame, // Recebe a instância ATIVA do jogo (criada em PokerAppNavigation).
    snackbarHostState: SnackbarHostState, // Recebe o estado do Snackbar (embora showSnackbar seja usado diretamente).
    showSnackbar: (String) -> Unit, // Recebe a FUNÇÃO para mostrar Snackbars.
    onEndGame: () -> Unit, // Recebe a FUNÇÃO (callback) para ser chamada quando o usuário quiser encerrar o jogo.
    // --- NOVO PARÂMETRO ---
    onGameUpdated: () -> Unit // Callback para notificar que o jogo foi atualizado e precisa ser salvo
) {
    // --- Estados da UI específicos desta tela ---
    // Estados para os campos de input de adicionar jogador/buy-in.
    var newPlayerName by remember { mutableStateOf("") }
    var newPlayerBuyInAmount by remember { mutableStateOf("") }
    var additionalBuyInAmount by remember { mutableStateOf("") }
    // Estado para guardar o jogador selecionado no dropdown para adicionar buy-in.
    var selectedPlayerForBuyIn by remember { mutableStateOf<Player?>(null) }
    // Estado para guardar o jogador selecionado para remoção (no diálogo).
    var selectedPlayerForRemoval by remember { mutableStateOf<Player?>(null) }
    // Estado para controlar a visibilidade do diálogo de confirmação de remoção.
    var showRemoveDialog by remember { mutableStateOf(false) }
    // Estado para o input de fichas no diálogo de remoção.
    var chipsToRemoveInput by remember { mutableStateOf("") }
    // Estado para controlar a visibilidade do diálogo de confirmação de encerramento da mesa.
    var showEndGameDialog by remember { mutableStateOf(false) }
    // ESTADO: Para o diálogo de resultado do Cash-Out ---
    var showCashOutResultDialog by remember { mutableStateOf(false) }
    // Guarda a informação para o diálogo: Nome do jogador e Valor do cash-out
    var lastCashOutInfo by remember { mutableStateOf<Pair<String, Double>?>(null) }

    // --- Mecanismo de Atualização do Estado do Jogo ---
    // 'gameVersion' é um contador simples. Quando a lógica do 'pokerGame' é alterada
    // (jogador adicionado, buy-in, remoção), chamamos 'signalGameUpdate()'.
    // Isso incrementa 'gameVersion', forçando a reavaliação dos 'derivedStateOf' abaixo.
    var gameVersion by remember { mutableStateOf(0) }
    fun signalGameUpdate() {
        gameVersion++
        onGameUpdated() // <--- CHAMA O SALVAMENTO AQUI
    }

    // --- Estados Derivados (Observam 'gameVersion') ---
    // 'derivedStateOf' otimiza a leitura do estado do 'pokerGame'.
    // O bloco dentro dele só é reexecutado se a 'key' (gameVersion) mudar.
    // Isso evita chamar os getters do 'pokerGame' em toda recomposição da UI,
    // apenas quando sabemos que os dados do jogo PODEM ter mudado.
    val players by remember(gameVersion) { derivedStateOf { pokerGame.getPlayers() } }
    val currentBB by remember(gameVersion) { derivedStateOf { pokerGame.getBB() } }
    val currentMinBuyIn by remember(gameVersion) { derivedStateOf { pokerGame.getCurrentMinBuyIn() } }
    val totalPot by remember(gameVersion) { derivedStateOf { pokerGame.getTotalPot() } }
    val sumBuyIns by remember(gameVersion) { derivedStateOf { pokerGame.getSumBuyIns() } }
    val cashOutTotal by remember(gameVersion) { derivedStateOf { pokerGame.getCashOutTotal() } }
    val multiplier by remember(gameVersion) { derivedStateOf { pokerGame.multiplier } } // Multiplicador não muda, mas incluído por consistência

    // --- Utilitários ---
    val focusManager = LocalFocusManager.current // Para esconder o teclado

    // --- Layout Principal ---
    // LazyColumn é usado porque a lista de jogadores pode crescer.
    // Ele só compõe e exibe os itens que estão visíveis na tela, sendo eficiente.
    LazyColumn(
        modifier = Modifier.fillMaxSize(), // Ocupa toda a tela.
        contentPadding = PaddingValues(16.dp), // Espaçamento interno em todos os lados.
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espaço vertical entre cada item da lista.
    ) {
        // --- Botão Encerrar Mesa ---
        // 'item' define um único elemento dentro da LazyColumn.
        item {
            Button(
                onClick = { showEndGameDialog = true }, // Abre o diálogo de confirmação.
                modifier = Modifier.fillMaxWidth(), // Ocupa a largura toda.
                // Define cores customizadas para o botão (cor de erro para ação destrutiva).
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Encerrar Mesa e Voltar")
            }
        }

        // --- Seção de Estatísticas ---
        item {
            // Usa o Composable auxiliar 'GameStatsSection', passando os valores derivados do estado do jogo.
            GameStatsSection(
                bb = currentBB, minBuyIn = currentMinBuyIn, totalPot = totalPot,
                sumBuyIns = sumBuyIns, cashOutTotal = cashOutTotal, multiplier = multiplier
            )
        }

        // --- Seção Adicionar Novo Jogador ---
        item {
            // Usa o Composable auxiliar 'AddPlayerSection'.
            AddPlayerSection(
                newPlayerName = newPlayerName, // Passa o estado atual do nome.
                newBuyInAmount = newPlayerBuyInAmount, // Passa o estado atual do valor.
                onNameChange = { newPlayerName = it }, // Callback para atualizar o nome quando o TextField muda.
                onAmountChange = { newValue -> // Callback para atualizar o valor do buy-in.
                    // Filtra a entrada para permitir apenas números e um ponto decimal.
                    newPlayerBuyInAmount = newValue.filter { it.isDigit() || it == '.' }
                        .let { filtered -> // Garante no máximo um ponto.
                            if (filtered.count { it == '.' } > 1) {
                                val firstDotIndex = filtered.indexOf('.')
                                filtered.substring(0, firstDotIndex + 1) +
                                        filtered.substring(firstDotIndex + 1).replace(".", "")
                            } else { filtered }
                        }
                },
                // Callback chamado quando o botão "Adicionar Jogador" é clicado.
                onAddPlayer = {
                    focusManager.clearFocus() // Esconde o teclado.
                    val name = newPlayerName.trim() // Pega o nome, removendo espaços extras.
                    val amount = newPlayerBuyInAmount.toDoubleOrNull() // Tenta converter o valor para Double.

                    // Validação básica local antes de chamar a lógica do jogo.
                    if (name.isNotBlank() && amount != null && amount > 0) {
                        try { // Tenta adicionar o jogador usando a instância 'pokerGame'.
                            println("DEBUG: Tentando adicionar jogador $name com buy-in $amount")
                            pokerGame.addPlayer(name, amount) // Chama o método da classe PokerGame.
                            // Limpa os campos de input após sucesso.
                            newPlayerName = ""; newPlayerBuyInAmount = ""
                            // SINALIZA que o estado do jogo mudou, para atualizar a UI (lista de jogadores, stats).
                            signalGameUpdate()
                            println("DEBUG: Jogador $name adicionado com sucesso.")
                            // Opcional: Mostrar confirmação via Snackbar.
                            // showSnackbar("$name adicionado com buy-in de ${formatCurrency(amount)}")
                        } catch (e: IllegalArgumentException) { // Captura erros de validação do PokerGame (ex: buy-in baixo).
                            val errorMsg = "Erro: ${e.message}" // Usa a mensagem da exceção.
                            println("DEBUG: Falha ao adicionar jogador - $errorMsg")
                            showSnackbar(errorMsg) // Mostra o erro na UI.
                        } catch (e: Exception) { // Captura outros erros inesperados.
                            val errorMsg = "Erro inesperado ao adicionar jogador."
                            println("DEBUG: Falha ao adicionar jogador - $errorMsg Exception: ${e.message}")
                            showSnackbar(errorMsg)
                        }
                    } else { // Falha na validação local (nome vazio ou valor inválido).
                        val errorMsg = "Nome ou valor do buy-in inválido."
                        println("DEBUG: Falha ao adicionar jogador - $errorMsg (Validação local)")
                        showSnackbar(errorMsg)
                    }
                }
            )
        }

        // --- Seção Adicionar Buy-In ---
        item {
            // Usa o Composable auxiliar 'AddBuyInSection'.
            AddBuyInSection(
                players = players, // Passa a lista atual de jogadores para o dropdown.
                selectedPlayer = selectedPlayerForBuyIn, // Passa o jogador atualmente selecionado.
                onPlayerSelected = { selectedPlayerForBuyIn = it }, // Callback para atualizar o jogador selecionado.
                buyInAmount = additionalBuyInAmount, // Passa o estado atual do valor do buy-in adicional.
                onAmountChange = { newValue -> // Callback para atualizar o valor, com filtro numérico.
                    additionalBuyInAmount = newValue.filter { it.isDigit() || it == '.' }
                        .let { filtered -> /* Limita a um ponto */
                            if (filtered.count { it == '.' } > 1) {
                                val firstDotIndex = filtered.indexOf('.')
                                filtered.substring(0, firstDotIndex + 1) +
                                        filtered.substring(firstDotIndex + 1).replace(".", "")
                            } else { filtered }
                        }
                },
                // Callback chamado quando o botão "Adicionar Buy-In" é clicado.
                onAddBuyIn = {
                    focusManager.clearFocus() // Esconde teclado.
                    val amount = additionalBuyInAmount.toDoubleOrNull() // Tenta converter o valor.

                    // Validação local: jogador selecionado e valor válido.
                    if (selectedPlayerForBuyIn != null && amount != null && amount > 0) {
                        try { // Tenta adicionar o buy-in usando a instância 'pokerGame'.
                            println("DEBUG: Tentando adicionar buy-in de $amount para ${selectedPlayerForBuyIn!!.name}")
                            // Chama o método da classe PokerGame. Usa '!!' pois já verificou que não é null.
                            pokerGame.addBuyIn(selectedPlayerForBuyIn!!, amount)
                            // Limpa o campo de valor após sucesso.
                            additionalBuyInAmount = ""
                            // NÃO limpa o jogador selecionado, o usuário pode querer adicionar mais.
                            // SINALIZA que o estado do jogo mudou.
                            signalGameUpdate()
                            println("DEBUG: Buy-in adicionado com sucesso para ${selectedPlayerForBuyIn!!.name}")
                            // Opcional: Mostrar confirmação via Snackbar.
                            // showSnackbar("Buy-in de ${formatCurrency(amount)} adicionado para ${selectedPlayerForBuyIn!!.name}")
                        } catch (e: IllegalArgumentException) { // Captura erro de validação (ex: buy-in baixo).
                            val errorMsg = "Erro: ${e.message}"
                            println("DEBUG: Falha ao adicionar buy-in - $errorMsg")
                            showSnackbar(errorMsg)
                        } catch (e: Exception) { // Captura outros erros.
                            val errorMsg = "Erro inesperado ao adicionar buy-in."
                            println("DEBUG: Falha ao adicionar buy-in - $errorMsg Exception: ${e.message}")
                            showSnackbar(errorMsg)
                        }
                    } else { // Falha na validação local.
                        val errorMsg = "Selecione um jogador e insira um valor de buy-in válido."
                        println("DEBUG: Falha ao adicionar buy-in - $errorMsg (Validação local)")
                        showSnackbar(errorMsg)
                    }
                }
            )
        }

        // --- Cabeçalho Lista de Jogadores ---
        // Só mostra o cabeçalho se a lista de jogadores não estiver vazia.
        if (players.isNotEmpty()) {
            item {
                Text("Jogadores na Mesa (${players.size})", style = MaterialTheme.typography.headlineSmall)
            }
        }

        // --- Lista de Jogadores ---
        // 'items' é um helper da LazyColumn para processar listas.
        // Ele itera sobre a lista 'players'.
        // 'key' é importante para o Compose identificar unicamente cada item,
        // otimizando recomposições quando a lista muda (adiciona, remove, reordena).
        // Usar o nome do jogador como chave funciona se os nomes forem únicos.
        // Idealmente, se Player tivesse um ID único, seria melhor usá-lo.
        items(players, key = { player -> player.name }) { player ->
            // Para cada jogador na lista, cria um Composable 'PlayerListItem'.
            PlayerListItem(
                player = player, // Passa os dados do jogador atual.
                // Callback chamado quando o botão "Remover" dentro do item é clicado.
                onRemove = {
                    selectedPlayerForRemoval = player // Guarda qual jogador foi selecionado para remoção.
                    chipsToRemoveInput = "" // Limpa o input de fichas do diálogo.
                    showRemoveDialog = true // Abre o diálogo de remoção.
                }
            )
        }

        // --- Placeholder Lista Vazia ---
        // Mostra uma mensagem se não houver jogadores na mesa.
        if (players.isEmpty()) {
            item {
                Text(
                    text = "Nenhum jogador na mesa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth() // Faz o texto ocupar a largura do item da LazyColumn.
                        .padding(vertical = 16.dp), // Adiciona espaçamento vertical.
                    textAlign = TextAlign.Center // Centraliza o texto DENTRO do componente Text.
                )
            }
        }

        // --- Item de Teste do Snackbar (Remover após depuração) ---
        // Deixei comentado, mas é útil para testar se o Snackbar está funcionando.
//        item {
//            Button(onClick = {
//                println("DEBUG: Botão de teste do Snackbar clicado.")
//                showSnackbar("Mensagem de teste do Snackbar!")
//            }) {
//                Text("Testar Snackbar (Debug)")
//            }
//        }

    } // Fim da LazyColumn

    // --- Diálogo de Remoção ---
    // O diálogo só é composto (desenhado) se 'showRemoveDialog' for true E um jogador estiver selecionado.
    if (showRemoveDialog && selectedPlayerForRemoval != null) {
        // Usa o Composable auxiliar 'RemovePlayerDialog'.
        RemovePlayerDialog(
            playerName = selectedPlayerForRemoval!!.name, // Passa o nome do jogador a remover.
            chipsInput = chipsToRemoveInput, // Passa o estado atual do input de fichas.
            onChipsInputChange = { newValue -> // Callback para atualizar o estado das fichas, com filtro numérico.
                chipsToRemoveInput = newValue.filter { it.isDigit() || it == '.' }
                    .let { filtered -> /* Limita a um ponto */
                        if (filtered.count { it == '.' } > 1) {
                            val firstDotIndex = filtered.indexOf('.')
                            filtered.substring(0, firstDotIndex + 1) +
                                    filtered.substring(firstDotIndex + 1).replace(".", "")
                        } else { filtered }
                    }
            },
            // Callback chamado quando o diálogo é dispensado (clique fora ou botão Cancelar).
            onDismiss = {
                showRemoveDialog = false // Fecha o diálogo.
                selectedPlayerForRemoval = null // Limpa a seleção.
            },
            // Callback chamado quando o botão "Confirmar Remoção" é clicado.
            onConfirm = {
                focusManager.clearFocus() // Esconde teclado.
                val chips = chipsToRemoveInput.toDoubleOrNull() // Tenta converter o valor das fichas.
                val playerToRemove = selectedPlayerForRemoval!! // Guarda referência segura

                // Fecha o diálogo de pedir fichas AGORA
                showRemoveDialog = false
                selectedPlayerForRemoval = null
                chipsToRemoveInput = "" // Limpa o input

                // Validação local: permite remover com 0 fichas, mas o valor deve ser numérico.
                if (chips != null) {
                    try {
                        println("DEBUG: [UI] Tentando chamar pokerGame.removePlayer para ${playerToRemove.name} com $chips fichas.")
                        // Chama a função que AGORA retorna o valor do cash-out
                        val cashOutValue = pokerGame.removePlayer(playerToRemove, chips)
                        println("DEBUG: [UI] pokerGame.removePlayer retornou $cashOutValue.") // Log de sucesso
                        signalGameUpdate() // Atualiza a UI (lista, stats)

                        // Prepara e mostra o NOVO diálogo com o resultado
                        lastCashOutInfo = Pair(playerToRemove.name, cashOutValue)
                        showCashOutResultDialog = true // <-- Ativa o novo diálogo
                        println("DEBUG: [UI] Diálogo de resultado de cash-out preparado.")

                    } catch (e: Exception) { // Captura erros do pokerGame.removePlayer
                        // --- ALTERAÇÃO AQUI: Logar a Stack Trace completa ---
                        println("ERROR: [UI] Falha ao tentar remover jogador ${playerToRemove.name}. Exception Type: ${e::class.simpleName}")
                        // IMPRIME A PILHA DE ERROS DETALHADA NO LOGCAT
                        e.printStackTrace()
                        // --- FIM DA ALTERAÇÃO ---

                        // Atualiza a mensagem para sugerir olhar o Logcat
                        val errorMsg = "Erro ao remover jogador: ${e.message ?: "Detalhes no Logcat"}"
                        println("DEBUG: [UI] Exibindo snackbar de erro: $errorMsg")
                        showSnackbar(errorMsg) // Mostra erro no snackbar
                    }
                } else { // Erro no input de fichas
                    val errorMsg = "Valor de fichas inválido."
                    println("DEBUG: Falha ao remover jogador - $errorMsg (Validação local)")
                    showSnackbar(errorMsg)

//                        pokerGame.removePlayer(selectedPlayerForRemoval!!, chips)
//                        // SINALIZA que o estado do jogo mudou.
//                        signalGameUpdate()
//                        val successMsg = "${selectedPlayerForRemoval!!.name} removido."
//                        println("DEBUG: Jogador removido com sucesso. Tentando mostrar snackbar.")
//                        showSnackbar(successMsg) // Mostra mensagem de sucesso.
//                    } catch (e: Exception) { // Captura qualquer erro durante a remoção.
//                        val errorMsg = "Erro ao remover jogador."
//                        println("DEBUG: Falha ao remover jogador - $errorMsg Exception: ${e.message}")
//                        showSnackbar(errorMsg) // Mostra mensagem de erro.
//                    }
//                    // Fecha o diálogo e limpa estados, INDEPENDENTEMENTE de sucesso ou erro na lógica principal.
//                    showRemoveDialog = false
//                    selectedPlayerForRemoval = null
//                    chipsToRemoveInput = ""
//                } else { // Falha na validação local (valor de fichas inválido).
//                    val errorMsg = "Valor de fichas inválido."
//                    println("DEBUG: Falha ao remover jogador - $errorMsg (Validação local)")
//                    showSnackbar(errorMsg)
                }
            }
        )
    }
    // --- NOVO DIÁLOGO: Resultado do Cash-Out ---
    if (showCashOutResultDialog && lastCashOutInfo != null) {
        CashOutResultDialog(
            info = lastCashOutInfo!!, // Passa o par (Nome, Valor) - !! é seguro aqui
            onDismiss = {
                showCashOutResultDialog = false // Fecha o diálogo
                lastCashOutInfo = null // Limpa a informação
            }
        )
    }

    // --- Diálogo de Confirmação para Encerrar Mesa ---
    // Só é composto se 'showEndGameDialog' for true.
    if (showEndGameDialog) {
        AlertDialog(
            onDismissRequest = { showEndGameDialog = false }, // Fecha o diálogo se clicar fora.
            title = { Text("Encerrar Mesa?") }, // Título do diálogo.
            text = { Text("Tem certeza que deseja encerrar a mesa atual? Todos os dados do jogo atual serão perdidos.") }, // Texto explicativo.
            confirmButton = { // Botão de confirmação (Encerrar).
                Button(
                    onClick = {
                        showEndGameDialog = false // Fecha o diálogo.
                        onEndGame() // CHAMA O CALLBACK passado para PokerGameScreen, que navegará de volta para Setup.
                    },
                    // Botão com cor de erro para indicar ação destrutiva.
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Encerrar") }
            },
            dismissButton = { // Botão para cancelar.
                TextButton(onClick = { showEndGameDialog = false }) { Text("Cancelar") }
            }
        )
    }
} // Fim de PokerGameScreen