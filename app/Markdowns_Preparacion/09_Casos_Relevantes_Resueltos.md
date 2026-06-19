# Casos relevantes resueltos por modulo

## Objetivo

Este archivo resume un caso probable por cada modulo estudiado. Cada caso sigue la estructura esperada en defensa:

1. Caso asignado.
2. Cambio realizado.
3. Archivo modificado.
4. Fragmento de codigo.
5. Evidencia esperada.
6. Explicacion teorica.

La idea no es memorizar codigo exacto, sino practicar como ubicar el cambio, modificar lo minimo y explicar por que se hizo en esa capa.

## 1. Entrada Principal

### Caso asignado

Ocultar la barra inferior tambien en la pantalla del scanner para que la camara use mas espacio.

### Cambio realizado

Modificar la condicion `showBottomBar` en `MainActivity.kt` para que sea falsa en `Login` y tambien en `Scanner`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/MainActivity.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
val showBottomBar = navBackStackEntry
    ?.destination
    ?.hasRoute<Routes.Login>() != true
```

Cambio propuesto:

```kotlin
val currentDestination = navBackStackEntry?.destination
val showBottomBar = currentDestination?.hasRoute<Routes.Login>() != true &&
    currentDestination?.hasRoute<Routes.Scanner>() != true
```

### Evidencia esperada

Captura del emulador en la pantalla `Scanner`, mostrando que ya no aparece la barra inferior.

### Explicacion teorica

```text
Modifique MainActivity porque alli se define el Scaffold principal y la condicion global que decide si se muestra la BottomNavigationBar. No cambie ScannerScreen porque la barra inferior no pertenece a esa pantalla, sino al layout principal de la app.
```

## 2. Navegacion

### Caso asignado

Despues de iniciar sesion, enviar al usuario directamente al catalogo en lugar de Home.

### Cambio realizado

Cambiar el destino de navegacion dentro del callback `onLoginSuccess`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/navigation/StickrVaultNavHost.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
onLoginSuccess = {
    navController.navigate(Routes.Home) {
        popUpTo<Routes.Login> { inclusive = true }
    }
}
```

Cambio propuesto:

```kotlin
onLoginSuccess = {
    navController.navigate(Routes.Catalog) {
        popUpTo<Routes.Login> { inclusive = true }
    }
}
```

### Evidencia esperada

Captura del emulador despues de login exitoso, mostrando la pantalla de catalogo como primera pantalla interna.

### Explicacion teorica

```text
Modifique StickrVaultNavHost porque alli se conectan las rutas y callbacks de navegacion. LoginScreen solo informa que el login fue exitoso; el NavHost decide hacia que ruta ir.
```

## 3. Autenticacion y Sesion

### Caso asignado

Validar que el correo ingresado contenga `@` antes de intentar iniciar sesion.

### Cambio realizado

Agregar una validacion en `AuthViewModel.login`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/auth/AuthViewModel.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
if (email.isBlank()) {
    _uiState.value = AuthUiState.Error("Ingresa un correo valido")
    return
}
```

Cambio propuesto:

```kotlin
val cleanEmail = email.trim()

if (cleanEmail.isBlank()) {
    _uiState.value = AuthUiState.Error("Ingresa un correo valido")
    return
}

if (!cleanEmail.contains("@")) {
    _uiState.value = AuthUiState.Error("El correo debe contener @")
    return
}
```

Y luego usar:

```kotlin
val user = loginUseCase(cleanEmail)
```

### Evidencia esperada

Captura del emulador mostrando el mensaje `El correo debe contener @` al intentar ingresar un texto invalido.

### Explicacion teorica

```text
La validacion se agrego en AuthViewModel porque es una regla de presentacion del login. LoginScreen solo captura el texto y envia el evento; el ViewModel decide si el dato es valido y actualiza el estado de error.
```

## 4. Catalogo e Inventario

### Caso asignado

Cambiar el margen externo de las cards de productos.

### Cambio realizado

Modificar el `Modifier.padding` del `Card` dentro de `ProductCard`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/catalog/CatalogScreen.kt
```

### Fragmento de codigo

Codigo actual:

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

Cambio propuesto:

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 12.dp),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
)
```

### Evidencia esperada

Captura del catalogo mostrando mayor separacion vertical entre cards de productos.

### Explicacion teorica

```text
El cambio se hizo en ProductCard porque el caso solo afecta la presentacion visual de cada producto. No fue necesario tocar ViewModel, repositorios ni base de datos porque no cambia datos ni reglas de negocio.
```

## 5. Home

### Caso asignado

Mostrar 10 movimientos recientes en Home en lugar de 5.

### Cambio realizado

Cambiar el limite usado por `GetStockMovementsUseCase`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/home/HomeViewModel.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
val movements = getStockMovementsUseCase(limit = 5)
```

Cambio propuesto:

```kotlin
val movements = getStockMovementsUseCase(limit = 10)
```

### Evidencia esperada

Captura del Home con una lista mas larga de actividad reciente, si existen suficientes movimientos.

### Explicacion teorica

```text
El limite de movimientos pertenece al ViewModel porque afecta cuantos datos se solicitan para construir el estado de Home. La pantalla solo renderiza la lista que recibe en HomeUiState.
```

## 6. Reportes

### Caso asignado

Cambiar la grilla de metricas de reportes para que tenga una sola columna.

### Cambio realizado

Cambiar `GridCells.Fixed(2)` por `GridCells.Fixed(1)`.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/reports/ReportsScreen.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier.height(220.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    userScrollEnabled = false
)
```

Cambio propuesto:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(1),
    modifier = Modifier.height(420.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    userScrollEnabled = false
)
```

### Evidencia esperada

Captura de Reportes mostrando las metricas apiladas en una sola columna.

### Explicacion teorica

```text
La cantidad de columnas es un cambio visual de layout, por eso se modifica ReportsScreen. Tambien se ajusta la altura para que las cuatro metricas puedan mostrarse sin quedar comprimidas.
```

## 7. Scanner y OCR

### Caso asignado

Aceptar codigos OCR con guion, por ejemplo `ARG-12`, ademas del formato actual `ARG 12`.

### Cambio realizado

Modificar la expresion regular del extractor OCR.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/presentation/scanner/OcrCodeExtractor.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
private val PANINI_CODE = Regex("""^[A-Z]{2,4}\s+\d{1,3}$""")
```

Cambio propuesto:

```kotlin
private val PANINI_CODE = Regex("""^[A-Z]{2,4}[\s-]+\d{1,3}$""")
```

### Evidencia esperada

Captura del scanner mostrando que un codigo con guion fue reconocido. Si no se puede usar camara en el momento, evidenciar el fragmento de codigo modificado y explicar el caso de prueba teorico: `ARG-12` ahora coincide con la regex.

### Explicacion teorica

```text
La regla de reconocimiento del codigo esta centralizada en OcrCodeExtractor. ScannerScreen solo muestra la UI y ScannerViewModel procesa el resultado, pero el formato valido del codigo se decide en la regex del extractor.
```

## 8. Capa de Datos

### Caso asignado

Ordenar localmente los productos por stock ascendente en lugar de nombre.

### Cambio realizado

Modificar la consulta Room que obtiene todos los productos.

### Archivo modificado

```text
app/src/main/java/com/example/myapplication/data/local/dao/ProductDao.kt
```

### Fragmento de codigo

Codigo actual:

```kotlin
@Query("SELECT * FROM products ORDER BY name ASC")
suspend fun getAllProducts(): List<ProductEntity>
```

Cambio propuesto:

```kotlin
@Query("SELECT * FROM products ORDER BY currentStock ASC")
suspend fun getAllProducts(): List<ProductEntity>
```

### Evidencia esperada

Captura del catalogo o reporte mostrando productos ordenados desde menor stock hacia mayor stock cuando se usan datos locales.

### Explicacion teorica

```text
La consulta local se modifica en ProductDao porque Room obtiene los productos desde la tabla products. Este cambio afecta el orden cuando la app usa datos locales. Si se quisiera cambiar el orden final siempre, tambien habria que revisar el sortedBy del repositorio.
```

## Checklist final para cualquier caso

Antes de entregar evidencia:

- La app compila.
- El cambio se ve en el emulador o se justifica con una prueba clara.
- La captura muestra la pantalla afectada.
- El fragmento de codigo corresponde al archivo correcto.
- La explicacion menciona la capa modificada.
- La explicacion aclara por que no se tocaron otras capas.

## Frase final para defensa

```text
Identifique el tipo de cambio, ubique la capa responsable, modifique el archivo minimo necesario, verifique el resultado en el emulador y prepare la explicacion tecnica relacionando pantalla, estado, ViewModel, datos o navegacion segun correspondia.
```
