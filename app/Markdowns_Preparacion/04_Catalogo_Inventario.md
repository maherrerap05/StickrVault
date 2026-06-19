# 4.4 Catalogo e inventario

## Objetivo de esta fase

Entender como StikerVault muestra productos, permite buscar, filtrar, agregar productos, ajustar stock y sincronizar datos entre Room y Supabase.

Esta es una de las secciones mas importantes para la defensa porque concentra muchos cambios probables de examen:

- Modificar cards de productos.
- Cambiar padding, colores, textos o estados vacios.
- Cambiar validaciones de formularios.
- Cambiar filtros o busqueda.
- Cambiar reglas de stock.
- Cambiar permisos por rol.
- Cambiar valores por defecto como stock minimo.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/catalog/CatalogScreen.kt
app/src/main/java/com/example/myapplication/presentation/catalog/CatalogViewModel.kt
app/src/main/java/com/example/myapplication/presentation/catalog/CatalogUiState.kt
app/src/main/java/com/example/myapplication/presentation/catalog/AddProductDraft.kt
app/src/main/java/com/example/myapplication/domain/model/Product.kt
app/src/main/java/com/example/myapplication/domain/model/ProductCategory.kt
app/src/main/java/com/example/myapplication/data/repository/ProductRepositoryImpl.kt
app/src/main/java/com/example/myapplication/data/local/dao/ProductDao.kt
```

Flujo general:

```text
CatalogScreen
-> CatalogViewModel
-> UseCases
-> ProductRepositoryImpl
-> SupabaseApiService / ProductDao
-> Room local / API remota
```

## 1. CatalogScreen.kt

`CatalogScreen` es la pantalla visual del catalogo.

Firma:

```kotlin
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    currentUser: AppUser? = null
)
```

Recibe:

- `viewModel`: administra productos, busqueda, filtros, formulario y guardado.
- `currentUser`: usuario actual para controlar permisos.

Idea clave:

```text
CatalogScreen muestra la UI y envia eventos. CatalogViewModel contiene las reglas y operaciones.
```

## 2. Estados observados en la pantalla

```kotlin
val uiState by viewModel.uiState.collectAsState()
val addProductDraft by viewModel.addProductDraft.collectAsState()
val isAddProductDialogVisible by viewModel.isAddProductDialogVisible.collectAsState()
val catalogProducts by viewModel.catalogProducts.collectAsState()
```

La pantalla observa cuatro estados:

- `uiState`: loading, success, error o empty.
- `addProductDraft`: datos temporales del formulario.
- `isAddProductDialogVisible`: si el dialogo esta abierto.
- `catalogProducts`: productos disponibles para verificar existencia.

Ademas tiene un estado local:

```kotlin
var searchText by remember { mutableStateOf("") }
```

`searchText` vive en la pantalla porque solo controla el texto escrito en el campo de busqueda.

Respuesta teorica posible:

```text
La pantalla observa StateFlow desde el ViewModel para redibujarse cuando cambian los datos. El texto de busqueda es estado local porque pertenece al input visual.
```

## 3. Permisos de edicion

```kotlin
val canEdit = currentUser?.role != UserRole.AUDITOR
```

Regla:

```text
AUDITOR -> no puede editar
Jefe u operador -> puede editar
Usuario null -> la condicion queda true porque null != AUDITOR
```

`canEdit` controla si se muestra el boton flotante para agregar producto.

```kotlin
if (canEdit) {
    FloatingActionButton(onClick = viewModel::openAddProductDialog) {
        Icon(Icons.Default.Add, contentDescription = "Agregar producto")
    }
}
```

Importante:

```text
La pantalla oculta la accion visual, pero CatalogViewModel tambien valida el rol antes de guardar.
```

Esto es una doble proteccion:

- UI: no muestra boton.
- ViewModel: bloquea guardado si el rol es auditor.

## 4. Dialogo de producto

Si el estado indica que el dialogo esta visible:

```kotlin
if (isAddProductDialogVisible) {
    AddProductDialog(...)
}
```

El dialogo recibe:

- `draft`: datos temporales.
- `products`: lista para encontrar producto existente.
- `onDismiss`: cerrar sin borrar necesariamente.
- `onCancel`: cancelar y limpiar.
- `onDraftChange`: actualizar draft.
- `onVerify`: verificar si existe producto.
- `onConfirm`: guardar o actualizar.

Idea clave:

```text
AddProductDialog no guarda directamente. Llama a onConfirm y CatalogScreen delega a CatalogViewModel.saveManualProduct.
```

## 5. Busqueda de productos

Campo:

```kotlin
OutlinedTextField(
    value = searchText,
    onValueChange = {
        searchText = it
        if (it.isBlank()) {
            viewModel.loadProducts()
        } else {
            viewModel.searchProducts(it)
        }
    },
    label = { Text("Buscar producto") }
)
```

Funcionamiento:

- Si el texto queda vacio, recarga todos los productos.
- Si hay texto, ejecuta busqueda.

Ruta:

```text
CatalogScreen
-> CatalogViewModel.searchProducts
-> SearchProductsUseCase
-> ProductRepositoryImpl.searchProducts
```

Cambios de examen probables:

- Cambiar label del buscador.
- Buscar solo cuando el texto tenga minimo 3 caracteres.
- Cambiar busqueda para incluir otra propiedad.
- Limpiar filtro cuando se busca.

## 6. Indicador offline

```kotlin
if (successState?.isOffline == true || (successState?.pendingSyncCount ?: 0) > 0) {
    AssistChip(
        label = {
            Text("Modo offline · Pendientes: ${successState?.pendingSyncCount ?: 0}")
        }
    )
}
```

Muestra un chip cuando hay productos pendientes de sincronizacion.

Esto depende de:

- `isOffline`
- `pendingSyncCount`

Estos valores vienen desde `CatalogUiState.Success`.

## 7. Filtros por categoria

El filtro usa `LazyRow` y `FilterChip`.

Primero aparece `Todos`:

```kotlin
FilterChip(
    selected = activeFilter == null,
    onClick = {
        searchText = ""
        viewModel.loadProducts()
    },
    label = { Text("Todos") }
)
```

Luego se crea un chip por cada categoria:

```kotlin
items(ProductCategory.entries) { category ->
    FilterChip(
        selected = activeFilter == category,
        onClick = {
            searchText = ""
            viewModel.filterByCategory(category)
        },
        label = { Text(category.displayName()) }
    )
}
```

Cambios de examen probables:

- Cambiar texto `Todos`.
- Ocultar una categoria.
- Agregar nueva categoria.
- Cambiar orden de categorias.
- Cambiar nombres visibles en `displayName`.

## 8. Estados visuales del catalogo

La pantalla usa `when (val state = uiState)`:

```kotlin
when (val state = uiState) {
    is CatalogUiState.Loading -> ...
    is CatalogUiState.Empty -> ...
    is CatalogUiState.Error -> ...
    is CatalogUiState.Success -> ...
}
```

Estados:

- `Loading`: muestra `CircularProgressIndicator`.
- `Empty`: muestra `No hay productos disponibles`.
- `Error`: muestra mensaje de error.
- `Success`: muestra lista de productos.

Cambios de examen probables:

- Cambiar mensaje vacio.
- Cambiar color de error.
- Agregar boton reintentar.
- Cambiar loading por texto.
- Limitar cantidad de productos mostrados.

## 9. Lista y ProductCard

Cuando hay productos:

```kotlin
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 80.dp)
) {
    items(state.products) { product ->
        ProductCard(product = product, canEdit = canEdit)
    }
}
```

La lista usa:

- `LazyColumn`: lista eficiente.
- `spacedBy(10.dp)`: espacio entre elementos.
- `PaddingValues(bottom = 80.dp)`: espacio final para no chocar con botones/barras.

Cambios de examen probables:

- Cambiar separacion entre cards.
- Cambiar padding inferior.
- Mostrar solo primeros N productos con `state.products.take(N)`.
- Ordenar antes de mostrar.

## 10. ProductCard

`ProductCard` muestra cada producto.

```kotlin
@Composable
fun ProductCard(product: Product, canEdit: Boolean = true)
```

Calcula si el stock es critico:

```kotlin
val isCritical = product.currentStock <= product.minimumStock
```

Color:

```kotlin
val stockColor = if (isCritical) MaterialTheme.colorScheme.error
else MaterialTheme.colorScheme.tertiary
```

Texto:

```kotlin
val stockLabel = if (isCritical) "Critico" else "Normal"
```

Card:

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
)
```

Esta parte es muy probable para examen.

Cambios tipicos:

- Cambiar `padding(8.dp)`.
- Cambiar `shape`.
- Cambiar `containerColor`.
- Cambiar `padding(16.dp)` interno.
- Cambiar texto de estado critico/normal.
- Cambiar condicion de stock critico.

Respuesta teorica posible:

```text
ProductCard es el lugar correcto para cambios visuales de cada producto porque encapsula como se representa un producto dentro de la lista.
```

## 11. displayName de ProductCategory

Al final de `CatalogScreen.kt` existe:

```kotlin
private fun ProductCategory.displayName() = when (this) {
    ProductCategory.STICKER_INDIVIDUAL -> "Cromos"
    ProductCategory.STICKER_PACK -> "Packs"
    ProductCategory.ALBUM -> "Albumes"
    ProductCategory.STICKER_BOX -> "Cajas"
    ProductCategory.PLUSH -> "Peluches"
    ProductCategory.BALL -> "Balones"
    ProductCategory.SPECIAL_EDITION -> "Edicion Especial"
}
```

Esto convierte valores tecnicos del enum a textos visibles.

Cambios de examen probables:

- Cambiar nombre visible de una categoria.
- Cambiar "Cromos" por "Stickers".
- Cambiar "Packs" por "Paquetes".

Idea clave:

```text
El enum mantiene nombres tecnicos; displayName define textos visibles para UI.
```

## 12. AddProductDialog

`AddProductDialog` maneja el formulario de agregar o ajustar producto.

Firma:

```kotlin
fun AddProductDialog(
    draft: AddProductDraft,
    products: List<Product>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onDraftChange: ((AddProductDraft) -> AddProductDraft) -> Unit,
    onVerify: () -> Unit,
    onConfirm: (name: String, category: ProductCategory, stock: Int, minStock: Int, ocrId: String) -> Unit
)
```

El dialogo tiene dos fases:

1. Verificar producto.
2. Agregar nuevo producto o ajustar stock existente.

## 13. Producto existente vs nuevo

```kotlin
val existingProduct = draft.existingProductId?.let { id ->
    products.firstOrNull { it.id == id }
}
```

```kotlin
val isExistingProduct = wasVerified && existingProduct != null
val isNewProduct = wasVerified && existingProduct == null
```

Regla:

```text
No verificado -> solo muestra boton Verificar existencia
Verificado y existe -> ajustar stock
Verificado y no existe -> crear producto nuevo
```

## 14. Validacion del formulario

Valores:

```kotlin
val stockValue = stock.toIntOrNull()
val minStockValue = minStock.toIntOrNull()
```

Producto existente:

```kotlin
stockValue != null &&
stockValue != 0 &&
resultingStock != null &&
resultingStock >= 0
```

Producto nuevo:

```kotlin
stockValue != null &&
minStockValue != null &&
stockValue > 0 &&
minStockValue >= 0 &&
minStockValue < stockValue
```

Reglas actuales:

- Para producto existente, la cantidad no puede ser 0.
- Para producto existente, el stock resultante no puede ser negativo.
- Para producto nuevo, stock inicial debe ser mayor a 0.
- Para producto nuevo, stock minimo debe ser mayor o igual a 0.
- Para producto nuevo, stock minimo debe ser menor que stock inicial.

Cambios de examen probables:

- Permitir stock minimo igual al stock inicial.
- Cambiar stock minimo por defecto.
- Bloquear stock inicial menor a 5.
- Permitir ajuste 0.
- Cambiar mensaje de error.

## 15. CatalogViewModel.kt

`CatalogViewModel` concentra la logica de presentacion del catalogo.

Constructor:

```kotlin
class CatalogViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getProductByNameAndCategoryUseCase: GetProductByNameAndCategoryUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val addStockMovementUseCase: AddStockMovementUseCase
) : ViewModel()
```

Usa:

- `SavedStateHandle`: conservar draft al recrear pantalla.
- Casos de uso: cargar, buscar, filtrar, agregar, actualizar y registrar movimientos.

Idea clave:

```text
El ViewModel no conoce detalles de Room o Supabase. Usa casos de uso.
```

## 16. Estados del ViewModel

```kotlin
private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()
```

Estado general de la pantalla.

```kotlin
private val _addProductDraft = MutableStateFlow(restoreDraft())
val addProductDraft: StateFlow<AddProductDraft> = _addProductDraft.asStateFlow()
```

Datos temporales del formulario.

```kotlin
private val _isAddProductDialogVisible = MutableStateFlow(...)
val isAddProductDialogVisible: StateFlow<Boolean> = _isAddProductDialogVisible.asStateFlow()
```

Visibilidad del dialogo.

```kotlin
private val _catalogProducts = MutableStateFlow<List<Product>>(emptyList())
val catalogProducts: StateFlow<List<Product>> = _catalogProducts.asStateFlow()
```

Lista auxiliar para verificar productos existentes.

## 17. Carga inicial

```kotlin
init { loadProducts() }
```

Cuando se crea el ViewModel, carga productos automaticamente.

`loadProducts`:

```kotlin
fun loadProducts() {
    viewModelScope.launch {
        _uiState.value = CatalogUiState.Loading
        try {
            val products = getProductsUseCase()
            updateCatalogProducts(products)
            _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
            else CatalogUiState.Success(products = products)
        } catch (e: Exception) {
            _uiState.value = CatalogUiState.Error(e.message ?: "Error al cargar productos")
        }
    }
}
```

Respuesta teorica posible:

```text
La carga se ejecuta en viewModelScope para no bloquear la UI. El estado cambia a Loading, luego a Success, Empty o Error.
```

## 18. Buscar y filtrar

Busqueda:

```kotlin
fun searchProducts(query: String)
```

Usa `SearchProductsUseCase` y guarda el texto en `searchQuery`.

Filtro:

```kotlin
fun filterByCategory(category: ProductCategory)
```

Usa `FilterProductsByCategoryUseCase` y guarda `activeFilter`.

Respuesta teorica posible:

```text
La pantalla dispara la busqueda o el filtro, pero el ViewModel actualiza el estado segun el resultado del caso de uso.
```

## 19. Verificar producto antes de guardar

```kotlin
fun verifyAddProductDraft() {
    viewModelScope.launch {
        val draft = _addProductDraft.value
        val existing = getProductByNameAndCategoryUseCase(draft.name.trim(), draft.category)
        updateAddProductDraft {
            it.copy(
                wasVerified = true,
                existingProductId = existing?.id,
                stock = "",
                minStock = "15"
            )
        }
    }
}
```

Funcionamiento:

1. Toma nombre y categoria del draft.
2. Busca si ya existe un producto con ese nombre y categoria.
3. Marca `wasVerified = true`.
4. Guarda el ID si existe.
5. Reinicia stock.
6. Coloca stock minimo por defecto en `15`.

Cambio probable:

```text
Si piden cambiar stock minimo por defecto de 15 a 10, revisar AddProductDraft, restoreDraft y verifyAddProductDraft.
```

## 20. Guardar producto o ajustar stock

Funcion principal:

```kotlin
fun saveManualProduct(...)
```

Primera regla:

```kotlin
if (currentUser?.role == UserRole.AUDITOR) {
    _uiState.value = CatalogUiState.Error(
        "El rol de auditor no tiene permiso para modificar el inventario."
    )
    return
}
```

Luego:

1. Limpia el nombre.
2. Busca producto existente por nombre y categoria.
3. Si existe, calcula nuevo stock.
4. Si el nuevo stock es negativo, muestra error.
5. Si existe, actualiza producto y registra movimiento.
6. Si no existe, crea producto y registra movimiento de entrada.
7. Recarga productos.
8. Cuenta pendientes de sincronizacion.
9. Limpia draft y cierra dialogo.
10. Muestra mensaje de exito.

## 21. Movimiento de stock

Cuando se actualiza producto existente:

```kotlin
movementType = if (stockValue >= 0) MovementType.ENTRY else MovementType.EXIT
quantity = kotlin.math.abs(stockValue)
```

Regla:

```text
Stock positivo -> entrada
Stock negativo -> salida
La cantidad se guarda como valor absoluto
```

Cuando se crea producto nuevo:

```kotlin
movementType = MovementType.ENTRY
quantity = stockValue
```

Respuesta teorica posible:

```text
Cada cambio de stock registra un StockMovement para mantener historial de inventario.
```

## 22. SavedStateHandle y AddProductDraft

`AddProductDraft`:

```kotlin
data class AddProductDraft(
    val name: String = "",
    val category: ProductCategory = ProductCategory.STICKER_INDIVIDUAL,
    val stock: String = "",
    val minStock: String = "15",
    val ocrId: String = "",
    val wasVerified: Boolean = false,
    val existingProductId: String? = null
)
```

Representa el formulario en progreso.

Campos:

- `name`: nombre del producto.
- `category`: categoria seleccionada.
- `stock`: cantidad escrita.
- `minStock`: stock minimo escrito.
- `ocrId`: identificador OCR.
- `wasVerified`: si ya se verifico existencia.
- `existingProductId`: producto encontrado.

`SavedStateHandle` guarda cada campo:

```kotlin
savedStateHandle[KEY_NAME] = draft.name
savedStateHandle[KEY_CATEGORY] = draft.category.name
...
```

Idea clave:

```text
SavedStateHandle permite restaurar el formulario si la pantalla se recrea.
```

## 23. CatalogUiState.kt

Estados:

```kotlin
sealed class CatalogUiState {
    object Loading : CatalogUiState()
    data class Success(...) : CatalogUiState()
    data class Error(...) : CatalogUiState()
    object Empty : CatalogUiState()
}
```

`Success` contiene:

- `products`: lista visible.
- `activeFilter`: filtro actual.
- `searchQuery`: busqueda actual.
- `isOffline`: indica modo offline.
- `pendingSyncCount`: cantidad pendiente de sincronizar.
- `message`: mensaje de exito.

`Error` contiene:

- `message`: texto del error.
- `canRetry`: si podria reintentarse.

Respuesta teorica posible:

```text
CatalogUiState permite que la pantalla represente carga, exito, error o lista vacia con un unico estado observable.
```

## 24. Product.kt

Modelo de dominio:

```kotlin
data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val description: String,
    val currentStock: Int,
    val minimumStock: Int,
    val imageUrl: String?,
    val ocrIdentifier: String?,
    val lastUpdated: Long,
    val isSynced: Boolean = false
)
```

Campos clave para defensa:

- `currentStock`: stock actual.
- `minimumStock`: limite para marcar critico.
- `ocrIdentifier`: codigo que puede detectar el scanner.
- `isSynced`: indica si esta sincronizado con remoto.

Idea clave:

```text
Product es el modelo que usa la capa de dominio y presentacion. No es directamente una entidad Room ni un DTO remoto.
```

## 25. ProductCategory.kt

Enum:

```kotlin
enum class ProductCategory {
    STICKER_INDIVIDUAL,
    STICKER_PACK,
    ALBUM,
    STICKER_BOX,
    PLUSH,
    BALL,
    SPECIAL_EDITION
}
```

Define categorias tecnicas.

Se muestran en UI con `displayName`.

Cambios de examen probables:

- Agregar una categoria.
- Cambiar nombre visible.
- Ocultar categoria del filtro.

Advertencia:

```text
Agregar una categoria puede requerir revisar UI, datos remotos, datos locales y mappers si la API o base tambien dependen del enum.
```

## 26. ProductRepositoryImpl.kt

`ProductRepositoryImpl` implementa acceso a productos.

Constructor:

```kotlin
class ProductRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val productDao: ProductDao
) : ProductRepository
```

Usa:

- `apiService`: Supabase remoto.
- `productDao`: Room local.

Patron general:

```text
Intentar remoto + sincronizar
Si falla, usar local
```

## 27. getProducts

```kotlin
override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
    val localProducts = productDao.getAllProducts().map { it.toDomain() }

    try {
        syncPendingProducts()
        val remote = getAllRemoteProducts()
        productDao.upsertProducts(remote.map { it.toEntity() })
        val pendingLocal = productDao.getUnsyncedProducts().map { it.toDomain() }

        (remote + pendingLocal)
            .distinctBy { it.id }
            .sortedBy { it.name }
    } catch (e: Exception) {
        localProducts
    }
}
```

Funcionamiento:

1. Lee productos locales como respaldo.
2. Intenta sincronizar pendientes.
3. Descarga productos remotos.
4. Guarda remotos en Room.
5. Recupera productos locales no sincronizados.
6. Une remoto + pendientes.
7. Elimina duplicados por `id`.
8. Ordena por nombre.
9. Si falla, devuelve datos locales.

Respuesta teorica posible:

```text
El repositorio tiene comportamiento offline-first parcial: intenta usar remoto, pero si hay error devuelve productos locales de Room.
```

## 28. searchProducts

Remoto:

```kotlin
apiService.searchProductsRemote(
    orFilter = "(name.ilike.*$query*,description.ilike.*$query*,ocr_identifier.ilike.*$query*)"
)
```

Busca por:

- nombre.
- descripcion.
- identificador OCR.

Fallback local:

```kotlin
p.name.contains(query, ignoreCase = true) ||
p.description.contains(query, ignoreCase = true) ||
p.ocrIdentifier?.contains(query, ignoreCase = true) == true
```

Cambios de examen probables:

- Buscar solo por nombre.
- Agregar busqueda por otra propiedad.
- Cambiar sensibilidad a mayusculas.

## 29. filterProductsByCategory

Remoto:

```kotlin
categoryFilter = "eq.${category.name}"
```

Local:

```kotlin
filter { it.category == category }
```

Respuesta teorica posible:

```text
El filtro se aplica remoto si hay conexion y local si falla la API.
```

## 30. addProduct y updateProduct

`addProduct`:

1. Guarda local con `isSynced = false`.
2. Intenta crear remoto.
3. Si remoto responde, guarda como sincronizado.
4. Si falla, deja local pendiente.

`updateProduct`:

1. Actualiza local con `lastUpdated` e `isSynced = false`.
2. Intenta actualizar remoto.
3. Si remoto responde, guarda sincronizado.
4. Si falla, deja pendiente.

Idea clave:

```text
Los cambios se guardan primero localmente para no perder informacion si falla la red.
```

## 31. syncPendingProducts

```kotlin
val pendingProducts = productDao.getUnsyncedProducts()
```

Para cada producto pendiente:

1. Intenta actualizar remoto.
2. Si no hay respuesta, intenta crearlo remoto.
3. Si remoto responde, actualiza Room como sincronizado.

Respuesta teorica posible:

```text
syncPendingProducts intenta enviar al servidor los productos locales que quedaron con isSynced=false.
```

## 32. ProductDao.kt

`ProductDao` define consultas locales de Room.

Consultas principales:

```kotlin
@Query("SELECT * FROM products ORDER BY name ASC")
suspend fun getAllProducts(): List<ProductEntity>
```

Obtiene todos los productos ordenados por nombre.

```kotlin
@Query("SELECT * FROM products WHERE id = :id")
suspend fun getProductById(id: String): ProductEntity?
```

Busca por ID.

```kotlin
@Query("SELECT * FROM products WHERE ocrIdentifier = :identifier COLLATE NOCASE LIMIT 1")
suspend fun getProductByOcrIdentifier(identifier: String): ProductEntity?
```

Busca por OCR ignorando mayusculas/minusculas.

```kotlin
@Upsert
suspend fun upsertProduct(product: ProductEntity)
```

Inserta o actualiza.

```kotlin
@Query("SELECT * FROM products WHERE isSynced = 0")
suspend fun getUnsyncedProducts(): List<ProductEntity>
```

Obtiene pendientes de sincronizacion.

```kotlin
@Query("""
SELECT * FROM products
WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
AND category = :category
LIMIT 1
""")
suspend fun getProductByNameAndCategory(...)
```

Busca producto existente por nombre y categoria.

## 33. Cambios de examen probables en Catalogo

### Cambiar margen de cards de productos

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
ProductCard -> Card -> Modifier.padding(8.dp)
```

Explicacion:

```text
Es un cambio visual de cada card, por eso se modifica ProductCard.
```

### Cambiar separacion entre cards

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(10.dp)
)
```

### Cambiar stock minimo por defecto

Archivos:

```text
AddProductDraft.kt
CatalogViewModel.kt
```

Ubicaciones:

```kotlin
val minStock: String = "15"
minStock = "15"
savedStateHandle.get<String>(KEY_MIN_STOCK) ?: "15"
```

### Cambiar mensaje de lista vacia

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
Text("No hay productos disponibles")
```

### Cambiar busqueda

Archivos posibles:

```text
CatalogScreen.kt, si cambia cuando se busca
CatalogViewModel.kt, si cambia flujo de estado
ProductRepositoryImpl.kt, si cambia criterio real de busqueda
```

### Cambiar orden de productos

Archivos posibles:

```text
ProductDao.kt, si cambia orden local
ProductRepositoryImpl.kt, si cambia orden final
CatalogScreen.kt, si solo cambia orden visual
```

### Cambiar permisos de auditor

Archivos:

```text
CatalogScreen.kt
CatalogViewModel.kt
```

Pantalla:

```kotlin
val canEdit = currentUser?.role != UserRole.AUDITOR
```

ViewModel:

```kotlin
if (currentUser?.role == UserRole.AUDITOR) { ... }
```

### Cambiar texto de categorias

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
private fun ProductCategory.displayName()
```

### Cambiar regla de stock critico

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
val isCritical = product.currentStock <= product.minimumStock
```

Ejemplo:

```text
Marcar critico solo si currentStock < minimumStock.
```

## 34. Explicacion corta para defensa

Version de 30 segundos:

```text
CatalogScreen muestra el catalogo, buscador, filtros, dialogo y cards de productos. CatalogViewModel maneja la carga, busqueda, filtro, verificacion y guardado. CatalogUiState representa loading, success, error y empty. ProductRepositoryImpl conecta la app con Supabase y Room, guardando localmente y usando datos locales si falla la red.
```

Version de 1 minuto:

```text
El catalogo esta separado por responsabilidades. La pantalla Compose observa estados del ViewModel y muestra buscador, filtros, lista y formulario. El ViewModel ejecuta casos de uso para cargar, buscar, filtrar, verificar si existe un producto y guardar cambios de stock. Si el usuario es auditor, se bloquea la modificacion. El formulario usa AddProductDraft y SavedStateHandle para conservar datos temporales. ProductRepositoryImpl combina Supabase y Room: intenta sincronizar pendientes y consultar remoto, pero si falla devuelve datos locales. ProductDao contiene las consultas Room para productos.
```

## 35. Checklist para dominar esta fase

- Puedo explicar que hace `CatalogScreen`.
- Puedo explicar que estados observa la pantalla.
- Puedo explicar como funciona el buscador.
- Puedo explicar como funcionan los filtros.
- Puedo explicar que muestra `ProductCard`.
- Puedo ubicar donde cambiar padding de cards.
- Puedo explicar que es `AddProductDraft`.
- Puedo explicar por que se usa `SavedStateHandle`.
- Puedo explicar como se verifica si un producto ya existe.
- Puedo explicar como se actualiza stock.
- Puedo explicar como se registra un movimiento.
- Puedo explicar que contiene `CatalogUiState.Success`.
- Puedo explicar que representa `Product`.
- Puedo explicar que representa `ProductCategory`.
- Puedo explicar el fallback local de `ProductRepositoryImpl`.
- Puedo explicar consultas principales de `ProductDao`.

## 36. Mini simulacros de esta fase

### Simulacro 1: cambiar padding de ProductCard

Tipo de cambio: UI/diseno.

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
ProductCard -> Card -> Modifier.padding(8.dp)
```

Explicacion:

```text
El margen externo de cada card se controla con Modifier.padding dentro de ProductCard.
```

### Simulacro 2: cambiar separacion entre productos

Tipo de cambio: UI/lista.

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
verticalArrangement = Arrangement.spacedBy(10.dp)
```

Explicacion:

```text
La separacion entre items de la lista se controla en LazyColumn.
```

### Simulacro 3: cambiar stock minimo por defecto de 15 a 10

Tipo de cambio: valor por defecto/formulario.

Archivos:

```text
AddProductDraft.kt
CatalogViewModel.kt
```

Cambiar todos los defaults `"15"` relacionados con `minStock`.

Explicacion:

```text
El valor por defecto existe en el draft y tambien se restaura o asigna desde el ViewModel al verificar producto.
```

### Simulacro 4: cambiar texto de categoria "Cromos" a "Stickers"

Tipo de cambio: texto visible.

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
ProductCategory.STICKER_INDIVIDUAL -> "Cromos"
```

Explicacion:

```text
Solo cambia el nombre visible de la categoria, no el enum ni la base de datos.
```

### Simulacro 5: ordenar productos por stock ascendente

Tipo de cambio: lista/datos.

Opcion simple visual:

```text
CatalogScreen.kt -> items(state.products.sortedBy { it.currentStock })
```

Opcion de repositorio:

```text
ProductRepositoryImpl.kt -> cambiar sortedBy { it.name } por sortedBy { it.currentStock }
```

Explicacion:

```text
Si el cambio solo afecta la vista del catalogo, puede hacerse en la pantalla. Si debe ser regla general del repositorio, se cambia en ProductRepositoryImpl.
```

### Simulacro 6: cambiar regla de critico

Tipo de cambio: logica visual.

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
val isCritical = product.currentStock <= product.minimumStock
```

Explicacion:

```text
La regla solo afecta como se etiqueta y colorea la card, por eso se modifica ProductCard.
```

### Simulacro 7: impedir stock inicial menor a 5

Tipo de cambio: validacion de formulario.

Archivo:

```text
CatalogScreen.kt
```

Ubicacion:

```kotlin
isFormValid -> isNewProduct
```

Idea:

```text
Cambiar stockValue > 0 por stockValue >= 5 y agregar mensaje si es necesario.
```

Explicacion:

```text
La validacion del formulario se calcula en AddProductDialog antes de habilitar el boton de confirmacion.
```

### Simulacro 8: cambiar mensaje cuando auditor intenta guardar

Tipo de cambio: permisos/mensaje.

Archivo:

```text
CatalogViewModel.kt
```

Ubicacion:

```kotlin
"El rol de auditor no tiene permiso para modificar el inventario."
```

Explicacion:

```text
El bloqueo real de guardado esta en el ViewModel, por eso el mensaje de permiso se cambia alli.
```

## 37. Frase clave para recordar

```text
CatalogScreen dibuja el inventario; CatalogViewModel decide estados y reglas; ProductRepositoryImpl sincroniza remoto/local; ProductDao consulta Room.
```
