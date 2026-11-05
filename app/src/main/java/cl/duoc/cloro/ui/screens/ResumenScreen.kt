@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.ui.components.ChipEstado
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.theme.colorEstado
import cl.duoc.cloro.ui.theme.formatoFecha
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun ResumenScreen(vm: MainViewModel) {
    val puntos by vm.puntos.collectAsStateWithLifecycle()
    val alertas by vm.alertas.collectAsStateWithLifecycle()
    val acciones by vm.ultimasAcciones.collectAsStateWithLifecycle()

    val conteo = puntos.groupingBy { it.estado }.eachCount()
    val activas = alertas.count { it.activa }
    val fueraDeRango = puntos.filter { it.estado == Estado.ADVERTENCIA || it.estado == Estado.CRITICO }
    val porGranja = puntos.groupBy { it.punto.granja }.mapValues { (_, lista) ->
        lista.mapNotNull { it.ultima?.nivel }.average()
    }

    Pantalla("Resumen") {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "conteos") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Estado.entries.forEach { e ->
                        Card(Modifier.weight(1f)) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${conteo[e] ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorEstado(e)
                                )
                                Text(e.etiqueta, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item(key = "activas") {
                Text("Alertas activas: $activas", style = MaterialTheme.typography.titleMedium)
            }
            item(key = "granjas") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Promedio actual por granja", style = MaterialTheme.typography.titleMedium)
                        porGranja.forEach { (granja, prom) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(granja)
                                Text(if (prom.isNaN()) "—" else String.format(Locale("es", "CL"), "%.2f ppm", prom))
                            }
                        }
                    }
                }
            }
            item(key = "fuera") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Puntos fuera de rango", style = MaterialTheme.typography.titleMedium)
                        if (fueraDeRango.isEmpty()) Text("Todos los puntos están en rango normal")
                        fueraDeRango.forEach { p ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${p.punto.granja} · ${p.punto.galpon}", Modifier.weight(1f))
                                ChipEstado(p.estado)
                            }
                        }
                    }
                }
            }
            item(key = "acciones") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Últimas acciones registradas", style = MaterialTheme.typography.titleMedium)
                        if (acciones.isEmpty()) Text("Sin acciones registradas")
                        acciones.take(5).forEach { a ->
                            Text(
                                "${a.tipo} · ${a.usuario} (${Perfil.valueOf(a.perfil).etiqueta}) · ${formatoFecha(a.timestamp)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
