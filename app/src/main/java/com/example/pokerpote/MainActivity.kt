package com.example.pokerpote

// Imports do Android e Jetpack Compose
import android.os.Bundle // Para estado da Activity
import androidx.activity.ComponentActivity // Activity base para Compose
import androidx.activity.compose.setContent // Função para definir o conteúdo da UI com Compose
import androidx.compose.animation.AnimatedVisibility // Para mostrar/esconder elementos com animação
import androidx.compose.animation.core.tween // Define a duração e curva da animação
import androidx.compose.animation.fadeIn // Animação de fade in
import androidx.compose.animation.fadeOut // Animação de fade out
import androidx.compose.foundation.layout.* // Layouts como Column, Row, Spacer, padding, fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn // Lista rolável eficiente para muitos itens
import androidx.compose.foundation.lazy.items // Helper para adicionar itens a LazyColumn
import androidx.compose.foundation.rememberScrollState // Estado para lembrar a posição de rolagem de uma Column normal
import androidx.compose.foundation.text.KeyboardActions // Ações do teclado (como botão 'Done')
import androidx.compose.foundation.text.KeyboardOptions // Opções do teclado (tipo numérico, ação IME)
import androidx.compose.foundation.verticalScroll // Modificador para tornar uma Column rolável
import androidx.compose.material3.* // Componentes do Material Design 3 (Button, TextField, Card, Scaffold, etc.)
import androidx.compose.runtime.* // Funções do Compose Runtime (remember, mutableStateOf, Composable, LaunchedEffect, derivedStateOf)
import androidx.compose.ui.Alignment // Para alinhar elementos (CenterHorizontally, CenterVertically, etc.)
import androidx.compose.ui.Modifier // Modificador para alterar aparência e comportamento dos Composables
import androidx.compose.ui.platform.LocalFocusManager // Para controlar o foco (ex: esconder teclado)
import androidx.compose.ui.text.font.FontWeight // Peso da fonte (Bold, Medium, Normal)
import androidx.compose.ui.text.input.ImeAction // Ações do teclado (Next, Done, Send, etc.)
import androidx.compose.ui.text.input.KeyboardType // Tipo de teclado (Number, Text, Email, etc.)
import androidx.compose.ui.text.style.TextAlign // Alinhamento do texto (Center, Start, End)
import androidx.compose.ui.unit.dp // Unidade de densidade de pixels para tamanhos e espaçamentos
import androidx.compose.ui.unit.sp // Unidade de pixels escaláveis para tamanho de fonte
import com.example.pokerpote.ui.theme.PokerPotTheme // Importa o tema customizado do app
import kotlinx.coroutines.launch // Para iniciar coroutines (usado para Snackbar)
import java.text.NumberFormat // Para formatar números como moeda
import java.util.Locale // Para definir a localidade (Brasil) para formatação de moeda
import kotlin.math.roundToInt // Para arredondar Double para Int (usado em formatChips)
//import kotlin.reflect.KSuspendFunction1 // Não está sendo usado, pode ser removido
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontStyle
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong // <-- Mudar de roundToInt para roundToLong
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog // Usar BasicAlertDialog para mais controle ou manter AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api // Para BasicAlertDialog se usado


// Define os dois estados de tela possíveis para a navegação simples
enum class Screen {
    Setup, // Tela de configuração inicial
    Game   // Tela principal do jogo
}

// Activity principal do aplicativo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o conteúdo da Activity usando Jetpack Compose
        setContent {
            // Aplica o tema customizado 'PokerPotTheme' a toda a UI do app
            // dynamicColor = false (opcional) pode ser usado se preferir SEMPRE as cores definidas manualmente no tema
            PokerPotTheme {
                // Chama o Composable principal que gerencia a navegação e o estado do app
                PokerAppNavigation()
            }
        }
    }
}

// --- Composable Principal de Navegação e Gerenciamento de Estado ---
@Composable
fun PokerAppNavigation() {
    // --- Estados Principais ---
    // Estado para controlar qual tela (Setup ou Game) está ativa. Começa em Setup.
    // 'remember' garante que o estado sobreviva a recomposições.
    // 'mutableStateOf' cria um estado observável; mudanças nele causam recomposição.
    var currentScreen by remember { mutableStateOf(Screen.Setup) }
    // Estado para guardar a instância ativa do jogo. Começa como null (sem jogo ativo).
    var activePokerGame by remember { mutableStateOf<PokerGame?>(null) }
    // Estado necessário para controlar a exibição de Snackbars (mensagens temporárias na parte inferior).
    val snackbarHostState = remember { SnackbarHostState() }
    // Escopo de Coroutine ligado ao ciclo de vida deste Composable. Usado para lançar operações assíncronas (mostrar Snackbar).
    val scope = rememberCoroutineScope()

    // --- Funções Auxiliares ---
    // Função para mostrar Snackbars de forma centralizada e segura.
    fun showSnackbar(message: String) {
        // Lança a exibição do Snackbar em uma Coroutine separada (launch) para não bloquear a UI thread.
        scope.launch {
            // Opcional: Cancela qualquer Snackbar anterior para evitar filas. Descomente se necessário.
            // snackbarHostState.currentSnackbarData?.dismiss()
            // Mostra o Snackbar com a mensagem e duração definidas.
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short // Duração curta (pode ser Long ou Indefinite)
            )
        }
    }

    // --- Layout Estrutural ---
    // Scaffold provê a estrutura básica do Material Design (TopAppBar, BottomAppBar, FloatingActionButton, Drawer, SnackbarHost).
    Scaffold(
        // Define onde os Snackbars gerenciados por 'snackbarHostState' serão exibidos.
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding -> // 'innerPadding' contém os paddings aplicados pelo Scaffold (ex: se houver TopAppBar).
        // É CRUCIAL aplicá-lo ao container principal do conteúdo.

        // Surface é um container básico que aplica cor de fundo e elevação do tema.
        Surface(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo o espaço disponível na tela.
                .padding(innerPadding), // << IMPORTANTE: Aplica o padding do Scaffold aqui.
            color = MaterialTheme.colorScheme.background // Usa a cor de fundo definida no tema.
        ) {
            // Controla qual Composable (tela) exibir com base no estado 'currentScreen'.
            when (currentScreen) {
                // Se a tela atual for Setup...
                Screen.Setup -> {
                    // Exibe o Composable da tela de configuração.
                    SetupScreen(
                        snackbarHostState = snackbarHostState, // Passa o estado para que SetupScreen possa mostrar Snackbars.
                        // Define a ação a ser executada quando o jogo for iniciado no SetupScreen.
                        // Esta é uma função lambda (callback) passada como parâmetro.
                        onStartGame = {  multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate ->
                            // Linha de Debug para verificar os parâmetros recebidos.
                            println("DEBUG: Iniciando Jogo - MF: $multiplier, BuyIn: $initialBuyIn, BB Ini: $initialBB, StackDepth Ini: $initialStackDepth, Dinâmico: $dynamicUpdates, Taxa BB: $bbRate")
                            // Cria uma NOVA instância do jogo com os parâmetros configurados na tela Setup.
                            activePokerGame = PokerGame(
                                multiplier = multiplier,
                                initialMinBuyIn = initialBuyIn,
                                initialBB = initialBB,
                                initialStackDepth = initialStackDepth,
                                enableDynamicUpdates = dynamicUpdates,
                                // minBuyInUpdateRate REMOVIDO
                                bbUpdateRate = bbRate
                            )
                            // Muda o estado 'currentScreen', o que causará a recomposição e a exibição da tela do jogo.
                            currentScreen = Screen.Game
                        }
                    )
                }
                // Se a tela atual for Game...
                Screen.Game -> {
                    // Garante que só mostra a tela do jogo se 'activePokerGame' não for nulo (jogo foi iniciado).
                    // 'let' executa o bloco se activePokerGame não for null, passando a instância como 'game'.
                    activePokerGame?.let { game ->
                        // Exibe o Composable da tela principal do jogo.
                        PokerGameScreen(
                            pokerGame = game, // Passa a instância ativa do jogo para a tela.
                            snackbarHostState = snackbarHostState, // Passa o estado do Snackbar (pode ser útil).
                            showSnackbar = ::showSnackbar, // Passa a REFERÊNCIA da função showSnackbar definida acima.
                            // Define a ação (callback) para quando o usuário encerrar o jogo dentro de PokerGameScreen.
                            onEndGame = {
                                // Linha de Debug.
                                println("DEBUG: Encerrando Jogo.")
                                activePokerGame = null // Limpa a instância do jogo, liberando memória.
                                currentScreen = Screen.Setup // Muda o estado para voltar à tela de configuração.
                            }
                        )
                    } ?: run { // Bloco 'run' é executado se activePokerGame for null.
                        // Este cenário não deveria ocorrer com a lógica atual, mas é um fallback seguro.
                        // Usa LaunchedEffect para mudar o estado (navegar) de forma segura após a composição inicial.
                        // Mudar estado diretamente aqui poderia causar problemas. 'Unit' significa que executa só na primeira vez.
                        LaunchedEffect(Unit) {
                            println("ERRO: activePokerGame nulo na tela Game, voltando para Setup.")
                            currentScreen = Screen.Setup // Volta para Setup.
                            showSnackbar("Erro inesperado: Jogo não encontrado. Voltando para configuração.") // Informa o usuário.
                        }
                    }
                }
            }
        }
    }
}

// ==================================================
//         Tela de Configuração (SetupScreen)
// ==================================================


// ==================================================
//         Tela Principal do Jogo (PokerGameScreen)
// ==================================================



// ==================================================
//         Componentes Composable Auxiliares
//         (Usados principalmente por PokerGameScreen)
// ==================================================



// --- Seção de Estatísticas ---
// Composable privado que exibe as principais estatísticas do jogo em um Card.
