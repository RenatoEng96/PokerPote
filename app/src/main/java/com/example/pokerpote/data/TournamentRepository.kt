package com.example.pokerpote.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pokerpote.tournament.Tournament
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tournament_settings")

class TournamentRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val TOURNAMENT_KEY = stringPreferencesKey("tournament_state")
    }

    val tournamentFlow: Flow<Tournament?> = dataStore.data.map { preferences ->
        preferences[TOURNAMENT_KEY]?.let { jsonString ->
            try {
                Json.decodeFromString<Tournament>(jsonString)
            } catch (e: Exception) {
                // Se falhar a deserialização, retorna null
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun saveTournament(tournament: Tournament) {
        dataStore.edit { preferences ->
            val jsonString = Json.encodeToString(tournament)
            preferences[TOURNAMENT_KEY] = jsonString
        }
    }

    suspend fun clearTournament() {
        dataStore.edit { preferences ->
            preferences.remove(TOURNAMENT_KEY)
        }
    }
}