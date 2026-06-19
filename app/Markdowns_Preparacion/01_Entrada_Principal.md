# 4.1 Entrada Principal: MainActivity.kt

## Objetivo de esta fase

Entender como inicia StikerVault, como se conectan sus capas principales y por que `MainActivity.kt` es el punto donde se arma la aplicacion antes de mostrar las pantallas.

En una defensa, esta seccion ayuda a explicar:

- Donde empieza la ejecucion de la app.
- Como se crean las dependencias principales.
- Como se conectan base local, API remota, repositorios, casos de uso y ViewModels.
- Como Compose muestra la interfaz inicial.
- Como se decide si se muestra o no la barra inferior.

## Archivo principal

```text
app/src/main/java/com/example/myapplication/MainActivity.kt
```

`MainActivity` hereda de `ComponentActivity`, que es la actividad base usada para aplicaciones modernas con Jetpack Compose.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...
    }
}
```

`onCreate` se ejecuta cuando Android abre la actividad principal. En este metodo se prepara todo lo necesario para que la app funcione.

## Flujo general de MainActivity

La actividad principal sigue este orden:

1. Crear la base de datos local.
2. Obtener los DAOs.
3. Crear repositorios.
4. Crear casos de uso.
5. Crear factories de ViewModels.
6. Inicializar Compose con `setContent`.
7. Crear los ViewModels.
8. Configurar navegacion y barra inferior.
9. Mostrar el `NavHost` principal.

Flujo resumido:

```text
MainActivity
-> AppDatabase
-> DAOs
-> Repositories
-> UseCases
-> ViewModelFactories
-> ViewModels
-> Scaffold
-> StickrVaultNavHost
-> Screens
```

## 1. Creacion de la base de datos local

```kotlin
val db = AppDatabase.getInstance(applicationContext)
```

Aqui se obtiene una instancia unica de la base de datos Room.

Room se usa para guardar informacion local, como:

- Productos.
- Movimientos de stock.
- Usuarios.

La base local permite que la app pueda consultar datos aunque falle la conexion con Supabase.

Respuesta teorica posible:

```text
La base de datos se inicializa en MainActivity mediante AppDatabase.getInstance. Esto permite acceder a los DAOs y mantener persistencia local con Room.
```

## 2. Obtencion de DAOs

```kotlin
val productDao = db.productDao()
val stockMovementDao = db.stockMovementDao()
val appUserDao = db.appUserDao()
```

Los DAOs son interfaces que permiten consultar o modificar tablas de Room.

En StikerVault:

- `ProductDao`: operaciones sobre productos.
- `StockMovementDao`: operaciones sobre movimientos de inventario.
- `AppUserDao`: operaciones sobre usuarios.

Idea clave:

```text
El DAO no contiene logica visual. Solo define operaciones de acceso a datos locales.
```

## 3. Creacion de repositorios

```kotlin
val productRepository = ProductRepositoryImpl(RetrofitClient.apiService, productDao)
```

Los repositorios conectan la capa de dominio con las fuentes de datos.

En StikerVault se crean tres repositorios:

- `ProductRepositoryImpl`
- `StockMovementRepositoryImpl`
- `AuthRepositoryImpl`

Cada repositorio puede usar datos locales y remotos:

```text
Repository -> Room local
Repository -> Supabase remoto
```

Ejemplo:

- `ProductRepositoryImpl` usa `SupabaseApiService` y `ProductDao`.
- `StockMovementRepositoryImpl` usa `SupabaseApiService`, `StockMovementDao` y `ProductDao`.
- `AuthRepositoryImpl` usa `SupabaseApiService`, `AppUserDao` y `SessionPreferences`.

Respuesta teorica posible:

```text
Los repositorios se crean en MainActivity para centralizar el acceso a datos. La UI no llama directamente a Room ni a Retrofit; llama a ViewModels y casos de uso.
```

## 4. Sesion local con DataStore

```kotlin
val sessionPreferences = SessionPreferences(applicationContext)
```

`SessionPreferences` guarda informacion de sesion usando DataStore.

Se usa para recordar al usuario autenticado, por ejemplo:

- ID del usuario.
- Correo del usuario.

Esto permite restaurar sesion cuando la app se vuelve a abrir.

## 5. Creacion de casos de uso

```kotlin
val getProductsUseCase = GetProductsUseCase(productRepository)
val getStockMovementsUseCase = GetStockMovementsUseCase(stockMovementRepository)
val getUsersUseCase = GetUsersUseCase(authRepository)
```

Los casos de uso representan acciones concretas de la aplicacion.

Ejemplos:

- Obtener productos.
- Buscar productos.
- Filtrar productos.
- Agregar productos.
- Actualizar stock.
- Login de usuario.
- Obtener movimientos.

Ventaja:

```text
La pantalla no necesita saber como se obtienen los datos. Solo ejecuta una accion del dominio por medio del ViewModel.
```

Respuesta teorica posible:

```text
Los UseCases separan acciones de negocio especificas de la implementacion de datos. Esto hace que el ViewModel sea mas claro y facil de probar.
```

## 6. Creacion de factories de ViewModels

```kotlin
val authFactory = AuthViewModelFactory(LoginUseCase(authRepository), authRepository)
```

Los ViewModels necesitan parametros, por ejemplo repositorios o casos de uso.

Como no se esta usando Hilt u otro framework de inyeccion de dependencias, el proyecto usa factories manuales.

Factories principales:

- `AuthViewModelFactory`
- `HomeViewModelFactory`
- `CatalogViewModelFactory`
- `ReportsViewModelFactory`
- `ScannerViewModelFactory`

Idea clave:

```text
Una factory construye un ViewModel cuando ese ViewModel no tiene constructor vacio.
```

Respuesta teorica posible:

```text
Se usan factories porque los ViewModels necesitan dependencias en el constructor. Android no puede crearlos automaticamente sin indicarle como construirlos.
```

## 7. Inicio de la interfaz con setContent

```kotlin
setContent {
    MyApplicationTheme {
        ...
    }
}
```

`setContent` indica que la interfaz se construira con Jetpack Compose.

Dentro se aplica el tema:

```kotlin
MyApplicationTheme {
    ...
}
```

El tema define colores, tipografia y estilos generales.

En un cambio visual global, se revisa `ui/theme`. En un cambio visual de una pantalla concreta, se revisa el composable de esa pantalla.

## 8. NavController

```kotlin
val navController = rememberNavController()
```

El `NavController` controla la navegacion entre pantallas Compose.

Permite:

- Ir a Home.
- Ir a Catalogo.
- Ir a Scanner.
- Ir a Reportes.
- Ir a Login.

Idea clave:

```text
El NavController es el objeto que ejecuta los cambios de pantalla.
```

## 9. Creacion de ViewModels dentro de Compose

```kotlin
val authViewModel: AuthViewModel = viewModel(factory = authFactory)
```

Dentro de `setContent`, se crean los ViewModels con sus factories.

ViewModels usados:

- `AuthViewModel`
- `CatalogViewModel`
- `HomeViewModel`
- `ReportsViewModel`
- `ScannerViewModel`

Cada ViewModel administra el estado y acciones de una parte de la aplicacion.

Ejemplo:

- `AuthViewModel`: login, logout y usuario actual.
- `CatalogViewModel`: productos, busqueda, filtros y agregado.
- `HomeViewModel`: resumen principal.
- `ReportsViewModel`: reportes y metricas.
- `ScannerViewModel`: estado del scanner y resultado OCR.

## 10. Mostrar u ocultar la barra inferior

```kotlin
val navBackStackEntry by navController.currentBackStackEntryAsState()
val showBottomBar = navBackStackEntry
    ?.destination
    ?.hasRoute<Routes.Login>() != true
```

Esta logica revisa la ruta actual.

Si la pantalla actual es Login, la barra inferior no se muestra.

Si la pantalla actual no es Login, la barra inferior se muestra.

```text
Login -> sin bottom bar
Home/Catalog/Scanner/Reports -> con bottom bar
```

Esta es una modificacion probable de examen.

Ejemplos:

- Hacer que la barra inferior tambien se oculte en Scanner.
- Mostrar la barra inferior incluso en Login.
- Cambiar que pantalla tiene barra inferior.

## 11. Scaffold principal

```kotlin
Scaffold(
    bottomBar = {
        if (showBottomBar) BottomNavigationBar(navController = navController)
    }
) { innerPadding ->
    ...
}
```

`Scaffold` es un layout base de Material Design.

Permite organizar:

- Barra superior.
- Barra inferior.
- Contenido principal.
- Floating action button.

En StikerVault se usa principalmente para insertar la barra inferior.

`innerPadding` evita que el contenido quede debajo de la barra inferior.

```kotlin
Box(modifier = Modifier.padding(innerPadding)) {
    ...
}
```

Respuesta teorica posible:

```text
Scaffold estructura la pantalla principal y permite colocar la barra inferior. El innerPadding se aplica para que el contenido no se superponga con esa barra.
```

## 12. NavHost principal

```kotlin
StickrVaultNavHost(
    navController = navController,
    authViewModel = authViewModel,
    catalogViewModel = catalogViewModel,
    homeViewModel = homeViewModel,
    scannerViewModel = scannerViewModel,
    reportsViewModel = reportsViewModel
)
```

`StickrVaultNavHost` recibe:

- El controlador de navegacion.
- Los ViewModels que necesitan las pantallas.

Este componente decide que pantalla se muestra segun la ruta actual.

Idea clave:

```text
MainActivity prepara las dependencias, pero StickrVaultNavHost decide que pantalla concreta se renderiza.
```

## 13. Que cambios de examen pueden caer en esta fase

### Cambiar la pantalla inicial

Buscar en:

```text
StickrVaultNavHost.kt
```

Normalmente se cambia el `startDestination`.

### Ocultar la bottom bar en otra pantalla

Buscar en:

```text
MainActivity.kt
```

Modificar la condicion `showBottomBar`.

### Agregar un ViewModel nuevo

Cambios necesarios:

1. Crear o ubicar repositorio/caso de uso.
2. Crear factory.
3. Crear ViewModel con `viewModel(factory = ...)`.
4. Pasarlo al NavHost o pantalla correspondiente.

### Cambiar tema global

Buscar en:

```text
ui/theme
```

MainActivity solo aplica el tema; no define colores directamente.

### Cambiar una dependencia usada por una pantalla

Buscar en:

```text
MainActivity.kt
```

Revisar donde se construye el repositorio, caso de uso o factory.

## 14. Explicacion corta para defensa

Version de 30 segundos:

```text
MainActivity es el punto de entrada de StikerVault. En onCreate se inicializan la base Room, los DAOs, los repositorios, los casos de uso y las factories de ViewModels. Luego setContent inicia Jetpack Compose, aplica el tema, crea el NavController y los ViewModels, configura el Scaffold con la barra inferior y finalmente carga StickrVaultNavHost para mostrar las pantallas.
```

Version de 1 minuto:

```text
La app inicia en MainActivity porque es la actividad declarada como launcher en AndroidManifest. Alli se arma manualmente la inyeccion de dependencias: primero Room con AppDatabase, luego DAOs, despues repositorios que combinan Room y Supabase, luego casos de uso y factories. En setContent se entra al mundo Compose, se aplica el tema, se crea el NavController y se obtienen los ViewModels. El Scaffold permite mostrar la barra inferior excepto en Login, y dentro del contenido se renderiza StickrVaultNavHost, que decide que pantalla mostrar segun la ruta actual.
```

## 15. Checklist para dominar esta fase

- Puedo explicar que `MainActivity` es el punto de entrada.
- Puedo explicar que `onCreate` inicializa la app.
- Puedo identificar donde se crea Room.
- Puedo identificar para que sirven los DAOs.
- Puedo explicar por que existen repositorios.
- Puedo explicar por que existen casos de uso.
- Puedo explicar por que se usan ViewModel factories.
- Puedo explicar que hace `setContent`.
- Puedo explicar que hace `rememberNavController`.
- Puedo explicar que hace `Scaffold`.
- Puedo explicar por que la bottom bar no aparece en Login.
- Puedo ubicar donde cambiaria una condicion global de navegacion o layout.

## 16. Mini simulacros de esta fase

### Simulacro 1: mostrar la bottom bar en Login

Tipo de cambio: navegacion/layout global.

Archivo probable:

```text
MainActivity.kt
```

Ubicacion:

```kotlin
val showBottomBar = navBackStackEntry
    ?.destination
    ?.hasRoute<Routes.Login>() != true
```

Explicacion:

```text
Se cambia la condicion que decide si se muestra la barra inferior. Es un cambio de layout global porque afecta al Scaffold principal.
```

### Simulacro 2: ocultar la bottom bar en Scanner

Tipo de cambio: navegacion/layout global.

Archivo probable:

```text
MainActivity.kt
```

Idea:

```text
Agregar una condicion para que showBottomBar sea falso tambien cuando la ruta actual sea Scanner.
```

Explicacion:

```text
El scanner puede requerir pantalla completa, por eso se modifica MainActivity, donde se controla la bottom bar global.
```

### Simulacro 3: explicar por que no se llama al repositorio desde la pantalla

Respuesta:

```text
La pantalla solo debe mostrar UI y enviar eventos. El ViewModel recibe esos eventos, ejecuta casos de uso y actualiza estados. El repositorio queda oculto detras del dominio para separar responsabilidades.
```

## 17. Frase clave para recordar

```text
MainActivity no contiene la logica de negocio de la app; contiene el armado inicial de dependencias, tema, navegacion y estructura principal de Compose.
```
