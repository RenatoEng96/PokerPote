// main/java/com/example/pokerpote/MainActivity.kt
package com.example.pokerpote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pokerpote.ui.theme.PokerPoteTheme

/**
 * Activity principal e ponto de entrada do aplicativo.
 * Sua única responsabilidade é configurar o tema e o conteúdo da UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplica o tema customizado 'PokerPoteTheme'
            PokerPoteTheme {
                // Chama o Composable que gerencia toda a navegação do app
                AppNavigation()
            }
        }
    }
}