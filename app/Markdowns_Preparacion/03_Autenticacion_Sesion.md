# 4.3 Autenticacion y sesion

## Objetivo de esta fase

Entender como StikerVault permite iniciar sesion, restaurar una sesion guardada, cerrar sesion y controlar permisos segun el rol del usuario.

En una defensa, esta seccion ayuda a explicar:

- Como la pantalla de login captura el correo.
- Como el ViewModel valida y ejecuta el inicio de sesion.
- Como se representa el estado de autenticacion.
- Como se guarda una sesion local con DataStore.
- Como se modela un usuario.
- Como los roles afectan permisos en la app.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/auth/LoginScreen.kt
app/src/main/java/com/example/myapplication/presentation/auth/AuthViewModel.kt
app/src/main/java/com/example/myapplication/presentation/auth/AuthUiState.kt
app/src/main/java/com/example/myapplication/data/local/session/SessionPreferences.kt
app/src/main/java/com/example/myapplication/domain/model/AppUser.kt
app/src/main/java/com/example/myapplication/domain/model/UserRole.kt
```

Flujo general:

```text
LoginScreen
-> AuthViewModel.login(email)
-> LoginUseCase
-> AuthRepository
-> Supabase / Room
-> AuthViewModel actualiza AuthUiState y currentUser
-> LoginScreen detecta Success
-> NavHost navega a Home
```

## 1. LoginScreen.kt

`LoginScreen` es la pantalla visual de autenticacion.

Firma:

```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
)
```

Recibe:

- `viewModel`: controla estado y acciones de login.
- `onLoginSuccess`: callback que se ejecuta cuando el login fue correcto.

Idea clave:

```text
LoginScreen no decide a que pantalla navegar. Solo avisa que el login fue exitoso usando onLoginSuccess.
```

## 2. Estado local del correo

```kotlin
var email by remember { mutableStateOf("") }
```

La pantalla guarda temporalmente el correo escrito por el usuario.

`remember` conserva el valor durante recomposiciones de Compose.

`mutableStateOf` hace que Compose redibuje la UI cuando cambia el correo.

Respuesta teorica posible:

```text
El email es estado local de la pantalla porque solo se necesita mientras el usuario escribe en el formulario. No hace falta guardarlo en el ViewModel hasta presionar Ingresar.
```

## 3. Lectura del estado del ViewModel

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

`uiState` viene de `AuthViewModel`.

La pantalla reacciona a estos estados:

- `Idle`: estado inicial.
- `Loading`: se esta intentando iniciar sesion.
- `Success`: login correcto.
- `Error`: hubo un problema.

Idea clave:

```text
La pantalla observa el estado; el ViewModel decide el estado.
```

## 4. Navegacion despues de login exitoso

```kotlin
LaunchedEffect(uiState) {
    if (uiState is AuthUiState.Success) onLoginSuccess()
}
```

Cuando `uiState` cambia a `Success`, se llama `onLoginSuccess`.

`LaunchedEffect` se usa para ejecutar un efecto secundario en Compose.

En este caso, el efecto secundario es navegar.

Respuesta teorica posible:

```text
La pantalla usa LaunchedEffect para reaccionar al estado Success y ejecutar el callback de navegacion. Asi la navegacion ocurre una vez cuando cambia el estado.
```

## 5. Estructura visual del login

La pantalla usa una `Column` centrada:

```kotlin
Column(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
)
```

Elementos principales:

- Titulo `StickrVault`.
- Subtitulo `Inventario PANINI Mundial 2026`.
- Campo `OutlinedTextField` para correo.
- Chips de acceso rapido demo.
- Boton `Ingresar`.
- Mensaje de error si existe.

Cambios de examen probables:

- Cambiar el padding general de la pantalla.
- Cambiar titulo o subtitulo.
- Cambiar texto del boton.
- Cambiar label del campo.
- Quitar o cambiar chips de demo.
- Cambiar colores o estilos.

## 6. Campo de correo

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Correo electronico") },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    modifier = Modifier.fillMaxWidth(),
    enabled = uiState !is AuthUiState.Loading
)
```

Funcionamiento:

- Muestra el valor de `email`.
- Actualiza `email` cuando el usuario escribe.
- Muestra teclado de tipo email.
- Se deshabilita mientras carga.

Idea clave:

```text
El campo no hace login por si solo; solo actualiza el estado local email.
```

## 7. Chips de acceso rapido

```kotlin
listOf(
    "martin.herrera@stickrvault.com" to "Jefe",
    "katherine.maldonado@stickrvault.com" to "Operador",
    "paulina.astudillo@stickrvault.com" to "Auditor"
).forEach { (mail, label) ->
    AssistChip(
        onClick = { email = mail },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}
```

Estos chips facilitan pruebas de demo.

Cuando se toca un chip:

```text
email = correo del usuario demo
```

Cambios de examen probables:

- Cambiar los correos demo.
- Cambiar labels de roles.
- Quitar un chip.
- Agregar un chip nuevo.

Respuesta teorica posible:

```text
Los chips solo modifican el estado local del email para facilitar el acceso demo. No autentican directamente al usuario.
```

## 8. Boton Ingresar

```kotlin
Button(
    onClick = { viewModel.login(email) },
    modifier = Modifier.fillMaxWidth(),
    enabled = uiState !is AuthUiState.Loading
)
```

Cuando se presiona:

```text
LoginScreen -> viewModel.login(email)
```

Si esta cargando, el boton se deshabilita.

Dentro del boton:

- Si `Loading`: muestra `CircularProgressIndicator`.
- Si no: muestra `Text("Ingresar")`.

Respuesta teorica posible:

```text
La pantalla envia el evento al ViewModel. El ViewModel valida y ejecuta el login; la pantalla solo muestra loading, error o exito segun el estado.
```

## 9. Mensaje de error

```kotlin
if (uiState is AuthUiState.Error) {
    Text(
        text = (uiState as AuthUiState.Error).message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}
```

El mensaje viene desde el ViewModel.

Cambios de examen probables:

- Cambiar color del error.
- Cambiar estilo del texto.
- Cambiar mensajes en `AuthViewModel`.
- Mover el mensaje a otra posicion.

## 10. AuthViewModel.kt

`AuthViewModel` administra el estado de autenticacion.

Constructor:

```kotlin
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel()
```

Recibe:

- `LoginUseCase`: accion de iniciar sesion.
- `AuthRepository`: acceso a sesion guardada y cierre de sesion.

Idea clave:

```text
AuthViewModel conecta la UI con la logica de autenticacion.
```

## 11. Estados internos del AuthViewModel

```kotlin
private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
```

`uiState` informa a la UI si esta en idle, loading, success o error.

```kotlin
private val _currentUser = MutableStateFlow<AppUser?>(null)
val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()
```

`currentUser` guarda el usuario autenticado actual.

```kotlin
private val _isSessionReady = MutableStateFlow(false)
val isSessionReady: StateFlow<Boolean> = _isSessionReady.asStateFlow()
```

`isSessionReady` indica si ya termino la restauracion de sesion.

Respuesta teorica posible:

```text
Se usan StateFlow para exponer estados observables a Compose. Los MutableStateFlow quedan privados para que solo el ViewModel pueda modificarlos.
```

## 12. Restauracion de sesion

```kotlin
init {
    restoreSession()
}
```

Cuando se crea el ViewModel, intenta restaurar la sesion.

```kotlin
private fun restoreSession() {
    viewModelScope.launch {
        _currentUser.value = authRepository.getSavedSession()
        _isSessionReady.value = true
    }
}
```

Funcionamiento:

1. Llama al repositorio para obtener sesion guardada.
2. Si existe usuario, lo asigna a `_currentUser`.
3. Marca `_isSessionReady` como `true`.

El NavHost usa `isSessionReady` para decidir cuando crear la navegacion.

Respuesta teorica posible:

```text
La restauracion de sesion se ejecuta al crear el AuthViewModel. Asi la app puede iniciar directamente en Home si ya habia un usuario guardado.
```

## 13. Login

```kotlin
fun login(email: String) {
    if (email.isBlank()) {
        _uiState.value = AuthUiState.Error("Ingresa un correo valido")
        return
    }
    viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        val user = loginUseCase(email.trim())
        if (user != null) {
            _currentUser.value = user
            _uiState.value = AuthUiState.Success(user)
        } else {
            _uiState.value = AuthUiState.Error("Usuario no encontrado. Verifica tu correo.")
        }
    }
}
```

Pasos:

1. Valida que el correo no este vacio.
2. Cambia estado a `Loading`.
3. Ejecuta `LoginUseCase`.
4. Si encuentra usuario:
   - actualiza `currentUser`.
   - cambia estado a `Success`.
5. Si no encuentra usuario:
   - cambia estado a `Error`.

Cambios de examen probables:

- Validar formato de correo.
- Cambiar mensaje de error.
- Bloquear ciertos dominios.
- Permitir login con usuario ademas de email.

Respuesta teorica posible:

```text
La validacion basica de login esta en el ViewModel porque es una regla de presentacion. La pantalla solo captura el texto y envia el evento.
```

## 14. Logout

```kotlin
fun logout() {
    _currentUser.value = null
    _uiState.value = AuthUiState.Idle
    viewModelScope.launch {
        authRepository.clearSession()
    }
}
```

Pasos:

1. Limpia el usuario actual.
2. Regresa el estado UI a `Idle`.
3. Borra la sesion guardada en DataStore.

La navegacion a Login no ocurre aqui.

La navegacion ocurre en `StickrVaultNavHost`.

Idea clave:

```text
AuthViewModel limpia la sesion; NavHost decide a que pantalla navegar.
```

## 15. AuthUiState.kt

`AuthUiState` representa los estados posibles del login.

```kotlin
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(
        val user: AppUser,
        val isOffline: Boolean = false
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
```

Estados:

- `Idle`: estado inicial, sin accion en progreso.
- `Loading`: se esta procesando login.
- `Success`: login correcto; incluye usuario.
- `Error`: login fallido; incluye mensaje.

### Por que es sealed class

Una `sealed class` limita los estados posibles a los definidos en el archivo.

Ventaja:

```text
La UI puede manejar de forma clara todos los estados esperados.
```

`Success` tiene `isOffline`, aunque en el flujo actual no se usa desde la pantalla. Puede servir para indicar si el login vino de cache local.

Respuesta teorica posible:

```text
AuthUiState permite representar el estado de autenticacion de forma ordenada. En lugar de muchas variables sueltas, la pantalla observa un unico estado.
```

## 16. SessionPreferences.kt

`SessionPreferences` guarda datos simples de sesion usando DataStore Preferences.

Creacion del DataStore:

```kotlin
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stickrvault_session"
)
```

Nombre del archivo de preferencias:

```text
stickrvault_session
```

Datos guardados:

- `user_id`
- `user_email`

## 17. Guardar sesion

```kotlin
suspend fun saveSession(userId: String, email: String) {
    context.sessionDataStore.edit { prefs ->
        prefs[KEY_USER_ID] = userId
        prefs[KEY_USER_EMAIL] = email
    }
}
```

Guarda el ID y email del usuario.

Se marca como `suspend` porque DataStore trabaja de forma asincronica.

Respuesta teorica posible:

```text
DataStore guarda la sesion de forma persistente y asincronica, por eso las funciones son suspend.
```

## 18. Leer sesion

```kotlin
suspend fun getSavedUserId(): String? =
    context.sessionDataStore.data.map { it[KEY_USER_ID] }.first()
```

```kotlin
suspend fun getSavedEmail(): String? =
    context.sessionDataStore.data.map { it[KEY_USER_EMAIL] }.first()
```

Estas funciones leen el primer valor disponible del flujo de DataStore.

Se usan desde el repositorio de autenticacion para restaurar la sesion.

## 19. Limpiar sesion

```kotlin
suspend fun clearSession() {
    context.sessionDataStore.edit { it.clear() }
}
```

Borra todos los valores guardados en ese DataStore.

Se usa al cerrar sesion.

Cambios de examen probables:

- Guardar otro dato simple de sesion.
- Cambiar nombre de keys.
- Limpiar solo una key especifica.
- Explicar por que la sesion persiste al cerrar y abrir la app.

## 20. AppUser.kt

`AppUser` es el modelo de dominio del usuario.

```kotlin
data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole
)
```

Campos:

- `id`: identificador unico.
- `name`: nombre del usuario.
- `email`: correo usado para login.
- `role`: rol dentro del sistema.

Idea clave:

```text
AppUser pertenece a domain/model, por eso representa al usuario que entiende la app, no necesariamente la forma exacta de la API o de Room.
```

Respuesta teorica posible:

```text
AppUser es el modelo de dominio del usuario. La UI y los ViewModels trabajan con este modelo, mientras DTOs y entidades se usan en la capa data.
```

## 21. UserRole.kt

`UserRole` define los roles permitidos.

```kotlin
enum class UserRole {
    WAREHOUSE_CHIEF,
    WAREHOUSE_OPERATOR,
    AUDITOR
}
```

Roles:

- `WAREHOUSE_CHIEF`: jefe de bodega.
- `WAREHOUSE_OPERATOR`: operador de bodega.
- `AUDITOR`: auditor.

Estos roles se usan para controlar permisos.

Ejemplo en el proyecto:

```text
El auditor no puede modificar inventario.
El jefe y operador pueden usar acciones de inventario desde scanner.
```

Respuesta teorica posible:

```text
UserRole centraliza los roles disponibles. Esto evita usar strings dispersos y permite comparar permisos de forma segura.
```

## 22. Relacion con otros archivos

Aunque esta seccion se enfoca en pantalla, ViewModel, estado, sesion y modelos, el flujo tambien depende de:

```text
LoginUseCase
AuthRepository
AuthRepositoryImpl
AppUserDao
AppUserMapper
SupabaseApiService
```

Resumen:

```text
LoginScreen captura email.
AuthViewModel valida y ejecuta login.
LoginUseCase delega al repositorio.
AuthRepositoryImpl busca usuario remoto o local.
SessionPreferences guarda/restaura sesion.
StickrVaultNavHost navega segun currentUser.
```

## 23. Cambios de examen probables en autenticacion

### Cambiar texto del login

Archivo probable:

```text
LoginScreen.kt
```

Ejemplos:

- Cambiar `StickrVault`.
- Cambiar subtitulo.
- Cambiar `Ingresar`.
- Cambiar `Correo electronico`.

### Cambiar padding o alineacion

Archivo probable:

```text
LoginScreen.kt
```

Ubicacion:

```kotlin
modifier = Modifier.fillMaxSize().padding(32.dp)
```

### Validar formato de correo

Archivo probable:

```text
AuthViewModel.kt
```

Ubicacion:

```kotlin
fun login(email: String)
```

Idea:

```text
Agregar una condicion antes de ejecutar loginUseCase.
```

### Cambiar mensaje de error

Archivo probable:

```text
AuthViewModel.kt
```

Ubicaciones:

```kotlin
AuthUiState.Error("Ingresa un correo valido")
AuthUiState.Error("Usuario no encontrado. Verifica tu correo.")
```

### Cambiar usuarios demo

Archivo probable:

```text
LoginScreen.kt
```

Ubicacion:

```kotlin
listOf(
    "martin.herrera@stickrvault.com" to "Jefe",
    ...
)
```

### Cerrar sesion y regresar a Login

Archivos probables:

```text
AuthViewModel.kt
StickrVaultNavHost.kt
```

Separacion:

- `AuthViewModel.logout()` limpia sesion.
- `StickrVaultNavHost` navega a Login.

### Cambiar permisos por rol

Archivo depende del flujo:

```text
StickrVaultNavHost.kt
CatalogViewModel.kt
CatalogScreen.kt
```

Si el cambio es de scanner:

```text
StickrVaultNavHost.kt
```

Si el cambio es de inventario:

```text
CatalogViewModel.kt
```

## 24. Explicacion corta para defensa

Version de 30 segundos:

```text
El login se muestra en LoginScreen, que captura el correo y llama a AuthViewModel.login. El ViewModel valida el correo, cambia el estado a Loading y ejecuta LoginUseCase. Si encuentra usuario, actualiza currentUser y emite Success; la pantalla detecta ese estado y ejecuta onLoginSuccess para que el NavHost navegue. La sesion se guarda con SessionPreferences usando DataStore.
```

Version de 1 minuto:

```text
La autenticacion esta dividida por responsabilidades. LoginScreen solo muestra la UI, maneja el texto del correo y envia eventos. AuthViewModel administra el estado con StateFlow: Idle, Loading, Success y Error. Al iniciar, restaura una sesion guardada mediante AuthRepository y marca isSessionReady para que el NavHost sepa si debe iniciar en Home o Login. SessionPreferences usa DataStore para persistir user_id y user_email. El usuario se representa con AppUser y su rol con UserRole, lo que permite controlar permisos como bloquear acciones de inventario para auditores.
```

## 25. Checklist para dominar esta fase

- Puedo explicar que `LoginScreen` solo muestra UI y envia eventos.
- Puedo explicar para que sirve `email` con `remember`.
- Puedo explicar como la pantalla observa `uiState`.
- Puedo explicar por que se usa `LaunchedEffect` para navegar.
- Puedo explicar que hace `AuthViewModel.login`.
- Puedo explicar que hace `AuthViewModel.logout`.
- Puedo explicar para que sirve `currentUser`.
- Puedo explicar para que sirve `isSessionReady`.
- Puedo explicar los estados de `AuthUiState`.
- Puedo explicar como DataStore guarda la sesion.
- Puedo explicar que representa `AppUser`.
- Puedo explicar que roles existen en `UserRole`.
- Puedo ubicar donde cambiar texto, validacion, usuarios demo y mensajes de error.

## 26. Mini simulacros de esta fase

### Simulacro 1: cambiar el texto del boton Ingresar

Tipo de cambio: UI/texto.

Archivo:

```text
LoginScreen.kt
```

Ubicacion:

```kotlin
Text("Ingresar")
```

Explicacion:

```text
Es un cambio visual de texto, por eso se modifica la pantalla Compose y no el ViewModel.
```

### Simulacro 2: cambiar el padding del login

Tipo de cambio: UI/diseno.

Archivo:

```text
LoginScreen.kt
```

Ubicacion:

```kotlin
modifier = Modifier.fillMaxSize().padding(32.dp)
```

Explicacion:

```text
El margen interno de la pantalla se controla con Modifier.padding dentro del composable.
```

### Simulacro 3: validar que el correo contenga arroba

Tipo de cambio: validacion.

Archivo:

```text
AuthViewModel.kt
```

Ubicacion:

```kotlin
fun login(email: String)
```

Idea:

```kotlin
if (!email.contains("@")) {
    _uiState.value = AuthUiState.Error("El correo debe contener @")
    return
}
```

Explicacion:

```text
La regla se agrega en el ViewModel porque forma parte de la logica de presentacion del login, no del dibujo de la pantalla.
```

### Simulacro 4: agregar un nuevo chip demo

Tipo de cambio: UI/demo.

Archivo:

```text
LoginScreen.kt
```

Ubicacion:

```kotlin
listOf(
    "martin.herrera@stickrvault.com" to "Jefe",
    ...
)
```

Explicacion:

```text
Los chips solo cargan rapidamente un email en el campo. No modifican autenticacion ni base de datos.
```

### Simulacro 5: explicar por que la app no muestra Login al restaurar sesion

Respuesta:

```text
AuthViewModel tiene isSessionReady. Mientras ese valor es false, StickrVaultNavHost muestra un loading. Cuando termina la restauracion, decide si inicia en Home o Login segun currentUser.
```

## 27. Frase clave para recordar

```text
LoginScreen muestra y envia eventos; AuthViewModel decide el estado; SessionPreferences persiste la sesion; AppUser y UserRole representan usuario y permisos.
```
