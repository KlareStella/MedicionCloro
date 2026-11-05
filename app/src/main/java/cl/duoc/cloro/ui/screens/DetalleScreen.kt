@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.model.Rangos
import cl.duoc.cloro.ui.components.ChipEstado
import cl.duoc.cloro.ui.components.EvidenciaAccion
import cl.duoc.cloro.ui.components.GraficoCloro
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.theme.colorEstado
import cl.duoc.cloro.ui.theme.formatoFecha
import cl.duoc.cloro.ui.theme.nivelTexto
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DetalleScreen(vm: MainViewModel, id: Int, onBack: () -> Unit, onRegistrar: () -> Unit) {
    val punto by remember(id) { vm.punto(id) }.collectAsStateWithLifecycle(initialValue = null)
    Pantalla(titulo = punto?.galpon ?: "Detalle", onBack = onBack) {
        DetalleContenido(vm, id, onRegistrar, Modifier.weight(1f))
    }
}

@Composable
fun DetalleContenido(vm: MainViewModel, id: Int, onRegistrar: () -> Unit, modifier: Modifier = Modifier) {
    val punto by remember(id) { vm.punto(id) }.collectAsStateWithLifecycle(initialValue = null)
    val hist by remember(id) { vm.historial(id) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val acciones by remember(id) { vm.acciones(id) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val sesion by vm.sesion.collectAsStateWithLifecycle()

    val actual = hist.firstOrNull()
    val estado = actual?.let { Rangos.estadoDe(it.nivel) }
    val puedeRegistrar = sesion?.perfil != Perfil.JEFATURA
    val colorNivel by animateColorAsState(colorEstado(estado), tween(600), label = "colorNivelDetalle")

    LazyColumn(
        modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "actual") {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        punto?.let { "${it.granja} · ${it.galpon}" } ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            actual?.let { nivelTexto(it.nivel) } ?: "—",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorNivel
                        )
                        Spacer(Modifier.width(12.dp))
                        ChipEstado(estado)
                    }
                    Text(
                        actual?.let { "Actualizado: ${formatoFecha(it.timestamp)}" } ?: "Sin mediciones",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Rango normal: ${Rangos.NORMAL_MIN} – ${Rangos.NORMAL_MAX} ppm",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item(key = "grafico") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Últimas 24 mediciones", style = MaterialTheme.typography.titleMedium)
                GraficoCloro(hist.take(24).reversed())
            }
        }
        if (puedeRegistrar) {
            item(key = "boton") {
                Button(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar control o acción")
                }
            }
        }
        item(key = "t_acciones") {
            Text("Acciones registradas", style = MaterialTheme.typography.titleMedium)
        }
        if (acciones.isEmpty()) {
            item(key = "sin_acciones") {
                Text("Aún no hay acciones registradas", style = MaterialTheme.typography.bodySmall)
            }
        }
        items(acciones, key = { "a${it.id}" }) { a ->
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(12.dp)) {
                    Text(a.tipo + if (a.resuelta) " · Resuelta" else "", style = MaterialTheme.typography.titleSmall)
                    Text(a.descripcion, style = MaterialTheme.typography.bodyMedium)
                    EvidenciaAccion(a)
                    Text(
                        "${a.usuario} (${Perfil.valueOf(a.perfil).etiqueta}) · ${formatoFecha(a.timestamp)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item(key = "t_hist") {
            Text("Historial de mediciones", style = MaterialTheme.typography.titleMedium)
        }
        items(hist, key = { "m${it.id}" }) { m ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(formatoFecha(m.timestamp), style = MaterialTheme.typography.bodyMedium)
                Text(nivelTexto(m.nivel), style = MaterialTheme.typography.bodyMedium)
                ChipEstado(Rangos.estadoDe(m.nivel))
            }
        }
    }
}
