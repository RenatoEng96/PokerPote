// Este é o caminho onde está esse arquivo: com/example/pokerpote/data/GameStateRepository.kt
package com.example.pokerpote.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.pokerpote.PokerGame // Importa a classe PokerGame original
import com.example.pokerpote.datastore.GameStateProto // Importa a classe gerada pelo Protobuf
import com.example.pokerpote.datastore.PlayerProto
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// --- Define o nome do arquivo onde o DataStore salvará os dados ---
private const val DATA_STORE_FILE_NAME = "game_state.pb"

// --- Cria a instância do DataStore usando delegação ---
// 'context.dataStore' cria um singleton do DataStore para este contexto/nome de arquivo.
val Context.gameStateDataStore: DataStore<GameStateProto> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = GameStateSerializer // Usa o Serializer definido abaixo
)

// --- Serializer para GameStateProto ---
// Ensina o DataStore como ler e escrever o objeto GameStateProto.
object GameStateSerializer : Serializer<GameStateProto> {
    // Valor padrão retornado se não houver dados salvos ou ocorrer erro na leitura inicial.
    // Importante: 'is_game_active' deve ser false por padrão.
    override val defaultValue: GameStateProto = GameStateProto.newBuilder()
        .setIsGameActive(false)
        .build()

    // Lê dados do InputStream e transforma em GameStateProto.
    override suspend fun readFrom(input: InputStream): GameStateProto {
        try {
            // Usa o método parseFrom gerado pelo Protobuf.
            return GameStateProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            // Se os dados estiverem corrompidos/inválidos.
            throw CorruptionException("Cannot read proto.", exception)
        } catch (exception: IOException) {
            // Se houver erro de I/O ao ler o arquivo.
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    // Transforma o objeto GameStateProto e escreve no OutputStream.
    override suspend fun writeTo(t: GameStateProto, output: OutputStream) {
        try {
            // Usa o método writeTo gerado pelo Protobuf.
            t.writeTo(output)
        } catch(exception: IOException) {
            // Se houver erro de I/O ao escrever o arquivo.
            throw CorruptionException("Cannot write proto.", exception)
        }
    }
}

// --- Repositório (ou classe de acesso) para simplificar o uso ---
// Encapsula a lógica de acesso ao DataStore.
class GameStateRepository(private val dataStore: DataStore<GameStateProto>) {

    // Expõe o Flow dos dados. A UI pode observar este Flow para reagir a mudanças.
    val gameStateFlow: Flow<GameStateProto> = dataStore.data
        .catch { exception ->
            // Trata erros na leitura do Flow (exceto na leitura inicial tratada no serializer)
            if (exception is IOException) {
                // Log ou emite um estado de erro específico, se necessário
                println("Error reading GameState from DataStore: $exception")
                emit(GameStateSerializer.defaultValue) // Emite valor padrão em caso de erro
            } else {
                throw exception // Re-lança outras exceções
            }
        }

    // Função para SALVAR o estado completo do jogo.
    suspend fun saveGameState(pokerGame: PokerGame) {
        println("DEBUG: [Repository] Salvando estado do jogo...")
        dataStore.updateData { currentState ->
            // Cria um builder para o GameStateProto
            val builder = GameStateProto.newBuilder()
                .setIsGameActive(true) // MARCA COMO ATIVO
                .setMultiplier(pokerGame.multiplier)
                .setInitialMinBuyIn(pokerGame.getCurrentMinBuyIn()) // Salva o minimo inicial configurado
                .setInitialBb(pokerGame.getBB()) // Salva o BB minimo inicial configurado
                .setInitialStackDepth(0.0) // TODO: Precisa expor initialStackDepth em PokerGame ou salvar separadamente
                .setDynamicUpdatesEnabled(false) // TODO: Precisa expor dynamicUpdatesEnabled em PokerGame
                .setBbRate(0.0) // TODO: Precisa expor bbRate em PokerGame
                .setCurrentMinBuyIn(pokerGame.getCurrentMinBuyIn())
                .setCurrentBb(pokerGame.getBB())
                .setSumBuyIns(pokerGame.getSumBuyIns())
                .setCashOutTotal(pokerGame.getCashOutTotal())
            // .setEntriesCount(0) // TODO: Precisa expor entriesCount em PokerGame

            // Mapeia a lista de Players para PlayerProto e adiciona ao builder
            val playerProtos = pokerGame.getPlayers().map { player ->
                PlayerProto.newBuilder()
                    .setName(player.name)
                    .setTotalBuyIn(player.totalBuyIn)
                    .build()
            }
            builder.addAllPlayers(playerProtos)

            // Constrói e retorna o novo estado
            builder.build()
        }
        println("DEBUG: [Repository] Estado do jogo salvo.")
    }

    // Função para LIMPAR o estado salvo (quando o jogo termina).
    suspend fun clearGameState() {
        println("DEBUG: [Repository] Limpando estado do jogo salvo...")
        dataStore.updateData {
            // Retorna o estado padrão (com is_game_active = false)
            GameStateSerializer.defaultValue
        }
        println("DEBUG: [Repository] Estado do jogo limpo.")
    }

    // Função para LER o estado atual uma única vez (útil na inicialização)
    suspend fun loadInitialGameState(): GameStateProto? {
        return try {
            gameStateFlow.firstOrNull() // Pega o primeiro valor emitido ou null
        } catch (e: Exception) {
            println("Error loading initial game state: $e")
            null
        }
    }

    // TODO: Expor as propriedades que faltam em PokerGame
    // Para poder salvar/ler `initialStackDepth`, `dynamicUpdatesEnabled`, `bbRate`, `entriesCount`,
    // você precisará torná-las acessíveis na classe PokerGame (talvez através de getters ou
    // tornando as propriedades `internal` ou `public` se apropriado).
    // Alternativamente, passe esses valores para a função saveGameState.
}