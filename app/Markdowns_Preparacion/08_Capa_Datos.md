# 4.8 Capa de datos

## Objetivo de esta fase

Entender como StikerVault guarda, recupera, sincroniza y transforma datos usando Room, DataStore, Retrofit y Supabase.

Esta capa responde preguntas como:

- Donde se guardan productos, usuarios y movimientos localmente.
- Como se consulta Supabase.
- Como se transforma un DTO remoto en modelo de dominio.
- Como se transforma una entidad Room en modelo de dominio.
- Como se conserva sesion local.
- Como se maneja funcionamiento offline o con fallos de red.

## Archivos revisados

```text
data/local/database/AppDatabase.kt
data/local/dao/AppUserDao.kt
data/local/dao/ProductDao.kt
data/local/dao/StockMovementDao.kt
data/local/entity/AppUserEntity.kt
data/local/entity/ProductEntity.kt
data/local/entity/StockMovementEntity.kt
data/local/model/StockMovementWithProductName.kt
data/local/session/SessionPreferences.kt
data/remote/RetrofitClient.kt
data/remote/api/SupabaseApiService.kt
data/remote/dto/AppUserDto.kt
data/remote/dto/ProductDto.kt
data/remote/dto/StockMovementDto.kt
data/mapper/AppUserMapper.kt
data/mapper/ProductMapper.kt
data/mapper/StockMovementMapper.kt
data/repository/AuthRepositoryImpl.kt
data/repository/ProductRepositoryImpl.kt
data/repository/StockMovementRepositoryImpl.kt
```

Flujo general:

```text
ViewModel
-> UseCase
-> RepositoryImpl
-> API remota / DAO local
-> DTO / Entity
-> Mapper
-> Domain Model
```

## 1. Responsabilidad de la capa data

La capa `data` implementa detalles concretos de almacenamiento y red.

Incluye:

- Room para base local.
- DataStore para sesion.
- Retrofit para Supabase.
- DTOs para datos remotos.
- Entities para datos locales.
- Mappers para convertir formatos.
- Repositorios para combinar fuentes de datos.

Idea clave:

```text
La UI no deberia saber si un dato viene de Room o Supabase. Esa decision vive en los repositorios.
```

## 2. AppDatabase.kt

`AppDatabase` define la base local Room.

```kotlin
@Database(
    entities = [ProductEntity::class, StockMovementEntity::class, AppUserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase()
```

Entidades registradas:

- `ProductEntity`
- `StockMovementEntity`
- `AppUserEntity`

DAOs expuestos:

```kotlin
abstract fun productDao(): ProductDao
abstract fun stockMovementDao(): StockMovementDao
abstract fun appUserDao(): AppUserDao
```

Nombre de base:

```text
stickrvault.db
```

Usa singleton:

```kotlin
@Volatile private var INSTANCE: AppDatabase? = null
```

Y construye con:

```kotlin
Room.databaseBuilder(...)
    .fallbackToDestructiveMigration()
    .build()
```

`fallbackToDestructiveMigration` elimina y recrea la base si cambia la version sin migracion.

Respuesta teorica posible:

```text
AppDatabase centraliza la configuracion de Room, registra entidades, expone DAOs y usa singleton para evitar crear multiples instancias de base de datos.
```

## 3. Entities: modelos locales de Room

Las entidades representan tablas locales.

### AppUserEntity

```kotlin
@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val lastLogin: Long
)
```

Tabla:

```text
app_users
```

Guarda usuarios para login offline y sesion restaurada.

### ProductEntity

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String,
    val currentStock: Int,
    val minimumStock: Int,
    val imageUrl: String?,
    val ocrIdentifier: String?,
    val lastUpdated: Long,
    val isSynced: Boolean
)
```

Tabla:

```text
products
```

Campos clave:

- `currentStock`: stock actual.
- `minimumStock`: stock minimo.
- `ocrIdentifier`: codigo OCR.
- `isSynced`: indica si el producto ya fue sincronizado.

### StockMovementEntity

```kotlin
@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val movementType: String,
    val quantity: Int,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val isSynced: Boolean,
    val productName: String? = null
)
```

Tabla:

```text
stock_movements
```

Guarda historial de entradas, salidas y ajustes.

Respuesta teorica posible:

```text
Las entities son la representacion local de las tablas Room. Pueden parecerse al dominio, pero pertenecen a la capa local de persistencia.
```

## 4. DAOs: consultas locales

Los DAOs definen operaciones sobre Room.

### AppUserDao

Consultas:

```kotlin
getUserByEmail(email)
getUserById(id)
getAllUsers()
upsertUser(user)
```

Detalles:

- Busca email con `COLLATE NOCASE`, ignorando mayusculas/minusculas.
- Ordena usuarios por nombre.
- Usa `@Upsert` para insertar o actualizar.

Uso principal:

```text
Login offline, restauracion de sesion y dialogo de usuarios.
```

### ProductDao

Consultas:

```kotlin
getAllProducts()
getProductById(id)
getProductByOcrIdentifier(identifier)
upsertProducts(products)
upsertProduct(product)
deleteProductById(id)
getUnsyncedProducts()
markProductAsSynced(id)
getProductByNameAndCategory(name, category)
```

Detalles importantes:

- `getAllProducts` ordena por nombre.
- `getProductByOcrIdentifier` usa `COLLATE NOCASE`.
- `getUnsyncedProducts` busca `isSynced = 0`.
- `getProductByNameAndCategory` normaliza con `LOWER(TRIM(...))`.

Uso principal:

```text
Catalogo, scanner, sincronizacion offline y verificacion de producto existente.
```

### StockMovementDao

Consultas:

```kotlin
getRecentMovements(limit)
getRecentMovementsWithProductName(limit)
upsertMovements(movements)
upsertMovement(movement)
```

Consulta especial:

```sql
SELECT m.id, m.productId, m.movementType, m.quantity, m.userId, m.userName,
       m.timestamp, m.isSynced, COALESCE(m.productName, p.name) AS productName
FROM stock_movements m
LEFT JOIN products p ON m.productId = p.id
ORDER BY m.timestamp DESC
LIMIT :limit
```

Esta consulta une movimientos con productos para mostrar nombre del producto.

Respuesta teorica posible:

```text
Los DAOs contienen las consultas SQL locales. Los repositorios los usan, pero la UI nunca deberia llamar directamente a un DAO.
```

## 5. StockMovementWithProductName

Modelo local auxiliar:

```kotlin
data class StockMovementWithProductName(
    val id: String,
    val productId: String,
    val movementType: String,
    val quantity: Int,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val isSynced: Boolean,
    @ColumnInfo(name = "productName") val productName: String?
)
```

No es una entidad.

Sirve para recibir el resultado de una consulta con `JOIN`.

Idea clave:

```text
Cuando una consulta devuelve columnas combinadas, se puede usar un modelo local auxiliar en lugar de una Entity.
```

## 6. SessionPreferences.kt

`SessionPreferences` guarda datos simples de sesion con DataStore Preferences.

DataStore:

```kotlin
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stickrvault_session"
)
```

Funciones:

```kotlin
saveSession(userId, email)
getSavedUserId()
getSavedEmail()
clearSession()
```

Keys:

```text
user_id
user_email
```

Uso:

- Guardar sesion al iniciar sesion.
- Restaurar usuario al abrir app.
- Limpiar sesion al cerrar sesion.

Respuesta teorica posible:

```text
DataStore se usa para guardar informacion ligera de sesion, mientras Room guarda entidades mas estructuradas como usuarios y productos.
```

## 7. RetrofitClient.kt

`RetrofitClient` configura la conexion remota con Supabase.

Es un `object`, por lo tanto funciona como singleton.

Contiene:

- `BASE_URL` de Supabase REST.
- API key anonima de Supabase.
- `HttpLoggingInterceptor`.
- `OkHttpClient`.
- Headers globales.
- Instancia lazy de `SupabaseApiService`.

Headers agregados:

```text
apikey
Authorization
Content-Type
```

Instancia:

```kotlin
val apiService: SupabaseApiService by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SupabaseApiService::class.java)
}
```

Respuesta teorica posible:

```text
RetrofitClient centraliza la configuracion HTTP de Supabase, incluyendo base URL, headers y conversion JSON con Gson.
```

## 8. SupabaseApiService.kt

Define endpoints REST.

Productos:

```kotlin
getProducts(range, order)
getProductById(idFilter)
addProduct(product)
updateProduct(idFilter, product)
deleteProduct(idFilter)
searchProductsRemote(orFilter)
filterProductsByCategoryRemote(categoryFilter)
```

Usuarios:

```kotlin
getUsers()
getUserByEmail(emailFilter)
```

Movimientos:

```kotlin
getStockMovements(order, limit)
addStockMovement(movement)
```

Filtros Supabase:

```text
eq.valor
or=(...)
order=timestamp.desc
limit=10
```

`Prefer: return=representation` permite que Supabase devuelva el registro creado o actualizado.

Respuesta teorica posible:

```text
SupabaseApiService declara las llamadas HTTP. Los repositorios construyen filtros como eq.id u or para buscar datos.
```

## 9. DTOs: modelos remotos

Los DTOs representan el JSON de Supabase.

### AppUserDto

```kotlin
data class AppUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)
```

### ProductDto

Usa `SerializedName` para mapear snake_case:

```kotlin
@SerializedName("current_stock") val currentStock: Int
@SerializedName("minimum_stock") val minimumStock: Int
@SerializedName("image_url") val imageUrl: String?
@SerializedName("ocr_identifier") val ocrIdentifier: String?
@SerializedName("last_updated") val lastUpdated: Long
@SerializedName("is_synced") val isSynced: Boolean
```

### StockMovementDto

Campos remotos:

```kotlin
product_id
movement_type
user_id
user_name
is_synced
```

Idea clave:

```text
DTO se adapta al JSON remoto. Domain model se adapta a la app.
```

## 10. Mappers

Los mappers convierten entre DTO, Entity y Domain.

### AppUserMapper

Convierte:

```kotlin
AppUserDto -> AppUser
AppUserEntity -> AppUser
AppUser -> AppUserEntity
```

Rol:

```kotlin
runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.AUDITOR)
```

Si el rol viene mal, usa `AUDITOR` por seguridad.

### ProductMapper

Convierte:

```kotlin
ProductDto -> Product
ProductEntity -> Product
Product -> ProductEntity
```

Tambien convierte strings de categoria:

```kotlin
private fun String.toProductCategory(): ProductCategory
```

Acepta variantes como:

- `CROMO`, `CROMOS`, `STICKER`
- `PACK`, `SOBRE`
- `ALBUM`, `ALBUMES`
- `PELUCHE`, `PLUSH`
- `BALON`, `BALL`
- `SPECIAL_EDITION`

Si no reconoce la categoria:

```text
STICKER_INDIVIDUAL
```

### StockMovementMapper

Convierte:

```kotlin
StockMovementDto -> StockMovement
StockMovement -> StockMovementDto
StockMovementEntity -> StockMovement
StockMovement -> StockMovementEntity
```

Tipo de movimiento:

```kotlin
runCatching { MovementType.valueOf(movementType) }
    .getOrDefault(MovementType.ADJUSTMENT)
```

Respuesta teorica posible:

```text
Los mappers separan formatos de red, base local y dominio. Asi la UI trabaja con modelos de dominio sin depender del JSON ni de Room.
```

## 11. AuthRepositoryImpl.kt

Implementa autenticacion.

Constructor:

```kotlin
class AuthRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val appUserDao: AppUserDao,
    private val sessionPreferences: SessionPreferences
) : AuthRepository
```

Responsabilidades:

- Login remoto.
- Fallback local.
- Guardar sesion.
- Restaurar sesion.
- Limpiar sesion.
- Obtener usuarios.

### getSavedSession

1. Lee `user_id` desde DataStore.
2. Si existe, busca usuario en Room por ID.
3. Si no hay ID, lee email.
4. Busca usuario local por email.

### login

1. Limpia email con `trim`.
2. Intenta buscar remoto:

```kotlin
apiService.getUserByEmail("eq.$cleanEmail")
```

3. Si existe, guarda sesion y usuario local.
4. Si falla la red, intenta usuario local.

### getUsers

1. Intenta traer usuarios remotos.
2. Los guarda localmente.
3. Si falla, devuelve usuarios locales.

Respuesta teorica posible:

```text
AuthRepositoryImpl intenta usar Supabase para autenticar, pero si falla la red puede usar usuarios cacheados en Room.
```

## 12. ProductRepositoryImpl.kt

Implementa productos.

Constructor:

```kotlin
class ProductRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val productDao: ProductDao
) : ProductRepository
```

Responsabilidades:

- Obtener productos.
- Buscar productos.
- Filtrar por categoria.
- Buscar por OCR.
- Agregar producto.
- Actualizar producto.
- Eliminar producto.
- Sincronizar pendientes.

### getProducts

Estrategia:

1. Leer productos locales como respaldo.
2. Intentar sincronizar pendientes.
3. Descargar remotos por paginas.
4. Guardarlos en Room.
5. Unir remotos con pendientes locales.
6. Eliminar duplicados.
7. Ordenar por nombre.
8. Si falla, devolver local.

### searchProducts

Remoto:

```text
name ilike query
description ilike query
ocr_identifier ilike query
```

Fallback local:

```text
contains por nombre, descripcion u OCR ignorando mayusculas
```

### filterProductsByCategory

Remoto:

```text
category = eq.CATEGORY_NAME
```

Fallback local:

```kotlin
filter { it.category == category }
```

### addProduct

1. Guarda local con `isSynced = false`.
2. Intenta crear remoto.
3. Si remoto responde, guarda sincronizado.
4. Si falla, deja pendiente.

### updateProduct

1. Actualiza local con `lastUpdated` e `isSynced = false`.
2. Intenta PATCH remoto.
3. Si responde, guarda sincronizado.
4. Si falla, conserva pendiente.

### syncPendingProducts

Busca productos con `isSynced = false` y trata de actualizarlos o crearlos en Supabase.

Respuesta teorica posible:

```text
ProductRepositoryImpl combina Room y Supabase. Guarda cambios localmente primero y luego intenta sincronizar remoto para soportar fallos de red.
```

## 13. StockMovementRepositoryImpl.kt

Implementa movimientos de stock.

Constructor:

```kotlin
class StockMovementRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val dao: StockMovementDao,
    private val productDao: ProductDao
) : StockMovementRepository
```

Responsabilidades:

- Obtener movimientos recientes.
- Agregar movimiento.
- Resolver nombre de producto.

### getRecentMovements

1. Intenta obtener remoto.
2. Convierte DTO a modelo.
3. Resuelve nombre de producto.
4. Guarda en Room.
5. Devuelve movimientos locales con nombre de producto.

Incluso si falla la red:

```kotlin
dao.getRecentMovementsWithProductName(limit).map { it.toModel() }
```

### addMovement

1. Resuelve nombre del producto.
2. Guarda movimiento local.
3. Intenta guardar remoto.
4. Si remoto responde, actualiza local.
5. Si falla, devuelve local.

### resolveProductName

Primero busca en Room:

```kotlin
productDao.getProductById(productId)
```

Si no encuentra, intenta remoto:

```kotlin
apiService.getProductById("eq.$productId")
```

Y guarda el producto localmente si lo obtiene.

Respuesta teorica posible:

```text
StockMovementRepositoryImpl garantiza que los movimientos puedan mostrarse con nombre de producto, usando Room primero y Supabase como respaldo.
```

## 14. Patrones importantes de esta capa

### withContext(Dispatchers.IO)

Los repositorios usan:

```kotlin
withContext(Dispatchers.IO)
```

Esto mueve trabajo de red/base de datos a un hilo de IO.

### runCatching / try-catch

Se usa para tolerar fallos de red.

Ejemplo:

```text
intentar remoto, si falla usar local
```

### isSynced

Campo usado para saber si un producto local ya fue enviado a Supabase.

### Upsert

Inserta o actualiza sin tener que preguntar antes si existe.

### DTO vs Entity vs Domain

```text
DTO -> API remota
Entity -> Room local
Domain -> app, ViewModels y UI
```

## 15. Cambios de examen probables en data

### Cambiar orden local de productos

Archivo:

```text
ProductDao.kt
```

Ubicacion:

```sql
SELECT * FROM products ORDER BY name ASC
```

### Cambiar limite de movimientos

Archivo posible:

```text
HomeViewModel.kt o ReportsViewModel.kt
```

Pero si piden consulta local:

```text
StockMovementDao.kt
```

### Agregar campo visual calculado

Preferencia:

```text
Calcular en ViewModel o Screen antes que cambiar Entity/DTO.
```

### Agregar campo persistente real

Archivos posibles:

```text
Product.kt
ProductEntity.kt
ProductDto.kt
ProductMapper.kt
AppDatabase.kt
Supabase tabla remota
```

Advertencia:

```text
Cambiar una Entity de Room puede requerir subir version de base y crear migracion, o usar fallback destructivo durante desarrollo.
```

### Cambiar busqueda remota

Archivo:

```text
ProductRepositoryImpl.kt
```

Ubicacion:

```kotlin
apiService.searchProductsRemote(orFilter = ...)
```

### Cambiar endpoint o filtro Supabase

Archivo:

```text
SupabaseApiService.kt
```

O el repositorio que arma el filtro.

### Cambiar texto visible

No tocar data.

Archivo probable:

```text
Screen.kt
```

Idea clave:

```text
La capa data no deberia usarse para cambios puramente visuales.
```

## 16. Explicacion corta para defensa

Version de 30 segundos:

```text
La capa data implementa Room, DataStore, Retrofit y Supabase. Room guarda productos, usuarios y movimientos localmente; DataStore guarda la sesion; Retrofit consulta Supabase; los DTOs representan datos remotos, las entities datos locales y los mappers convierten todo a modelos de dominio. Los repositorios combinan local y remoto para que la app pueda funcionar aun si falla la red.
```

Version de 1 minuto:

```text
StikerVault separa la capa de datos en local, remoto, mappers y repositorios. AppDatabase configura Room con entidades de productos, movimientos y usuarios. Los DAOs contienen consultas SQL locales. RetrofitClient y SupabaseApiService definen la conexion remota. Los DTOs usan SerializedName para mapear el JSON de Supabase, mientras las entities representan tablas Room. Los mappers convierten DTO o Entity a modelos de dominio. Los repositorios implementan la logica de acceso, intentando sincronizar con Supabase y usando Room como cache o fallback offline.
```

## 17. Checklist para dominar esta fase

- Puedo explicar que hace `AppDatabase`.
- Puedo nombrar las tres entidades Room.
- Puedo explicar que hace cada DAO.
- Puedo explicar para que sirve `SessionPreferences`.
- Puedo explicar que configura `RetrofitClient`.
- Puedo explicar que declara `SupabaseApiService`.
- Puedo diferenciar DTO, Entity y Domain.
- Puedo explicar para que sirven los mappers.
- Puedo explicar el fallback local de autenticacion.
- Puedo explicar la sincronizacion de productos pendientes.
- Puedo explicar como se resuelve `productName` en movimientos.
- Puedo ubicar donde cambiar una consulta local.
- Puedo ubicar donde cambiar una llamada remota.

## 18. Mini simulacros de esta fase

### Simulacro 1: ordenar productos por stock en Room

Tipo de cambio: consulta local.

Archivo:

```text
ProductDao.kt
```

Ubicacion:

```sql
SELECT * FROM products ORDER BY name ASC
```

Idea:

```sql
SELECT * FROM products ORDER BY currentStock ASC
```

Explicacion:

```text
El orden local de productos se controla desde la consulta DAO.
```

### Simulacro 2: cambiar busqueda para no incluir descripcion

Tipo de cambio: busqueda/remoto-local.

Archivo:

```text
ProductRepositoryImpl.kt
```

Ubicacion:

```kotlin
searchProducts(query)
```

Explicacion:

```text
La busqueda se implementa en el repositorio porque combina filtro remoto y fallback local.
```

### Simulacro 3: cambiar nombre de base local

Tipo de cambio: configuracion Room.

Archivo:

```text
AppDatabase.kt
```

Ubicacion:

```kotlin
"stickrvault.db"
```

Explicacion:

```text
El nombre de la base local se define al construir Room.
```

### Simulacro 4: guardar un dato adicional de sesion

Tipo de cambio: DataStore.

Archivo:

```text
SessionPreferences.kt
```

Acciones:

```text
Crear nueva key, escribirla en saveSession o funcion nueva, y leerla con data.map { ... }.first()
```

Explicacion:

```text
Datos simples de sesion se guardan en DataStore, no en una pantalla.
```

### Simulacro 5: agregar campo persistente a producto

Tipo de cambio: modelo persistente.

Archivos probables:

```text
Product.kt
ProductEntity.kt
ProductDto.kt
ProductMapper.kt
AppDatabase.kt
```

Explicacion:

```text
Un campo persistente atraviesa dominio, Room, API y mappers. No basta con agregarlo en la UI.
```

### Simulacro 6: cambiar categoria por defecto al mapear valor desconocido

Tipo de cambio: mapper.

Archivo:

```text
ProductMapper.kt
```

Ubicacion:

```kotlin
else -> ProductCategory.STICKER_INDIVIDUAL
```

Explicacion:

```text
La categoria fallback se define al convertir string remoto/local a ProductCategory.
```

## 19. Frase clave para recordar

```text
Room guarda local, Retrofit habla con Supabase, DataStore guarda sesion, los mappers traducen formatos y los repositorios deciden de donde salen los datos.
```
