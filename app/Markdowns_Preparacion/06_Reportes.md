# 4.6 Reportes

## Objetivo de esta fase

Entender como StikerVault muestra reportes de inventario: metricas generales y movimientos recientes.

En una defensa, esta seccion ayuda a explicar:

- Como se cargan productos y movimientos para reportes.
- Como se calculan metricas.
- Como se muestran metricas en una grilla.
- Como se muestra la lista de movimientos recientes.
- Donde modificar textos, limites, tarjetas, colores o calculos.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/reports/ReportsScreen.kt
app/src/main/java/com/example/myapplication/presentation/reports/ReportsViewModel.kt
app/src/main/java/com/example/myapplication/presentation/reports/ReportUiState.kt
```

Flujo general:

```text
ReportsScreen
-> ReportsViewModel.loadReports()
-> GetProductsUseCase
-> GetStockMovementsUseCase
-> ReportsUiState
-> ReportsScreen muestra metricas y movimientos
```

## 1. ReportsScreen.kt

`ReportsScreen` es la pantalla visual de reportes.

Firma:

```kotlin
@Composable
fun ReportsScreen(viewModel: ReportsViewModel)
```

Recibe solo su ViewModel.

No recibe usuario ni callbacks de navegacion porque esta pantalla no tiene acciones complejas de navegacion internas.

Idea clave:

```text
ReportsScreen observa estado y renderiza. ReportsViewModel calcula los datos del reporte.
```

## 2. Estado observado

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

La pantalla observa `ReportsUiState`, que contiene:

- Total de productos.
- Total de movimientos.
- Productos con stock critico.
- Producto con mayor stock.
- Movimientos recientes.

Respuesta teorica posible:

```text
ReportsScreen usa collectAsState para convertir el StateFlow del ViewModel en estado observable por Compose.
```

## 3. Carga de reportes

```kotlin
LaunchedEffect(Unit) { viewModel.loadReports() }
```

Cuando la pantalla se muestra, se ejecuta la carga del reporte.

`ReportsViewModel` tambien llama `loadReports()` en `init`, por lo que puede haber una recarga adicional al entrar a la pantalla.

Idea clave:

```text
LaunchedEffect permite ejecutar una accion cuando la pantalla entra en composicion.
```

Respuesta teorica posible:

```text
La pantalla dispara loadReports con LaunchedEffect para actualizar las metricas al abrir reportes.
```

## 4. Estructura visual

La pantalla usa:

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
)
```

Secciones:

1. Titulo `Reportes de Inventario`.
2. Grilla de metricas.
3. Titulo `Movimientos Recientes`.
4. Mensaje vacio o lista de movimientos.

Cambios de examen probables:

- Cambiar padding general.
- Cambiar separacion entre secciones.
- Cambiar titulo.
- Cambiar orden de secciones.

## 5. Titulo de reportes

```kotlin
Text(
    text = "Reportes de Inventario",
    style = MaterialTheme.typography.headlineSmall,
    fontWeight = FontWeight.Bold
)
```

Cambios probables:

- Cambiar texto.
- Cambiar estilo.
- Cambiar peso de fuente.
- Agregar color.

Archivo:

```text
ReportsScreen.kt
```

## 6. Grilla de metricas

La pantalla crea una lista de pares:

```kotlin
val metrics = listOf(
    "Total Productos" to uiState.totalProducts.toString(),
    "Movimientos" to uiState.totalMovements.toString(),
    "Stock Critico" to uiState.criticalStockProducts.toString(),
    "Mayor Stock" to uiState.mostStockedProductName
)
```

Cada par representa:

```text
titulo -> valor
```

Metricas actuales:

- `Total Productos`: cantidad total de productos.
- `Movimientos`: cantidad de movimientos cargados.
- `Stock Critico`: productos con stock menor o igual al minimo.
- `Mayor Stock`: nombre del producto con mayor stock.

Cambios de examen probables:

- Cambiar texto de una metrica.
- Agregar una nueva metrica.
- Quitar una metrica.
- Cambiar orden de metricas.
- Cambiar el valor mostrado.

## 7. LazyVerticalGrid

Las metricas se muestran en una grilla:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier.height(220.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    userScrollEnabled = false
)
```

Detalles:

- `GridCells.Fixed(2)`: dos columnas.
- `height(220.dp)`: altura fija.
- `spacedBy(12.dp)`: separacion horizontal y vertical.
- `userScrollEnabled = false`: no permite scroll interno.

Cambios de examen probables:

- Cambiar de 2 a 1 o 3 columnas.
- Cambiar altura de la grilla.
- Cambiar separacion entre tarjetas.
- Permitir scroll interno.

Respuesta teorica posible:

```text
LazyVerticalGrid organiza las metricas en columnas. En este caso se usan dos columnas fijas y scroll deshabilitado porque la grilla esta dentro de una LazyColumn.
```

## 8. ReportMetricCard

`ReportMetricCard` muestra una metrica.

Firma:

```kotlin
@Composable
fun ReportMetricCard(title: String, value: String)
```

Contenido:

```kotlin
Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, ...)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, ...)
    }
}
```

Muestra:

- Titulo pequeno.
- Valor destacado.

Cambios de examen probables:

- Cambiar padding interno.
- Cambiar color del valor.
- Cambiar estilo del titulo.
- Agregar icono.
- Cambiar borde o color de la card.

Respuesta teorica posible:

```text
ReportMetricCard encapsula la forma visual de cada tarjeta de metrica. La pantalla solo le pasa titulo y valor.
```

## 9. Movimientos recientes

Titulo:

```kotlin
Text(
    text = "Movimientos Recientes",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.SemiBold
)
```

Si no hay movimientos:

```kotlin
Text(
    text = "Sin movimientos registrados",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

Si hay movimientos:

```kotlin
items(uiState.recentMovements) { MovementCard(it) }
```

Cambios de examen probables:

- Cambiar titulo.
- Cambiar mensaje vacio.
- Mostrar solo movimientos de entrada o salida.
- Cambiar cantidad de movimientos desde ViewModel.
- Cambiar formato visual de cada movimiento.

## 10. MovementCard

`MovementCard` representa un movimiento de stock.

Firma:

```kotlin
@Composable
fun MovementCard(movement: StockMovement)
```

Calcula texto y color segun tipo:

```kotlin
val (quantityText, quantityColor) = when (movement.movementType) {
    MovementType.ENTRY -> "↑ +${movement.quantity}" to Color(0xFF2E7D32)
    MovementType.EXIT -> "↓ -${movement.quantity}" to MaterialTheme.colorScheme.error
    MovementType.ADJUSTMENT -> "~ ${movement.quantity}" to MaterialTheme.colorScheme.primary
}
```

Reglas:

- Entrada: flecha arriba, signo positivo, verde.
- Salida: flecha abajo, signo negativo, color de error.
- Ajuste: signo `~`, color primario.

Contenido visible:

```kotlin
movement.productName ?: movement.productId
```

Si hay nombre del producto, lo muestra. Si no, muestra el ID.

Detalle:

```kotlin
"${movement.userName} · ${formatRelativeTime(movement.timestamp)}"
```

Muestra usuario y tiempo relativo.

Cambios de examen probables:

- Cambiar flechas por texto.
- Cambiar color de entradas.
- Cambiar padding de la card.
- Mostrar ID siempre.
- Mostrar tipo de movimiento.
- Cambiar formato de tiempo.

## 11. Uso de formatRelativeTime

Reports importa:

```kotlin
import com.example.myapplication.presentation.home.formatRelativeTime
```

Esto reutiliza la funcion definida en `HomeScreen.kt`.

Ventaja:

```text
Evita duplicar la logica de tiempo relativo en Reportes.
```

Punto importante para defensa:

```text
Aunque la funcion esta en presentation.home, Reportes la reutiliza para mostrar fechas relativas de movimientos.
```

Si piden cambiar el formato de fecha para Home y Reportes, se puede cambiar esa funcion compartida.

## 12. ReportsViewModel.kt

`ReportsViewModel` calcula el estado de reportes.

Constructor:

```kotlin
class ReportsViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase
) : ViewModel()
```

Recibe:

- `GetProductsUseCase`: obtiene productos.
- `GetStockMovementsUseCase`: obtiene movimientos.

No recibe `GetUsersUseCase` porque reportes no necesita listar usuarios.

Idea clave:

```text
ReportsViewModel no consulta DAO ni API directamente. Usa casos de uso.
```

## 13. Estado del ReportsViewModel

```kotlin
private val _uiState = MutableStateFlow(ReportsUiState())
val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
```

Patron:

- `_uiState` es mutable y privado.
- `uiState` es publico y solo lectura.

Respuesta teorica posible:

```text
El ViewModel protege el estado exponiendo StateFlow de solo lectura a la pantalla.
```

## 14. Carga inicial

```kotlin
init { loadReports() }
```

Cuando se crea el ViewModel, carga los datos.

La pantalla tambien llama `loadReports()` con `LaunchedEffect`, lo que actualiza datos al entrar.

## 15. loadReports

```kotlin
fun loadReports() {
    viewModelScope.launch {
        try {
            val products = getProductsUseCase()
            val movements = getStockMovementsUseCase(limit = 10)
            _uiState.value = ReportsUiState(
                totalProducts = products.size,
                totalMovements = movements.size,
                criticalStockProducts = products.count { it.currentStock <= it.minimumStock },
                mostStockedProductName = products.maxByOrNull { it.currentStock }?.name ?: "Sin datos",
                recentMovements = movements
            )
        } catch (e: Exception) {
            _uiState.value = ReportsUiState()
        }
    }
}
```

Funcionamiento:

1. Lanza corrutina en `viewModelScope`.
2. Obtiene productos.
3. Obtiene 10 movimientos recientes.
4. Calcula total de productos.
5. Calcula total de movimientos cargados.
6. Cuenta productos con stock critico.
7. Encuentra producto con mayor stock.
8. Guarda movimientos recientes.
9. Si falla, reinicia a estado por defecto.

Calculos clave:

```kotlin
products.size
```

```kotlin
products.count { it.currentStock <= it.minimumStock }
```

```kotlin
products.maxByOrNull { it.currentStock }?.name ?: "Sin datos"
```

Cambios de examen probables:

- Cambiar limite de movimientos de 10 a 5.
- Cambiar regla de stock critico.
- Mostrar producto con menor stock.
- Agregar stock total.
- Cambiar texto `Sin datos`.
- Cambiar manejo de error.

Respuesta teorica posible:

```text
ReportsViewModel obtiene datos mediante casos de uso y calcula metricas antes de exponerlas en ReportsUiState.
```

## 16. ReportsUiState

Archivo llamado:

```text
ReportUiState.kt
```

Clase definida:

```kotlin
data class ReportsUiState(
    val totalProducts: Int = 0,
    val totalMovements: Int = 0,
    val criticalStockProducts: Int = 0,
    val mostStockedProductName: String = "Sin datos",
    val recentMovements: List<StockMovement> = emptyList()
)
```

Campos:

- `totalProducts`: total de productos.
- `totalMovements`: total de movimientos cargados para el reporte.
- `criticalStockProducts`: productos con stock critico.
- `mostStockedProductName`: producto con mayor stock.
- `recentMovements`: lista de movimientos recientes.

Valores por defecto:

```text
0 para numeros, "Sin datos" para texto, lista vacia para movimientos.
```

Ventaja:

```text
La pantalla puede renderizar aunque los datos todavia no hayan cargado.
```

Respuesta teorica posible:

```text
ReportsUiState agrupa todos los datos necesarios para pintar la pantalla de reportes con valores iniciales seguros.
```

## 17. Relacion con Home

Reportes se parece a Home porque ambos:

- Calculan metricas.
- Muestran movimientos recientes.
- Usan casos de uso.
- Reutilizan formato de tiempo relativo.

Diferencias:

- Home tiene accesos rapidos y usuario actual.
- Reportes se enfoca en metricas y movimientos.
- Reportes carga 10 movimientos; Home carga 5.
- Reportes muestra grilla de 2 columnas.

## 18. Cambios de examen probables en Reportes

### Cambiar titulo de la pantalla

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
Text(text = "Reportes de Inventario")
```

### Cambiar padding general

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
contentPadding = PaddingValues(16.dp)
```

### Cambiar columnas de metricas

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
columns = GridCells.Fixed(2)
```

### Cambiar limite de movimientos

Archivo:

```text
ReportsViewModel.kt
```

Ubicacion:

```kotlin
val movements = getStockMovementsUseCase(limit = 10)
```

### Cambiar producto mostrado en metrica

Archivo:

```text
ReportsViewModel.kt
```

Mayor stock:

```kotlin
products.maxByOrNull { it.currentStock }
```

Menor stock:

```kotlin
products.minByOrNull { it.currentStock }
```

### Agregar una nueva metrica

Archivos:

```text
ReportUiState.kt
ReportsViewModel.kt
ReportsScreen.kt
```

Pasos:

1. Agregar campo en `ReportsUiState`.
2. Calcular valor en `ReportsViewModel`.
3. Agregarlo a la lista `metrics`.

### Cambiar color de movimientos

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
MovementCard -> when (movement.movementType)
```

### Cambiar mensaje sin movimientos

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
Text(text = "Sin movimientos registrados")
```

## 19. Explicacion corta para defensa

Version de 30 segundos:

```text
ReportsScreen muestra reportes de inventario con una grilla de metricas y una lista de movimientos recientes. ReportsViewModel obtiene productos y movimientos mediante casos de uso, calcula total de productos, total de movimientos, stock critico y producto con mayor stock, y expone esos datos en ReportsUiState.
```

Version de 1 minuto:

```text
La pantalla de reportes esta separada en UI y estado. ReportsScreen observa ReportsUiState con collectAsState y renderiza el titulo, una grilla de metricas con LazyVerticalGrid y movimientos recientes con MovementCard. ReportsViewModel carga productos y movimientos en viewModelScope usando GetProductsUseCase y GetStockMovementsUseCase. Luego calcula las metricas y actualiza el estado. ReportsUiState contiene valores por defecto para que la pantalla pueda mostrarse aun antes de cargar datos.
```

## 20. Checklist para dominar esta fase

- Puedo explicar que muestra `ReportsScreen`.
- Puedo explicar como se crean las metricas.
- Puedo explicar por que se usa `LazyVerticalGrid`.
- Puedo explicar que hace `ReportMetricCard`.
- Puedo explicar que hace `MovementCard`.
- Puedo explicar de donde sale `formatRelativeTime`.
- Puedo explicar que casos de uso usa `ReportsViewModel`.
- Puedo explicar como se calcula `totalProducts`.
- Puedo explicar como se calcula `criticalStockProducts`.
- Puedo explicar como se calcula `mostStockedProductName`.
- Puedo ubicar donde cambiar limite de movimientos.
- Puedo ubicar donde agregar una metrica nueva.
- Puedo ubicar donde cambiar texto o color de movimientos.

## 21. Mini simulacros de esta fase

### Simulacro 1: cambiar movimientos recientes de 10 a 5

Tipo de cambio: dato/lista.

Archivo:

```text
ReportsViewModel.kt
```

Ubicacion:

```kotlin
val movements = getStockMovementsUseCase(limit = 10)
```

Explicacion:

```text
El limite de movimientos se define cuando el ViewModel solicita datos al caso de uso.
```

### Simulacro 2: cambiar grilla a una columna

Tipo de cambio: UI/diseno.

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
columns = GridCells.Fixed(2)
```

Cambio:

```kotlin
columns = GridCells.Fixed(1)
```

Explicacion:

```text
La cantidad de columnas de metricas se controla en LazyVerticalGrid.
```

### Simulacro 3: mostrar producto con menor stock

Tipo de cambio: metrica.

Archivo:

```text
ReportsViewModel.kt
```

Ubicacion:

```kotlin
products.maxByOrNull { it.currentStock }
```

Idea:

```kotlin
products.minByOrNull { it.currentStock }?.name ?: "Sin datos"
```

Tambien cambiar label:

```text
"Mayor Stock" -> "Menor Stock"
```

Explicacion:

```text
El calculo de la metrica vive en el ViewModel y el texto visible vive en la lista de metricas de ReportsScreen.
```

### Simulacro 4: agregar metrica de stock total

Tipo de cambio: nueva metrica.

Archivos:

```text
ReportUiState.kt
ReportsViewModel.kt
ReportsScreen.kt
```

Idea:

```text
Agregar totalStock, calcular products.sumOf { it.currentStock } y mostrarlo en metrics.
```

Explicacion:

```text
Una metrica nueva necesita campo de estado, calculo en ViewModel y representacion en pantalla.
```

### Simulacro 5: cambiar mensaje sin movimientos

Tipo de cambio: texto visible.

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
Text(text = "Sin movimientos registrados")
```

Explicacion:

```text
Es un mensaje visual de estado vacio, por eso se cambia en la pantalla.
```

### Simulacro 6: cambiar color de entradas

Tipo de cambio: UI/estado visual.

Archivo:

```text
ReportsScreen.kt
```

Ubicacion:

```kotlin
MovementType.ENTRY -> "↑ +${movement.quantity}" to Color(0xFF2E7D32)
```

Explicacion:

```text
MovementCard decide color y texto segun el tipo de movimiento.
```

## 22. Frase clave para recordar

```text
ReportsScreen dibuja metricas y movimientos; ReportsViewModel calcula el reporte; ReportsUiState guarda los valores listos para mostrar.
```
