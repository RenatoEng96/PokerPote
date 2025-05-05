// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/GameScreenComponents.kt
package com.example.pokerpote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// --- Seção de Estatísticas ---
// Composable privado que exibe as principais estatísticas do jogo em um Card.

@Composable
fun GameStatsSection(
    bb: Int, minBuyIn: Double, totalPot: Double, sumBuyIns: Double, cashOutTotal: Double, multiplier: Double
) {
    // Card para agrupar visualmente as estatísticas.
    Card(modifier = Modifier.fillMaxWidth()) {
        // Column para organizar os itens de estatística verticalmente.
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(), // Padding interno e ocupa largura.
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espaço entre cada item de estatística.
        ) {
            // Título da seção.
            Text("Estatísticas do Jogo", style = MaterialTheme.typography.titleLarge)
            // Usa o Composable auxiliar 'StatItem' para cada linha de estatística.
            // Passa o rótulo e o valor formatado.
            StatItem("Buy-in Mínimo Atual", formatCurrency(minBuyIn))
            //StatItem("Big Blind (BB)", "$bb fichas") // Combina valor Int com texto.
            StatItem("Big Blind (BB)", "${formatChips(bb.toDouble())} fichas")
            //StatItem("Pote Total Estimado", "${formatChips(totalPot)} fichas")
            StatItem("Pote Total Estimado", "${formatChips(totalPot)} fichas")
            StatItem("Total de Buy-ins", formatCurrency(sumBuyIns))
            StatItem("Total de Cash-outs", formatCurrency(cashOutTotal))
            //StatItem("Multiplicador (MF)", formatChips(multiplier)) // Formata o multiplicador como "inteiro".
            StatItem("Multiplicador (MF)", multiplier.toString())
        }
    }
}

// --- Seção Adicionar Jogador ---
// Composable privado para a seção de adicionar um novo jogador.
@Composable
fun AddPlayerSection(
    newPlayerName: String,          // Estado do nome do jogador (recebido)
    newBuyInAmount: String,       // Estado do valor do buy-in (recebido)
    onNameChange: (String) -> Unit, // Callback para quando o nome muda
    onAmountChange: (String) -> Unit,// Callback para quando o valor muda
    onAddPlayer: () -> Unit        // Callback para quando o botão "Adicionar" é clicado
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Adicionar Novo Jogador", style = MaterialTheme.typography.titleLarge)
            // Campo de texto para o nome.
            OutlinedTextField(
                value = newPlayerName,
                onValueChange = onNameChange, // Chama o callback recebido.
                label = { Text("Nome do Jogador") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // Define a ação do teclado como "Next" (bom para formulários).
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            // Campo de texto para o valor do buy-in.
            OutlinedTextField(
                value = newBuyInAmount,
                onValueChange = onAmountChange, // Chama o callback recebido.
                // Usa substring(0,2) para pegar "R$" da formatação de 0.0
                label = { Text("Valor do Buy-in (${formatCurrency(0.0).substring(0,2)})") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, // Teclado numérico.
                    imeAction = ImeAction.Done // Ação "Done" (concluído).
                ),
                // Quando "Done" é pressionado no teclado, chama o callback onAddPlayer.
                keyboardActions = KeyboardActions(onDone = { onAddPlayer() }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Botão para adicionar o jogador.
            Button(
                onClick = onAddPlayer, // Chama o callback recebido.
                modifier = Modifier.align(Alignment.End), // Alinha o botão à direita.
                // O botão só é habilitado se o nome não estiver em branco E o valor for um número > 0.
                enabled = newPlayerName.isNotBlank() && newBuyInAmount.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Adicionar Jogador") }
        }
    }
}

// --- Seção Adicionar Buy-In ---
// Composable privado para a seção de adicionar buy-in/rebuy a um jogador existente.
@Composable
fun AddBuyInSection(
    players: List<Player>,             // Lista de jogadores para o dropdown
    selectedPlayer: Player?,           // Jogador atualmente selecionado (pode ser null)
    onPlayerSelected: (Player) -> Unit, // Callback quando um jogador é selecionado no dropdown
    buyInAmount: String,               // Estado do valor do buy-in adicional
    onAmountChange: (String) -> Unit,  // Callback para quando o valor muda
    onAddBuyIn: () -> Unit             // Callback para quando o botão "Adicionar Buy-In" é clicado
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Adicionar Buy-In / Rebuy", style = MaterialTheme.typography.titleLarge)
            // Dropdown customizado para selecionar o jogador.
            CustomDropdownMenu(
                items = players, // A lista de jogadores.
                selectedItem = selectedPlayer, // O item atualmente selecionado.
                onItemSelected = onPlayerSelected, // Callback chamado na seleção.
                label = "Selecionar Jogador", // Rótulo do dropdown.
                itemToString = { it.name }, // Como converter um objeto Player em String para exibição.
                enabled = players.isNotEmpty() // Desabilita o dropdown se não houver jogadores.
            )
            Spacer(Modifier.height(8.dp)) // Pequeno espaço.
            // Campo de texto para o valor adicional.
            OutlinedTextField(
                value = buyInAmount,
                onValueChange = onAmountChange,
                label = { Text("Valor Adicional (${formatCurrency(0.0).substring(0,2)})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddBuyIn() }), // Chama onAddBuyIn no "Done" do teclado.
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = players.isNotEmpty() // Desabilita se não houver jogadores (não faz sentido adicionar buy-in).
            )
            // Botão para adicionar o buy-in.
            Button(
                onClick = onAddBuyIn,
                modifier = Modifier.align(Alignment.End),
                // Habilitado somente se um jogador estiver selecionado E o valor for um número > 0.
                enabled = selectedPlayer != null && buyInAmount.toDoubleOrNull()?.let { it > 0 } == true
            ) { Text("Adicionar Buy-In") }
        }
    }
}

// --- Item da Lista de Jogadores ---
// Composable privado que representa um único item na lista de jogadores.
@Composable
fun PlayerListItem(
    player: Player,    // Os dados do jogador a serem exibidos
    onRemove: () -> Unit // Callback chamado quando o botão "Remover" deste item é clicado
) {
    // Card para dar um fundo e elevação ao item.
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Pequena sombra.
    ) {
        // Row para organizar o nome/buy-in e o botão horizontalmente.
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Padding interno.
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, // Alinha verticalmente no centro.
            horizontalArrangement = Arrangement.SpaceBetween // Espaço máximo entre o conteúdo e o botão.
        ) {
            // Column para agrupar o nome e o total de buy-in verticalmente.
            Column(modifier = Modifier.weight(1f)) { // 'weight(1f)' faz ocupar o espaço restante na Row.
                // Nome do jogador em negrito.
                Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                // Total de buy-in do jogador, com cor secundária.
                Text(
                    "Buy-in Total: ${formatCurrency(player.totalBuyIn)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Cor mais suave do tema.
                )
            }
            Spacer(Modifier.width(16.dp)) // Espaço entre as informações e o botão.
            // Botão de remover.
            Button(
                onClick = onRemove, // Chama o callback recebido quando clicado.
                // Cor de erro para indicar ação de remoção.
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remover")
            }
        }
    }
}

// --- Diálogo de Remoção ---
// Composable privado para o diálogo que pede a quantidade de fichas ao remover um jogador.
@Composable
fun RemovePlayerDialog(
    playerName: String,               // Nome do jogador sendo removido
    chipsInput: String,               // Estado atual do input de fichas
    onChipsInputChange: (String) -> Unit, // Callback quando o input de fichas muda
    onDismiss: () -> Unit,            // Callback quando o diálogo é dispensado (Cancelar/Fora)
    onConfirm: () -> Unit             // Callback quando o botão "Confirmar Remoção" é clicado
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Chamado ao clicar fora ou no botão de voltar.
        title = { Text("Remover ${playerName}?") }, // Título dinâmico com o nome do jogador.
        text = { // Conteúdo principal do diálogo.
            Column {
                Text("Quantas fichas ${playerName} está retirando da mesa?")
                Spacer(modifier = Modifier.height(16.dp))
                // Campo de texto para inserir o total de fichas na saída.
                OutlinedTextField(
                    value = chipsInput,
                    onValueChange = onChipsInputChange, // Atualiza o estado.
                    label = { Text("Total de Fichas na Saída") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm() }), // Confirma no "Done" do teclado.
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = { // Botão de confirmação.
            Button(
                onClick = onConfirm,
                // Habilitado apenas se o input de fichas for um número válido (toDoubleOrNull não retorna null).
                enabled = chipsInput.toDoubleOrNull() != null
            ) { Text("Confirmar Remoção") }
        },
        dismissButton = { // Botão de cancelar.
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// --- NOVO COMPOSABLE: Diálogo de Resultado do Cash-Out ---
@Composable
fun CashOutResultDialog(
    info: Pair<String, Double>, // Recebe o Par(Nome do Jogador, Valor do Cash-Out)
    onDismiss: () -> Unit       // Função para fechar o diálogo
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Permite fechar clicando fora
        title = { Text("Cash-Out Realizado") },
        text = {
            // Mostra o nome do jogador e o valor formatado em Reais
            Text("${info.first} recebeu ${formatCurrency(info.second)}.")
        },
        confirmButton = {
            // Botão simples para fechar o diálogo
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
        // Não precisamos de dismissButton explícito aqui, onDismissRequest já cobre
    )
}

// --- Item de Estatística ---
// Composable privado e genérico para exibir uma linha de estatística (Rótulo: Valor).
@Composable
fun StatItem(label: String, value: String) {
    // Row para alinhar rótulo e valor na mesma linha.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Espaço máximo entre rótulo e valor.
        verticalAlignment = Alignment.CenterVertically // Alinha verticalmente.
    ) {
        // Texto do rótulo.
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            // Opcional: weight pode ser usado para controlar o espaço, mas SpaceBetween geralmente basta.
            modifier = Modifier.weight(0.6f) // Dá 60% do espaço ao rótulo (ajustar conforme necessário)
        )
        // Texto do valor, com peso médio para destaque.
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            // Opcional: weight para o valor.
            // modifier = Modifier.weight(0.4f) // Dá 40% do espaço ao valor
        )
    }
}

// --- Dropdown Customizado ---
// Composable privado e genérico para criar um menu dropdown usando ExposedDropdownMenuBox do Material 3.
@OptIn(ExperimentalMaterial3Api::class) // Necessário para ExposedDropdownMenuBox
@Composable
fun <T> CustomDropdownMenu( // Tipo genérico 'T' para os itens.
    items: List<T>,                    // Lista de itens a serem exibidos.
    selectedItem: T?,                  // O item atualmente selecionado (pode ser null).
    onItemSelected: (T) -> Unit,       // Callback quando um item é selecionado.
    label: String,                     // Texto do rótulo do campo.
    itemToString: (T) -> String = { it.toString() }, // Função para converter um item 'T' em String para exibição (padrão é toString()).
    modifier: Modifier = Modifier,     // Modificador opcional.
    enabled: Boolean = true            // Se o dropdown está habilitado.
) {
    // Estado para controlar se o menu dropdown está expandido (aberto) ou não.
    var expanded by remember { mutableStateOf(false) }

    // Box que contém o TextField e o menu suspenso.
    ExposedDropdownMenuBox(
        expanded = expanded && enabled, // O menu só expande se 'expanded' for true E estiver habilitado.
        // Alterna o estado 'expanded' quando o box é clicado (se habilitado).
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier.fillMaxWidth() // Ocupa a largura por padrão.
    ) {
        // O TextField que exibe o item selecionado e serve como âncora para o menu.
        OutlinedTextField(
            // 'menuAnchor' identifica este TextField como a âncora para o ExposedDropdownMenu.
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true, // O usuário não pode digitar diretamente no TextField.
            // Exibe a string do item selecionado (usando itemToString) ou vazio se nada selecionado.
            value = selectedItem?.let(itemToString) ?: "",
            onValueChange = {}, // Não faz nada na mudança de valor (readOnly).
            label = { Text(label) }, // O rótulo do campo.
            // Ícone de seta padrão para indicar que é um dropdown.
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            // Cores padrão para o TextField dentro de um ExposedDropdownMenuBox.
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = enabled // Desabilita visualmente o TextField se 'enabled' for false.
        )
        // O menu suspenso que aparece abaixo do TextField.
        ExposedDropdownMenu(
            expanded = expanded && enabled, // Visível apenas se expandido e habilitado.
            onDismissRequest = { expanded = false } // Fecha o menu se clicar fora dele.
        ) {
            // Itera sobre a lista de itens.
            items.forEach { item ->
                // Cria um item de menu para cada item na lista.
                DropdownMenuItem(
                    // Texto do item (usando itemToString).
                    text = { Text(itemToString(item)) },
                    onClick = { // Chamado quando o item é clicado.
                        onItemSelected(item) // Chama o callback de seleção.
                        expanded = false // Fecha o menu.
                    }
                )
            }
            // Se a lista de itens estiver vazia, mostra um item indicando isso.
            if (items.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nenhum item disponível") },
                    onClick = { expanded = false }, // Fecha o menu.
                    enabled = false // Item desabilitado visualmente.
                )
            }
        }
    }
}