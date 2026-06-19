# 4.5 Home

## Objetivo de esta fase

Entender como funciona la pantalla principal de StikerVault: resumen de inventario, accesos rapidos, usuario actual, cierre de sesion, dialogo de usuarios y actividad reciente.

En una defensa, esta seccion ayuda a explicar:

- Como se muestran metricas generales.
- Como se cargan productos, movimientos y usuarios.
- Como se calculan datos como stock critico y pendientes de sincronizacion.
- Como se navega desde Home hacia Catalogo, Scanner y Reportes.
- Como se muestra informacion segun el rol del usuario.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/home/HomeScreen.kt
app/src/main/java/com/example/myapplication/presentation/home/HomeViewModel.kt
app/src/main/java/com/example/myapplication/presentation/home/HomeUiState.kt
```

Flujo general:

```text
HomeScreen
-> HomeViewModel.loadHomeSummary()
-> GetProductsUseCase
-> GetStockMovementsUseCase
-> GetUsersUseCase
-> HomeUiState
-> HomeScreen redibuja metricas y listas
```

## 1. HomeScreen.kt

`HomeScreen` es la pantalla principal despues del login.

Firma:

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentUser: AppUser? = null,
    onLogout: () -> Unit = {},
    onNavigateCatalog: () -> Unit = {},
    onNavigateScanner: () -> Unit = {},
    onNavigateReports: () -> Unit = {}
)
```

Recibe:

- `viewModel`: carga y expone el resumen del Home.
- `currentUser`: usuario autenticado.
- `onLogout`: callback para cerrar sesion.
- `onNavigateCatalog`: callback para ir al catalogo.
- `onNavigateScanner`: callback para ir al scanner.
- `onNavigateReports`: callback para ir a reportes.

Idea clave:

```text
HomeScreen no navega directamente. Recibe callbacks desde StickrVaultNavHost.
```

## 2. Estado observado por la pantalla

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

La pantalla observa `HomeUiState`, que contiene:

- Total de productos.
- Productos con stock critico.
- Items pendientes de sincronizacion.
- Texto de ultima sincronizacion.
- Movimientos recientes.
- Usuarios.

Tambien tiene estado local:

```kotlin
var showUsersDialog by remember { mutableStateOf(false) }
```

`showUsersDialog` solo controla si se muestra el dialogo de usuarios, por eso vive en la pantalla.

Respuesta teorica posible:

```text
HomeScreen observa el estado del ViewModel con collectAsState. El dialogo de usuarios usa estado local porque solo afecta la UI de esta pantalla.
```

## 3. Carga del resumen

```kotlin
LaunchedEffect(Unit) { viewModel.loadHomeSummary() }
```

`LaunchedEffect(Unit)` ejecuta la carga cuando la pantalla entra en composicion.

Importante:

```text
HomeViewModel tambien carga en init, por lo que HomeScreen fuerza una recarga cuando se muestra.
```

Esto asegura que al volver al Home se actualicen las metricas.

Respuesta teorica posible:

```text
LaunchedEffect se usa para lanzar una accion asociada al ciclo de vida de la composicion. En este caso recarga el resumen del Home.
```

## 4. Estructura general de la pantalla

La pantalla usa `LazyColumn`:

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
)
```

`LazyColumn` permite mostrar secciones verticales:

1. Encabezado con nombre de app, bienvenida, rol y logout.
2. Tarjetas de metricas.
3. Acceso rapido.
4. Actividad reciente.

Cambios de examen probables:

- Cambiar padding general.
- Cambiar separacion entre secciones.
- Cambiar orden de secciones.
- Agregar una nueva seccion.

## 5. Encabezado

El encabezado muestra:

- Titulo `StickrVault`.
- Mensaje de bienvenida.
- Badge de rol.
- Boton de cerrar sesion.

Titulo:

```kotlin
Text(
    text = "StickrVault",
    style = MaterialTheme.typography.headlineSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
)
```

Subtitulo:

```kotlin
text = if (currentUser != null) "Bienvenido, ${currentUser.name}"
else "Gestion de inventario PANINI"
```

Boton logout:

```kotlin
IconButton(onClick = onLogout) {
    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesion")
}
```

Cambios de examen probables:

- Cambiar titulo de la app.
- Cambiar texto de bienvenida.
- Cambiar icono de logout.
- Ocultar logout para un rol.
- Cambiar color del titulo.

## 6. RoleBadge

`RoleBadge` muestra el rol del usuario.

```kotlin
@Composable
fun RoleBadge(role: UserRole)
```

Labels:

```kotlin
UserRole.WAREHOUSE_CHIEF -> "Jefe de Bodega"
UserRole.WAREHOUSE_OPERATOR -> "Operador"
UserRole.AUDITOR -> "Auditor"
```

Colores:

```kotlin
WAREHOUSE_CHIEF -> primary
WAREHOUSE_OPERATOR -> secondary
AUDITOR -> tertiary
```

La etiqueta se dibuja dentro de un `Surface`:

```kotlin
Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
```

Cambios de examen probables:

- Cambiar texto visible de un rol.
- Cambiar color por rol.
- Cambiar padding del badge.
- Ocultar badge.

Respuesta teorica posible:

```text
RoleBadge encapsula como se muestra visualmente un UserRole. Si cambia el texto o color del rol, se modifica este composable.
```

## 7. Tarjetas de metricas

Home muestra tres metricas en una `LazyRow`:

```kotlin
MetricCard("Total Productos", uiState.totalProducts.toString(),
    Icons.Default.Inventory, MaterialTheme.colorScheme.primary)

MetricCard("Stock Critico", uiState.criticalStockProducts.toString(),
    Icons.Default.Warning, MaterialTheme.colorScheme.error)

MetricCard("Pendientes Sync", uiState.pendingSyncItems.toString(),
    Icons.Default.Sync, MaterialTheme.colorScheme.secondary)
```

Metricas:

- `totalProducts`: cantidad total de productos.
- `criticalStockProducts`: productos con stock critico.
- `pendingSyncItems`: productos pendientes de sincronizacion.

Cambios de examen probables:

- Cambiar texto de una metrica.
- Cambiar icono.
- Cambiar color.
- Agregar una cuarta metrica.
- Cambiar ancho de las cards.

## 8. MetricCard

`MetricCard` es el componente reutilizable de metricas.

```kotlin
@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, color: Color)
```

Card:

```kotlin
Card(
    modifier = Modifier.width(140.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
)
```

Contenido:

- Icono.
- Valor grande.
- Titulo pequeno.

Cambios de examen probables:

- Cambiar ancho `140.dp`.
- Cambiar padding interno.
- Cambiar orden de icono y texto.
- Cambiar alpha del fondo.
- Cambiar estilo del valor.

Respuesta teorica posible:

```text
MetricCard encapsula la presentacion de una metrica. HomeScreen solo le pasa titulo, valor, icono y color.
```

## 9. Acceso Rapido

Home tiene una seccion:

```kotlin
Text("Acceso Rapido", style = MaterialTheme.typography.titleMedium)
```

Primera fila:

```kotlin
QuickAccessCard(..., "Catalogo", Icons.Default.Category, onNavigateCatalog)
QuickAccessCard(..., "Escaner", Icons.Default.QrCodeScanner, onNavigateScanner)
```

Segunda fila:

```kotlin
QuickAccessCard(..., "Reportes", Icons.Default.Assessment, onNavigateReports)
```

Y para jefe de bodega:

```kotlin
if (currentUser?.role == UserRole.WAREHOUSE_CHIEF) {
    QuickAccessCard(..., "Usuarios", Icons.Default.People,
        onClick = { showUsersDialog = true })
} else {
    Spacer(modifier = Modifier.weight(1f))
}
```

Regla:

```text
Solo WAREHOUSE_CHIEF ve el acceso a Usuarios.
```

Cambios de examen probables:

- Cambiar texto de accesos.
- Cambiar iconos.
- Ocultar Scanner para auditores.
- Mostrar Usuarios para otro rol.
- Cambiar destino de un acceso.

## 10. QuickAccessCard

`QuickAccessCard` representa una accion rapida.

```kotlin
@Composable
fun QuickAccessCard(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
)
```

Usa:

```kotlin
Card(modifier = modifier, onClick = onClick)
```

Contenido:

- Icono de 32.dp.
- Texto del label.
- Alineacion centrada.

Cambios de examen probables:

- Cambiar padding interno.
- Cambiar tamano de icono.
- Cambiar color del icono.
- Cambiar estilo del label.

Respuesta teorica posible:

```text
QuickAccessCard es un componente reutilizable para botones grandes de navegacion dentro del Home.
```

## 11. Dialogo de usuarios

Si `showUsersDialog` es verdadero:

```kotlin
UsersDialog(
    users = uiState.users,
    onDismiss = { showUsersDialog = false }
)
```

`UsersDialog` muestra:

- Titulo `Gestion de Usuarios`.
- Texto `Cargando usuarios...` si la lista esta vacia.
- Una card por cada usuario.
- Nombre, email y rol.
- Boton `Cerrar`.

Este dialogo solo se abre desde el acceso rapido `Usuarios`, visible para `WAREHOUSE_CHIEF`.

Cambios de examen probables:

- Cambiar titulo del dialogo.
- Cambiar texto cuando no hay usuarios.
- Mostrar solo usuarios de un rol.
- Cambiar orden de usuarios.
- Cambiar contenido de cada card.

## 12. Actividad Reciente

La seccion muestra:

```kotlin
Text("Actividad Reciente")
Icon(Icons.Default.ChevronRight, contentDescription = null)
```

Si no hay movimientos:

```kotlin
Text("Sin actividad reciente")
```

Si hay movimientos:

```kotlin
items(uiState.recentMovements) { RecentMovementItem(it) }
```

Cambios de examen probables:

- Cambiar titulo.
- Cambiar mensaje vacio.
- Cambiar cantidad de movimientos desde `HomeViewModel`.
- Cambiar formato de cada movimiento.

## 13. RecentMovementItem

Muestra un movimiento de stock.

```kotlin
@Composable
fun RecentMovementItem(movement: StockMovement)
```

Calcula texto y color segun tipo:

```kotlin
MovementType.ENTRY -> "+${movement.quantity}" to Color(0xFF2E7D32)
MovementType.EXIT -> "-${movement.quantity}" to MaterialTheme.colorScheme.error
MovementType.ADJUSTMENT -> "~${movement.quantity}" to MaterialTheme.colorScheme.primary
```

Muestra:

- Nombre del producto o ID si no hay nombre.
- Usuario que hizo el movimiento.
- Tiempo relativo.
- Cantidad con signo.

Producto:

```kotlin
movement.productName ?: movement.productId
```

Detalle:

```kotlin
"${movement.userName} · ${formatRelativeTime(movement.timestamp)}"
```

Cambios de examen probables:

- Cambiar color de entradas.
- Cambiar signos `+`, `-`, `~`.
- Mostrar fecha absoluta.
- Mostrar solo movimientos de salida.
- Cambiar padding de cada card.

## 14. formatRelativeTime

Funcion:

```kotlin
fun formatRelativeTime(timestamp: Long): String
```

Calcula diferencia entre ahora y el timestamp:

```kotlin
val diff = System.currentTimeMillis() - timestamp
val minutes = diff / 60_000
val hours = minutes / 60
val days = hours / 24
```

Devuelve:

- Menos de 1 minuto: `Ahora mismo`.
- Menos de 60 minutos: `Hace X min`.
- Menos de 24 horas: `Hace Xh`.
- Mas de 24 horas: `Hace X dias`.

Cambios de examen probables:

- Cambiar textos.
- Mostrar minutos como `X minutos`.
- Mostrar fecha con formato.
- Cambiar limite de horas o dias.

## 15. SummaryCard

Existe un componente:

```kotlin
@Composable
fun SummaryCard(title: String, value: String)
```

Actualmente no se usa en el Home principal.

Puede servir para cambios o practicas si se necesita una card simple de resumen.

Idea clave:

```text
No todo composable definido necesariamente esta en uso. Hay que revisar donde se llama.
```

## 16. HomeViewModel.kt

`HomeViewModel` carga datos para el dashboard.

Constructor:

```kotlin
class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel()
```

Recibe tres casos de uso:

- `GetProductsUseCase`: productos.
- `GetStockMovementsUseCase`: movimientos recientes.
- `GetUsersUseCase`: usuarios.

Idea clave:

```text
HomeViewModel no consulta Room ni Supabase directamente. Usa casos de uso.
```

## 17. Estado del HomeViewModel

```kotlin
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
```

Se expone como `StateFlow` de solo lectura.

La pantalla puede observar, pero no modificar directamente.

Respuesta teorica posible:

```text
El ViewModel mantiene MutableStateFlow privado y expone StateFlow publico para proteger el estado.
```

## 18. Carga inicial

```kotlin
init { loadHomeSummary() }
```

Cuando se crea el ViewModel, carga el resumen.

Ademas, HomeScreen vuelve a llamar:

```kotlin
LaunchedEffect(Unit) { viewModel.loadHomeSummary() }
```

Esto puede causar una recarga adicional, pero mantiene la pantalla actualizada cuando entra en composicion.

## 19. loadHomeSummary

```kotlin
fun loadHomeSummary() {
    viewModelScope.launch {
        try {
            val products = getProductsUseCase()
            val movements = getStockMovementsUseCase(limit = 5)
            val users = getUsersUseCase()
            _uiState.value = HomeUiState(
                totalProducts = products.size,
                criticalStockProducts = products.count { it.currentStock <= it.minimumStock },
                pendingSyncItems = products.count { !it.isSynced },
                lastSyncText = "Sincronizado con Supabase",
                recentMovements = movements,
                users = users
            )
        } catch (e: Exception) {
            _uiState.value = HomeUiState(lastSyncText = "Error al sincronizar")
        }
    }
}
```

Funcionamiento:

1. Lanza corrutina en `viewModelScope`.
2. Obtiene productos.
3. Obtiene 5 movimientos recientes.
4. Obtiene usuarios.
5. Calcula metricas.
6. Actualiza `HomeUiState`.
7. Si falla, muestra error en texto de sincronizacion.

Calculos importantes:

```kotlin
totalProducts = products.size
```

```kotlin
criticalStockProducts = products.count { it.currentStock <= it.minimumStock }
```

```kotlin
pendingSyncItems = products.count { !it.isSynced }
```

Cambios de examen probables:

- Cambiar limite de movimientos de 5 a 10.
- Cambiar regla de stock critico.
- Agregar metrica de stock total.
- Cambiar texto de sincronizacion.
- Manejar error mostrando otro mensaje.

Respuesta teorica posible:

```text
HomeViewModel calcula metricas a partir de productos y movimientos obtenidos mediante casos de uso, luego expone todo en HomeUiState para que la pantalla solo renderice.
```

## 20. HomeUiState.kt

`HomeUiState` es un `data class` con valores por defecto.

```kotlin
data class HomeUiState(
    val totalProducts: Int = 0,
    val criticalStockProducts: Int = 0,
    val pendingSyncItems: Int = 0,
    val lastSyncText: String = "Sin sincronizacion reciente",
    val recentMovements: List<StockMovement> = emptyList(),
    val users: List<AppUser> = emptyList()
)
```

Campos:

- `totalProducts`: cantidad total de productos.
- `criticalStockProducts`: productos en estado critico.
- `pendingSyncItems`: productos pendientes de sincronizacion.
- `lastSyncText`: texto de estado de sincronizacion.
- `recentMovements`: movimientos recientes.
- `users`: usuarios para el dialogo.

Ventaja de valores por defecto:

```text
La pantalla puede renderizar sin esperar datos, usando ceros y listas vacias inicialmente.
```

Respuesta teorica posible:

```text
HomeUiState agrupa todos los datos que necesita HomeScreen. Al tener valores por defecto, la pantalla tiene un estado inicial seguro.
```

## 21. Relacion con navegacion

Home recibe los callbacks desde `StickrVaultNavHost`.

```kotlin
onNavigateCatalog = { navController.navigate(Routes.Catalog) }
onNavigateScanner = { navController.navigate(Routes.Scanner) }
onNavigateReports = { navController.navigate(Routes.Reports) }
```

Por eso:

```text
HomeScreen muestra botones.
StickrVaultNavHost define a donde navegan.
```

Respuesta teorica posible:

```text
La navegacion se mantiene fuera de HomeScreen mediante callbacks, lo que hace que la pantalla sea mas reutilizable y facil de probar.
```

## 22. Cambios de examen probables en Home

### Cambiar texto de bienvenida

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
"Bienvenido, ${currentUser.name}"
```

### Cambiar padding general

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
contentPadding = PaddingValues(16.dp)
```

### Cambiar separacion entre secciones

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
verticalArrangement = Arrangement.spacedBy(16.dp)
```

### Cambiar limite de movimientos recientes

Archivo:

```text
HomeViewModel.kt
```

Ubicacion:

```kotlin
val movements = getStockMovementsUseCase(limit = 5)
```

### Agregar una metrica nueva

Archivos:

```text
HomeUiState.kt
HomeViewModel.kt
HomeScreen.kt
```

Pasos:

1. Agregar campo en `HomeUiState`.
2. Calcular valor en `HomeViewModel`.
3. Mostrarlo con `MetricCard` en `HomeScreen`.

### Cambiar acceso de Usuarios para otro rol

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
if (currentUser?.role == UserRole.WAREHOUSE_CHIEF)
```

### Cambiar mensaje sin actividad

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
Text("Sin actividad reciente")
```

### Cambiar regla de stock critico

Archivos posibles:

```text
HomeViewModel.kt
CatalogScreen.kt
```

Si cambia la metrica del Home:

```kotlin
products.count { it.currentStock <= it.minimumStock }
```

Si cambia la etiqueta en cards:

```text
ProductCard en CatalogScreen.kt
```

## 23. Explicacion corta para defensa

Version de 30 segundos:

```text
HomeScreen muestra el dashboard principal con bienvenida, rol, metricas, accesos rapidos y movimientos recientes. HomeViewModel carga productos, movimientos y usuarios usando casos de uso, calcula metricas como total de productos, stock critico y pendientes de sincronizacion, y expone todo en HomeUiState.
```

Version de 1 minuto:

```text
La pantalla Home esta dividida en UI y estado. HomeScreen observa HomeUiState desde el ViewModel y renderiza secciones en una LazyColumn. Muestra el usuario actual, su rol, metricas en MetricCard, accesos rapidos con callbacks de navegacion y actividad reciente. HomeViewModel usa GetProductsUseCase, GetStockMovementsUseCase y GetUsersUseCase para obtener datos, calcular metricas y actualizar el estado. HomeUiState agrupa los datos necesarios para que la pantalla pueda dibujarse de forma simple.
```

## 24. Checklist para dominar esta fase

- Puedo explicar que muestra `HomeScreen`.
- Puedo explicar por que Home recibe callbacks de navegacion.
- Puedo explicar que datos contiene `HomeUiState`.
- Puedo explicar como `HomeViewModel` calcula las metricas.
- Puedo explicar de donde sale `totalProducts`.
- Puedo explicar de donde sale `criticalStockProducts`.
- Puedo explicar de donde sale `pendingSyncItems`.
- Puedo ubicar donde cambiar limite de movimientos recientes.
- Puedo ubicar donde cambiar textos del Home.
- Puedo ubicar donde cambiar cards de metricas.
- Puedo ubicar donde cambiar accesos rapidos.
- Puedo explicar por que Usuarios solo aparece para jefe de bodega.
- Puedo explicar que hace `formatRelativeTime`.
- Puedo explicar como se muestra el rol con `RoleBadge`.

## 25. Mini simulacros de esta fase

### Simulacro 1: cambiar cantidad de movimientos recientes de 5 a 10

Tipo de cambio: dato/resumen.

Archivo:

```text
HomeViewModel.kt
```

Ubicacion:

```kotlin
val movements = getStockMovementsUseCase(limit = 5)
```

Explicacion:

```text
El limite de movimientos se define al solicitar datos desde el ViewModel, porque afecta cuantos datos se cargan para el Home.
```

### Simulacro 2: cambiar padding del Home

Tipo de cambio: UI/diseno.

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
contentPadding = PaddingValues(16.dp)
```

Explicacion:

```text
El margen interno general de la pantalla se define en la LazyColumn principal.
```

### Simulacro 3: cambiar texto "Acceso Rapido"

Tipo de cambio: texto visible.

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
Text("Acceso Rapido")
```

Explicacion:

```text
Es un texto visual de la pantalla Home, por eso se cambia directamente en el composable.
```

### Simulacro 4: agregar metrica de stock total

Tipo de cambio: nueva metrica.

Archivos:

```text
HomeUiState.kt
HomeViewModel.kt
HomeScreen.kt
```

Idea:

```text
Agregar totalStock en HomeUiState, calcular products.sumOf { it.currentStock } en HomeViewModel y mostrar MetricCard en HomeScreen.
```

Explicacion:

```text
Una nueva metrica necesita campo de estado, calculo en ViewModel y componente visual en pantalla.
```

### Simulacro 5: mostrar Usuarios tambien para Operador

Tipo de cambio: permisos/UI.

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
if (currentUser?.role == UserRole.WAREHOUSE_CHIEF)
```

Idea:

```text
Agregar condicion OR para WAREHOUSE_OPERATOR.
```

Explicacion:

```text
La visibilidad del acceso Usuarios se decide en la pantalla segun el rol actual.
```

### Simulacro 6: cambiar formato de tiempo relativo

Tipo de cambio: formato visual.

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
fun formatRelativeTime(timestamp: Long)
```

Explicacion:

```text
El texto de fecha relativa se calcula en una funcion auxiliar usada por RecentMovementItem.
```

### Simulacro 7: cambiar color de entradas de stock

Tipo de cambio: UI/estado visual.

Archivo:

```text
HomeScreen.kt
```

Ubicacion:

```kotlin
MovementType.ENTRY -> "+${movement.quantity}" to Color(0xFF2E7D32)
```

Explicacion:

```text
El color de cada movimiento se calcula dentro de RecentMovementItem segun MovementType.
```

## 26. Frase clave para recordar

```text
HomeScreen muestra el dashboard; HomeViewModel calcula el resumen; HomeUiState guarda los datos listos para pintar.
```
