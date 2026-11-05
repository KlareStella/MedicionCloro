package cl.duoc.cloro.repository

import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.model.Sesion
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_cloro")

class PreferenciasRepository(private val context: Context) {

    private object Claves {
        val USUARIO = stringPreferencesKey("usuario")
        val PERFIL = stringPreferencesKey("perfil")
        val SOLO_PROBLEMAS = booleanPreferencesKey("solo_problemas")
    }

    val sesion: Flow<Sesion?> = context.dataStore.data.map { p ->
        val usuario = p[Claves.USUARIO]
        val perfil = p[Claves.PERFIL]?.let { runCatching { Perfil.valueOf(it) }.getOrNull() }
        if (usuario != null && perfil != null) Sesion(usuario, perfil) else null
    }

    val soloProblemas: Flow<Boolean> = context.dataStore.data.map { it[Claves.SOLO_PROBLEMAS] ?: false }

    suspend fun guardarSesion(sesion: Sesion) {
        context.dataStore.edit {
            it[Claves.USUARIO] = sesion.usuario
            it[Claves.PERFIL] = sesion.perfil.name
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit {
            it.remove(Claves.USUARIO)
            it.remove(Claves.PERFIL)
        }
    }

    suspend fun guardarSoloProblemas(valor: Boolean) {
        context.dataStore.edit { it[Claves.SOLO_PROBLEMAS] = valor }
    }
}
