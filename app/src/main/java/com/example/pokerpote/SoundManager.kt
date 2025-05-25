// main/java/com/example/pokerpote/SoundManager.kt
package com.example.pokerpote

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log

/**
 * Objeto singleton para gerenciar a reprodução de sons no aplicativo.
 */
object SoundManager {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Toca o som de notificação padrão do sistema.
     * Garante que apenas uma instância do som seja tocada por vez e libera os recursos
     * do MediaPlayer após a conclusão.
     *
     * @param context O contexto da aplicação, necessário para acessar os recursos do sistema.
     */
    fun playSound(context: Context) {
        // Para o player anterior se ele ainda estiver tocando
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: IllegalStateException) {
            Log.e("SoundManager", "Erro ao parar MediaPlayer anterior.", e)
            mediaPlayer = null
        }


        try {
            // Obtém o URI do som de notificação padrão do sistema
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // Cria uma nova instância do MediaPlayer
            mediaPlayer = MediaPlayer.create(context, notificationSoundUri).apply {
                // Adiciona um listener para liberar os recursos quando o som terminar de tocar
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null // Define como nulo para que possa ser recriado na próxima chamada
                }
                // Adiciona um listener de erro para debugging
                setOnErrorListener { mp, what, extra ->
                    Log.e("SoundManager", "MediaPlayer Error: what: $what, extra: $extra")
                    mp.release()
                    mediaPlayer = null
                    true // Indica que o erro foi tratado
                }
            }
            // Inicia a reprodução do som
            mediaPlayer?.start()

        } catch (e: Exception) {
            Log.e("SoundManager", "Não foi possível tocar o som.", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}