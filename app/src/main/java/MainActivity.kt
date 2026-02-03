package com.santamarta.safestudentsm
import android.Manifest
import android.content.Context
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




// =====================
// SHARED PREFERENCES
// =====================
private const val PREFS_NAME = "SafeStudentPrefs"
private const val PREF_NOMBRE = "nombre_usuario"
private const val PREF_APELLIDO = "apellido_usuario"
private const val PREF_SEXO = "sexo_usuario"
private const val PREF_EDAD = "edad_usuario"
private const val PREF_TIPO_SANGRE = "tipo_sangre"
private const val PREF_TELEFONO = "telefono_usuario"
private const val PREF_FECHA_REGISTRO = "fecha_registro"
private const val PREF_CONTACTOS = "contactos_emergencia"
private const val PREF_HISTORIAL = "historial_alertas"

// =====================
// CONSTANTES NAVEGACIÓN
// ====================
const val PANTALLA_LOGIN = "login"
const val PANTALLA_TERMINOS = "terminos_privacidad"
const val PANTALLA_REGISTRO = "registro"
const val PANTALLA_PRINCIPAL = "principal"
const val PANTALLA_CONTACTOS = "contactos"
const val PANTALLA_HISTORIAL = "historial"
const val PANTALLA_PERFIL = "perfil"
const val PANTALLA_EDITAR_PERFIL = "editar_perfil"



data class ContactoEmergencia(val nombre: String, val telefono: String)

data class Usuario(
    val nombre: String = "",
    val apellido: String = "",
    val sexo: String = "",
    val edad: String = "0",
    val tipoSangre: String = "",
    val telefono: String = "",
    val fechaRegistro: String = ""
)

data class Alerta(val fecha: String, val estado: String, val cantidad: Int)


fun guardarUsuario(context: Context, usuario: Usuario) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(PREF_NOMBRE, usuario.nombre)
        putString(PREF_APELLIDO, usuario.apellido)
        putString(PREF_SEXO, usuario.sexo)
        putString(PREF_TIPO_SANGRE, usuario.tipoSangre)
        putString(PREF_TELEFONO, usuario.telefono)
        putString(PREF_EDAD, usuario.edad)
        putString(PREF_FECHA_REGISTRO, usuario.fechaRegistro)
    }
}

fun cargarUsuario(context: Context): Usuario {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return Usuario(
        nombre = prefs.getString(PREF_NOMBRE, "") ?: "",
        apellido = prefs.getString(PREF_APELLIDO, "") ?: "",
        sexo = prefs.getString(PREF_SEXO, "") ?: "",
        edad = prefs.getString(PREF_EDAD, "0") ?: "0",
        tipoSangre = prefs.getString(PREF_TIPO_SANGRE, "") ?: "",
        telefono = prefs.getString(PREF_TELEFONO, "") ?: "",
        fechaRegistro = prefs.getString(PREF_FECHA_REGISTRO, "") ?: ""
    )
}


fun guardarContactos(context: Context, contactos: List<ContactoEmergencia>) {
    val texto = contactos.joinToString(";") { "${it.nombre}|${it.telefono}" }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(PREF_CONTACTOS, texto)
    }
}

fun cargarContactos(context: Context): List<ContactoEmergencia> {
    val texto = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(PREF_CONTACTOS, "") ?: ""
    if (texto.isBlank()) return emptyList()
    return texto.split(";").mapNotNull {
        val partes = it.split("|")
        if (partes.size >= 2) ContactoEmergencia(partes[0], partes[1]) else null
    }
}


fun guardarHistorial(context: Context, historial: List<Alerta>) {
    val texto = historial.joinToString(";") { "${it.fecha}|${it.estado}|${it.cantidad}" }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
        putString(PREF_HISTORIAL, texto)
    }
}

fun cargarHistorial(context: Context): List<Alerta> {
    val texto = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(PREF_HISTORIAL, "") ?: ""
    if (texto.isBlank()) return emptyList()
    return texto.split(";").mapNotNull {
        val partes = it.split("|")
        if (partes.size >= 3) Alerta(partes[0], partes[1], partes[2].toIntOrNull() ?: 0) else null
    }
}

fun eliminarHistorial(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { remove(PREF_HISTORIAL) }
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {

                val pantallaActualState = remember {
                    mutableStateOf(PANTALLA_LOGIN)
                }

                val pantallaActual = pantallaActualState.value

                when (pantallaActual) {


                    PANTALLA_LOGIN -> LoginScreen(
                        onLoginExitoso = {
                            pantallaActualState.value = PANTALLA_PRINCIPAL
                        },
                        onCrearCuenta = {
                            pantallaActualState.value = PANTALLA_TERMINOS
                        }
                    )


                    PANTALLA_TERMINOS -> TerminosPrivacidadScreen(
                        onAceptar = {
                            pantallaActualState.value = PANTALLA_REGISTRO
                        },
                        onBack = {
                            pantallaActualState.value = PANTALLA_LOGIN
                        }
                    )



                    PANTALLA_REGISTRO -> RegistroScreen(
                        onRegistroExitoso = {
                            pantallaActualState.value = PANTALLA_PRINCIPAL
                        },
                        onBack = {
                            pantallaActualState.value = PANTALLA_LOGIN
                        }
                    )


                    PANTALLA_PRINCIPAL -> SafeStudentScreen(
                        onBack = {
                            pantallaActualState.value = PANTALLA_LOGIN
                        },
                        onIrAContactos = {
                            pantallaActualState.value = PANTALLA_CONTACTOS
                        },
                        onIrAHistorial = {
                            pantallaActualState.value = PANTALLA_HISTORIAL
                        },
                        onIrAPerfil = {
                            pantallaActualState.value = PANTALLA_PERFIL
                        }
                    )


                    PANTALLA_CONTACTOS -> ContactosScreen(
                        onBack = {
                            pantallaActualState.value = PANTALLA_PRINCIPAL
                        }
                    )


                    PANTALLA_HISTORIAL -> HistorialScreen(
                        onBack = {
                            pantallaActualState.value = PANTALLA_PRINCIPAL
                        }
                    )



                    PANTALLA_PERFIL -> PerfilUsuarioScreen(
                        onBack = {
                            pantallaActualState.value = PANTALLA_PRINCIPAL
                        },
                        onEditar = {
                            pantallaActualState.value = PANTALLA_EDITAR_PERFIL
                        },
                        onLogout = {
                            pantallaActualState.value = PANTALLA_LOGIN
                        }
                    )





                    PANTALLA_EDITAR_PERFIL -> {
                        val context = LocalContext.current
                        val usuarioActual = remember {
                            cargarUsuario(context)
                        }

                        EditarPerfilScreen(
                            usuario = usuarioActual,
                            onGuardar = { nuevoUsuario ->
                                guardarUsuario(context, nuevoUsuario)
                                pantallaActualState.value = PANTALLA_PERFIL
                            },
                            onBack = {
                                pantallaActualState.value = PANTALLA_PERFIL
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun BackButton(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
        ) {
            Text("← Atrás", fontSize = 16.sp, fontWeight = FontWeight.Medium)  // ✅ SIN ICONO
        }
    }
}

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val context = LocalContext.current
    var telefonoLogin by remember { mutableStateOf("") }
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val telefonoGuardado = prefs.getString(PREF_TELEFONO, "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🛡️ SafeMate",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = telefonoLogin,
            onValueChange = { input ->
                telefonoLogin = input.filter { it.isDigit() }.take(10)
            },
            label = { Text("Tu teléfono (10 dígitos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (telefonoGuardado.isNotEmpty() && telefonoLogin == telefonoGuardado) {
                    Toast.makeText(context, "✅ ¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    onLoginExitoso()
                } else {
                    Toast.makeText(context, "❌ Regístrate primero", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = telefonoLogin.length == 10,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text("INICIAR SESIÓN", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable { onCrearCuenta() }
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {


            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "¿No tienes cuenta? ➕ Crear cuenta",
                color = Color(0xFF8B5CF6),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun TerminosPrivacidadScreen(
    onAceptar: () -> Unit,
    onBack: () -> Unit
) {

    val scrollState = rememberScrollState()
    var aceptado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {


        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text("⬅ Atrás", color = Color.Gray, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "📜 Términos, Condiciones y Privacidad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bienvenido a SafeMate – Seguridad para estudiantes y familia. " +
                    "Esta aplicación fue desarrollada por Keyler Ortiz con el objetivo de brindar " +
                    "herramientas de seguridad y monitoreo para estudiantes, familias y personas en general.",
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("⚠️ Condiciones de uso:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text =
                "1. El uso de esta aplicación es responsabilidad del usuario.\n" +
                        "2. SafeMate no se hace responsable por incidentes que ocurran fuera de la app.\n" +
                        "3. Se prohíbe el uso para actividades ilegales o malintencionadas.",
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("🔒 Privacidad y permisos:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text =
                "SafeMate solicita acceso a:\n" +
                        "- Ubicación: Para enviar alertas y ubicar al usuario en tiempo real.\n" +
                        "- Contactos: Para seleccionar a quién enviar alertas.\n" +
                        "- SMS: Para enviar mensajes de emergencia.\n\n" +
                        "Todos los datos se almacenan localmente en el dispositivo y no se comparten sin autorización.",
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("📌 Recomendaciones:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text =
                "- Mantén tu teléfono cargado y con conexión a internet.\n" +
                        "- Actualiza la aplicación regularmente.\n" +
                        "- No compartas tu contraseña con terceros.",
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))


        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = aceptado,
                onCheckedChange = { aceptado = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Acepto términos y condiciones", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAceptar,
            enabled = aceptado,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continuar al Registro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "© 2026 Keyler Ortiz. Todos los derechos reservados.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val tiposSangre = listOf(
        "O+", "O-", "A+", "A-",
        "B+", "B-", "AB+", "AB-"
    )

    var telefono by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var tipoSangre by remember { mutableStateOf("") }
    var sexoSeleccionado by remember { mutableStateOf("M") }
    var expanded by remember { mutableStateOf(false) }
    val errorTelefono = telefono.length != 10
    val errorNombre = nombre.isBlank() || nombre.any { it.isDigit() }
    val errorApellido = apellido.isBlank() || apellido.any { it.isDigit() }
    val errorEdad = edad.toIntOrNull() == null || edad.toInt() !in 10..100
    val errorTipoSangre = tipoSangre.isBlank()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {

        BackButton(onBack)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Crear Cuenta Completa",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            item {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it.filter { c -> c.isDigit() }.take(10)
                    },
                    label = { Text("Teléfono") },
                    isError = errorTelefono,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorTelefono) {
                    Text("Debe tener 10 dígitos", color = Color.Red, fontSize = 12.sp)
                }
            }


            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    isError = errorNombre,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorNombre) {
                    Text("El nombre no debe contener números", color = Color.Red, fontSize = 12.sp)
                }
            }


            item {
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    isError = errorApellido,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorApellido) {
                    Text("El apellido no debe contener números", color = Color.Red, fontSize = 12.sp)
                }
            }


            item {
                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it.filter { c -> c.isDigit() } },
                    label = { Text("Edad") },
                    isError = errorEdad,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorEdad) {
                    Text("Edad válida entre 10 y 100", color = Color.Red, fontSize = 12.sp)
                }
            }


            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = tipoSangre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Sangre") },
                        isError = errorTipoSangre,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tiposSangre.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoSangre = tipo
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (errorTipoSangre) {
                    Text("Selecciona un tipo de sangre", color = Color.Red, fontSize = 12.sp)
                }
            }


            item {
                Text("Género", fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        "M" to "👨 Masculino",
                        "F" to "👩 Femenino"
                    ).forEach { (valor, texto) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    if (sexoSeleccionado == valor) Color(0xFF1E88E5) else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { sexoSeleccionado = valor }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = sexoSeleccionado == valor,
                                onClick = { sexoSeleccionado = valor }
                            )
                            Text(
                                text = texto,
                                color = if (sexoSeleccionado == valor) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                val formularioValido =
                    !errorTelefono &&
                            !errorNombre &&
                            !errorApellido &&
                            !errorEdad &&
                            !errorTipoSangre

                if (formularioValido) {

                    val usuario = Usuario(
                        nombre = nombre,
                        apellido = apellido,
                        sexo = sexoSeleccionado,
                        edad = edad,
                        tipoSangre = tipoSangre,
                        telefono = telefono,
                        fechaRegistro = SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date())
                    )

                    guardarUsuario(context, usuario)
                    onRegistroExitoso()
                }
            }
        ) {
            Text(
                "CREAR CUENTA COMPLETA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }


    }
}

@Composable
fun PerfilUsuarioScreen(
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val usuario = cargarUsuario(context)


    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                )
            )
            .padding(bottom = 20.dp)
    ) {


        BackButton(onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 BOTONES EDITAR + ELIMINAR
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            Toast.makeText(context, "✏️ Editando...", Toast.LENGTH_SHORT).show()
                            onEditar()
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF8B5CF6)),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "✏️ Editar Cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "🗑️ Eliminar Cuenta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {


                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            if (usuario.sexo == "M") Color(0xFF1E40AF) else Color(0xFFBE185D),
                                            if (usuario.sexo == "M") Color(0xFF1E3A8A) else Color(0xFF9D174D)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }


                        Text(
                            text = "${usuario.nombre} ${usuario.apellido}".trim(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )

                        // 🔹 DATOS
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PerfilItem("📱 Teléfono", usuario.telefono)
                            PerfilItem("🎂 Edad", "${usuario.edad} años")
                            PerfilItem(
                                "👤 Género",
                                when (usuario.sexo) {
                                    "M" -> "Masculino"
                                    "F" -> "Femenino"
                                    else -> "No definido"
                                }
                            )
                            PerfilItem("🩸 Tipo Sangre", usuario.tipoSangre)
                            PerfilItem("📅 Miembro desde", usuario.fechaRegistro)
                        }
                    }
                }
            }
        }


        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "⚠️ Eliminar Cuenta",
                        color = Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Esta acción eliminará TODOS tus datos permanentemente:\n\n" +
                                "• Perfil completo\n" +
                                "• Historial de alertas\n" +
                                "• Configuraciones\n\n" +
                                "¿Estás completamente seguro?",
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {

                            listOf(
                                "safe_student_prefs",
                                "SafeStudentPrefs",
                                "safe_student_usuario",
                                "alertas_historial"
                            ).forEach { name ->
                                context.getSharedPreferences(name, Context.MODE_PRIVATE).edit { clear() }
                            }

                            Toast.makeText(
                                context,
                                "🗑️ Cuenta eliminada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            showDeleteDialog = false
                            onLogout()
                        }
                    ) {
                        Text(
                            "SÍ, ELIMINAR TODO",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(
                            "Cancelar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            )
        }

    }
}



@Composable
fun EditarPerfilScreen(
    usuario: Usuario,
    onGuardar: (Usuario) -> Unit,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var apellido by remember { mutableStateOf(usuario.apellido ) }
    var edad by remember { mutableStateOf(usuario.edad) }
    var sexo by remember { mutableStateOf(usuario.sexo) }
    var telefono by remember { mutableStateOf(usuario.telefono) }
    var tipoSangre by remember { mutableStateOf(usuario.tipoSangre ) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = sexo,
                onValueChange = { sexo = it },
                label = { Text("Sexo (M/F)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = telefono,
                onValueChange = { input ->
                    telefono = input.filter { it.isDigit() }.take(10)
                },
                label = { Text("Teléfono (10 dígitos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = tipoSangre,
                onValueChange = { tipoSangre = it },
                label = { Text("Tipo Sangre") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        onGuardar(Usuario(
                            nombre = nombre,
                            apellido = apellido,
                            edad = edad.toIntOrNull()?.toString() ?: "18",
                            sexo = sexo,
                            telefono = telefono,
                            tipoSangre = tipoSangre,
                            fechaRegistro = usuario.fechaRegistro
                        )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }

            }
        }
    }
}




@Composable
private fun PerfilItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            textAlign = TextAlign.End
        )
    }
}


@Composable
fun PremiumInfoCard(
    onIrAContactos: () -> Unit,
    onIrAHistorial: () -> Unit,
    onIrAPerfil: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // BOTÓN CONTACTOS
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .clickable { onIrAContactos() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
                        )
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 32.sp)
                    Text("Contactos", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // BOTÓN HISTORIAL
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .clickable { onIrAHistorial() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF43A047), Color(0xFF66BB6A))
                        )
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 32.sp)
                    Text("Historial", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // BOTÓN PERFIL
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp)
                .clickable { onIrAPerfil() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFB794F6))
                        )
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👤", fontSize = 32.sp)
                    Text("Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}





@Composable
fun SafeStudentScreen(
    onBack: () -> Unit,
    onIrAContactos: () -> Unit,
    onIrAHistorial: () -> Unit,
    onIrAPerfil: () -> Unit
) {
    val context = LocalContext.current
    val contactos = cargarContactos(context)

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && contactos.isNotEmpty()) {
            enviarSMS(context, contactos)
        }
    }

    //  NUEVA VARIABLE PARA CUENTA REGRESIVA
    var panicCountdown by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        BackButton(onBack)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "🛡️ SafeMate",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5)
            )

            Spacer(modifier = Modifier.height(140.dp))

            //  BOTÓN PÁNICO CON CUENTA REGRESIVA (REEMPLAZADO)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (panicCountdown > 0) {
                    //  CUENTA REGRESIVA (GRANDE + ROJO)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color(0xFFF4511E).copy(alpha = 0.95f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { panicCountdown = 0 }, // Cancelar tocando
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = panicCountdown.toString(),
                                fontSize = 48.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "TOCA PARA CANCELAR",
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }


                    LaunchedEffect(panicCountdown) {
                        if (panicCountdown > 0) {
                            delay(1000L)
                            panicCountdown--
                            if (panicCountdown <= 0) {
                                // 🚨 TU LÓGICA ORIGINAL (perfecta)
                                if (contactos.isEmpty()) {
                                    Toast.makeText(context, "❌ Agrega contactos primero", Toast.LENGTH_LONG).show()
                                } else {
                                    smsLauncher.launch(Manifest.permission.SEND_SMS)
                                }
                            }
                        }
                    }

                } else {

                    Button(
                        onClick = {
                            panicCountdown = 3
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4511E)),
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = "Alerta", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🚨 BOTÓN DE PÁNICO",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            PremiumInfoCard(
                onIrAContactos = onIrAContactos,
                onIrAHistorial = onIrAHistorial,
                onIrAPerfil = onIrAPerfil
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun enviarSMS(context: Context, contactos: List<ContactoEmergencia>) {
    try {
        val smsManager = context.getSystemService(SmsManager::class.java)
        var smsEnviados = 0

        contactos.forEach { contacto ->
            if (contacto.telefono.isNotBlank() && contacto.telefono.length >= 10) {
                // 🔥 TU MENSAJE PERFECTO
                val mensajeUrgente = "AUXILIO POR FAVOR LLAMAME URGENTE"
                smsManager.sendTextMessage(contacto.telefono, null, mensajeUrgente, null, null)
                smsEnviados++
            }
        }

        val fecha = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
        val alerta = Alerta(fecha, "URGENTE ✓", smsEnviados)
        val historial = cargarHistorial(context).toMutableList()
        historial.add(0, alerta)
        guardarHistorial(context, historial)

        Toast.makeText(context, "🚨 $smsEnviados ALERTAS URGENTES", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Log.e("SafeStudent", "Error SMS: ${e.message}", e)
        Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}




@Composable
fun ContactosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var contactos by remember { mutableStateOf(cargarContactos(context)) }
    var nombreNuevo by remember { mutableStateOf("") }
    var telefonoNuevo by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp)) {
        BackButton(onBack)
        Text("Contactos Emergencia (Máx 5)", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (contactos.isEmpty()) {
                Text("No hay contactos agregados", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(contactos) { contacto ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contacto.nombre, fontWeight = FontWeight.Bold)
                                    Text(contacto.telefono, color = Color.Gray)
                                }
                                IconButton(onClick = {
                                    contactos = contactos.filter { it != contacto }
                                    guardarContactos(context, contactos)
                                }) {
                                    Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = nombreNuevo, onValueChange = { nombreNuevo = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = telefonoNuevo,
            onValueChange = { input ->
                telefonoNuevo = input.filter { it.isDigit() }.take(10)
            },
            label = { Text("Teléfono (10 dígitos)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (nombreNuevo.isNotBlank() && telefonoNuevo.length == 10 && contactos.size < 5) {
                contactos = contactos + ContactoEmergencia(nombreNuevo, telefonoNuevo)
                guardarContactos(context, contactos)
                nombreNuevo = ""
                telefonoNuevo = ""
                Toast.makeText(context, "✅ Contacto agregado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "❌ Máximo 5 contactos o completa campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar Contacto")
        }

    }
}


@Composable
fun HistorialScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var historial by remember { mutableStateOf(cargarHistorial(context)) }

    LaunchedEffect(Unit) { historial = cargarHistorial(context) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp)) {
        BackButton(onBack)
        if (historial.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    eliminarHistorial(context)
                    historial = emptyList()
                    Toast.makeText(context, "🗑 Historial limpiado", Toast.LENGTH_SHORT).show()
                }) {
                    Text("🗑 Limpiar", color = Color.Red)
                }
            }
        }

        Text("Historial de Alertas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (historial.isEmpty()) {
                Text("No hay alertas registradas", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(historial) { alerta ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(alerta.fecha, fontWeight = FontWeight.Bold)
                                Text("Estado: ${alerta.estado} | SMS: ${alerta.cantidad}")
                            }
                        }
                    }
                }
            }
        }
    }
}


