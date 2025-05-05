// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/PokerGame.kt
package com.example.pokerpote
import kotlin.math.max // Importa função para encontrar o valor máximo entre dois números
import kotlin.math.roundToInt // Importa função para arredondar Double para Int
import com.example.pokerpote.datastore.GameStateProto // Importe o Proto
import com.example.pokerpote.datastore.PlayerProto


/**
 * Gerencia o estado e a lógica de um jogo de poker (cash game).
 * Calcula buy-ins, pote, Big Blind (BB), e gerencia a lista de jogadores.
 * Pode opcionalmente atualizar o BB e o buy-in mínimo dinamicamente com base no estado do jogo.
 *
 * @property multiplier O fator pelo qual o valor em dinheiro (buy-in) é convertido em fichas.
 * @param initialMinBuyIn O valor mínimo de buy-in inicial permitido em dinheiro.
 * @param enableDynamicUpdates Define se o Big Blind (BB) e o Buy-in Mínimo devem ser atualizados dinamicamente.
 * @param initialStackDepth A profundidade do stack calculada na configuração inicial.
 * @param bbUpdateRate A taxa (decimal, ex: 0.001 para 0.1%) usada para calcular o novo Big Blind com base no pote total em fichas, se `enableDynamicUpdates` for true.
 */
class PokerGame(
    val multiplier: Double, // Fator de conversão dinheiro -> fichas (público e imutável)
    private val initialMinBuyIn: Double, // Buy-in mínimo inicial (privado e imutável)
    initialBB: Int, // BB mínimo inicial
    initialStackDepth: Double,
    enableDynamicUpdates: Boolean, // Flag recebida para habilitar/desabilitar atualizações
    // Taxas para atualizações dinâmicas (recebidas no construtor)
    //minBuyInUpdateRate: Double,
    bbUpdateRate: Double
) {
    // Armazena a configuração de atualizações dinâmicas (privado e imutável)
    private val dynamicUpdatesEnabled: Boolean = enableDynamicUpdates
    // Armazena as taxas de atualização (privado e imutável)
    //private val minBuyInRate: Double = minBuyInUpdateRate
    private val bbRate: Double = bbUpdateRate
    private val minimumBB: Int = initialBB
    private val stackDepth: Double = initialStackDepth

    // Lista mutável interna para armazenar os jogadores ativos na mesa
    private val players = mutableListOf<Player>()
    // Soma total de todos os buy-ins (dinheiro) que entraram na mesa
    private var sumBuyIns: Double = 0.0
    // Soma total de todos os cash-outs (dinheiro) que saíram da mesa
    private var cashOutTotal: Double = 0.0
    // Contador de "entradas" baseado no buy-in mínimo, usado para a regra de atualização dinâmica
    // TODO: Adicionar comentário explicando a regra exata por trás deste contador e do limite 9.
    private var entriesCount: Int = 0
    // O valor atual do buy-in mínimo permitido (pode mudar se atualizações dinâmicas estiverem ativas)
    private var currentMinBuyIn: Double = initialMinBuyIn
    // O valor atual do Big Blind (BB) em FICHAS (pode mudar se atualizações dinâmicas estiverem ativas)
    private var bb: Int = initialBB // Valor inicial padrão, será recalculado no init

    // --- Construtor Secundário para carregar do DataStore ---
    constructor(savedState: GameStateProto) : this(
        // Recria a classe usando os dados do proto
        multiplier = savedState.multiplier,
        initialMinBuyIn = savedState.initialMinBuyIn, // Usa o mínimo inicial salvo
        initialBB = savedState.initialBb, // Usa o BB inicial salvo
        initialStackDepth = savedState.initialStackDepth, // Usa o stack depth salvo
        enableDynamicUpdates = savedState.dynamicUpdatesEnabled, // Usa a config dinâmica salva
        bbUpdateRate = savedState.bbRate // Usa a taxa salva
    ) {
        println("DEBUG: [PokerGame] Recriando jogo a partir do estado salvo...")
        // --- Sobrescreve o estado INTERNO com os valores ATUAIS salvos ---
        this.currentMinBuyIn = savedState.currentMinBuyIn
        this.bb = savedState.currentBb
        this.sumBuyIns = savedState.sumBuyIns
        this.cashOutTotal = savedState.cashOutTotal
        this.entriesCount = savedState.entriesCount

        // Limpa a lista de jogadores (caso o construtor primário adicione algo)
        // e popula com os jogadores salvos.
        this.players.clear()
        this.players.addAll(savedState.playersList.map { protoPlayer ->
            Player(name = protoPlayer.name, totalBuyIn = protoPlayer.totalBuyIn)
        })
        println("DEBUG: [PokerGame] Jogo recriado. ${players.size} jogadores carregados.")
    }

    // Bloco init original (será chamado por AMBOS os construtores ANTES do corpo do construtor secundário)
    init {
        // A inicialização do BB agora deve considerar se veio do construtor primário ou secundário.
        // O construtor secundário sobrescreverá 'this.bb' logo após o init.
        // Poderia refatorar para evitar a dupla definição, mas sobrescrever funciona.

        println("DEBUG: [PokerGame] Bloco Init executado. BB inicializado com: ${this.bb}") // Log para verificar

        // Garante que as propriedades adicionadas sejam inicializadas
        // (Já feito pela passagem de parâmetros no 'this(...)')
        // this.dynamicUpdatesEnabled = enableDynamicUpdates
        // this.bbRate = bbUpdateRate
        // this.minimumBB = initialBB
        // this.stackDepth = initialStackDepth
    }

    /**
     * Adiciona um novo jogador à mesa.
     * @param name O nome do novo jogador.
     * @param buyInAmount O valor em dinheiro do buy-in inicial do jogador.
     * @throws IllegalArgumentException Se o buyInAmount for menor que o `currentMinBuyIn`.
     */
    fun addPlayer(name: String, buyInAmount: Double) {
        // 1. Limpa espaços em branco do nome
        val trimmedName = name.trim()

        // --- CORREÇÃO INSERIDA AQUI ---
        // 2. Verifica se já existe um jogador com esse nome (ignorando case)
        //    'any' retorna true se algum elemento na lista 'players' satisfizer a condição.
        //    'it' representa cada 'Player' na lista durante a iteração.
        //    'equals(trimmedName, ignoreCase = true)' compara os nomes sem diferenciar maiúsculas/minúsculas.
        if (players.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            // 3. Se encontrar um nome igual, lança a exceção com a mensagem específica.
            throw IllegalArgumentException("Jogador já cadastrado, utilize o Rebuy!")
        }

        // Valida se o buy-in é suficiente
        validateBuyIn(buyInAmount)
        // Atualiza o contador de entradas se ainda não atingiu o limite (regra específica do jogo)
        if (entriesCount < 9) {
            // Calcula quantas "unidades de buy-in mínimo" este buy-in representa
            entriesCount += (buyInAmount / initialMinBuyIn).toInt()
        }
        // Adiciona o valor ao total de buy-ins
        sumBuyIns += buyInAmount
        // Cria e adiciona o novo jogador à lista
        players.add(Player(name.trim(), buyInAmount)) // trim() remove espaços extras do nome
        // Atualiza o buy-in mínimo e o BB (se as condições forem atendidas e estiverem habilitados)
        updateBB()
        updateMinBuyInIfNeeded()

    }

    /**
     * Adiciona um buy-in (ou rebuy) para um jogador existente.
     * @param player O jogador que está fazendo o buy-in adicional.
     * @param buyInAmount O valor em dinheiro do buy-in adicional.
     * @throws IllegalArgumentException Se o buyInAmount for menor que o `currentMinBuyIn`.
     */
    fun addBuyIn(player: Player, buyInAmount: Double) {
        // Valida se o buy-in é suficiente
        validateBuyIn(buyInAmount)
        // Atualiza o contador de entradas (mesma lógica do addPlayer)
        if (entriesCount < 9) {
            entriesCount += (buyInAmount / initialMinBuyIn).toInt()
        }
        // Adiciona o valor ao total de buy-ins
        sumBuyIns += buyInAmount
        // Adiciona o valor ao total de buy-in do jogador específico
        player.totalBuyIn += buyInAmount
        // Atualiza o buy-in mínimo e o BB (se as condições forem atendidas e estiverem habilitados)
        updateBB()
        updateMinBuyInIfNeeded()

    }

    /**
     * Remove um jogador da mesa e registra o valor do cash-out.
     * @param player O jogador a ser removido.
     * @param chips O número de FICHAS que o jogador está retirando da mesa.
     * @return O valor em dinheiro (Reais) correspondente ao cash-out.
     */
    fun removePlayer(player: Player, chips: Double): Double {
        println("DEBUG: [GAME] Iniciando removePlayer para ${player.name} com $chips fichas.")
        var cashOutAmount = 0.0 // Inicializa
        val removedSuccessfully: Boolean // Para checar se a remoção da lista funcionou

        try {
            println("DEBUG: [GAME] Calculando cashOutAmount (MF: $multiplier)...")
            cashOutAmount = if (multiplier != 0.0) chips / multiplier else 0.0
            println("DEBUG: [GAME] cashOutAmount calculado: $cashOutAmount")
            cashOutTotal += cashOutAmount
            println("DEBUG: [GAME] Removendo ${player.name} da lista (Tamanho atual: ${players.size})...")
            // --- Log mais detalhado da remoção ---
            removedSuccessfully = players.remove(player)
            println("DEBUG: [GAME] Jogador ${player.name} removido da lista: $removedSuccessfully. Novo tamanho: ${players.size}")
            if (!removedSuccessfully) {
                // Isso não deveria acontecer se a lógica estiver correta, mas é bom logar
                println("WARN: [GAME] Jogador ${player.name} NÃO foi encontrado na lista 'players' para remoção!")
            }
            // --- Fim do log detalhado ---
        } catch (e: Exception) {
            println("ERROR: [GAME] Exceção durante cálculo/remoção em removePlayer para ${player.name}: ${e::class.simpleName}")
            e.printStackTrace()
            throw e // Re-lança a exceção para ser pega pela UI
        }

        // Chama as funções de atualização após a remoção
        try {
            println("DEBUG: [GAME] Chamando updateBB...")
            updateBB()
            println("DEBUG: [GAME] updateBB concluído. Novo BB: $bb")
            println("DEBUG: [GAME] Chamando updateMinBuyInIfNeeded...")
            updateMinBuyInIfNeeded()
            println("DEBUG: [GAME] updateMinBuyInIfNeeded concluído. Novo MinBuyIn: $currentMinBuyIn")
        } catch (e: Exception) {
            println("ERROR: [GAME] Exceção durante updates (BB/BuyIn) em removePlayer para ${player.name}: ${e::class.simpleName}")
            e.printStackTrace()
            throw e // Re-lança a exceção
        }

        println("DEBUG: [GAME] removePlayer concluído para ${player.name}. Retornando cashOutAmount: $cashOutAmount")
        return cashOutAmount

//        // Calcula o valor em dinheiro correspondente às fichas retiradas
//        val cashOutAmount = if (multiplier != 0.0) chips / multiplier else 0.0 // Evita divisão por zero
//        // Adiciona ao total de cash-outs
//        cashOutTotal += cashOutAmount
//        // Remove o jogador da lista
//        players.remove(player)
//        // Atualiza o buy-in mínimo e o BB (se as condições forem atendidas e estiverem habilitados)
//        updateBB()
//        updateMinBuyInIfNeeded()
//        return cashOutAmount

    }

    /**
     * Valida se o valor do buy-in é permitido (maior ou igual ao mínimo atual).
     * @param buyInAmount O valor do buy-in a ser validado.
     * @throws IllegalArgumentException Se a validação falhar.
     */
    private fun validateBuyIn(buyInAmount: Double) {
        // Compara o valor do buy-in com o mínimo ATUAL (que pode ter sido atualizado dinamicamente)
        if (buyInAmount < currentMinBuyIn) {
            // Lança uma exceção se o valor for insuficiente, a mensagem será mostrada na UI.
            throw IllegalArgumentException("Buy-in mínimo é ${formatCurrencyGame(currentMinBuyIn)}.")
        }
    }

    /**
     * Atualiza o `currentMinBuyIn` com base no pote líquido (BuyIns - CashOuts)
     * e na taxa `minBuyInRate`, mas SOMENTE SE:
     * 1. As atualizações dinâmicas estiverem habilitadas (`dynamicUpdatesEnabled`).
     * 2. O contador `entriesCount` tiver atingido o limite (9 neste caso).
     * Garante que o novo mínimo nunca seja menor que o `initialMinBuyIn`.
     * Arredonda o valor calculado para baixo, para a dezena mais próxima.
     */
    private fun updateMinBuyInIfNeeded() {
        // Verifica se as atualizações dinâmicas estão habilitadas E se o contador atingiu o limite
        if (dynamicUpdatesEnabled && entriesCount >= 9) {
            // Fórmula: (Profundidade Stack * BB Atual) / Multiplicador
            // Garante que multiplier não seja zero para evitar divisão por zero
            val calculatedMinRaw = if (multiplier != 0.0) {
                (stackDepth * bb) / multiplier
            } else {
                0.0 // Ou algum outro tratamento de erro se MF=0 for possível/inválido
            }
            // Arredonda para baixo para a dezena mais próxima (mantendo a lógica anterior)
            val calculatedMinRounded = max(0.0, (calculatedMinRaw / 10.0).toInt() * 10.0)
            // Garante que o mínimo NÃO seja menor que o initialMinBuyIn definido na configuração
            currentMinBuyIn = max(initialMinBuyIn, calculatedMinRounded)
            // Calcula o valor líquido que entrou na mesa
//            val netBuyIns = sumBuyIns - cashOutTotal
//            // Calcula o valor "bruto" do novo mínimo usando a taxa definida
//            val rawMin = minBuyInRate * netBuyIns
//            // Arredonda para baixo para a dezena mais próxima (ex: 23.5 -> 20, 48.9 -> 40)
//            // Divide por 10, converte para Int (truncando) e multiplica por 10.
//            // Usa max(0.0, ...) para garantir que não seja negativo se cashOuts > buyIns (improvável)
//            val calculatedMin = max(0.0, (rawMin / 10.0).toInt() * 10.0)
//            // Define o novo mínimo, garantindo que ele nunca seja menor que o valor inicial configurado
//            currentMinBuyIn = max(initialMinBuyIn, calculatedMin)
        }
        // Se as condições não forem atendidas, o currentMinBuyIn não é alterado.
        // Se entriesCount < 9 ou dynamicUpdatesEnabled == false, ele permanece como initialMinBuyIn ou o último valor calculado.
    }

    /**
     * Atualiza o valor do Big Blind (BB) em fichas.
     * A lógica de cálculo só é executada se as atualizações dinâmicas estiverem habilitadas.
     */
    private fun updateBB() {
        // Se as atualizações dinâmicas estiverem DESABILITADAS, simplesmente retorna.
        // O valor do BB permanecerá o que foi calculado no init ou na última atualização válida.
        if (!dynamicUpdatesEnabled) {
            return
        }
        // Se chegou aqui, as atualizações estão habilitadas, então executa a lógica real de cálculo.
        updateBBInternalLogic()
    }

    /**
     * Contém a lógica real de cálculo do BB.
     * Chamado pelo `init` e por `updateBB` (se as atualizações dinâmicas estiverem habilitadas).
     * Calcula o BB com base no pote total em FICHAS e na taxa `bbRate`,
     * aplicando regras de arredondamento e valor mínimo.
     */
    private fun updateBBInternalLogic() {
        // Calcula o pote total atual em FICHAS (usa o getter que já aplica o multiplicador)
        val totalPotInChips = getTotalPot()

        // Calcula o BB "bruto" multiplicando o pote em fichas pela taxa de BB definida.
        // Converte para Int, o que trunca (remove) a parte decimal.
        var rawBB = (totalPotInChips * bbRate).toInt()

        // Aplica as regras de arredondamento e mínimo específicas do jogo:
        // 1. Mínimo de 2: Se for menor que 2, ajusta para 2.
        // 2. Entre 2 e 5: Arredonda para baixo para o par mais próximo (se for ímpar, subtrai 1).
        // 3. Maior ou igual a 5: Arredonda para baixo para o múltiplo de 5 mais próximo.
        bb = when {
            // Caso 1: BB calculado < 2 --> BB = 2
            rawBB < minimumBB -> minimumBB

            // Caso 2: 2 <= BB calculado < 5
            rawBB < 5 -> if (rawBB % 2 != 0) rawBB - 1 else rawBB // Se for ímpar (3), vira 2. Se for par (2, 4), mantém.

            // Caso 3: BB calculado >= 5
            else -> rawBB - (rawBB % 5) // Subtrai o resto da divisão por 5 (ex: 17 -> 17 - 2 = 15; 24 -> 24 - 4 = 20)
        }

        // --- AJUSTE ADICIONAL PÓS-ARREDONDAMENTO ---
        // Garante que mesmo após os arredondamentos (para par ou múltiplo de 5),
        // o BB final não seja menor que o mínimo estabelecido.
        // Ex: Se minimumBB for 3, e rawBB for 3, o caso 2 o tornaria 2. Este ajuste corrige para 3.
        // Ex: Se minimumBB for 7, e rawBB for 8, o caso 3 o tornaria 5. Este ajuste corrige para 7.
        if (bb < minimumBB) {
            bb = minimumBB
        }
        // Nota: O arredondamento para par entre minimumBB e 5 pode ser complexo
        // se minimumBB for ímpar (ex: 3). A regra "mantém par" pode precisar
        // de reavaliação dependendo do comportamento exato desejado nesse intervalo.
        // A lógica atual prioriza o arredondamento e depois garante o mínimo.
        // Se minimumBB for 3, e rawBB for 4, bb será 4. Se rawBB for 3, bb será 3 (pelo ajuste final).
    }


    // --- Getters Públicos ---
    // Fornecem acesso seguro (somente leitura) ao estado interno do jogo para a UI.

    /** Retorna o valor atual do buy-in mínimo permitido. */
    fun getCurrentMinBuyIn(): Double = currentMinBuyIn

    /** Retorna o valor atual do Big Blind (BB) em fichas. */
    fun getBB(): Int = bb

    /** Retorna uma cópia da lista de jogadores atuais (imutável para quem chama). */
    fun getPlayers(): List<Player> = players.toList() // .toList() cria uma cópia

    /** Retorna a soma total de todos os buy-ins em dinheiro. */
    fun getSumBuyIns(): Double = sumBuyIns

    /** Retorna a soma total de todos os cash-outs em dinheiro. */
    fun getCashOutTotal(): Double = cashOutTotal

    /**
     * Retorna o valor estimado do pote total atual em FICHAS.
     * Calculado como: (Total Buy-ins - Total Cash-outs) * Multiplicador.
     * Garante que o resultado não seja negativo.
     */
    fun getTotalPot(): Double = max(0.0, (sumBuyIns - cashOutTotal)) * multiplier

    // --- Getters Adicionais (ou tornar propriedades acessíveis) ---
    // Necessários para o GameStateRepository.saveGameState
    // Alternativa: Tornar as propriedades internas (internal) ou públicas

    fun getInitialMinBuyIn(): Double = initialMinBuyIn // Exemplo, se initialMinBuyIn fosse privada
    fun getInitialBB(): Int = minimumBB
    fun getInitialStackDepth(): Double = stackDepth
    fun areDynamicUpdatesEnabled(): Boolean = dynamicUpdatesEnabled
    fun getBBRate(): Double = bbRate
    fun getEntriesCount(): Int = entriesCount

    /**
     * Formata um valor Double como moeda (R$) arredondado para o inteiro mais próximo.
     * Função auxiliar interna.
     * SUGESTÃO: Mover a lógica de formatação para a camada de UI.
     */
    private fun formatCurrencyGame(value: Double): String {
        // Arredonda o valor para o inteiro mais próximo e adiciona "R$"
        return "R$${value.roundToInt()}"
    }
}