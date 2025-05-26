# PokerPote - Gerenciador de Jogos de Poker

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue?logo=kotlin) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.x-4285F4?logo=jetpackcompose) ![Material 3](https://img.shields.io/badge/Material%203-Design-lightgrey?logo=materialdesign)

O PokerPote é um aplicativo Android desenvolvido para auxiliar jogadores e organizadores de poker a gerenciar com facilidade tanto partidas de **Cash Game** quanto **Torneios**. O app elimina a necessidade de planilhas complexas ou anotações manuais, centralizando todas as informações importantes em uma interface intuitiva e moderna.

<!-- Sugestão: Adicione um screenshot do seu app aqui.
     Exemplo: ![Imagem da tela principal do PokerPote](URL_DA_SUA_IMAGEM_AQUI) -->

## Como Usar

1.  Faça o Download do [app-PokerPote.apk](https://github.com/RenatoEng96/PokerPote/blob/Torneio/app-PokerPote.apk) e instale no seu dispositivo Android.

## Funcionalidades Principais

O aplicativo é dividido em dois modos principais, cada um com um conjunto completo de ferramentas.

### ♣️ Cash Game

O modo Cash Game é ideal para gerenciar jogos onde as fichas têm um valor monetário direto.

* **Configuração de Mesa Flexível**: Defina o multiplicador (quanto vale o dinheiro em fichas), o buy-in mínimo inicial e o valor do Big Blind.
* **Atualizações Dinâmicas**: Opcionalmente, configure o Big Blind e o buy-in mínimo para aumentarem de forma dinâmica com base no volume de dinheiro na mesa.
* **Gerenciamento de Jogadores**: Adicione novos jogadores, registre rebuys e remova jogadores da mesa, calculando automaticamente o valor do cash-out.
* **Estatísticas em Tempo Real**: Acompanhe o valor total do pote em fichas, a soma de todos os buy-ins e o total de cash-outs.
* **Persistência de Estado**: O estado de um cash game ativo é salvo automaticamente. Você pode fechar o app e continuar o jogo exatamente de onde parou.

### 🏆 Torneio

O modo Torneio oferece um conjunto robusto de ferramentas para organizar eventos do início ao fim.

* **Setup Detalhado**: Configure todos os aspectos do torneio:
  * **Valores**: Buy-in, contribuição para o prêmio e taxa (fee).
  * **Estruturas**: Crie estruturas de blinds e de premiação totalmente personalizadas, nível por nível e posição por posição.
  * **Regras**: Habilite e defina valores para Rebuy e Add-on. Defina até qual nível de blind o registro tardio e os rebuys são permitidos, e um nível limite separado para o add-on.
* **Dashboard de Torneio ao Vivo**:
  * **Cronômetro Regressivo**: Um relógio central marca o tempo restante para o próximo nível de blind.
  * **Alarme Sonoro**: Um alarme sonoro padrão do sistema é acionado a cada mudança de nível.
  * **Informações Detalhadas**: Exibição do nível de blind atual, valores de buy-in, taxas, e detalhes de rebuy/add-on.
  * **Gerenciamento Completo**: Pause/continue o torneio, adicione jogadores tardiamente (com validação de nome duplicado), realize rebuys, add-ons e elimine jogadores através de diálogos de confirmação para evitar erros.
* **Logística Automatizada**:
  * **Distribuição de Assentos**: Os jogadores são distribuídos aleatoriamente pelas mesas no início.
  * **Balanceamento de Mesas**: O app monitora a quantidade de jogadores por mesa (permitindo uma diferença de até 2 jogadores) e realiza o balanceamento automático para manter o jogo justo.
  * **Cálculo de Prêmios**: A premiação é calculada e atribuída automaticamente aos jogadores conforme são eliminados, com base na estrutura definida. A taxa (fee) é descontada dos valores de rebuy e add-on ao calcular o prêmio total.
  * **Média de Fichas**: Calculada considerando o total de fichas em jogo (iniciais + rebuys + addons) dividido pelos jogadores restantes.
* **Persistência de Torneio**: Assim como no Cash Game, um torneio em andamento é salvo para que possa ser continuado posteriormente.

## Tecnologias e Arquitetura

O PokerPote foi construído utilizando tecnologias modernas recomendadas para o desenvolvimento Android.

* **Linguagem**: 100% [**Kotlin**](https://kotlinlang.org/), aproveitando seus recursos de segurança e concisão.
* **Interface de Usuário**: [**Jetpack Compose**](https://developer.android.com/jetpack/compose), o framework moderno do Android para a construção de UIs declarativas.
* **Arquitetura**: O projeto segue uma abordagem baseada em **MVVM (Model-View-ViewModel)**.
  * **View (`Screens`)**: Composables que exibem o estado da UI e delegam eventos.
  * **ViewModel (`TournamentViewModel`, `PokerAppViewModel` / `CashGameViewModel`)**: Mantêm e gerenciam o estado da UI, contendo a lógica de negócios e se comunicando com os repositórios.
  * **Model (`Repository`, `Data Classes`)**: Representa os dados e a lógica de acesso a eles (ex: `Tournament`, `PokerGame`).
* **Navegação**: [**Jetpack Navigation Compose**](https://developer.android.com/jetpack/compose/navigation) para gerenciar o fluxo de navegação entre as diferentes telas do app.
* **Persistência de Dados**: [**Jetpack DataStore**](https://developer.android.com/topic/libraries/architecture/datastore) é utilizado para salvar o estado dos jogos de forma assíncrona e segura.
  * **Preferences DataStore**: Para salvar o estado do torneio (serializado em JSON).
  * **Proto DataStore**: Para salvar o estado do cash game de forma tipada e eficiente.
* **Assincronia**: [**Kotlin Coroutines**](https://kotlinlang.org/docs/coroutines-overview.html) são usadas extensivamente para operações de fundo, como salvar dados e gerenciar o cronômetro do torneio, sem bloquear a thread principal.
* **Gerenciamento de Som**: Utiliza o `MediaPlayer` do Android para tocar o som de notificação padrão do sistema.

## Sugestões de Melhorias Futuras

O PokerPote já é uma ferramenta poderosa, mas há sempre espaço para evoluir. Aqui estão algumas ideias para futuras versões:

* **Gerenciamento de Fichas em Torneio**: Implementar a capacidade de atualizar os *chip counts* dos jogadores durante o torneio. Isso permitiria exibir um ranking em tempo real e tomar decisões de "chip up" (remoção de fichas de menor valor).
* **Histórico de Jogos**: Salvar torneios e cash games finalizados em um histórico, permitindo consultar resultados, vencedores e outras estatísticas de partidas passadas.
* **Estatísticas de Jogadores**: Criar perfis de jogadores que persistem entre as partidas para rastrear estatísticas como lucro/prejuízo total, número de torneios jogados, ITM (In The Money), etc.
* **Pausas Programadas**: Na configuração da estrutura de blinds, permitir adicionar níveis de "Pausa" com duração definida, que seriam anunciados pelo cronômetro.
* **Temas e Personalização**: Oferecer mais opções de temas (ex: um tema escuro dedicado) e permitir que o usuário personalize cores ou sons (além do alarme padrão).
* **Internacionalização (i18n)**: Extrair todos os textos para arquivos de `strings.xml` para facilitar a tradução para outros idiomas, como inglês e espanhol.
* **Backup na Nuvem**: Integrar com o Google Drive ou Firebase para permitir que os usuários façam backup e restaurem seus dados de jogos e torneios entre diferentes dispositivos.
* **Testes**: Escrever testes unitários para a lógica de negócios (em `PokerGame`, `TournamentManager`, etc.) e testes de instrumentação para a UI, garantindo a estabilidade do app a cada nova funcionalidade.
* **Melhorias na Interface de Balanceamento**: Visualmente indicar quando o balanceamento de mesas está ocorrendo ou permitir um acionamento manual, se desejado.
* **Importação/Exportação de Estruturas**: Permitir que os usuários salvem e carreguem estruturas de blinds e premiação pré-definidas.
* **Calculadora de Potes/Odds**: Integrar uma ferramenta simples para ajudar os jogadores com cálculos comuns durante o jogo.
