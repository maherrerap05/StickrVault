# 4.2 Navegacion: Routes, NavHost y BottomNavigationBar

## Objetivo de esta fase

Entender como StikerVault cambia entre pantallas, como decide la pantalla inicial y como funciona la barra inferior.

En una defensa, esta seccion ayuda a explicar:

- Que rutas existen en la aplicacion.
- Como se conecta cada ruta con una pantalla.
- Como se navega despues de login, logout o acciones del scanner.
- Como se marca seleccionada una opcion de la barra inferior.
- Donde modificar la pantalla inicial o los accesos de navegacion.

## Archivos principales

```text
app/src/main/java/com/example/myapplication/presentation/navigation/Routes.kt
app/src/main/java/com/example/myapplication/presentation/navigation/StickrVaultNavHost.kt
app/src/main/java/com/example/myapplication/presentation/navigation/BottomNavigationBar.kt
```

Flujo general:

```text
Routes.kt
-> define destinos posibles

StickrVaultNavHost.kt
-> conecta rutas con pantallas
-> decide pantalla inicial segun sesion
-> ejecuta navegaciones internas

BottomNavigationBar.kt
-> muestra accesos inferiores
-> navega a Home, Catalogo, Scanner y Reportes
```

## 1. Routes.kt

`Routes.kt` define las rutas de la aplicacion.

```kotlin
object Routes {
    @Serializable
    data object Login

    @Serializable
    data object Home

    @Serializable
    data object Catalog

    @Serializable
    data object Scanner

    @Serializable
    data object Reports
}
```

Cada `data object` representa una pantalla o destino.

Rutas existentes:

- `Routes.Login`: pantalla de inicio de sesion.
- `Routes.Home`: pantalla principal.
- `Routes.Catalog`: catalogo de productos.
- `Routes.Scanner`: escaner OCR.
- `Routes.Reports`: reportes.

### Por que usan `@Serializable`

El proyecto usa rutas tipadas con Navigation Compose. `@Serializable` permite que Navigation pueda identificar y manejar estos destinos como objetos tipados.

Idea clave:

```text
En vez de navegar con strings como "home" o "catalog", la app navega usando objetos tipados como Routes.Home.
```

Ventajas:

- Menos errores por escribir mal una ruta.
- Mejor organizacion.
- Rutas centralizadas en un solo archivo.

Respuesta teorica posible:

```text
Routes.kt centraliza los destinos de navegacion. Cada ruta es un objeto serializable para que Navigation Compose pueda usar rutas tipadas en lugar de strings.
```

## 2. StickrVaultNavHost.kt

`StickrVaultNavHost` es el componente que decide que pantalla se muestra segun la ruta actual.

Firma principal:

```kotlin
@Composable
fun StickrVaultNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    catalogViewModel: CatalogViewModel,
    homeViewModel: HomeViewModel,
    scannerViewModel: ScannerViewModel,
    reportsViewModel: ReportsViewModel
)
```

Recibe:

- `navController`: objeto que ejecuta la navegacion.
- `authViewModel`: estado de sesion y login.
- `catalogViewModel`: catalogo e inventario.
- `homeViewModel`: resumen principal.
- `scannerViewModel`: scanner OCR.
- `reportsViewModel`: reportes.

Idea clave:

```text
El NavHost no crea los ViewModels. Los recibe desde MainActivity y los entrega a cada pantalla.
```

## 3. Control de sesion antes de navegar

Al inicio del NavHost se leen dos estados desde `AuthViewModel`:

```kotlin
val isSessionReady by authViewModel.isSessionReady.collectAsState()
val currentUser by authViewModel.currentUser.collectAsState()
```

`isSessionReady` indica si ya termino la revision de sesion guardada.

`currentUser` indica si hay usuario autenticado.

Mientras la sesion no esta lista, se muestra un loading:

```kotlin
if (!isSessionReady) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
    return
}
```

Esto evita que la app muestre Login por un instante si en realidad habia una sesion guardada.

Respuesta teorica posible:

```text
Antes de crear el NavHost se espera a que AuthViewModel restaure la sesion. Si todavia no esta lista, se muestra un indicador de carga para evitar navegar a una pantalla incorrecta.
```

## 4. Pantalla inicial

La pantalla inicial se decide con:

```kotlin
val startDestination: Any = if (currentUser != null) Routes.Home else Routes.Login
```

Regla:

```text
Hay usuario autenticado -> Home
No hay usuario autenticado -> Login
```

Este es un punto muy probable para cambios de examen.

Ejemplos:

- Cambiar para que siempre inicie en Login.
- Cambiar para que despues de sesion inicie en Catalog.
- Cambiar para que usuarios auditores inicien en Reports.

Respuesta teorica posible:

```text
La pantalla inicial se calcula segun currentUser. Si existe usuario, se inicia en Home; si no existe, se inicia en Login.
```

## 5. NavHost

```kotlin
NavHost(navController = navController, startDestination = startDestination) {
    ...
}
```

`NavHost` es el contenedor que registra las pantallas navegables.

Dentro se definen rutas con:

```kotlin
composable<Routes.Login> { ... }
```

Cada `composable` conecta una ruta con una pantalla.

## 6. Ruta Login

```kotlin
composable<Routes.Login> {
    LoginScreen(
        viewModel = authViewModel,
        onLoginSuccess = {
            navController.navigate(Routes.Home) {
                popUpTo<Routes.Login> { inclusive = true }
            }
        }
    )
}
```

Funcionamiento:

1. Se muestra `LoginScreen`.
2. La pantalla usa `authViewModel`.
3. Cuando el login es exitoso, se navega a `Routes.Home`.
4. `popUpTo<Routes.Login> { inclusive = true }` elimina Login del historial.

Por que se elimina Login del historial:

```text
Para que al presionar atras despues de iniciar sesion no se regrese al Login.
```

Respuesta teorica posible:

```text
Despues del login exitoso se navega a Home y se elimina Login del back stack, porque el usuario ya no deberia volver a la pantalla de autenticacion con el boton atras.
```

## 7. Ruta Home

```kotlin
composable<Routes.Home> {
    HomeScreen(
        viewModel = homeViewModel,
        currentUser = currentUser,
        onLogout = { ... },
        onNavigateCatalog = { navController.navigate(Routes.Catalog) },
        onNavigateScanner = { navController.navigate(Routes.Scanner) },
        onNavigateReports = { navController.navigate(Routes.Reports) }
    )
}
```

Home recibe:

- Su ViewModel.
- Usuario actual.
- Accion para cerrar sesion.
- Acciones para navegar a Catalogo, Scanner y Reportes.

### Logout

```kotlin
onLogout = {
    authViewModel.logout()
    navController.navigate(Routes.Login) {
        popUpTo(0) { inclusive = true }
    }
}
```

Funcionamiento:

1. Limpia el usuario actual y la sesion.
2. Navega a Login.
3. Limpia el historial completo con `popUpTo(0)`.

Respuesta teorica posible:

```text
Al cerrar sesion se limpia el estado de autenticacion y se navega a Login eliminando el historial, para impedir que el usuario vuelva a pantallas internas con el boton atras.
```

## 8. Ruta Catalog

```kotlin
composable<Routes.Catalog> {
    CatalogScreen(
        viewModel = catalogViewModel,
        currentUser = currentUser
    )
}
```

Catalogo recibe:

- `catalogViewModel`: productos, busqueda, filtros y guardado.
- `currentUser`: usuario actual para validar permisos.

Ejemplo:

```text
Si currentUser es AUDITOR, el ViewModel bloquea modificaciones de inventario.
```

## 9. Ruta Scanner

La ruta Scanner tiene una logica extra de permisos.

```kotlin
val canEditInventory = currentUser?.role == UserRole.WAREHOUSE_CHIEF ||
    currentUser?.role == UserRole.WAREHOUSE_OPERATOR
```

Solo pueden editar inventario:

- `WAREHOUSE_CHIEF`
- `WAREHOUSE_OPERATOR`

No puede editar:

- `AUDITOR`

Luego se muestra la pantalla:

```kotlin
ScannerScreen(
    viewModel = scannerViewModel,
    canEditInventory = canEditInventory,
    onAddProduct = { code -> ... },
    onAddExistingProduct = { product -> ... }
)
```

### Agregar producto nuevo desde Scanner

```kotlin
onAddProduct = { code ->
    if (canEditInventory) {
        catalogViewModel.openAddProductFromScan(code)
        navController.navigate(Routes.Catalog)
    }
}
```

Funcionamiento:

1. Se detecta un codigo OCR.
2. Si el usuario puede editar, se abre el formulario del catalogo con el codigo cargado.
3. Se navega a Catalogo.

### Agregar stock a producto existente

```kotlin
onAddExistingProduct = { product ->
    if (canEditInventory) {
        catalogViewModel.saveManualProduct(
            name = product.name,
            category = product.category,
            stockValue = 1,
            minimumStock = product.minimumStock,
            ocrIdentifier = product.ocrIdentifier,
            currentUser = currentUser
        )
        navController.navigate(Routes.Catalog) {
            launchSingleTop = true
        }
    }
}
```

Funcionamiento:

1. El scanner encuentra un producto existente.
2. Si el usuario puede editar, se suma 1 al stock.
3. Se navega a Catalogo.
4. `launchSingleTop = true` evita duplicar la misma pantalla si ya esta arriba.

Respuesta teorica posible:

```text
Scanner no modifica directamente la base de datos. Usa CatalogViewModel para abrir el formulario o actualizar stock, porque el catalogo concentra la logica de inventario.
```

## 10. Ruta Reports

```kotlin
composable<Routes.Reports> {
    ReportsScreen(viewModel = reportsViewModel)
}
```

Reportes recibe solo su ViewModel.

Se usa para mostrar metricas y movimientos.

## 11. BottomNavigationBar.kt

`BottomNavigationBar` muestra la barra inferior con accesos principales.

Firma:

```kotlin
@Composable
fun BottomNavigationBar(
    navController: NavHostController
)
```

Recibe el mismo `NavController` para poder navegar.

## 12. Ruta actual seleccionada

```kotlin
val currentDestination = navController
    .currentBackStackEntryAsState()
    .value
    ?.destination
```

Esto obtiene la pantalla actual.

Luego cada item compara si su ruta es la actual:

```kotlin
selected = currentDestination?.hasRoute<Routes.Home>() == true
```

Idea clave:

```text
La barra inferior no guarda manualmente cual item esta seleccionado. Lo deduce desde la ruta actual del NavController.
```

Respuesta teorica posible:

```text
La seleccion de cada item se calcula comparando la ruta actual del NavController con la ruta asociada al item usando hasRoute.
```

## 13. Items de la barra inferior

La barra tiene cuatro items:

- Home.
- Catalogo.
- Scanner.
- Reportes.

Cada item tiene:

- `selected`: indica si esta activo.
- `onClick`: navega a la ruta.
- `icon`: icono visual.
- `label`: texto visible.

Ejemplo:

```kotlin
NavigationBarItem(
    selected = currentDestination?.hasRoute<Routes.Home>() == true,
    onClick = {
        navController.navigate(Routes.Home) {
            launchSingleTop = true
        }
    },
    icon = {
        Icon(Icons.Default.Home, contentDescription = "Home")
    },
    label = {
        Text("Home")
    }
)
```

## 14. launchSingleTop

En los items de la bottom bar se usa:

```kotlin
launchSingleTop = true
```

Esto evita crear muchas copias de la misma pantalla cuando el usuario toca varias veces el mismo item.

Ejemplo:

```text
Si ya estoy en Home y toco Home otra vez, no se apila otro Home encima.
```

Respuesta teorica posible:

```text
launchSingleTop evita duplicar destinos cuando se navega a una pantalla que ya esta en la parte superior del back stack.
```

## 15. Relacion con MainActivity

`MainActivity` crea el `NavController` y decide cuando mostrar la bottom bar.

```text
MainActivity
-> rememberNavController
-> Scaffold
-> BottomNavigationBar
-> StickrVaultNavHost
```

La bottom bar no se muestra en Login porque `MainActivity` calcula:

```kotlin
val showBottomBar = navBackStackEntry
    ?.destination
    ?.hasRoute<Routes.Login>() != true
```

Entonces:

```text
MainActivity decide si la barra existe.
BottomNavigationBar decide que items muestra.
StickrVaultNavHost decide que pantalla se renderiza.
```

## 16. Cambios de examen probables en navegacion

### Cambiar pantalla inicial

Archivo probable:

```text
StickrVaultNavHost.kt
```

Ubicacion:

```kotlin
val startDestination: Any = if (currentUser != null) Routes.Home else Routes.Login
```

Ejemplo:

```text
Si piden que al iniciar sesion vaya al catalogo, cambiar la navegacion de login exitoso hacia Routes.Catalog.
```

### Cambiar destino despues de login

Archivo probable:

```text
StickrVaultNavHost.kt
```

Ubicacion:

```kotlin
onLoginSuccess = {
    navController.navigate(Routes.Home) {
        popUpTo<Routes.Login> { inclusive = true }
    }
}
```

Cambiar `Routes.Home` por el destino solicitado.

### Cambiar destino despues de logout

Archivo probable:

```text
StickrVaultNavHost.kt
```

Ubicacion:

```kotlin
onLogout = {
    authViewModel.logout()
    navController.navigate(Routes.Login) {
        popUpTo(0) { inclusive = true }
    }
}
```

### Ocultar una opcion de la bottom bar

Archivo probable:

```text
BottomNavigationBar.kt
```

Ejemplo:

```text
Si piden ocultar Reportes, se elimina o condiciona el NavigationBarItem de Routes.Reports.
```

### Cambiar texto o icono de una opcion

Archivo probable:

```text
BottomNavigationBar.kt
```

Ubicaciones:

```kotlin
Icon(Icons.Default.Assessment, contentDescription = "Reportes")
Text("Reportes")
```

### Agregar una nueva pantalla

Archivos probables:

```text
Routes.kt
StickrVaultNavHost.kt
BottomNavigationBar.kt, si debe aparecer en la barra inferior
```

Pasos:

1. Crear nueva ruta en `Routes.kt`.
2. Crear `composable<Routes.NuevaRuta>` en `StickrVaultNavHost.kt`.
3. Navegar hacia esa ruta desde boton o bottom bar.
4. Agregar item en `BottomNavigationBar.kt` si corresponde.

### Cambiar permisos del Scanner

Archivo probable:

```text
StickrVaultNavHost.kt
```

Ubicacion:

```kotlin
val canEditInventory = currentUser?.role == UserRole.WAREHOUSE_CHIEF ||
    currentUser?.role == UserRole.WAREHOUSE_OPERATOR
```

Ejemplo:

```text
Si piden permitir que auditor agregue stock, se ajusta esta condicion.
```

## 17. Explicacion corta para defensa

Version de 30 segundos:

```text
La navegacion esta separada en tres archivos. Routes define los destinos tipados, StickrVaultNavHost conecta cada destino con su pantalla y BottomNavigationBar muestra los accesos inferiores. El NavHost tambien decide la pantalla inicial segun la sesion: si hay usuario va a Home, si no hay usuario va a Login.
```

Version de 1 minuto:

```text
StikerVault usa Navigation Compose con rutas tipadas. En Routes.kt se declaran Login, Home, Catalog, Scanner y Reports como objetos serializables. En StickrVaultNavHost se observa el estado de sesion desde AuthViewModel; mientras la sesion carga se muestra un CircularProgressIndicator, y despues se define el startDestination segun currentUser. Cada composable conecta una ruta con una pantalla y sus callbacks de navegacion. BottomNavigationBar usa el NavController para saber la ruta actual y marcar el item seleccionado, y navega con launchSingleTop para evitar duplicar pantallas.
```

## 18. Checklist para dominar esta fase

- Puedo explicar para que sirve `Routes.kt`.
- Puedo nombrar las rutas actuales de la app.
- Puedo explicar por que se usa `@Serializable`.
- Puedo explicar que hace `StickrVaultNavHost`.
- Puedo explicar como se decide la pantalla inicial.
- Puedo explicar por que se muestra loading antes del NavHost.
- Puedo explicar que hace `popUpTo` despues de login.
- Puedo explicar que hace `popUpTo(0)` despues de logout.
- Puedo explicar que hace `launchSingleTop`.
- Puedo explicar como se marca seleccionado un item de la bottom bar.
- Puedo ubicar donde cambiar texto, icono o destino de una opcion inferior.
- Puedo ubicar donde cambiar permisos del scanner.

## 19. Mini simulacros de esta fase

### Simulacro 1: despues del login ir a Catalogo

Tipo de cambio: navegacion.

Archivo:

```text
StickrVaultNavHost.kt
```

Cambio:

```kotlin
navController.navigate(Routes.Catalog) {
    popUpTo<Routes.Login> { inclusive = true }
}
```

Explicacion:

```text
Se cambia el destino del callback onLoginSuccess. No se modifica LoginScreen porque la pantalla solo avisa que el login fue exitoso; la navegacion se controla desde el NavHost.
```

### Simulacro 2: ocultar Reportes de la bottom bar

Tipo de cambio: UI/navegacion.

Archivo:

```text
BottomNavigationBar.kt
```

Accion:

```text
Eliminar o condicionar el NavigationBarItem de Routes.Reports.
```

Explicacion:

```text
La opcion visible de la barra inferior esta definida en BottomNavigationBar, por eso el cambio se realiza alli.
```

### Simulacro 3: cambiar texto "Catalogo" por "Inventario"

Tipo de cambio: texto visible.

Archivo:

```text
BottomNavigationBar.kt
```

Ubicacion:

```kotlin
Text("Catalogo")
```

Explicacion:

```text
Es un cambio visual de label en la barra inferior, por eso no afecta rutas, ViewModels ni repositorios.
```

### Simulacro 4: permitir que auditor use acciones del scanner

Tipo de cambio: permiso/estado.

Archivo:

```text
StickrVaultNavHost.kt
```

Ubicacion:

```kotlin
val canEditInventory = currentUser?.role == UserRole.WAREHOUSE_CHIEF ||
    currentUser?.role == UserRole.WAREHOUSE_OPERATOR
```

Explicacion:

```text
La variable canEditInventory controla si ScannerScreen permite acciones que modifican inventario. Cambiar esta condicion altera permisos sin tocar la UI del scanner.
```

## 20. Frase clave para recordar

```text
Routes define a donde se puede ir, StickrVaultNavHost decide que pantalla mostrar y BottomNavigationBar ofrece accesos rapidos a las rutas principales.
```
