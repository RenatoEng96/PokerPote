// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/ui/theme/Theme.kt
package com.example.pokerpote.ui.theme

import android.os.Build // Para verificar a versão do Android (necessário para cores dinâmicas)
import androidx.compose.foundation.isSystemInDarkTheme // Para detectar se o sistema está em modo escuro
import androidx.compose.material3.MaterialTheme // O provedor principal do tema Material Design 3
import androidx.compose.material3.darkColorScheme // Função para criar um esquema de cores escuras
import androidx.compose.material3.dynamicDarkColorScheme // Função para criar esquema escuro dinâmico (Android 12+)
import androidx.compose.material3.dynamicLightColorScheme // Função para criar esquema claro dinâmico (Android 12+)
import androidx.compose.material3.lightColorScheme // Função para criar um esquema de cores claras
import androidx.compose.runtime.Composable // Annotation para funções Composable
import androidx.compose.ui.graphics.Color // Classe de Cor do Compose
import androidx.compose.ui.platform.LocalContext // Para obter o Contexto do Android (necessário para cores dinâmicas)

// Esquema de cores MANUALMENTE definido para o tema escuro (Dark Mode)
// Usa as cores definidas em Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,         // Cor principal (botões, elementos ativos)
    secondary = PurpleGrey80,   // Cor secundária (elementos menos proeminentes)
    tertiary = Pink80,          // Cor terciária (destaques, alguns botões) - ATENÇÃO: É VERDE
    // Você pode customizar outras cores se necessário:
    background = Color(0xFF1C1B1F),      // Cor de fundo principal da tela
    surface = Color(0xFF2C2B2F),         // Cor da superfície de componentes como Cards, Menus
    error = Color(0xFFF2B8B5),           // Cor para indicar erros (ex: texto de erro, borda de TextField)
    onError = Color(0xFF56110E),         // Cor do conteúdo (texto/ícones) sobre a cor 'error'
    surfaceVariant = Color(0xFF49454F),  // Cor de superfície variante (outros fundos, divisores)
    onSurfaceVariant = Color(0xFFCAC4D0) // Cor do conteúdo sobre 'surfaceVariant'
    // onPrimary, onSecondary, onTertiary, onBackground, onSurface são geralmente calculadas automaticamente,
    // mas podem ser sobrescritas se necessário.
)

// Esquema de cores MANUALMENTE definido para o tema claro (Light Mode)
// Usa as cores definidas em Color.kt
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,          // ATENÇÃO: É VERDE

    // Cores padrão do Material 3 Light - descomente e ajuste se precisar sobrescrever
    background = Color(0xFFFFFBFE),     // Fundo padrão (quase branco)
    surface = Color(0xFFFFFBFE),        // Superfície padrão (igual ao fundo)
    onPrimary = Color.White,            // Texto/ícones sobre a cor 'primary'
    onSecondary = Color.White,          // Texto/ícones sobre a cor 'secondary'
    onTertiary = Color.White,           // Texto/ícones sobre a cor 'tertiary'
    onBackground = Color(0xFF1C1B1F),   // Texto/ícones sobre a cor 'background' (escuro)
    onSurface = Color(0xFF1C1B1F),      // Texto/ícones sobre a cor 'surface' (escuro)
    error = Color(0xFFB3261E),          // Cor de erro padrão (vermelho)
    onError = Color(0xFF601410),        // Texto/ícones sobre a cor 'error'
    surfaceVariant = Color(0xFFE7E0EC), // Superfície variante (cinza claro)
    onSurfaceVariant = Color(0xFF49454F) // Texto/ícones sobre 'surfaceVariant' (cinza escuro)
)

/**
 * O Composable principal do tema para o aplicativo PokerPot.
 * Aplica o esquema de cores apropriado (claro/escuro, dinâmico ou manual) e a tipografia.
 * Envolve todo o conteúdo da UI do aplicativo.
 *
 * @param darkTheme Força o tema escuro (`true`) ou claro (`false`). Padrão é usar a configuração do sistema (`isSystemInDarkTheme()`).
 * @param dynamicColor Habilita cores dinâmicas baseadas no wallpaper do usuário (requer Android 12+). Padrão é `true`.
 * Se `false`, ou se no Android < 12, usará os esquemas Dark/LightColorScheme definidos manualmente acima.
 * @param content O conteúdo da UI (sua árvore de Composables) que receberá este tema.
 */
@Composable
fun PokerPoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta automaticamente o modo do sistema.
    dynamicColor: Boolean = true, // Habilita cores dinâmicas por padrão.
    content: @Composable () -> Unit // O lambda que contém a UI do seu app.
) {
    // Determina qual ColorScheme usar com base nos parâmetros e na versão do Android.
    val colorScheme = when {
        // 1. Cores Dinâmicas (Android 12+)?
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current // Precisa do Context para gerar cores dinâmicas.
            // Usa o esquema dinâmico apropriado (claro ou escuro) baseado no parâmetro 'darkTheme'.
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 2. Não usa cor dinâmica, e o tema é escuro?
        darkTheme -> DarkColorScheme // Usa o esquema escuro definido manualmente.
        // 3. Não usa cor dinâmica, e o tema é claro?
        else -> LightColorScheme // Usa o esquema claro definido manualmente.
    }

    // Aplica o MaterialTheme, passando o ColorScheme selecionado e a Tipografia definida em Type.kt.
    // Todo Composable dentro do lambda 'content' herdará essas definições.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Tipografia definida no arquivo Type.kt
        content = content        // Renderiza a UI do aplicativo passada como parâmetro.
    )
}

/* // <<< MELHORIA: Removido PokerAppTheme pois era redundante >>>
   // Esta função apenas aplicava o MaterialTheme padrão, sem as customizações.
   // É melhor usar diretamente PokerPotTheme que já faz tudo.
@Composable
fun PokerAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}
*/