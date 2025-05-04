// Este é o caminho onde está esse arquivo: app/src/main/com/example/pokerpote/MainActivity.kt
package com.example.pokerpote

// Imports do Android e Jetpack Compose
import android.os.Bundle // Para estado da Activity
import androidx.activity.ComponentActivity // Activity base para Compose
import androidx.activity.compose.setContent // Função para definir o conteúdo da UI com Compose
import androidx.compose.foundation.layout.* // Layouts como Column, Row, Spacer, padding, fillMaxSize
import androidx.compose.material3.* // Componentes do Material Design 3 (Button, TextField, Card, Scaffold, etc.)
import androidx.compose.runtime.* // Funções do Compose Runtime (remember, mutableStateOf, Composable, LaunchedEffect, derivedStateOf)
import androidx.compose.ui.Alignment // Para alinhar elementos (CenterHorizontally, CenterVertically, etc.)
import androidx.compose.ui.Modifier // Modificador para alterar aparência e comportamento dos Composables
import com.example.pokerpote.ui.theme.PokerPotTheme // Importa o tema customizado do app
import kotlinx.coroutines.launch // Para iniciar coroutines (usado para Snackbar)
//import kotlin.reflect.KSuspendFunction1 // Não está sendo usado, pode ser removido
import androidx.compose.runtime.LaunchedEffect // Import necessário
import androidx.compose.runtime.rememberCoroutineScope // Import necessário
import androidx.compose.ui.platform.LocalContext // Import necessário
import com.example.pokerpote.data.GameStateRepository // Import Repository
import com.example.pokerpote.data.gameStateDataStore // Import DataStore instance access
import androidx.lifecycle.viewmodel.compose.viewModel // Para um ViewModel (opcional) ********
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope


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

// --- (OPCIONAL, MAS RECOMENDADO) ViewModel para Gerenciar Estado e Repositório ---
// Isso desacopla a lógica de dados da UI
class PokerAppViewModel(private val repository: GameStateRepository) : ViewModel() {

    // Estado observável para a instância do jogo
    var activePokerGame by mutableStateOf<PokerGame?>(null)
        private set // Só pode ser alterado dentro do ViewModel

    // Estado observável para a tela atual
    var currentScreen by mutableStateOf(Screen.Setup) // Começa em Setup por padrão
        private set

    // Flag para indicar se o carregamento inicial terminou
    var isLoading by mutableStateOf(true)
        private set

    init {
        loadInitialGame()
    }

    // Carrega o estado inicial do jogo do repositório
    private fun loadInitialGame() {
        viewModelScope.launch {
            println("DEBUG: [ViewModel] Iniciando carregamento do estado inicial...")
            val loadedState = repository.loadInitialGameState()
            if (loadedState != null && loadedState.isGameActive) {
                println("DEBUG: [ViewModel] Jogo ativo encontrado no DataStore. Recriando...")
                activePokerGame = PokerGame(loadedState) // Recria usando o construtor secundário
                currentScreen = Screen.Game // Define a tela do jogo
                println("DEBUG: [ViewModel] Jogo recriado. Navegando para Screen.Game.")
            } else {
                println("DEBUG: [ViewModel] Nenhum jogo ativo encontrado ou estado inválido. Iniciando em Screen.Setup.")
                activePokerGame = null
                currentScreen = Screen.Setup // Garante que começa no Setup
            }
            isLoading = false // Marca o carregamento como concluído
            println("DEBUG: [ViewModel] Carregamento inicial concluído. isLoading: $isLoading")
        }
    }

    // Inicia um NOVO jogo (chamado pelo SetupScreen)
    fun startGame(multiplier: Double, initialBuyIn: Double, initialBB: Int, initialStackDepth: Double, dynamicUpdates: Boolean, bbRate: Double) {
        println("DEBUG: [ViewModel] Iniciando NOVO jogo...")
        val newGame = PokerGame(
            multiplier = multiplier,
            initialMinBuyIn = initialBuyIn,
            initialBB = initialBB,
            initialStackDepth = initialStackDepth,
            enableDynamicUpdates = dynamicUpdates,
            bbUpdateRate = bbRate
        )
        activePokerGame = newGame
        currentScreen = Screen.Game
        // Salva o estado inicial do novo jogo
        viewModelScope.launch {
            repository.saveGameState(newGame)
            println("DEBUG: [ViewModel] Estado do NOVO jogo salvo.")
        }
    }

    // Encerra o jogo ATUAL (chamado pelo GameScreen)
    fun endGame() {
        println("DEBUG: [ViewModel] Encerrando jogo...")
        val gameToEnd = activePokerGame // Pega referência antes de limpar
        activePokerGame = null
        currentScreen = Screen.Setup
        // Limpa o estado salvo no DataStore
        if (gameToEnd != null) { // Só limpa se realmente havia um jogo
            viewModelScope.launch {
                repository.clearGameState()
                println("DEBUG: [ViewModel] Estado do jogo limpo no DataStore.")
            }
        }
    }

    // Salva o estado ATUAL do jogo (chamado após ações em GameScreen)
    fun saveCurrentGame() {
        activePokerGame?.let { game ->
            println("DEBUG: [ViewModel] Salvando estado atual do jogo...")
            viewModelScope.launch {
                repository.saveGameState(game)
                println("DEBUG: [ViewModel] Estado ATUAL do jogo salvo.")
            }
        }
    }
}

// --- Factory para criar o ViewModel com o Repositório ---
// Necessário porque o ViewModel tem dependência (repository) no construtor
class PokerAppViewModelFactory(private val repository: GameStateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokerAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokerAppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- Composable Principal de Navegação e Gerenciamento de Estado ---
@Composable
fun PokerAppNavigation() {
    // --- Obter Contexto e Criar Repositório/ViewModel ---
    val context = LocalContext.current
    // Cria a instância do repositório usando o DataStore do contexto
    val repository = remember { GameStateRepository(context.gameStateDataStore) }
    // Cria o ViewModel usando a Factory
    val viewModel: PokerAppViewModel = viewModel(
        factory = PokerAppViewModelFactory(repository)
    )

    // --- Estados observados do ViewModel ---
    val currentScreen = viewModel.currentScreen
    val activePokerGame = viewModel.activePokerGame
    val isLoading = viewModel.isLoading // Pega o estado de loading

    // --- Snackbar (semelhante ao original) ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    // --- Exibir um indicador de carregamento enquanto o estado inicial é lido ---
    if (isLoading) {
        println("DEBUG: [Navigation] Exibindo tela de Loading...")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Mostra um spinner de progresso
        }
    } else {
        println("DEBUG: [Navigation] Carregamento concluído. Exibindo tela: $currentScreen")
        // --- Layout Estrutural (semelhante ao original) ---
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                // Controla qual tela exibir com base no estado do ViewModel
                when (currentScreen) {
                    Screen.Setup -> {
                        SetupScreen(
                            snackbarHostState = snackbarHostState,
                            // Chama a função do ViewModel para iniciar o jogo
                            onStartGame = { multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate ->
                                viewModel.startGame(multiplier, initialBuyIn, initialBB, initialStackDepth, dynamicUpdates, bbRate)
                            }
                        )
                    }
                    Screen.Game -> {
                        // Garante que só mostra a tela do jogo se 'activePokerGame' não for nulo
                        activePokerGame?.let { game ->
                            PokerGameScreen(
                                pokerGame = game,
                                snackbarHostState = snackbarHostState,
                                showSnackbar = ::showSnackbar,
                                // Chama a função do ViewModel para encerrar o jogo
                                onEndGame = { viewModel.endGame() },
                                // --- NOVO: Passa callback para salvar o jogo ---
                                onGameUpdated = { viewModel.saveCurrentGame() } // Chama o save do ViewModel
                            )
                        } ?: run {
                            // Fallback se algo der errado (jogo nulo na tela Game)
                            LaunchedEffect(Unit) {
                                println("ERRO: activePokerGame nulo na tela Game (ViewModel), voltando para Setup.")
                                viewModel.endGame() // Tenta limpar e voltar ao Setup de forma segura
                                showSnackbar("Erro inesperado: Jogo não encontrado.")
                            }
                        }
                    }
                }
            }
        }
    } // Fim do else (isLoading)
}

