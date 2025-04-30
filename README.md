# Poker Pote

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue?logo=kotlin) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.x-4285F4?logo=jetpackcompose) ![Material 3](https://img.shields.io/badge/Material%203-Design-lightgrey?logo=materialdesign)

Um aplicativo Android simples, construído com Kotlin e Jetpack Compose, para auxiliar no gerenciamento de um jogo de poker estilo *cash game* caseiro. Ele ajuda a calcular o pote, controlar buy-ins, rebuys e cash-outs dos jogadores, além de opcionalmente ajustar o Big Blind (BB) e o buy-in mínimo dinamicamente.

## Funcionalidades Principais

* **Configuração da Mesa:**
    * Defina o Multiplicador (Fator de Conversão Dinheiro -> Fichas).
    * Defina o Buy-in Mínimo inicial.
    * Defina o Big Blind (BB) inicial em fichas.
    * Calcule e visualize a Profundidade do Stack inicial.
    * Opção para habilitar atualizações dinâmicas do BB e Buy-in Mínimo baseadas no andamento do jogo.
* **Gerenciamento do Jogo:**
    * Adicione novos jogadores com seu buy-in inicial (respeitando o mínimo).
    * Adicione buy-ins (rebuys) para jogadores existentes.
    * Remova jogadores da mesa, registrando a quantidade de fichas na saída para calcular o cash-out em dinheiro.
    * Exibe estatísticas atualizadas: Buy-in Mínimo atual, BB atual, Pote Total Estimado (fichas), Total de Buy-ins (R$) e Total de Cash-outs (R$).
    * Lista clara dos jogadores na mesa com seus respectivos totais de buy-in.
* **Interface Moderna:**
    * Construído com Jetpack Compose e seguindo as diretrizes do Material Design 3.
    * Suporte a tema claro e escuro (incluindo cores dinâmicas no Android 12+).

## Como Usar

1.  Faça o Download do app-PokerPote.apk e instale no seu dispositivo Android.

## Possíveis Melhorias Futuras

* Persistência de dados (salvar o estado do jogo se o app for fechado).
* Histórico de ações (buy-ins, cash-outs).
* Opções de timer/níveis de blinds para formato de torneio.
* Testes unitários e de UI.
* Tradução para outros idiomas.

---
