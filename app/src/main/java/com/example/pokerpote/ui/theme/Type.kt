// Este é o caminho onde está esse arquivo: app/src/main/java/com/example/pokerpote/ui/theme/Type.kt
package com.example.pokerpote.ui.theme

import androidx.compose.material3.Typography // Classe que define os estilos de texto do Material Theme
import androidx.compose.ui.text.TextStyle // Define propriedades de um estilo de texto (fonte, tamanho, peso, etc.)
import androidx.compose.ui.text.font.FontFamily // Família da fonte (Default, SansSerif, Serif, etc.)
import androidx.compose.ui.text.font.FontWeight // Peso da fonte (Normal, Bold, Medium, Light, etc.)
import androidx.compose.ui.unit.sp // Unidade para tamanho de fonte (Scale-independent Pixels)

// Define o conjunto de estilos de tipografia para o aplicativo.
// Você pode sobrescrever os estilos padrão do Material Design aqui.
val Typography = Typography(
    // Estilo para o corpo de texto principal (parágrafos, etc.)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Usa a fonte padrão do sistema
        fontWeight = FontWeight.Normal, // Peso normal (não negrito)
        fontSize = 16.sp,               // Tamanho da fonte
        lineHeight = 24.sp,             // Altura da linha (espaçamento vertical)
        letterSpacing = 0.5.sp          // Espaçamento entre letras
    )

    /* Outros estilos de texto padrão que você PODE sobrescrever se desejar:
    // Estilo para títulos grandes
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Estilo para rótulos pequenos (ex: texto de suporte em TextFields)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium, // Peso médio (um pouco mais forte que Normal)
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    // Existem muitos outros estilos: bodyMedium, bodySmall, titleMedium, titleSmall,
    // headlineLarge, headlineMedium, headlineSmall, displayLarge, displayMedium, displaySmall, etc.
    */
)