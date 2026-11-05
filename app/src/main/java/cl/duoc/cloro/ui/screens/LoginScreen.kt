package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.R
import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.ui.components.CampoValidado
import cl.duoc.cloro.ui.viewmodel.LoginViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(onEntrar: (String, Perfil) -> Unit, form: LoginViewModel = viewModel()) {
    val estado by form.estado.collectAsStateWithLifecycle()
    val errores by form.errores.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = "Logo de la app",
                modifier = Modifier.size(72.dp)
            )
            Text("Control de cloro", style = MaterialTheme.typography.headlineMedium)
            Text("Acceso por perfil (datos simulados)", style = MaterialTheme.typography.bodyMedium)

            CampoValidado(
                valor = estado.nombre,
                onCambio = form::onNombre,
                etiqueta = "Nombre",
                icono = Icons.Filled.Person,
                error = errores.nombre
            )
            CampoValidado(
                valor = estado.correo,
                onCambio = form::onCorreo,
                etiqueta = "Correo",
                icono = Icons.Filled.Email,
                error = errores.correo,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            CampoValidado(
                valor = estado.clave,
                onCambio = form::onClave,
                etiqueta = "Clave",
                icono = Icons.Filled.Lock,
                error = errores.clave,
                esClave = true,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Password),
                ayuda = "Mínimo 6 caracteres y un número"
            )

            Text("Perfil", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
            Perfil.entries.forEach { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = estado.perfil == p, onClick = { form.onPerfil(p) }, role = Role.RadioButton),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.perfil == p, onClick = null)
                    Text(p.etiqueta, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = estado.aceptaTerminos, onCheckedChange = form::onTerminos)
                Text("Acepto el uso de datos simulados", style = MaterialTheme.typography.bodyMedium)
            }
            errores.terminos?.let {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                onClick = { if (form.validarFormulario()) onEntrar(estado.nombre.trim(), estado.perfil) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) { Text("Ingresar") }
        }
    }
}
